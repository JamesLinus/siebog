(function() {
	"use strict";

	angular
		.module('siebog.agent-runner')
		.controller('AgentRunnerController', AgentRunnerController);

	AgentRunnerController.$inject = ['agentRunnerModal', 'xjaf', 'aid'];
	function AgentRunnerController(agentRunnerModal, xjaf, aid) {
		var arc = this;

		arc.accordian = {'agents':true, 'messages':true};
		arc.agents = [];
		arc.performatives = [];
		arc.createdAgents = [];
		arc.request = {receivers: []};

		arc.newAgent = newAgent;
		arc.sendMessage = sendMessage;

        fetchData();
        
        function newAgent(agent) {
            agentRunnerModal.open(agent).result.then(function(selectedItem) {
            	xjaf.startAgent(selectedItem).then(function(response) {
            		arc.createdAgents.push(response.data);
            	});
            });
        };

        function sendMessage() {
        	xjaf.sendMessage(arc.request);
        };

        function fetchData() {
			xjaf.getAgentClasses().then(function(response) {
                arc.agents = response.data;
            });
	        
	        xjaf.getPerformatives().then(function(response) {
	        	arc.performatives = response.data;
			});
	        
	        xjaf.getRunning().then(function(response) {
	        	if (response.data != '') {
	        		arc.createdAgents = response.data;
	        	}
	        });
		};
	}
})();