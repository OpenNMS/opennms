xdescribe('Shared Node Controllers Module:', function() {

  beforeEach(module('opennms.controllers.shared.nodes'));
  beforeEach(function() {
     module('opennms.controllers.shared.nodes');
  });

  // Stub out the services.
  var NodeService;
  var AlarmService;
  var PagedResourceFactory;
  var ModelFactory;

  beforeEach(inject(function(_NodeService_, _AlarmService_) {
    NodeService = _NodeService_;
    AlarmService = _AlarmService_;

    // For the Node Service we know we use these.
    NodeService.get = getEmptyThenStub;
    NodeService.list = getEmptyThenStub;
    NodeService.getIpInterfaces = getEmptyThenStub;
    NodeService.getIpInterfaceServices = getEmptyThenStub;

    // For the Alarm Service we know we use these.
    AlarmService.getByNode = getEmptyThenStub;
  }));

  describe('NodesCtrl', function() {
    var scope;
    var $controller;
    var rootScope;

    var nodesController;
    //var NodeService;

    beforeEach(inject(function(_$rootScope_, _$controller_, _$log_, _NodeService_, _PagedResourceFactory_, _ModelFactory_) {
      rootScope = _$rootScope_;
      scope = rootScope.$new();
      $controller = _$controller_;
      NodeService = _NodeService_;
      PagedResourceFactory = _PagedResourceFactory_;
      ModelFactory = _ModelFactory_;

      // Stub these out.
      //NodeService.list = function() {}
      //spyOn(NodeService, 'list').andReturn(getThenObject({}));

      nodesController = $controller('NodesCtrl', {
        $rootScope: rootScope,
        $scope: scope,
        $log: _$log_,
        NodeService: NodeService
      });

    }));

    describe('init', function() {
      it('should fetch nodes with the default limits', function() {
        expect(scope.limit).toBe(10);
        expect(scope.offset).toBe(0);
      });

      it('should fetch nodes', function() {
        spyOn(scope, 'processNodes');
        spyOn(NodeService, 'list').andReturn(getThenObject({_id: 1}));


        scope.init();
        expect(NodeService.list).toHaveBeenCalled();
      });
    });

    describe('getNodeLink', function() {
      it('should return a hash-path with node id', function() {
        var node = { id: 3};
        expect(scope.getNodeLink(node)).toBe('#/node/3');
      });
    });
  });

  describe('NodeDetailCtrl', function() {
    var scope;
    var $controller;
    var rootScope;

    var nodeDetailController;

    beforeEach(inject(function(_$rootScope_, _$controller_, _$log_) {
      rootScope = _$rootScope_;
      scope = rootScope.$new();
      $controller = _$controller_;

      // Stub these out.
      scope.isTest = true;
      scope.fakeStateParams = { nodeId: 1 };  // Add fake state params.

      nodeDetailController = $controller('NodeDetailCtrl', {
        $rootScope: rootScope,
        $scope: scope,
        $stateParams: scope.fakeStateParams,
        $log: _$log_,
        NodeService: NodeService,
        AlarmService: AlarmService
      });
    }));

    describe('the node fetching process', function() {
      describe('fetchNode', function() {
        it('should use the NodeService to get the nodeId', function() {
          spyOn(scope, 'processNode');
          spyOn(NodeService, 'get').andReturn(getThenObject({_id: 1}));

          scope.fetchNode(1);
          expect(NodeService.get).toHaveBeenCalledWith(1);
        });

        it('should call processNode to handle the results', function() {
          spyOn(scope, 'processNode');
          spyOn(NodeService, 'get').andReturn(getThenObject({_id: 1}));

          scope.fetchNode(1);
          expect(scope.processNode).toHaveBeenCalled();
        });
      });

      describe('processNode', function() {
        it('should save the processed node to the scope', function() {
          spyOn(scope, 'processInterfaces');
          var fakeNode = { _id: 600, _label: 'fakeNode' };

          scope.processNode(fakeNode);
          expect(scope.node._id).toBe(fakeNode._id);
          expect(scope.node._label).toBe(fakeNode._label);
        });

        it('should retrieve interfaces and have processInterface handle them', function() {
          var fakeNode = { _id: 600, _label: 'fakeNode' };

          spyOn(NodeService, 'getIpInterfaces').andReturn(getThenObject({}));
          spyOn(scope, 'processInterfaces');

          scope.processNode(fakeNode);
          expect(NodeService.getIpInterfaces).toHaveBeenCalled();
          expect(scope.processInterfaces).toHaveBeenCalled();
        });

        it('should retrieve alarms for the current node', function() {
          var fakeNode = { _id: 600, _label: 'fakeNode' };
          var fakeAlarm = { bogus: 'data' };
          spyOn(AlarmService, 'getByNode').andReturn(getThenObject(fakeAlarm));
          spyOn(scope, 'processInterfaces');

          scope.processNode(fakeNode);

          expect(AlarmService.getByNode).toHaveBeenCalledWith(fakeNode._id);
        });

        it('should save the alarms to the current node', function() {
          var fakeNode = { _id: 600, _label: 'fakeNode' };
          var fakeAlarm = { bogus: 'data' };

          spyOn(AlarmService, 'getByNode').andReturn(getThenObject(fakeAlarm));
          spyOn(scope, 'processInterfaces');

          scope.processNode(fakeNode);

          expect(scope.node.alarms.bogus).toBe('data');
        });
      });

      describe('processInterfaces', function() {
        var fakeIfaces;
        var fakeServices;

        beforeEach(function() {
          scope.node._id = 30;
          fakeIfaces = [ { ipAddress: '1.2.3.4'} ];
          fakeServices = [ { name: 'foo' }];
        });

        it('should assign the interfaces array to the node', function() {
          scope.processInterfaces(fakeIfaces);
          expect(scope.node.ifaces[0].ipAddress).toBe(fakeIfaces[0].ipAddress);
        });

        it('should retrieve services for each interface', function() {
          spyOn(NodeService, 'getIpInterfaceServices').andReturn(getThenObject(fakeServices));

          scope.processInterfaces(fakeIfaces);
          expect(NodeService.getIpInterfaceServices).toHaveBeenCalledWith(scope.node._id, fakeIfaces[0].ipAddress);
          expect(scope.node.ifaces[0].services[0].name).toBe(fakeServices[0].name);
        });
      });
    });
  });
});
