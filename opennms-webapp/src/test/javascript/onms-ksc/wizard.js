/**
* @author Alejandro Galue <agalue@opennms.org>
* @copyright 2016 The OpenNMS Group, Inc.
*/

'use strict';

var topLevelResources = {
    "offset": null,
    "count": 3,
    "totalCount": 3,
    "resource": [{
        "id": "node[Cisco%3ABarcelona]",
        "label": "Barcelona",
        "name": "Cisco:Barcelona",
        "link": "element/node.jsp?node=Cisco:Barcelona",
        "typeLabel": "Node",
        "parentId": null,
        "stringPropertyAttributes": {},
        "externalValueAttributes": {},
        "rrdGraphAttributes": {}
    }, {
        "id": "node[Cisco%3AMerida]",
        "label": "Merida",
        "name": "Cisco:Merida",
        "link": "element/node.jsp?node=Cisco:Merida",
        "typeLabel": "Node",
        "parentId": null,
        "stringPropertyAttributes": {},
        "externalValueAttributes": {},
        "rrdGraphAttributes": {}
    }, {
        "id": "node[Cisco%3AValencia]",
        "label": "Valencia",
        "name": "Cisco:Valencia",
        "link": "element/node.jsp?node=Cisco:Valencia",
        "typeLabel": "Node",
        "parentId": null,
        "stringPropertyAttributes": {},
        "externalValueAttributes": {},
        "rrdGraphAttributes": {}
    }]
};

var resources = {
    "id": "node[Cisco%3ABarcelona]",
    "label": "Barcelona",
    "name": "Cisco:Barcelona",
    "link": "element/node.jsp?node=Cisco:Barcelona",
    "typeLabel": "Node",
    "parentId": null,
    "children": {
        "offset": null,
        "count": 2,
        "totalCount": 2,
        "resource": [{
            "id": "node[Cisco%3ABarcelona].nodeSnmp[]",
            "label": "Node-level Performance Data",
            "name": "",
            "link": null,
            "typeLabel": "SNMP Node Data",
            "parentId": "node[Cisco%3ABarcelona]",
            "children": {
                "offset": null,
                "count": null,
                "totalCount": null,
                "resource": []
            },
            "stringPropertyAttributes": {},
            "externalValueAttributes": {},
            "rrdGraphAttributes": {
                "tcpCurrEstab": {
                    "name": "tcpCurrEstab",
                    "relativePath": "snmp/fs/Cisco/Barcelona",
                    "rrdFile": "mib2-tcp.rrd"
                },
                "tcpRetransSegs": {
                    "name": "tcpRetransSegs",
                    "relativePath": "snmp/fs/Cisco/Barcelona",
                    "rrdFile": "mib2-tcp.rrd"
                },
                "tcpEstabResets": {
                    "name": "tcpEstabResets",
                    "relativePath": "snmp/fs/Cisco/Barcelona",
                    "rrdFile": "mib2-tcp.rrd"
                }
            }
        },{
            "id": "node[Cisco%3ABarcelona].interfaceSnmp[Fa0_0-ca0731630008]",
            "label": "Fa0/0 (100.0.0.10, 100 Mbps)",
            "name": "Fa0_0-ca0731630008",
            "link": "element/interface.jsp?ipinterfaceid=1893",
            "typeLabel": "SNMP Interface Data",
            "parentId": "node[Cisco%3ABarcelona]",
            "children": {
                "offset": null,
                "count": null,
                "totalCount": null,
                "resource": []
            },
            "stringPropertyAttributes": {
                "ifHighSpeed": "100",
                "ifName": "Fa0/0",
                "ifSpeed": "100000000",
                "ifDescr": "FastEthernet0/0"
            },
            "externalValueAttributes": {
                "ifSpeed": "100000000",
                "ifSpeedFriendly": "100 Mbps"
            },
            "rrdGraphAttributes": {
                "ifHCInOctets": {
                    "name": "ifHCInOctets",
                    "relativePath": "snmp/fs/Cisco/Barcelona/Fa0_0-ca0731630008",
                    "rrdFile": "mib2-X-interfaces.rrd"
                },
                "ifHCOutOctets": {
                    "name": "ifHCOutOctets",
                    "relativePath": "snmp/fs/Cisco/Barcelona/Fa0_0-ca0731630008",
                    "rrdFile": "mib2-X-interfaces.rrd"
                }
            }
        }]
    }
};

var kscList = {
    "offset": null,
    "count": 2,
    "totalCount": 2,
    "kscReport": [{
        "id": 0,
        "label": "Test 01",
        "show_timespan_button": null,
        "show_graphtype_button": null,
        "graphs_per_line": null,
        "kscGraph": []
    }, {
        "id": 2,
        "label": "Test 02",
        "show_timespan_button": null,
        "show_graphtype_button": null,
        "graphs_per_line": null,
        "kscGraph": []
    }]
};

describe('Controller: KSCResourceCtrl', function () {

  var createController, scope, httpBackend;

  beforeEach(module('onms-ksc-wizard', function($provide) {
    $provide.value('$log', console);
  }));

  beforeEach(inject(function($rootScope, $httpBackend, $controller) {
    scope = $rootScope.$new();
    httpBackend = $httpBackend;
    createController = function() {
      return $controller('KSCResourceCtrl', {
        '$scope': scope
      });
    };
  }));

  afterEach(function() {
    httpBackend.verifyNoOutstandingExpectation();
    httpBackend.verifyNoOutstandingRequest();
  });

  it('Test Resources', function() {
    console.log('Testing KSCResourceCtrl: Test Resources');
    createController();

    // Validate Level 0

    httpBackend.expect('GET', 'rest/resources?depth=0').respond(topLevelResources);
    httpBackend.flush();
    expect(scope.level).toEqual(0);
    expect(scope.selectedNode).toEqual(null);
    expect(scope.selectedResource).toEqual(null);
    expect(scope.numPages).toEqual(1);
    expect(scope.resources.length).toEqual(topLevelResources.resource.length);
    expect(scope.resources[0].label).toEqual("Barcelona");
    expect(scope.filteredResources).toEqual(scope.resources);

    // Validate Level 1

    var selectedResource = topLevelResources.resource[0];
    scope.selectResource(selectedResource);
    var id = scope.getSelectedId();
    httpBackend.expect('GET', 'rest/resources/'+id).respond(resources);
    scope.setLevel(1);
    httpBackend.flush();
    expect(scope.level).toEqual(1);
    expect(scope.selectedNode).toEqual(selectedResource);
    expect(scope.selectedResource).toEqual(selectedResource);
    expect(scope.numPages).toEqual(1);
    expect(scope.resources.length).toEqual(resources.children.resource.length);
    expect(scope.resources[0].label).toEqual("Node-level Performance Data");
    expect(scope.filteredResources).toEqual(scope.resources);

    // Validate Level 2

    scope.setLevel(2);
    expect(scope.level).toEqual(2);
    expect(scope.resources.length).toEqual(0);
    expect(scope.filteredResources.length).toEqual(0);
    expect(scope.selectedNode).toEqual(selectedResource);
    expect(scope.selectedResource).toEqual(selectedResource);

    // Go back to Level 1

    httpBackend.expect('GET', 'rest/resources/'+id).respond(resources);
    scope.goBack();
    httpBackend.flush();
    expect(scope.level).toEqual(1);
    expect(scope.selectedNode).toEqual(selectedResource);
    expect(scope.selectedResource).toEqual(selectedResource);
    expect(scope.resources.length).toEqual(resources.children.resource.length);
    expect(scope.resources[0].label).toEqual("Node-level Performance Data");
    expect(scope.filteredResources).toEqual(scope.resources);

    // Go back to Level 0

    httpBackend.expect('GET', 'rest/resources?depth=0').respond(topLevelResources);
    scope.goBack();
    httpBackend.flush();
    expect(scope.level).toEqual(0);
    expect(scope.resources.length).toEqual(topLevelResources.resource.length);
    expect(scope.resources[0].label).toEqual("Barcelona");
    expect(scope.filteredResources).toEqual(scope.resources);
  });

});

describe('Controller: KSCWizardCtrl', function () {

  var createController, scope, httpBackend;

  beforeEach(module('onms-ksc-wizard', function($provide) {
    $provide.value('$log', console);
  }));

  beforeEach(inject(function($rootScope, $httpBackend, $controller) {
    scope = $rootScope.$new();
    httpBackend = $httpBackend;
    createController = function() {
      return $controller('KSCWizardCtrl', {
        '$scope': scope
      });
    };
  }));

  afterEach(function() {
    httpBackend.verifyNoOutstandingExpectation();
    httpBackend.verifyNoOutstandingRequest();
  });

  it('Test Wizard', function() {
    console.log('Testing KSCWizardCtrl: Test Wizard');
    createController();

    httpBackend.expect('GET', 'rest/resources?depth=0').respond(topLevelResources);
    httpBackend.expect('GET', 'rest/ksc').respond(kscList);
    httpBackend.flush();

    expect(scope.resources.length).toEqual(topLevelResources.resource.length);

    expect(scope.resources[0].label).toEqual("Barcelona");
    expect(scope.filteredResources).toEqual(scope.resources);
    expect(scope.numPages).toEqual(1);

    expect(scope.reports.length).toEqual(kscList.kscReport.length);
    expect(scope.reports[0].label).toEqual("Test 01");
    expect(scope.filteredReports).toEqual(scope.reports);
    expect(scope.kscNumPages).toEqual(1);
  });

});
