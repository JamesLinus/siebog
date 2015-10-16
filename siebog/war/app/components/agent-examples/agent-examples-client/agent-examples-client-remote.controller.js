(function() {
    "use strict";

    angular
        .module('siebog.agent-examples')
        .controller('AgentExamplesClientRemoteController', AgentExamplesClientRemoteController);

    AgentExamplesClientRemoteController.$inject = ['radigost', 'agentObserver', 'aclMessage', 'aclPerformative', 'xjaf', '$window'];
    function AgentExamplesClientRemoteController(radigost, agentObserver, aclMessage, aclPerformative, xjaf, $window) {
        var aecc = this;

        aecc.radigost = radigost.createRadigost("xjaf", true);

        aecc.remoteAgents = [];
        aecc.remoteMessageList = [];
        aecc.runRemoteAgent = runRemoteAgent;
        aecc.sendMessage = sendMessage;

        /*xjaf.getRunning().then(function(response) {
            aecc.remoteAgents = response.data;
            angular.forEach(aecc.remoteAgents, function(agent) {

            });
        });*/

        function createNewRemoteObserver() {
            return agentObserver.createAgentObserver(function(aid, message) {
                aecc.remoteMessageList.push(new Date() + " : " + message);
            });
        }

        function runRemoteAgent() {
            aecc.remoteAgents.push(aecc.radigost.start("/siebog/js/agents/remoteAgent.js", aecc.newAgent, createNewRemoteObserver()));
        }

        function sendMessage(targetName, targetHost) {
            var message = aclMessage.createACLMessage(aclPerformative.INFORM);
            message.receivers = [{
                name: targetName,
                host: targetHost,
                radigost: true
            }];
            message.content = "Radigost@" + aecc.radigost.host + " says: HI";// + content;
            aecc.radigost.postToServer(message);
        }
    }
})();