/*global RequisitionNode:true, Requisition:true, QuickNode:true */
/*jshint sub:true */

/**
* @author Alejandro Galue <agalue@opennms.org>
* @copyright 2014 The OpenNMS Group, Inc.
*/

'use strict';

describe('Service: RequisitionsService', function () {

  var deployedStats = {
    'count': 2,
    'totalCount': 2,
    'foreign-source': [{
      'name' : 'test-network',
      'count': 2,
      'totalCount': 2,
      'foreign-id': [
        '1001',
        '1003'
      ]
    },{
      'name' : 'test-monitoring',
      'count': 1,
      'totalCount': 1,
      'foreign-id': [
        'onms'
      ]
    }]
  };

  var requisitions = {
    'count': 3,
    'totalCount': 3,
    'model-import': [{
      'foreign-source' : 'test-network', // Modified requisition
      'node': [{
        'foreign-id': '1001', // Unmodified node
        'node-label': 'testing-server',
        'building' : 'Office',
        'interface': [{
          'ip-addr': '10.0.0.1',
          'descr': 'eth0',
          'snmp-primary': 'P',
          'status': '1',
          'monitored-service': [{
            'service-name': 'ICMP'
          }]
        }],
        'asset': [{
          'name': 'address1',
          'value': '220 Chatham Business Drive'
        },{
          'name': 'city',
          'value': 'Pittsboro'
        }],
        'category': [{
          'name': 'Servers'
        }]
      },{
        'foreign-id': '1002', // New node
        'node-label': 'testing-router',
        'building' : 'Office',
        'interface': [{
          'ip-addr': '10.0.0.2',
          'descr': 'Fa0/0',
          'snmp-primary': 'P',
          'status': '1'
        }],
        'category': [{
          'name': 'Routers'
        }]
      },{
        'foreign-id': '1003', // Modified node
        'node-label': 'testing-switch',
        'interface': [{
          'ip-addr': '10.0.0.4', // New IP
          'descr': 'Fa0/1', // New Description
          'snmp-primary': 'P',
          'status': '1'
        }]
      }]
    },{
      'foreign-source' : 'test-empty', // New requisition
      'node': []
    },{
      'foreign-source' : 'test-monitoring', // Deployed requisition
      'node': [{
        'foreign-id': 'onms',
        'node-label': 'onms.local',
        'interface': [{
          'ip-addr': '192.168.0.1', // New IP
          'descr': 'eth0',
          'snmp-primary': 'P',
          'status': '1'
        }]
      }]
    }]
  };

  var foreignSourceDef = {
    'name': 'add-node-to-requisition-test',
    'date-stamp': 1458575873998,
    'scan-interval': '1d',
    'detectors': [{
      'name': 'ICMP',
      'class': 'org.opennms.netmgt.provision.detector.icmp.IcmpDetector',
      'parameter': []
    }, {
      'name': 'SNMP',
      'class': 'org.opennms.netmgt.provision.detector.snmp.SnmpDetector',
      'parameter': []
    }, {
      'name ': 'HTTP-8980',
      'class': 'org.opennms.netmgt.provision.detector.simple.HttpDetector',
      'parameter': [{
        'key': 'port',
        'value': '8980'
      }]
    }],
    'policies': [{
      'name': 'No IPs',
      'class': 'org.opennms.netmgt.provision.persist.policies.MatchingIpInterfacePolicy',
      'parameter': [{
        'key': 'action',
        'value': 'DO_NOT_PERSIST'
      }, {
        'key': 'matchBehavior',
        'value': 'NO_PARAMETERS'
      }]
    }]
  };

  var errorHandlerFn = function(msg) {
      throw 'This is not expected. ' + (msg == null ? '' : ' ' + msg);
  };

  // Initialize testing environment

  var scope, $httpBackend, requisitionsService;

  var initializeCache = function() {
    var requisitionsUrl = requisitionsService.internal.requisitionsUrl;
    $httpBackend.expect('GET', requisitionsUrl).respond(requisitions);
    $httpBackend.expect('GET', requisitionsUrl + '/deployed/stats').respond(deployedStats);
    requisitionsService.getRequisitions().then(function() { console.log('Cache updated!'); });
    $httpBackend.flush();
  };

  beforeEach(module('onms-requisitions', function($provide) {
    $provide.value('$log', console);
  }));

  beforeEach(inject(function($injector) {
    scope = $injector.get('$rootScope').$new();
    $httpBackend = $injector.get('$httpBackend');
    $httpBackend.whenGET('views/requisitions.html').respond(200, '');
    requisitionsService = $injector.get('RequisitionsService');
  }));

  afterEach(function() {
    $httpBackend.verifyNoOutstandingExpectation();
    $httpBackend.verifyNoOutstandingRequest();
  });

  // Testing cache related functions

  it('getCached*', function() {
    console.log('Running tests for cache');

    initializeCache();
    var requisitionsData = requisitionsService.internal.getCachedRequisitionsData();
    var requisition = requisitionsService.internal.getCachedRequisition('test-network');
    var node = requisitionsService.internal.getCachedNode('test-network', '1001');
    expect(requisitionsData).not.toBe(undefined);
    expect(requisition).not.toBe(null);
    expect(node).not.toBe(null);

    requisitionsService.clearCache();
    requisitionsData = requisitionsService.internal.getCachedRequisitionsData();
    requisition = requisitionsService.internal.getCachedRequisition('test-network');
    node = requisitionsService.internal.getCachedNode('test-network', '1001');
    expect(requisitionsData).toBe(undefined);
    expect(requisition).toBe(null);
    expect(node).toBe(null);
  });

  // Testing getRequisitions

  it('getRequisitions', function() {
    console.log('Running tests for getRequisitions');

    var requisitionsUrl = requisitionsService.internal.requisitionsUrl;
    $httpBackend.expect('GET', requisitionsUrl).respond(requisitions);
    $httpBackend.expect('GET', requisitionsUrl + '/deployed/stats').respond(deployedStats);

    var handlerFn = function(data) {
      expect(data).not.toBe(null);
      console.log(angular.toJson(data));
      expect(data.requisitions.length).toBe(3);
      expect(data.requisitions[0].foreignSource).toBe('test-network');
      expect(data.requisitions[0].deployed).toBe(true);
      expect(data.requisitions[0].nodes.length).toBe(3);
      expect(data.requisitions[0].nodes[0].foreignId).toBe('1001');
      expect(data.requisitions[0].nodes[0].deployed).toBe(true);
      expect(data.requisitions[0].nodes[1].foreignId).toBe('1002');
      expect(data.requisitions[0].nodes[1].deployed).toBe(false);
      expect(data.requisitions[0].nodes[2].foreignId).toBe('1003');
      expect(data.requisitions[0].nodes[2].deployed).toBe(true);
      expect(data.requisitions[1].foreignSource).toBe('test-empty');
      expect(data.requisitions[1].deployed).toBe(false);
      expect(data.requisitions[1].nodes.length).toBe(0);
    };

    requisitionsService.getRequisitions().then(handlerFn, errorHandlerFn);

    $httpBackend.flush();

    expect(requisitionsService.internal.getCachedRequisitionsData()).not.toBe(null);

    // The following calls should use internal cache

    requisitionsService.getRequisitions().then(handlerFn, errorHandlerFn);
    requisitionsService.getRequisition('test-network').then(function(r) {
      expect(r).not.toBe(null);
      expect(r.foreignSource).toBe('test-network');
    }, errorHandlerFn);
    requisitionsService.getNode('test-network', '1001').then(function(n) {
      expect(n).not.toBe(null);
      expect(n.foreignId).toBe('1001');
    }, errorHandlerFn);
  });

  // Testing getRequisitionNames

  it('getRequisitionNames', function() {
    console.log('Running tests for getRequisitionNames');

    var requisitionNames = {
      'count': 3,
      'totalCount': 3, 
      'foreign-source': [
        requisitions['model-import'][0]['foreign-source'],
        requisitions['model-import'][1]['foreign-source'],
        requisitions['model-import'][2]['foreign-source']
      ]
    };

    $httpBackend.expect('GET', requisitionsService.internal.requisitionNamesUrl).respond(requisitionNames);

    requisitionsService.getRequisitionNames().then(function(names) {
      expect(names.length).toBe(3);
      expect(names[2]).toBe('test-monitoring'); // 3rd requsition
    }, errorHandlerFn);

    $httpBackend.flush();
  });

  // Testing getRequisition

  it('getRequisition', function() {
    console.log('Running tests for getRequisition from server');

    var req = requisitions['model-import'][0];
    var fs  = req['foreign-source'];
    var requisitionUrl = requisitionsService.internal.requisitionsUrl + '/' + fs;
    $httpBackend.expect('GET', requisitionUrl).respond(req);

    requisitionsService.getRequisition(fs).then(function(data) {
      expect(data).not.toBe(null);
      expect(data.nodes.length).toBe(3);
    }, errorHandlerFn);

    $httpBackend.flush();
  });

  // Testing updateDeployedStatsForRequisition

  it('updateDeployedStatsForRequisition', function() {
    console.log('Running tests for updateDeployedStatsForRequisition');

    initializeCache();
    var req = requisitionsService.internal.getCachedRequisition('test-network');
    expect(req.nodesInDatabase).toBe(2);
    expect(req.nodesDefined).toBe(3);

    var stats = {
      'name' : 'test-network',
      'count': 3,
      'totalCount': 3,
      'foreign-id': [
        '1001',
        '1002',
        '1003'
      ]
    };
    var url = requisitionsService.internal.requisitionsUrl + '/deployed/stats/test-network';
    $httpBackend.expect('GET', url).respond(stats);

    requisitionsService.updateDeployedStatsForRequisition(req).then(function() {
      expect(req.nodesInDatabase).toBe(3);
      expect(req.nodesDefined).toBe(3);
    }, errorHandlerFn);

    $httpBackend.flush();
  });

  // Testing updateDeployedStats

  it('updateDeployedStats', function() {
    console.log('Running tests for updateDeployedStats');

    initializeCache();

    var stats = {
      'count': 2,
      'totalCount': 2,
      'foreign-source': [{
        'name' : 'test-network',
        'count': 3,
        'totalCount': 3,
        'foreign-id': [
          '1001',
          '1002',
          '1003'
        ]
      },{
        'name' : 'test-monitoring',
        'count': 1,
        'totalCount': 1,
        'foreign-id': [
          'onms'
        ]
      }]
    };

    var url = requisitionsService.internal.requisitionsUrl + '/deployed/stats';
    $httpBackend.expect('GET', url).respond(stats);

    var requisitionsData = requisitionsService.internal.getCachedRequisitionsData();
    requisitionsService.updateDeployedStats(requisitionsData).then(function() {
      var req = requisitionsData.getRequisition('test-network');
      expect(req.nodesInDatabase).toBe(3);
      expect(req.nodesDefined).toBe(3);
    }, errorHandlerFn);

    $httpBackend.flush();
  });

  // Testing synchronizeRequisition

  it('synchronizeRequisition', function() {
    console.log('Running tests for synchronizeRequisition');

    var foreignSource = 'test-requisition';
    var importUrl = requisitionsService.internal.requisitionsUrl + '/' + foreignSource + '/import?rescanExisting=false';
    $httpBackend.expect('PUT', importUrl).respond({});

    requisitionsService.synchronizeRequisition(foreignSource, 'false').then(function() {}, errorHandlerFn);
    $httpBackend.flush();
  });

  // Testing synchronizeRequisition (unknown requisition)

  it('synchronizeRequisition::unkonwnRequisition', function() {
    console.log('Running tests for synchronizeRequisition (unknown requisition)');

    initializeCache();

    var foreignSource = 'blah-blah';
    requisitionsService.synchronizeRequisition(foreignSource).then(errorHandlerFn, function(msg) {
      expect(msg).toBe('The foreignSource ' + foreignSource + ' does not exist.');
    });
  });

  // Testing addRequisition

  it('addRequisition', function() {
    console.log('Running tests for addRequisition');

    var foreignSource = 'test-requisition';
    var emptyReq = { 'foreign-source': foreignSource, node: [] };
    var requisition = new Requisition(emptyReq, false);
    var addUrl = requisitionsService.internal.requisitionsUrl;
    $httpBackend.expect('POST', addUrl, emptyReq).respond(requisition);

    requisitionsService.addRequisition(foreignSource).then(function(requisition) {
      expect(requisition.foreignSource).toBe(foreignSource);
    }, errorHandlerFn);
    $httpBackend.flush();
  });

  // Testing addRequisition (existing requisition)

  it('addRequisition::existingRequisition', function() {
    console.log('Running tests for addRequisition (existing requisition)');

    initializeCache();

    var foreignSource = 'test-network';
    requisitionsService.addRequisition(foreignSource).then(errorHandlerFn, function(msg) {
      expect(msg).toBe('Invalid foreignSource ' + foreignSource + ', it already exist.');
    });
  });

  // Testing deleteRequisition (deployed requisition with nodes in database)

  it('deleteRequisition::deployed', function() {
    console.log('Running tests for deleteRequisition (deployed requisition with nodes in database)');

    initializeCache();

    var foreignSource = 'test-network';
    requisitionsService.deleteRequisition(foreignSource).then(errorHandlerFn, function(msg) {
      expect(msg).toBe('The foreignSource ' + foreignSource + ' contains 2 nodes on the database, it cannot be deleted.');
    });
  });

  // Testing deleteRequisition (unknown requisition)

  it('deleteRequisition::unkonwn', function() {
    console.log('Running tests for deleteRequisition (unknown requisition)');

    initializeCache();

    var foreignSource = 'blah-blah';
    requisitionsService.deleteRequisition(foreignSource).then(errorHandlerFn, function(msg) {
      expect(msg).toBe('The foreignSource ' + foreignSource + ' does not exist.');
    });
  });

  // Testing deleteRequisition (pending)

  it('deleteRequisition::pending', function() {
    console.log('Running tests for deleteRequisition (pending)');

    initializeCache();

    var foreignSource = 'test-empty';
    var r = requisitionsService.internal.getCachedRequisition(foreignSource);
    expect(r).not.toBe(null);

    $httpBackend.expect('DELETE', requisitionsService.internal.requisitionsUrl + '/' + foreignSource).respond({});
    $httpBackend.expect('DELETE', requisitionsService.internal.requisitionsUrl + '/deployed/' + foreignSource).respond({});
    $httpBackend.expect('DELETE', requisitionsService.internal.foreignSourcesUrl + '/' + foreignSource).respond({});
    $httpBackend.expect('DELETE', requisitionsService.internal.foreignSourcesUrl + '/deployed/' + foreignSource).respond({});

    requisitionsService.deleteRequisition(foreignSource).then(function() {}, errorHandlerFn);
    $httpBackend.flush();

    r = requisitionsService.internal.getCachedRequisition(foreignSource);
    expect(r).toBe(null);
  });

  // Testing deleteRequisition (deployed)

  it('deleteRequisition::deployed-empty', function() {
    console.log('Running tests for deleteRequisition (deployed after empty it)');

    initializeCache();

    var foreignSource = 'test-network';
    var r = requisitionsService.internal.getCachedRequisition(foreignSource);
    expect(r).not.toBe(null);
    r.nodes = [];
    r.nodesInDatabase = 0;

    $httpBackend.expect('DELETE', requisitionsService.internal.requisitionsUrl + '/' + foreignSource).respond({});
    $httpBackend.expect('DELETE', requisitionsService.internal.requisitionsUrl + '/deployed/' + foreignSource).respond({});
    $httpBackend.expect('DELETE', requisitionsService.internal.foreignSourcesUrl + '/' + foreignSource).respond({});
    $httpBackend.expect('DELETE', requisitionsService.internal.foreignSourcesUrl + '/deployed/' + foreignSource).respond({});

    requisitionsService.deleteRequisition(foreignSource).then(function() {}, errorHandlerFn);
    $httpBackend.flush();

    r = requisitionsService.internal.getCachedRequisition(foreignSource);
    expect(r).toBe(null);
  });

  // Testing removeAllNodesFromRequisition

  it('removeAllNodesFromRequisition', function() {
    console.log('Running tests for removeAllNodesFromRequisition');

    initializeCache();

    var requisition = {'foreign-source': 'test-network', node: []};
    var url = requisitionsService.internal.requisitionsUrl;
    $httpBackend.expect('POST', url, requisition).respond({});
    $httpBackend.expect('PUT', url + '/test-network/import?rescanExisting=false').respond({});

    requisitionsService.removeAllNodesFromRequisition('test-network').then(function() {}, errorHandlerFn);
    $httpBackend.flush();

    var r = requisitionsService.internal.getCachedRequisition('test-network');
    expect(r.nodes.length).toBe(0);
    expect(r.nodesDefined).toBe(0);
    expect(r.isModified()).toBe(true);
  });

  // Testing getNode from server

  it('getNode::fromServer', function() {
    console.log('Running tests for getNode (from server)');

    var req  = requisitions['model-import'][0];
    var node = req['node'][0];
    var fs   = req['foreign-source'];
    var fid  = node['foreign-id'];
    var nodeUrl = requisitionsService.internal.requisitionsUrl + '/' + fs + '/nodes/' + fid;
    $httpBackend.expect('GET', nodeUrl).respond(node);

    requisitionsService.getNode(fs, fid).then(function(node) {
      expect(node).not.toBe(null);
      expect(node.nodeLabel).toBe('testing-server');
    }, errorHandlerFn);

    $httpBackend.flush();
  });

  // Testing getNode from cache

  it('getNode::fromCache', function() {
    console.log('Running tests for getNode (from cache)');

    initializeCache();

    requisitionsService.getNode('test-network', '1001').then(function(node) {
      expect(node).not.toBe(null);
      expect(node.nodeLabel).toBe('testing-server');
    }, errorHandlerFn);
  });

  // Testing saveNode

  it('saveNode', function() {
    console.log('Running tests for saveNode');

    initializeCache();

    var node = new RequisitionNode('test-network', {
      'foreign-id': '10',
      'node-label': 'test',
      'interface': [{'ip-address': '172.16.0.1', 'snmp-primary': 'P'}]
    }, false);
    var saveUrl = requisitionsService.internal.requisitionsUrl + '/test-network/nodes';
    $httpBackend.expect('POST', saveUrl, node.getOnmsRequisitionNode()).respond({});

    var requisition = requisitionsService.internal.getCachedRequisition('test-network');
    var nodeCount = requisition.nodes.length;
    var pendingCount = requisition.nodesDefined;

    requisitionsService.saveNode(node).then(function() {}, errorHandlerFn);
    $httpBackend.flush();

    expect(requisition.isModified()).toBe(true);
    expect(requisition.nodes.length).toBe(nodeCount + 1);
    expect(requisition.nodesDefined).toBe(pendingCount + 1);
  });

  // Testing deleteNode

  it('deleteNode', function() {
    console.log('Running tests for deleteNode');

    initializeCache();

    var node = requisitionsService.internal.getCachedNode('test-network', '1001');
    expect(node).not.toBe(null);
    var deleteUrl = requisitionsService.internal.requisitionsUrl + '/' + node.foreignSource + '/nodes/' + node.foreignId;
    $httpBackend.expect('DELETE', deleteUrl).respond({});

    var requisition = requisitionsService.internal.getCachedRequisition('test-network');
    var nodeCount = requisition.nodes.length;
    var pendingCount = requisition.nodesDefined;

    requisitionsService.deleteNode(node).then(function() {}, errorHandlerFn);
    $httpBackend.flush();

    expect(requisition.isModified()).toBe(true);
    expect(requisition.nodes.length).toBe(nodeCount - 1);
    expect(requisition.nodesDefined).toBe(pendingCount - 1);
  });

  // Testing getAvailableServices

  it('getAvailableServices', function() {
    console.log('Running tests for getAvailableServices');

    initializeCache();

    var services = { element: ['ICMP','SNMP'] };
    var url = requisitionsService.internal.foreignSourcesConfigUrl + '/services/test-server';
    $httpBackend.expect('GET', url).respond(services);

    requisitionsService.getAvailableServices('test-server').then(function(data) {
      expect(data).not.toBe(null);
      expect(data.length).toBe(2);
      expect(data[1]).toBe('SNMP');
    }, errorHandlerFn);

    $httpBackend.flush();
  });

  // Testing getAvailableAssets

  it('getAvailableAssets', function() {
    console.log('Running tests for getAvailableAssets');

    initializeCache();

    var assets = { element: ['address1','city','state','zip'] };
    var url = requisitionsService.internal.foreignSourcesConfigUrl + '/assets';
    $httpBackend.expect('GET', url).respond(assets);

    requisitionsService.getAvailableAssets().then(function(data) {
      expect(data).not.toBe(null);
      expect(data.length).toBe(4);
      expect(data[1]).toBe('city');
    }, errorHandlerFn);

    $httpBackend.flush();
  });

  // Testing getAvailableCategories

  it('getAvailableCategories', function() {
    console.log('Running tests for getAvailableCategories');

    initializeCache();

    var categories = { element: ['Production','Development','Testing'] };
    var url = requisitionsService.internal.foreignSourcesConfigUrl + '/categories';
    $httpBackend.expect('GET', url).respond(categories);

    requisitionsService.getAvailableCategories().then(function(data) {
      expect(data).not.toBe(null);
      expect(data.length).toBe(3);
      expect(data[1]).toBe('Development');
    }, errorHandlerFn);

    $httpBackend.flush();
  });

  // Testing getAvailablePolicies

  it('getAvailablePolicies', function() {
    console.log('Running tests for getAvailablePolicies');

    initializeCache();

    var policies = { plugins: [{
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
    }]};

    var url = requisitionsService.internal.foreignSourcesConfigUrl + '/policies';
    $httpBackend.expect('GET', url).respond(policies);

    requisitionsService.getAvailablePolicies().then(function(data) {
      expect(data).not.toBe(null);
      expect(data.length).toBe(1);
      expect(data[0].name).toBe('Match IP Interface');
      expect(data[0].parameters.length).toBe(4);
    }, errorHandlerFn);

    $httpBackend.flush();
  });

  // Testing getAvailableDetectors

  it('getAvailableDetectors', function() {
    console.log('Running tests for getAvailableDetectors');

    initializeCache();

    var detectors = { plugins: [{
      'name': 'ICMP',
      'class': 'org.opennms.netmgt.provision.detector.icmp.IcmpDetector',
      'parameters': [{
        'key': 'port',
        'required': false,
        'options': []
      }, {
        'key': 'ipMatch',
        'required': false,
        'options': []
      }, {
        'key': 'retries',
        'required': false,
        'options': []
      }, {
        'key': 'timeout',
        'required': false,
        'options': []
      }, {
        'key': 'serviceName',
        'required': false,
        'options': []
      }]
    }]};
    var url = requisitionsService.internal.foreignSourcesConfigUrl + '/detectors';
    $httpBackend.expect('GET', url).respond(detectors);

    requisitionsService.getAvailableDetectors().then(function(data) {
      expect(data).not.toBe(null);
      expect(data.length).toBe(1);
      expect(data[0].name).toBe('ICMP');
      expect(data[0].parameters.length).toBe(5);
    }, errorHandlerFn);

    $httpBackend.flush();
  });

  // Testing isForeignIdOnRequisition

  it('isForeignIdOnRequisition', function() {
    console.log('Running tests for isForeignIdOnRequisition');

    initializeCache();

    requisitionsService.isForeignIdOnRequisition('test-network', '1001').then(function(exist) {
      expect(exist).toBe(true);
    }, errorHandlerFn);

    requisitionsService.isForeignIdOnRequisition('test-network', '1004').then(function(exist) {
      expect(exist).toBe(false);
    }, errorHandlerFn);
  });

  // Testing isIpAddressOnNode

  it('isIpAddressOnNode', function() {
    console.log('Running tests for isIpAddressOnNode');

    initializeCache();

    requisitionsService.isIpAddressOnNode('test-network', '1001', '10.0.0.1').then(function(exist) {
      expect(exist).toBe(true);
    }, errorHandlerFn);

    requisitionsService.isIpAddressOnNode('test-network', '1001', '10.0.0.2').then(function(exist) {
      expect(exist).toBe(false);
    }, errorHandlerFn);
  });

  // Testing isCategoryOnNode

  it('isCategoryOnNode', function() {
    console.log('Running tests for isCategoryOnNode');

    initializeCache();

    requisitionsService.isCategoryOnNode('test-network', '1001', 'Servers').then(function(exist) {
      expect(exist).toBe(true);
    }, errorHandlerFn);

    requisitionsService.isCategoryOnNode('test-network', '1001', 'Router').then(function(exist) {
      expect(exist).toBe(false);
    }, errorHandlerFn);
  });

  // Testing isServiceOnNode

  it('isServiceOnNode', function() {
    console.log('Running tests for isServiceOnNode');

    initializeCache();

    requisitionsService.isServiceOnNode('test-network', '1001', '10.0.0.1', 'ICMP').then(function(exist) {
      expect(exist).toBe(true);
    }, errorHandlerFn);

    requisitionsService.isServiceOnNode('test-network', '1001', '10.0.0.2', 'HTTP').then(function(exist) {
      expect(exist).toBe(false);
    }, errorHandlerFn);
  });

  // Test getForeignSourceDefinition

  it('getForeignSourceDefinition', function() {
    console.log('Running tests for getForeignSourceDefinition');

    var url = requisitionsService.internal.foreignSourcesUrl + '/default';
    $httpBackend.expect('GET', url).respond(foreignSourceDef);

    requisitionsService.getForeignSourceDefinition('default').then(function(data) {
      expect(data).not.toBe(null);
      expect(data.detectors.length).toBe(3);
      expect(data.detectors[0].name).toBe('ICMP');
      expect(data.policies.length).toBe(1);
      expect(data.policies[0].name).toBe('No IPs');
    }, errorHandlerFn);

    $httpBackend.flush();
  });

  // Testing saveForeignSourceDefinition

  it('saveForeignSourceDefinition', function() {
    console.log('Running tests for saveForeignSourceDefinition');

    var url = requisitionsService.internal.foreignSourcesUrl;
    $httpBackend.expect('POST', url).respond();

    requisitionsService.saveForeignSourceDefinition(foreignSourceDef).then(function() {}, errorHandlerFn);

    $httpBackend.flush();
  });
 
  // Test cloneForeignSourceDefinition

  it('cloneForeignSourceDefinition::unknownSource', function() {
    console.log('Running tests for cloneForeignSourceDefinition for an unknown source');

    var requisitionNames = {
      'count': 3,
      'totalCount': 3, 
      'foreign-source': [ 'Routers', 'Servers', 'Storage' ]
    };

    $httpBackend.expect('GET', requisitionsService.internal.requisitionNamesUrl).respond(requisitionNames);

    requisitionsService.cloneForeignSourceDefinition('this_does_not_exist', 'Routers').then(errorHandlerFn, function(msg) {
      expect(msg).toBe('The source requisition this_does_not_exist does not exist.');
    });

    $httpBackend.flush();
  });

  it('cloneForeignSourceDefinition::unknownDestination', function() {
    console.log('Running tests for cloneForeignSourceDefinition for an unknown destination');

    var requisitionNames = {
      'count': 3,
      'totalCount': 3, 
      'foreign-source': [ 'Routers', 'Servers', 'Storage' ]
    };

    $httpBackend.expect('GET', requisitionsService.internal.requisitionNamesUrl).respond(requisitionNames);

    requisitionsService.cloneForeignSourceDefinition('Routers', 'this_does_not_exist').then(errorHandlerFn, function(msg) {
      expect(msg).toBe('The target requisition this_does_not_exist does not exist.');
    });

    $httpBackend.flush();
  });

  it('cloneForeignSourceDefinition', function() {
    console.log('Running tests for cloneForeignSourceDefinition');

    var requisitionNames = {
      'count': 3,
      'totalCount': 3, 
      'foreign-source': [ 'Routers', 'Servers', 'Storage' ]
    };

    $httpBackend.expect('GET', requisitionsService.internal.requisitionNamesUrl).respond(requisitionNames);
    $httpBackend.expect('GET', requisitionsService.internal.foreignSourcesUrl + '/Routers').respond(foreignSourceDef);
    $httpBackend.expect('POST', requisitionsService.internal.foreignSourcesUrl).respond();

    requisitionsService.cloneForeignSourceDefinition('Routers', 'Servers').then(function(fs) {
        expect(fs.name).toBe('Servers');
    }, errorHandlerFn);

    $httpBackend.flush();
  });

  // Testing deleteForeignSourceDefinition

  it('deleteForeignSourceDefinition', function() {
    console.log('Running tests for deleteForeignSourceDefinition');

    var urlP = requisitionsService.internal.foreignSourcesUrl + '/test-requisition';
    $httpBackend.expect('DELETE', urlP).respond();
    var urlD = requisitionsService.internal.foreignSourcesUrl + '/deployed/test-requisition';
    $httpBackend.expect('DELETE', urlD).respond();

    requisitionsService.deleteForeignSourceDefinition('test-requisition').then(function() {}, errorHandlerFn);

    $httpBackend.flush();
  });

  // Testing updateSnmpCommunity

  it('updateSnmpCommunity', function() {
    console.log('Running tests for updateSnmpCommunity');

    var url = requisitionsService.internal.snmpConfigUrl + '/192.168.1.1';
    $httpBackend.expect('PUT', url, {'readCommunity' : 'my_community', 'version' : 'v2c'}).respond();

    requisitionsService.updateSnmpCommunity('192.168.1.1', 'my_community', 'v2c').then(function(ip) {
      expect(ip).toBe('192.168.1.1');
    }, errorHandlerFn);

    $httpBackend.flush();
  });

  // Testing quickAddNode without SNMP

  it('quickAddNode::noSnmp', function() {
    console.log('Running tests for quickAddNode without SNMP');

    var quickNode = new QuickNode();
    quickNode.ipAddress = '192.168.1.1';
    quickNode.foreignSource = 'test-network';
    quickNode.foreignId = '123456789';
    quickNode.nodeLabel = 'new-node.local';
    quickNode.noSnmp = true;
    var node = quickNode.createRequisitionedNode().getOnmsRequisitionNode();

    var saveUrl = requisitionsService.internal.requisitionsUrl + '/' + quickNode.foreignSource + '/nodes';
    $httpBackend.expect('POST', saveUrl, node).respond({});
    var importUrl = requisitionsService.internal.requisitionsUrl + '/' + quickNode.foreignSource + '/import?rescanExisting=false';
    $httpBackend.expect('PUT', importUrl).respond({});

    requisitionsService.quickAddNode(quickNode).then(function(n) {
      expect(n.nodeLabel).toBe('new-node.local');
    }, errorHandlerFn);

    $httpBackend.flush();
  });

  it('quickAddNode', function() {
    console.log('Running tests for quickAddNode');

    var quickNode = new QuickNode();
    quickNode.ipAddress = '192.168.1.1';
    quickNode.foreignSource = 'test-network';
    quickNode.foreignId = '123456789';
    quickNode.nodeLabel = 'new-node.local';
    quickNode.snmpCommunity = 'my_community';
    var node = quickNode.createRequisitionedNode().getOnmsRequisitionNode();

    var updateSnmpUrl = requisitionsService.internal.snmpConfigUrl + '/' + quickNode.ipAddress;
    $httpBackend.expect('PUT', updateSnmpUrl, {'readCommunity' : quickNode.snmpCommunity, 'version' : quickNode.snmpVersion}).respond();
    var saveUrl = requisitionsService.internal.requisitionsUrl + '/' + quickNode.foreignSource + '/nodes';
    $httpBackend.expect('POST', saveUrl, node).respond({});
    var importUrl = requisitionsService.internal.requisitionsUrl + '/' + quickNode.foreignSource + '/import?rescanExisting=false';
    $httpBackend.expect('PUT', importUrl).respond({});

    requisitionsService.quickAddNode(quickNode).then(function(n) {
      expect(n.nodeLabel).toBe('new-node.local');
    }, errorHandlerFn);

    $httpBackend.flush();
  });

});
