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
        var nodeUrl = config.getRoot() + '/rest/reports/'+ id;
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
          var node = undefined;
          if (results && results.node) {
            node = results.node;
            node._id = parseInt(node._id);
          }
          deferred.resolve(node);
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

        var reportsUrl = config.getRoot() + '/rest/reports?limit=' + limit + '&offset=' + offset;
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
          if (results && results.reports && results.reports.node) {
            if(!angular.isArray(results.reports.node)) {
              reports.push(results.reports.node);
            } else {
              reports = results.reports.node;
            }
            for (var i = 0; i < reports.length; i++) {
              reports[i]['_id'] = parseInt(reports[i]['_id']);
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
        'get': getReport,
      };
    }])

  ;
}());