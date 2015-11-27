(function() {
	'use strict';

	angular
		.module('siebog.radigost')
		.factory('aclMessage', aclMessage);

	function aclMessage() {
		return {
			createACLMessage: createACLMessage
		};

		function createACLMessage(performative) {
			return {
				performative: performative,
				receivers: []
			};
		};
	}
})();