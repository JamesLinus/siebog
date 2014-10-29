importScripts("/siebog/radigost.js");

function ClientServerAgent() {};

ClientServerAgent.prototype = new Agent();

ClientServerAgent.prototype.onMessage = function(msg) {
	var reply = ACLMessage.makeReply(msg, ACLPerformative.INFORM, this.aid);
	this.post(reply);
};

setAgentInstance(new ClientServerAgent());