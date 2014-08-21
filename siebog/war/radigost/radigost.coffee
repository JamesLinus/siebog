class AID
	constructor: (@name, @hap) -> @str = "#{@name}@#{@hap}"

class AgentObserver
	onStart: (aid) ->
	onStep: (aid, msg) ->
	onStop: (aid) ->
	
class OpCode
	this.INIT = 1
	this.STEP = 2

class Radigost
	running = {}
	constructor: (@hap) ->
	start: (url, name, observer = null, agentInitArgs = null) ->
		newAid = new AID(name, @hap)
		if not running.hasOwnProperty(newAid.str)
			# init
			agent = {}
			agent.observer = observer
			agent.worker = new Worker(url)
			agent.worker.onmessage = (ev) ->
				msg = ev.data
				switch msg.opcode
					when OpCode.INIT
						running[msg.aid.str]?.observer?.onStart(msg.aid)
					when OpCode.STEP
						running[msg.aid.str]?.observer?.onStep(msg.aid, msg.info)
					else
						alert(msg)
			agent.worker.onerror = (ev) -> 
				# TODO				
			running[newAid.str] = agent
			# send the initialization message
			msg = 
				opcode: OpCode.INIT
				aid: newAid
				args: agentInitArgs
			agent.worker.postMessage(msg)
		return newAid
	post: (msg) ->
		for aid in msg.receivers
			running[aid.str]?.worker.postMessage(msg)
	
class ACLPerformative
	this.ACCEPT_PROPOSAL = 0
	this.AGREE = 1
	this.CANCEL = 2
	this.CFP = 3
	this.CONFIRM = 4
	this.DISCONFIRM = 5
	this.FAILURE = 6
	this.INFORM = 7
	this.INFORM_IF = 8
	this.INFORM_REF = 9
	this.NOT_UNDERSTOOD = 10
	this.PROPOSE = 11
	this.QUERY_IF = 12
	this.QUERY_REF = 13
	this.REFUSE = 14
	this.REJECT_PROPOSAL = 15
	this.REQUEST = 16
	this.REQUEST_WHEN = 17
	this.REQUEST_WHENEVER = 18
	this.SUBSCRIBE = 19
	this.PROXY = 20
	this.PROPAGATE = 21
	this.UNKNOWN = -1
	
class ACLMessage
	constructor: (@performative) -> 
		@receivers = []
	this.makeReply = (msg, performative, sender) ->
		reply = new ACLMessage(performative)
		reply.sender = sender
		if msg.replyTo?
			reply.receivers.push(msg.replyTo)
		else
			reply.receivers.push(msg.sender)
		# description of content
		reply.language = msg.language
		reply.ontology = msg.ontology
		reply.encoding = msg.encoding
		# control of conversation
		reply.protocol = msg.protocol
		reply.conversationId = msg.conversationId
		reply.inReplyTo = msg.replyWith
		# done
		return reply
