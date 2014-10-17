function XJAF() {
	this.agm = "/siebog/rest/agents"; // agent manager
	this.msm = "/siebog/rest/messages"; // message manager
};

XJAF.getAgClasses = function(onSuccess, onError) {
	$.ajax(this.agm + "/classes", {
		type : "GET",
		dataType : "json",
		success : onSuccess,
		error : onError
	});
};

XJAF.getRunning = function(onSuccess, onError) {
	$.ajax(this.agm + "/running", {
		type : "GET",
		dataType : "json",
		success : onSuccess,
		error : onError
	});
};

XJAF.start = function(agClass, name, onSuccess, onError) {
	$.ajax((this.agm + "/running/" + agClass + "/" + name)({
		type : "PUT",
		contentType : "application/x-www-form-urlencoded; charset=UTF-8",
		success : onSuccess,
		error : onError
	}));
};

XJAF.getPerformatives = function(onSuccess, onError) {
	$.ajax(this.msm + "/messages", {
		type : "GET",
		dataType : "json",
		success : onSuccess,
		error : onError
	});
};

XJAF.post = function(msg, onSuccess, onError) {
	$.ajax(this.msm + "/messages", {
		type : "POST",
		contentType : "application/x-www-form-urlencoded; charset=UTF-8",
		success : onSuccess,
		error : onError
	});
};

XJAF.accept = function(url, aid, state, onSuccess, onError) {
	$.ajax({
		url : "/siebog/rest/radigost",
		type : "PUT",
		contentType : "application/x-www-form-urlencoded; charset=UTF-8",
		data : {
			url : url,
			aid : aid,
			state : state
		},
		success : onSuccess,
		error : onError
	});
};

function AID(name, hap, remote) {
	this.name = name;
	this.hap = hap;
	this.str = "" + this.name + "@" + this.hap;
	// is this agent located on the server (or in my web page)?
	this.remote = typeof remote !== "undefined" && remote !== null ? true : false;
}

function AgentObserver() {
}
AgentObserver.prototype.onStart = function(aid) {
};
AgentObserver.prototype.onStep = function(aid, msg) {
};
AgentObserver.prototype.onStop = function(aid) {
};

function OpCode() {
}
OpCode.INIT = 1;
OpCode.STEP = 2;
OpCode.MOVE_TO_SERVER = 3;

function Radigost(hap) {
	this.hap = hap;
	this.running = {};

	this.start = function(url, name, agentObserver, agentInitArgs) {
		var newAid = new AID(name, this.hap);
		if (this.getAgent(newAid) == null) {
			var agent = {};
			agent.url = url;
			agent.observer = agentObserver;
			agent.worker = new Worker(url);
			var self = this;
			agent.worker.onmessage = function(ev) {
				var msg = ev.data;
				switch (msg.opcode) {
				case OpCode.INIT:
					var ag = self.getAgent(msg.aid);
					if (ag !== null && ag.observer !== null)
						ag.observer.onStart(msg.aid);
					break;
				case OpCode.STEP:
					var ag = self.getAgent(msg.aid);
					if (ag !== null && ag.observer !== null)
						ag.observer.onStep(msg.aid, msg.info);
					break;
				case OpCode.MOVE_TO_SERVER:
					var ag = self.getAgent(msg.aid);
					if (ag !== null && ag.url !== null)
						XJAF.accept(ag.url, msg.aid.str, msg.state);
					break;
				default:
					alert("Unrecognized OpCode: " + JSON.stringify(msg));
				}
			};
			agent.worker.onerror = function(ev) {
			};
			this.putAgent(newAid, agent);
			// initialize it
			msg = {
				opcode : OpCode.INIT,
				aid : newAid,
				args : agentInitArgs
			};
			agent.worker.postMessage(msg);
		}
		return newAid;
	};

	this.post = function(msg) {
		for (i = 0, len = msg.receivers.length; i < len; i++) {
			var aid = msg.receivers[i];
			var ag = this.getAgent(aid);
			if (ag !== null && ag.worker !== null) 
				ag.worker.postMessage(msg);
		}
	};

	this.getAgent = function(aid) {
		if (this.running.hasOwnProperty(aid.str))
			return this.running[aid.str];
		return null;
	};

	this.putAgent = function(aid, agent) {
		this.running[aid.str] = agent;
	};
}

function ACLPerformative() {
}
ACLPerformative.ACCEPT_PROPOSAL = 0;
ACLPerformative.AGREE = 1;
ACLPerformative.CANCEL = 2;
ACLPerformative.CFP = 3;
ACLPerformative.CONFIRM = 4;
ACLPerformative.DISCONFIRM = 5;
ACLPerformative.FAILURE = 6;
ACLPerformative.INFORM = 7;
ACLPerformative.INFORM_IF = 8;
ACLPerformative.INFORM_REF = 9;
ACLPerformative.NOT_UNDERSTOOD = 10;
ACLPerformative.PROPOSE = 11;
ACLPerformative.QUERY_IF = 12;
ACLPerformative.QUERY_REF = 13;
ACLPerformative.REFUSE = 14;
ACLPerformative.REJECT_PROPOSAL = 15;
ACLPerformative.REQUEST = 16;
ACLPerformative.REQUEST_WHEN = 17;
ACLPerformative.REQUEST_WHENEVER = 18;
ACLPerformative.SUBSCRIBE = 19;
ACLPerformative.PROXY = 20;
ACLPerformative.PROPAGATE = 21;
ACLPerformative.UNKNOWN = -1;

function ACLMessage(performative) {
	this.performative = performative;
	this.receivers = [];
	// since this object is going to be sent to web workers and back, we cannot
	// have any functions here
}

ACLMessage.makeReply = function(msg, performative, sender) {
	var reply = new ACLMessage(performative);
	reply.sender = sender;
	if (msg.replyTo != null)
		reply.receivers.push(msg.replyTo);
	else
		reply.receivers.push(msg.sender);
	reply.language = msg.language;
	reply.ontology = msg.ontology;
	reply.encoding = msg.encoding;
	reply.protocol = msg.protocol;
	reply.conversationId = msg.conversationId;
	reply.inReplyTo = msg.replyWith;
	return reply;
};

function Agent() {
	this.aid = null;
}

Agent.prototype.post = function(msg) {
	self.postMessage(msg);
};

Agent.prototype.onInit = function(args) {
};

Agent.prototype.onMessage = function(msg) {
};

Agent.prototype.onStep = function(step) {
	var msg = {
		opcode : OpCode.STEP,
		aid : this.aid,
		info : step
	};
	this.post(msg);
};

Agent.prototype.getState = function() {
};

Agent.prototype.moveToServer = function() {
	var agState = this.getState();
	var msg = {
		opcode : OpCode.MOVE_TO_SERVER,
		aid : this.aid,
		state : agState
	};
	this.post(msg);
};

self.agentInstance = null;

self.onmessage = function(ev) {
	var msg = ev.data;
	if (msg.opcode === OpCode.INIT) {
		self.agentInstance.aid = msg.aid;
		self.agentInstance.onInit(msg.args);
		var initMsg = {
			opcode : OpCode.INIT,
			aid : msg.aid
		};
		postMessage(initMsg);
	} else
		return self.agentInstance.onMessage(msg);
};
