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
  .factory('PagedResourceFactory', ['$q', '$rootScope', '$log', '$http', 'ConfigService', function ($q, $rootScope, $log, $http, config) {
    $log.debug('Initializing PagedResourceFactory.');

    var x2js = new X2JS();
    var paged = new Object();
    paged.internal = new Object();

    /**
     * @ngdoc service
     * @name PagedResource
     * @param {String} url The URL fragment that this paged resource is based on.
     *                 This URL fragment will be prepended by the base URL returned
     *                 by the config module.
     * @param {Number} limit (Optional) The default number of responses to return
     *                 per page.  Defaults to 50.
     *
     * @description A PagedResource is an object which represents an OpenNMS ReST
     * URL and can be interacted with to manage pagination and sorting of ReST
     * responses.
     */
    function PagedResource(url, limit) {
      var self = this;
      self.internal = {
        url: url,
        limit: (limit? limit : 50),
        page: 0,
        orderBy: undefined,
        ascending: true,
        params: {},
        callback: undefined
      };

      /**
       * @description Get the current page.
       *
       * @ngdoc method
       * @name PagedResource#currentPage
       * @methodOf PagedResource
       * @returns {Number} Returns the current page number.
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
       * @returns {Number} Returns the new page number.
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
       * @returns {Number} Returns the new page number.
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
       * @returns {Number} Returns the first page number (0).
       */
      self.firstPage = function() {
        self.internal.page = 0;
        return self.internal.page;
      };

      /**
       * @description Set the current page.
       *
       * @ngdoc method
       * @name PagedResource#setPage
       * @methodOf PagedResource
       * @param {Number} page The page number to set.
       * @returns {Number} Returns the newly set page number.
       */
      self.setPage = function(page) {
        self.internal.page = page;
        return self.internal.page;
      };

      /**
       * @description Set the attribute to sort by.
       *
       * Note that changing orderBy will also reset the current page to 0.
       *
       * @ngdoc method
       * @name PagedResource#orderBy
       * @methodOf PagedResource
       * @param {String} orderBy The attribute to sort by.  Set to undefined to revert to default ordering.
       */
      self.orderBy = function(orderBy) {
        self.internal.orderBy = orderBy;
        self.firstPage();
      };

      /**
       * @description Set the sort order.
       *
       * Note that changing the order will also reset the current page to 0.
       *
       * @ngdoc method
       * @name PagedResource#order
       * @methodOf PagedResource
       * @param {String} order Set to 'asc' or 'desc'. If unspecified or invalid, defaults to 'asc'.
       */
      self.order = function(order) {
        if (order === 'desc') {
          self.internal.ascending = false;
        } else {
          self.internal.ascending = true;
        }
        self.firstPage();
      };

      /**
       * @description Reverse the sort order.
       *
       * Note that reversing the order will also reset the current page to 0.
       *
       * @ngdoc method
       * @name PagedResource#reverse
       * @methodOf PagedResource
       */
      self.reverse = function() {
        self.internal.ascending = !self.internal.ascending;
        self.firstPage();
      };

      /**
       * @description Set the options to be used when querying.
       *
       * Note that setting the query parameters will reset the current page to 0.
       *
       * @ngdoc method
       * @name PagedResource#setParams
       * @methodOf PagedResource
       * @param {Object} params The query parameters to pass with the request.
       */
      self.setParams = function(params) {
        if (params === undefined) {
          self.internal.params = {};
        } else {
          self.internal.params = params;
        }
        self.firstPage();
      };

      /**
       * @description Set a callback to be called upon successful HTTP response.
       *
       * @ngdoc method
       * @name PagedResource#setCallback
       * @methodOf PagedResource
       * @param {function} callback The callback to call when the request succeeds.
       */
      self.setCallback = function(callback) {
        self.internal.callback = callback;
      };

      /**
       * @description Set how many results should be returned per page.
       *
       * Note that setting the result limit will reset the current page to 0.
       *
       * @ngdoc method
       * @name PagedResource#setLimit
       * @methodOf PagedResource
       * @param {Number} limit The number of results expected.
       * @returns {Number} Returns the number of results that should be returned.
       */
      self.setLimit = function(limit) {
        self.internal.limit = limit;
        self.firstPage();
        return self.internal.limit;
      };

      /**
       * @description Get the current page's resource data.
       *
       * @ngdoc method
       * @name PagedResource#getCurrentResponse
       * @methodOf PagedResource
       * @returns {Promise} Returns a promise which will be resolved with the data from the current page's URL.
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
       * @returns {Promise} Returns a promise which will be resolved with the data from the next page's URL.
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
       * @returns {Promise} Returns a promise which will be resolved with the data from the previous page's URL.
       */
      self.getPreviousResponse = function() {
        self.previousPage();
        return self.getCurrentResponse();
      };

      self.internal.buildUrl = function() {
        var ret = self.internal.url + '?offset=' + (self.internal.page * self.internal.limit) + '&limit=' + self.internal.limit;
        if (self.internal.orderBy !== undefined) {
          ret += '&orderBy=' + encodeURIComponent(self.internal.orderBy);
          ret += '&order=' + (self.internal.ascending? 'asc' : 'desc');
        }

        var params = self.internal.params;
        for (var key in params) {
          if (angular.isArray(params[key])) {
            for (var i=0; i < params[key].length; i++) {
              ret += '&' + key + '=' + encodeURIComponent(params[key][i]);
            }
          } else {
            ret += '&' + key + '=' + encodeURIComponent(params[key]);
          }
        }

        $log.debug('PagedResource.buildUrl: URL=' + ret);
        return ret;
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
          if (self.internal.callback) {
            self.internal.callback(results);
          }
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
     * @param {String} urlBase the URL component under the OpenNMS ReST service to
     *        query (ie, '/alarms' for localhost:8980/opennms/rest/alarms)
     * @param {Number} limit the number of results to return per page (defaults to 50)
     * @returns {PagedResource} a PagedResource object.
     */
    paged.createResource = function(urlBase, limit) {
      var url = config.getRoot() + '/rest' + ((urlBase.indexOf('/') == 0)? urlBase : ('/' + urlBase));
      $log.debug('PagedResourceFactory.createResource(' + urlBase + '): Creating PagedResource with URL: ' + url);
      var resource = new PagedResource(url, limit);
      return resource;
    }

    return paged;
  }])
  ;
}());
