(function() {
	'use strict';

	var baseUrl = 'api/v2';

	String.prototype.endsWith = function(suffix) {
		return this.indexOf(suffix, this.length - suffix.length) !== -1;
	};

	function appendTransform(defaultTransform, transform) {
		defaultTransform = angular.isArray(defaultTransform) ? defaultTransform : [ defaultTransform ];
		return defaultTransform.concat(transform);
	}

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

	angular.module('minion', [ 'ngResource' ])

	.factory('Minions', function($resource, $log, $http) {
		return $resource(baseUrl + '/minions/:id', { id: '@id' },
			{
				'query': { 
					method: 'GET',
					isArray: true,
					/* Append a transformation that will unwrap the minion array */
					transformResponse: appendTransform($http.defaults.transformResponse, function(data, headers) {
						/* Always return the data as an array */
						return angular.isArray(data.minion) ? data.minion : [ data.minion ];
					})
				},
				'update': { 
					method: 'PUT'
				}
			}
		);
	})

	.controller('MinionListCtrl', ['$scope', '$http', '$log', 'Minions', function($scope, $http, $log, Minions) {
		$log.debug('MinionListCtrl Initialized.');

		$scope.enableEditLabel = false;
		$scope.enableEditLocation = false;
		$scope.enableEditProperties = false;

		$scope.searchParam = '';
		$scope.searchClauses = new Array();
		$scope.limit = '';
		$scope.offset = '';
		$scope.orderBy = '';
		$scope.order = '';

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

		$scope.editLabel = function(id) {
			$log.debug(id);
			$scope.enableEditLabel = true;
		}

		$scope.editLocation = function(id) {
			$log.debug(id);
			$scope.enableEditLocation = true;
		}

		// Add the search clause to the list of clauses
		$scope.addSearchClause = function(clause) {
			// TODO: Add validation
			$log.debug(clause);
			$scope.searchClauses.push(angular.copy(clause));
			$log.debug($scope.searchClauses);
			$scope.searchParam = toFiql($scope.searchClauses);
			$scope.refresh();
		}

		// Clear the current search
		$scope.clearSearch = function() {
			$scope.searchClauses = new Array();
			$scope.searchParam = '';
			$scope.refresh();
		}

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

			//$scope.refresh();

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