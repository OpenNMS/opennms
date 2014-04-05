(function() {
	'use strict';

	angular.module('opennms.services.shared.alarms', [
		'opennms.services.shared.config'
	])

	.factory('AlarmService', ['$q', '$log', '$http', 'ConfigService', function($q, $log, $http, config) {
		/* global X2JS: true */
		var x2js = new X2JS();

		var getAlarm = function() {
		};

		var getAlarms = function(offset, limit) {
			if (limit === undefined) {
				limit = 50;
			}
			if (offset === undefined) {
				offset = 0;
			}
			
			var alarmsUrl = config.getRoot() + '/rest/alarms?limit=' + limit + '&offset=' + offset;
			$log.debug('getAlarms: GET ' + alarmsUrl);

			var deferred = $q.defer();
			$http({
				'method': 'GET',
				'url': alarmsUrl,
				'headers': {
					'Accept': 'application/xml'
				}
			}).success(function(data, status, headers, config) {
				var results = x2js.xml_str2json(data);
				var alarms = [];
				if (results && results.alarms && results.alarms.alarm) {
					alarms = results.alarms.alarm;
				}
				deferred.resolve(alarms);
			}).error(function(data, status, headers, config) {
				$log.error('GET ' + alarmsUrl + ' failed:', data, status);
				deferred.reject(status);
			});
			return deferred.promise;
		};

		var getAlarmSummaries = function() {
			var summaryUrl = config.getRoot() + '/rest/alarms/summaries';
			$log.debug('getAlarmSummaries: GET ' + summaryUrl);

			var deferred = $q.defer();
			$http({
				'method': 'GET',
				'url': summaryUrl,
				'headers': {
					'Accept': 'application/xml'
				}
			}).success(function(data, status, headers, config) {
				/* global AlarmSummary: true */

				var results = x2js.xml_str2json(data);
				var ret = [];
				if (results && results['alarm-summaries'] && results['alarm-summaries']['alarm-summary']) {
					var summaries = results['alarm-summaries']['alarm-summary'];
					for (var i=0; i < summaries.length; i++) {
						ret.push(new AlarmSummary(summaries[i]));
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
			'summaries': getAlarmSummaries,
			'list': getAlarms,
			'get': getAlarm
		};
	}])

	;
}());
