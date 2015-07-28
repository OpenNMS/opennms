(function() {
	'use strict';

	var MODULE_NAME = 'onmsList.minion';

	// $filters that can be used to create human-readable versions of filter values
	angular.module('minionListFilters', [ 'onmsListFilters' ])
	.filter('property', function() {
		return function(input) {
			switch (input) {
			case 'id':
				return 'ID';
			case 'label':
				return 'Label';
			case 'location':
				return 'Location';
			case 'type':
				return 'Type';
			case 'status':
				return 'Status';
			case 'lastUpdated':
				return 'Last updated';
			}
			// If no match, return the input
			return input;
		}
	})
	.filter('value', function($filter) {
		return function(input, property) {
			switch (property) {
			case 'lastUpdated':
				// Return the date in our preferred format
				return $filter('date')(input, 'MMM d, yyyy h:mm:ss a');
			}
			return input;
		}
	});

	// Minion list module
	angular.module(MODULE_NAME, [ 'ngResource', 'onmsList', 'minionListFilters' ])

	/**
	 * OnmsMinion REST $resource
	 */
	.factory('Minions', function($resource, $log, $http, $location) {
		return $resource(BASE_REST_URL + '/minions/:id', { id: '@id' },
			{
				'query': { 
					method: 'GET',
					isArray: true,
					// Append a transformation that will unwrap the minion array
					transformResponse: appendTransform($http.defaults.transformResponse, function(data, headers, status) {
						// TODO: Figure out how to handle session timeouts that redirect to 
						// the login screen
						/*
						if (status === 302) {
							$window.location.href = $location.absUrl();
							return [];
						}
						*/
						// Always return the data as an array
						return angular.isArray(data.minion) ? data.minion : [ data.minion ];
					})
				},
				'update': { 
					method: 'PUT'
				}
			}
		);
	})

	/**
	 * Minion list controller
	 */
	.controller('MinionListCtrl', ['$scope', '$location', '$window', '$log', '$filter', 'Minions', function($scope, $location, $window, $log, $filter, Minions) {
		$log.debug('MinionListCtrl initializing...');

		$scope.$parent.DEFAULT_ORDERBY = 'label';

		// Reload all resources via REST
		$scope.$parent.refresh = function() {
			// Fetch all of the items
			Minions.query(
				{
					_s: $scope.query.searchParam, // FIQL search
					limit: $scope.query.limit,
					offset: $scope.query.offset,
					orderBy: $scope.query.orderBy,
					order: $scope.query.order
				}, 
				function(value, headers) {
					$scope.items = value;

					var contentRange = parseContentRange(headers('Content-Range'));
					$scope.query.lastOffset = contentRange.end;
					// Subtract 1 from the value since offsets are zero-based
					$scope.query.maxOffset = contentRange.total - 1;
					$scope.setOffset(contentRange.start);
				},
				function(response) {
					switch(response.status) {
					case 404:
						// If we didn't find any elements, then clear the list
						$scope.items = [];
						$scope.query.lastOffset = 0;
						$scope.query.maxOffset = -1;
						$scope.setOffset(0);
						break;
					case 401:
					case 403:
						// Handle session timeout by reloading page completely
						$window.location.href = $location.absUrl();
						break;
					}
					// TODO: Handle 500 Server Error by executing an undo callback?
				}
			);
		};

		// Save an item by using $resource.$update
		$scope.$parent.update = function(minion) {
			var saveMe = Minions.get({id: minion.id}, function() {
				saveMe.label = minion.label;
				saveMe.location = minion.location;

				// TODO
				//saveMe.status = minion.status;
				// TODO
				//saveMe.properties = minion.properties;

				// Read-only fields
				// saveMe.type = minion.type;
				// saveMe.date = minion.date;

				saveMe.$update({}, function() {
					// Reset the editing flags
					$scope.enableEditLabel = false;
					$scope.enableEditLocation = false;
					$scope.enableEditProperties = false;

					// If there's a search in effect, refresh the view
					if ($scope.query.searchParam !== '') {
						$scope.refresh();
					}
				});
			}, function(response) {
				$log.debug(response);
			});

		};

		// Refresh the item list;
		$scope.$parent.refresh();

		$log.debug('MinionListCtrl initialized');
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
