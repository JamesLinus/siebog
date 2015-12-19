(function() {
	"use strict";

	angular
		.module('siebog.layout')
		.config(config);

	config.$inject = ['$stateProvider', '$urlRouterProvider'];
	function config($stateProvider, $urlRouterProvider) {
		$urlRouterProvider.otherwise('/home');

		$stateProvider
			.state('main', {
				abstract: true,
				views: {
					'header': {
						templateUrl: 'app/components/layout/header.html'
					},
					'sideBar': {
						templateUrl: 'app/shared/console/siebog-console.html',
						controller: 'ConsoleController',
						controllerAs: 'cc'
					}
				}
			})
			.state('main.home', {
				url: '/home',
				views: {
					'content@': {
						templateUrl: 'app/components/layout/home.html'
					},
				}
			});
	}
})();