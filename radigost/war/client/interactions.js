importScripts("/radigost/client/agent.js");
importScripts("/radigost/client/fipaacl.js");

/**** FIPA Contract Net interaction protocol ****/
/**** http://www.fipa.org/specs/fipa00029/SC00029H.html ****/

/**
 * Participant/Contractor agent.
 */
function CNetContractor() {
};

CNetContractor.prototype = new Agent();

CNetContractor.prototype.getProposal = function(cfp) {
	return null;
};

CNetContractor.prototype.onAcceptProposal = function(msg) {
	return null;
};

CNetContractor.prototype.onRejectProposal = function(msg) {
};

CNetContractor.prototype.onMessage = function(msg) {
	switch (msg.performative) {
	case ACLPerformative.CFP:
		var reply = this.getProposal(msg);
		if ((typeof reply !== "undefined") && (reply != null))
			postMessage(reply);
		break;
	case ACLPerformative.ACCEPT_PROPOSAL:
		var result = this.onAcceptProposal(msg);
		if ((typeof result !== "undefined") && (result != null))
			postMessage(result);
		break;
	case ACLPerformative.REJECT_PROPOSAL:
		this.onRejectProposal(msg);
		break;
	}
};