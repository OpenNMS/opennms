/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
'use strict';

// Base URL of the REST service
const BASE_REST_URL = 'api/v2';

// Module name
const MODULE_NAME = 'onms.restResources';

const angular = require('vendor/angular-js');
require('lib/onms-http');

/**
 * Function used to append an extra transformer to the default $http transforms.
 * 
 * @param defaultTransform Existing response transformer(s)
 * @param transform New transformer to append to the end of the list
 */
const appendTransform = (defaultTransform, transform) => {
	const t = angular.isArray(defaultTransform) ? defaultTransform : [ defaultTransform ];
	return t.concat(transform);
};

/**
 * Ensure that the REST responses are always returned as arrays
 * 
 * @param data HTTP response
 * @param headers HTTP response headers
 * @param status HTTP response status code
 * @param key Name of the key where values are stored in {@code data}
 */
const arrayify = (data, headers, status, key) => {
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
	}
	// Always return the data as an array
	return angular.isArray(data[key]) ? data[key] : [ data[key] ];
};

// REST $resource module
angular.module(MODULE_NAME, [ 'onms.http', 'ngResource' ])

// OnmsAlarm REST $resource
.factory('alarmFactory', /* @ngInject */ function($resource, $log, $http, $location) {
	return $resource(
		BASE_REST_URL + '/alarms/:id', 
		{ id: '@id' },
		{
			'query': { 
				method: 'GET',
				isArray: true,
				// Append a transformation that will unwrap the item array
				transformResponse: appendTransform($http.defaults.transformResponse, function(data, headers, status) {
					return arrayify(data, headers, status, 'alarm');
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
					return arrayify(data, headers, status, 'searchProperty');
				})
			},
			'queryPropertyValues': {
				url: BASE_REST_URL + '/alarms/properties/:id',
				method: 'GET',
				isArray: true,
				// Append a transformation that will unwrap the item array
				transformResponse: appendTransform($http.defaults.transformResponse, function(data, headers, status) {
					return arrayify(data, headers, status, 'value');
				})
			}
		}
	);
})

// OnmsEvent REST $resource
.factory('eventFactory', /* @ngInject */ function($resource, $log, $http, $location) {
	return $resource(
		BASE_REST_URL + '/events/:id', 
		{ id: '@id' },
		{
			'query': { 
				method: 'GET',
				isArray: true,
				// Append a transformation that will unwrap the item array
				transformResponse: appendTransform($http.defaults.transformResponse, function(data, headers, status) {
					return arrayify(data, headers, status, 'event');
				})
			},
			'update': { 
				method: 'PUT'
			},
			'queryProperties': {
				url: BASE_REST_URL + '/events/properties',
				method: 'GET',
				isArray: true,
				// Append a transformation that will unwrap the item array
				transformResponse: appendTransform($http.defaults.transformResponse, function(data, headers, status) {
					return arrayify(data, headers, status, 'searchProperty');
				})
			},
			'queryPropertyValues': {
				url: BASE_REST_URL + '/events/properties/:id',
				method: 'GET',
				isArray: true,
				// Append a transformation that will unwrap the item array
				transformResponse: appendTransform($http.defaults.transformResponse, function(data, headers, status) {
					return arrayify(data, headers, status, 'value');
				})
			}
		}
	);
})

// OnmsMinion REST $resource
.factory('minionFactory', /* @ngInject */ function($resource, $log, $http, $location) {
	return $resource(BASE_REST_URL + '/minions/:id', { id: '@id' },
		{
			'query': { 
				method: 'GET',
				isArray: true,
				// Append a transformation that will unwrap the item array
				transformResponse: appendTransform($http.defaults.transformResponse, function(data, headers, status) {
					return arrayify(data, headers, status, 'minion');
				})
			},
			'update': { 
				method: 'PUT'
			}
		}
	);
})

// OnmsMonitoringLocation REST $resource
.factory('monitoringLocationFactory', /* @ngInject */ function($resource, $log, $http, $location) {
	return $resource(BASE_REST_URL + '/monitoringLocations/:id', {},
		{
			'query': { 
				method: 'GET',
				isArray: true,
				// Append a transformation that will unwrap the item array
				transformResponse: appendTransform($http.defaults.transformResponse, function(data, headers, status) {
					return arrayify(data, headers, status, 'location');
				})
			},
			'update': { 
				method: 'PUT'
			}
		}
	);
})

// OnmsNode REST $resource
.factory('nodeFactory', /* @ngInject */ function($resource, $log, $http, $location) {
	return $resource(BASE_REST_URL + '/nodes/:id', { id: '@id' },
		{
			'query': { 
				method: 'GET',
				isArray: true,
				// Append a transformation that will unwrap the item array
				transformResponse: appendTransform($http.defaults.transformResponse, function(data, headers, status) {
					return arrayify(data, headers, status, 'node');
				})
			},
			'update': { 
				method: 'PUT'
			},
			'queryProperties': {
				url: BASE_REST_URL + '/nodes/properties',
				method: 'GET',
				isArray: true,
				// Append a transformation that will unwrap the item array
				transformResponse: appendTransform($http.defaults.transformResponse, function(data, headers, status) {
					return arrayify(data, headers, status, 'searchProperty');
				})
			},
			'queryPropertyValues': {
				url: BASE_REST_URL + '/nodes/properties/:id',
				method: 'GET',
				isArray: true,
				// Append a transformation that will unwrap the item array
				transformResponse: appendTransform($http.defaults.transformResponse, function(data, headers, status) {
					return arrayify(data, headers, status, 'value');
				})
			}
		}
	);
})

// OnmsNotification REST $resource
.factory('notificationFactory', /* @ngInject */ function($resource, $log, $http, $location) {
	return $resource(BASE_REST_URL + '/notifications/:id', { id: '@id' },
		{
			'query': { 
				method: 'GET',
				isArray: true,
				// Append a transformation that will unwrap the item array
				transformResponse: appendTransform($http.defaults.transformResponse, function(data, headers, status) {
					return arrayify(data, headers, status, 'notification');
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
					return arrayify(data, headers, status, 'searchProperty');
				})
			},
			'queryPropertyValues': {
				url: BASE_REST_URL + '/notifications/properties/:id',
				method: 'GET',
				isArray: true,
				// Append a transformation that will unwrap the item array
				transformResponse: appendTransform($http.defaults.transformResponse, function(data, headers, status) {
					return arrayify(data, headers, status, 'value');
				})
			}
		}
	);
})

// OnmsOutage REST $resource
.factory('outageFactory', /* @ngInject */ function($resource, $log, $http, $location) {
	return $resource(BASE_REST_URL + '/outages/:id', { id: '@id' },
		{
			'query': { 
				method: 'GET',
				isArray: true,
				// Append a transformation that will unwrap the item array
				transformResponse: appendTransform($http.defaults.transformResponse, function(data, headers, status) {
					return arrayify(data, headers, status, 'outage');
				})
			},
			'update': { 
				method: 'PUT'
			},
			'queryProperties': {
				url: BASE_REST_URL + '/outages/properties',
				method: 'GET',
				isArray: true,
				// Append a transformation that will unwrap the item array
				transformResponse: appendTransform($http.defaults.transformResponse, function(data, headers, status) {
					return arrayify(data, headers, status, 'searchProperty');
				})
			},
			'queryPropertyValues': {
				url: BASE_REST_URL + '/outages/properties/:id',
				method: 'GET',
				isArray: true,
				// Append a transformation that will unwrap the item array
				transformResponse: appendTransform($http.defaults.transformResponse, function(data, headers, status) {
					return arrayify(data, headers, status, 'value');
				})
			}
		}
	);
});
