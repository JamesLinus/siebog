importScripts("/radigost/client/fipaacl.js");

/**
 * Defines the main agent prototype.
 * 
 * @constructor
 * @this {Agent}
 * @returns {Agent} New Agent instance.
 */
function Agent() {
	this.aid == null;
};

Agent.prototype.onStep = function(step) {
	var msg = {};
	msg.op = 2;
	msg.aid = this.aid;
	msg.msg = step;
	postMessage(msg);
};

/**
 * Invoked automatically by the system once the agent has been initialized.
 * 
 * @param {object} args Initial arguments for the agent.
 */
Agent.prototype.onInit = function(args) {
};

/**
 * Agents that require persistent state should override this function, and
 * return the state object to be preserved.
 * 
 * @returns Agent state to be preserved.
 */
Agent.prototype.getState = function() {
	return null;
};

/**
 * Sets the previously persisted state of the agent.
 * 
 * @param {object} state
 */
Agent.prototype.setState = function(state) {
};

/**
 * Invoked when the agent receives a message.
 * 
 * @this {Agent}
 * @param {ACLMessage} msg ACLMessage instance.
 */
Agent.prototype.onMessage = function(msg) {
};

self.__AGENT_INSTANCE__ = null; // place the instance of your agent here

self.onmessage = function(e) {
	var msg = e.data;

	// debug
	if (self.__AGENT_INSTANCE__ == null)
		self.__AGENT_INSTANCE__ = new Agent();

	// initialization message?
	if (self.__AGENT_INSTANCE__.aid == null) {
		self.__AGENT_INSTANCE__.aid = msg.aid;
		self.__AGENT_INSTANCE__.onInit(msg.args);
		// signal onstart
		postMessage({
			op : 1,
			aid : msg.aid
		});
	} else
		self.__AGENT_INSTANCE__.onMessage(msg);
};