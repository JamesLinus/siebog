function PredatorPreyApp() {
	this.mapSize = 15;
	this.numPredators = 4;
	this.epCount = 20;
	this.triCount = 1000;

	this.running = false;

	this.epIndex = 0;
	this.epTime = new Array(this.epCount);
	for ( var i = 0; i < this.epCount; i++)
		this.epTime[i] = 0;
	this.triIndex = 0;
	this.startTime = 0;

	this.domTrialNum = document.getElementById("trialNum");
	this.domEpNum = document.getElementById("epNum");
	this.domLog = document.getElementById("log");

	// create predators
	this.predators = {}; // aid.value = PInfo
	for ( var i = 0; i < this.numPredators; i++) {
		var aid_ = radigost.runAgent("Pred_" + i, "Predator.js", this);
		this.predators[aid_.value] = {
			aid : aid_
		};
	}
};

PredatorPreyApp.prototype = new AgentListener();

PredatorPreyApp.prototype.reset = function() {
	var sz = this.mapSize;

	// initial prey position
	this.prey = {
		x : (sz - 1) / 2,
		y : (sz - 1) / 2
	};

	// initial positions of predators
	var pos = [];
	pos.push({ x : 0,      y : 0 });
	pos.push({ x : sz - 1, y : 0 });
	pos.push({ x : sz - 1, y : sz - 1});
	pos.push({ x : 0,      y : sz - 1});
	
	// (re)initialize each predator
	var hardReset = this.epIndex == 0;
	var i = 0;
	for ( var p in this.predators) {
		var info = this.predators[p];
		info.x = pos[i].x;
		info.y = pos[i].y;

		// send the 'reset' message
		var msg = new ACLMessage(ACLPerformative.INFORM);
		msg.receivers.push(info.aid);
		msg.content = {
			x : pos[i].x,
			y : pos[i].y,
			dim : sz,
			hr : hardReset
		};
		radigost.post(msg);

		++i;
	}

	this.predatorMoves = 0;
};

PredatorPreyApp.prototype.start = function() {
	if (this.running)
		return;
	this.running = true;
	this.epIndex = 0;
	this.runEpisode();
};

PredatorPreyApp.prototype.runEpisode = function() {
	this.reset();
	this.startTime = new Date().getTime();
	this.signalMove();
};

PredatorPreyApp.prototype.onStep = function(aid, msg) {
	if (!this.running)
		return;
	++this.predatorMoves;
	// remember new position
	this.predators[aid.value].x = msg.x;
	this.predators[aid.value].y = msg.y;
	
	// got response from all predators?
	if (this.predatorMoves % this.numPredators == 0) {
		var tm = new Date().getTime() - this.startTime;
		// do we have a winner?
		for ( var p in this.predators) {
			if ((this.predators[p].x == this.prey.x) && (this.predators[p].y == this.prey.y)) { 
				this.epTime[this.epIndex++] += tm;
				// restart episode?
				if (this.epIndex < this.epCount)
					this.runEpisode();
				else {
					this.domTrialNum.textContent = "Trial " + this.triIndex + " done";
					if (++this.triIndex < this.triCount) {
						this.epIndex = 0;
						this.runEpisode();
					} else {
						this.stop();
						// print results
						var res = "";
						for ( var i = 0; i < this.epCount; i++) {
							this.epTime[i] /= this.triCount;
							res += this.epTime[i] + "\n";
						}
						this.domLog.value = res.replace(/[.]/gi, ",");
					}
				}
				return;
			}
		} // for
		
		// no winner, move the prey
		var newX = this.prey.x + 1 - Math.floor(Math.random() * 3);
		var newY = this.prey.y + 1 - Math.floor(Math.random() * 3);
		if ((newX >= 0) && (newX < this.mapSize))
			this.prey.x = newX;
		if ((newY >= 0) && (newY < this.mapSize))
			this.prey.y = newY;
		// instruct predators to make the next move
		this.signalMove();
	}
};

PredatorPreyApp.prototype.signalMove = function() {
	var msg = new ACLMessage(ACLPerformative.REQUEST);
	msg.content = {};
	msg.content.px = this.prey.x;
	msg.content.py = this.prey.y;
	for ( var i in this.predators)
		msg.receivers.push(this.predators[i].aid);
	radigost.post(msg);
};

PredatorPreyApp.prototype.stop = function() {
	if (!this.running)
		return;
	this.running = false;
	for ( var p in this.predators)
		radigost.stopAgent(this.predators[p].aid);
};

PredatorPreyApp.prototype.log = function(msg) {
	document.getElementById("log").value += msg + "\n";
};