'use strict';

var opennms = require('../../util/opennms')(casper),
	utils = require('utils');

casper.test.begin('Node List Page', {
	setUp: function() {
		opennms.initialize();
		opennms.login();
		opennms.ensureNoRequisitions();
		opennms.createOrReplaceRequisition('node-list-test', {
			node: [
				{
					'foreign-id': 'a',
					'node-label': 'a'
				},
				{
					'foreign-id': 'b',
					'node-label': 'b'
				}
			]
		});
		opennms.importRequisition('node-list-test');
		opennms.waitForRequisition('node-list-test', 2);
		casper.thenOpen(opennms.root() + '/element/nodeList.htm');
	},

	test: function(test) {
		casper.then(function() {
			test.assertSelectorHasText('h3.panel-title', 'Nodes', 'The Node List page should have a "Nodes" panel header');
			test.assertExists('i.fa-database', 'database node info toggle should exist');
			test.assertSelectorHasText('div#content > p > a', 'Show interfaces', 'The Node List page should have a "Show Interfaces" link');
		});
		casper.then(function() {
			casper.clickLabel('Show interfaces');
		});
		casper.then(function() {
			test.assertSelectorHasText('div#content > p > a', 'Hide interfaces', 'The "Show interfaces" link should turn to "Hide interfaces" when clicked');
		});

		opennms.finished(test);
	}
});
