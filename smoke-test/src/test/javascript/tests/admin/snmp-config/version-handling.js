'use strict';

var opennms = require('../../util/opennms')(casper),
	utils = require('utils');

casper.test.begin('Configure SNMP Community Names by IP Address > Version Handling', {
	setUp: function() {
		opennms.initialize();
		opennms.login();
	},

	test: function(test) {
		casper.thenOpen(opennms.root() + '/admin/snmpConfig');

		casper.then(function() {
			this.fill('form[name="snmpConfigForm"]', {
				version: 'v1'
			}, false);
		});
		casper.then(function() {
			test.assertVisible('div.snmp-v1 h3.panel-title', 'V1/V2C header should be visible.');
			test.assertNotVisible('div.snmp-v3 h3.panel-title', 'V3 header should not be visible.');
		});

		casper.then(function() {
			this.fill('form[name="snmpConfigForm"]', {
				version: 'v3'
			}, false);
		});
		casper.then(function() {
			test.assertVisible('div.snmp-v3 h3.panel-title', 'V3 header should be visible.');
			test.assertNotVisible('div.snmp-v1 h3.panel-title', 'V1 header should not be visible.');
			test.assertNotVisible('div.snmp-v2c h3.panel-title', 'V2 header should not be visible.');
		});

		casper.then(function() {
			this.fill('form[name="snmpConfigForm"]', {
				version: 'v2c'
			}, false);
		});
		casper.then(function() {
			test.assertVisible('div.snmp-v2c h3.panel-title', 'V1/V2C header should be visible.');
			test.assertNotVisible('div.snmp-v3 h3.panel-title', 'V3 header should not be visible.');
		});

		opennms.finished(test);
	}
});
