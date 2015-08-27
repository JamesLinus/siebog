importScripts("/siebog/radigost/radigost.js");

function RemoteAgent() {
	this.sendMessageToRemoteAgent = function(rcvdMsg) {
		var msgToSend = new ACLMessage(ACLPerformative.INFORM);
		msgToSend.sender = this.aid;
		msgToSend.receivers = [ rcvdMsg.content.target ];
		msgToSend.content = rcvdMsg.content.content;
		this.post(msgToSend);
	};
	
	this.showReceivedMessage = function(rcvdMsg) {
		this.onStep(rcvdMsg);
	};
};

RemoteAgent.prototype = new Agent();

RemoteAgent.prototype.onMessage = function(msg) {
	if (msg.performative === ACLPerformative.REQUEST) {
		this.sendMessageToRemoteAgent(msg);
	} else {
		this.showReceivedMessage(msg);
	}
};

setAgentInstance(new RemoteAgent());