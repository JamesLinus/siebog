(function() {
    "use strict";

    angular
        .module('siebog.agent-examples')
        .controller('AgentExamplesServerController', AgentExamplesServerController);

    AgentExamplesServerController.$inject = ['xjaf', '$window', '$q'];
    function AgentExamplesServerController(xjaf, $window, $q) {
        var aesc = this;

        aesc.createTestAgent = createTestAgent;
        aesc.sendMessageToTestAgent = sendMessageToTestAgent;

        aesc.createPingAndPongAgents = createPingAndPongAgents;
        aesc.sendMessageToPingAgent = sendMessageToPingAgent;

        function createTestAgent() {
            var testAgent = {
                agClass: {
                    ejbName: "TestAgent",
                    module: "TestAgent",
                    path: "/TestAgent/agents/xjaf"
                },
                name: "test",
                host: "xjaf"
            };
            xjaf.startAgent(testAgent).then(function(data) {
                $window.alert("Agent test created. Check the wildfly console.");
            });
        };

        function sendMessageToTestAgent() {
            var request = {
                receivers:[{
                    name: "test",
                    host: "xjaf",
                    str: "test@xjaf",
                    agClass: {
                        module: "TestAgent",
                        ejbName: "TestAgent",
                        path: ""
                    }
                }],
                performative: "INFORM",
                content: "hello"
            }
            xjaf.sendMessage(request).then(function(data) {
                $window.alert("Agent test created. Check the wildfly console.");
            });
        };

        function createPingAndPongAgents() {
            var pingAgent = {
                agClass: {
                    ejbName: "Ping",
                    module: "siebog",
                    path: "/siebog/agents/xjaf"
                },
                name: "ping",
                host: "xjaf"
            };
            var pongAgent = {
                agClass: {
                    ejbName: "Pong",
                    module: "siebog",
                    path: "/siebog/agents/xjaf"
                },
                name: "pong",
                host: "xjaf"
            };

            $q.all(xjaf.startAgent(pingAgent), xjaf.startAgent(pongAgent)).then(function(data) {
                $window.alert("Ping and pong agents created. Check the wildfly console.");
            });
        };

        function sendMessageToPingAgent() {
            var request = {
                receivers: [{
                    name: "ping",
                    host: "xjaf",
                    str: "ping@xjaf",
                    agClass: {
                        module: "siebog",
                        ejbName: "Ping",
                        path: ""
                    }
                }],
                performative: "REQUEST",
                content: "pong"
            };
            xjaf.sendMessage(request).then(function(data) {
                $window.alert('Message sent the the ping agent. Check the wildfly console.');
            });
        };
    }
})();