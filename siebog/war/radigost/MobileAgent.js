importScripts("/siebog/radigost/radigost.js");

function MobileAgent() {
	this.num = 42;
};

MobileAgent.prototype = new Agent();

MobileAgent.prototype.onInit = function(args) {
	this.moveToServer();
};

MobileAgent.prototype.onArrived = function(host, isServer) {
	if (isServer) {
		this.isServer = true;
		print("I'm at " + host + " and the number is " + this.num);
	}
}

MobileAgent.prototype.onMessage = function(msg) {
	if (this.isServer) {
		print("I'm on the server and I received the following message:\n" + msg)
	}
}

setAgentInstance(new MobileAgent());
