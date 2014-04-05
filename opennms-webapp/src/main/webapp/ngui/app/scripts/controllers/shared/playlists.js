(function() {
	'use strict';

	angular.module('opennms.controllers.shared.playlists', [
	])

	.controller('PlaylistsCtrl', ['$scope', function($scope) {
		$scope.playlists = [
			{ title: 'Reggae', id: 1 },
			{ title: 'Chill', id: 2 },
			{ title: 'Dubstep', id: 3 },
			{ title: 'Indie', id: 4 },
			{ title: 'Rap', id: 5 },
			{ title: 'Cowbell', id: 6 }
		];
	}])

	.controller('PlaylistCtrl', [function() {
	}])

	;
}());