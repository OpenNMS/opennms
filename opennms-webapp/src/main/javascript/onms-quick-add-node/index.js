/* eslint no-undef:off */

/**
* @author Alejandro Galue <agalue@opennms.org>
* @copyright 2014 The OpenNMS Group, Inc.
*/

(function() {
  'use strict';

  var angular = require('angular');

  // Load Libraries and Extensions
  require('jquery');
  require('angular-route');
  require('angular-cookies');
  require('angular-sanitize');
  require('angular-animate');
  require('angular-bootstrap/ui-bootstrap-tpls');
  require('angular-loading-bar');
  require('angular-growl-v2/build/angular-growl');
  require('ip-address/ip-address-globals');
  require('bootbox');

  // CSS
  require('angular-loading-bar/build/loading-bar.css');
  require('angular-growl-v2/build/angular-growl.css');

  // HTML
  var quickAddNodeTemplate = require('./views/quick-add-node-standalone.html');

  angular.module('onms-requisitions', [
    'ngRoute',
    'ngCookies',
    'ngAnimate',
    'ui.bootstrap',
    'angular-growl',
    'angular-loading-bar'
  ])

  .config(['$routeProvider', function ($routeProvider) {
    $routeProvider
    .when('/', {
      templateUrl: quickAddNodeTemplate,
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

  // Load Directives and Filters
  require('onms-requisitions/scripts/directives/requisitionConstraints.js');

  // Load Application Services
  require('onms-requisitions/scripts/services/Requisitions.js');
  require('onms-requisitions/scripts/services/Synchronize.js');

  // Load Application Controllers
  require('onms-requisitions/scripts/controllers/QuickAddNode.js');
  require('onms-requisitions/scripts/controllers/QuickAddNodeModal.js');

  // Trigger AngularJS
  document.addEventListener('DOMContentLoaded', function() {
    angular.bootstrap(document, ['onms-requisitions']);
  }, false);

}());
