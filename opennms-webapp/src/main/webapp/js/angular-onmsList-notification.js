(function() {
	'use strict';

	var MODULE_NAME = 'onmsList.notification';

	// $filters that can be used to create human-readable versions of filter values
	angular.module('notificationListFilters', [ 'onmsListFilters' ])
	.filter('property', function() {
		return function(input) {
			switch (input) {
			case 'notifyId':
				return 'ID';
			case 'event.id':
				return 'Event ID';
			case 'event.eventSeverity':
				return 'Event Severity';
			case 'pageTime':
				return 'Sent Time';
			case 'answeredBy':
				return 'Responder';
			case 'respondTime':
				return 'Response Time';
			case 'node.id':
				return 'Node ID';
			case 'node.label':
				return 'Node Label';
			// TODO: ipAddress doesn't work because it is type InetAddress
			case 'ipAddress':
				return 'IP Address';
			case 'serviceType.name':
				return 'Service';
			}
			// If no match, return the input
			return input;
		}
	})
	.filter('value', function($filter) {
		return function(input, property) {
			switch (property) {
			case 'pageTime':
			case 'respondTime':
				// Return the date in our preferred format
				return $filter('date')(input, 'MMM d, yyyy h:mm:ss a');
			}
			if (input === '\u0000') {
				return "null";
			}
			return input;
		}
	});

	// Notification list module
	angular.module(MODULE_NAME, [ 'onms.restResources', 'onmsList', 'notificationListFilters' ])

	/**
	 * Notification list controller
	 */
	.controller('NotificationListCtrl', ['$scope', '$http', '$location', '$window', '$log', '$filter', 'notificationFactory', function($scope, $http, $location, $window, $log, $filter, notificationFactory) {
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

		// Refresh the item list;
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
