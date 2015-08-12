/**
* @author Alejandro Galue <agalue@opennms.org>
* @copyright 2014 The OpenNMS Group, Inc.
*/

'use strict';

describe('Controller: DetectorController', function () {

  var scope, $q, controllerFactory, mockModalInstance, mockRequisitionsService = {},
    detector = { 'name': 'HTTP', 'class': 'org.opennms.netmgt.provision.http.HttpDetector', 'parameters': [] };

  function createController() {
    return controllerFactory('DetectorController', {
      $scope: scope,
      $modalInstance: mockModalInstance,
      RequisitionsService: mockRequisitionsService,
      detector: detector
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
    mockRequisitionsService.getAvailableDetectors = jasmine.createSpy('getAvailableDetectors');
    var detectors = $q.defer();
    detectors.resolve([{
      "name": "ICMP",
      "class": "org.opennms.netmgt.provision.detector.icmp.IcmpDetector",
      "parameters": [{"key": "port"}, {"key": "ipMatch"}, {"key": "retries"}, {"key": "timeout"}]
    },{
      "name": "SNMP",
      "class": "org.opennms.netmgt.provision.detector.snmp.SnmpDetector",
      "parameters": [{"key": "port"}, {"key": "vbvalue"}, {"key": "oid"}, {"key": "ipMatch"}, {"key": "retries"}, {"key": "agentConfigFactory"}, {"key": "timeout"}]
    }]);
    mockRequisitionsService.getAvailableDetectors.andReturn(detectors.promise);

    mockModalInstance = {
      close: function(obj) { console.info(obj); },
      dismiss: function(msg) { console.info(msg); }
    };
  });

  it('test controller', function() {
    createController();
    scope.$digest();
    expect(scope.detector.name).toBe(detector.name);
    expect(scope.detector.class).toBe(detector.class);
    expect(scope.availableDetectors.length).toBe(2);
    expect(scope.availableDetectors[0].name).toBe('ICMP');
    expect(scope.availableDetectors[1].name).toBe('SNMP');

    // Auto-select the class for a specific detector implementation based on the name
    scope.setClassForName({'name': 'ICMP', 'class': 'org.opennms.netmgt.provision.detector.icmp.IcmpDetector', 'parameters': []});
    expect(scope.detector.class).toBe('org.opennms.netmgt.provision.detector.icmp.IcmpDetector');

    // Clear the detector class for an unknown or new service name.
    scope.setClassForName({'name': 'PostgreSQL', 'class': 'org.opennms.netmgt.provision.detector.tcp.TcpDetector', 'parameters': [{'port': '5432'}]});
    expect(scope.detector.class).toBe('org.opennms.netmgt.provision.detector.tcp.TcpDetector');

    // Auto-select the name for a specific detector implementation if the name is not set.
    scope.detector.name = null;
    scope.setNameForClass({'name': 'ICMP', 'class': 'org.opennms.netmgt.provision.detector.snmp.SnmpDetector', 'parameters': []});
    expect(scope.detector.name).toBe('ICMP');

    // Do not touch the detector name after selecting an implementation if it was already set.
    scope.setNameForClass({'name': 'MySQL', 'class': 'org.opennms.netmgt.provision.detector.tcp.TcpDetector', 'parameters': [{'port': '3306'}]});
    expect(scope.detector.name).toBe('ICMP');
  });

});