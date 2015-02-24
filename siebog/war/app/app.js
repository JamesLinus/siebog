var siebog = angular.module('siebog', [
    'ngRoute',
    'ui.bootstrap'
]);

siebog.config(['$routeProvider', function ($routeProvider) {
        $routeProvider.when('/home', {
            templateUrl:'partials/body.tpl.html',
            controller:'BodyCtrl'
        }).otherwise({redirectTo:'/home'});
}]);