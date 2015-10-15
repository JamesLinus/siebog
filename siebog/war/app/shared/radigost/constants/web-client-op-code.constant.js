(function() {
	'use strict';

	var webClientOpCode = {
		REGISTER: 'r',
		DEREGISTER: 'd',
		NEW_AGENT: 'a'
	};

	angular
		.module('siebog.radigost')
		.constant('webClientOpCode', webClientOpCode);
})();