/**
 * Defines a globally-unique agent identifier. The general syntax is <code>name@device</code>,
 * where <code>name</code> is the agent's local name, and <code>device</code> is the host
 * identifier (e.g. IP address, IMEI, UUID, ...).
 * 
 * @constructor
 * @this {AID}
 * @param {string} name Agent's local name.
 * @param {string} device Device identifier.
 * @param {Boolean} server Optional, indicates whether the agent resides on the server.
 * @returns {AID} New AID instance.
 */
function AID(name, device, server) {
	this.name = name;
	this.device = device;
	this.value = name + "@" + device;
	this.server = server ? true : false; // [in] server can be undefined
}

/**
 * Defines FIPA ACL performative constants.
 */
var ACLPerformative = new function() {
	this.ACCEPT_PROPOSAL = 0;
	this.AGREE = 1;
	this.CANCEL = 2;
	this.CFP = 3;
	this.CONFIRM = 4;
	this.DISCONFIRM = 5;
	this.FAILURE = 6;
	this.INFORM = 7;
	this.INFORM_IF = 8;
	this.INFORM_REF = 9;
	this.NOT_UNDERSTOOD = 10;
	this.PROPOSE = 11;
	this.QUERY_IF = 12;
	this.QUERY_REF = 13;
	this.REFUSE = 14;
	this.REJECT_PROPOSAL = 15;
	this.REQUEST = 16;
	this.REQUEST_WHEN = 17;
	this.REQUEST_WHENEVER = 18;
	this.SUBSCRIBE = 19;
	this.PROXY = 20;
	this.PROPAGATE = 21;
	this.UNKNOWN = -1;
};

/**
 * Represents a FIPA ACL message. Refer to <a
 * href="http://www.fipa.org/specs/fipa00061/SC00061G.pdf">FIPA ACL Message Structure Specification</a>
 * for more details.
 * 
 * @constructor
 * @this {ACLMessage}
 * @param {number} performative Message performative.
 * @returns {ACLMessage} New ACLMessage instance.
 */
function ACLMessage(performative) {
	this.performative = performative;
	this.receivers = [];
	this.content = null;
};

/**
 * Returns a reply for the given message, filling-in the as much fields as possible.
 * 
 * @param {ACLMessage} msg Received ACLMessage object.
 * @param {number} performative Performative to use in the reply.
 * @param {AID} sender Sender of the reply.
 * @returns {ACLMessage} ACLMessage object representing the reply.
 */
function ACLMakeReply(msg, performative, sender) {
	var reply = new ACLMessage(performative);
	reply.sender = sender;
	// receiver
	if (typeof msg.replyTo !== "undefined")
		reply.receivers.push(msg.replyTo);
	else if (typeof msg.sender !== "undefined")
		reply.receivers.push(msg.sender);
	// description of content
	if (typeof msg.language !== "undefined")
		reply.language = msg.language;
	if (typeof msg.ontology !== "undefined")
		reply.ontology = msg.ontology;
	if (typeof msg.encoding !== "undefined")
		reply.encoding = msg.encoding;
	// control of conversation
	if (typeof msg.protocol !== "undefined")
		reply.protocol = msg.protocol;
	if (typeof msg.conversationId !== "undefined")
		reply.conversationId = msg.conversationId;
	if (typeof msg.replyWith !== "undefined")
		reply.inReplyTo = msg.replyWith;
	// done
	return reply;
}
