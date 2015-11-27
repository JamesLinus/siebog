(function() {
	'use strict';

	angular
		.module('siebog.agent-runner')
		.factory('agentRunnerModal', agentRunnerModal);

	agentRunnerModal.$inject = ['$uibModal'];
	function agentRunnerModal($uibModal) {
		return {
			open: openAgentRunnerModal
		};

		function openAgentRunnerModal(agent) {
			var modalInstance = $uibModal.open({
                templateUrl: 'app/components/agent-runner/agent-runner-modal/agent-runner-modal.html',
                controller: 'AgentRunnerModalController',
                controllerAs: 'armc',
                size: 'sm',
                resolve: {
                    agent: function () {
                        return agent;
                    }
                }
            });
            return modalInstance;
		}
	}
})();