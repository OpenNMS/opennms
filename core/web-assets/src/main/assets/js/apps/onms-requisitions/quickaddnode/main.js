/**
* @author Alejandro Galue <agalue@opennms.org>
* @copyright 2016-2022 The OpenNMS Group, Inc.
*/

(function() {

  'use strict';

  const quickAddNodeStandaloneView = require('../lib/views/quick-add-node-standalone.html');

  angular.module('onms-requisitions', [
    'onms.http',
    'onms.default.apps',
    'ngRoute',
    'ngAnimate',
    'ui.bootstrap',
    'angular-growl',
    'angular-loading-bar',
    'ngSanitize',
    'onmsDateFormatter'
  ])

  .config(['$locationProvider', function($locationProvider) {
    $locationProvider.hashPrefix('');
  }])

  .config(['$routeProvider', function ($routeProvider) {
    $routeProvider
    .when('/', {
      templateUrl: quickAddNodeStandaloneView,
      controller: 'QuickAddNodeController',
      resolve: {
        foreignSources: function() { return null; }
      }
    })
    .otherwise({
      redirectTo: '/'
    });
  }])

  .config(['growlProvider', function(growlProvider) {
    growlProvider.globalTimeToLive(3000);
    growlProvider.globalPosition('bottom-center');
  }]);

}());
