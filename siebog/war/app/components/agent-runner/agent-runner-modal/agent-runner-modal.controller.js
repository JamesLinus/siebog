(function() {
	'use strict';

	angular
		.module('siebog.agent-runner')
		.controller('AgentRunnerModalController', AgentRunnerModalController);

	AgentRunnerModalController.$inject = ['$modalInstance', 'agent'];
	function AgentRunnerModalController($modalInstance, agent) {
		var armc = this;

        armc.agent = agent;
        armc.agentName = '';
        
        armc.ok = ok;
        armc.cancel = cancel;
        armc.keyFn = keyFn;

		function ok() {
            var agentInstance = {'name':armc.agentName,'host':"xjaf", 'agClass':armc.agent};
            $modalInstance.close(agentInstance);
        };

        function cancel() {
            $modalInstance.dismiss('cancel');
        };

        function keyFn(event) {
        	if (event.keyCode == 13) {
        		 ok();
        	}
        }
	}
})();