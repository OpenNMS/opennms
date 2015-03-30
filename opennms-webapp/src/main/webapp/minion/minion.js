(function() {
	'use strict';

	String.prototype.endsWith = function(suffix) {
		return this.indexOf(suffix, this.length - suffix.length) !== -1;
	};

	angular.module('minion', [
		'ng'
	])

	.controller('MinionListCtrl', ['$scope', '$http', '$log', function($scope, $http, $log) {
		$log.debug('MinionListCtrl Initialized.');
		
		$http.get('rest/minions')
		.success(function(data, status) {
			if (angular.isArray(data.minion)) {
				$scope.minions = data.minion;
			} else {
				$scope.minions = [ data.minion ];
			}
		}).error(function(data, status) {
			$log.error('Failed to get minion data:',data,status);
		});

		/*
		$scope.minions = [
			{ uuid: '0f45fda0-b05e-4ae6-8dc9-981d5307aac1', location: 'Pittsboro' },
			{ uuid: '4125bb00-7084-47a2-a7f3-95309fd578dc', location: 'San Francisco' }
		];
		*/
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