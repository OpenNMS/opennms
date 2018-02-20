/*global Requisition:true */

/**
* @author Alejandro Galue <agalue@opennms.org>
* @copyright 2014 The OpenNMS Group, Inc.
*/

'use strict';

const angular = require('angular-js');
require('angular-mocks');
require('../../../../../src/main/assets/js/apps/onms-requisitions/requisitions');

const Requisition = require('../../../../../src/main/assets/js/apps/onms-requisitions/lib/scripts/model/Requisition');

// Initialize testing environment

var controllerFactory, scope, $q, mockGrowl = {}, mockRequisitionsService = {}, foreignSource = 'test-requisition', requisition = new Requisition(foreignSource);

function createController() {
  return controllerFactory('RequisitionController', {
    $scope: scope,
    $routeParams: { 'foreignSource': foreignSource },
    RequisitionsService: mockRequisitionsService,
    growl: mockGrowl
  });
}

beforeEach(angular.mock.module('onms-requisitions', function($provide) {
  console.debug = console.log;
  $provide.value('$log', console);
}));

beforeEach(angular.mock.inject(function($rootScope, $controller, _$q_) {
  scope = $rootScope.$new();
  controllerFactory = $controller;
  $q = _$q_;
}));

beforeEach(function() {
  mockRequisitionsService.getTiming = jasmine.createSpy('getTiming');
  mockRequisitionsService.getRequisition = jasmine.createSpy('getRequisition');
  var requisitionDefer = $q.defer();
  requisitionDefer.resolve(requisition);
  mockRequisitionsService.getRequisition.and.returnValue(requisitionDefer.promise);
  mockRequisitionsService.getTiming.and.returnValue({ isRunning: false });

  mockGrowl = {
    warning: function(msg) { console.warn(msg); },
    error: function(msg) { console.error(msg); },
    info: function(msg) { console.info(msg); },
    success: function(msg) { console.info(msg); }
  };
});

test('Controller: RequisitionController: test controller', function() {
  createController();
  scope.$digest();
  expect(mockRequisitionsService.getRequisition).toHaveBeenCalledWith(foreignSource);
  expect(scope.foreignSource).toBe(foreignSource);
});
