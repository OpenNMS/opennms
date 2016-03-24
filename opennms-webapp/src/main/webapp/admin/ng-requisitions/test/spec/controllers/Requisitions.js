/*global RequisitionsData:true */

/**
* @author Alejandro Galue <agalue@opennms.org>
* @copyright 2014 The OpenNMS Group, Inc.
*/

'use strict';

describe('Controller: RequisitionsController', function () {

  // Initialize testing environment

  var controllerFactory, scope, $q, mockGrowl = {}, mockRequisitionsService = {}, requisitionsData = new RequisitionsData();

  function createController() {
    return controllerFactory('RequisitionsController', {
      $scope: scope,
      RequisitionsService: mockRequisitionsService,
      growl: mockGrowl
    });
  }

  beforeEach(module('onms-requisitions', function($provide) {
    $provide.value('$log', console);
  }));

  beforeEach(inject(function($rootScope, $controller, _$q_) {
    scope = $rootScope.$new();
    controllerFactory = $controller;
    $q = _$q_;
  }));

  beforeEach(function() {
    mockRequisitionsService.getTiming = jasmine.createSpy('getTiming');
    mockRequisitionsService.getRequisitions = jasmine.createSpy('getRequisitions');
    var requisitionsDefer = $q.defer();
    requisitionsDefer.resolve(requisitionsData);
    mockRequisitionsService.getRequisitions.and.returnValue(requisitionsDefer.promise);
    mockRequisitionsService.getTiming.and.returnValue({ isRunning: false });

    mockGrowl = {
      warning: function(msg) { console.warn(msg); },
      error: function(msg) { console.error(msg); },
      info: function(msg) { console.info(msg); },
      success: function(msg) { console.info(msg); }
    };
  });

  it('test controller', function() {
    createController();
    scope.$digest();
    expect(mockRequisitionsService.getRequisitions).toHaveBeenCalled();
    expect(scope.requisitionsData.requisitions.length).toBe(0);
  });

});
