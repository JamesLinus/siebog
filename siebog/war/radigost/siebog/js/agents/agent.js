function OpCode() {
}
OpCode.INIT = 1;
OpCode.STEP = 2;
OpCode.MOVE_TO_SERVER = 3;

function Agent() {
	this.aid = null;
	this.radigostHelper = null;
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

function getAgentInstance() {
	return self.agentInstance;
}

function setAgentInstance(agent) {
	self.agentInstance = agent;
}

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
	} else {
		checkPreconditions(msg.interceptor);
		self.agentInstance.onMessage(msg);
		checkPostconditions(msg.interceptor);
	}
};

function checkPreconditions(interceptor) {
	if (typeof interceptor !== "undefined"
			&& typeof interceptor.preconditions !== "undefined") {
		assertState(interceptor.preconditions, self.agentInstance);
		console.log("Pre-conditions for the agent satisfied.");
	}
}

function checkPostconditions(interceptor) {
	if (typeof interceptor !== "undefined") {
		if (typeof interceptor.postconditions === "undefined") {
			throw new Error("Interceptors must include post-conditions.");
		}
		assertState(interceptor.postconditions, self.agentInstance);
		console.log("Post-conditions for the agent satisfied.");
	}
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
