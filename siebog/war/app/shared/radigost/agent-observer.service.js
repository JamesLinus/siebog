(function() {
	'use strict';

	angular
		.module('siebog.radigost')
		.factory('agentObserver', agentObserver);

	function agentObserver() {
		return {
			createAgentObserver: createAgentObserver
		};

		function createAgentObserver(onStep, onStart, onStop) {
			var agentObserver = {
				onStep: onStep,
				onStart: onStart,
				onStop: onStop
			};
			if(!onStart) {
				agentObserver.onStart = function(aid) {};
			}
			if(!onStop) {
				agentObserver.onStop = function(aid) {};
			}

			return agentObserver;
		};
	}
})();