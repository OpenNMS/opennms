'use strict';

var opennms = require('../../util/opennms')(casper),
	utils = require('utils');

var testUser = 'SmokeTestUser',
	testGroup = 'SmokeTestGroup',
	testPassword = 'SmokeTestPassword';

casper.test.begin('User Admin Page', {
	setUp: function() {
		opennms.initialize();
		opennms.login();
		casper.thenOpen(opennms.root() + '/admin/index.jsp');
	},

	test: function(test) {
		casper.then(function() {
			casper.clickLabel('Configure Users, Groups and On-Call Roles');
		});
		casper.then(function() {
			casper.clickLabel('Configure Users');
		});
		casper.then(function() {
			casper.click('a#doNewUser');
		});

		casper.then(function() {
			casper.fill('form#newUserForm', {
				userID: testUser,
				pass1: testPassword,
				pass2: testPassword
			}, false);
		});
		casper.then(function() {
			casper.clickLabel('OK');
		});
		casper.then(function() {
			test.assertSelectorHasText('h3.panel-title', 'Modify User: ' + testUser, 'there should be a "Modify User" panel');
			casper.clickLabel('Finish');
		});
		casper.then(function() {
			test.assertExists('a[id="users(' + testUser + ').doModify"]', 'the "doModify" action should exist for user ' + testUser);
		});

		casper.waitForSelector('li a[href="admin/userGroupView/index.jsp"]', function() {
			casper.clickLabel('Users and Groups');
		});
		casper.waitForSelector('div.panel-body p a[href="admin/userGroupView/groups/list.htm"]', function() {
			casper.clickLabel('Configure Groups');
		});
		casper.waitForSelector('a[href="javascript:addNewGroup()"]', function() {
			casper.click('a[href="javascript:addNewGroup()"]');
		});
		casper.waitForSelector('form#newGroupForm', function() {
			casper.fill('form#newGroupForm', {
				groupName: testGroup,
				groupComment: 'Test'
			}, false);
		});
		casper.then(function() {
			casper.clickLabel('OK');
		});
		casper.then(function() {
			test.assertSelectorHasText('h3.panel-title', 'Assignments', 'Assignments header should exist');
			casper.fillSelectors('form#modifyGroup', {
				'select[name="availableUsers"]': testUser
			}, false);
		});
		casper.then(function() {
			casper.click('button[id="users.doAdd"]');
		});
		casper.then(function() {
			casper.clickLabel('Finish');
		});

		casper.then(function() {
			test.assertExists('a[href="javascript:detailGroup(\''+testGroup+'\')"]', testGroup + ' should exist in group list');
			casper.clickLabel(testGroup);
		});
		casper.then(function() {
			test.assertSelectorHasText('h2.panel-title', 'Details for Group: ' + testGroup, testGroup + ' details should be visible');
			test.assertSelectorHasText('table td > ul > li', testUser, testGroup + ' should have user ' + testUser + ' in the details page user list');
			casper.clickLabel('Group List');
		});

		var testingConfirm = 'group';
		casper.setFilter('page.confirm', function(msg) {
			if (testingConfirm === 'group') {
				return msg === 'Are you sure you want to delete the group '+testGroup+'?';
			} else if (testingConfirm === 'user') {
				return msg === 'Are you sure you want to delete the user '+testUser+'?';
			} else {
				console.log('unknown confirmation test: ' + testingConfirm);
			}
		});

		casper.then(function() {
			casper.click('a[id="' + testGroup + '.doDelete"]');
		});
		casper.then(function() {
			test.assertSelectorHasText('ol.breadcrumb > li', 'Group List', 'Group List page should be visible');
			test.assertDoesntExist('table tr[id="'+testGroup+'"]', testGroup + ' should not be in the group list');
			casper.clickLabel('Users and Groups');
		});

		casper.then(function() {
			casper.clickLabel('Configure Users');
		});
		casper.then(function() {
			testingConfirm = 'user';
			test.assertSelectorHasText('ol.breadcrumb > li', 'User List', 'User List page should be visible');
			test.assertExists('table tr[id="user-' + testUser + '"]', testUser + ' should be in the user list');
			casper.click('a[id="users('+testUser+').doDelete"]');
		});
		casper.then(function() {
			test.assertSelectorHasText('ol.breadcrumb > li', 'User List', 'User List page should be visible');
			test.assertDoesntExist('table tr[id="user-' + testUser + '"]', testUser + ' should not be in the user list');
		});

		opennms.finished(test);
	}
});
