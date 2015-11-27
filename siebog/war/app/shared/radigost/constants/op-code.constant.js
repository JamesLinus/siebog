(function() {
	'use strict';

	var opCode = {
		INIT: 1,
		STEP: 2,
		MOVE_TO_SERVER: 3
	};

	angular
		.module('siebog.radigost')
		.constant('opCode', opCode);
})();