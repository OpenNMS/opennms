(function () {
  'use strict';

  angular.module('opennms.services.shared.pagedresource', [
    'opennms.services.shared.config'
  ])

  /**
   * @ngdoc service
   * @name PagedResourceFactory
   *
   * @description The PagedResourceFactory creates objects intended to aid access to
   * standard OpenNMS ReST resources with support for sorting and pagination.
   */
  .factory('PagedResourceFactory', ['$q', '$log', '$http', 'ConfigService', function ($q, $log, $http, config) {
    var x2js = new X2JS();
    var paged = new Object();
    paged.internal = new Object();

    /**
     * @ngdoc service
     * @name PagedResource
     *
     * @description A PagedResource is an object which represents an OpenNMS ReST
     * URL and can be interacted with to manage pagination and sorting of ReST
     * responses.
     */
    function PagedResource(url, limit) {
      var self = this;
      self.internal = {};
      self.internal.url = url;
      self.internal.limit = (limit? limit : 50);
      self.internal.page = 0;

      /**
       * @description Get the current page.
       *
       * @ngdoc method
       * @name PagedResource#currentPage
       * @methodOf PagedResource
       * @returns {*} the current page number.
       */
      self.currentPage = function() {
        return angular.copy(self.internal.page);
      };

      /**
       * @description Increment the current page.
       *
       * @ngdoc method
       * @name PagedResource#nextPage
       * @methodOf PagedResource
       * @returns {*} the new page number.
       */
      self.nextPage = function() {
        self.internal.page += 1;
        return self.internal.page;
      };

      /**
       * @description Decrement the current page.
       *
       * @ngdoc method
       * @name PagedResource#previousPage
       * @methodOf PagedResource
       * @returns {*} the new page number.
       */
      self.previousPage = function() {
        self.internal.page -= 1;
        self.internal.page = Math.max(0, self.internal.page);
        return self.internal.page;
      };

      /**
       * @description Go to the first page.
       *
       * @ngdoc method
       * @name PagedResource#firstPage
       * @methodOf PagedResource
       * @returns {*} the first page number (0).
       */
      self.previousPage = function() {
        self.internal.page = 0;
        return self.internal.page;
      };

      /**
       * @description Get the current page's resource data.
       *
       * @ngdoc method
       * @name PagedResource#getCurrentResponse
       * @methodOf PagedResource
       * @returns {*} a promise which will be resolved with the data from the current page's URL.
       */
      self.getCurrentResponse = function() {
        return self.internal.httpGet(self.internal.buildUrl());
      };

      /**
       * @description Go to the next page and get its resource data.
       *
       * @ngdoc method
       * @name PagedResource#getNextResponse
       * @methodOf PagedResource
       * @returns {*} a promise which will be resolved with the data from the next page's URL.
       */
      self.getNextResponse = function() {
        self.nextPage();
        return self.getCurrentResponse();
      };

      /**
       * @description Go to the next page and get its resource data.
       *
       * @ngdoc method
       * @name PagedResource#getPreviousResponse
       * @methodOf PagedResource
       * @returns {*} a promise which will be resolved with the data from the previous page's URL.
       */
      self.getPreviousResponse = function() {
        self.previousPage();
        return self.getCurrentResponse();
      };

      self.internal.buildUrl = function() {
        return self.internal.url + '?offset=' + (self.internal.page * self.internal.limit) + '&limit=' + self.internal.limit;
      };

      self.internal.httpGet = function(url) {
        var deferred = $q.defer();
        $http({
          'method': 'GET',
          'url': url,
          'headers': {
            'Accept': 'application/xml'
          }
        }).success(function(data, status, headers, config) {
          var results = x2js.xml_str2json(data);
          deferred.resolve(results);
        }).error(function(data, status, headers, config) {
          $log.error('GET ' + url + ' failed:', data, status);
          deferred.reject(status);
        });
        return deferred.promise;
      };
    }

    /**
     * @description Create a PagedResource
     *
     * @ngdoc method
     * @name PagedResourceFactory#createResource
     * @methodOf PagedResourceFactory
     * @param {urlBase} the URL component under the OpenNMS ReST service to
     *        query (ie, '/alarms' for localhost:8980/opennms/rest/alarms)
     * @param {limit} the number of results to return per page (defaults to 50)
     * @returns {PagedResource} a PagedResource object.
     */
    paged.createResource = function(urlBase, limit) {
      var url = config.getRoot() + ((urlBase.indexOf('/') == 0)? urlBase : ('/' + urlBase));
      $log.debug('PagedResourceFactory.createResource(' + urlBase + '): Creating PagedResource with URL: ' + url);
      var resource = new PagedResource(url, limit);
      return resource;
    }

    return paged;
  }])
  ;
}());
