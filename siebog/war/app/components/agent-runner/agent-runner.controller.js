(function() {
	"use strict";

	angular
		.module('siebog.agent-runner')
		.controller('AgentRunnerController', AgentRunnerController);

	AgentRunnerController.$inject = ['agentRunnerModal', '$http'];
	function AgentRunnerController(agentRunnerModal, $http) {
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
            agentRunnerModal.openAgentRunnerModal.result.then(function(selectedItem) {
            	var req = {
					method: 'PUT',
					url: '/siebog/rest/agents/running/'+selectedItem.agClass['module']+'$'+selectedItem.agClass['ejbName']+'/'+selectedItem['name'],
					headers: {
						'Content-Type': 'application/x-www-form-urlencoded'
					},
					data: {}
            	}

            	$http(req).success(function(data) {
            		var found = false;
            		for (var aid in arc.createdAgents) {
            			var agent = arc.createdAgents[aid];
            			if (agent.str == data.str) {
            				found = true;
            				break;
            			}
            		}
            		if (!found) {
            			arc.createdAgents = arc.createdAgents.concat(data);
            		}
                });
            });
        };

        function sendMessage() {
        	var req = {
       			 method: 'POST',
       			 url: '/siebog/rest/messages',
       			 headers: {
       			   'Content-Type': 'application/x-www-form-urlencoded'//'application/json'
       			 },
       			 data: "acl=" + JSON.stringify($scope.request)
       			}
        	$http(req).success(function(data, status) {
        		console.log("USPEH");
        		console.log(data);
        	});
        };

        function fetchData() {
			$http.get('/siebog/rest/agents/classes').
	            success(function(data) {
	                arc.agents = data;
	            });
	        
	        $http.get('/siebog/rest/messages').
	            success(function(data) {
	                arc.performatives = data;
	            });
	        
	        $http.get('/siebog/rest/agents/running').
		        success(function(data) {
		        	if (data != '')
		        		arc.createdAgents = data;
		        });
		};
	}
})();