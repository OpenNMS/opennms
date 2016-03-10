'use strict';

var opennms = require('../../util/opennms')(casper),
	utils = require('utils');

casper.test.begin('Configure SNMP Community Names by IP Address > Defaults', {
	setUp: function() {
		opennms.initialize();
		opennms.login();
	},

	test: function(test) {
		// v2c
		casper.thenOpen(opennms.root() + '/admin/snmpConfig');

		casper.then(function() {
			casper.fill('form[name="snmpConfigGetForm"]', {
				ipAddress: '1.1.1.1'
			}, false);
		});
		casper.then(function() {
			casper.clickLabel('Look up');
		});
		casper.then(function() {
			test.assertField('ipAddress', '', 'IP Address lookup field should be empty.');
			test.assertField('version', 'v2c', 'Version should be "v2c".');
			test.assertField('firstIPAddress', '1.1.1.1', 'First IP address should be "1.1.1.1".');
		});

		// v3
		casper.thenOpen(opennms.root() + '/admin/snmpConfig');

		// select v3 first so the extra fields open
		casper.then(function() {
			this.fill('form[name="snmpConfigForm"]', {
				version: 'v3'
			}, false);
		});
		casper.then(function() {
			this.fill('form[name="snmpConfigForm"]', {
				firstIPAddress: '1.1.1.2',
				securityLevel: 'authPriv',
				authProtocol: 'SHA',
				authPassPhrase: 'authMe!',
				privPassPhrase: 'privMe!'
			}, false);
		});
		casper.then(function() {
			casper.clickLabel('Save Config');
		});

		// validate v3 lookup
		casper.thenOpen(opennms.root() + '/admin/snmpConfig');
		casper.wait(100);
		casper.then(function() {
			casper.fill('form[name="snmpConfigGetForm"]', {
				ipAddress: '1.1.1.2'
			}, false);
		});
		casper.then(function() {
			casper.clickLabel('Look up');
		});
		casper.then(function() {
			casper.waitUntilVisible('#lookup_ipAddress');
		});
		casper.then(function() {
			test.assertVisible('#authPassPhrase', 'V3 "Auth Passphrase" field should be visible.');
			test.assertVisible('#privPassPhrase', 'V3 "Privacy Passphrase" field should be visible.');
			test.assertVisible('#securityLevel', 'V3 "Security Level" field should be visible.');
			test.assertVisible('#authProtocol', 'V3 "Auth Protocol" field should be visible.');
			test.assertField('ipAddress', '', 'IP address lookup field should be empty.');
			test.assertField('version', 'v3', 'Version should be set to V3.');
			test.assertField('firstIPAddress', '1.1.1.2', 'First IP address field should be filled out.');
			test.assertField('authPassPhrase', 'authMe!', 'Auth Passphrase should be filled out.');
			test.assertField('privPassPhrase', 'privMe!', 'Privacy Passphrase should be filled out.');
			test.assertField('securityLevel', '3', 'Security level should be "authPriv".'); // <option value="3" selected="">authPriv</option>
			test.assertField('authProtocol', 'SHA', 'Auth Protocol should be "SHA".'); // <option value="SHA" selected="">SHA</option>
		});

		opennms.finished(test);
	}
});
