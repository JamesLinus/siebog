(function() {
	'use strict';

	angular
		.module('siebog.radigost')
		.factory('xjaf', xjaf);

	xjaf.$inject = ['$http'];
	function xjaf($http) {
		var XJAF = {
			agentManagerUrl: "/siebog/rest/agents",
			messageManagerUrl: "/siebog/rest/messages",
			agents: {
				array: []
			}
		};

		XJAF.getAgentClasses = getAgentClasses;
		XJAF.getRunning = getRunning;
		XJAF.startAgent = startAgent;
		XJAF.getPerformatives = getPerformatives;
		XJAF.sendMessage = sendMessage;
		XJAF.accept = accept;
		getRunning().then(function(response) {
			if(response.data != '') {
				XJAF.agents.array = response.data;
			} else {
				XJAF.agents.array = [];
			}
		});

		return XJAF;

		function getAgentClasses() {
			return $http.get(XJAF.agentManagerUrl + '/classes');
		};

		function getRunning() {
			return $http.get(XJAF.agentManagerUrl + '/running');
		};

		function getPerformatives() {
			return $http.get(XJAF.messageManagerUrl);
		};

		function startAgent(agent, initArgs) {
			var req = {
				method: 'PUT',
				url: XJAF.agentManagerUrl + '/running/' + agent.agClass['module']+'$'+agent.agClass['ejbName']+'/'+agent['name'],
				headers: {
					'Content-Type': 'application/x-www-form-urlencoded'
				},
				data: (initArgs === undefined) ? {} : initArgs
        	}

        	return $http(req);
		};

		function sendMessage(request) {
			var req = {
       			 method: 'POST',
       			 url: '/siebog/rest/messages',
       			 headers: {
       			   'Content-Type': 'application/x-www-form-urlencoded'//'application/json'
       			 },
       			 data: "acl=" + JSON.stringify(request)
       			}
        	return $http(req);
		};

		function accept(url, aid, state) {
			var req = {
   				method: 'PUT',
       			url: '/siebog/rest/webclient',
       			headers: {
       				'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'
       			},
       			data: "url="+ url + "&aid=" + JSON.stringify(aid) + "&state=" + state
       		}
       		return $http(req);
		};
	}
})();