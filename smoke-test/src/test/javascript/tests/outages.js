'use strict';

var opennms = require('../../util/opennms')(casper),
	utils = require('utils');

casper.test.begin('Outage Page', {
	setUp: function() {
		opennms.initialize();
		opennms.login();
		casper.thenOpen(opennms.root() + '/outage/index.jsp');
	},

	test: function(test) {
		casper.then(function() {
			test.assertSelectorDoesntHaveText('button.active', 'Current', '"Current" chooser should not be selected.');
			test.assertSelectorDoesntHaveText('button.active', 'Resolved', '"Resolved" chooser should not be selected.');
			test.assertSelectorHasText('button.active', 'Both', '"Both" chooser should be selected.');
		});

		casper.then(function() {
			casper.clickLabel('Current');
		});
		casper.then(function() {
			test.assertSelectorHasText('button.active', 'Current', '"Current" chooser should be selected.');
			test.assertSelectorDoesntHaveText('button.active', 'Resolved', '"Resolved" chooser should not be selected.');
			test.assertSelectorDoesntHaveText('button.active', 'Both', '"Both" chooser should not be selected.');
			casper.back();
		});

		casper.then(function() {
			casper.clickLabel('Resolved');
		});
		casper.then(function() {
			test.assertSelectorDoesntHaveText('button.active', 'Current', '"Current" chooser should not be selected.');
			test.assertSelectorHasText('button.active', 'Resolved', '"Resolved" chooser should be selected.');
			test.assertSelectorDoesntHaveText('button.active', 'Both', '"Both" chooser should not be selected.');
			casper.back();
		});

		// TODO: Test filters, ordering

		opennms.finished(test);
	}
});
