(function() {
	'use strict';

	angular.module('opennms.services.shared.config', [
	])

	.factory('ConfigService', ['$log', function($log) {
		return {
			'getRoot': function() {
				return 'http://admin:admin@localhost:8980/opennms';
			}
		};
	}])

	;
}());
