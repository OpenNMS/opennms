(function() {
	'use strict';

	angular.module('opennms', [
		'ionic',
		'opennms.controllers.desktop.app'
	])

	/*
	.run(function($ionicPlatform) {
		$ionicPlatform.ready(function() {
			if(window.StatusBar) {
				window.StatusBar.styleDefault();
			}
		});
	})
	*/

	.config(function($stateProvider, $urlRouterProvider) {
		$stateProvider

			.state('app', {
				url: '/app',
				abstract: true,
				templateUrl: 'templates/desktop/main.html',
				controller: 'AppCtrl'
			})

			.state('app.dashboard', {
				url: '/dashboard',
				views: {
					'mainContent': {
						templateUrl: 'templates/desktop/dashboard.html',
						controller: 'DashboardCtrl'
					}
				}
			})

			;
		/*
			.state('app.search', {
				url: '/search',
				views: {
					'menuContent' :{
						templateUrl: 'templates/search.html'
					}
				}
			})

			.state('app.browse', {
				url: '/browse',
				views: {
					'menuContent' :{
						templateUrl: 'templates/browse.html'
					}
				}
			})
			.state('app.playlists', {
				url: '/playlists',
				views: {
					'menuContent' :{
						templateUrl: 'templates/playlists.html',
						controller: 'PlaylistsCtrl'
					}
				}
			})

			.state('app.single', {
				url: '/playlists/:playlistId',
				views: {
					'menuContent' :{
						templateUrl: 'templates/playlist.html',
						controller: 'PlaylistCtrl'
					}
				}
			});
	*/
		// if none of the above states are matched, use this as the fallback
		$urlRouterProvider.otherwise('/app/dashboard');
	});
}());
