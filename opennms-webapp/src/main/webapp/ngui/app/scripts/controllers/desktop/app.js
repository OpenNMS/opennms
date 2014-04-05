(function() {
	'use strict';

	angular.module('opennms.controllers.desktop.app', [
		'opennms.controllers.desktop.dashboard'
	])
	
	.controller('AppCtrl', ['$scope', function($scope) {
		$scope.type = 'Desktop';
	}])

	;
}());