(function() {
	"use strict";

	angular
		.module('siebog.console')
		.controller('ConsoleController', ConsoleController);

	ConsoleController.$inject = ['$websocket', 'xjaf'];
	function ConsoleController($websocket, xjaf) {
		var cc = this;
		cc.accordion = {
			messages: true,
			agents: true
		};
		cc.agents = xjaf.agents;
		cc.messages = [];
		
		cc.clearConsole = function() {
			cc.messages = [];
		}
		
		var socket = $websocket("ws://" + window.location.host + "/siebog/console");

		socket.onMessage(function(message) {
			var msg = JSON.parse(message.data);
			if(msg.type === "LOG") {
				var d = new Date();
			    var time = ("0" + d.getHours()).substr(-2) + ':' + ("0" + d.getMinutes()).substr(-2) + ':' + ("0" + d.getSeconds()).substr(-2);
				cc.messages.push(time + " - " + msg.data);
			} else if(msg.type === "ADD") {
				xjaf.agents.array.push(JSON.parse(msg.data));
			} else if(msg.type === "REMOVE") {
				var agent = JSON.parse(msg.data);
				for(var i = 0, n = xjaf.agents.array.length; i < n; i++) {
					if(agent.str === xjaf.agents.array[i].str) {
						xjaf.agents.array.splice(i, 1);
						break;
					}
				}
			}
		});

		socket.onOpen(function() {
			console.log("WebSocket for console connection opened.");
		});

		socket.onClose(function(e) {
			console.log("WebSocket for console connection closed.");
		});

		socket.onError(function(e) {
		    console.log("WebSocket for console  connection error: " + e.data);
		});
	}
})();