importScripts("/siebog/radigost.js");

function HelloWorld() {
};

HelloWorld.prototype = new Agent();

HelloWorld.prototype.onMessage = function(msg) {
	this.onStep("Hello from " + this.aid.str + "!");
};

self.agentInstance = new HelloWorld();
