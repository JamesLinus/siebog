class Agent
	constructor: () -> @aid = null
	post: (msg) -> self.postMessage(msg)
	onInit: (args) ->
		msg =
			opcode: OpCode.INIT
			aid: @aid
		@post(msg)
	onMessage: (msg) ->
	onStep: (step) ->
		msg =
			opcode: OpCode.STEP
			aid: @aid
			info: step
		@post(msg)
	
self.__AGENT_INSTANCE__ = null

self.onmessage = (ev) ->
	msg = ev.data
	if msg.opcode == OpCode.INIT
		self.__AGENT_INSTANCE__.aid = msg.aid
		self.__AGENT_INSTANCE__.onInit(msg.args)
	else
		self.__AGENT_INSTANCE__.onMessage(msg)
