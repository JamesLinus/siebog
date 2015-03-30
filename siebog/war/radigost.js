function XJAF() {
};
XJAF.agm = "/siebog/rest/agents"; // agent manager
XJAF.msm = "/siebog/rest/messages"; // message manager

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

XJAF.start = function(agClass, name, args, onSuccess, onError) {
	$.ajax(XJAF.agm + "/running/" + agClass + "/" + name, {
		type : "PUT",
		contentType : "application/x-www-form-urlencoded; charset=UTF-8",
		data : args,
		success : onSuccess,
		error : onError
	});
};

XJAF.getPerformatives = function(onSuccess, onError) {
	$.ajax(XJAF.msm, {
		type : "GET",
		dataType : "json",
		success : onSuccess,
		error : onError
	});
};

XJAF.post = function(msg, onSuccess, onError) {
	$.ajax(XJAF.msm, {
		type : "POST",
		contentType : "application/x-www-form-urlencoded; charset=UTF-8",
		data : "acl=" + JSON.stringify(msg),
		success : onSuccess,
		error : onError
	});
};

XJAF.accept = function(url, aid, state, onSuccess, onError) {
	$.ajax({
		url : "/siebog/rest/webclient",
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

function AID(name, hap) {
	this.name = name;
	this.hap = hap;
	this.radigost = true;
	this.str = "" + this.name + "@" + this.hap;
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

function WebClientOpCode() {
}
WebClientOpCode.REGISTER = 'r';
WebClientOpCode.DEREGISTER = 'd';
WebClientOpCode.NEW_AGENT = 'a';

function WebClientSocket(radigost) {
	this.radigost = radigost;

	var url = "ws://" + window.location.host + "/siebog/webclient";
	this.socket = new WebSocket(url);
	var self = this;
	this.socket.onmessage = function(e) {
		var msg = JSON.parse(e.data);
		if (typeof msg.sender === "string")
			msg.sender = JSON.parse(msg.sender);
		if (typeof msg.replyTo === "string")
			msg.replyTo = JSON.parse(msg.replyTo);
		for (var i = 0, len = msg.receivers.length; i < len; i++)
			if (typeof msg.receivers[i] === "string")
				msg.receivers[i] = JSON.parse(msg.receivers[i]);
		self.radigost.post(msg);
	};
	this.socket.onopen = function(e) {
		self.socket.send(WebClientOpCode.REGISTER + self.radigost.hap);
	};
	this.socket.onclose = function(e) {
		// self.socket = null;
		console.log("WebSocket connection closed.");
	};
	this.socket.onerror = function(e) {
		console.log("WebSocket connection error: " + e.data);
	};
};

function Radigost(hap) {
	this.hap = hap;
	this.running = {};
	this.socket = new WebClientSocket(this);

	var self = this;
	var onWorkerMessage = function(ev) {
		var msg = ev.data;
		if (typeof msg.opcode === "undefined") // a regular message
			self.post(msg);
		else {
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
				console.log("Unrecognized OpCode: " + JSON.stringify(msg));
			}
		}
	};

	this.start = function(url, name, agentObserver, agentInitArgs) {
		var newAid = new AID(name, this.hap);
		if (this.getAgent(newAid) == null) {
			var agent = {};
			agent.url = url;
			agent.observer = agentObserver ? agentObserver : null;
			agent.worker = new Worker(url);
			agent.worker.onmessage = onWorkerMessage;
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
			// create the server-side stub
			var agClass = "siebog$RadigostStub";
			XJAF.start(agClass, name, "arg[host].value=" + this.hap);
		}
		return newAid;
	};

	this.post = function(msg) {
		var server = [];
		for (var i = 0, j = 0, len = msg.receivers.length; i < len; i++) {
			var aid = msg.receivers[i];
			if (aid.radigost) {
				var ag = this.getAgent(aid);
				if (ag !== null && ag.worker !== null)
					ag.worker.postMessage(msg);
			} else {
				server[j++] = aid;
			}
		}
		// send to server?
		if (server.length > 0) {
			msg.receivers = server;
			XJAF.post(msg);
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
ACLPerformative.ACCEPT_PROPOSAL = "ACCEPT_PROPOSAL";
ACLPerformative.AGREE = "AGREE";
ACLPerformative.CANCEL = "CANCEL";
ACLPerformative.CFP = "CFP";
ACLPerformative.CONFIRM = "CONFIRM";
ACLPerformative.DISCONFIRM = "DISCONFIRM";
ACLPerformative.FAILURE = "FAILURE";
ACLPerformative.INFORM = "INFORM";
ACLPerformative.INFORM_IF = "INFORM_IF";
ACLPerformative.INFORM_REF = "INFORM_REF";
ACLPerformative.NOT_UNDERSTOOD = "NOT_UNDERSTOOD";
ACLPerformative.PROPOSE = "PROPOSE";
ACLPerformative.QUERY_IF = "QUERY_IF";
ACLPerformative.QUERY_REF = "QUERY_REF";
ACLPerformative.REFUSE = "REFUSE";
ACLPerformative.REJECT_PROPOSAL = "REJECT_PROPOSAL";
ACLPerformative.REQUEST = "REQUEST";
ACLPerformative.REQUEST_WHEN = "REQUEST_WHEN";
ACLPerformative.REQUEST_WHENEVER = "REQUEST_WHENEVER";
ACLPerformative.SUBSCRIBE = "SUBSCRIBE";
ACLPerformative.PROXY = "PROXY";
ACLPerformative.PROPAGATE = "PROPAGATE";
ACLPerformative.UNKNOWN = "UNKNOWN";

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
	this.radigostHelper = null;

	this.getRadigostHelper = function() {
		if (this.radigostHelper == null) {
			importClass(Packages.siebog.xjaf.radigostlayer.RadigostHelper);
			this.radigostHelper = Packages.siebog.xjaf.radigostlayer.RadigostHelper;
		}
		return this.radigostHelper;
	};
}

Agent.prototype.post = function(msg) {
	self.postMessage(msg);
};

Agent.prototype.onInit = function(args) {
};

Agent.prototype.onMessage = function(msg) {
};

Agent.prototype.onArrived = function(host, isServer) {
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
	var state = {};
	for ( var prop in this)
		if (typeof this[prop] !== "function")
			state[prop] = this[prop];
	return state;
};

Agent.prototype.setState = function(state) {
	var st = typeof state === "string" ? JSON.parse(state) : state;
	for ( var prop in st)
		this[prop] = st[prop];
};

Agent.prototype.moveToServer = function() {
	var agState = this.getState();
	var msg = {
		opcode : OpCode.MOVE_TO_SERVER,
		aid : this.aid,
		state : JSON.stringify(agState)
	};
	this.post(msg);
};

/** * Web Worker ** */

if (typeof self === "undefined")
	self = new Object(); // needed for the JS scripting engine on the server

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
		self.interceptor = msg.interceptor;
	} else {
		if (typeof msg.interceptor !== "undefined"
				&& typeof msg.interceptor.preconditions !== "undefined") {
			assertState(msg.interceptor.preconditions, self.agentInstance);
			console.log("Pre-conditions for the MessagingTest agent satisfied.");
		}
		self.agentInstance.onMessage(msg);
		if (typeof msg.interceptor !== "undefined") {
			if (typeof msg.interceptor.postconditions === "undefined") {
				throw new Error("Interceptors must include post-conditions.");
			}
			assertState(msg.interceptor.postconditions, self.agentInstance);
			console.log("Post-conditions for the MessagingTest agent satisfied.");
		}
	}
};

function getAgentInstance() {
	return self.agentInstance;
}

function setAgentInstance(agent) {
	self.agentInstance = agent;
}

function assertState(expected, actual) {
	for ( var key in expected) {
		if (!actual.hasOwnProperty(key)) {
			throw new Error("Property " + key + " not found.");
		}
		if (expected[key] !== actual[key]) {
			var msg = "Mismatched property " + key + ", expected:"
					+ expected[key] + ", actual:" + actual[key] + "."
			throw new Error(msg);
		}
	}
}
