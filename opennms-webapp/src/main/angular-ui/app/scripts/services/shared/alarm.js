(function () {
  'use strict';

  angular.module('opennms.services.shared.alarms', [
    'opennms.services.shared.config'
  ])

    .factory('AlarmService', ['$q', '$log', '$http', 'ConfigService', function ($q, $log, $http, config) {
      /* global X2JS: true */
      var x2js = new X2JS();

      var alarmService = new Object();
      alarmService.internal = new Object();

      alarmService.get = function () {
      };

      alarmService.getByNode = function (nodeId, offset, limit) {
        if (limit === undefined) {
          limit = 50;
        }
        if (offset === undefined) {
          offset = 0;
        }

        var alarmsUrl = config.getRoot() + '/rest/alarms?limit=' + limit + '&offset=' + offset + '&comparator=eq&nodeId=' + nodeId;
        return alarmService.internal.fetchAlarms(alarmsUrl);
      };

      alarmService.list = function (offset, limit) {
        if (limit === undefined) {
          limit = 50;
        }
        if (offset === undefined) {
          offset = 0;
        }

        var alarmsUrl = config.getRoot() + '/rest/alarms?limit=' + limit + '&offset=' + offset;
        return alarmService.internal.fetchAlarms(alarmsUrl);
      };

      alarmService.internal.fetchAlarms = function (alarmsUrl) {
        $log.debug('getAlarms: GET ' + alarmsUrl);

        var deferred = $q.defer();
        $http({
          'method': 'GET',
          'url': alarmsUrl,
          'headers': {
            'Accept': 'application/xml'
          }
        }).success(alarmService.internal.getAlarmListSuccessHandler(deferred)).error();
        return deferred.promise;
      };

      alarmService.internal.processAlarmListResults = function(results) {
        var alarms = [];
        if (results && results.alarms && results.alarms.alarm) {
          results.alarms.alarm.map(function (alarm) {
            alarms.push(new Alarm(alarm));
          });
        }

        return alarms;
      };

      alarmService.internal.getAlarmListErrorHandler = function(deferred) {
        var handler = function (data, status, headers, config) {
          $log.error('GET ' + alarmsUrl + ' failed:', data, status);
          deferred.reject(status);
        }

        return handler;
      }
      alarmService.internal.getAlarmListSuccessHandler = function(deferred) {
        var handler = function (data, status, headers, config) {
          var results = x2js.xml_str2json(data);
          var alarms = alarmService.internal.processAlarmListResults(results);
          deferred.resolve(alarms);
        };

        return handler;
      };

      alarmService.internal.getAlarmSummarySuccessHandler = function(deferred) {
        var handler = function (data, status, headers, config) {
          /* global AlarmSummary: true */

          var results = x2js.xml_str2json(data);
          var ret = alarmService.internal.processAlarmSummaryResults(results)
          deferred.resolve(ret);
        };

        return handler;
      };

      alarmService.internal.processAlarmSummaryResults = function(results) {
        var ret = [];
        if (results && results['alarm-summaries'] && results['alarm-summaries']['alarm-summary']) {
          var summaries = results['alarm-summaries']['alarm-summary'];
          for (var i = 0; i < summaries.length; i++) {
            ret.push(new AlarmSummary(summaries[i]));
          }
        }

        return ret;
      }

      alarmService.internal.getAlarmSummaryErrorHandler = function(deferred) {
        var handler = function (data, status, headers, config) {
          $log.error('GET ' + summaryUrl + ' failed:', data, status);
          deferred.reject(status);
        };

        return handler;
      };

      alarmService.summaries = function () {
        var summaryUrl = config.getRoot() + '/rest/alarms/summaries';
        $log.debug('getAlarmSummaries: GET ' + summaryUrl);

        var deferred = $q.defer();
        $http({
          'method': 'GET',
          'url': summaryUrl,
          'headers': {
            'Accept': 'application/xml'
          }
        }).success(alarmService.internal.getAlarmSummarySuccessHandler(deferred))
          .error(alarmService.internal.getAlarmSummaryErrorHandler(deferred));
        return deferred.promise;
      };

      return alarmService;
    }])

  ;
}());
