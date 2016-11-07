/*global Requisition:true */

/**
* @author Alejandro Galue <agalue@opennms.org>
* @copyright 2014 The OpenNMS Group, Inc.
*/

'use strict';

describe('Model: Requisitions', function () {

  var onmsNode = {
    'foreign-id': '1001',
    'node-label': 'testing-node',
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
  };

  var onmsRequisition = {
    'foreign-source' : 'test-requisition',
    'node': [ onmsNode ]
  };

  it('verify object generation', function () {
    var req = new Requisition(onmsRequisition, false);
    expect(req).not.toBe(null);
    expect(req.nodesDefined).toBe(1);
    expect(req.nodesInDatabase).toBe(0);
    expect(req.nodes.length).toBe(1);
    expect(req.nodes[0].nodeLabel).toBe('testing-node');
  });

});
