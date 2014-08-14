importScripts("../../client/agent.js");
importScripts("../../client/fipaacl.js");

function Receiver() {
	this.process = function() {
		var primes = 0;
		
		for (var i = 1; i <= this.limit; i++) {
			var j = 2;
			while (j <= i) {
				if (i % j == 0)
					break;
				j++;
			}
			if (j == i)
				primes++;
		}
		
		return primes;
	};
}

Receiver.prototype = new Agent();

Receiver.prototype.onInit = function(args) {
	this.limit = args.limit;
};

Receiver.prototype.onMessage = function(msg) {
	var r = ACLMakeReply(msg, ACLPerformative.INFORM, this.aid);
	r.content = msg.content + this.process();
	postMessage(r);
};

self.__AGENT_INSTANCE__ = new Receiver();