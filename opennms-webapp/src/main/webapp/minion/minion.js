(function() {
	'use strict';

	// Base URL of the REST service
	var BASE_REST_URL = 'api/v2';
	var ISO_8601_DATE_FORMAT_WITHOUT_MILLIS = 'yyyy-MM-ddTHH:mm:ssZ';

	String.prototype.endsWith = function(suffix) {
		return this.indexOf(suffix, this.length - suffix.length) !== -1;
	};

	// Function used to append an extra transformer to the default $http transforms
	function appendTransform(defaultTransform, transform) {
		defaultTransform = angular.isArray(defaultTransform) ? defaultTransform : [ defaultTransform ];
		return defaultTransform.concat(transform);
	}

	// Convert from a clause into a FIQL query string
	function toFiql(clauses) {
		var first = true;
		var fiql = '';
		for (var i = 0; i < clauses.length; i++) {
			if (!first) {
				fiql += ';';
			}
			fiql += clauses[i].property;

			switch (clauses[i].operator) {
			case 'EQ':
				fiql += '=='; break;
			case 'NE':
				fiql += '!='; break;
			case 'LT':
				fiql += '=lt='; break;
			case 'LE':
				fiql += '=le='; break;
			case 'GT':
				fiql += '=gt='; break;
			case 'GE':
				fiql += '=ge='; break;
			}

			fiql += clauses[i].value;

			first = false;
		}
		return fiql;
	}

	function parseContentRange(contentRange) {
		// Example: items 0-14/28
		var pattern = /items\s+?(\d+)\s*\-\s*(\d+)\s*\/\s*(\d+)/;
		return {
			start: Number(contentRange.replace(pattern, '$1')),
			end: Number(contentRange.replace(pattern, '$2')),
			total: Number(contentRange.replace(pattern, '$3'))
		};
	}

	// $filters that can be used to create human-readable versions of filter values
	angular.module('minionListFilters', [])
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
	.filter('operator', function() {
		return function(input, value) {
			// See if the string contains a wildcard
			var fuzzy = (typeof value === 'String' && value.indexOf('*') > -1);

			switch (input) {
			case 'EQ':
				return fuzzy ? 'is like' : 'equals';
			case 'NE':
				return fuzzy ? 'is not like' : 'does not equal';
			case 'LT':
				return 'is less than';
			case 'LE':
				return 'is less than or equal';
			case 'GT':
				return 'is greater than';
			case 'GE':
				return 'is greater than or equal';
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


	// Minion module
	angular.module('minion', [ 'ngResource', 'minionListFilters' ])

	.config(function($locationProvider) {
		$locationProvider.html5Mode({
			// Use HTML5 
			enabled: true,
			// Don't rewrite all <a> links on the page
			rewriteLinks: false
		});
	})

	// Create a minion REST $resource
	.factory('Minions', function($resource, $log, $http) {
		return $resource(BASE_REST_URL + '/minions/:id', { id: '@id' },
			{
				'query': { 
					method: 'GET',
					isArray: true,
					// Append a transformation that will unwrap the minion array
					transformResponse: appendTransform($http.defaults.transformResponse, function(data, headers, status) {
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

	// Minion controller
	.controller('MinionListCtrl', ['$scope', '$location', '$window', '$http', '$log', '$filter', 'Minions', function($scope, $location, $window, $http, $log, $filter, Minions) {
		$log.debug('MinionListCtrl initializing...');

		var DEFAULT_LIMIT = 20;
		var DEFAULT_OFFSET = 0;
		var DEFAULT_ORDERBY = 'label';
		var DEFAULT_ORDER = 'asc';

		// Blank out the editing flags
		$scope.enableEditLabel = false;
		$scope.enableEditLocation = false;
		$scope.enableEditProperties = false;

		// Restore any query parameters that you can from the 
		// query string, blank out the rest
		$scope.query = {
			// TODO: Figure out how to parse and restore the FIQL search param
			searchParam: '',
			searchClauses: [],
			limit: typeof $location.search().limit === 'undefined' ? DEFAULT_LIMIT : (Number($location.search().limit) > 0 ? Number($location.search().limit) : DEFAULT_LIMIT),
			newLimit: typeof $location.search().limit === 'undefined' ? DEFAULT_LIMIT : (Number($location.search().limit) > 0 ? Number($location.search().limit) : DEFAULT_LIMIT),
			offset: typeof $location.search().offset === 'undefined' ? DEFAULT_OFFSET : (Number($location.search().offset) > 0 ? Number($location.search().offset) : DEFAULT_OFFSET),

			lastOffset: 0,
			maxOffset: 0,

			// TODO: Validate that the orderBy is in a list of supported properties
			orderBy: typeof $location.search().orderBy === 'undefined' ? DEFAULT_ORDERBY : $location.search().orderBy,
			order: typeof $location.search().order === 'undefined' ? DEFAULT_ORDER : ($location.search().order === 'asc' ? 'asc' : 'desc')
		};

		// Sync the query hash with the $location query string
		$scope.$watch('query', function() {
			var queryParams = angular.copy($scope.query);

			// Delete derived values that we don't need in the query string
			delete queryParams.searchClauses;
			delete queryParams.newLimit;
			delete queryParams.lastOffset;
			delete queryParams.maxOffset;

			// Rename searchParam to _s
			queryParams._s = queryParams.searchParam === '' ? null : queryParams.searchParam;
			delete queryParams.searchParam;

			// Delete any parameters that have default values
			if (queryParams.limit === DEFAULT_LIMIT) { delete queryParams.limit; }
			if (queryParams.offset === DEFAULT_OFFSET) { delete queryParams.offset; }
			if (queryParams.orderBy === DEFAULT_ORDERBY) { delete queryParams.orderBy; }
			if (queryParams.order === DEFAULT_ORDER) { delete queryParams.order; }
			if (queryParams._s === '') { delete queryParams._s; }

			$location.search(queryParams);
		}, 
		true // Use object equality because the reference doesn't change
		);

		// Load all minion resources via REST
		// TODO: Figure out how to eliminate redundancy with $scope.refresh()
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
				$log.debug($scope.items);

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

		// Reload all minion resources via REST
		$scope.refresh = function() {
			// Fetch all of the Minions
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
					$log.debug($scope.items);

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

		// Go out of edit mode
		$scope.unedit = function() {
			$scope.enableEditLabel = false;
			$scope.enableEditLocation = false;
			$scope.refresh();
		}

		// Mark label as editable
		// TODO: Change this so that it uniquely edits one table cell
		$scope.editLabel = function(id) {
			$log.debug(id);
			$scope.enableEditLabel = true;
		}

		// Mark location as editable
		// TODO: Change this so that it uniquely edits one table cell
		$scope.editLocation = function(id) {
			$log.debug(id);
			$scope.enableEditLocation = true;
		}

		// Add the search clause to the list of clauses
		$scope.addSearchClause = function(clause) {
			if(angular.isDate(clause.value)) {
				// Returns a value in yyyy-MM-ddTHH:mm:ss.sssZ format
				// Unfortunately, I don't think CXF will like this because
				// it includes the millisecond portion of the date.
				//clause.value = new Date(clause.value).toISOString();

				// TODO: Add milliseconds to this timestamp once CXF can parse it
				clause.value = $filter('date')(new Date(clause.value), ISO_8601_DATE_FORMAT_WITHOUT_MILLIS);
			}

			// Make sure the clause isn't already in the list of search clauses
			for (var i = 0; i < $scope.query.searchClauses.length; i++) {
				if (
					clause.property === $scope.query.searchClauses[i].property &&
					clause.operator === $scope.query.searchClauses[i].operator &&
					clause.value === $scope.query.searchClauses[i].value
				) {
					return;
				}
			}
			// TODO: Add validation?
			$scope.query.searchClauses.push(angular.copy(clause));
			$scope.query.searchParam = toFiql($scope.query.searchClauses);
			$scope.refresh();
		}

		// Convert an epoch timestamp into String format before adding the search clause
		$scope.addEpochTimestampSearchClause = function(clause) {
			// TODO: Add milliseconds to this timestamp once CXF can parse it
			clause.value = $filter('date')(clause.value, ISO_8601_DATE_FORMAT_WITHOUT_MILLIS);
			$scope.addSearchClause(clause);
		}

		// Remove a search clause from the list of clauses
		$scope.removeSearchClause = function(clause) {
			// TODO: Add validation?
			$scope.query.searchClauses.splice($scope.query.searchClauses.indexOf(clause), 1);
			$scope.query.searchParam = toFiql($scope.query.searchClauses);
			$scope.refresh();
		}

		// Clear the current search
		$scope.clearSearch = function() {
			if ($scope.query.searchClauses.length > 0) {
				$scope.query.searchClauses = [];
				$scope.query.searchParam = '';
				$scope.refresh();
			}
		}

		// Change the sorting of the table
		$scope.changeOrderBy = function(property) {
			if ($scope.query.orderBy === property) {
				// TODO: Figure out if we should reset limit/offset here also
				// If the property is already selected then reverse the sorting
				$scope.query.order = ($scope.query.order === 'asc' ? 'desc' : 'asc');
			} else {
				// TODO: Figure out if we should reset limit/offset here also
				$scope.query.orderBy = property;
				$scope.query.order = DEFAULT_ORDER;
			}
			$scope.refresh();
		}

		$scope.setOffset = function(offset) {
			// Offset of the last page
			var lastPageOffset;
			if ($scope.query.maxOffset < 0) { 
				offset = 0;
				lastPageOffset = 0; 
			} else {
				lastPageOffset = Math.floor($scope.query.maxOffset / $scope.query.limit) * $scope.query.limit; 
			}

			// Bounds checking
			offset = ((offset < 0) ? 0 : offset);
			offset = ((offset > lastPageOffset) ? lastPageOffset : offset);

			if ($scope.query.offset !== offset) {
				$scope.query.offset = offset;
				$scope.refresh();
			}
		}

		$scope.setLimit = function(limit) {
			if (limit < 1) {
				$scope.query.newLimit = $scope.query.limit;
				// TODO: Throw a validation error
				return;
			}
			if ($scope.query.limit !== limit) {
				$scope.query.limit = limit;
				$scope.query.offset = Math.floor($scope.query.offset / limit) * limit;
				$scope.refresh();
			}
		}

		// Save a minion by using $resource.$update
		$scope.update = function(minion) {
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

			$log.debug('MinionListCtrl initialized.');
		};
	}])

	.run(['$rootScope', '$log', function($rootScope, $log) {
		$log.debug('Finished initializing Minion.');
		$rootScope.base = document.baseURI;
		if (!$rootScope.base.endsWith('/')) {
			$rootScope.base += '/';
		}
		$rootScope.base += 'minion';
	}])

	;

	angular.element(document).ready(function() {
		console.log('Bootstrapping minion UI.');
		angular.bootstrap(document, ['minion']);
	});
}());
