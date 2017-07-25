/**
 * Convert from a clause into a FIQL query string.
 */
function toFiql(clauses) {
	var first = true;
	var fiql = '';
	for (var i = 0; i < clauses.length; i++) {
		if (!first) {
			// Use OR to combine all operations
			fiql += ',';
		}
		fiql += clauses[i].property;

		switch (clauses[i].operator) {
		case 'EQ':
			fiql += '=='; break;
		case 'NE':
			fiql += '!='; break;
		case 'LT':
			fiql += '=lt='; break;
		case 'LE':
			fiql += '=le='; break;
		case 'GT':
			fiql += '=gt='; break;
		case 'GE':
			fiql += '=ge='; break;
		}

		fiql += escapeSearchValue(clauses[i].value);

		first = false;
	}
	return fiql;
}

/**
 * Escape FIQL reserved characters by URL-encoding them. Reserved characters are:
 * <ul>
 * <li>!</li>
 * <li>$</li>
 * <li>'</li>
 * <li>(</li>
 * <li>)</li>
 * <li>*</li>
 * <li>+</li>
 * <li>,</li>
 * <li>;</li>
 * <li>=</li>
 * </ul>
 * @param value
 * @returns String with reserved characters URL-encoded
 */
function escapeSearchValue(value) {
	if (typeof value === 'string') {
		return value
			.replace('!', '%21')
			.replace('$', '%24')
			.replace("'", '%27')
			.replace('(', '%28')
			.replace(')', '%29')
			// People are going to type this in as a wildcard, so I 
			// guess they'll have to type in '%2A' if they want to
			// match an asterisk...
			//.replace('*', '%2A')
			.replace('+', '%2B')
			.replace(',', '%2C')
			.replace(';', '%3B')
			.replace('=', '%3D');
	} else {
		return value;
	}
}

(function() {
	'use strict';
	
	var MODULE_NAME = 'onms.quickSearch';

	// List module
	angular.module(MODULE_NAME, [ 'ngResource', 'onms.restResources', 'ui.bootstrap.typeahead' ])

	.config(function($locationProvider) {
		$locationProvider.html5Mode({
			// Use HTML5 
			enabled: true,
			// Don't rewrite all <a> links on the page
			rewriteLinks: false
		});
	})

	.filter('stripTags', function() {
		return function(input) {
			if (typeof input === 'string') {
				return input.replace(/<(?:.|\n)*?>/gm, '');
			} else if (typeof input === 'number') {
				return input + '';
			} else {
				return '';
			}
		}
	})

	/**
	 * Generic list controller
	 */
	.controller('QuickSearchCtrl', ['$scope', '$location', '$window', '$log', '$filter', 'alarmFactory', 'eventFactory', 'notificationFactory', function($scope, $location, $window, $log, $filter, alarmFactory, eventFactory, notificationFactory) {
		$log.debug('QuickSearchCtrl initializing...');

		$scope.defaults = {
			q: '',
			alarms: true,
			events: true,
			notifications: true,
			nodes: true
		}

		// Restore any query parameters that you can from the 
		// query string, blank out the rest
		$scope.query = {
			q: typeof $location.search().q === 'undefined' ? $scope.defaults.q : $location.search().q,
			alarms: typeof $location.search().alarms === 'undefined' ? $scope.defaults.alarms : $location.search().alarms === "true",
			events: typeof $location.search().events === 'undefined' ? $scope.defaults.events : $location.search().events === "true",
			notifications: typeof $location.search().notifications === 'undefined' ? $scope.defaults.notifications : $location.search().notifications === "true",
			nodes: typeof $location.search().nodes === 'undefined' ? $scope.defaults.nodes : $location.search().nodes === "true"
		};

		// Sync the query hash with the $location query string
		$scope.$watch('query', function() {
				var queryParams = angular.copy($scope.query);

				// Delete derived values that we don't need in the query string
				//delete queryParams.[...];

				// Delete any parameters that have default or blank values
				if (queryParams.q === $scope.defaults.q || queryParams.q === '') { delete queryParams.q; }
				if (queryParams.alarms === $scope.defaults.alarms || queryParams.alarms === 'undefined') { delete queryParams.alarms; }
				if (queryParams.events === $scope.defaults.events || queryParams.events === 'undefined') { delete queryParams.events; }
				if (queryParams.notifications === $scope.defaults.notifications || queryParams.notifications === 'undefined') { delete queryParams.notifications; }
				if (queryParams.nodes === $scope.defaults.nodes || queryParams.nodes === 'undefined') { delete queryParams.nodes; }

				$location.search(queryParams);

				$scope.refresh();
			}, 
			true // Use object equality because the reference doesn't change
		);

		/**
		 * Generate a hash containing all fields of <code>item</code>
		 * whose values match the current query string. Always include
		 * the fields listed in <code>required</code>.
		 */
		$scope.getKeyFields = function(item, required) {
			var retval = {};
			var keys = Object.keys(item);
			keyLoop:
			for (var i = 0; i < keys.length; i++) {
				var value = item[keys[i]];

				// If the field is in the required list, always add the value
				for (var j = 0; j < required.length; j++) {
					if (keys[i] === required[j]) {
						// Special case for formatting epoch dates
						if(
							keys[i] === 'lastEventTime' ||
							keys[i] === 'time'
						) {
							value = $filter('date')(new Date(value), 'MMM d, yyyy h:mm:ss a');
						}
						retval[keys[i]] = value;
						continue keyLoop;
					}
				}

				// Handle string values
				if (
					typeof $scope.query.q === 'string' && 
					typeof value === 'string' && 
					value.match($scope.query.q) != null
				) {
					// Special case for formatting epoch dates
					if(
						keys[i] === 'lastEventTime' ||
						keys[i] === 'time'
					) {
						value = $filter('date')(new Date(clause.value), 'MMM d, yyyy h:mm:ss a');
					}
					retval[keys[i]] = value;
				} else if (
					value &&
					typeof value === 'object'
				) {
					// Handle object values by recursively calling getKeyFields()
					var childFields = $scope.getKeyFields(value, required);
					var childKeys = Object.keys(childFields);
					for (var k = 0; k < childKeys.length; k++) {
						retval[keys[i] + '.' + childKeys[k]] = childFields[childKeys[k]];
					}
				}
			};
			return retval;
		};

		$scope.assetFields = [
			"assetRecord.assetNumber",
			"assetRecord.building",
			"assetRecord.category",
			"assetRecord.comment",
			"assetRecord.cpu",
			"assetRecord.department",
			"assetRecord.description",
			"assetRecord.floor",
			"assetRecord.lastModifiedBy",
			"assetRecord.manufacturer",
			"assetRecord.modelNumber",
			"assetRecord.operatingSystem",
			"assetRecord.rack",
			"assetRecord.ram",
			"assetRecord.region",
			"assetRecord.room",
			"assetRecord.serialNumber",
			"assetRecord.slot",
			"assetRecord.supportPhone",
			"assetRecord.vendor",
			"assetRecord.vendorAssetNumber",
			"assetRecord.vendorFax",
			"assetRecord.vendorPhone"
		];

		$scope.categoryFields = [
			"category.description",
			"category.name"
		];

		$scope.distPollerFields = [
			"distPoller.label",
			"distPoller.location"
		];

		$scope.ipInterfaceFields = [
			'ipInterface.ipHostName'
		];

		$scope.locationFields = [
			"location.locationName",
			"location.monitoringArea"
			//"location.priority"
		];

		$scope.nodeFields = [
			"node.foreignId",
			"node.foreignSource",
			"node.label",
			//"node.labelSource",
			"node.netBiosDomain",
			"node.netBiosName",
			"node.operatingSystem",
			"node.sysContact",
			"node.sysDescription",
			"node.sysLocation",
			"node.sysName",
			"node.sysObjectId",
			//"node.type"
		];

		$scope.serviceTypeFields = [
			"serviceType.name"
		];

		$scope.alarmFields = [
			//"alarm.alarmType",
			"alarm.clearKey",
			"alarm.description",
			"alarm.ipAddr",
			"alarm.logMsg",
			"alarm.operInstruct",
			"alarm.reductionKey",
			"alarm.uei",
			"lastEvent.eventAutoAction",
			"lastEvent.eventCorrelation",
			"lastEvent.eventDescr",
			"lastEvent.eventHost",
			"lastEvent.eventLogMsg",
			"lastEvent.eventOperAction",
			"lastEvent.eventOperActionMenuText",
			"lastEvent.eventOperInstruct",
			"lastEvent.eventPathOutage",
			"lastEvent.eventSnmp",
			"lastEvent.eventSnmpHost",
			"lastEvent.eventSource",
			"lastEvent.eventTTicket",
			//"lastEvent.eventTTicketState",
			"lastEvent.eventUei",
			//"lastEvent.ifIndex",
			//"lastEvent.ipAddr",
			//"snmpInterface.ifAdminStatus",
			//"snmpInterface.ifIndex",
			//"snmpInterface.ifOperStatus",
			//"snmpInterface.ifSpeed",
			//"snmpInterface.netMask"
		];
		// Add the asset fields
		Array.prototype.push.apply($scope.alarmFields, $scope.assetFields);
		// Add the category fields
		Array.prototype.push.apply($scope.alarmFields, $scope.categoryFields);
		// Add the distPoller fields
		Array.prototype.push.apply($scope.alarmFields, $scope.distPollerFields);
		// Add the ipInterface fields
		Array.prototype.push.apply($scope.alarmFields, $scope.ipInterfaceFields);
		// Add the location fields
		Array.prototype.push.apply($scope.alarmFields, $scope.locationFields);
		// Add the node fields
		Array.prototype.push.apply($scope.alarmFields, $scope.nodeFields);
		// Add the serviceType fields
		Array.prototype.push.apply($scope.alarmFields, $scope.serviceTypeFields);

		$scope.eventFields = [
			"event.eventAutoAction",
			"event.eventCorrelation",
			"event.eventDescr",
			"event.eventHost",
			"event.eventLogMsg",
			"event.eventOperAction",
			"event.eventOperActionMenuText",
			"event.eventOperInstruct",
			"event.eventPathOutage",
			"event.eventSnmp",
			"event.eventSnmpHost",
			"event.eventSource",
			"event.eventTTicket",
			//"event.eventTTicketState",
			"event.eventUei",
			//"event.ifIndex",
			//"event.ipAddr"
		];
		// Add the asset fields
		Array.prototype.push.apply($scope.eventFields, $scope.assetFields);
		// Add the category fields
		Array.prototype.push.apply($scope.eventFields, $scope.categoryFields);
		// Add the distPoller fields
		Array.prototype.push.apply($scope.eventFields, $scope.distPollerFields);
		// Add the ipInterface fields
		Array.prototype.push.apply($scope.eventFields, $scope.ipInterfaceFields);
		// Add the location fields
		Array.prototype.push.apply($scope.eventFields, $scope.locationFields);
		// Add the node fields
		Array.prototype.push.apply($scope.eventFields, $scope.nodeFields);
		// Add the serviceType fields
		Array.prototype.push.apply($scope.eventFields, $scope.serviceTypeFields);

		$scope.notificationFields = [
			//"notification.notifyId",
			"notification.answeredBy",
			//"notification.ipAddress",
			"notification.numericMsg",
			//"notification.pageTime",
			"notification.queueId",
			//"notification.respondTime",
			"notification.subject",
			"notification.textMsg"
		];
		// Add the event fields
		Array.prototype.push.apply($scope.notificationFields, $scope.eventFields);

		// Override this to implement updates to an object
		$scope.refresh = function() {

			/*
			 * TODO: Inspect search string and add IPLIKE, integer, enum 
			 * constraints if the value is appropriate.
			 */

			// If there is a query string then use the REST services to perform
			// a typeahead search
			if (typeof $scope.query.q === 'string' && $scope.query.q.length > 0) {

				var searchClauses = [];
				for (var i = 0; i < $scope.alarmFields.length; i++) {
					searchClauses.push({
						property: $scope.alarmFields[i],
						operator: 'EQ',
						value: '*' + $scope.query.q + '*' // Substring query
					});
				}

				var alarmQuery = toFiql(searchClauses);

				// Only search among unacknowledged alarms
				alarmQuery = toFiql([{
					property: 'alarmAckUser',
					operator: 'EQ',
					value: '\u0000' // null
				}]) + ';(' + alarmQuery + ')';

				// Fetch all of the items
				alarmFactory.query(
					{
						_s: alarmQuery,
						limit: 10,
						orderBy: 'id',
						order: 'desc'
					},
					function(value, headers) {
						$scope.alarms = value;
					},
					function(response) {
						switch(response.status) {
						case 404:
							// If we didn't find any elements, then clear the list
							$scope.alarms = [];
							break;
						case 401:
						case 403:
							// Handle session timeout by reloading page completely
							$window.location.href = $location.absUrl();
							break;
						}
						// TODO: Handle 500 Server Error by executing an undo callback?
						// TODO: Handle 431 Request Header Fields Too Large
						// TODO: Handle 414 URI Too Long
					}
				);

				searchClauses = [];
				for (var i = 0; i < $scope.eventFields.length; i++) {
					searchClauses.push({
						property: $scope.eventFields[i],
						operator: 'EQ',
						value: '*' + $scope.query.q + '*' // Substring query
					});
				}

				// Fetch all of the items
				eventFactory.query(
					{
						_s: toFiql(searchClauses),
						limit: 10,
						orderBy: 'id',
						order: 'desc'
					},
					function(value, headers) {
						$scope.events = value;
					},
					function(response) {
						switch(response.status) {
						case 404:
							// If we didn't find any elements, then clear the list
							$scope.events = [];
							break;
						case 401:
						case 403:
							// Handle session timeout by reloading page completely
							$window.location.href = $location.absUrl();
							break;
						}
						// TODO: Handle 500 Server Error by executing an undo callback?
						// TODO: Handle 431 Request Header Fields Too Large
						// TODO: Handle 414 URI Too Long
					}
				);

				searchClauses = [];
				for (var i = 0; i < $scope.notificationFields.length; i++) {
					searchClauses.push({
						property: $scope.notificationFields[i],
						operator: 'EQ',
						value: '*' + $scope.query.q + '*' // Substring query
					});
				}

				var notificationQuery = toFiql(searchClauses);

				// Only search among open notifications
				notificationQuery = toFiql([{
					property: 'answeredBy',
					operator: 'EQ',
					value: '\u0000' // null
				}]) + ';(' + notificationQuery + ')';

				// Fetch all of the items
				notificationFactory.query(
					{
						_s: notificationQuery,
						limit: 10,
						orderBy: 'notifyId',
						order: 'desc'
					},
					function(value, headers) {
						$scope.notifications = value;
					},
					function(response) {
						switch(response.status) {
						case 404:
							// If we didn't find any elements, then clear the list
							$scope.notifications = [];
							break;
						case 401:
						case 403:
							// Handle session timeout by reloading page completely
							$window.location.href = $location.absUrl();
							break;
						}
						// TODO: Handle 500 Server Error by executing an undo callback?
						// TODO: Handle 431 Request Header Fields Too Large
						// TODO: Handle 414 URI Too Long
					}
				);
			} else {
				$scope.alarms = [];
				$scope.events = [];
				$scope.nodes = [];
				$scope.notifications = [];
			}
		}

		// Refresh the item list;
		$scope.refresh();

		$log.debug('QuickSearchCtrl initialized');
	}])

	.run(['$rootScope', '$log', function($rootScope, $log) {
		$log.debug('Finished initializing ' + MODULE_NAME);
	}])

	;

	angular.element(document).ready(function() {
		console.log('Bootstrapping ' + MODULE_NAME);
		angular.bootstrap(document, [MODULE_NAME]);
	});
}());
