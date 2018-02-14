'use strict';

const MODULE_NAME = 'onms.elementList.outage';

const angular = require('vendor/angular-js');
const elementList = require('../lib/elementList');
require('../lib/restResources');

const mainTemplate = require('./main.html');

const getSearchProperty = (searchProperties, id) => {
	for (let i = 0; i < searchProperties.length; i++) {
		if (searchProperties[i].id === id) {
			return searchProperties[i];
		}
	}
	return {};
};

// $filters that can be used to create human-readable versions of filter values
angular.module('outageListFilters', [ 'onmsListFilters' ])
.directive('onmsOutageList', () => {
	return {
		restrict: 'E',
		transclude: true,
		templateUrl: mainTemplate
	};
})
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
				return 'null';
			}
			// Return the date in our preferred format
			return $filter('date')(input, 'MMM d, yyyy h:mm:ss a');
		default:
			break;
		}

		if (input === '\u0000') {
			return 'null';
		}

		if (property.values && property.values[input]) {
			return property.values[input];
		}

		return input;
	}
});

// Outage list module
angular.module(MODULE_NAME, [ 'onms.restResources', 'onms.elementList', 'outageListFilters', 'ui.bootstrap', 'ngSanitize' ])

/**
 * Outage list controller
 */
.controller('OutageListCtrl', ['$scope', '$http', '$location', '$window', '$log', '$filter', '$q', 'outageFactory', function($scope, $http, $location, $window, $log, $filter, $q, outageFactory) {
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

	$scope.searchProperties = [];
	$scope.searchPropertiesLoaded = false;

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
			alert('Acknowledgement failed.')
		})
	}


	// Set the default sort and set it on $scope.$parent.query
	$scope.$parent.defaults.orderBy = 'id';
	$scope.$parent.query.orderBy = 'id';
	$scope.clauseValues = [];

	// Reload all resources via REST
	$scope.$parent.refresh = function() {
		// Reset the list of selected notifications
		$scope.selectedOutages = [];

		// Fetch all of the items
		outageFactory.query(
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
				$scope.$parent.query.offset = elementList.normalizeOffset(contentRange.start, $scope.$parent.query.maxOffset, $scope.$parent.query.limit);
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
			}
		);
	};

	// Save an item by using $resource.$update
	$scope.$parent.update = function(item) {
		var saveMe = outageFactory.get({id: item.id}, function() {

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
			loading.notify('Loading search properties');
		}, 200);

		var retval = outageFactory.queryPropertyValues({ id: id, q: query }, function(value, headers) {
			return value;
		}).$promise;

		retval.then(
			function() {
				clearTimeout(timeout);
				loading.resolve('Loaded search properties');
			},
			function() {
				clearTimeout(timeout);
				loading.resolve('Failed to load search properties');
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

	outageFactory.queryProperties(
		{}, 
		function(value, headers) {
			$scope.searchProperties = value;
			$scope.searchPropertiesLoaded = true;
		},
		function(response) {
			switch(response.status) {
			case 404:
				return input;
			case 401:
			case 403:
				// Handle session timeout by reloading page completely
				$window.location.href = $location.absUrl();
				break;
			default:
				// TODO: Handle 500 Server Error?
				break;
			}
			return undefined;
		}
	);

	// Refresh the item list
	$scope.$parent.refresh();

	$log.debug('OutageListCtrl initialized');
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
