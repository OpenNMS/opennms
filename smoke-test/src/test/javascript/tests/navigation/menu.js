'use strict';

var opennms = require('../../util/opennms')(casper),
	utils = require('utils');

var expected = {
	'Search': {
		href: '/element/index.jsp',
		linkPageSelector: 'h3.panel-title:first-of-type',
		linkPageText: 'Search for Nodes'
	},
	'Info': {
		children: {
			'Nodes': {
				href: '/element/nodeList.htm',
				linkPageSelector: 'h3.panel-title',
				linkPageText: 'Nodes'
			},
			'Assets': {
				href: '/asset/index.jsp',
				linkPageSelector: 'h3.panel-title',
				linkPageText: 'Search Asset Information'
			},
			'Path Outages': {
				href: '/pathOutage/index.jsp',
				linkPageSelector: 'h3.panel-title',
				linkPageText: 'All Path Outages'
			}
		}
	},
	'Status': {
		children: {
			'Events': {
				href: '/event/index',
				linkPageSelector: 'h3.panel-title',
				linkPageText: 'Event Queries'
			},
			'Alarms': {
				href: '/alarm/index.htm',
				linkPageSelector: 'h3.panel-title',
				linkPageText: 'Alarm Queries'
			},
			'Notifications': {
				href: '/notification/index.jsp',
				linkPageSelector: 'h3.panel-title',
				linkPageText: 'Notification queries'
			},
			'Outages': {
				href: '/outage/index.jsp',
				linkPageSelector: 'h3.panel-title',
				linkPageText: 'Outage Menu'
			},
			'Surveillance': '/surveillance-view.jsp',
			'Heatmap': '/heatmap/index.jsp'
			/*,
			'Distributed Status': {
				href: '/distributedStatusSummary.htm',
				linkPageSelector: 'h3.panel-title',
				linkPageText: 'Distributed Status Summary Error: No Applications Defined'
			},
			'Scan Reports': {
				href: '/scanreports/index.jsp',
				linkPageSelector: 'table.table.table-bordered.severity'
			}
			*/
		}
	},
	'Reports': {
		href: '/report/index.jsp',
		linkPageSelector: 'h3.panel-title',
		linkPageText: 'Reports',
		children: {
			'Charts': {
				href: '/charts/index.jsp',
				linkPageSelector: 'img[src="charts?chart-name=sample-bar-chart"]'
			},
			'Resource Graphs': {
				href: '/graph/index.jsp',
				linkPageSelector: 'h3.panel-title',
				linkPageText: 'Network Performance Data'
			},
			'KSC Reports': {
				href: '/KSC/index.htm',
				linkPageSelector: 'h3.panel-title',
				linkPageText: 'Node & Domain Interface Reports'
			},
			'Database Reports': {
				href: '/report/database/index.htm',
				linkPageSelector: 'h3.panel-title',
				linkPageText: 'Database Reports'
			},
			'Statistics': {
				href: '/statisticsReports/index.htm',
				linkPageSelector: 'h3.panel-title',
				linkPageText: 'Statistics Report List'
			}
		}
	},
	'Dashboards': {
		href: '/dashboards.htm',
		linkPageSelector: 'h3.panel-title',
		linkPageText: 'OpenNMS Dashboards',
		children: {
			'Dashboard': '/dashboard.jsp',
			'Ops Board': {
				href: '/vaadin-wallboard',
				linkPageSelector: 'div.v-label.v-widget',
				linkPageText: 'Nothing to display'
			}
		}
	},
	'Maps': {
		href: '/maps.htm',
		linkPageSelector: 'h3.panel-title',
		linkPageText: 'Maps',
		children: {
			/* smoke tests have this, but a default install does not, skip it for now
			'Distributed': {
				'/RemotePollerMap/index.jsp',
			},
			*/
			'Topology': {
				href: '/topology',
				linkPageSelector: 'table.topoHudDisplay div.gwt-Label',
				linkPageText: 'Vertices'
			},
			'Geographical': {
				href: '/node-maps',
				linkPageSelector: 'div[for="alarmControl"]',
				linkPageText: 'Show Severity >='
			}
		}
	},
	'admin': {
		href: '/account/selfService/index.jsp',
		linkPageSelector: 'h3.panel-title',
		linkPageText: 'User Account Self-Service',
		children: {
			'Notices: Off': {
				name: 'nav-admin-notice-status'
			},
			'Configure OpenNMS': {
				name: 'nav-admin-admin',
				href: '/admin/index.jsp',
				linkPageSelector: 'h3.panel-title',
				linkPageText: 'Performance Measurement'
			},
			'Quick-Add Node': {
				name: 'nav-admin-quick-add',
				href: '/admin/ng-requisitions/app/quick-add-node.jsp',
				linkPageSelector: 'h3.panel-title',
				linkPageText: 'Node Quick-Add'
			},
			'Help/Support': {
				name: 'nav-admin-support',
				href: '/support/index.htm',
				linkPageSelector: 'h3.panel-title',
				linkPageText: 'Commercial Support'
			},
			'Change Password': {
				name: 'nav-admin-self-service',
				href: '/account/selfService/index.jsp',
				linkPageSelector: 'h3.panel-title',
				linkPageText: 'User Account Self-Service'
			},
			'Log Out': {
				name: 'nav-admin-logout',
				href: '/j_spring_security_logout',
				linkPageSelector: 'label[for="input_j_username"]',
				linkPageText: 'Username'
			}
		}
	}
};

casper.test.begin('OpenNMS Nav Bar Menu', {
	setUp: function() {
		opennms.initialize();
		opennms.login();
		opennms.ensureNoRequisitions();
	},

	test: function(test) {
		var getElement = function(selector) {
			var elements = rootElement.querySelectorAll(selector);
			if (elements) {
				return elements.length;
			} else {
				return 0;
			}
		};

		var getEntry = function(text, obj, parent) {
			var ret = {
				selector: 'nav-' + text + '-top',
				href: undefined,
				text: text
			};

			if (parent) {
				ret.selector = 'nav-' + parent + '-' + text;
			}

			var entry = obj[text];
			if (typeof entry === 'string') {
				ret.href = opennms.root() + entry;
			} else {
				if (entry.name) {
					ret.selector = entry.name;
				}
				if (entry.href) {
					ret.href = opennms.root() + entry.href;
				}
				if (entry.linkPageSelector) {
					ret.linkPageSelector = entry.linkPageSelector;
				}
				if (entry.linkPageText) {
					ret.linkPageText = entry.linkPageText;
				}
			}
			if (!ret.href) {
				ret.href = '#';
			}

			return ret;
		};

		var testSelectorExists = function(selector, name) {
			casper.waitForSelector(selector);
			casper.then(function() {
				test.assertExists(selector, name);
			});
		};

		var testClickable = function(moveto, selector, text, name) {
			if (!utils.isArray(moveto)) {
				moveto = [moveto];
			}
			for (var m=0, len=moveto.length; m < len; m++) {
				var loc = moveto[m];
				casper.waitForSelector(loc);
				casper.then(function() {
					this.mouseEvent('mouseover', loc);
				});
			}
			casper.then(function() {
				this.click(moveto[moveto.length-1]);
			});

			if (selector) {
				if (text) {
					var desc;
					if (name) {
						desc = name + ' link target page has text "' + text + '"';
					}
					casper.waitForSelector(selector, function() {
						test.assertSelectorHasText(selector, text, desc);
					});
				} else {
					var desc;
					if (name) {
						desc = name + ' link target page selector matches.';
					}
					casper.waitForSelector(selector, function() {
						test.assertExists(selector, desc);
					});
				}
			}

			// Vaadin apps do weird redirects on first launch sometimes, so make sure we've gone back enough to reset.
			casper.then(function() {
				casper.back();
			});
			casper.then(function() {
				casper.back();
			});
			casper.then(function() {
				casper.back();
			});
		};

		var getMenuEntryName = function(entries) {
			if (!utils.isArray(entries)) {
				entries = [entries];
			}
			return '[' + entries.join(' -> ') + ']';
		};

		for (var text in expected) {
			casper.thenOpen(opennms.root());
			if (expected.hasOwnProperty(text)) {
				var entry = getEntry(text, expected);
				var entrySelector = 'ul > li > a[name=\"' + entry.selector.replace(/\"/, '\\\"') + '\"]';
				testSelectorExists(entrySelector, getMenuEntryName(text) + ' menu entry exists');
				testClickable(entrySelector, entry.linkPageSelector, entry.linkPageText, getMenuEntryName(text));
				if (expected[text].children) {
					var children = expected[text].children;
					for (var child in children) {
						casper.thenOpen(opennms.root());
						if (children.hasOwnProperty(child)) {
							var childEntry = getEntry(child, children, text);
							var childSelector = 'ul > li > ul > li > a[name=\"' + childEntry.selector.replace(/\"/, '\\\"') + '\"]';
							testSelectorExists(childSelector, getMenuEntryName([text, child]) + ' menu entry exists');
							testClickable([entrySelector, childSelector], childEntry.linkPageSelector, childEntry.linkPageText, getMenuEntryName([text, child]));
						}
					}
				}
			}
		}

		////// Special Cases //////
		opennms.login(casper);

		// surveillance view
		casper.thenOpen(opennms.root() + '/surveillance-view.jsp');
		casper.waitForSelector('#surveillance-view-ui');
		casper.then(function() {
			this.page.switchToChildFrame(0);
		});
		casper.waitForSelector('span.v-captiontext', function() {
			test.assertSelectorHasText('span.v-captiontext', 'Surveillance view: default', 'Surveillance View iframe loads');
		});
		casper.then(function() {
			this.page.switchToParentFrame();
		});

		// heatmap
		casper.thenOpen(opennms.root() + '/heatmap/index.jsp');
		casper.waitForSelector('#coreweb', function() {
			test.assertSelectorHasText('h3.panel-title > a', 'Alarm Heatmap  (by Categories)', 'Heatmap iframe loads');
		});

		// dashboard
		casper.thenOpen(opennms.root() + '/dashboard.jsp');
		casper.waitForSelector('#surveillance-view-ui');
		casper.then(function() {
			this.page.switchToChildFrame(0);
		});
		casper.waitForSelector('span.v-captiontext', function() {
			test.assertSelectorHasText('span.v-captiontext', 'Surveillance view: default', 'Surveillance View iframe loads');
		});
		casper.then(function() {
			this.page.switchToParentFrame();
		});

		// distributed maps
		/*
		casper.thenOpen(opennms.root() + '/RemotePollerMap/index.jsp');
		casper.waitForSelector('#app');
		casper.then(function() {
			this.page.switchToChildFrame(0);
		});
		casper.waitForSelector('div.gwt-hyperlink', function() {
			test.assertSelectorHasText('div.gwt-hyperlink', 'Applications', 'Distributed Map has "Applications" chooser in the sidebar.');
		});
		casper.then(function() {
			this.page.switchToParentFrame();
		});
		*/

		opennms.finished(test);
	}
});
