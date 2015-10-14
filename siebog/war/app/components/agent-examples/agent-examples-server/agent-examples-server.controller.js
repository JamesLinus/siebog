(function() {
    "use strict";

    angular
        .module('siebog.agent-examples')
        .controller('AgentExamplesServerController', AgentExamplesServerController);

    AgentExamplesServerController.$inject = ['$http', '$window', '$q'];
    function AgentExamplesServerController($http, $window, $q) {
        var aesc = this;

        aesc.createTestAgent = createTestAgent;
        aesc.sendMessageToTestAgent = sendMessageToTestAgent;

        aesc.createPingAndPongAgents = createPingAndPongAgents;
        aesc.sendMessageToPingAgent = sendMessageToPingAgent;

        function createTestAgent() {
            var req = {
                url: "/siebog/rest/agents/running/TestAgent$TestAgent/test",
                method: 'PUT',
                headers: {
                   'Content-Type': 'application/x-www-form-urlencoded'
                },
                data: {}
            };
            $http(req).success(function(data) {
                $window.alert("Agent test created. Check the wildfly console.");
            });
        };

        function sendMessageToTestAgent() {
            var req = {
                url: "/siebog/rest/messages",
                method: 'POST',
                headers: {
                   'Content-Type': 'application/x-www-form-urlencoded'
                },
                data: 'acl={"receivers":[{"name":"test","host":"xjaf","str":"test@xjaf","agClass":{"module":"TestAgent","ejbName":"TestAgent","path":""}}],"performative":"INFORM","content":"hello"}'
            };
            $http(req).success(function(data) {
                $window.alert("Agent test created. Check the wildfly console.");
            });
        };

        function createPingAndPongAgents() {
            var reqPing = {
                url: "/siebog/rest/agents/running/siebog$Ping/ping",
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                data: {}
            };
            var reqPong = {
                url: "/siebog/rest/agents/running/siebog$Pong/pong",
                method: 'PUT',
                headers: {
                   'Content-Type': 'application/x-www-form-urlencoded'
                },
                data: {}
            };
            $q.all($http(reqPing), $http(reqPong)).then(function(data) {
                $window.alert("Ping and pong agents created. Check the wildfly console.");
            });
        };

        function sendMessageToPingAgent() {
            var req = {
                url: "/siebog/rest/messages",
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                data: 'acl={"receivers":[{"name":"ping","host":"xjaf","str":"ping@xjaf","agClass":{"module":"siebog","ejbName":"Ping","path":""}}],"performative":"REQUEST","content":"pong"}'
            };
            $http(req).success(function(data) {
                $window.alert('Message sent the the ping agent. Check the wildfly console.');
            });
        };
    }
})();