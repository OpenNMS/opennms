/* eslint no-console: 0 */

const angular = require('vendor/angular-js');
const moment = require('moment');
const JSJoda = require('js-joda');
require('js-joda-timezone');

function OnmsDateFormatter() {
}

OnmsDateFormatter.prototype.init = function init(readyCallback) {
	var self = this;
	var defaultFormat = "yyyy-MM-dd'T'HH:mm:ssxxx"; // eslint-disable-line quotes
	window._onmsZoneId = undefined;

	var xhr = new XMLHttpRequest();
	xhr.onreadystatechange = function readystatechange() {
		try {
			if (xhr.readyState === XMLHttpRequest.DONE) {
				if (xhr.status === 200) {
					var config = JSON.parse(xhr.responseText);
					if (config.datetimeformatConfig && config.datetimeformatConfig.datetimeformat) {
						window._onmsDateTimeFormat = config.datetimeformatConfig.datetimeformat || defaultFormat;
						window._onmsZoneId = config.datetimeformatConfig.zoneId;
					} else {
						console.log('Error: datetimeformatConfig property not found:',config);
						window._onmsDateTimeFormat = defaultFormat;
					}
				} else {
					console.log('Error: failed to request format info: ' + xhr.status + ' ' + xhr.statusText);
					window._onmsDateTimeFormat = defaultFormat;
				}
				readyCallback(self, xhr.status);
			}
		} catch (e) {
			console.log('Error: failed to request format info: ', e);
			window._onmsDateTimeFormat = defaultFormat;
			readyCallback(self, xhr.status);
		}
	};
	xhr.open('GET', 'rest/info');
	xhr.setRequestHeader('Accept', 'application/json');
	xhr.send();
};

OnmsDateFormatter.prototype.assertInitialized = function assertInitialized() {
	if (!window._onmsDateTimeFormat) {
		console.log('OnmsDateFormatter.init() must complete before using!');
		throw new Error('OnmsDateFormatter.init() must complete before using!');
	}
};

OnmsDateFormatter.prototype.getFormatter = function getFormatter() {
	var self = this;
	self.assertInitialized();

	if (!self._formatter) {
		self._formatter = JSJoda.DateTimeFormatter.ofPattern(window._onmsDateTimeFormat);
	}
	return self._formatter;
};

OnmsDateFormatter.prototype.getZoneId = function getZoneId() {
	var self = this;
	self.assertInitialized();

	if (!self._zoneId) {
		if (window._onmsZoneId) {
			try {
				self._zoneId = JSJoda.ZoneId.of(window._onmsZoneId);
			} catch (err) {
				console.log('Unhandled zone ID ' + window._onmsZoneId + ': ' + err);
				console.log('Falling back to default browser zone.');
				self._zoneId = JSJoda.ZoneId.SYSTEM;
			}
		} else {
			self._zoneId = JSJoda.ZoneId.SYSTEM;
		}
	}
	return self._zoneId;
};

OnmsDateFormatter.prototype.format = function format(date) {
	var self = this;
	self.assertInitialized();

	if (date === undefined || date === null) {
		return date;
	}

	//console.log('Formatting "' + date + '" with formatter "' + window._onmsDateTimeFormat + '" and zone ID "' + window._onmsZoneId + '"');
	var jodaDate = JSJoda.ZonedDateTime
	.from(JSJoda.nativeJs(moment(date)))
	.withZoneSameLocal(self.getZoneId());

	return self.getFormatter().format(jodaDate);
};

//window.OnmsDateFormatter = OnmsDateFormatter;

(function() {
	'use strict';
	if (typeof jest === 'undefined') {
		var f = new OnmsDateFormatter();
		f.init(function() {
			window._onmsFormatter = f;
		});
	} else {
		console.log('Running in a test environment. Skipping automatic initialization.');
	}
})();

(function() {
	'use strict';

	if (window.angular) {
		angular.module('onmsDateFormatter', ['ng']).factory('DateFormatterService', ['$interval', '$q', function DateFormatterService($interval, $q) {
			console.log('Initializing DateFormatterService');

			var deferred = $q.defer();

			var count = 0;
			var i = $interval(function() {
				if (window._onmsFormatter) {
					console.log('Global formatter found: ' + window._onmsDateTimeFormat);
					deferred.resolve(window._onmsFormatter);
					$interval.cancel(i);
				}
				if (count++ > 300) {
					console.log('DateFormatterService: giving up waiting for global formatter.');
					$interval.cancel(i);
				}
			}, 100);

			return {
				raw: window._onmsFormatter,
				formatter: deferred.promise,
				format: function(date) {
					return deferred.promise.then(function(f) {
						return f.format(date);
					});
				}
			}
		}]);

		angular.module('onmsDateFormatter').filter('onmsDate', ['$filter', 'DateFormatterService', function($filter, DateFormatterService) {
			return function onmsDate(input, ifEmpty) {
				var ret;
				if (window._onmsFormatter) {
					// If the formatter has finished initializing, use it
					ret = window._onmsFormatter.format(input);
					//console.log('onmsDate: formatter returned: ' + ret);
				} else {
					// Otherwise, use ISO format
					ret = $filter('date')(input, 'yyyy-MM-ddTHH:mm:ss.sssZ');
					//console.log('onmsDate: $filter(date) returned: ' + ret);
				}
				if (ret !== undefined && ret !== null) {
					return ret;
				}
				return ifEmpty;
			}
		}]);

		angular.module('onmsDateFormatter').directive('onmsDate', ['DateFormatterService', function(dateFormatterService) {
			return {
				restrict: 'E',
				compile: function(element) {
					dateFormatterService.format(element.text()).then(function(formatted) {
						element.replaceWith(formatted);
					}).catch(function(e) {
						console.error('Failed to format ' + element.text(), e);
					});
				}
			};
		}]);
	} else {
		console.log('Angular not found.  Not initializing OnmsDateFormatter Angular components.');
	}
})();

module.exports = OnmsDateFormatter;