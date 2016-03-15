'use strict';

var opennms = require('../../util/opennms')(casper),
	utils = require('utils');

casper.test.begin('Assets Page', {
	setUp: function() {
		opennms.initialize();
		opennms.login();
		casper.thenOpen(opennms.root() + '/asset/index.jsp');
	},

	test: function(test) {
		casper.then(function() {
			test.assertElementCount('h3.panel-title', 3, 'There should be 3 panel sections on the Assets page');
			test.assertSelectorHasText('h3.panel-title', 'Search Asset Information', 'The Assets page should have a "Search Asset Information" panel');
			test.assertSelectorHasText('h3.panel-title', 'Assets with Asset Numbers', 'The Assets page should have an "Assets with Asset Numbers" panel');
			test.assertSelectorHasText('h3.panel-title', 'Assets Inventory', 'The Assets page should have an "Assets Inventory" panel');
            test.assertExists('a[href="asset/nodelist.jsp?column=_allNonEmpty"]', 'The Assets page should have a link for nodes with assets');
		});

		casper.then(function() {
			casper.clickLabel('All nodes with asset info');
		});
		casper.then(function() {
            test.assertSelectorHasText('h3.panel-title', 'Assets', 'The nodews with assets page should have an "Assets" panel');
		});

		opennms.finished(test);
	}
});
