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
					}
				}
			})
			.state('main.agent-examples.client', {
				url: '/agent-examples/client',
				views: {
					'example@main.agent-examples': {
						templateUrl: 'app/components/agent-examples/agent-examples-client/agent-examples-client.html'
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