'use strict';

var opennms = require('../../util/opennms')(casper),
	utils = require('utils');

casper.test.begin('Notifications Page', {
	setUp: function() {
		opennms.initialize();
		opennms.login();
		casper.thenOpen(opennms.root() + '/notification/index.jsp');
	},

	test: function(test) {
		casper.then(function() {
			test.assertSelectorHasText('h3.panel-title', 'Notification queries', 'The Notifications page should have a "Notification queries" panel header');
			test.assertSelectorHasText('h3.panel-title', 'Outstanding and Acknowledged Notices', 'The Notifications page should have an "Outstanding and Acknowledged Notices" panel header');
			test.assertSelectorHasText('h3.panel-title', 'Notification Escalation', 'The Notifications page should have a "Notification Escalation" panel header');

			test.assertSelectorHasText('form button', 'Check notices', 'The Notifications page should have a "Check notices" button');
			test.assertSelectorHasText('form button', 'Get details', 'The Notifications page should have a "Get details" button');

			test.assertExists('div.panel-body ul > li > a', 'Your outstanding notices', 'The Notifications page should have a "Your outstanding notices" link');
			test.assertExists('div.panel-body ul > li > a', 'All outstanding notices', 'The Notifications page should have an "All outstanding notices" link');
			test.assertExists('div.panel-body ul > li > a', 'All acknowledged notices', 'The Notifications page should havean "All acknowledged notices" link');
		});

		casper.then(function() {
			casper.clickLabel('Your outstanding notices');
		});
		casper.then(function() {
			test.assertSelectorHasText('div#content > p > strong', 'outstanding', 'The "Your outstanding notices" page should have "outstanding" bolded');
			test.assertSelectorHasText('div#content > p > a', '[Show acknowledged]', 'The "Your outstanding notices" page should have a "Show acknowledged" link');
			test.assertSelectorHasText('th a', 'Respond Time', 'The "Your outstanding notices" page should have a "Respond Time" header link');
			test.assertSelectorHasText('span[class="label label-default"]', 'admin was notified [-]', 'The "Your outstanding notices" page should have an "admin was notified" marker');
			test.assertSelectorHasText('div#content > p > a', '[Remove all]', 'The "Your outstanding notices" page should have a "Remove all" link');
		});
		casper.back();

		casper.then(function() {
			casper.clickLabel('All outstanding notices');
		});
		casper.then(function() {
			test.assertSelectorHasText('div#content > p > strong', 'outstanding', 'The "All outstanding notices" page should have "outstanding" bolded');
			test.assertSelectorHasText('div#content > p > a', '[Show acknowledged]', 'The "All outstanding notices" page should have a "Show acknowledged" link');
			test.assertSelectorHasText('th a', 'Respond Time', 'The "All outstanding notices" page should have a "Respond Time" header link');
			test.assertSelectorDoesntHaveText('span[class="label label-default"]', 'admin was notified [-]', 'The "All outstanding notices" page should have an "admin was notified" marker');
		});
		casper.back();

		casper.then(function() {
			casper.clickLabel('All acknowledged notices');
		});
		casper.then(function() {
			test.assertSelectorHasText('div#content > p > strong', 'acknowledged', 'The "All acknowledged notices" page should have "acknowledged" bolded');
			test.assertSelectorHasText('div#content > p > a', '[Show outstanding]', 'The "All acknowledged notices" page should have a "Show outstanding" link');
			test.assertSelectorHasText('th a', 'Respond Time', 'The "All acknowledged notices" page should have a "Respond Time" headr link');
			test.assertSelectorDoesntHaveText('span[class="label label-default"]', 'admin was notified [-]', 'The "All acknowledged notices" page should have an "admin was notified" marker');
		});

		opennms.finished(test);
	}
});
