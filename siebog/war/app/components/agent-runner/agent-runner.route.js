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
						controllerAs: 'arc',
						resolve: {
							performatives: getPerformatives,
							agentClasses: getAgentClasses
						}
					}
				}
			});
		
		getAgentClasses.$inject = ['xjaf'];
        function getAgentClasses(xjaf) {
			return xjaf.getAgentClasses().then(function(response) {
                return response.data;
            });
		};
		
		getPerformatives.$inject = ['xjaf'];
		function getPerformatives(xjaf) {
			return xjaf.getPerformatives().then(function(response) {
	        	return response.data;
			});
		};
	}
})();