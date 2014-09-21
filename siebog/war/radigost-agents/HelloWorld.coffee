importScripts("/siebog/radigost.js")

class HelloWorld extends Agent
	onMessage: (msg) ->
		@onStep("Hello from #{@aid.str}!")
		
self.agentInstance = new HelloWorld