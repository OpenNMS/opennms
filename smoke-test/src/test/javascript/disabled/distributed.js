'use strict';

var opennms = require('../../util/opennms')(casper),
	utils = require('utils');

casper.test.begin('Distributed Maps', {
	setUp: function() {
		opennms.initialize();
		opennms.login();
		casper.thenOpen(opennms.root() + '/RemotePollerMap/index.jsp');
	},

	test: function(test) {
		casper.then(function() {
			this.page.switchToChildFrame(0);
		});

		casper.then(function() {
			// first 5 checkboxes are checked by default
			for (var i=1; i <= 5; i++) {
				test.assertExists('#gwt-uid-' + i + '[checked]', 'checkbox #' + i + ' should be checked');
			}

			test.assertExists('#gwt-uid-6', 'checkbox #6 should exist');
			test.assertDoesntExist('#gwt-uid-6[checked]', 'checkbox #6 should NOT be checked');
		});

		opennms.finished(test);
	}
});
