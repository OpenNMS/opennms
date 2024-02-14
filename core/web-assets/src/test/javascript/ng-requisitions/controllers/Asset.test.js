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
/**
* @author Alejandro Galue <agalue@opennms.org>
* @copyright 2014 The OpenNMS Group, Inc.
*/

'use strict';

const angular = require('angular-js');
require('angular-mocks');
require('../../../../../src/main/assets/js/apps/onms-requisitions/requisitions');

const OnmsDateFormatter = require('../../../../../src/main/assets/js/apps/onms-date-formatter');

var scope, $q, controllerFactory, dateFormatterService, mockModalInstance, mockRequisitionsService = {}, asset = { key: 'admin', value: 'agalue' };

function createController() {
  return controllerFactory('AssetController', {
    $scope: scope,
    $uibModalInstance: mockModalInstance,
    DateFormatterService: dateFormatterService,
    RequisitionsService: mockRequisitionsService,
    asset: asset,
    assetsBlackList: []
  });
}

beforeEach(function() {
  window._onmsDateTimeFormat = "yyyy-MM-dd'T'HH:mm:ssxxx";
  window._onmsZoneId = 'America/New_York';
  window._onmsFormatter = new OnmsDateFormatter();
});

beforeEach(angular.mock.module('onms-requisitions', function($provide) {
  $provide.value('$log', console);    
}));

beforeEach(angular.mock.inject(function($rootScope, $controller, $interval, _$q_, DateFormatterService) {
  scope = $rootScope.$new();
  controllerFactory = $controller;
  $q = _$q_;
  dateFormatterService = DateFormatterService;
  $interval.flush(200);
}));

beforeEach(function() {
  mockRequisitionsService.getAvailableAssets = jasmine.createSpy('getAvailableAssets');
  var assets = $q.defer();
  assets.resolve(['address1','city','state','zip']);
  mockRequisitionsService.getAvailableAssets.and.returnValue(assets.promise);

  mockModalInstance = {
    close: function(obj) { console.info(obj); },
    dismiss: function(msg) { console.info(msg); }
  };
});

test('Controller: AssetController: test controller', function() {
  createController();
  scope.$digest();
  expect(scope.asset.value).toBe(asset.value);
  expect(scope.assetFields.length).toBe(4);
  expect(scope.assetFields[0]).toBe('address1');
  expect(scope.getAvailableAssetFields()).toEqual(['address1','city','state','zip']);
  scope.assetsBlackList.push('address1');
  expect(scope.getAvailableAssetFields()).toEqual(['city','state','zip']);
});
