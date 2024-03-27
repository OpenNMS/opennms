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

var scope, $q, controllerFactory, mockModalInstance, mockRequisitionsService = {};

var detector = {
  'name': 'HTTP',
  'class': 'org.opennms.netmgt.provision.http.HttpDetector',
  'parameters': []
};

var detectorList = [{
  'name': 'ICMP',
  'class': 'org.opennms.netmgt.provision.detector.icmp.IcmpDetector',
  'parameters': [{'key': 'port'}, {'key': 'ipMatch'}, {'key': 'retries'}, {'key': 'timeout'}]
},{
  'name': 'SNMP',
  'class': 'org.opennms.netmgt.provision.detector.snmp.SnmpDetector',
  'parameters': [{'key': 'port'}, {'key': 'vbvalue'}, {'key': 'oid'}, {'key': 'ipMatch'}, {'key': 'retries'}, {'key': 'agentConfigFactory'}, {'key': 'timeout'}]
}];

function createController() {
  return controllerFactory('DetectorController', {
    $scope: scope,
    $uibModalInstance: mockModalInstance,
    RequisitionsService: mockRequisitionsService,
    detector: detector
  });
}

beforeEach(angular.mock.module('onms-requisitions', function($provide) {
  $provide.value('$log', console);
}));

beforeEach(angular.mock.inject(function($rootScope, $controller, _$q_) {
  scope = $rootScope.$new();
  controllerFactory = $controller;
  $q = _$q_;
}));

beforeEach(function() {
  mockRequisitionsService.getAvailableDetectors = jasmine.createSpy('getAvailableDetectors');
  var detectors = $q.defer();
  detectors.resolve(detectorList);
  mockRequisitionsService.getAvailableDetectors.and.returnValue(detectors.promise);

  mockModalInstance = {
    close: function(obj) { console.info(obj); },
    dismiss: function(msg) { console.info(msg); }
  };
});

test('test controller', function() {
  createController();
  scope.$digest();
  expect(scope.detector.name).toBe(detector.name);
  expect(scope.detector.class).toBe(detector.class);
  expect(scope.availableDetectors.length).toBe(2);
  expect(scope.availableDetectors[0].name).toBe('ICMP');
  expect(scope.availableDetectors[1].name).toBe('SNMP');

  scope.updateAvailableParameters(detectorList[1]);
  expect(scope.availableParameters.length).toBe(7);
  expect(scope.availableParameters[0].key).toBe('port');
});
