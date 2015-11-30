(function() {
	"use strict";

	angular
		.module('siebog.console')
		.controller('ConsoleController', ConsoleController);

	ConsoleController.$inject = ['siebogConsole', 'xjaf'];
	function ConsoleController(siebogConsole, xjaf) {
		var cc = this;
		cc.messages = siebogConsole.messages;
		cc.agents = xjaf.agents;
	}
})();