(function() {
	'use strict';

	angular.module('opennms', [
		'ionic',
		'opennms.controllers.mobile.app'
	])

	.run(function($ionicPlatform) {
		$ionicPlatform.ready(function() {
			if(window.StatusBar) {
				window.StatusBar.styleDefault();
			}
		});
	})

	.config(function($stateProvider, $urlRouterProvider) {
		$stateProvider

			.state('app', {
				url: '/app',
				abstract: true,
				templateUrl: 'templates/mobile/menu.html',
				controller: 'AppCtrl'
			})

			.state('app.search', {
				url: '/search',
				views: {
					'menuContent' :{
						templateUrl: 'templates/mobile/search.html'
					}
				}
			})

			.state('app.browse', {
				url: '/browse',
				views: {
					'menuContent' :{
						templateUrl: 'templates/mobile/browse.html'
					}
				}
			})
			.state('app.playlists', {
				url: '/playlists',
				views: {
					'menuContent' :{
						templateUrl: 'templates/mobile/playlists.html',
						controller: 'PlaylistsCtrl'
					}
				}
			})

			.state('app.single', {
				url: '/playlists/:playlistId',
				views: {
					'menuContent' :{
						templateUrl: 'templates/mobile/playlist.html',
						controller: 'PlaylistCtrl'
					}
				}
			});
		// if none of the above states are matched, use this as the fallback
		$urlRouterProvider.otherwise('/app/playlists');
	});
}());