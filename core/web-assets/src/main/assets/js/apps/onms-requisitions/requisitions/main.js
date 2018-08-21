'use strict';

/**
* @author Alejandro Galue <agalue@opennms.org>
* @copyright 2014 The OpenNMS Group, Inc.
*/

import Util from 'lib/util';

const requisitionsView = require('../lib/views/requisitions.html');
const requisitionView = require('../lib/views/requisition.html');
const foreignsourceView = require('../lib/views/foreignsource.html');
const nodeView = require('../lib/views/node.html');
const nodePanelsView = require('../lib/views/node-panels.html');

angular.module('onms-requisitions', [
  'onms.http',
  'ngRoute',
  'ngCookies',
  'ngAnimate',
  'ui.bootstrap',
  'angular-growl',
  'angular-loading-bar',
  'ngSanitize',
  'onmsDateFormatter'
])

.constant('Configuration', {
  'baseHref': Util.getBaseHref() + 'admin/ng-requisitions/index.jsp'
})

.config(['$routeProvider', function ($routeProvider) {
  $routeProvider
  .when('/requisitions', {
    templateUrl: requisitionsView,
    controller: 'RequisitionsController'
  })
  .when('/requisitions/:foreignSource', {
    templateUrl: requisitionView,
    controller: 'RequisitionController'
  })
  .when('/requisitions/:foreignSource/foreignSource', {
    templateUrl: foreignsourceView,
    controller: 'ForeignSourceController'
  })
  .when('/requisitions/:foreignSource/nodes/:foreignId', {
    templateUrl: nodeView,
    controller: 'NodeController'
  })
  .when('/requisitions/:foreignSource/nodes/:foreignId/vertical', {
    templateUrl: nodePanelsView,
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

