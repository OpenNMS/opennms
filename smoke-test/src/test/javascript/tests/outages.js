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
			test.assertSelectorHasText('h3.panel-title', 'Outage Menu', 'The Outage page should have an "Outage Menu" panel header');
			test.assertSelectorHasText('h3.panel-title', 'Outages and Service Level Availability', 'The Outage page should have an "Outages and Service Level Availability" panel header');
			test.assertExists('form[name="outageIdForm"] input#input_id', 'Outage ID form should exist');
		});

		casper.then(function() {
			casper.clickLabel('Current outages');
		});
		casper.then(function() {
			test.assertSelectorHasText('button.active', 'Current', '"Current" chooser should be selected.');
			test.assertSelectorDoesntHaveText('button.active', 'Resolved', '"Resolved" chooser should not be selected.');
			test.assertSelectorDoesntHaveText('button.active', 'Both Current & Resolved', '"Both Current & Resolved" chooser should not be selected.');
			casper.back();
		});

		casper.then(function() {
			casper.clickLabel('All outages');
		});
		casper.then(function() {
			test.assertSelectorHasText('button.active', 'Both Current & Resolved', '"Both Current & Resolved" chooser should be selected.');
			casper.back();
		});

		casper.then(function() {
			casper.waitForAlert(function(response) {
				casper.test.assertEquals(response.data, 'Please enter a valid outage ID.', 'Alert box should show when clicking "Get details" without an outage ID.');
				return true;
			}, function() {
				casper.test.fail('No alert box showed when clicking "Get details".');
			});
			casper.clickLabel('Get details');
		});

		opennms.finished(test);
	}
});
