describe('Shared Services Module - PagedResourceFactory', function() {
  var $q;
  var rootScope;
  var ConfigService;
  var PagedResourceFactory;

  //var alarmsResponse;

  beforeEach(module('opennms.services.shared.pagedresource'));
  beforeEach(inject(function(_$q_, _$rootScope_, _PagedResourceFactory_, _ConfigService_) {
    $q = _$q_;
    rootScope = _$rootScope_;
    ConfigService = _ConfigService_;
    PagedResourceFactory = _PagedResourceFactory_;
  }));

  describe('public function', function() {
    describe('createResource', function() {
      it('should create a PagedResource object with the given URL, and a default limit and offset', function() {
        var restUrl = '/blah';
        var resource = PagedResourceFactory.createResource(restUrl);
        expect(resource).toBeDefined();
        expect(resource.internal.url).toEqual(ConfigService.getRoot() + '/rest' + restUrl);
        expect(resource.internal.limit).toEqual(50);
        expect(resource.internal.page).toEqual(0);
      });
    });

    it('should attempt to get the expected URL', function() {
      var resource = PagedResourceFactory.createResource('/blah');
      var url = '';
      resource.internal.httpGet = function(u) {
        url = u;
        return true;
      };

      var response = resource.getCurrentResponse();
      expect(response).toBeDefined();
      expect(url).toEqual('/opennms/rest/blah?offset=0&limit=50');
    });

    it('should increase the offset by limit', function() {
      var resource = PagedResourceFactory.createResource('/blah');
      expect(resource.nextPage()).toEqual(1);
    });

    it('should decrease the offset by limit', function() {
      var resource = PagedResourceFactory.createResource('/blah');
      resource.internal.page=1;
      expect(resource.previousPage()).toEqual(0);
    });

    it('should not decrease below 0', function() {
      var resource = PagedResourceFactory.createResource('/blah');
      expect(resource.previousPage()).toEqual(0);
    });

    it('should attempt to get the next page', function() {
      var resource = PagedResourceFactory.createResource('/blah');
      var url = '';
      resource.internal.httpGet = function(u) {
        url = u;
        return true;
      };

      var response = resource.getNextResponse();
      expect(response).toBeDefined();
      expect(url).toEqual('/opennms/rest/blah?offset=50&limit=50');
    });

    it('should attempt to get the previous page', function() {
      var resource = PagedResourceFactory.createResource('/blah');
      var url = '', response;
      resource.internal.httpGet = function(u) {
        url = u;
        return true;
      };

      resource.nextPage();
      response = resource.getCurrentResponse();
      expect(url).toEqual('/opennms/rest/blah?offset=50&limit=50');

      response = resource.getPreviousResponse();
      expect(response).toBeDefined();
      expect(url).toEqual('/opennms/rest/blah?offset=0&limit=50');
    });

    it('should work with a different limit', function() {
      var resource = PagedResourceFactory.createResource('/blah', 9);
      var url = '', response;
      resource.internal.httpGet = function(u) {
        url = u;
        return true;
      };

      resource.getCurrentResponse();
      expect(url).toEqual('/opennms/rest/blah?offset=0&limit=9');

      resource.getNextResponse();
      expect(url).toEqual('/opennms/rest/blah?offset=9&limit=9');

      resource.getNextResponse();
      expect(url).toEqual('/opennms/rest/blah?offset=18&limit=9');

      resource.previousPage();
      resource.previousPage();
      resource.getCurrentResponse();
      expect(url).toEqual('/opennms/rest/blah?offset=0&limit=9');

      resource.nextPage();
      resource.getCurrentResponse();
      expect(url).toEqual('/opennms/rest/blah?offset=9&limit=9');

      resource.previousPage();
      resource.previousPage();
      resource.getCurrentResponse();
      expect(url).toEqual('/opennms/rest/blah?offset=0&limit=9');
    });

    it('should include orderBy if specified', function() {
      var resource = PagedResourceFactory.createResource('/blah');
      resource.orderBy('alarmId');
      var url = '', response;
      resource.internal.httpGet = function(u) {
        url = u;
        return true;
      };

      resource.getCurrentResponse();
      expect(url).toEqual('/opennms/rest/blah?offset=0&limit=50&orderBy=alarmId&order=asc');

      resource.reverse();
      resource.getCurrentResponse();
      expect(url).toEqual('/opennms/rest/blah?offset=0&limit=50&orderBy=alarmId&order=desc');
    });

    it('should reset the offset if order is modified', function() {
      var resource = PagedResourceFactory.createResource('/blah');
      var url = '', response;
      resource.internal.httpGet = function(u) {
        url = u;
        return true;
      };

      resource.nextPage();
      resource.nextPage();
      resource.nextPage();

      expect(resource.currentPage()).toBe(3);
      resource.orderBy('alarmId');
      expect(resource.currentPage()).toBe(0);

      resource.setPage(14);
      expect(resource.currentPage()).toBe(14);
      resource.reverse();
      expect(resource.currentPage()).toBe(0);
    });

    it('should add the specified parameters to the URL', function() {
      var resource = PagedResourceFactory.createResource('/blah');
      var url = '', response;
      resource.internal.httpGet = function(u) {
        url = u;
        return true;
      };

      resource.setPage(2);
      resource.getCurrentResponse();
      expect(url).toBe('/opennms/rest/blah?offset=100&limit=50');

      resource.setParams({
        foo: 'bar',
        baz: ['a', 'b', 'c']
      });

      resource.getCurrentResponse();
      expect(url).toBe('/opennms/rest/blah?offset=0&limit=50&foo=bar&baz=a&baz=b&baz=c');
    });

  });


  describe('internal functions', function() {
    it('should return the default URL when buildUrl() is called', function() {
      var resource = PagedResourceFactory.createResource('/blah');
      expect(resource.internal.buildUrl()).toEqual('/opennms/rest/blah?offset=0&limit=50');
    });

    it('should fetch the expected URL', inject(function(_$httpBackend_) {
      var resource = PagedResourceFactory.createResource('/blah');
      _$httpBackend_.expectGET('/opennms/rest/blah?offset=0&limit=50').respond('<thing>blah</thing>');
      var promise = resource.getCurrentResponse();
      expect(promise).toBeDefined();
      promise.then(function(ret) {
        expect(ret.thing).toBeDefined();
        expect(ret.thing).toEqual('blah');
      }, function(err) {
        expect(err).toBeUndefined();
      });
      _$httpBackend_.flush();
      rootScope.$apply();
    }));


    it('should call the callback', inject(function(_$httpBackend_) {
      var resource = PagedResourceFactory.createResource('/blah');
      var callbackResult = undefined;
      resource.setCallback(function(r) {
        callbackResult = r;
      });

      _$httpBackend_.expectGET('/opennms/rest/blah?offset=0&limit=50').respond('<thing>blah</thing>');

      var promise = resource.getCurrentResponse();
      expect(promise).toBeDefined();
      promise.then(function(response) {
        expect(response.thing).toBeDefined();
        expect(response.thing).toEqual('blah');

        expect(callbackResult).toBeDefined();
        expect(callbackResult.thing).toBeDefined();
        expect(callbackResult.thing).toEqual('blah');
      });
      _$httpBackend_.flush();
      rootScope.$apply();
    }));

  });

});
