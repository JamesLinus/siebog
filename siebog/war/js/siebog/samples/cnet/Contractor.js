importScripts("../../client/agent.js");
importScripts("../../client/fipaacl.js");
importScripts("../../client/interactions.js");

function Contractor() {
	this.process = function(content) {
		var sum = 0;
		for (var i = 0; i < content.length; i++)
			sum += content.charCodeAt(i);
		return "" + sum;
	};
}

Contractor.prototype = new CNetContractor();

Contractor.prototype.getProposal = function(cfp) {
	var proposal = ACLMakeReply(cfp, ACLPerformative.PROPOSE, this.aid);
	proposal.content = this.process(cfp.content);
	return proposal;
};

Contractor.prototype.onAcceptProposal = function(msg) {
	var result = ACLMakeReply(msg, ACLPerformative.INFORM, this.id);
	result.content = this.process(msg.content);
	return result;
};

self.__AGENT_INSTANCE__ = new Contractor();