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
          var nodeUrl = config.getRoot() + '/rest/ksc/' + id;
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
              if (!angular.isArray(results.kscReports.kscReport)) {
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

        var getMetrics = function(nodeId) {
          var reportsUrl = config.getRoot() + '/rest/nodes/' + nodeId + '/metrics';
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
            $log.debug('results:', results);
            var metrics = [];
            if (results && results.metrics && results.metrics.resource) {
              metrics = results.metrics;
              if (!angular.isArray(metrics.resource)) {
                metrics.resource = [metrics.resource];
              }
              metrics.resource.map(function(resource) {
                if (!angular.isArray(resource.metric)) {
                  resource.metric = [resource.metric];
                }
              });

            }
            deferred.resolve(metrics);
          }).error(function(data, status, headers, config) {
            $log.error('GET ' + reportsUrl + ' failed:', data, status);
            deferred.reject(status);
          });
          return deferred.promise;
        };

        var getGraphs = function(nodeId, resourceId) {
          var reportsUrl = config.getRoot() + '/rest/nodes/' + nodeId + '/metrics/' + resourceId;
          $log.debug('getGraphs: GET ' + reportsUrl);

          var deferred = $q.defer();
          $http({
            'method': 'GET',
            'url': reportsUrl,
            'headers': {
              'Accept': 'application/xml'
            }
          }).success(function(data, status, headers, config) {
            var results = x2js.xml_str2json(data);
            var metrics = [];
            if (results && results.metrics && results.metrics.resource) {
              metrics = results.metrics;
              if (!angular.isArray(metrics.resource)) {
                metrics.resource = [metrics.resource];
              }
              metrics.resource.map(function(resource) {
                if (!angular.isArray(resource.metric)) {
                  resource.metric = [resource.metric];
                }

                resource.metric.map(function(metric) {
                  $log.debug('metric:', metric);
                  var graphs = metric.graph;
                  metric.graph = [];
                  graphs.map(function(graph) {
                    metric.graph.push(new ReportGraph({'_title': '', '_timespan': '1_hour', '_graphtype': graph, '_resourceId': metric._resourceId}));
                  });
                });
              });
            }
            deferred.resolve(metrics);
          }).error(function(data, status, headers, config) {
            $log.error('GET ' + reportsUrl + ' failed:', data, status);
            deferred.reject(status);
          });
          return deferred.promise;
        };

        return {
          'getGraphs': getGraphs,
          'getMetrics': getMetrics,
          'list': getReports,
          'get': getReport
        };
      }])

    ;
}());