(function() {
	'use strict';

	// Base URL of the REST service
	var baseUrl = 'api/v2';

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

	// Minion module
	angular.module('minion', [ 'ngResource' ])

	// Create a minion REST $resource
	.factory('Minions', function($resource, $log, $http) {
		return $resource(baseUrl + '/minions/:id', { id: '@id' },
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
	.controller('MinionListCtrl', ['$scope', '$http', '$log', 'Minions', function($scope, $http, $log, Minions) {
		$log.debug('MinionListCtrl initializing...');

		// Blank out the editing flags
		$scope.enableEditLabel = false;
		$scope.enableEditLocation = false;
		$scope.enableEditProperties = false;

		// Blank out all of the query parameters
		$scope.searchParam = '';
		$scope.searchClauses = new Array();

		$scope.limit = 20;
		$scope.newLimit = 20;
		$scope.offset = 0;

		$scope.lastOffset = 0;
		$scope.maxOffset = 0;

		$scope.orderBy = 'label';
		$scope.order = 'asc';

		// Load all minion resources via REST
		Minions.query(
			{
				_s: $scope.searchParam, // FIQL search
				limit: $scope.limit,
				offset: $scope.offset,
				orderBy: $scope.orderBy,
				order: $scope.order
			}, 
			function(value, headers) {
				$scope.minions = value;
				$log.debug($scope.minions);

				var contentRange = parseContentRange(headers("Content-Range"));
				$scope.lastOffset = contentRange.end;
				// Subtract 1 from the value since offsets are zero-based
				$scope.maxOffset = contentRange.total - 1;
				$scope.setOffset(contentRange.start);
			},
			function(response) {
				// If we didn't find any elements, then clear the list
				if (response.status == 404) {
					$scope.minions = new Array();
					$scope.lastOffset = 0;
					$scope.maxOffset = 0;
					$scope.setOffset(0);
				}
				// TODO: Handle 500 Server Error by executing an undo callback?
				// TODO: Handle session timeout by reloading page completely
			}
		);

		// Reload all minion resources via REST
		$scope.refresh = function() {
			/* Fetch all of the Minions */
			Minions.query(
				{
					_s: $scope.searchParam, // FIQL search
					limit: $scope.limit,
					offset: $scope.offset,
					orderBy: $scope.orderBy,
					order: $scope.order
				}, 
				function(value, headers) {
					$scope.minions = value;
					$log.debug($scope.minions);

					var contentRange = parseContentRange(headers("Content-Range"));
					$scope.lastOffset = contentRange.end;
					// Subtract 1 from the value since offsets are zero-based
					$scope.maxOffset = contentRange.total - 1;
					$scope.setOffset(contentRange.start);
				},
				function(response) {
					// If we didn't find any elements, then clear the list
					if (response.status == 404) {
						$scope.minions = new Array();
						$scope.lastOffset = 0;
						$scope.maxOffset = 0;
						$scope.setOffset(0);
					}
					// TODO: Handle 500 Server Error by executing an undo callback?
					// TODO: Handle session timeout by reloading page completely
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
		$scope.editLabel = function(id) {
			$log.debug(id);
			$scope.enableEditLabel = true;
		}

		// Mark location as editable
		$scope.editLocation = function(id) {
			$log.debug(id);
			$scope.enableEditLocation = true;
		}

		// Add the search clause to the list of clauses
		$scope.addSearchClause = function(clause) {
			// Make sure the clause isn't already in the list of search clauses
			for (var i = 0; i < $scope.searchClauses.length; i++) {
				if (
					clause.property === $scope.searchClauses[i].property &&
					clause.operator === $scope.searchClauses[i].operator &&
					clause.value === $scope.searchClauses[i].value
				) {
					return;
				}
			}
			// TODO: Add validation?
			$scope.searchClauses.push(angular.copy(clause));
			$scope.searchParam = toFiql($scope.searchClauses);
			$scope.refresh();
		}

		// Remove a search clause from the list of clauses
		$scope.removeSearchClause = function(clause) {
			// TODO: Add validation?
			$scope.searchClauses.splice($scope.searchClauses.indexOf(clause), 1);
			$scope.searchParam = toFiql($scope.searchClauses);
			$scope.refresh();
		}

		// Clear the current search
		$scope.clearSearch = function() {
			if ($scope.searchClauses.length > 0) {
				$scope.searchClauses = new Array();
				$scope.searchParam = '';
				$scope.refresh();
			}
		}

		// Change the sorting of the table
		$scope.changeOrderBy = function(property) {
			if ($scope.orderBy === property) {
				// TODO: Figure out if we should reset limit/offset here also
				// If the property is already selected then reverse the sorting
				$scope.order = ($scope.order === 'asc' ? 'desc' : 'asc');
			} else {
				// TODO: Figure out if we should reset limit/offset here also
				$scope.orderBy = property;
				$scope.order = 'asc';
			}
			$scope.refresh();
		}

		$scope.setOffset = function(offset) {
			// Offset of the last page
			var lastPageOffset = Math.floor($scope.maxOffset / $scope.limit) * $scope.limit; 

			// Bounds checking
			offset = ((offset < 0) ? 0 : offset);
			offset = ((offset > lastPageOffset) ? lastPageOffset : offset);

			if ($scope.offset !== offset) {
				$scope.offset = offset;
				$scope.refresh();
			}
		}

		$scope.setLimit = function(limit) {
			if (limit < 1) {
				$scope.newLimit = $scope.limit;
				// TODO: Throw a validation error
				return;
			}
			if ($scope.limit !== limit) {
				$scope.limit = limit;
				$scope.offset = Math.floor($scope.offset / limit) * limit;
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
				// saveMe.lastUpdated = minion.lastUpdated;

				saveMe.$update({}, function() {
					// Reset the editing flags
					$scope.enableEditLabel = false;
					$scope.enableEditLocation = false;
					$scope.enableEditProperties = false;

					// If there's a search in effect, refresh the view
					if ($scope.searchParam !== '') {
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
