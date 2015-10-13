var siebog = angular.module('siebog', [
    'ngRoute',
    'ui.bootstrap'
]);

siebog.config(['$routeProvider', function ($routeProvider) {
        $routeProvider.when('/', {
            templateUrl:'partials/body.tpl.html',
            controller:'BodyCtrl'
        }).when('/Examples',
        		{
    		templateUrl: 'partials/Examples.html'
    	}).when('/xjafExamples',
        		{
    		templateUrl: 'xjaf/Examples.html'
    	}).when('/radigostExamples',
        		{
    		templateUrl: 'radigost/Examples.html'
    	}).otherwise({redirectTo:'/'});
}]);