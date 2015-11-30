(function() {
	'use strict';

	angular
		.module('siebog.console')
		.factory('siebogConsole', siebogConsole);

	siebogConsole.$inject = ['$websocket'];
	function siebogConsole($websocket) {
		var siebogConsole = {
			socket: $websocket("ws://" + window.location.host + "/siebog/console"),
			messages: []
		};
		
		return siebogConsole;
		
		siebogConsole.socket.onMessage(function(message) {
			messages.push(message.data);
		});

		siebogConsole.socket.onOpen(function() {
			console.log("WebSocket for console connection opened.");
		});

		siebogConsole.socket.onClose(function(e) {
			console.log("WebSocket for console connection closed.");
		});

		siebogConsole.socket.onError(function(e) {
			console.log("WebSocket for console  connection error: " + e.data);
		});
	}
})();