importScripts("../../client/agent.js");
importScripts("../../client/fipaacl.js");

// global constants
var Epsilon = 0.0001;
var Reward_Win = 10;
var Reward_Step = -0.1;
var LearningRate = 0.8;
var DiscountRate = 0.9;
var LearningRate_1 = 1.0 - LearningRate;

function Predator() {
};

Predator.prototype = new Agent();

Predator.prototype.onInit = function(args) {
	this.pos = {}; // my current position

	// actions: id, delta x, delta y
	this.actions = {
		u : { id : "u", dx :  0, dy : -1 }, // up
		r : { id : "r",	dx :  1, dy :  0 }, // right
		d : { id : "d",	dx :  0, dy :  1 }, // down
		l : { id : "l",	dx : -1, dy :  0 }, // left
		s : { id : "s", dx :  0, dy :  0 }  // stay
	};

	// returns a list of valid moves
	this.getValidActions = function(px, py) {
		// if the prey is near by, go directly there
		for (a in this.actions) {
			var act = this.actions[a];
			if ((this.pos.x + act.dx == px) && (this.pos.y + act.dy == py))
				return [ act.id ];
		}
		// return all valid actions
		var valid = [];
		for (a in this.actions) {
			var act = this.actions[a];
			var x = this.pos.x + act.dx;
			var y = this.pos.y + act.dy;
			if ((x >= 0) && (x < this.dim) && (y >= 0) && (y < this.dim))
				valid.push(act.id);
		}

		return valid;
	};

	this.hardReset = function() {
		this.q = {}; // Q values
		this.eg = 0.4; // e-greedy constant
		// information about the previous action
		this.hasPrev = false;
		this.prevStateAct = null;
	};

	this.getQ = function(state, action) {
		var val = this.q[state + action];
		if (typeof val === "undefined") {
			this.q[state + action] = 0;
			return 0;
		}
		return val;
	};

	this.hardReset();
};

Predator.prototype.onMessage = function(msg) {
	switch (msg.performative) {
	case ACLPerformative.INFORM:
		// reset
		this.pos.x = msg.content.x;
		this.pos.y = msg.content.y;
		this.dim = msg.content.dim;
		if (msg.content.hr) // hard-reset?
			this.hardReset();
		break;
	case ACLPerformative.REQUEST:
		this.move(msg.content.px, msg.content.py);
		break;
	}
};

Predator.prototype.move = function(px, py) {
	var valid = this.getValidActions(px, py);
	// create state description
	var x = (px - this.pos.x) + this.dim - 1;
	var y = (py - this.pos.y) + this.dim - 1;
	var state = "$" + x + "_" + y + "_";

	var maxA;
	if (Math.random() < this.eg) {
		var n = Math.floor(Math.random() * valid.length);
		maxA = valid[n];
	} else {
		maxA = valid[0];
		var mx = this.getQ(state, maxA);

		for ( var i = 1; i < valid.length; i++) {
			var val = this.getQ(state, valid[i]);
			if (val > mx) {
				mx = val;
				maxA = valid[i];
			}
		}
	}
	this.eg -= 0.05;

	// win?
	if ((this.pos.x == px) && (this.pos.y == py))
		this.q[state + maxA] = Reward_Win;

	// update previous Q-value
	if (this.hasPrev)
		this.q[this.prevStateAct] = LearningRate_1 * this.q[this.prevStateAct] + LearningRate
				* (Reward_Step + DiscountRate * this.getQ(state, maxA));

	// remember the state-action pair
	this.prevStateAct = state + maxA;
	this.hasPrev = true;

	// advance
	this.pos.x += this.actions[maxA].dx;
	this.pos.y += this.actions[maxA].dy;

	// inform
	this.onStep(this.pos);
};

self.__AGENT_INSTANCE__ = new Predator();