importScripts("agent.js");

function HelloWorld() {
	this.messageProcessed = false;
};

HelloWorld.prototype = new Agent();

HelloWorld.prototype.onMessage = function(msg) {
	this.messageProcessed = true;
	this.onStep("Hello from " + this.aid.str + "!");
};

setAgentInstance(new HelloWorld());
