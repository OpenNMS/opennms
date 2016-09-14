/**
* @author Alejandro Galue <agalue@opennms.org>
* @copyright 2016 The OpenNMS Group, Inc.
*/

'use strict';

describe('Controller: AddToKscCtrl', function () {

  var createController, createModalController, scope, httpBackend;

  beforeEach(module('onms-ksc', function($provide) {
    $provide.value('$log', console);
  }));

  beforeEach(inject(function($rootScope, $httpBackend, $controller) {
    scope = $rootScope.$new();
    httpBackend = $httpBackend;
    createController = function() {
      return $controller('AddToKscCtrl', {
        '$scope': scope
      });
    };
    createModalController = function() {
      return $controller('AddToKscModalInstanceCtrl', {
        '$scope': scope,
        '$uibModalInstance': {}, // Not required for the tests
        'resourceLabel': 'Node Resource',
        'graphTitle': 'My Metric'
      });
    };
 
  }));

  afterEach(function() {
    httpBackend.verifyNoOutstandingExpectation();
    httpBackend.verifyNoOutstandingRequest();
  });

  it('Add new graph to a KSC report', function() {
    console.log('Testing AddToKscCtrl: Add new graph to a KSC report');
    createController();

    httpBackend.expect('GET', 'rest/ksc/1').respond({ id: 1, label: 'Test Report', kscGraph: [] });
    httpBackend.expect('PUT', 'rest/ksc/1?reportName=myMetric&resourceId=node%5B1%5D.nodeSnmp&timespan=1_day&title=My+Title').respond({});

    scope.updateReport({ id: 1, label: 'Test Report' }, 'node[1].nodeSnmp', 'Node Resource', 'myMetric', 'My Title', '1_day');

    httpBackend.flush();
  });

  it('Add an existing graph to a KSC report', function() {
    console.log('Testing AddToKscCtrl: Add an existing graph to a KSC report');
    createController();

    httpBackend.expect('GET', 'rest/ksc/1').respond({ id: 1, label: 'Test Report', kscGraph: [{ resourceId: 'node[1].nodeSnmp', graphtype: 'myMetric'}] });

    scope.updateReport({ id: 1, label: 'Test Report' }, 'node[1].nodeSnmp', 'Node Resource', 'myMetric', 'My Title', '1_day');

    httpBackend.flush();
  });

   it('Verify modal controller', function() {
    console.log('Testing AddToKscModalInstanceCtrl');
    createModalController();

    httpBackend.expect('GET', 'rest/ksc').respond({ kscReport: [{ id: 1, label: 'Test Report'}] });
    httpBackend.flush();

    expect(scope.resourceLabel).toEqual('Node Resource');
    expect(scope.graphTitle).toEqual('My Metric');
    expect(scope.kscReports.length).toEqual(1);
    expect(scope.kscReports[0].label).toEqual('Test Report');
    expect(scope.timespan).toEqual('1_day');
    expect(scope.timespans.length).toEqual(25);
  });

});
