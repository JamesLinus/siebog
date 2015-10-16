(function() {
    "use strict";

    angular
        .module('siebog.agent-examples')
        .controller('AgentExamplesClientController', AgentExamplesClientController);

    AgentExamplesClientController.$inject = ['radigost', 'agentObserver', 'aclMessage', 'aclPerformative', 'xjaf', '$window'];
    function AgentExamplesClientController(radigost, agentObserver, aclMessage, aclPerformative, xjaf, $window) {
        var aecc = this;

        aecc.radigost = radigost.createRadigost("Examples");
        aecc.runHelloWorld = runHelloWorld;
        aecc.runMobileAgent = runMobileAgent;

        aecc.remoteAgents = [];
        aecc.remoteMessageList = [];
        aecc.runRemoteAgent = runRemoteAgent;
        aecc.sendMessage = sendMessage;

        xjaf.getRunning().then(function(response) {
            aecc.remoteAgents = response.data;
        });

        function runHelloWorld() {
            var helloWorldObserver = agentObserver.createAgentObserver(function(aid, message) {
                $window.alert(message);
            });
            var aid = aecc.radigost.start("/siebog/js/agents/helloWorld.js", "HelloWorld", helloWorldObserver, null);
            var message = aclMessage.createACLMessage(aclPerformative.REQUEST);
            message.receivers.push(aid);
            message.interceptor = {
                preconditions: { messageProcessed: false },
                postconditions: { messageProcessed: true }
            };
            aecc.radigost.post(message);
        };

        function runMobileAgent() {
            aecc.radigost.start("/siebog/js/agents/mobileAgent.js", "MobileAgent", null);
        }

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
            message.content = "Radigost@" + radigost.host + " says: HI";// + content;
            aecc.radigost.postToServer(message);
        }
    }
})();