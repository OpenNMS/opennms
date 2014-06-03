(function(PluginManager) {
  'use strict';

  angular.module('opennms.controllers.desktop.app', [
    'ui.router'
  ])

  .controller('AppCtrl', ['$scope', function($scope) {
    $scope.type = 'Desktop';
  }])

  .config(['$stateProvider', function($stateProvider) {
    $stateProvider.state('app', {
      url: '/app',
      abstract: true,
      templateUrl: 'templates/desktop/main.html',
      controller: 'AppCtrl'
    });
  }])
  ;

  PluginManager.register('opennms.controllers.desktop.app');
}(PluginManager));
