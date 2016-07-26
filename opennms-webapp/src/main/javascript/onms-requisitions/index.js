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
  var requisitionsTemplate = require('./views/requisitions.html');
  var requisitionTemplate = require('./views/requisition.html');
  var foreignSourceTemplate = require('./views/foreignsource.html');
  var nodeHorizontalTemplate = require('./views/node.html');
  var nodeVerticalTemplate = require('./views/node-panels.html');

  var appModule = angular.module('onms-requisitions', [
    'ngRoute',
    'ngCookies',
    'ngAnimate',
    'ui.bootstrap',
    'angular-growl',
    'angular-loading-bar'
  ])

  .config(['$routeProvider', function ($routeProvider) {
    $routeProvider
    .when('/requisitions', {
      templateUrl: requisitionsTemplate,
      controller: 'RequisitionsController'
    })
    .when('/requisitions/:foreignSource', {
      templateUrl: requisitionTemplate,
      controller: 'RequisitionController'
    })
    .when('/requisitions/:foreignSource/foreignSource', {
      templateUrl: foreignSourceTemplate,
      controller: 'ForeignSourceController'
    })
    .when('/requisitions/:foreignSource/nodes/:foreignId', {
      templateUrl: nodeHorizontalTemplate,
      controller: 'NodeController'
    })
    .when('/requisitions/:foreignSource/nodes/:foreignId/vertical', {
      templateUrl: nodeVerticalTemplate,
      controller: 'NodeController'
    })
    .otherwise({
      redirectTo: '/requisitions'
    });
  }])

  .config(['growlProvider', function(growlProvider) {
    growlProvider.globalTimeToLive(3000);
    growlProvider.globalPosition('bottom-center');
  }])

  .config(['$uibTooltipProvider', function($uibTooltipProvider) {
    $uibTooltipProvider.setTriggers({
      'mouseenter': 'mouseleave'
    });
    $uibTooltipProvider.options({
      'placement': 'left',
      'trigger': 'mouseenter'
    });
  }]);

  // Load Directives and Filters
  require('./scripts/directives/requisitionConstraints.js');
  require('./scripts/filters/startFrom.js');

  // Load Application Services
  require('./scripts/services/Requisitions.js');
  require('./scripts/services/Synchronize.js');

  // Load Application Controllers
  require('./scripts/controllers/Asset.js');
  require('./scripts/controllers/CloneForeignSource.js');
  require('./scripts/controllers/Detector.js');
  require('./scripts/controllers/ForeignSource.js');
  require('./scripts/controllers/Interface.js');
  require('./scripts/controllers/Move.js');
  require('./scripts/controllers/Node.js');
  require('./scripts/controllers/Policy.js');
  require('./scripts/controllers/QuickAddNode.js');
  require('./scripts/controllers/QuickAddNodeModal.js');
  require('./scripts/controllers/Requisition.js');
  require('./scripts/controllers/Requisitions.js');

  // Triggers AngularJS
  // Replaces ng-app="appName"
  angular.element(document).ready(function () {
    angular.bootstrap(document, [appModule.name]);
  });

}());
