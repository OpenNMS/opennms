(function() {
	'use strict';

	var MODULE_NAME = 'onmsList.outage';

	// $filters that can be used to create human-readable versions of filter values
	angular.module('outageListFilters', [ 'onmsListFilters' ])
	.filter('property', function() {
		return function(input) {
			// TODO: Update these values
			switch (input) {
			case 'id':
				return 'ID';
			case 'node.foreignSource':
				return 'Foreign Source';
			case 'node.id':
				return 'Node ID';
			case 'node.label':
				return 'Node Label';
			case 'ipInterface.ipAddress':
				return 'IP Address';
			case 'serviceType.name':
				return 'Service';
			case 'ifLostService':
				return 'Lost Service Time';
			case 'ifRegainedService':
				return 'Regained Service Time';
			}
			// If no match, return the input
			return input;
		}
	})
	.filter('value', function($filter) {
		return function(input, property) {
			switch (property) {
			case 'ifLostService':
			case 'ifRegainedService':
				// Return the date in our preferred format
				return $filter('date')(input, 'MMM d, yyyy h:mm:ss a');
			}
			if (input === '\u0000') {
				return "null";
			}
			return input;
		}
	});

	// Outage list module
	angular.module(MODULE_NAME, [ 'ngResource', 'onmsList', 'outageListFilters' ])

	/**
	 * OnmsOutage REST $resource
	 */
	.factory('Outages', function($resource, $log, $http, $location) {
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

	/**
	 * Outage list controller
	 */
	.controller('OutageListCtrl', ['$scope', '$http', '$location', '$window', '$log', '$filter', 'Outages', function($scope, $http, $location, $window, $log, $filter, Outages) {
		$log.debug('OutageListCtrl initializing...');

		/**
		 * Search clause that represents current Outages
		 */
		$scope.currentClause = {
			property: 'ifRegainedService',
			operator: 'EQ',
			value: $filter('date')(0, ISO_8601_DATE_FORMAT) // null
		};

		/**
		 * Search clause that represents resolved Outages
		 */
		$scope.resolvedClause = {
			property: 'ifRegainedService',
			operator: 'NE',
			value: $filter('date')(0, ISO_8601_DATE_FORMAT) // null
		};

		/**
		 * Array that will hold the currently selected outage IDs.
		 */
		$scope.selectedOutages = [];

		/**
		 * Toggle the selected state for one outage ID.
		 */
		$scope.toggleOutageSelection = function(id) {
			var index = $scope.selectedOutages.indexOf(id);

			if (index >= 0) {
				$scope.selectedOutages.splice(index, 1);
			} else {
				$scope.selectedOutages.push(id);
			}
		}

		/**
		 * Select all of the items in the current view.
		 */
		$scope.selectAllOutages = function() {
			var newSelection = [];
			for (var i = 0; i < $scope.$parent.items.length; i++) {
				newSelection.push($scope.$parent.items[i].id);
			}
			$scope.selectedOutages = newSelection;
		}

		/**
		 * Acknowledge the selected outages as the current user.
		 */
		$scope.acknowledgeSelectedOutages = function() {
			$http({
				method: 'POST',
				url: 'notification/acknowledge',
				params: {
					notices: $scope.selectedOutages
				}
			}).then(function success(response) {
				$scope.$parent.refresh();
			}, function error(response) {
				alert("Acknowledgement failed.")
			})
		}


		// Set the default sort and set it on $scope.$parent.query
		$scope.$parent.defaults.orderBy = 'id';
		$scope.$parent.query.orderBy = 'id';

		// Reload all resources via REST
		$scope.$parent.refresh = function() {
			// Reset the list of selected notifications
			$scope.selectedOutages = [];

			// Fetch all of the items
			Outages.query(
				{
					_s: $scope.$parent.query.searchParam, // FIQL search
					limit: $scope.$parent.query.limit,
					offset: $scope.$parent.query.offset,
					orderBy: $scope.$parent.query.orderBy,
					order: $scope.$parent.query.order
				}, 
				function(value, headers) {
					$scope.$parent.items = value;

					var contentRange = parseContentRange(headers('Content-Range'));
					$scope.$parent.query.lastOffset = contentRange.end;
					// Subtract 1 from the value since offsets are zero-based
					$scope.$parent.query.maxOffset = contentRange.total - 1;
					$scope.$parent.setOffset(contentRange.start);
				},
				function(response) {
					switch(response.status) {
					case 404:
						// If we didn't find any elements, then clear the list
						$scope.$parent.items = [];
						$scope.$parent.query.lastOffset = 0;
						$scope.$parent.query.maxOffset = -1;
						$scope.$parent.setOffset(0);
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
		$scope.$parent.update = function(item) {
			var saveMe = Outages.get({id: item.id}, function() {

				// TODO: Update updateable fields

				saveMe.$update({}, function() {
					// If there's a search in effect, refresh the view
					if ($scope.$parent.query.searchParam !== '') {
						$scope.$parent.refresh();
					}
				});
			}, function(response) {
				$log.debug(response);
			});

		};

		// Refresh the item list;
		$scope.$parent.refresh();

		$log.debug('OutageListCtrl initialized');
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
