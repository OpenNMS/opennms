describe('Shared Services Module - Node', function() {
  var rootScope;
  var NodeService;

  beforeEach(module('opennms.services.shared.nodes'));
  beforeEach(inject(function(_$rootScope_, _NodeService_) {
    rootScope = _$rootScope_;
    NodeService = _NodeService_;
  }));

  describe('public functions', function() {
    describe('get', function() {
      it('should build a REST URL with the node ID', function() {
        spyOn(NodeService.internal, 'fetchNode');

        NodeService.get(1);
        expect(NodeService.internal.fetchNode).toHaveBeenCalledWith('/opennms/rest/nodes/1')
      });
    });

    describe('list', function() {
      it('should default the limit and offset', function() {
        var defaultLimit = 50;
        var defaultOffset = 0;
        spyOn(NodeService.internal, 'fetchNodes');

        NodeService.list();

        var nodesUrl = NodeService.internal.fetchNodes.mostRecentCall.args[0];
        expect(nodesUrl).toContain('limit=' + defaultLimit);
        expect(nodesUrl).toContain('offset=' + defaultOffset);
      });
    });
  });

  describe('internal functions', function() {
    describe('fetchNode', function() {
      it('should retrieve and call a success handler', inject(function(_$httpBackend_) {
        var fakeUrl = '/people/1234';
        // The actual URL doesn't matter, as long as it is used unmodified
        _$httpBackend_.expectGET(fakeUrl).respond({});

        // We don't want it to actually get and use a handler, so we'll stub it out.
        NodeService.handlerFn = function() {};
        NodeService.internal.getNodeSuccessHandler = function() {
          return NodeService.handlerFn;
        };
        spyOn(NodeService, 'handlerFn');

        NodeService.internal.fetchNode(fakeUrl);
        _$httpBackend_.flush();
        expect(NodeService.handlerFn).toHaveBeenCalled();
      }));
    });

    describe('processNode', function() {
      var fakeResults;

      beforeEach(function() {
        fakeResults = { node: { _id: 123, _label: 'foo' } };
      });

      it('should return a node object', function() {
        var node = NodeService.internal.processNode(fakeResults);

        expect(node._id).toBe(fakeResults.node._id);
      });

      it('should parse the node ID to be an integer', function() {
        var node = NodeService.internal.processNode(fakeResults);

        expect(typeof node._id).toBe('number');
      })


    })
  });
});