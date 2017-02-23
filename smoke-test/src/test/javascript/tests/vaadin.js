'use strict';

var opennms = require('../../util/opennms')(casper),
	utils = require('utils');

var urls = [
	'jmx-config-tool',
	'vaadin-surveillance-views?dashboard=true',
	'vaadin-surveillance-views?dashboard=false',
	'vaadin-surveillance-views-config',
	'wallboard-config'
];

casper.test.begin('Vaadin Applications', {
	setUp: function() {
		opennms.initialize();
	},

	test: function(test) {
		opennms.start();

		var checkRedirect = function(segment) {
			casper.thenOpen(opennms.root() + '/osgi/' + segment, function() {
				test.assertExists('#input_j_username', 'login form should exist when navigating to ' + segment);
			});
		};

		var checkValidPage = function(segment) {
			casper.thenOpen(opennms.root() + '/osgi/' + segment, function() {
				test.assertDoesntExist('#input_j_username', 'login form should not exist when navigating to ' + segment);
				test.assertExists('#__gwt_historyFrame', segment + ' should have a GWT history frame');
				test.assertExists('div.v-app', segment + ' should have a vaadin app div');
			});
		};

		// first confirm that we get redirected to the login page if not logged in
		for (var i=0; i < urls.length; i++) {
			checkRedirect(urls[i]);
		}

		opennms.login();

		// next, make sure we actually get a vaadin app if we are logged in
		for (var u=0; u < urls.length; u++) {
			checkValidPage(urls[u]);
		}

		opennms.finished(test);
	}
});
