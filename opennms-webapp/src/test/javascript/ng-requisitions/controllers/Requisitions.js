/*global RequisitionsData:true */

/**
* @author Alejandro Galue <agalue@opennms.org>
* @copyright 2014 The OpenNMS Group, Inc.
*/

'use strict';

describe('Controller: RequisitionsController', function () {

  // Initialize testing environment

  var controllerFactory, scope, $q, dateFormatterService, mockGrowl = {}, mockRequisitionsService = {}, requisitionsData = new RequisitionsData();

  function createController() {
    return controllerFactory('RequisitionsController', {
      $scope: scope,
      DateFormatterService: dateFormatterService,
      RequisitionsService: mockRequisitionsService,
      growl: mockGrowl
    });
  }

  beforeEach(function() {
    window._onmsDateTimeFormat = "yyyy-MM-dd'T'HH:mm:ssxxx";
    window._onmsZoneId = 'America/New_York';
    window._onmsFormatter = new OnmsDateFormatter();
  });

  beforeEach(module('onms-requisitions', function($provide) {
    $provide.value('$log', console);
  }));

  beforeEach(inject(function($rootScope, $controller, $interval, _$q_, DateFormatterService) {
    scope = $rootScope.$new();
    controllerFactory = $controller;
    $q = _$q_;

    dateFormatterService = DateFormatterService;
    $interval.flush(200);
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
