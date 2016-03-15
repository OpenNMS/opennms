'use strict';

var opennms = require('../../util/opennms')(casper),
	utils = require('utils');

var x = require('casper').selectXPath;
var nextButton = 'div#next > span.v-button-wrap > span.v-icon';
var previousButton = 'div#previous > span.v-button-wrap > span.v-icon';

casper.test.begin('JMX Configuration Generator', {
	setUp: function() {
		opennms.initialize();
		opennms.login();
		casper.thenOpen(opennms.root() + '/admin/jmxConfigGenerator.jsp');
		casper.then(function() {
			this.page.switchToChildFrame(0);
		});
	},

	test: function(test) {
		/*
		 * In Vaadin 7 a Navigator was introduced to navigate within Vaadin applications
		 * Somehow normal links cause a vaadin exception. This verifies that no exception
		 * is thrown when clicking the Header links
		 */
		casper.waitForText('1. Service Configuration');
		casper.then(function() {
			casper.clickLabel('1. Service Configuration');
		});
		casper.then(function() {
			// sometimes empty v-errormessage divs are present, so we can't rely on just counting them
			var errorText = casper.fetchText('div.v-errormessage');
			test.assertTruthy(!errorText || errorText.trim().length === 0, 'v-errormessage elements should be empty');
		});

		/*
		 * Verify that navigation works.
		 */
		casper.then(function() {
			casper.sendKeys('input#port', '18980', {reset:true});
			var info = casper.getElementInfo('span#authenticate input[type="checkbox"]');
			test.assertTruthy(!info.attributes.hasOwnProperty('checked'), 'Authentication checkbox should be checked');
			casper.click('span#authenticate label');
		});
		casper.wait(200);
		casper.then(function() {
			var info = casper.getElementInfo('span#authenticate input[type="checkbox"]');
			test.assertTruthy(info.attributes.hasOwnProperty('checked'), 'Authentication checkbox should be checked');
			casper.sendKeys('input#authenticateUser', opennms.options().username, {reset:true});
			casper.sendKeys('input#authenticatePassword', opennms.options().password, {reset:true});
		});
		casper.then(function() {
			casper.click(nextButton);
		});
		casper.waitForText('com.mchange.v2.c3p0');
		casper.then(function() {
			test.assertTextExists('com.mchange.v2.c3p0', 'After navigating to next page, "com.mchange.v2.c3p0" should be in the tree view');
			casper.click(nextButton);
		});
		casper.waitForText('collectd-configuration.xml');
		casper.then(function() {
			test.assertTextExists('collectd-configuration.xml', 'After navigating to the last page, "collectd-configuration.xml" should be visible');
			casper.click(previousButton);
		});
		casper.waitForText('com.mchange.v2.c3p0');
		casper.then(function() {
			test.assertTextExists('com.mchange.v2.c3p0', 'After navigating to the previous page, "com.mchange.v2.c3p0" should be in the tree view');
			casper.click(previousButton);
		});
		casper.waitForText('Skip JVM MBeans');
		casper.then(function() {
			test.assertTextExists('Skip JVM MBeans', 'After navigating to the previous page, "Skip JVM beans" should be visible');
		});

		/*
		 * Verify that selected CompMembers do show up in the generated jmx-datacollection-config.xml snippet.
		 */
		casper.then(function() {
			var info = casper.getElementInfo('span#skipDefaultVM input[type="checkbox"]');
			test.assertTruthy(info.attributes.hasOwnProperty('checked'), 'Skip JVM MBeans should be checked');
			casper.click('span#skipDefaultVM input');
		});
		casper.wait(200);
		casper.then(function() {
			var info = casper.getElementInfo('span#skipDefaultVM input[type="checkbox"]');
			test.assertTruthy(!info.attributes.hasOwnProperty('checked'), 'Skip JVM MBeans should not be checked');
			casper.click(nextButton);
		});
		casper.waitForText('com.mchange.v2.c3p0');
		casper.then(function() {
			opennms.scrollToElementWithText('span', 'PS MarkSweep');
		});
		casper.then(function() {
			this.mouse.rightclick(x('//span[text()=\'PS MarkSweep\']'));
		});
		casper.wait(100);
		casper.then(function() {
			casper.click(x('//td[@role="menuitem"]/div[text()=\'select\']'));
		});
		casper.wait(100);
		opennms.scrollToElementWithText('span', 'PS MarkSweep');
		casper.then(function() {
			casper.click(nextButton);
		});
		casper.waitForText('collectd-configuration.xml');
		casper.then(function() {
			test.assertTextExists('collectd-configuration.xml', 'collectd-configuration.xml tab should exist');
			test.assertTextExists('jmx-datacollection-config.xml', 'jmx-datacollection-config.xml tab should exist');
			casper.clickLabel('jmx-datacollection-config.xml');
		});
		casper.waitForText('JMXMP protocol');
		casper.then(function() {
			var info = casper.fetchText('textarea');
			test.assertEquals(info.match(/\<comp-attrib/g).length, 1, 'There should be 1 <comp-attrib/> tag in jmx-datacollection-config.xml');
			test.assertEquals(info.match(/\<comp-member/g).length, 7, 'There should be 7 <comp-member/> tags in jmx-datacollection-config.xml');
		});

		opennms.finished(test);
	}
});
