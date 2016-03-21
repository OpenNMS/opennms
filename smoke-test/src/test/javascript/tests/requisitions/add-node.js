'use strict';

var opennms = require('../../util/opennms')(casper),
	utils = require('utils'),
	xpath = require('casper').selectXPath;

var foreignSource = 'add-node-to-requisition-test';
var foreignId = 'localNode';
var nodeLabel = 'localNode';
var ipAddress = '127.0.0.1';
var service   = 'HTTP-8980';
var category  = 'Test';

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
		casper.thenClick('button[uib-tooltip="Edit detectors and policies of the '+foreignSource+' Requisition"]');
		casper.waitForSelector('input[placeholder="Search/Filter Detectors"]');
		casper.then(function() {
			test.assertSelectorHasText('h4', 'Foreign Source Definition for Requisition add-node-to-requisition-test', 'Foreign source edit header should be found.');
		});
		casper.wait(10000);

		// Adding a detector
		casper.waitForSelector('#add-detector');
		casper.then(function() {
			test.assertVisible('#add-detector', 'The "Add Detector" button should be visible');
			casper.click('#add-detector');
		})
		casper.waitForSelector('form[name="detectorForm"]');
		casper.then(function() {
			test.assertExists('form[name="detectorForm"]', 'The "Add Detector" form should be visible');
			casper.sendKeys('input#name', service);
			casper.sendKeys('input#clazz', 'HTTP');
		});
		casper.waitForSelector('.modal-dialog ul.dropdown-menu a[title="HTTP"]', function() {
			casper.click('.modal-dialog ul.dropdown-menu a[title="HTTP"]'); // This should happen if the dropdown-menu is present.
		});
		casper.waitWhileVisible('.modal-dialog ul.dropdown-menu');
		casper.thenClick('#add-detector-parameter');
		casper.waitForSelector('input[name="paramName"]');
		casper.then(function() {
			casper.sendKeys('input[name="paramName"]', 'port');
			casper.sendKeys('input[name="paramName"]', casper.page.event.key.Enter);
			casper.sendKeys('input[name="paramValue"]', '8980');
			casper.sendKeys('input[name="paramValue"]', casper.page.event.key.Enter);
		});
		casper.waitForSelector('#save-detector');
		casper.then(function() {
			test.assertVisible('#save-detector', 'The "Save Detector" button should be visible');
			casper.click('#save-detector');
		});
		casper.waitWhileVisible('.modal-dialog');
		casper.then(function() {
			test.assertVisible('ul[ng-model="detectorsCurrentPage"] .pagination-next .ng-binding', 'The "Next Button" on the pagination bar should be visible.');
			casper.click('ul[ng-model="detectorsCurrentPage"] .pagination-next .ng-binding');
		});
		casper.waitForText(service);
		casper.then(function() {
			test.assertSelectorHasText('td', service, 'There should be a row entry for the detector '+service);
		});

		// Add a policy
		casper.waitForSelector('#tab-policies .ng-binding');
		casper.then(function() {
			test.assertExists('#tab-policies .ng-binding', 'The "Tab" policies should be visible');
			casper.click('#tab-policies .ng-binding');
		});
		casper.waitForSelector('#add-policy');
		casper.then(function() {
			test.assertVisible('#add-policy', 'The "Add Policy" button should be visible');
			casper.click('#add-policy');
		})
		casper.waitForSelector('form[name="policyForm"]');
		casper.then(function() {
			test.assertExists('form[name="policyForm"]', 'The "Add Policy" form should be visible');
			casper.sendKeys('input#name', 'No IPs');
			casper.sendKeys('input#clazz', 'Match IP Interface');
			casper.sendKeys('input#clazz', casper.page.event.key.Enter);
			casper.sendKeys(xpath('(//input[@name="paramValue"])[1]'), 'DO_NOT_PERSIST');
			casper.sendKeys(xpath('(//input[@name="paramValue"])[1]'), casper.page.event.key.Enter);
			casper.sendKeys(xpath('(//input[@name="paramValue"])[2]'), 'NO_PARAMETERS');
			casper.sendKeys(xpath('(//input[@name="paramValue"])[2]'), casper.page.event.key.Enter);
		});
		casper.waitUntilVisible('#save-policy');
		casper.then(function() {
			test.assertVisible('#save-policy', 'The "Save Policy" button should be visible');
			casper.click('#save-policy');
		});
		casper.waitWhileVisible('.modal-dialog');
		casper.waitForText('No IPs');
		casper.then(function() {
			test.assertSelectorHasText('td', 'No IPs', 'There should be a row entry for the policy "No IPs"');
		});

		// Save foreign source definition
		casper.thenClick('#save-foreign-source');
		casper.waitWhileVisible('#save-foreign-source');

		// Go back to the requisition's page
		casper.thenClick('#go-back');
		casper.waitForText('(0 defined, 0 deployed)');

		// Add a node to the requisition
		casper.wait(10000);
		casper.thenClick('#add-node');
		casper.waitUntilVisible('input#nodeLabel');
		casper.then(function() {
			casper.sendKeys('input#nodeLabel', nodeLabel, {reset:true});
			casper.sendKeys('input#foreignId', foreignId, {reset:true});
		});

		// Add an interface to the node
		casper.waitForSelector('#tab-interfaces .ng-binding');
		casper.thenClick('#tab-interfaces .ng-binding');
		casper.waitUntilVisible('#add-interface');
		casper.then(function() {
			test.assertExists('#add-interface', 'The "Add Interface" button should exist');
			casper.click('#add-interface');
		});
		casper.waitUntilVisible('form[name="intfForm"]');
		casper.then(function() {
			test.assertExists('form[name="intfForm"]', 'The "Add Interface" form should be visible');
			casper.sendKeys('input#ipAddress', ipAddress);
		});

		// Add a service to the interface
		casper.thenClick('#add-service');
		casper.waitUntilVisible('input[name="serviceName"]', function() {
			test.assertExists('input[name="serviceName"]', 'The "Service Name" field should be visible');
			casper.sendKeys('input[name="serviceName"]', service);
			casper.sendKeys('input[name="serviceName"]', casper.page.event.key.Enter);
		});
		casper.waitForSelector('.modal-dialog ul.dropdown-menu a[title="'+service+'"]', function() {
			casper.then('.modal-dialog ul.dropdown-menu a[title="'+service+'"]'); // This should happen if the dropdown-menu is present.
		});
		casper.waitWhileVisible('.modal-dialog ul.dropdown-menu');

		// Save the interface
		casper.waitUntilVisible('#save-interface');
		casper.then(function() {
			test.assertVisible('#save-interface', 'The "Save Interface" button should be visible');
			casper.click('#save-interface');
		});
		casper.waitWhileVisible('.modal-dialog');
		casper.wait(10000);

		// Add an asset to the node
		casper.waitForSelector('#tab-assets .ng-binding');
		casper.thenClick('#tab-assets .ng-binding');
		casper.waitUntilVisible('#add-asset');
		casper.then(function() {
			test.assertExists('#add-asset', 'The "Add Asset" button should exist');
			casper.click('#add-asset');
		});
		casper.waitUntilVisible('form[name="assetForm"]');
		casper.then(function() {
			test.assertExists('form[name="assetForm"]', 'The "Add Asset" form should be visible');
			casper.sendKeys('input#asset-name', 'country');
			casper.sendKeys('input#asset-name', casper.page.event.key.Enter);
			casper.sendKeys('input#asset-value', 'USA');
		});

		// Save the asset
		casper.waitUntilVisible('#save-asset');
		casper.then(function() {
			test.assertVisible('#save-asset', 'The "Save Asset" button should be visible');
			casper.click('#save-asset');
		});
		casper.waitWhileVisible('.modal-dialog');
		casper.wait(10000);

		// Add a category to the node
		casper.waitForSelector('#tab-categories .ng-binding');
		casper.thenClick('#tab-categories .ng-binding');
		casper.waitUntilVisible('#add-category');
		casper.then(function() {
			test.assertExists('#add-category', 'The "Add Category" button should exist');
			casper.click('#add-category');
		});
		casper.waitUntilVisible('input[name="categoryName"]');
		casper.then(function() {
			test.assertExists('input[name="categoryName"]', 'The "Category Name" field should be visible');
			this.sendKeys('input[name="categoryName"]', category, {keepFocus: true});
			this.sendKeys('input[name="categoryName"]', casper.page.event.key.Enter);
		});
		casper.waitWhileVisible('ul.dropdown-menu');
		casper.wait(10000);

		// Save the node
		casper.waitForSelector('#save-node');
		casper.then(function() {
			test.assertVisible('#save-node', 'The "Save Node" button should be visible');
			casper.click('#save-node');
		});

		// Go back to the requisition's page
		casper.waitWhileVisible('#save-node');
		casper.thenClick('#go-back');
		casper.waitForText(nodeLabel, function() {
			test.assertSelectorHasText('td', nodeLabel, 'There should be a node row entry for the test node label');
			test.assertSelectorHasText('td', foreignId, 'There should be a node row entry for the test foreign ID');
			test.assertSelectorHasText('td', ipAddress + ' (P)', 'There should be a node row entry for the IP address');
			//test.assertSelectorDoesntHaveText('td', '0', 'There should be a node row entry for the asset and the category');
		});

		// Synchronize the requisition
		casper.wait(10000);
		casper.waitWhileSelector('button[disabled][id="synchronize"]');
		casper.thenClick('#synchronize');
		casper.waitForSelector('.modal-dialog button.btn.btn-success', function() {
			test.assertSelectorHasText('.modal-dialog button.btn.btn-success', 'Yes', 'Make sure "Yes" button is visible');
		});
		casper.thenClick('.modal-dialog button.btn.btn-success');
		casper.waitWhileSelector('.modal_dialog');
		casper.waitForText('(1 defined, 0 deployed)', function() {
			test.assertTextExists('(1 defined, 0 deployed)', 'There should be one defined node on the requisition node page');
		});
		casper.waitWhileSelector('.danger', function() {
			test.assertDoesntExist('.danger', 'There should be no unfinished/red entries on the screen');
		});
		casper.wait(10000); // Wait until the requisition is synchronized (it should be quick)
		casper.thenClick('#refresh');
		casper.waitForText('(1 defined, 1 deployed)', function() {
			test.assertTextExists('(1 defined, 1 deployed)', 'There should be one deployed node on the requisition node page');
		});

		// Verify Node
		var nodeUrl = opennms.root() + '/rest/nodes/' + foreignSource + ':' + foreignId;
		casper.thenOpen(nodeUrl, function(response) {
			if (response.status !== 200) {
				test.fail('There was an error retrieving the provisioned node.');
			} else {
				test.pass('The node has been successfuly provisioned');
			}
		});
		casper.thenOpen(nodeUrl + '/ipinterfaces/' + ipAddress + '/services/' + service, function(response) {
			if (response.status !== 200) {
				test.fail('The service "'+service+'" should exist on the provisioned node.');
			} else {
				test.pass('The monitored service has been successfuly provisioned');				
			}
		});
		casper.thenOpen(nodeUrl + '/categories/' + category, function(response) {
			if (response.status !== 200) {
				test.fail('The category "'+category+'" should exist on the provisioned node.');
			} else {
				test.pass('The monitored service has been successfuly provisioned');
			}
		});

		opennms.finished(test);
	}
});
