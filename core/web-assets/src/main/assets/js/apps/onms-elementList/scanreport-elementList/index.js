'use strict';

const MODULE_NAME = 'onms.elementList.scanreport';

const angular = require('vendor/angular-js');
const elementList = require('../lib/elementList');
require('../lib/restResources');
require('../../onms-date-formatter');

const mainTemplate = require('./main.html');

// $filters that can be used to create human-readable versions of filter values
angular.module('scanReportListFilters', [ 'onmsListFilters', 'onmsDateFormatter' ])
.directive('onmsScanreportList', () => {
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
		case 'timestamp':
			return 'Scan Time';
		case 'location':
			return 'Location';
		case 'applications':
			return 'Application';
		default:
			// If no match, return the input
			return input;
		}
	}
})
.filter('value', function($filter) {
	return function(input, property) {
		switch (property) {
		case 'timestamp':
			// Return the date in our preferred format
			return $filter('onmsDate')(input);
		default:
			return input;
		}
	}
})
.filter('prettyProperty', function($filter) {
	return function(_input) {
		// Strip off remote poller property prefix
		const input = _input.replace('org.opennms.netmgt.poller.remote','')
			// Split on dots or dashes
			.split(/\.|-/);

		var retval = '';
		var first = true;
		for (var i = 0; i < input.length; i++) {
			if (!first) {
				retval += ' ';
			}
			switch(input[i]) {
				// Fully uppercase abbreviations
				case 'id':
				case 'ip':
				case 'os':
					retval += input[i].toUpperCase();
					break;
				// Otherwise, uppercase first letter only
				default:
					retval += input[i].slice(0,1).toUpperCase() + input[i].slice(1);
					break;
			}
			first = false;
		}
		return retval;
	}
});

// ScanReport list module
angular.module(MODULE_NAME, [ 'onms.restResources', 'onms.elementList', 'scanReportListFilters' ])

.directive('scanReportLogs', function($window) {
	return {
		controller: function($log, $scope, scanReportLogFactory) {
			$scope.$watch('report', function(report) {
				var response = scanReportLogFactory.query({
					id: report.id
				}, 
				function() {
					$log.debug('response = ' + angular.toJson(response));
					if (response.text) {
						$scope.logText = response.text;
					} else {
						$log.warn('Unknown response: ' + angular.toJson(response));
					}
				},
				function(response) {
					switch(response.status) {
					case 404:
						// If we didn't find any elements, then clear the list
						$scope.logText = undefined;
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
				});
			});
		},
		scope: {
			report: '='
		},
		templateUrl: 'js/angular-onms-elementList-scanreportlogs.html',
		transclude: true
	};
})

.directive('scanReportDetails', function($window) {
	return {
		controller: function($scope) {
			// Do something?
		},
		// Use an isolated scope
		scope: {
			report: '='
		},
		templateUrl: 'js/angular-onms-elementList-scanreportdetails.html',
		transclude: true
	};
})

/**
 * ScanReport list controller
 */
.controller('ScanReportListCtrl', ['$scope', '$location', '$window', '$log', '$filter', 'scanReportFactory', function($scope, $location, $window, $log, $filter, scanReportFactory) {
	$log.debug('ScanReportListCtrl initializing...');

	$scope.selectedScanReport = {};

	$scope.selectScanReport = function(item) {
		$scope.selectedScanReport = item;
	}

	// Set the default sort and set it on $scope.$parent.query
	$scope.$parent.defaults.orderBy = 'timestamp';
	$scope.$parent.defaults.order = 'desc';
	$scope.$parent.query.orderBy = 'timestamp';
	$scope.$parent.query.order = 'desc';

	// Reload all resources via REST
	$scope.$parent.refresh = function() {
		// Fetch all of the items
		scanReportFactory.query(
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
		var saveMe = scanReportFactory.get({id: item.id}, function() {
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

	$scope.$parent.deleteItem = function(item) {
		var saveMe = scanReportFactory.get({id: item.id}, function() {
			if ($window.confirm('Are you sure you want to remove scan report "' + item.id + '"?')) {
				saveMe.$delete({id: item.id}, function() {
					$scope.refresh();
				});
			}
		}, function(response) {
			if (response.status === 404) {
				// We didn't find the item so it can't be deleted
				// Might as well call refresh()
				$scope.refresh();
			}
		});
	};

	// Refresh the item list;
	$scope.$parent.refresh();

	$log.debug('ScanReportListCtrl initialized');
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
