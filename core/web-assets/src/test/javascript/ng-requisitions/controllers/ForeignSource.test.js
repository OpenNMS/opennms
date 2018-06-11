/**
* @author Alejandro Galue <agalue@opennms.org>
* @copyright 2014 The OpenNMS Group, Inc.
*/

'use strict';

const angular = require('angular-js');
require('angular-mocks');
require('../../../../../src/main/assets/js/apps/onms-requisitions/requisitions');

var controllerFactory, scope, $q, mockModal = {}, mockGrowl = {}, mockRequisitionsService = {}, foreignSource = 'test-requisition';

function createController() {
  return controllerFactory('ForeignSourceController', {
    $scope: scope,
    $routeParams: { 'foreignSource': foreignSource },
    $modal: mockModal,
    RequisitionsService: mockRequisitionsService,
    growl: mockGrowl
  });
}

beforeEach(angular.mock.module('onms-requisitions', function($provide) {
  $provide.value('$log', console);
}));

beforeEach(angular.mock.inject(function($rootScope, $controller, _$q_) {
  scope = $rootScope.$new();
  $q = _$q_;
  controllerFactory = $controller;
}));

beforeEach(function() {
  mockModal = {};

  mockRequisitionsService.getForeignSourceDefinition = jasmine.createSpy('getForeignSourceDefinition');
  mockRequisitionsService.getTiming = jasmine.createSpy('getTiming');
  var requisitionDefer = $q.defer();
  requisitionDefer.resolve({ detectors: [{'name':'ICMP'},{'name':'SNMP'}], policies: [{'name':'Foo'},{'name':'Bar'}] });
  mockRequisitionsService.getForeignSourceDefinition.and.returnValue(requisitionDefer.promise);
  mockRequisitionsService.getTiming.and.returnValue({ isRunning: false });

  mockGrowl = {
    warning: function(msg) { console.warn(msg); },
    error: function(msg) { console.error(msg); },
    info: function(msg) { console.info(msg); },
    success: function(msg) { console.info(msg); }
  };
});

test('Controller: ForeignSourceController: test controller', function() {
  createController();
  scope.$digest();
  expect(mockRequisitionsService.getForeignSourceDefinition).toHaveBeenCalledWith(foreignSource);
  expect(scope.foreignSource).toBe(foreignSource);
  expect(scope.indexOfDetector({name:'ICMP'})).toBe(0);
  expect(scope.indexOfPolicy({name:'Foo'})).toBe(0);
  expect(scope.indexOfDetector({name:'HTTP'})).toBe(-1);
  expect(scope.indexOfPolicy({name:'Test'})).toBe(-1);
});
