(function() {
    "use strict";

    angular
        .module('siebog.agent-examples')
        .controller('AgentExamplesClientRemoteController', AgentExamplesClientRemoteController);

    AgentExamplesClientRemoteController.$inject = ['radigost', 'agentObserver', 'aclMessage', 'aclPerformative', 'xjaf', '$window', '$scope'];
    function AgentExamplesClientRemoteController(radigost, agentObserver, aclMessage, aclPerformative, xjaf, $window, $scope) {
        var aecc = this;

        aecc.radigost = radigost.createRadigost("xjaf", true);

        aecc.remoteAgents = [];
        aecc.remoteMessageList = [];
        aecc.runRemoteAgent = runRemoteAgent;
        aecc.sendMessage = sendMessage;

        xjaf.getRunning().then(function(response) {
            angular.forEach(response.data, function(agent) {
            	if(agent.agClass.ejbName === "RadigostStub") {
            		aecc.remoteAgents.push(aecc.radigost.recreateAgent("/siebog/js/agents/remoteAgent.js", agent.name, createNewRemoteObserver()));
            	}
            });
        });

        function createNewRemoteObserver() {
            return agentObserver.createAgentObserver(function(aid, message) {
                aecc.remoteMessageList.push(new Date() + " : " + message);
                $scope.$digest();
            });
        }

        function runRemoteAgent() {
            aecc.remoteAgents.push(aecc.radigost.start("/siebog/js/agents/remoteAgent.js", aecc.newAgent, createNewRemoteObserver()));
            aecc.newAgent = "";
        }

        function sendMessage(targetName, targetHost) {
            var message = aclMessage.createACLMessage(aclPerformative.INFORM);
            message.receivers = [{
                name: targetName,
                host: targetHost,
                radigost: true
            }];
            message.content = "Radigost@" + aecc.radigost.host + " says: " + aecc.content;
            aecc.radigost.postToServer(message);
            aecc.content = "";
        }
    }
})();