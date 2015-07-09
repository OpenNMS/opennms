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

		/* Fetch all of the Minions */
		Minions.query([], function(value, headers) {
			$scope.minions = value;
			$log.debug($scope.minions);
		});

		$scope.editLabel = function(id) {
			$log.debug(id);
			$scope.enableEditLabel = true;
		}

		$scope.editLocation = function(id) {
			$log.debug(id);
			$scope.enableEditLocation = true;
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

				saveMe.$update();

				// Reset the editing flags
				$scope.enableEditLabel = false;
				$scope.enableEditLocation = false;
				$scope.enableEditProperties = false;
			});
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