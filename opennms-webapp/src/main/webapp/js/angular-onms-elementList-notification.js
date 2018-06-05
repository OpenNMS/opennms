(function() {
	'use strict';

	var MODULE_NAME = 'onms.elementList.notification';

	function getSearchProperty(searchProperties, id) {
		for (var i = 0; i < searchProperties.length; i++) {
			if (searchProperties[i].id === id) {
				return searchProperties[i];
			}
		}
		return {};
	}

	// $filters that can be used to create human-readable versions of filter values
	angular.module('notificationListFilters', [ 'onmsListFilters' ])
	.filter('property', function() {
		return function(input, searchProperties) {
			var property = getSearchProperty(searchProperties, input);
			return property.name || '';
		}
	})
	.filter('value', function($filter) {
		return function(input, searchProperties, propertyId) {
			var property = getSearchProperty(searchProperties, propertyId);
			switch (property.type) {
			case 'TIMESTAMP':
				if (input === '0') {
					return "null";
				} else {
					// Return the date in our preferred format
					return $filter('date')(input, 'MMM d, yyyy h:mm:ss a');
				}
			}

			if (input === '\u0000') {
				return "null";
			}

			if (property.values && property.values[input]) {
				return property.values[input];
			}

			return input;
		}
	});

	// Notification list module
	angular.module(MODULE_NAME, [ 'onms.restResources', 'onms.elementList', 'notificationListFilters', 'ui.bootstrap', 'ngSanitize' ])

	/**
	 * Notification list controller
	 */
	.controller('NotificationListCtrl', ['$scope', '$http', '$location', '$window', '$log', '$filter', '$q', 'notificationFactory', function($scope, $http, $location, $window, $log, $filter, $q, notificationFactory) {
		$log.debug('NotificationListCtrl initializing...');

		/**
		 * Search clause that represents Unacknowledged Notifications
		 */
		$scope.unackClause = {
			property: 'answeredBy',
			operator: 'EQ',
			value: '\u0000' // null
		};

		/**
		 * Search clause that represents Acknowledged Notifications
		 */
		$scope.ackClause = {
			property: 'answeredBy',
			operator: 'NE',
			value: '\u0000' // null
		};

		/**
		 * Array that will hold the currently selected notification IDs.
		 */
		$scope.selectedNotifications = [];

		$scope.searchProperties = [];
		$scope.searchPropertiesLoaded = false;

		/**
		 * Toggle the selected state for one notification ID.
		 */
		$scope.toggleNotificationSelection = function(id) {
			var index = $scope.selectedNotifications.indexOf(id);

			if (index >= 0) {
				$scope.selectedNotifications.splice(index, 1);
			} else {
				$scope.selectedNotifications.push(id);
			}
		}

		/**
		 * Select all of the items in the current view.
		 */
		$scope.selectAllNotifications = function() {
			var newSelection = [];
			for (var i = 0; i < $scope.$parent.items.length; i++) {
				newSelection.push($scope.$parent.items[i].id);
			}
			$scope.selectedNotifications = newSelection;
		}

		/**
		 * Acknowledge the selected notifications as the current user.
		 */
		$scope.acknowledgeSelectedNotifications = function() {
			$http({
				method: 'POST',
				url: 'notification/acknowledge',
				params: {
					notices: $scope.selectedNotifications
				}
			}).then(function success(response) {
				$scope.$parent.refresh();
			}, function error(response) {
				alert("Acknowledgement failed.")
			})
		}


		// Set the default sort and set it on $scope.$parent.query
		$scope.$parent.defaults.orderBy = 'notifyId';
		$scope.$parent.query.orderBy = 'notifyId';
		$scope.clauseValues = [];

		// Reload all resources via REST
		$scope.$parent.refresh = function() {
			// Reset the list of selected notifications
			$scope.selectedNotifications = [];

			// Fetch all of the items
			notificationFactory.query(
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
					$scope.$parent.query.offset = normalizeOffset(contentRange.start, $scope.$parent.query.maxOffset, $scope.$parent.query.limit);
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
			var saveMe = notificationFactory.get({id: item.id}, function() {

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

		$scope.getSeverityClass = function(severity) {
			return 'severity-' + severity.substr(0,1).toUpperCase() + severity.substr(1).toLowerCase();
		}

		$scope.getPrettySeverity = function(severity) {
			return severity.substr(0,1).toUpperCase() + severity.substr(1).toLowerCase();
		}

		$scope.getSearchProperty = function(id) {
			return getSearchProperty($scope.searchProperties, id);
		}

		$scope.getSearchPropertyValues = function(id) {
			var values = getSearchProperty($scope.searchProperties, id).values || {};
			var retval = [];
			var keys = Object.keys(values);
			for (var i = 0; i < keys.length; i++) {
				retval.push({
					id: keys[i],
					name: values[keys[i]]
				});
			}
			return retval;
		}

		$scope.loadingSearchProperties = false;
		$scope.getSearchPropertyMatches = function(id, query) {
			var loading = $q.defer();
			loading.promise.then(
				function() {
					$scope.loadingSearchProperties = false;
				},
				function() {},
				function() {
					$scope.loadingSearchProperties = true;
				}
			);

			var timeout = setTimeout(function() {
				loading.notify("Loading search properties");
			}, 200);

			var retval = notificationFactory.queryPropertyValues({ id: id, q: query }, function(value, headers) {
				return value;
			}).$promise;

			retval.then(
				function() {
					clearTimeout(timeout);
					loading.resolve("Loaded search properties");
				},
				function() {
					clearTimeout(timeout);
					loading.resolve("Failed to load search properties");
				}
			);

			return retval;
		}

		$scope.updateClauseValue = function(id) {
			$scope.clauseValues = $scope.getSearchPropertyValues(id);
			// If the search property has enumerated values...
			if ($scope.clauseValues.length > 0) {
				// Set the selected value to the first value in the list
				$scope.clause.value = $scope.clauseValues[0].id;
			} else {
				// Otherwise erase the value
				$scope.clause.value = '';
			}
		}

		notificationFactory.queryProperties(
			{}, 
			function(value, headers) {
				$scope.searchProperties = value;
				$scope.searchPropertiesLoaded = true;
			},
			function(response) {
				switch(response.status) {
				case 404:
					return input;
					break;
				case 401:
				case 403:
					// Handle session timeout by reloading page completely
					$window.location.href = $location.absUrl();
					break;
				}
				// TODO: Handle 500 Server Error?
			}
		);

		// Refresh the item list
		$scope.$parent.refresh();

		$log.debug('NotificationListCtrl initialized');
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
