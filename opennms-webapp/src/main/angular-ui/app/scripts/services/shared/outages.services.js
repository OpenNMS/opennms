(function() {
  'use strict';

  angular.module('opennms.services.shared.outages', [
    'opennms.services.shared.config'
  ])

    .factory('OutageService', ['$q', '$log', '$http', 'ConfigService', function($q, $log, $http, config) {
        /* global X2JS: true */
        var x2js = new X2JS();

        var getOutageSummaries = function() {
          var summaryUrl = config.getRoot() + '/rest/outages/summaries';
          $log.debug('getOutageSummaries: GET ' + summaryUrl);

          var deferred = $q.defer();
          $http({
            'method': 'GET',
            'url': summaryUrl,
            'headers': {
              'Accept': 'application/xml'
            }
          }).success(function(data, status, headers, config) {
            /* global OutageSummary: true */

            var results = x2js.xml_str2json(data);
            var ret = [];
            if (results && results['outage-summaries'] && results['outage-summaries']['outage-summary']) {
              var summaries = results['outage-summaries']['outage-summary'];
              for (var i = 0; i < summaries.length; i++) {
                ret.push(new OutageSummary(summaries[i]));
              }
            }
            deferred.resolve(ret);
          }).error(function(data, status, headers, config) {
            $log.error('GET ' + summaryUrl + ' failed:', data, status);
            deferred.reject(status);
          });
          return deferred.promise;
        };

        var getOutages = function() {
          var summaryUrl = config.getRoot() + '/rest/outages';
          $log.debug('getOutages: GET ' + summaryUrl);

          var deferred = $q.defer();
          $http({
            'method': 'GET',
            'url': summaryUrl,
            'params': {'limit': 0},
            'headers': {
              'Accept': 'application/xml'
            }
          }).success(function(data, status, headers, config) {
            /* global OutageSummary: true */

            var results = x2js.xml_str2json(data);
            $log.debug('getOutages: results:', results.outages);
            var ret = [];
            if (results && results['outages'] && results['outages']['outage']) {
              var outage = results['outages']['outage'];
              for (var i = 0; i < outage.length; i++) {
                ret.push(new Outage(outage[i]));
              }
            }
            deferred.resolve(ret);
          }).error(function(data, status, headers, config) {
            $log.error('GET ' + summaryUrl + ' failed:', data, status);
            deferred.reject(status);
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
