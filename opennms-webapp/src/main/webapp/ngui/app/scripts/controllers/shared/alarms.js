(function() {
	'use strict';

	angular.module('opennms.controllers.shared.alarms', [
		'opennms.services.shared.alarms'
	])

	.controller('AlarmsCtrl', ['$log', '$scope', 'AlarmService', function($log, $scope, alarms) {
		alarms.list().then(function(alarms) {
			$log.debug('Got alarms:',alarms);
			$scope.alarms = alarms;
		});
	}])

	;
}());