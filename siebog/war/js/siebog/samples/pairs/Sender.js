importScripts("../../client/agent.js");
importScripts("../../client/fipaacl.js");

function Sender() { }

Sender.prototype = new Agent();

Sender.prototype.onInit = function(args) {
	this.index = args.index;
	this.numIter = args.numIter;
	
	this.sendMsg = function(content, ts) {
		var msg = new ACLMessage(ACLPerformative.REQUEST);
		msg.sender = this.aid;
		var r = new AID("R" + this.index, this.aid.device, false);
		msg.receivers.push(r);
		msg.content = content;
		msg.replyWith = ts;
		postMessage(msg);
	};	
};

Sender.prototype.onMessage = function(msg) {
	if (msg.performative === ACLPerformative.REQUEST) {
		this.iterIndex = 0;
		this.sendMsg(msg.content, new Date().getTime());
	} else {
		++this.iterIndex;
		if (this.iterIndex < this.numIter)
			this.sendMsg(msg.content, msg.inReplyTo);
		else {
			var tm = new Date().getTime() - msg.inReplyTo;
			this.onStep(tm / this.numIter);
		}
	}
};

self.__AGENT_INSTANCE__ = new Sender();