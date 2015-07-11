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
					transformResponse: appendTransform($http.defaults.transformResponse, function(data, headers) {
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
		$scope.limit = '';
		$scope.offset = '';
		$scope.orderBy = '';
		$scope.order = '';

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
				},
				function(response) {
					// If we didn't find any elements, then clear the list
					if (response.status == 404) {
						$scope.minions = new Array();
					}
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
			// TODO: Add validation
			$scope.searchClauses.push(angular.copy(clause));
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
