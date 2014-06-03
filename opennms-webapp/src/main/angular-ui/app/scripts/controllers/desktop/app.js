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
    .run(['$rootScope', function($rootScope) {
// Credits: Adam's answer in http://stackoverflow.com/a/20786262/69362
// Paste this in browser's console
// var $rootScope = angular.element(document.querySelectorAll("[ui-view]")[0]).injector().get('$rootScope');

        $rootScope.$on('$stateChangeStart', function(event, toState, toParams, fromState, fromParams) {
          console.log('$stateChangeStart to ' + toState.to + '- fired when the transition begins.');
          console.log('fromState', fromState);
          console.log('fromParams', fromParams);
          console.log('toState', toState);
          console.log('toParams', toParams);
        });

        $rootScope.$on('$stateChangeError', function(event, toState, toParams, fromState, fromParams) {
          console.log('$stateChangeError - fired when an error occurs during transition.');
          console.log(arguments);
        });

        $rootScope.$on('$stateChangeSuccess', function(event, toState, toParams, fromState, fromParams) {
          console.log('$stateChangeSuccess to ' + toState.name + '- fired once the state transition is complete.');
        });

        $rootScope.$on('$viewContentLoading', function(event, viewConfig) {
          console.log('$viewContentLoading - fired before dom rendered', event);
          console.log('viewConfig', viewConfig);
        });

        $rootScope.$on('$viewContentLoaded', function(event) {
          console.log('$viewContentLoaded - fired after dom rendered', event);
        });

        $rootScope.$on('$stateNotFound', function(event, unfoundState, fromState, fromParams) {
          console.log('$stateNotFound ' + unfoundState.to + ' - fired when a state cannot be found by its name.');
          console.log('unfoundState.to', unfoundState.to);
          console.log('unfoundState.toParams', unfoundState.toParams);
          console.log('unfoundState.options', unfoundState.options);
          console.log('fromState', fromState);
          console.log('fromParams', fromParams);
        });
      }])
    ;

  PluginManager.register('opennms.controllers.desktop.app');
}(PluginManager));
