'use strict';

var opennms = require('../../util/opennms')(casper),
	utils = require('utils');

casper.test.begin('Support Page', {
	setUp: function() {
		opennms.initialize();
		opennms.login();
		casper.thenOpen(opennms.root() + '/support/index.htm');
	},

	test: function(test) {
		casper.then(function() {
			test.assertElementCount('h3.panel-title', 3, 'The Support page should have 3 panel headers');
			test.assertSelectorHasText('h3.panel-title', 'Commercial Support', 'The Support page should have a "Commercial Support" section');
			test.assertSelectorHasText('h3.panel-title', 'About', 'The Support page should have an "About" section');
			test.assertSelectorHasText('h3.panel-title', 'Other Support Options', 'The Support page should have an "Other Support Options" section');
		});

		casper.then(function() {
			test.assertElementCount('div.panel-body a', 7, 'The Support page should have 7 links');
			test.assertExists('div.panel-body a', 'the OpenNMS.com support page');
			test.assertExists('div.panel-body a', 'About the OpenNMS Web Console');
			test.assertExists('div.panel-body a', 'Release Notes');
			test.assertExists('div.panel-body a', 'Online Documentation');
			test.assertExists('div.panel-body a', 'Generate a System Report');
			test.assertExists('div.panel-body a', 'Open a Bug or Enhancement Request');
			test.assertExists('div.panel-body a', 'Chat with Developers on IRC');
		});

		casper.then(function() {
			casper.clickLabel('About the OpenNMS Web Console');
		});
		casper.then(function() {
			test.assertUrlMatch(/support\/about\.jsp/, 'About Page');
			casper.back();
		});

		casper.then(function() {
			casper.clickLabel('Release Notes');
		});
		casper.then(function() {
			test.assertUrlMatch(/meridian\.opennms\.com/, 'Release Notes on meridian.opennms.com');
			casper.back();
		});

		casper.then(function() {
			casper.clickLabel('Online Documentation');
		});
		casper.then(function() {
			test.assertUrlMatch(/meridian\.opennms\.com/, 'Meridian Docs');
			casper.back();
		});

		casper.then(function() {
			casper.clickLabel('Generate a System Report');
		});
		casper.then(function() {
			test.assertUrlMatch(/support\/systemReportList\.htm/, 'System Report Page');
			casper.back();
		});

		casper.then(function() {
			casper.clickLabel('Open a Bug or Enhancement Request');
		});
		casper.then(function() {
			test.assertUrlMatch(/issues\.opennms\.org/, 'OpenNMS JIRA');
			casper.back();
		});

		opennms.finished(test);
	}
});
