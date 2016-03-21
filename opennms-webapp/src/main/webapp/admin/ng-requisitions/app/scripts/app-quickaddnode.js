/**
* @author Alejandro Galue <agalue@opennms.org>
* @copyright 2016 The OpenNMS Group, Inc.
*/

(function() {

  'use strict';

  angular.module('onms-requisitions', [
    'ngRoute',
    'ngAnimate',
    'ui.bootstrap',
    'angular-growl',
    'angular-loading-bar'
  ])

  .factory('authHttpResponseInterceptor',['$q','$window', function($q, $window) {
    return {
      response: function(response) {
        return response || $q.when(response);
      },
      responseError: function(rejection) {
        if (rejection.status === 401) {
          $window.location = '/opennms/login.jsp';
        }
        return $q.reject(rejection);
      }
    };
  }])

  .config(['$httpProvider',function($httpProvider) {
    $httpProvider.interceptors.push('authHttpResponseInterceptor');
  }])

  .config(['$routeProvider', function ($routeProvider) {
    $routeProvider
    .when('/', {
      templateUrl: 'views/quick-add-node-standalone.html',
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
