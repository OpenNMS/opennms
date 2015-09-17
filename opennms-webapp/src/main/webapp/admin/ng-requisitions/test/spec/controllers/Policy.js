/**
* @author Alejandro Galue <agalue@opennms.org>
* @copyright 2014 The OpenNMS Group, Inc.
*/

'use strict';

describe('Controller: PolicyController', function () {

  var scope, $q, controllerFactory, mockModalInstance, mockRequisitionsService = {},
    policy = { name: 'MyPolicy' };

  function createController() {
    return controllerFactory('PolicyController', {
      $scope: scope,
      $modalInstance: mockModalInstance,
      RequisitionsService: mockRequisitionsService,
      policy: policy
    });
  };

  beforeEach(module('onms-requisitions', function($provide) {
    $provide.value('$log', console);    
  }));

  beforeEach(inject(function($rootScope, $controller, _$q_) {
    scope = $rootScope.$new();
    controllerFactory = $controller;
    $q = _$q_;
  }));

  beforeEach(function() {
    mockRequisitionsService.getAvailablePolicies = jasmine.createSpy('getAvailablePolicies');
    var policies = $q.defer();
    policies.resolve([{
      "name": "Match IP Interface",
      "class": "org.opennms.netmgt.provision.persist.policies.MatchingIpInterfacePolicy",
      "parameters": [{
        "key": "matchBehavior",
        "required": true,
        "options": ["ALL_PARAMETERS", "ANY_PARAMETER", "NO_PARAMETERS"]
      }, {
        "key": "action",
        "required": true,
        "options": ["DISABLE_COLLECTION", "DISABLE_SNMP_POLL", "DO_NOT_PERSIST", "ENABLE_COLLECTION", "ENABLE_SNMP_POLL", "MANAGE", "UNMANAGE"]
      }, {
        "key": "hostName",
        "required": false,
        "options": []
      }, {
        "key": "ipAddress",
        "required": false,
        "options": []
      }]
    }]);
    mockRequisitionsService.getAvailablePolicies.andReturn(policies.promise);

    mockModalInstance = {
      close: function(obj) { console.info(obj); },
      dismiss: function(msg) { console.info(msg); }
    };
  });

  it('test controller', function() {
    createController();
    scope.$digest();
    expect(scope.policy.name).toBe(policy.name);
  });

});