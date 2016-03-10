'use strict';

var opennms = require('../../util/opennms')(casper),
	utils = require('utils');

casper.test.begin('Remoting Page', {
	setUp: function() {
		opennms.initialize();
		opennms.login();
		casper.thenOpen(opennms.root() + '-remoting/');
	},

	test: function(test) {
		casper.then(function() {
			test.assertSelectorHasText('h2 a', 'Remote Poller', 'The Remoting page should have a "Remote Poller" link');
			test.assertSelectorHasText('h2 a', 'Remote Poller without GUI', 'The Remoting page should have a "Remote Poller without GUI" link');
			test.assertSelectorHasText('h2 a', 'Remote Poller Scan Report', 'The Remoting page should have a "Remote Poller Scan Report" link');
		});

		opennms.finished(test);
	}
});
