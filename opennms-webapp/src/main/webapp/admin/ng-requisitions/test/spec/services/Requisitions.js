/*global RequisitionNode:true, Requisition:true */
/*jshint sub:true */

/**
* @author Alejandro Galue <agalue@opennms.org>
* @copyright 2014 The OpenNMS Group, Inc.
*/

'use strict';

describe('Service: RequisitionsService', function () {

  var deployedRequisitions = {
    'count': 2,
    'totalCount': 2,
    'model-import': [{
      'foreign-source' : 'test-network',
      'node': [{
        'foreign-id': '1001',
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
      }, {
        'foreign-id': '1003',
        'node-label': 'testing-switch',
        'interface': [{
          'ip-addr': '10.0.0.3',
          'descr': 'Fa0/0',
          'snmp-primary': 'P',
          'status': '1'
        }]
      }]
    },{
      'foreign-source' : 'test-monitoring',
      'node': [{
        'foreign-id': 'onms',
        'node-label': 'onms-server',
        'interface': [{
          'ip-addr': '11.0.0.1',
          'descr': 'eth0',
          'snmp-primary': 'P',
          'status': '1'
        }]
      }]
    }]
  };

  var pendingRequisitions = {
    'count': 2,
    'totalCount': 2,
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
    }]
  };

  // Initialize testing environment

  var scope, $httpBackend, requisitionsService;

  var initializeCache = function() {
    var results = [{ data: pendingRequisitions }, { data: deployedRequisitions }];
    requisitionsService.internal.mergeRequisitions(results);
  };

  beforeEach(module('onms-requisitions', function($provide) {
    $provide.value('$log', console);
  }));

  beforeEach(inject(function($injector) {
    scope = $injector.get('$rootScope').$new();
    $httpBackend = $injector.get('$httpBackend');
    requisitionsService = $injector.get('RequisitionsService');
  }));

  afterEach(function() {
    $httpBackend.verifyNoOutstandingExpectation();
    $httpBackend.verifyNoOutstandingRequest();
  });

  // Testing getRequisitions

  it('getRequisitions', function() {
    console.log('Running tests for getRequisitions');

    var requisitionsUrl = requisitionsService.internal.requisitionsUrl;
    $httpBackend.expect('GET', requisitionsUrl).respond(pendingRequisitions);
    $httpBackend.expect('GET', requisitionsUrl + '/deployed').respond(deployedRequisitions);

    var handlerFn = function(data) {
      expect(data).not.toBe(null);
      expect(data.status).not.toBe(null);
      expect(data.status.deployed).toBe(2);
      expect(data.status.pending).toBe(2);
      expect(data.requisitions.length).toBe(3);
      expect(data.requisitions[0].foreignSource).toBe('test-network');
      expect(data.requisitions[0].deployed).toBe(false);
      expect(data.requisitions[0].nodes.length).toBe(3);
      expect(data.requisitions[0].nodes[0].foreignId).toBe('1001');
      expect(data.requisitions[0].nodes[0].deployed).toBe(true);  // unmodified
      expect(data.requisitions[0].nodes[1].foreignId).toBe('1003');
      expect(data.requisitions[0].nodes[1].deployed).toBe(false); // modified
      expect(data.requisitions[0].nodes[2].foreignId).toBe('1002');
      expect(data.requisitions[0].nodes[2].deployed).toBe(false); // new
      expect(data.requisitions[1].foreignSource).toBe('test-monitoring');
      expect(data.requisitions[1].deployed).toBe(true);
      expect(data.requisitions[1].nodes.length).toBe(1);
    };

    requisitionsService.getRequisitions().then(handlerFn);

    $httpBackend.flush();

    expect(requisitionsService.internal.getCachedRequisitionsData()).not.toBe(null);

    // The following calls should use internal cache

    requisitionsService.getRequisitions().then(handlerFn);
    requisitionsService.getRequisition('test-network').then(function(r) {
      expect(r).not.toBe(null);
      expect(r.foreignSource).toBe('test-network');
    });
    requisitionsService.getNode('test-network', '1001').then(function(n) {
      expect(n).not.toBe(null);
      expect(n.foreignId).toBe('1001');
    });

  });

  // Testing Cache

  it('internal.cache', function() {
    console.log('Running tests for cache');

    initializeCache();
    var requisitionsData = requisitionsService.internal.getCachedRequisitionsData();
    var requisition = requisitionsService.internal.getCachedRequisition('test-network');
    var node = requisitionsService.internal.getCachedNode('test-network', '1001');
    expect(requisitionsData).not.toBe(null);
    expect(requisition).not.toBe(null);
    expect(node).not.toBe(null);
  });

  // Testing getRequisition::fromServer

  it('getRequisition::fromServer', function() {
    console.log('Running tests for getRequisition from server');

    var req = pendingRequisitions['model-import'][0];
    var fs  = req['foreign-source'];
    var requisitionUrl = requisitionsService.internal.requisitionsUrl + '/' + fs;
    $httpBackend.expect('GET', requisitionUrl).respond(req);

    var handlerFn = function(data) {
      expect(data).not.toBe(null);
      expect(data.nodes.length).toBe(3);
    };

    requisitionsService.getRequisition(fs).then(handlerFn);

    $httpBackend.flush();
  });

  // Testing synchronizeRequisition

  it('synchronizeRequisition', function() {
    console.log('Running tests for synchronizeRequisition');

    var foreignSource = 'test-requisition';
    var importUrl = requisitionsService.internal.requisitionsUrl + '/' + foreignSource + '/import?rescanExisting=false';
    $httpBackend.expect('PUT', importUrl).respond({});

    requisitionsService.synchronizeRequisition(foreignSource, "false");
    $httpBackend.flush();
  });

  // Testing synchronizeRequisition (unknown requisition)

  it('synchronizeRequisition::unkonwnRequisition', function() {
    console.log('Running tests for synchronizeRequisition (unknown requisition)');

    initializeCache();

    var foreignSource = 'blah-blah';
    requisitionsService.synchronizeRequisition(foreignSource).then(function() {
      throw 'This is not expected';
    }, function(msg) {
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
    });
    $httpBackend.flush();
  });

  // Testing addRequisition (existing requisition)

  it('addRequisition::existingRequisition', function() {
    console.log('Running tests for addRequisition (existing requisition)');

    initializeCache();

    var foreignSource = 'test-network';
    requisitionsService.addRequisition(foreignSource).then(function() {
      throw 'This is not expected';
    }, function(msg) {
      expect(msg).toBe('Invalid foreignSource ' + foreignSource + ', it already exist.');
    });
  });

  // Testing deleteRequisition (deployed requisition with nodes in database)

  it('deleteRequisition::deployed', function() {
    console.log('Running tests for deleteRequisition (deployed requisition with nodes in database)');

    initializeCache();

    var foreignSource = 'test-network';
    requisitionsService.deleteRequisition(foreignSource).then(function() {
      throw 'This is not expected';
    }, function(msg) {
      expect(msg).toBe('The foreignSource ' + foreignSource + ' contains 2 nodes on the database, it cannot be deleted.');
    });
  });

  // Testing deleteRequisition (unknown requisition)

  it('deleteRequisition::unkonwn', function() {
    console.log('Running tests for deleteRequisition (unknown requisition)');

    initializeCache();

    var foreignSource = 'blah-blah';
    requisitionsService.deleteRequisition(foreignSource).then(function() {
      throw 'This is not expected';
    }, function(msg) {
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

    requisitionsService.deleteRequisition(foreignSource).then(function() {}, function() {
      throw 'This is not expected';
    });
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

    requisitionsService.deleteRequisition(foreignSource).then(function() {}, function(msg) {
      throw msg;
    });
    $httpBackend.flush();

    r = requisitionsService.internal.getCachedRequisition(foreignSource);
    expect(r).toBe(null);
  });

  // Testing removeAllNodesFromRequisition

  it('removeAllNodesFromRequisition', function() {
    console.log('Running tests for removeAllNodesFromRequisition');

    initializeCache();

    var requisition = {'foreign-source': 'test-network', node: []};
    var saveUrl = requisitionsService.internal.requisitionsUrl;
    $httpBackend.expect('POST', saveUrl, requisition).respond({});

    requisitionsService.removeAllNodesFromRequisition('test-network');
    $httpBackend.flush();

    var r = requisitionsService.internal.getCachedRequisition('test-network');
    expect(r.nodes.length).toBe(0);
    expect(r.nodesDefined).toBe(0);
    expect(r.deployed).toBe(false);
  });

  // Testing getNode from server

  it('getNode::fromServer', function() {
    console.log('Running tests for getNode (from server)');

    var req  = pendingRequisitions['model-import'][0];
    var node = req['node'][0];
    var fs   = req['foreign-source'];
    var fid  = node['foreign-id'];
    var nodeUrl = requisitionsService.internal.requisitionsUrl + '/' + fs + '/nodes/' + fid;
    $httpBackend.expect('GET', nodeUrl).respond(node);

    var handlerFn = function(data) {
      expect(data).not.toBe(null);
      expect(data.nodeLabel).toBe('testing-server');
    };

    requisitionsService.getNode(fs, fid).then(handlerFn);

    $httpBackend.flush();
  });

  // Testing getNode from cache

  it('getNode::fromCache', function() {
    console.log('Running tests for getNode (from cache)');

    initializeCache();

    var handlerFn = function(data) {
      expect(data).not.toBe(null);
      expect(data.nodeLabel).toBe('testing-server');
    };

    requisitionsService.getNode('test-network', '1001').then(handlerFn);
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

    requisitionsService.saveNode(node);
    $httpBackend.flush();

    expect(requisition.deployed).toBe(false);
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

    requisitionsService.deleteNode(node);
    $httpBackend.flush();

    expect(requisition.deployed).toBe(false);
    expect(requisition.nodes.length).toBe(nodeCount - 1);
    expect(requisition.nodesDefined).toBe(pendingCount - 1);
  });

  // Get services

  it('getAvailableServices', function() {
    console.log('Running tests for getAvailableServices');

    initializeCache();

    var services = ['ICMP','SNMP'];
    var url = requisitionsService.internal.foreignSourcesConfigUrl + '/services/test-server';
    $httpBackend.expect('GET', url).respond(services);

    requisitionsService.getAvailableServices('test-server').then(function(data) {
      expect(data).not.toBe(null);
      expect(data.length).toBe(2);
      expect(data[1]).toBe('SNMP');
    }, function(msg) {
      throw 'This is not expected';
    });

    $httpBackend.flush();
  });

  // Get assets

  it('getAvailableAssets', function() {
    console.log('Running tests for getAvailableAssets');

    initializeCache();

    var assets = ['address1','city','state','zip'];
    var url = requisitionsService.internal.foreignSourcesConfigUrl + '/assets';
    $httpBackend.expect('GET', url).respond(assets);

    requisitionsService.getAvailableAssets().then(function(data) {
      expect(data).not.toBe(null);
      expect(data.length).toBe(4);
      expect(data[1]).toBe('city');
    }, function(msg) {
      throw 'This is not expected';
    });

    $httpBackend.flush();
  });

  // Get categories

  it('getAvailableCategories', function() {
    console.log('Running tests for getAvailableCategories');

    initializeCache();

    var categories = ['Production','Development','Testing'];
    var url = requisitionsService.internal.foreignSourcesConfigUrl + '/categories';
    $httpBackend.expect('GET', url).respond(categories);

    requisitionsService.getAvailableCategories().then(function(data) {
      expect(data).not.toBe(null);
      expect(data.length).toBe(3);
      expect(data[1]).toBe('Development');
    }, function(msg) {
      throw 'This is not expected';
    });

    $httpBackend.flush();
  });

  // Get policies

  it('getAvailablePolicies', function() {
    console.log('Running tests for getAvailablePolicies');

    initializeCache();

    var policies = [{
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
    }];

    var url = requisitionsService.internal.foreignSourcesConfigUrl + '/policies';
    $httpBackend.expect('GET', url).respond(policies);

    requisitionsService.getAvailablePolicies().then(function(data) {
      expect(data).not.toBe(null);
      expect(data.length).toBe(1);
      expect(data[0].name).toBe('Match IP Interface');
      expect(data[0].parameters.length).toBe(4);
    }, function(msg) {
      throw 'This is not expected';
    });

    $httpBackend.flush();
  });

  // Get detectors

  it('getAvailableDetectors', function() {
    console.log('Running tests for getAvailableDetectors');

    initializeCache();

    var detectors = [{
      "name": "ICMP",
      "class": "org.opennms.netmgt.provision.detector.icmp.IcmpDetector",
      "parameters": [{
        "key": "port",
        "required": false,
        "options": []
      }, {
        "key": "ipMatch",
        "required": false,
        "options": []
      }, {
        "key": "retries",
        "required": false,
        "options": []
      }, {
        "key": "timeout",
        "required": false,
        "options": []
      }, {
        "key": "serviceName",
        "required": false,
        "options": []
      }]
    }];
    var url = requisitionsService.internal.foreignSourcesConfigUrl + '/detectors';
    $httpBackend.expect('GET', url).respond(detectors);

    requisitionsService.getAvailableDetectors().then(function(data) {
      expect(data).not.toBe(null);
      expect(data.length).toBe(1);
      expect(data[0].name).toBe('ICMP');
      expect(data[0].parameters.length).toBe(5);
    }, function(msg) {
      throw 'This is not expected';
    });

    $httpBackend.flush();
  });

  // Test Foreign ID on Requisition

  it('isForeignIdOnRequisition', function() {
    console.log('Running tests for isForeignIdOnRequisition');

    initializeCache();

    requisitionsService.isForeignIdOnRequisition('test-network', '1001').then(function(exist) {
      expect(exist).toBe(true);
    }, function(msg) {
      throw 'This is not expected';
    });

    requisitionsService.isForeignIdOnRequisition('test-network', '1004').then(function(exist) {
      expect(exist).toBe(false);
    }, function(msg) {
      throw 'This is not expected';
    });
  });

  // Test IP Address on Node

  it('isIpAddressOnNode', function() {
    console.log('Running tests for isIpAddressOnNode');

    initializeCache();

    requisitionsService.isIpAddressOnNode('test-network', '1001', '10.0.0.1').then(function(exist) {
      expect(exist).toBe(true);
    }, function(msg) {
      throw 'This is not expected';
    });

    requisitionsService.isIpAddressOnNode('test-network', '1001', '10.0.0.2').then(function(exist) {
      expect(exist).toBe(false);
    }, function(msg) {
      throw 'This is not expected';
    });
  });

  // Test Category on Node

  it('isCategoryOnNode', function() {
    console.log('Running tests for isCategoryOnNode');

    initializeCache();

    requisitionsService.isCategoryOnNode('test-network', '1001', 'Servers').then(function(exist) {
      expect(exist).toBe(true);
    }, function(msg) {
      throw 'This is not expected';
    });

    requisitionsService.isCategoryOnNode('test-network', '1001', 'Router').then(function(exist) {
      expect(exist).toBe(false);
    }, function(msg) {
      throw 'This is not expected';
    });
  });

  // Test Service on Node

  it('isServiceOnNode', function() {
    console.log('Running tests for isServiceOnNode');

    initializeCache();

    requisitionsService.isServiceOnNode('test-network', '1001', '10.0.0.1', 'ICMP').then(function(exist) {
      expect(exist).toBe(true);
    }, function(msg) {
      throw 'This is not expected';
    });

    requisitionsService.isServiceOnNode('test-network', '1001', '10.0.0.2', 'HTTP').then(function(exist) {
      expect(exist).toBe(false);
    }, function(msg) {
      throw 'This is not expected';
    });
  });


});
