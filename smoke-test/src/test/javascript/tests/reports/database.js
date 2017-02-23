'use strict';

var opennms = require('../../util/opennms')(casper),
	utils = require('utils');

var reports = {
	'Early-Morning-Report': ['CSV', 'Early morning Report', 1],
	'Response-Time-Summary-Report': ['CSV', 'Response Time Summary', 1],
	'Node-Availability-Report': ['CSV', 'Node Availability Report', 1],
	'Availability-Summary-Report': ['CSV', 'Availability Summary Report', 1],
	'Response-Time-Report': ['CSV', 'Node Response Time', 1],
	'Serial-Interface-Utilization-Summary': ['CSV', 'Interface Utilization Summary', 1],
	'Total-Bytes-Transferred-By-Interface': ['CSV', 'Total Bytes Transferred', 1],
	'Average-Peak-Traffic-Rates': ['CSV', 'Average and Peak Traffic', 1],
	'Interface-Availability-Report': ['CSV', 'Interface Availability Report', 2],
	'Snmp-Interface-Oper-Availability': ['CSV', 'SNMP Interface Availability', 2],
	'AssetMangementMaintExpired': ['CSV', 'Asset Management Report', 2],
	'AssetMangementMaintStrategy': ['CSV', 'Maintenance contracts Report', 2],
	'Event-Analysis': ['CSV', 'Event Analysis', 2]
}

casper.test.begin('Database Batch Reports', {
	setUp: function() {
		opennms.initialize();
		opennms.login();
		casper.thenOpen(opennms.root() + '/report/index.jsp');
	},

	test: function(test) {
		var testReport = function(reportName) {
			casper.thenOpen(opennms.root());
			casper.then(function() {
				casper.clickLabel('Database Reports');
			});
			casper.then(function() {
				casper.clickLabel('List reports');
			});

			var reportFormat = reports[reportName][0];
			var resultPageContents = reports[reportName][1];
			var page = reports[reportName][2];
			if (page > 1) {
				casper.then(function() {
					casper.clickLabel(''+page);
				});
			}

			casper.then(function() {
				casper.click('#online-local_' + reportName);
			});
			casper.then(function() {
				this.fill('form', {
					format: reportFormat
				}, false);
			});
			casper.then(function() {
				casper.click('input#run');
			});
			casper.waitForText(resultPageContents, function() {
				test.assertTextExists(resultPageContents, reportFormat + ' report ' + reportName + ' should contain the text "' + resultPageContents + '"');
			});
		};

		for (var key in reports) {
			if (reports.hasOwnProperty(key)) {
				testReport(key);
			}
		}

		opennms.finished(test);
	}
});
