importScripts("../../client/agent.js");

function HelloWorld() {
};

HelloWorld.prototype = new Agent();

HelloWorld.prototype.onMessage = function(msg) {
	this.onStep("Hello, " + msg.content + "!");
};

self.__AGENT_INSTANCE__ = new HelloWorld();