'use strict';

var opennms = require('../../util/opennms')(casper),
	utils = require('utils');

casper.test.begin('Categories', {
	setUp: function() {
		opennms.initialize();
		opennms.login();
	},

	test: function(test) {
		casper.then(function() {
			casper.clickLabel('Network Interfaces');
		});
		casper.then(function() {

		});
		casper.then(function() {
			test.assertUrlMatches(/rtc\/category\.jsp/, 'The "Network Interfaces" link should go to the category.jsp URL');

			test.assertSelectorHasText('table.severity th', 'Nodes', 'The "Network Interfaces" page should have a "Nodes" header');
			test.assertSelectorHasText('table.severity th', 'Outages', 'The "Network Interfaces" page should have an "Outages" header');
			test.assertSelectorHasText('table.severity th', '24hr Availability', 'The "Network Interfaces" page should have a "24hr Availability" header');
		});

		opennms.finished(test);
	}
});
