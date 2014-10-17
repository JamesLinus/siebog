importScripts("/siebog/radigost.js");

function MobileAgent() {
};

MobileAgent.prototype = new Agent();

MobileAgent.prototype.onInit = function(args) {
	this.moveToServer();
};

self.agentInstance = new MobileAgent();
