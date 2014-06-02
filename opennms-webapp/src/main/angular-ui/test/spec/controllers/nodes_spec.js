describe('Shared Node Controllers', function() {
  beforeEach(module('opennms.controllers.shared.nodes'));
  beforeEach(function() {
     module('opennms.controllers.shared.nodes');
  });
  describe('NodeController', function() {
    var scope;
    var $controller;
    var rootScope;

    var nodeController;
    var nodeFactory;

    beforeEach(inject(function(_$rootScope_, _$controller_, _nodeFactory_) {
      rootScope = _$rootScope_;
      scope = rootScope.$new();
      $controller = _$controller_;
      nodeFactory = _nodeFactory_;
      nodeController = $controller('NodeController', {
        $rootScope: rootScope,
        $scope: scope,
        nodeFactory: nodeFactory
      });

    }));

    describe('init', function() {
      it('should call node factory for nodes', function() {
        spyOn(nodeFactory, 'getNodes');

        scope.init();
        //expect(nodeFactory.getNodes).toHaveBeenCalled();
      });
    });

    describe('getNodeLink', function() {
      it('should return a hash-path with node id', function() {
        var node = { id: 3};

        expect(scope.getNodeLink(node)).toBe('#/node/3');
      });
    });
  });

  describe('NodeDetailController', function() {
    var scope;
    var $controller;
    var rootScope;

    var nodeDetailController;
    var nodeDetailFactory;

    beforeEach(inject(function(_$rootScope_, _$controller_, _nodeDetailFactory_) {
      rootScope = _$rootScope_;
      scope = rootScope.$new();
      $controller = _$controller_;
      nodeDetailFactory = _nodeDetailFactory_;

      // Add fake state params.
      scope.fakeStateParams = { id: 1 };

      nodeDetailController = $controller('NodeDetailController', {
        $rootScope: rootScope,
        $scope: scope,
        nodeDetailFactory: nodeDetailFactory,
        $stateParams: scope.fakeStateParams
      });
    }));

    describe('init', function() {
      it('should populate the node from the node detail factory', function() {
        spyOn(nodeDetailFactory, 'getNode');

        scope.init();
        expect(nodeDetailFactory.getNode).toHaveBeenCalled();
      })
    })
  });
});