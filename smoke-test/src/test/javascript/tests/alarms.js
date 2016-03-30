'use strict';

var opennms = require('../../util/opennms')(casper),
	utils = require('utils');

casper.test.begin('Alarms Page', {
	setUp: function() {
		opennms.initialize();
		opennms.login();
		opennms.createEvent({
			uei: 'uei.opennms.org/internal/importer/importFailed',
			source: 'AlarmsPageTest',
			parms: [
				{
					parmName: 'importResource',
					value: 'foo'
				}
			]
		});
		casper.thenOpen(opennms.root() + '/alarm/index.htm');
	},

	test: function(test) {
		casper.then(function() {
			test.assertElementCount('h3.panel-title', 3, 'The Alarms page should have 3 panel sections');
			test.assertSelectorHasText('h3.panel-title', 'Alarm Queries', 'The Alarms page should have an "Alarm Queries" section');
			test.assertSelectorHasText('h3.panel-title', 'Alarm Filter Favorites', 'The Alarms page should have an "Alarm Filter Favorites" section');
			test.assertSelectorHasText('h3.panel-title', 'Outstanding and acknowledged alarms', 'The Alarms page should have an "Outstanding and acknowledged alarms" section');
			test.assertExists('form input#byalarmid_id');
		});

		casper.then(function() {
			casper.clickLabel('All alarms (summary)');
		});
        var alarmDetailLink;
		casper.then(function() {
			test.assertExists('a[title="Show acknowledged alarm(s)"]', '"Show acknowledged alarm(s)" should be visible in the alarm summary page');
            test.assertTextDoesntExist('First Event Time', 'The alarm summary page should not have the (details) "First Event Time" field');
            alarmDetailLink = casper.getElementAttribute('a[href*="alarm/detail.htm"]', 'href');
            test.assertTruthy(alarmDetailLink.indexOf(opennms.root()) === 0, 'Alarm detail link should start with the OpenNMS URL.');
		});
        casper.then(function() {
            casper.click('a[href="' + alarmDetailLink + '"]');
        });
        casper.then(function() {
            test.assertSelectorHasText('table.severity th:first-of-type', 'Severity', 'There should be a "Severity" field in the alarm summary');
            test.assertSelectorHasText('table.severity th:nth-of-type(2)', 'Node', 'There should be a "Node" field in the alarm summary');
        });
        casper.then(function() {
            casper.back();
        });
        casper.then(function() {
            casper.back();
        });

        casper.then(function() {
            casper.clickLabel('All alarms (detail)');
        });
        casper.then(function() {
            test.assertExists('a[title="Show acknowledged alarm(s)"]', '"Show acknowledged alarm(s)" should be visible in the alarm detail page');
            test.assertTextExists('First Event Time', 'The alarm detail page should have the "First Event Time" field');
        });
        casper.then(function() {
            casper.back();
        });

        casper.then(function() {
            casper.clickLabel('Advanced Search');
        });
        casper.then(function() {
            test.assertExists('input[name="msgsub"]', 'Advanced Search page should have a "msgsub" input field');
            test.assertExists('input[name="iplike"]', 'Advanced Search page should have an "iplike" input field');
        });
        casper.then(function() {
            casper.back();
        });

        casper.thenOpen(opennms.root() + '/alarm/detail.htm?id=999999999', function() {
            test.assertSelectorHasText('h1:first-of-type', 'Alarm ID Not Found', 'The alarm detail page should give a "not found" error on an invalid alarm ID');
        });

		opennms.finished(test);
	}
});
