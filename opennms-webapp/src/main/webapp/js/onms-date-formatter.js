function OnmsDateFormatter() {
}

OnmsDateFormatter.prototype.init = function init(readyCallback) {
	var self = this;
	var defaultFormat = "yyyy-MM-dd'T'HH:mm:ssxxx";
	window._onmsZoneId = undefined;

	var xhr = new XMLHttpRequest();
	xhr.onreadystatechange = function readystatechange() {
		try {
			if (xhr.readyState === XMLHttpRequest.DONE) {
				if (xhr.status === 200) {
					var config = JSON.parse(xhr.responseText);
					window._onmsDateTimeFormat = config.datetimeformat || defaultFormat;
					window._onmsZoneId = config.zoneId;
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
		throw 'OnmsDateFormatter.init() must complete before using!';
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
				console.log('Unhandled zone ID ' + this.config.zoneId + ': ' + err);
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

	if (!date) {
		throw 'OnmsDateFormatter.format: No date supplied!';
	}

	var jodaDate = JSJoda.ZonedDateTime
		.from(JSJoda.nativeJs(moment(date)))
		.withZoneSameLocal(self.getZoneId());

	return self.getFormatter().format(jodaDate);
};

window.OnmsDateFormatter = OnmsDateFormatter;

(function() {
	'use strict';

	if (window.angular) {
		angular.module('onmsDateFormatter', ['ng']).factory('onmsDateFormatterFactory', ['$q', function onmsDateFormatterService($q) {
			console.log('Initializing onmsDateFormatterFactory');

			var deferred = $q.defer();
			var formatter = new OnmsDateFormatter();
			formatter.init(function() {
				deferred.resolve(formatter);
			});

			return function() {
				return deferred.promise;
			}
		}]);

		angular.module('onmsDateFormatter').directive('onmsDate', ['onmsDateFormatterFactory', function(formatterFactory) {
			return {
				restrict: 'E',
				compile: function(element) {
					formatterFactory().then(function(formatter) {
						var formatted = formatter.format(element.text());
						element.replaceWith(formatted);
					}).catch(function(e) {
						console.error('Failed to format ' + element.text(), e);
					});
				}
			};
		}]);
	}
})();