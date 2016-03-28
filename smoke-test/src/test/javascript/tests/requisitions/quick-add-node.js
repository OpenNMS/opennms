'use strict';

var opennms = require('../../util/opennms')(casper),
    utils = require('utils');

var foreignSource = 'add-node-to-requisition-test';
var nodeLabel = 'localNode';
var ipAddress = '127.0.0.1';
var category = 'Test';

casper.test.begin('Quick-Add Node to Requisition', {
    setUp: function() {
        opennms.initialize();
        opennms.login();
        opennms.ensureNoRequisitions();
        opennms.createOrReplaceRequisition(foreignSource);
    },

    test: function(test) {
        casper.thenOpen(opennms.root() + '/admin/index.jsp');

        casper.waitForSelector('ul#navbar-nav a[name="nav-admin-quick-add"]');
        casper.then(function() {
            casper.clickLabel('Quick-Add Node');
        });

        // Basic fields
        casper.waitUntilVisible('input#foreignSource');
        casper.then(function() {
            test.assertVisible('input#foreignSource', 'The "Foreign Source" field should be visible');
            casper.sendKeys('input#nodeLabel', nodeLabel);
            casper.sendKeys('input#ipAddress', ipAddress);
            casper.sendKeys('input#foreignSource', foreignSource);
            casper.sendKeys('input#foreignSource', casper.page.event.key.Enter);
        });

        // Add a category to the node
        casper.waitForSelector('#add-category');
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

        // Provision
        casper.waitWhileSelector('button[disabled][id="provision"]');
        casper.thenClick('#provision');
        casper.waitUntilVisible('.modal-dialog button[data-bb-handler="main"]');
        casper.thenClick('.modal-dialog button[data-bb-handler="main"]');

        // Verify Node
        casper.wait(20000);
        var nodeUrl = opennms.root() + '/rest/nodes?label=' + nodeLabel;
        casper.thenOpen(nodeUrl, function(response) {
            if (response.status !== 200) {
                test.fail('There was an error retrieving the provisioned node.');
            } else {
                test.pass('The node has been successfuly provisioned');
            }
        });

        opennms.finished(test);
    }
});
