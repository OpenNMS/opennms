'use strict';

var opennms = require('../../util/opennms')(casper),
	utils = require('utils');

var foreignSource = 'add-node-to-requisition-test';
var foreignId = 'localNode';
var nodeLabel = 'localNode';
var ipAddress = '127.0.0.1';

casper.test.begin('Add Node to Requisition', {
	setUp: function() {
		opennms.initialize();
		opennms.login();
		opennms.ensureNoRequisitions();
	},

	test: function(test) {
		casper.thenOpen(opennms.root() + '/admin/index.jsp');
		casper.then(function() {
			test.assertSelectorHasText('a[href="admin/ng-requisitions/app/index.jsp"]', 'Manage Provisioning Requisitions', 'Manage Provisioning Requisitions link exists.');
		});
		casper.then(function() {
			casper.clickLabel('Manage Provisioning Requisitions');
		});

		// Add a requisition
		casper.waitForSelector('button#add-requisition', function() {
			casper.click('button#add-requisition');
		});
		casper.waitForSelector('form.bootbox-form > input', function() {
			casper.fillSelectors('form.bootbox-form', {
				'input': foreignSource
			}, true);
		});

		// Edit the foreign source
		casper.waitForSelector('button[uib-tooltip="Edit detectors and policies of the '+foreignSource+' Requisition"]');
		casper.then(function() {
			casper.click('button[uib-tooltip="Edit detectors and policies of the '+foreignSource+' Requisition"]');
		});
		casper.waitForSelector('input[placeholder="Search/Filter Detectors"]');
		casper.then(function() {
			test.assertSelectorHasText('h4', 'Foreign Source Definition for Requisition add-node-to-requisition-test', 'Foreign source edit header should be found.');
			casper.click('button#add-detector');
		});
		casper.waitForSelector('form[name="detectorForm"]');
		casper.then(function() {
			casper.fillSelectors('form[name="detectorForm"]', {
				'input[ng-model="detector.name"]': 'HTTP-8980'
			}, false);
			casper.sendKeys('input#clazz', 'HTTP\n', {reset:true});
		});
		casper.waitForSelector('.modal-dialog ul.dropdown-menu a[title="HTTP"]', function() {
			casper.click('.modal-dialog ul.dropdown-menu a[title="HTTP"]');
		});
		casper.waitWhileVisible('.modal-dialog ul.dropdown-menu');
		casper.then(function() {
			casper.click('.modal-dialog button.btn.btn-success');
		});
		casper.waitWhileVisible('.modal-dialog');
		casper.then(function() {
			casper.click('#save-foreign-source');
		});
		casper.waitWhileVisible('#save-foreign-source');
		casper.then(function() {
			casper.click('ul[ng-model="detectorsCurrentPage"] .pagination-next');
		});
		casper.waitForText('HTTP-8980');
		casper.then(function() {
			casper.click('#go-back');
		});
		casper.waitForText('(0 nodes)');

		// Add a node to the requisition
		casper.then(function() {
			casper.click('#add-node');
		});
		casper.waitUntilVisible('#nodeLabel');
		casper.then(function() {
			casper.fill('form[name="nodeForm"]', {
				nodeLabel: nodeLabel,
				foreignId: foreignId
			}, false);
		});
		// Add an interface to the node
		casper.then(function() {
			casper.click('#tab-interfaces');
		});
		casper.waitUntilVisible('a[ng-click="addInterface()"]');
		casper.then(function() {
			test.assertExists('a[ng-click="addInterface()"]', 'The "Add Interface" button should exist');
			casper.click('a[ng-click="addInterface()"]');
		});
		casper.waitUntilVisible('form[name="intfForm"]');
		casper.then(function() {
			test.assertExists('form[name="intfForm"]', 'The "Add Interface" form should be visible');
			casper.fill('form[name="intfForm"]', {
				ipAddress: ipAddress
			}, false);
		});
		casper.then(function() {
			casper.clickLabel('Add Service');
		});
		casper.waitUntilVisible('input[name="serviceName"]', function() {
			casper.sendKeys('input[name="serviceName"]', 'HTTP-8980\n', {reset:true});
		});
		casper.then(function() {
			casper.clickLabel('Save');
		});
		casper.waitUntilVisible('#save-node', function() {
			casper.click('#save-node');
		});
		casper.waitWhileVisible('#save-node', function() {
			casper.click('#go-back');
		});
		casper.waitForText(nodeLabel, function() {
			test.assertSelectorHasText('td', nodeLabel, 'There should be a node row entry for the test node label');
			test.assertSelectorHasText('td', foreignId, 'There should be a node row entry for the test foreign ID');
			test.assertSelectorHasText('td', ipAddress + ' (P)', 'There should be a node row entry for the IP address');
		});

		// Synchronize the requisition
		casper.waitWhileSelector('button[disabled][id="synchronize"]');
		casper.thenClick('#synchronize');
		casper.waitForSelector('.modal-dialog button.btn.btn-success', function() {
			test.assertSelectorHasText('.modal-dialog button.btn.btn-success', 'Yes', 'Make sure "Yes" button is visible');
		});
		casper.thenClick('.modal-dialog button.btn.btn-success');
		casper.waitWhileSelector('.modal_dialog');
		casper.waitForText('(1 nodes)', function() {
			test.assertTextExists('(1 nodes)', 'There should be one node on the requisition node page');
		});
		casper.waitWhileSelector('.danger', function() {
			test.assertDoesntExist('.danger', 'There should be no unfinished/red entries on the screen');
		});

		opennms.finished(test);
	}
});
