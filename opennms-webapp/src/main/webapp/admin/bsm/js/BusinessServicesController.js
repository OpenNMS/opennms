(function() {
	'use strict';

	var MODULE_NAME = 'businessServices';

	/**
	 * Function used to append an extra transformer to the default $http transforms.
	 */
	function appendTransform(defaultTransform, transform) {
		defaultTransform = angular.isArray(defaultTransform) ? defaultTransform : [ defaultTransform ];
		return defaultTransform.concat(transform);
	}

	angular.module(MODULE_NAME, ['ngResource'])

	/**
	 * BusinessService REST $resource
	 */
	.factory('BusinesServices', function($resource, $log, $http) {
		return $resource('api/v2/business-services/:id', {},
			{
				'query': { 
					method: 'GET',
					isArray: true,
					// Append a transformation that will unwrap the item array
					transformResponse: appendTransform($http.defaults.transformResponse, function(data, headers, status) {
						// Always return the data as an array
						return angular.isArray(data['business-service']) ? data['business-service'] : [ data['business-service'] ];
					})
				},
				'update': { 
					method: 'PUT'
				}
			}
		);
	})

	/**
	 * BusinessServices controller
	 */
	.controller('BusinessServicesController', ['$scope', '$location', '$window', '$log', '$filter', 'BusinesServices', function($scope, $location, $window, $log, $filter, BusinesServices) {
		$log.debug('BusinessServicesController initializing...');

		// Fetch all of the items
		BusinesServices.query(
			{
				limit: 0,
				orderBy:'name',
				order: 'asc'
			},
			function(value, headers) {
				$scope.items = value;
			},
			function(response) {
				switch(response.status) {
					case 404:
						// If we didn't find any elements, then clear the list
						$scope.items = [];
						break;
					case 401:
					case 403:
						// Handle session timeout by reloading page completely
						$window.location.href = $location.absUrl();
						break;
				}
			}
		);

		$log.debug('BusinessServicesController initialized');
	}])

	.run(['$rootScope', '$log', function($rootScope, $log) {
		$log.debug('Finished initializing ' + MODULE_NAME);
	}])

	;

	angular.element(document).ready(function() {
		console.log('Bootstrapping ' + MODULE_NAME);
		angular.bootstrap(document, [MODULE_NAME]);
	});
}());
