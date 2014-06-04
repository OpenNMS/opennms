(function(PluginManager) {
  'use strict';

  angular.module('opennms.controllers.shared.networkDashboard', [
    'ui.router', 'angularMoment', 'ngSanitize',
    'opennms.controllers.desktop.app',
    'opennms.services.shared.networkDashboard',
    'opennms.services.shared.menu'
  ])
    .controller('NetworkDashboardCtrl', ['$scope', '$log', '$sce', 'NetworkDashboardService', 'OutageService', 'AlarmService', function($scope, $log, $sce, NetworkDashboardService, OutageService, AlarmService) {
        $log.debug('Initializing NetworkDashboardCtrl.');
        $scope.$log = $log;
        $scope.outages = [];
        $scope.alarms = [];

        $scope.init = function() {
          OutageService.list({'ifRegainedService': 'null', 'orderBy': 'ifLostService', 'order': 'desc', 'limit': 40 }).then(function(outages) {
            $log.debug('Got outages:', outages);
            $scope.outages = outages;
          });

          AlarmService.list(0, 25).then(function(alarms) {
            $log.debug('Got alarms:', alarms);
            $scope.alarms = alarms;
          });
        };

        $scope.init();
        $log.debug('Finished Initializing NetworkDashboardCtrl.');
      }])
    .config(['$stateProvider', '$urlRouterProvider', function($stateProvider, $urlRouterProvider) {
        $stateProvider.state('app.network-dashboard', {
          url: '/network-dashboard',
          views: {
            'mainContent': {
              templateUrl: 'templates/desktop/network-dashboard.html',
              controller: 'NetworkDashboardCtrl'
            }
          }
        })
          ;
      }])

    .run(['$log', 'MenuService', function($log, menu) {
        menu.add('Info', '/app/network-dashboard', 'NetworkDashboard');
      }])
    ;

  PluginManager.register('opennms.controllers.shared.networkDashboard');
}(PluginManager));
