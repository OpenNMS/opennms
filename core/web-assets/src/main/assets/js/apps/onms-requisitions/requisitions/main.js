/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
'use strict';

/**
* @author Alejandro Galue <agalue@opennms.org>
* @copyright 2014-2022 The OpenNMS Group, Inc.
*/

import Util from 'lib/util';

const requisitionsView = require('../lib/views/requisitions.html');
const requisitionView = require('../lib/views/requisition.html');
const foreignsourceView = require('../lib/views/foreignsource.html');
const nodeView = require('../lib/views/node.html');
const nodePanelsView = require('../lib/views/node-panels.html');

angular.module('onms-requisitions', [
  'onms.http',
  'onms.default.apps',
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

.config(['$locationProvider', function($locationProvider) {
  $locationProvider.hashPrefix('');
}])

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

