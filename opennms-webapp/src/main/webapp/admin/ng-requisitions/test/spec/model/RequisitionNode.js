/**
* @author Alejandro Galue <agalue@opennms.org>
* @copyright 2014 The OpenNMS Group, Inc.
*/

'use strict';

describe('Model: RequisitionsNode', function () {

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
      },{
        'service-name': 'SNMP'
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

  it('verify object translation', function () {
    var reqNode = new RequisitionNode('test-requisition', onmsNode, false);
    expect(reqNode).not.toBe(null);
    expect(reqNode.categories.length).toBe(1);
    expect(reqNode.categories[0].name).toBe('Servers');
    expect(reqNode.interfaces.length).toBe(1);
    expect(reqNode.interfaces[0].ipAddress).toBe('10.0.0.1');
    expect(reqNode.interfaces[0].services.length).toBe(2);
    expect(reqNode.interfaces[0].services[0].name).toBe('ICMP');
    expect(reqNode.assets[1].value).toBe('Pittsboro');
    var genNode = reqNode.getOnmsRequisitionNode();
    expect(genNode).not.toBe(null);
    expect(genNode).toEqual(onmsNode);
    expect(angular.equals(genNode, onmsNode)).toBe(true);
  });

});
