describe('Shared Node Controllers Module', function() {
  var getThenObject;

  beforeEach(function() {
    // This is helpful for promises.
    getThenObject = function(response) {
      return { then: function(cb) { cb(response) } };
    }
  })
  beforeEach(module('opennms.controllers.shared.nodes'));
  beforeEach(function() {
     module('opennms.controllers.shared.nodes');
  });
  describe('NodesCtrl', function() {
    var scope;
    var $controller;
    var rootScope;

    var nodesController;
    var NodeService;

    beforeEach(inject(function(_$rootScope_, _$controller_, _$log_, _NodeService_) {
      rootScope = _$rootScope_;
      scope = rootScope.$new();
      $controller = _$controller_;
      NodeService = _NodeService_;

      // Stub these out.
      NodeService.list = function() {}
      spyOn(NodeService, 'list').andReturn(getThenObject({}));

      nodesController = $controller('NodesCtrl', {
        $rootScope: rootScope,
        $scope: scope,
        $log: _$log_,
        NodeService: NodeService
      });

    }));

    describe('init', function() {
      it('should call node factory for nodes', function() {
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
    var NodeService;

    beforeEach(inject(function(_$rootScope_, _$controller_, _$log_, _NodeService_) {
      rootScope = _$rootScope_;
      scope = rootScope.$new();
      $controller = _$controller_;
      NodeService = _NodeService_;

      // Stub these out.
      scope.fakeStateParams = { nodeId: 1 };  // Add fake state params.
      NodeService.list = function() {
        return { then: function(cb) { cb({}) } };
      };


      nodeDetailController = $controller('NodeDetailCtrl', {
        $rootScope: rootScope,
        $scope: scope,
        $stateParams: scope.fakeStateParams,
        $log: _$log_,
        NodeService: NodeService
      });
    }));

    describe('init', function() {
      it('should populate the node from the node detail factory', function() {
        spyOn(NodeService, 'get').andReturn(getThenObject({_id: 1}));

        scope.init();
        expect(NodeService.get).toHaveBeenCalledWith(1);
      })
    })
  });
});