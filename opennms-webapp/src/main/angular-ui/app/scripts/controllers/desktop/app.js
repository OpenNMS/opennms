(function() {
	'use strict';

	angular.module('opennms.controllers.desktop.app', [
	])
	
	.controller('AppCtrl', ['$scope', function($scope) {
		$scope.type = 'Desktop';
	}])

    .config(['$stateProvider', function($stateProvider) {
        var $injector = angular.injector();
        $stateProvider.state('app', {
            url: '/app',
            abstract: true,
            templateUrl: 'templates/desktop/main.html',
            controller: 'AppCtrl'
        });
    }])

    ;
}());