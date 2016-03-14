'use strict';

var opennms = require('../../util/opennms')(casper),
	utils = require('utils');

casper.test.begin('Event List Page', {
	setUp: function() {
		opennms.initialize();
		opennms.login();
		casper.thenOpen(opennms.root() + '/event/list');
	},

	test: function(test) {
		casper.then(function() {
			test.assertExists('form[name="event_search"]', 'event search form should exist');
			test.assertExists('form[name="acknowledge_form"]', 'acknowledgement form should exist');

			test.assertSelectorHasText('th a', 'ID', 'The Event List page should have an "ID" table header');
			test.assertSelectorHasText('th a', 'Severity', 'The Event List page should have a "Severity" table header');
			test.assertSelectorHasText('th a', 'Time', 'The Event List page should have a "Time" table header');
			test.assertSelectorHasText('th a', 'Node', 'The Event List page should have a "Node" table header');
			test.assertSelectorHasText('th a', 'Interface', 'The Event List page should have an "Interface" table header');
			test.assertSelectorHasText('th a', 'Service', 'The Event List page should have a "Service" table header');
		});

		casper.then(function() {
			casper.click('button[onclick="$(\'#advancedSearchModal\').modal()"]');
		});
		casper.then(function() {
			test.assertExists('input[name="msgsub"]', 'The Event List Page should have a "msgsub" form input field');
			test.assertExists('input[name="iplike"]', 'The Event List Page should have an "iplike" form input field');
			test.assertExists('input[name="nodenamelike"]', 'The Event List Page should have a "nodenamelike" form input field');
			test.assertExists('select[name="severity"]', 'The Event List Page should have a "severity" form select field');
			test.assertExists('input[name="exactuei"]', 'The Event List Page should have an "exactuei" form input field');
			test.assertExists('select[name="service"]', 'The Event List Page should have a "service" form select field');
			test.assertExists('input[name="usebeforetime"]', 'The Event List Page should have a "usebeforetime" form input field');
		});

		casper.thenOpen(opennms.root()+'/event/detail.jsp?id=999999999');
		casper.then(function() {
			test.assertSelectorHasText('div#content > p', 'Event not found in database.', 'The event detail page should give a "not found" error for an invalid event ID.');
		});

		opennms.finished(test);
	}
});
