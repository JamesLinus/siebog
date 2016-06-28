(function() {
	'use strict';

	angular
		.module('siebog.radigost')
		.factory('xjaf', xjaf);

	xjaf.$inject = ['$http'];
	function xjaf($http) {
		var XJAF = {
			clientManagerUrl: "/siebog/rest/webclient",
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
			return $http.get(XJAF.clientManagerUrl + '/classes');
		};

		function getRunning() {
			return $http.get(XJAF.clientManagerUrl + '/running');
		};

		function getPerformatives() {
			return $http.get(XJAF.clientManagerUrl + "/messages");
		};

		function startAgent(agent, initArgs) {
			var req = {
				method: 'PUT',
				url: XJAF.clientManagerUrl + '/running/' + agent.agClass['module']+'$'+agent.agClass['ejbName']+'/'+agent['name'],
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
       			 url: XJAF.clientManagerUrl + '/messages',
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
       			url: XJAF.clientManagerUrl,
       			headers: {
       				'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'
       			},
       			data: "url="+ url + "&aid=" + JSON.stringify(aid) + "&state=" + state
       		}
       		return $http(req);
		};
	}
})();