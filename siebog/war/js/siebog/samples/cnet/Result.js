importScripts("../../client/agent.js");

function Result() { }

Result.prototype = new Agent();

Result.prototype.onMessage = function(msg) {
	this.onStep(msg.content);
};

self.__AGENT_INSTANCE__ = new Result();