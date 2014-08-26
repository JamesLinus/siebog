importScripts("/siebog/radigost.js")

class MobileAgent extends Agent
	onInit: (args) ->
		@moveToServer()
		
self.agentInstance = new MobileAgent