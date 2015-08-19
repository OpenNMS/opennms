/**
* @author Alejandro Galue <agalue@opennms.org>
* @copyright 2014 The OpenNMS Group, Inc.
*/

'use strict';

describe('Controller: ForeignSourceController', function () {

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

  beforeEach(module('onms-requisitions', function($provide) {
    $provide.value('$log', console);
  }));

  beforeEach(inject(function($rootScope, $controller, _$q_) {
    scope = $rootScope.$new();
    $q = _$q_;
    controllerFactory = $controller;
  }));

  beforeEach(function() {
    mockModal = {};

    mockRequisitionsService.getForeignSourceDefinition = jasmine.createSpy('getForeignSourceDefinition');
    var requisitionDefer = $q.defer();
    requisitionDefer.resolve({ detectors: [], policies: [] });
    mockRequisitionsService.getForeignSourceDefinition.andReturn(requisitionDefer.promise);

    mockGrowl = {
      warn: function(msg) { console.warn(msg); },
      error: function(msg) { console.error(msg); },
      info: function(msg) { console.info(msg); },
      success: function(msg) { console.info(msg); }
    };
  });

  it('test controller', function() {
    createController();
    scope.$digest();
    expect(mockRequisitionsService.getForeignSourceDefinition).toHaveBeenCalledWith(foreignSource);
    expect(scope.foreignSource).toBe(foreignSource);
  });

});
