(function(PluginManager) {
	'use strict';

	angular.module('opennms.controllers.shared.alarms', [
        'ui.router',
        'opennms.services.shared.menu',
		'opennms.services.shared.alarms'
	])

	.controller('AlarmsCtrl', ['$log', '$scope', 'AlarmService', function($log, $scope, alarms) {
        $scope.init = function(options) {
            $log.info('Initializing Alarms Controller:',options);
        };

        alarms.list().then(function(alarms) {
			$log.debug('Got alarms:',alarms);
			$scope.alarms = alarms;
		});
	}])

    .config(['$stateProvider', function($stateProvider) {
		$stateProvider.state('app.alarms', {
            url: '/alarms',
            views: {
                'mainContent': {
                    templateUrl: 'templates/desktop/alarms.html',
                    controller: 'AlarmsCtrl'
                }
            }
        });
    }])

    .run(['$log', 'MenuService', function($log, menu) {
        menu.add('Info', '/app/alarms', 'Alarms');
    }])

    ;
    
    PluginManager.register('opennms.controllers.shared.alarms');
}(PluginManager));