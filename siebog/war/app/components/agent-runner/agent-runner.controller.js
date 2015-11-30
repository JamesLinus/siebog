(function() {
	"use strict";

	angular
		.module('siebog.agent-runner')
		.controller('AgentRunnerController', AgentRunnerController);

	AgentRunnerController.$inject = ['agentRunnerModal', 'xjaf', 'performatives', 'agentClasses'];
	function AgentRunnerController(agentRunnerModal, xjaf, performatives, agentClasses) {
		var arc = this;

		arc.accordian = {'agents':true, 'messages':true};
		arc.agentClasses = agentClasses;
		arc.performatives = performatives;
		arc.createdAgents = xjaf.agents;
		arc.request = {receivers: []};

		arc.newAgent = newAgent;
		arc.sendMessage = sendMessage;
        
        function newAgent(agent) {
            agentRunnerModal.open(agent).result.then(function(selectedItem) {
            	xjaf.startAgent(selectedItem);
            });
        };

        function sendMessage() {
        	xjaf.sendMessage(arc.request);
        };
	}
})();