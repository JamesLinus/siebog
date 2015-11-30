(function() {
	"use strict";

	angular
		.module('siebog.agent-examples')
		.config(config);

	config.$inject = ['$stateProvider'];
	function config($stateProvider) {
		$stateProvider
			.state('main.agent-examples', {
				url: '/agent-examples',
				views: {
					'content@': {
						templateUrl: 'app/components/agent-examples/agent-examples.html'
					},
					'sideBar@': {
						templateUrl: 'app/shared/console/siebog-console.html',
						controller: 'ConsoleController',
						controllerAs: 'cc'
					}
				}
			})
			.state('main.agent-examples.client', {
				url: '/agent-examples/client',
				views: {
					'example@main.agent-examples': {
						templateUrl: 'app/components/agent-examples/agent-examples-client/agent-examples-client.html',
						controller: 'AgentExamplesClientController',
						controllerAs: 'aecc'
					}
				}
			})
			.state('main.agent-examples.server', {
				url: '/agent-examples/server',
				views: {
					'example@main.agent-examples': {
						templateUrl: 'app/components/agent-examples/agent-examples-server/agent-examples-server.html',
						controller: 'AgentExamplesServerController',
						controllerAs: 'aesc'
					}
				}
			});
	}
})();