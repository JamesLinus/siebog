(function() {
    "use strict";

    angular
        .module('siebog.agent-examples')
        .controller('AgentExamplesClientController', AgentExamplesClientController);

    AgentExamplesClientController.$inject = ['radigost', 'agentObserver', 'aclMessage', 'aclPerformative', '$window'];
    function AgentExamplesClientController(radigost, agentObserver, aclMessage, aclPerformative, $window) {
        var aecc = this;

        aecc.radigost = radigost.createRadigost("Examples");
        aecc.runHelloWorld = runHelloWorld;
        aecc.runMobileAgent = runMobileAgent;

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
    }
})();