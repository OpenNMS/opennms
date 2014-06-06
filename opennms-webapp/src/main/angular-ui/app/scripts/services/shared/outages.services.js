(function() {
  'use strict';

  angular.module('opennms.services.shared.outages', [
    'opennms.services.shared.config',
    'opennms.services.shared.pagedresource'
  ])

    .factory('OutageService', ['$q', '$log', '$http', 'ConfigService', 'PagedResourceFactory', function($q, $log, $http, config, prFactory) {
        /* global X2JS: true */
        var x2js = new X2JS();

        var outageSummaryResource = prFactory.createResource('/outages/summaries');
        outageSummaryResource.setLimit(20);

        var getOutageSummaries = function() {
          var deferred = $q.defer();
          outageSummaryResource.getCurrentResponse().then(function(results) {
            var ret = [];
            if (results && results['outage-summaries'] && results['outage-summaries']['outage-summary']) {
              var summaries = results['outage-summaries']['outage-summary'];
              for (var i = 0; i < summaries.length; i++) {
                ret.push(new OutageSummary(summaries[i]));
              }
            }
            deferred.resolve(ret);
          }, function(err) {
            deferred.reject(err);
          });
          return deferred.promise;
        };

        var getOutages = function(options) {
          var deferred = $q.defer();

          var defaults = {
            limit: 25,
            orderBy: 'ifLostService',
            order: 'desc'
          };
          var params = {};
          angular.extend(params, defaults, options);

          var outages = prFactory.createResource('/outages', params['limit']);
          outages.orderBy(params['orderBy']);
          outages.order(params['order']);
          delete params['limit'];
          delete params['orderBy'];
          delete params['order'];

          outages.getCurrentResponse().then(function(results) {
            $log.debug('getOutages: results:', results.outages);
            var ret = [];
            if (results && results['outages'] && results['outages']['outage']) {
              var outage = results['outages']['outage'];
              for (var i = 0; i < outage.length; i++) {
                ret.push(new Outage(outage[i]));
              }
            }
            deferred.resolve(ret);
          }, function(err) {
            deferred.reject(err);
          });

          return deferred.promise;
        };

        return {
          'summaries': getOutageSummaries,
          'list': getOutages
        };
      }])
    .factory('OutageDetailService', ['$q', '$log', '$http', 'ConfigService', function($q, $log, $http, config) {
      }])
    ;
}());
