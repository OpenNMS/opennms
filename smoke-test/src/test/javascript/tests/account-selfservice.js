'use strict';

var opennms = require('../../util/opennms')(casper),
	utils = require('utils');

casper.test.begin('User Account Page', {
	setUp: function() {
		opennms.initialize();
		opennms.login();
		casper.thenOpen(opennms.root() + '/account/selfService/index.jsp');
	},

	test: function(test) {
		casper.then(function() {
			test.assertElementCount('h3.panel-title', 2, 'Account page should have 2 panels');
			test.assertSelectorHasText('h3.panel-title', 'User Account Self-Service', 'Account page should have "User Account Self-Service" panel');
			test.assertSelectorHasText('h3.panel-title', 'Account Self-Service Options', 'Account page should have "User Account Self-Service Options" panel');
		});

		casper.then(function() {
			test.assertElementCount('div.panel-body a', 1, 'Account page should have one link in the main body');
			test.assertExists('div.panel-body a', 'Change Password', 'Account page should have a "Change Password" link');
		});

		casper.then(function() {
			casper.click('div.panel-body a[href="javascript:changePassword()"]');
		});
		casper.then(function() {
			test.assertExists('form input#input_oldpass', '"Change Password" page should have a password form');
			casper.fill('form[name="goForm"]', {
				oldpass: '12345',
				pass1: '23456',
				pass2: '34567'
			}, false);
		});
		casper.then(function() {
			casper.waitForAlert(function(response) {
				test.assertEquals(response.data, 'The two new password fields do not match!', 'Expect an alert about the new passwords not matching.');
			}, function timeout() {
				test.fail('Never received non-matching password failure alert.');
			});
			casper.clickLabel('Submit');
		});

		casper.then(function() {
			test.assertDoesntExist('div.form-group.has-error input#input_oldpass', 'There should be no form errors on the page');
			test.assertExists('form[name="goForm"] input[name="pass1"]', 'There should be a password form');
			casper.fill('form[name="goForm"]', {
				oldpass: '12345',
				pass1: '23456',
				pass2: '23456'
			}, false);
		});
		casper.then(function() {
			casper.clickLabel('Submit');
		});
		casper.then(function() {
			test.assertExists('div.form-group.has-error input#input_oldpass', 'Form should give an error about an incorrect old password.');
		});

		opennms.finished(test);
	}
});
