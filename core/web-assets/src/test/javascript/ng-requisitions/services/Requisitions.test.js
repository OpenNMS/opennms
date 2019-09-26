/*global RequisitionNode:true, Requisition:true, QuickNode:true */
/*jshint sub:true */

/**
* @author Alejandro Galue <agalue@opennms.org>
* @copyright 2014 The OpenNMS Group, Inc.
*/

'use strict';

const angular = require('angular-js');
require('angular-mocks');
require('../../../../../src/main/assets/js/apps/onms-requisitions/requisitions');
require('../../../../../src/main/assets/js/apps/onms-requisitions/lib/scripts/services/Requisitions');

const QuickNode = require('../../../../../src/main/assets/js/apps/onms-requisitions/lib/scripts/model/QuickNode');
const Requisition = require('../../../../../src/main/assets/js/apps/onms-requisitions/lib/scripts/model/Requisition');
const RequisitionNode = require('../../../../../src/main/assets/js/apps/onms-requisitions/lib/scripts/model/RequisitionNode');

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

var scope, $httpBackend, requisitionsService, $timeout;

const promiseify = (q) => {
  return new Promise((resolve, reject) => {
    q.then((result) => {
      //console.log('promiseify: success');
      resolve(result);
    }, (error) => {
      //console.error('promiseify: failure');
      reject(error);
    });
  });
};

const doFlush = () => {
  if ($httpBackend.flush) {
    try {
      //console.log('doFlush: flushing. ', new Date());
      return Promise.resolve($httpBackend.flush());
    } catch (error) {
      //console.log('doFlush: error: ', error);
    }
  }
  return Promise.resolve(false);
};

const doApply = () => {
  if (scope.$apply) {
    return new Promise((resolve,reject) => {
      try {
        scope.$apply(() => {
          //console.info('doApply: $apply. ', new Date());
          resolve(true);
        });
      } catch (error) {
        //console.error('doApply: error: ', error);
        resolve(error);
      }
    });
  }
  return Promise.resolve(false);
}
/* this is a total hack to make angular digests happen to avoid deadlocks */
const runDigest = (q) => {
  if (q) {
    const ret = promiseify(q);
    doFlush().then(doApply);
    return ret;
  }
  return doFlush().then(doApply);
  //return doFlush();
};

const initializeCache = async function() {
  var requisitionsUrl = requisitionsService.internal.requisitionsUrl;
  console.log('initializing cache: ' + requisitionsUrl);
  $httpBackend.expect('GET', requisitionsUrl).respond(requisitions);
  $httpBackend.expect('GET', requisitionsUrl + '/deployed/stats').respond(deployedStats);
  return runDigest(requisitionsService.getRequisitions());
};

beforeEach(angular.mock.module('onms-requisitions', function($provide) {
  console.debug = console.log;
  $provide.value('$log', console);
}));

beforeEach(angular.mock.inject(function($injector) {
  scope = $injector.get('$rootScope').$new();
  $httpBackend = $injector.get('$httpBackend');
  $httpBackend.whenGET('views/requisitions.html').respond(200, '');
  requisitionsService = $injector.get('RequisitionsService');
  $timeout = $injector.get('$timeout');
}));

afterEach(() => {
  runDigest();
  $httpBackend.verifyNoOutstandingExpectation();
  $httpBackend.verifyNoOutstandingRequest();
});

//setInterval(() => runDigest, 50);

// Testing cache related functions

test('Service: RequisitionsService: getCached*', function() {
  console.log('Running tests for cache');

  return initializeCache().then(() => {
    console.log('Cache updated!');
    var requisitionsData = requisitionsService.internal.getCachedRequisitionsData();
    var requisition = requisitionsService.internal.getCachedRequisition('test-network');
    var node = requisitionsService.internal.getCachedNode('test-network', '1001');
    expect(requisitionsData).toBeDefined();
    expect(requisition).not.toBeNull();
    expect(node).not.toBeNull();

    requisitionsService.clearCache();
    requisitionsData = requisitionsService.internal.getCachedRequisitionsData();
    requisition = requisitionsService.internal.getCachedRequisition('test-network');
    node = requisitionsService.internal.getCachedNode('test-network', '1001');
    expect(requisitionsData).toBeUndefined();
    expect(requisition).toBeNull();
    expect(node).toBeNull();

    return true;
  });
});

// Testing getRequisitions

const getRequisitionsHandlerFn = function(data, source) {
  expect(data).not.toBe(null);
  console.log('getRequisitionsHandlerFn(' + source + '): ' + data.requisitions.length + ' requisitions.');
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
  return data;
};

test('Service: RequisitionsService: getRequisitions', function() {
  console.log('Running tests for getRequisitions');

  var requisitionsUrl = requisitionsService.internal.requisitionsUrl;
  $httpBackend.expect('GET', requisitionsUrl).respond(requisitions);
  $httpBackend.expect('GET', requisitionsUrl + '/deployed/stats').respond(deployedStats);

  const ret = requisitionsService.getRequisitions().then((data) => {
    return getRequisitionsHandlerFn(data, 'uncached');
  }, errorHandlerFn).then(() => {
    expect(requisitionsService.internal.getCachedRequisitionsData()).not.toBe(null);
    console.log('uncached passed');
    return requisitionsService.getRequisitions().then((data) => {
      return getRequisitionsHandlerFn(data, 'cached');
    }, errorHandlerFn);
  }).then(() => {
    console.log('cached passed');
    const testNetwork = requisitionsService.getRequisition('test-network');
    return testNetwork;
  }).then((r) => {
    console.log('got test-network');
    expect(r).not.toBe(null);
    expect(r.foreignSource).toBe('test-network');
    const oneThousandOne = requisitionsService.getNode('test-network', '1001');
    return oneThousandOne;
  }).then((n) => {
    console.log('got test-network:1001')
    expect(n).not.toBe(null);
    return expect(n.foreignId).toBe('1001');
  }).then(() => {
    console.log('done');
    return true;
  });

  runDigest();
  console.log('called flush');

  return promiseify(ret);
});

// Testing getRequisitionNames

test('Service: RequisitionsService: getRequisitionNames', function() {
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

  return runDigest(requisitionsService.getRequisitionNames().then(function(names) {
    expect(names.length).toBe(3);
    expect(names[2]).toBe('test-monitoring'); // 3rd requsition
  }, errorHandlerFn));
});

// Testing getRequisition

test('Service: RequisitionsService: getRequisition', function() {
  console.log('Running tests for getRequisition from server');

  var req = requisitions['model-import'][0];
  var fs  = req['foreign-source'];
  var requisitionUrl = requisitionsService.internal.requisitionsUrl + '/' + fs;
  $httpBackend.expect('GET', requisitionUrl).respond(req);

  return runDigest(requisitionsService.getRequisition(fs).then(function(data) {
    expect(data).not.toBe(null);
    expect(data.nodes.length).toBe(3);
  }, errorHandlerFn));
});

// Testing updateDeployedStatsForRequisition

test('Service: RequisitionsService: updateDeployedStatsForRequisition', function() {
  console.log('Running tests for updateDeployedStatsForRequisition');

  return initializeCache().then(() => {
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

    return runDigest(requisitionsService.updateDeployedStatsForRequisition(req).then(function() {
      expect(req.nodesInDatabase).toBe(3);
      expect(req.nodesDefined).toBe(3);
    }, errorHandlerFn));
  });
});

// Testing updateDeployedStats

test('Service: RequisitionsService: updateDeployedStats', function() {
  console.log('Running tests for updateDeployedStats');

  return initializeCache().then(() => {
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
    return runDigest(requisitionsService.updateDeployedStats(requisitionsData).then(function() {
      var req = requisitionsData.getRequisition('test-network');
      expect(req.nodesInDatabase).toBe(3);
      expect(req.nodesDefined).toBe(3);
    }, errorHandlerFn));
  });
});

// Testing synchronizeRequisition

test('Service: RequisitionsService: synchronizeRequisition', function() {
  console.log('Running tests for synchronizeRequisition');

  var foreignSource = 'test-requisition';
  var importUrl = requisitionsService.internal.requisitionsUrl + '/' + foreignSource + '/import?rescanExisting=false';
  $httpBackend.expect('PUT', importUrl).respond({});

  return runDigest(requisitionsService.synchronizeRequisition(foreignSource, 'false').then(function() {}, errorHandlerFn));
});

// Testing synchronizeRequisition (unknown requisition)

test('Service: RequisitionsService: synchronizeRequisition::unknownRequisition', function() {
  console.log('Running tests for synchronizeRequisition (unknown requisition)');

  const foreignSource = 'blah-blah';
  return initializeCache().then(() => {
    return runDigest(requisitionsService.synchronizeRequisition(foreignSource));
  }).then(errorHandlerFn, function(msg) {
    return expect(msg).toBe('The foreignSource ' + foreignSource + ' does not exist.');
  });
});

// Testing addRequisition

test('Service: RequisitionsService: addRequisition', function() {
  console.log('Running tests for addRequisition');

  var foreignSource = 'test-requisition';
  var emptyReq = { 'foreign-source': foreignSource, node: [] };
  var requisition = new Requisition(emptyReq, false);
  var addUrl = requisitionsService.internal.requisitionsUrl;
  $httpBackend.expect('POST', addUrl, emptyReq).respond(requisition);

  return runDigest(requisitionsService.addRequisition(foreignSource).then(function(requisition) {
    return expect(requisition.foreignSource).toBe(foreignSource);
  }, errorHandlerFn));
});

// Testing addRequisition (existing requisition)

test('Service: RequisitionsService: addRequisition::existingRequisition', function() {
  console.log('Running tests for addRequisition (existing requisition)');

  return initializeCache().then(() => {
    var foreignSource = 'test-network';
    return runDigest(requisitionsService.addRequisition(foreignSource)).then(errorHandlerFn, function(msg) {
      return expect(msg).toBe('Invalid foreignSource ' + foreignSource + ', it already exist.');
    });
  });
});

// Testing deleteRequisition (deployed requisition with nodes in database)

test('Service: RequisitionsService: deleteRequisition::deployed', function() {
  console.log('Running tests for deleteRequisition (deployed requisition with nodes in database)');

  return initializeCache().then(() => {
    var foreignSource = 'test-network';
    return runDigest(requisitionsService.deleteRequisition(foreignSource)).then(errorHandlerFn, function(msg) {
      return expect(msg).toBe('The foreignSource ' + foreignSource + ' contains 2 nodes on the database, it cannot be deleted.');
    });
  });
});

// Testing deleteRequisition (unknown requisition)

test('Service: RequisitionsService: deleteRequisition::unknown', function() {
  console.log('Running tests for deleteRequisition (unknown requisition)');

  return initializeCache().then(() => {
    var foreignSource = 'blah-blah';
    return runDigest(requisitionsService.deleteRequisition(foreignSource)).then(errorHandlerFn, function(msg) {
      return expect(msg).toBe('The foreignSource ' + foreignSource + ' does not exist.');
    });
  });
});

// Testing deleteRequisition (pending)

test('Service: RequisitionsService: deleteRequisition::pending', function() {
  console.log('Running tests for deleteRequisition (pending)');

  return initializeCache().then(() => {
    var foreignSource = 'test-empty';
    var r = requisitionsService.internal.getCachedRequisition(foreignSource);
    expect(r).not.toBe(null);

    $httpBackend.expect('DELETE', requisitionsService.internal.requisitionsUrl + '/' + foreignSource).respond({});
    $httpBackend.expect('DELETE', requisitionsService.internal.requisitionsUrl + '/deployed/' + foreignSource).respond({});
    $httpBackend.expect('DELETE', requisitionsService.internal.foreignSourcesUrl + '/' + foreignSource).respond({});
    $httpBackend.expect('DELETE', requisitionsService.internal.foreignSourcesUrl + '/deployed/' + foreignSource).respond({});

    return runDigest(requisitionsService.deleteRequisition(foreignSource)).then(function() {
      r = requisitionsService.internal.getCachedRequisition(foreignSource);
      return expect(r).toBe(null);
    }, errorHandlerFn);
  });
});

// Testing deleteRequisition (deployed)

test('Service: RequisitionsService: deleteRequisition::deployed-empty', function() {
  console.log('Running tests for deleteRequisition (deployed after empty it)');

  return initializeCache().then(() => {
    var foreignSource = 'test-network';
    var r = requisitionsService.internal.getCachedRequisition(foreignSource);
    expect(r).not.toBe(null);
    r.nodes = [];
    r.nodesInDatabase = 0;

    $httpBackend.expect('DELETE', requisitionsService.internal.requisitionsUrl + '/' + foreignSource).respond({});
    $httpBackend.expect('DELETE', requisitionsService.internal.requisitionsUrl + '/deployed/' + foreignSource).respond({});
    $httpBackend.expect('DELETE', requisitionsService.internal.foreignSourcesUrl + '/' + foreignSource).respond({});
    $httpBackend.expect('DELETE', requisitionsService.internal.foreignSourcesUrl + '/deployed/' + foreignSource).respond({});

    return runDigest(requisitionsService.deleteRequisition(foreignSource)).then(() => {
      r = requisitionsService.internal.getCachedRequisition(foreignSource);
      return expect(r).toBe(null);
    }, errorHandlerFn);
  });
});

// Testing removeAllNodesFromRequisition

test('Service: RequisitionsService: removeAllNodesFromRequisition', function() {
  console.log('Running tests for removeAllNodesFromRequisition');

  return initializeCache().then(() => {
    var requisition = {'foreign-source': 'test-network', node: []};
    var url = requisitionsService.internal.requisitionsUrl;
    $httpBackend.expect('POST', url, requisition).respond({});
    $httpBackend.expect('PUT', url + '/test-network/import?rescanExisting=false').respond({});

    return runDigest(requisitionsService.removeAllNodesFromRequisition('test-network')).then(() => {
      var r = requisitionsService.internal.getCachedRequisition('test-network');
      expect(r.nodes.length).toBe(0);
      expect(r.nodesDefined).toBe(0);
      expect(r.isModified()).toBe(true);
      return true;
    }, errorHandlerFn);
  });
});

// Testing getNode from server

test('Service: RequisitionsService: getNode::fromServer', function() {
  console.log('Running tests for getNode (from server)');

  var req  = requisitions['model-import'][0];
  var node = req['node'][0];
  var fs   = req['foreign-source'];
  var fid  = node['foreign-id'];
  var nodeUrl = requisitionsService.internal.requisitionsUrl + '/' + fs + '/nodes/' + fid;
  $httpBackend.expect('GET', nodeUrl).respond(node);

  return runDigest(requisitionsService.getNode(fs, fid)).then(function(node) {
    expect(node).not.toBe(null);
    expect(node.nodeLabel).toBe('testing-server');
    return true;
  }, errorHandlerFn);
});

// Testing getNode from cache

test('Service: RequisitionsService: getNode::fromCache', function() {
  console.log('Running tests for getNode (from cache)');

  return initializeCache().then(() => {
    return runDigest(requisitionsService.getNode('test-network', '1001')).then(function(node) {
      expect(node).not.toBe(null);
      expect(node.nodeLabel).toBe('testing-server');
      return true;
    }, errorHandlerFn);
  });
});

// Testing saveNode

test('Service: RequisitionsService: saveNode', function() {
  console.log('Running tests for saveNode');

  return initializeCache().then(() => {
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

    return runDigest(requisitionsService.saveNode(node)).then(() => {
      expect(requisition.isModified()).toBe(true);
      expect(requisition.nodes.length).toBe(nodeCount + 1);
      expect(requisition.nodesDefined).toBe(pendingCount + 1);
      return true;
    }, errorHandlerFn);
  });
});

// Testing deleteNode

test('Service: RequisitionsService: deleteNode', function() {
  console.log('Running tests for deleteNode');

  return initializeCache().then(() => {
    var node = requisitionsService.internal.getCachedNode('test-network', '1001');
    expect(node).not.toBe(null);
    var deleteUrl = requisitionsService.internal.requisitionsUrl + '/' + node.foreignSource + '/nodes/' + node.foreignId;
    $httpBackend.expect('DELETE', deleteUrl).respond({});

    var requisition = requisitionsService.internal.getCachedRequisition('test-network');
    var nodeCount = requisition.nodes.length;
    var pendingCount = requisition.nodesDefined;

    return runDigest(requisitionsService.deleteNode(node)).then(() => {
      expect(requisition.isModified()).toBe(true);
      expect(requisition.nodes.length).toBe(nodeCount - 1);
      expect(requisition.nodesDefined).toBe(pendingCount - 1);
      return true;
    }, errorHandlerFn);
  });
});

// Testing getAvailableServices

test('Service: RequisitionsService: getAvailableServices', function() {
  console.log('Running tests for getAvailableServices');

  return initializeCache().then(() => {
    var services = { element: ['ICMP','SNMP'] };
    var url = requisitionsService.internal.foreignSourcesConfigUrl + '/services/test-server';
    $httpBackend.expect('GET', url).respond(services);

    return runDigest(requisitionsService.getAvailableServices('test-server')).then(function(data) {
      expect(data).not.toBe(null);
      expect(data.length).toBe(2);
      expect(data[1]).toBe('SNMP');
      return true;
    }, errorHandlerFn);
  });
});

// Testing getAvailableAssets

test('Service: RequisitionsService: getAvailableAssets', function() {
  console.log('Running tests for getAvailableAssets');

  return initializeCache().then(() => {
    var assets = { element: ['address1','city','state','zip'] };
    var url = requisitionsService.internal.foreignSourcesConfigUrl + '/assets';
    $httpBackend.expect('GET', url).respond(assets);

    return runDigest(requisitionsService.getAvailableAssets()).then(function(data) {
      expect(data).not.toBe(null);
      expect(data.length).toBe(4);
      expect(data[1]).toBe('city');
      return true;
    }, errorHandlerFn);
  });
});

// Testing getAvailableCategories

test('Service: RequisitionsService: getAvailableCategories', function() {
  console.log('Running tests for getAvailableCategories');

  return initializeCache().then(() => {
    var categories = { element: ['Production','Development','Testing'] };
    var url = requisitionsService.internal.foreignSourcesConfigUrl + '/categories';
    $httpBackend.expect('GET', url).respond(categories);

    return runDigest(requisitionsService.getAvailableCategories()).then(function(data) {
      expect(data).not.toBe(null);
      expect(data.length).toBe(3);
      expect(data[1]).toBe('Development');
      return true;
    }, errorHandlerFn);
  });
});

// Testing getAvailablePolicies

test('Service: RequisitionsService: getAvailablePolicies', function() {
  console.log('Running tests for getAvailablePolicies');

  return initializeCache().then(() => {
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

    return runDigest(requisitionsService.getAvailablePolicies()).then(function(data) {
      expect(data).not.toBe(null);
      expect(data.length).toBe(1);
      expect(data[0].name).toBe('Match IP Interface');
      expect(data[0].parameters.length).toBe(4);
      return true;
    }, errorHandlerFn);
  });
});

// Testing getAvailableDetectors

test('Service: RequisitionsService: getAvailableDetectors', function() {
  console.log('Running tests for getAvailableDetectors');

  return initializeCache().then(() => {
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

    return runDigest(requisitionsService.getAvailableDetectors()).then(function(data) {
      expect(data).not.toBe(null);
      expect(data.length).toBe(1);
      expect(data[0].name).toBe('ICMP');
      expect(data[0].parameters.length).toBe(5);
      return true;
    }, errorHandlerFn);
  });
});

// Testing isForeignIdOnRequisition

test('Service: RequisitionsService: isForeignIdOnRequisition', function() {
  console.log('Running tests for isForeignIdOnRequisition');

  return initializeCache().then(() => {
    return runDigest(requisitionsService.isForeignIdOnRequisition('test-network', '1001')).then(function(exist) {
      expect(exist).toBe(true);
      return true;
    }, errorHandlerFn);
  }).then(() => {
    return runDigest(requisitionsService.isForeignIdOnRequisition('test-network', '1004')).then(function(exist) {
      expect(exist).toBe(false);
      return true;
    }, errorHandlerFn);
  });
});

// Testing isIpAddressOnNode

test('Service: RequisitionsService: isIpAddressOnNode', function() {
  console.log('Running tests for isIpAddressOnNode');

  return initializeCache().then(() => {
    return runDigest(requisitionsService.isIpAddressOnNode('test-network', '1001', '10.0.0.1')).then(function(exist) {
      expect(exist).toBe(true);
      return true;
    }, errorHandlerFn);
  }).then(() => {
    return runDigest(requisitionsService.isIpAddressOnNode('test-network', '1001', '10.0.0.2')).then(function(exist) {
      expect(exist).toBe(false);
      return true;
    }, errorHandlerFn);
  });
});

// Testing isCategoryOnNode

test('Service: RequisitionsService: isCategoryOnNode', function() {
  console.log('Running tests for isCategoryOnNode');

  return initializeCache().then(() => {
    return runDigest(requisitionsService.isCategoryOnNode('test-network', '1001', 'Servers')).then(function(exist) {
      expect(exist).toBe(true);
      return true;
    }, errorHandlerFn);
  }).then(() => {
    return runDigest(requisitionsService.isCategoryOnNode('test-network', '1001', 'Router')).then(function(exist) {
      expect(exist).toBe(false);
      return true;
    }, errorHandlerFn);
  });
});

// Testing isServiceOnNode

test('Service: RequisitionsService: isServiceOnNode', function() {
  console.log('Running tests for isServiceOnNode');

  return initializeCache().then(() => {
    return runDigest(requisitionsService.isServiceOnNode('test-network', '1001', '10.0.0.1', 'ICMP')).then(function(exist) {
      expect(exist).toBe(true);
      return true;
    }, errorHandlerFn);
  }).then(() => {
    return runDigest(requisitionsService.isServiceOnNode('test-network', '1001', '10.0.0.2', 'HTTP')).then(function(exist) {
      expect(exist).toBe(false);
      return true;
    }, errorHandlerFn);
  });
});

// Test getForeignSourceDefinition

test('Service: RequisitionsService: getForeignSourceDefinition', function() {
  console.log('Running tests for getForeignSourceDefinition');

  var url = requisitionsService.internal.foreignSourcesUrl + '/default';
  $httpBackend.expect('GET', url).respond(foreignSourceDef);

  return runDigest(requisitionsService.getForeignSourceDefinition('default')).then(function(data) {
    expect(data).not.toBe(null);
    expect(data.detectors.length).toBe(3);
    expect(data.detectors[0].name).toBe('ICMP');
    expect(data.policies.length).toBe(1);
    expect(data.policies[0].name).toBe('No IPs');
    return true;
  }, errorHandlerFn);
});

// Testing saveForeignSourceDefinition

test('Service: RequisitionsService: saveForeignSourceDefinition', function() {
  console.log('Running tests for saveForeignSourceDefinition');

  var url = requisitionsService.internal.foreignSourcesUrl;
  $httpBackend.expect('POST', url).respond();

  return runDigest(requisitionsService.saveForeignSourceDefinition(foreignSourceDef)).then(() => {}, errorHandlerFn);
});

// Test cloneForeignSourceDefinition

test('Service: RequisitionsService: cloneForeignSourceDefinition::unknownSource', function() {
  console.log('Running tests for cloneForeignSourceDefinition for an unknown source');

  var requisitionNames = {
    'count': 3,
    'totalCount': 3, 
    'foreign-source': [ 'Routers', 'Servers', 'Storage' ]
  };

  $httpBackend.expect('GET', requisitionsService.internal.requisitionNamesUrl).respond(requisitionNames);

  return runDigest(requisitionsService.cloneForeignSourceDefinition('this_does_not_exist', 'Routers')).then(errorHandlerFn, function(msg) {
    return expect(msg).toBe('The source requisition this_does_not_exist does not exist.');
  });
});

test('Service: RequisitionsService: cloneForeignSourceDefinition::unknownDestination', function() {
  console.log('Running tests for cloneForeignSourceDefinition for an unknown destination');

  var requisitionNames = {
    'count': 3,
    'totalCount': 3, 
    'foreign-source': [ 'Routers', 'Servers', 'Storage' ]
  };

  $httpBackend.expect('GET', requisitionsService.internal.requisitionNamesUrl).respond(requisitionNames);

  return runDigest(requisitionsService.cloneForeignSourceDefinition('Routers', 'this_does_not_exist')).then(errorHandlerFn, function(msg) {
    return expect(msg).toBe('The target requisition this_does_not_exist does not exist.');
  });
});

test('Service: RequisitionsService: cloneForeignSourceDefinition', function() {
  console.log('Running tests for cloneForeignSourceDefinition');

  var requisitionNames = {
    'count': 3,
    'totalCount': 3, 
    'foreign-source': [ 'Routers', 'Servers', 'Storage' ]
  };

  $httpBackend.expect('GET', requisitionsService.internal.requisitionNamesUrl).respond(requisitionNames);
  $httpBackend.expect('GET', requisitionsService.internal.foreignSourcesUrl + '/Routers').respond(foreignSourceDef);
  $httpBackend.expect('POST', requisitionsService.internal.foreignSourcesUrl).respond();

  return runDigest(requisitionsService.cloneForeignSourceDefinition('Routers', 'Servers')).then(function(fs) {
      return expect(fs.name).toBe('Servers');
  }, errorHandlerFn);
});

// Testing deleteForeignSourceDefinition

test('Service: RequisitionsService: deleteForeignSourceDefinition', function() {
  console.log('Running tests for deleteForeignSourceDefinition');

  var urlP = requisitionsService.internal.foreignSourcesUrl + '/test-requisition';
  $httpBackend.expect('DELETE', urlP).respond();
  var urlD = requisitionsService.internal.foreignSourcesUrl + '/deployed/test-requisition';
  $httpBackend.expect('DELETE', urlD).respond();

  return runDigest(requisitionsService.deleteForeignSourceDefinition('test-requisition')).then(() => {}, errorHandlerFn);
});

// Testing updateSnmpCommunity

test('Service: RequisitionsService: updateSnmpCommunity', function() {
  console.log('Running tests for updateSnmpCommunity');

  var url = requisitionsService.internal.snmpConfigUrl + '/192.168.1.1';
  $httpBackend.expect('PUT', url, {'readCommunity' : 'my_community', 'version' : 'v2c'}).respond();

  return runDigest(requisitionsService.updateSnmpCommunity('192.168.1.1', 'my_community', 'v2c')).then(function(ip) {
    return expect(ip).toBe('192.168.1.1');
  }, errorHandlerFn);
});

// Testing quickAddNode without SNMP

test('Service: RequisitionsService: quickAddNode::noSnmp', function() {
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

  return runDigest(requisitionsService.quickAddNode(quickNode)).then(function(n) {
    return expect(n.nodeLabel).toBe('new-node.local');
  }, errorHandlerFn);
});

test('Service: RequisitionsService: quickAddNode', function() {
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

  return runDigest(requisitionsService.quickAddNode(quickNode)).then(function(n) {
    return expect(n.nodeLabel).toBe('new-node.local');
  }, errorHandlerFn);
});
