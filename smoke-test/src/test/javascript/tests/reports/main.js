'use strict';

var opennms = require('../../util/opennms')(casper),
	utils = require('utils');

casper.test.begin('Reports Page', {
	setUp: function() {
		opennms.initialize();
		opennms.login();
		casper.thenOpen(opennms.root() + '/report/index.jsp');
	},

	test: function(test) {
		casper.then(function() {
			test.assertSelectorHasText('h3.panel-title', 'Report', 'There should be a panel header titled "Report"');
			test.assertSelectorHasText('h3.panel-title', 'Descriptions', 'There should be a panel header titled "Descriptions"');
			test.assertExists('form[name="resourceGraphs"] input#resourceName', 'Resource graphs form should exist');
			test.assertExists('form[name="kscReports"] input#kscName', 'KSC reports form should exist');
		});

		casper.then(function() {
			casper.clickLabel('Charts');
		});
		casper.then(function() {
			test.assertExists('img[src="charts?chart-name=sample-bar-chart"]', 'Sample Bar Chart image should exist');
			casper.back();
		});

		casper.then(function() {
			casper.clickLabel('Resource Graphs');
		});
		casper.then(function() {
			test.assertSelectorHasText('h3.panel-title', 'Network Performance Data', 'There should be a panel header titled "Network Performance Data"');
			test.assertTextExists('Choose a resource for a standard performance report.', 'The page should contain the standard performance report chooser');
			test.assertTextExists('Choose a resource for a custom performance report.', 'The page should contain the custom performance report chooser');
			casper.back();
		});

		casper.then(function() {
			casper.clickLabel('KSC Performance, Nodes, Domains');
		});
		casper.then(function() {
			test.assertSelectorHasText('h3.panel-title', 'Customized Reports', 'There should be a panel header titled "Customized Reports" on the KSC page');
			test.assertSelectorHasText('h3.panel-title', 'Descriptions', 'There should be a panel header titled "Descriptions" on the KSC page');
			test.assertSelectorHasText('h3.panel-title', 'Node & Domain Interface Reports', 'There should be a panel header titled "Node & Domain Interface Reports" on the KSC page');
			casper.back();
		});

		casper.then(function() {
			casper.clickLabel('Database Reports');
		});
		casper.then(function() {
			test.assertSelectorHasText('h3.panel-title', 'Database Reports', 'There should be a panel header titled "Database Reports" on the Database Reports page');
			test.assertSelectorHasText('h3.panel-title', 'Descriptions', 'There should be a panel header titled "Descriptions" on the Database Reports page');
			casper.back();
		});

		casper.then(function() {
			casper.clickLabel('Statistics Reports');
		});
		casper.then(function() {
			test.assertSelectorHasText('h3.panel-title', 'Statistics Report List', 'There should be a panel header titled "Statistics Report List" on the Statistics Reports page');
			test.assertTextExists('None found.', 'There should be no reports on the Statistics Reports page.');
			casper.back();
		});

		opennms.finished(test);
	}
});
