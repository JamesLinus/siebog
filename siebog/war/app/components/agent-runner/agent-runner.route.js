(function() {
	"use strict";

	angular
		.module('siebog.agent-runner')
		.config(config);

	config.$inject = ['$stateProvider'];
	function config($stateProvider) {
		$stateProvider
			.state('main.agent-runner', {
				url: '/agent-runner',
				views: {
					'content@': {
						templateUrl: 'app/components/agent-runner/agent-runner.html',
						controller: 'AgentRunnerController',
						controllerAs: 'arc'
					}
				}
			});
	}
})();