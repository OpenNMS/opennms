(function () {
  'use strict';

  angular.module('opennms.services.shared.alarms', [
    'opennms.services.shared.config'
  ])

  /**
   * @ngdoc service
   * @name alarms.Services.AlarmsService
   *
   * @description The AlarmService provides components with access to the OpenNMS alarms REST resource.
   */
    .factory('AlarmService', ['$q', '$log', '$http', 'ConfigService', function ($q, $log, $http, config) {
      /* global X2JS: true */
      var x2js = new X2JS();

      var alarmService = new Object();
      alarmService.internal = new Object();

      /**
       * @description Retrieve a specific alarm. (TODO)
       *
       * @ngdoc method
       * @name alarms.Services.AlarmsService#get
       * @methodOf alarms.Services.AlarmsService
       * @param {number} alarmId an alarm ID
       * @returns {*} an angular promise to return a specific Alarm
       */
      alarmService.get = function (alarmId) {
      };

      /**
       * @description Retrieve alarms for a given node ID, limit and offset to paginate.
       *
       * @name alarms.Services.AlarmsService#getByNode
       *
       * @ngdoc method
       * @methodOf alarms.Services.AlarmsService
       * @param {number} nodeId the node ID to retrieve alarms for.
       * @param {number} offset the alarm count to start the retrieval at. (default 0)
       * @param {number} limit the total alarms to retrieve. (default 50)
       * @returns {*} an angular promise to return an array of Alarms
       */
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

      /**
       * @description Retrieves all active alarms, limited by offset and limit.
       *
       * @name alarms.Services.AlarmsService#list
       * @ngdoc method
       * @methodOf alarms.Services.AlarmsService
       * @param {number} offset  the alarm count to start the retrieval at. (default 0)
       * @param {number} limit  the total alarms to retrieve. (default 50)
       * @returns {*} an angular promise to return an array of Alarms
       */
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

      /**
       * @description (Internal) Used internally to initiate the $http request..
       *
       * @name alarms.Services.AlarmsService:internal.fetchAlarms
       * @ngdoc method
       * @methodOf alarms.Services.AlarmsService
       * @param {string} alarmsUrl the formatted REST URL to retrieve alarms from.
       * @returns {*} an angular promise to return an array of Alarms
       * @private
       */
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

      /**
       * @description (Internal) Processes the REST results into a new model.
       *
       * @name alarms.Services.AlarmsService:internal.processAlarmsList
       * @ngdoc method
       * @methodOf alarms.Services.AlarmsService
       * @param {Object} results the REST results object from the alarms resource.
       * @returns {Array} an array of Alarm objects.
       * @private
       */
      alarmService.internal.processAlarmListResults = function(results) {
        var alarms = [];
        if (results && results.alarms && results.alarms.alarm) {
          results.alarms.alarm.map(function (alarm) {
            alarms.push(new Alarm(alarm));
          });
        }

        return alarms;
      };

      /**
       * @description (Internal) A closure method that provides deferred to the error handler for
       * the methods that retrieve alarms listings.
       *
       * @name alarms.Services.AlarmsService:internal.getAlarmsListErrorHandler
       * @ngdoc method
       * @methodOf alarms.Services.AlarmsService
       * @param {Object} deferred the promise object that will be passed back through the API.
       * @returns {*} a handler function.
       * @private
       */
      alarmService.internal.getAlarmListErrorHandler = function(deferred) {
        var handler = function (data, status, headers, config) {
          $log.error('GET ' + alarmsUrl + ' failed:', data, status);
          deferred.reject(status);
        };

        return handler;
      };

      /**
       * @description (Internal) A closure method that provides deferred to the success handler for
       * the methods that retrieve alarms listings. It converts the raw results
       * from XML to JSON and passes on into internal.processAlarmsListResults.
       *
       * @name alarms.Services.AlarmsService:internal.getAlarmListSuccessHandler
       * @ngdoc method
       * @methodOf alarms.Services.AlarmsService
       * @param {Object} deferred the promise object that will be passed back through the API.
       * @returns {*} a handler function.
       * @private
       */
      alarmService.internal.getAlarmListSuccessHandler = function(deferred) {
        var handler = function (data, status, headers, config) {
          var results = x2js.xml_str2json(data);
          var alarms = alarmService.internal.processAlarmListResults(results);
          deferred.resolve(alarms);
        };

        return handler;
      };

      /**
       * @description (Internal) A closure method that provides deferred to the success handler for
       * the methods that retrieve alarms summaries. It converts the raw results
       * from XML to JSON and passes on into internal.processAlarmSummaryResults.
       *
       * @name alarms.Services.AlarmsService:internal.getAlarmSummarySuccessHandler
       * @ngdoc method
       * @methodOf alarms.Services.AlarmsService
       * @param {Object} deferred the promise object that will be passed back through the API.
       * @returns {*} a handler function.
       */
      alarmService.internal.getAlarmSummarySuccessHandler = function(deferred) {
        var handler = function (data, status, headers, config) {
          /* global AlarmSummary: true */

          var results = x2js.xml_str2json(data);
          var ret = alarmService.internal.processAlarmSummaryResults(results)
          deferred.resolve(ret);
        };

        return handler;
      };

      /**
       * @description (Internal) Processes the REST results
       *
       * @ngdoc method
       * @name alarms.Services.AlarmsService:processAlarmSummaryResults
       * @methodOf alarms.Services.AlarmsService
       * @param {Object} results RESTful results already converted from XML to JSON.
       * @returns {Array} an array of AlarmSummary objects.
       */
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

      /**
       * @description (Internal) A closure method that provides deferred to the error handler for
       * the methods that retrieve alarms listings.
       *
       * @name alarms.Services.AlarmsService:internal.getAlarmSummaryErrorHandler
       * @ngdoc method
       * @methodOf alarms.Services.AlarmsService
       * @param {Object} deferred the promise object that will be passed back through the API.
       * @returns {*} a handler function.
       * @private
       */
      alarmService.internal.getAlarmSummaryErrorHandler = function(deferred) {
        var handler = function (data, status, headers, config) {
          $log.error('GET ' + summaryUrl + ' failed:', data, status);
          deferred.reject(status);
        };

        return handler;
      };

      /**
       * @description Requests all alarm summaries from the OpenNMS server and returns
       *              a promise to return an array of of AlarmSummary objects.
       * @ngdoc method
       * @name alarms.Services.AlarmsService#summaries
       * @methodOf alarms.Services.AlarmsService
       * @returns {Array} an angular promise to return an array of AlarmSummary objects
       */
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
