(function() {
	'use strict';

	// Base URL of the REST service
	var BASE_REST_URL = 'api/v2';

	// Module name
	var MODULE_NAME = 'onms.restResources';

	/**
	 * Function used to append an extra transformer to the default $http transforms.
	 */
	function appendTransform(defaultTransform, transform) {
		defaultTransform = angular.isArray(defaultTransform) ? defaultTransform : [ defaultTransform ];
		return defaultTransform.concat(transform);
	}

	// REST $resource module
	angular.module(MODULE_NAME, [ 'ngResource' ])

	// OnmsAlarm REST $resource
	.factory('alarmFactory', function($resource, $log, $http, $location) {
		return $resource(
			BASE_REST_URL + '/alarms/:id', 
			{ id: '@id' },
			{
				'query': { 
					method: 'GET',
					isArray: true,
					// Append a transformation that will unwrap the item array
					transformResponse: appendTransform($http.defaults.transformResponse, function(data, headers, status) {
						// TODO: Figure out how to handle session timeouts that redirect to 
						// the login screen
						/*
						if (status === 302) {
							$window.location.href = $location.absUrl();
							return [];
						}
						*/
						if (status === 204) { // No content
							return [];
						} else {
							// Always return the data as an array
							return angular.isArray(data.alarm) ? data.alarm : [ data.alarm ];
						}
					})
				},
				'update': { 
					method: 'PUT'
				},
				'queryProperties': { 
					url: BASE_REST_URL + '/alarms/properties',
					method: 'GET',
					isArray: true,
					// Append a transformation that will unwrap the item array
					transformResponse: appendTransform($http.defaults.transformResponse, function(data, headers, status) {
						// TODO: Figure out how to handle session timeouts that redirect to 
						// the login screen
						/*
						if (status === 302) {
							$window.location.href = $location.absUrl();
							return [];
						}
						*/
						if (status === 204) { // No content
							return [];
						} else {
							// Always return the data as an array
							return angular.isArray(data.searchProperty) ? data.searchProperty : [ data.searchProperty ];
						}
					})
				}
			}
		);
	})

	// OnmsEvent REST $resource
	.factory('eventFactory', function($resource, $log, $http, $location) {
		return $resource(
			BASE_REST_URL + '/events/:id', 
			{ id: '@id' },
			{
				'query': { 
					method: 'GET',
					isArray: true,
					// Append a transformation that will unwrap the item array
					transformResponse: appendTransform($http.defaults.transformResponse, function(data, headers, status) {
						// TODO: Figure out how to handle session timeouts that redirect to 
						// the login screen
						/*
						if (status === 302) {
							$window.location.href = $location.absUrl();
							return [];
						}
						*/
						if (status === 204) { // No content
							return [];
						} else {
							// Always return the data as an array
							return angular.isArray(data.event) ? data.event : [ data.event ];
						}
					})
				},
				'update': { 
					method: 'PUT'
				}
			}
		);
	})

	// OnmsMinion REST $resource
	.factory('minionFactory', function($resource, $log, $http, $location) {
		return $resource(BASE_REST_URL + '/minions/:id', { id: '@id' },
			{
				'query': { 
					method: 'GET',
					isArray: true,
					// Append a transformation that will unwrap the item array
					transformResponse: appendTransform($http.defaults.transformResponse, function(data, headers, status) {
						// TODO: Figure out how to handle session timeouts that redirect to 
						// the login screen
						/*
						if (status === 302) {
							$window.location.href = $location.absUrl();
							return [];
						}
						*/
						if (status === 204) { // No content
							return [];
						} else {
							// Always return the data as an array
							return angular.isArray(data.minion) ? data.minion : [ data.minion ];
						}
					})
				},
				'update': { 
					method: 'PUT'
				}
			}
		);
	})

	// OnmsMonitoringLocation REST $resource
	.factory('monitoringLocationFactory', function($resource, $log, $http, $location) {
		return $resource(BASE_REST_URL + '/monitoringLocations/:id', {},
			{
				'query': { 
					method: 'GET',
					isArray: true,
					// Append a transformation that will unwrap the item array
					transformResponse: appendTransform($http.defaults.transformResponse, function(data, headers, status) {
						// TODO: Figure out how to handle session timeouts that redirect to 
						// the login screen
						/*
						if (status === 302) {
							$window.location.href = $location.absUrl();
							return [];
						}
						*/
						if (status === 204) { // No content
							return [];
						} else {
							// Always return the data as an array
							return angular.isArray(data.location) ? data.location : [ data.location ];
						}
					})
				},
				'update': { 
					method: 'PUT'
				}
			}
		);
	})

	// OnmsNotification REST $resource
	.factory('notificationFactory', function($resource, $log, $http, $location) {
		return $resource(BASE_REST_URL + '/notifications/:id', { id: '@id' },
			{
				'query': { 
					method: 'GET',
					isArray: true,
					// Append a transformation that will unwrap the item array
					transformResponse: appendTransform($http.defaults.transformResponse, function(data, headers, status) {
						// TODO: Figure out how to handle session timeouts that redirect to 
						// the login screen
						/*
						if (status === 302) {
							$window.location.href = $location.absUrl();
							return [];
						}
						*/
						if (status === 204) { // No content
							return [];
						} else {
							// Always return the data as an array
							return angular.isArray(data.notification) ? data.notification : [ data.notification ];
						}
					})
				},
				'update': { 
					method: 'PUT'
				},
				'queryProperties': { 
					url: BASE_REST_URL + '/notifications/properties',
					method: 'GET',
					isArray: true,
					// Append a transformation that will unwrap the item array
					transformResponse: appendTransform($http.defaults.transformResponse, function(data, headers, status) {
						// TODO: Figure out how to handle session timeouts that redirect to 
						// the login screen
						/*
						if (status === 302) {
							$window.location.href = $location.absUrl();
							return [];
						}
						*/
						if (status === 204) { // No content
							return [];
						} else {
							// Always return the data as an array
							return angular.isArray(data.searchProperty) ? data.searchProperty : [ data.searchProperty ];
						}
					})
				}
			}
		);
	})

	// OnmsOutage REST $resource
	.factory('outageFactory', function($resource, $log, $http, $location) {
		return $resource(BASE_REST_URL + '/outages/:id', { id: '@id' },
			{
				'query': { 
					method: 'GET',
					isArray: true,
					// Append a transformation that will unwrap the item array
					transformResponse: appendTransform($http.defaults.transformResponse, function(data, headers, status) {
						// TODO: Figure out how to handle session timeouts that redirect to 
						// the login screen
						/*
						if (status === 302) {
							$window.location.href = $location.absUrl();
							return [];
						}
						*/
						if (status === 204) { // No content
							return [];
						} else {
							// Always return the data as an array
							return angular.isArray(data.outage) ? data.outage : [ data.outage ];
						}
					})
				},
				'update': { 
					method: 'PUT'
				}
			}
		);
	})

	.factory('scanReportLogFactory', function($resource, $log, $http, $location) {
		return $resource(BASE_REST_URL + '/scanreports/:id/logs', { id: '@id' },
			{
				'query': { 
					method: 'GET',
					transformResponse: function(data, headers, status) {
						var ret;
						switch(status) {
							case 302: // refresh on redirect
								$window.location.href = $location.absUrl();
								ret = {};
								break;
							case 204: // no content
								ret = {};
								break;
							default:
								ret = {text:data};
						}
						//$log.debug('$resource(logs) returning: ' + angular.toJson(ret));
						return ret;
					}
				}
			}
		);
	})

	// ScanReport REST $resource
	.factory('scanReportFactory', function($resource, $log, $http, $location) {
		return $resource(BASE_REST_URL + '/scanreports/:id', { id: '@id' },
			{
				'query': { 
					method: 'GET',
					isArray: true,
					// Append a transformation that will unwrap the item array
					transformResponse: appendTransform($http.defaults.transformResponse, function(data, headers, status) {
						// TODO: Figure out how to handle session timeouts that redirect to 
						// the login screen
						/*
						if (status === 302) {
							$window.location.href = $location.absUrl();
							return [];
						}
						*/
						if (status === 204) { // No content
							return [];
						} else {
							// Always return the data as an array
							return angular.isArray(data['scan-report']) ? data['scan-report'] : [ data['scan-report'] ];
						}
					})
				},
				'update': { 
					method: 'PUT'
				}
			}
		);
	});

}());
