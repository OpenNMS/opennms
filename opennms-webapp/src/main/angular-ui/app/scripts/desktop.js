(function() {
	'use strict';

	angular.module('opennms', [
        'opennms.controllers.shared.menu',
		'opennms.controllers.desktop.app',
		'opennms.controllers.desktop.dashboard'
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

    ;
}());
