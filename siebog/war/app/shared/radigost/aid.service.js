(function() {
	'use strict';

	angular
		.module('siebog.radigost')
		.factory('aid', aid);

	function aid() {
		return {
			createAid: createAid
		};
		
		function createAid(name, host) {
			return {
				name: name,
				host: host,
				radigost: true,
				str: name + "@" + host
			};
		};
	}
})();