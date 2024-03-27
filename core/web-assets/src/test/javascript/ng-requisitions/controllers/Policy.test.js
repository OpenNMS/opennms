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

var policy = {
  'name': 'No IPs',
  'class': 'org.opennms.netmgt.provision.persist.policies.MatchingIpInterfacePolicy',
  'parameter': [{
    'key': 'action',
    'value': 'DO_NOT_PERSIST'
  }, {
    'key': 'matchBehavior',
    'value': 'NO_PARAMETERS'
  }]
};

var policyList = [{
  'name': 'Match IP Interface',
  'class': 'org.opennms.netmgt.provision.persist.policies.MatchingIpInterfacePolicy',
  'parameters': [{
    'key': 'matchBehavior',
    'required': true,
    'options': ['ALL_PARAMETERS', 'ANY_PARAMETER', 'NO_PARAMETERS']
  }, {
    'key': 'action',
    'required': true,
    'options': ['DISABLE_COLLECTION', 'DISABLE_SNMP_POLL', 'DO_NOT_PERSIST', 'ENABLE_COLLECTION', 'ENABLE_SNMP_POLL', 'MANAGE', 'UNMANAGE']
  }, {
    'key': 'hostName',
    'required': false,
    'options': []
  }, {
    'key': 'ipAddress',
    'required': false,
    'options': []
  }]
},{
  'name': 'Match SNMP Interface',
  'class': 'org.opennms.netmgt.provision.persist.policies.MatchingSnmpInterfacePolicy',
  'parameters': [{
    'key': 'action',
    'required': true,
    'options': ['DISABLE_COLLECTION', 'DISABLE_POLLING', 'DO_NOT_PERSIST', 'ENABLE_COLLECTION', 'ENABLE_POLLING']
  }, {
    'key': 'matchBehavior',
    'required': true,
    'options': ['ALL_PARAMETERS', 'ANY_PARAMETER', 'NO_PARAMETERS']
  }, {
    'key': 'ifAdminStatus',
    'required': false,
    'options': []
  }, {
    'key': 'ifAlias',
    'required': false,
    'options': []
  }, {
    'key': 'ifDescr',
    'required': false,
    'options': []
  }, {
    'key': 'ifIndex',
    'required': false,
    'options': []
  }, {
    'key': 'ifName',
    'required': false,
    'options': []
  }, {
    'key': 'ifOperStatus',
    'required': false,
    'options': []
  }, {
    'key': 'ifSpeed',
    'required': false,
    'options': []
  }, {
    'key': 'ifType',
    'required': false,
    'options': []
  }, {
    'key': 'physAddr',
    'required': false,
    'options': []
  }]
}];

function createController() {
  return controllerFactory('PolicyController', {
    $scope: scope,
    $uibModalInstance: mockModalInstance,
    RequisitionsService: mockRequisitionsService,
    policy: policy
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
  mockRequisitionsService.getAvailablePolicies = jasmine.createSpy('getAvailablePolicies');
  var policies = $q.defer();
  policies.resolve(policyList);
  mockRequisitionsService.getAvailablePolicies.and.returnValue(policies.promise);

  mockModalInstance = {
    close: function(obj) { console.info(obj); },
    dismiss: function(msg) { console.info(msg); }
  };
});

test('Controller: PolicyController: test controller', function() {
  createController();
  scope.$digest();
  expect(scope.policy.name).toBe(policy.name);
  expect(scope.policy.class).toBe(policy.class);

  expect(scope.availablePolicies.length).toBe(2);
  expect(scope.availablePolicies[0].name).toBe('Match IP Interface');
  expect(scope.availablePolicies[1].name).toBe('Match SNMP Interface');

  scope.updatePolicyParameters(policyList[1]);
  scope.getTemplate({key: 'does_not_matter'}); // Triggers the update of $scope.optionalParameters
  expect(scope.policy.parameter.length).toBe(2);
  expect(scope.policy.parameter[0].key).toBe('action');

  var options = scope.getParameterOptions('matchBehavior');
  expect(options).toEqual(['ALL_PARAMETERS', 'ANY_PARAMETER', 'NO_PARAMETERS']);

  expect(scope.getOptionalParameters()).toEqual(['hostName', 'ipAddress']);
  scope.policy.parameter.push({'key': 'hostName'});
  expect(scope.getOptionalParameters()).toEqual(['ipAddress']);
});
