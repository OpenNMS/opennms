(function() {
  'use strict';

  angular.module('opennms.services.shared.reports', [
    'opennms.services.shared.config'
  ])

    .factory('ReportsService', ['$q', '$log', '$http', 'ConfigService', function($q, $log, $http, config) {
      $log.debug('ReportsService Initializing.');
      /* global X2JS: true */
      var x2js = new X2JS();

      var getReport = function(id) {
        var nodeUrl = config.getRoot() + '/rest/ksc/'+ id;
        $log.debug('getReport: GET ' + nodeUrl);

        var deferred = $q.defer();
        $http({
          'method': 'GET',
          'url': nodeUrl,
          'headers': {
            'Accept': 'application/xml'
          }
        }).success(function(data, status, headers, config) {
          var results = x2js.xml_str2json(data);
          $log.debug('getReport: got results: ', results);
          var report = undefined;
          if (results && results.kscReport) {
            report = new Report(results.kscReport);
          }
          deferred.resolve(report);
        }).error(function(data, status, headers, config) {
          $log.error('GET ' + nodeUrl + ' failed:', data, status);
          deferred.reject(status);
        });
        return deferred.promise;
      };

      var getReports = function(offset, limit) {
        if (limit === undefined) {
          limit = 50;
        }
        if (offset === undefined) {
          offset = 0;
        }

        var reportsUrl = config.getRoot() + '/rest/ksc?limit=' + limit + '&offset=' + offset;
        $log.debug('getReports: GET ' + reportsUrl);

        var deferred = $q.defer();
        $http({
          'method': 'GET',
          'url': reportsUrl,
          'headers': {
            'Accept': 'application/xml'
          }
        }).success(function(data, status, headers, config) {
          var results = x2js.xml_str2json(data);
          var reports = [];
          if (results && results.kscReports && results.kscReports.kscReport) {
            if(!angular.isArray(results.kscReports.kscReport)) {
              reports.push(new Report(results.kscReports.kscReport));
            } else {
              results.kscReports.kscReport.map(function(report) {
                reports.push(new Report(report));
              });
            }
          }
          deferred.resolve(reports);
        }).error(function(data, status, headers, config) {
          $log.error('GET ' + reportsUrl + ' failed:', data, status);
          deferred.reject(status);
        });
        return deferred.promise;
      };

      return {
        'list': getReports,
        'get': getReport
      };
    }])

  ;
}());