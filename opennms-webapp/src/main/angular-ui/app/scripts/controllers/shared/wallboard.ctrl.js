(function(PluginManager) {
  'use strict';

  angular.module('opennms.controllers.shared.wallboard', [
    'ui.router', 'angularMoment',
    'opennms.controllers.desktop.app',
    'opennms.services.shared.wallboard',
    'opennms.services.shared.menu'
  ])
    .controller('WallboardCtrl', ['$scope', '$log', '$sce', 'WallboardService', function($scope, $log, $sce, WallboardService) {
        $log.debug('Initializing WallboardCtrl.');
        $scope.$log = $log;
        
        $scope.board = function(url) {
            return $sce.trustAsResourceUrl(WallboardService.url(url, {}));
        };
        
        $log.debug('Finished Initializing WallboardCtrl.');
      }])
    .config(['$stateProvider', '$urlRouterProvider', function($stateProvider, $urlRouterProvider) {
        $stateProvider.state('app.wallboard', {
          url: '/wallboard',
          views: {
            'mainContent': {
              templateUrl: 'templates/desktop/wallboard.html',
              controller: 'WallboardCtrl'
            }
          }
        })
          ;
      }])

    .run(['$log', 'MenuService', function($log, menu) {
        menu.add('Info', '/app/wallboard', 'Wallboard');
      }])
    ;

  PluginManager.register('opennms.controllers.shared.wallboard');
}(PluginManager));
