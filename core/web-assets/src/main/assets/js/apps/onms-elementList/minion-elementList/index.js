'use strict';

const MODULE_NAME = 'onms.elementList.minion';

const angular = require('vendor/angular-js');
const elementList = require('../lib/elementList');
require('../lib/restResources');

const mainTemplate = require('./main.html');

// $filters that can be used to create human-readable versions of filter values
angular.module('minionListFilters', [ 'onmsListFilters' ])
.directive('onmsMinionList', () => {
	return {
		restrict: 'E',
		transclude: true,
		templateUrl: mainTemplate
	};
})
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
		default:
			// If no match, return the input
			return input;
		}
	}
})
.filter('value', function($filter) {
	return function(input, property) {
		switch (property) {
		case 'lastUpdated':
			// Return the date in our preferred format
			return $filter('date')(input, 'MMM d, yyyy h:mm:ss a');
		default:
			return input;
		}
	}
});

// Minion list module
angular.module(MODULE_NAME, [ 'onms.restResources', 'onms.elementList', 'minionListFilters' ])

/**
 * Minion list controller
 */
.controller('MinionListCtrl', ['$scope', '$location', '$window', '$log', '$filter', 'minionFactory', function($scope, $location, $window, $log, $filter, minionFactory) {
	$log.debug('MinionListCtrl initializing...');

	// Set the default sort and set it on $scope.$parent.query
	$scope.$parent.defaults.orderBy = 'label';
	$scope.$parent.query.orderBy = 'label';

	// Reload all resources via REST
	$scope.$parent.refresh = function() {
		// Fetch all of the items
		minionFactory.query(
			{
				_s: $scope.$parent.query.searchParam, // FIQL search
				limit: $scope.$parent.query.limit,
				offset: $scope.$parent.query.offset,
				orderBy: $scope.$parent.query.orderBy,
				order: $scope.$parent.query.order
			}, 
			function(value, headers) {
				$scope.$parent.items = value;

				var contentRange = elementList.parseContentRange(headers('Content-Range'));
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
				default:
					// TODO: Handle 500 Server Error by executing an undo callback?
					break;
				}
				return undefined;
			}
		);
	};

	// Save an item by using $resource.$update
	$scope.$parent.update = function(item) {
		var saveMe = minionFactory.get({id: item.id}, function() {
			saveMe.label = item.label;
			saveMe.location = item.location;
			saveMe.properties = item.properties;

			// TODO
			//saveMe.status = item.status;
			// TODO
			//saveMe.properties = item.properties;

			// Read-only fields
			// saveMe.type = item.type;
			// saveMe.date = item.date;

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

	$log.debug('MinionListCtrl initialized');
}])

.run(['$rootScope', '$log', function($rootScope, $log) {
	$log.debug('Finished initializing ' + MODULE_NAME);
}])

;

angular.element(document).ready(function() {
	// eslint-disable-next-line no-console
	console.log('Bootstrapping ' + MODULE_NAME);
	angular.bootstrap(document, [MODULE_NAME]);
});
