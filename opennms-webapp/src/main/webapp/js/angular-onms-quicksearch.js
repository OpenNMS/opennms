/**
 * ISO-8601 date format string.
 */
var ISO_8601_DATE_FORMAT = 'yyyy-MM-ddTHH:mm:ss.sssZ';
var ISO_8601_DATE_FORMAT_WITHOUT_MILLIS = 'yyyy-MM-ddTHH:mm:ssZ';

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
 * Convert from a FIQL query string into separate clause objects.
 * This only works for simple queries composed of multiple AND (';')
 * clauses.
 * 
 * TODO: Expand this to cover more FIQL syntax
 */
function fromFiql(fiql) {
	var statements = fiql.split(';');
	var segments = [];
	var clauses = [];
	for (var i = 0; i < statements.length; i++) {
		if (statements[i].indexOf('==') > 0) {
			segments = statements[i].split('==');
			clauses.push({
				property: segments[0],
				operator: 'EQ',
				value: segments[1]
			});
		} else if (statements[i].indexOf('!=') > 0) {
			segments = statements[i].split('!=');
			clauses.push({
				property: segments[0],
				operator: 'NE',
				value: segments[1]
			});
		} else if (statements[i].indexOf('=lt=') > 0) {
			segments = statements[i].split('=lt=');
			clauses.push({
				property: segments[0],
				operator: 'LT',
				value: segments[1]
			});
		} else if (statements[i].indexOf('=le=') > 0) {
			segments = statements[i].split('=le=');
			clauses.push({
				property: segments[0],
				operator: 'LE',
				value: segments[1]
			});
		} else if (statements[i].indexOf('=gt=') > 0) {
			segments = statements[i].split('=gt=');
			clauses.push({
				property: segments[0],
				operator: 'GT',
				value: segments[1]
			});
		} else if (statements[i].indexOf('=ge=') > 0) {
			segments = statements[i].split('=ge=');
			clauses.push({
				property: segments[0],
				operator: 'GE',
				value: segments[1]
			});
		}
	}
	return clauses;
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
	if (typeof value === 'String') {
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

/**
 * Parse an HTTP Content-Range header into the start, end, and total fields.
 * The header should be in a format like: "items 0-14/28".
 * 
 * @param contentRange String from the Content-Range header
 */
function parseContentRange(contentRange) {
	if (typeof contentRange === 'undefined' || contentRange === null) {
		return {start: 0, end: 0, total: 0};
	}
	// Example: items 0-14/28
	var pattern = /items\s+?(\d+)\s*\-\s*(\d+)\s*\/\s*(\d+)/;
	return {
		start: Number(contentRange.replace(pattern, '$1')),
		end: Number(contentRange.replace(pattern, '$2')),
		total: Number(contentRange.replace(pattern, '$3'))
	};
}

function normalizeOffset(offset, maxOffset, limit) {
	var newOffset = offset;

	// Offset of the last page
	var lastPageOffset;
	if (maxOffset < 0) {
		newOffset = 0;
		lastPageOffset = 0;
	} else {
		lastPageOffset = Math.floor(maxOffset / limit) * limit; 
	}

	// Bounds checking
	newOffset = ((newOffset < 0) ? 0 : newOffset);
	newOffset = ((newOffset > lastPageOffset) ? lastPageOffset : newOffset);

	// Make sure that offset is a multiple of limit
	newOffset = Math.floor(newOffset / limit) * limit;

	return newOffset;
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

	.directive('onmsQuickSearchInput', function() {
		return {
			controller: function($scope) {
				$scope.editing = false;
				$scope.originalValue = angular.copy($scope.value);

				// Start editing the value
				$scope.edit = function() {
					$scope.editing = true;
				}

				// Stop editing the value
				$scope.unedit = function() {
					$scope.editing = false;
				}

				$scope.onKeyup = function($event) {
					// If the user types ESC, then abort the edit
					if($event.keyCode === 27) {
						$scope.cancel();
					}
				}

				$scope.submit = function() {
					$scope.onSubmit();
					// TODO: Handle update failures
					// Now that we've save a new value, use it as the original value
					$scope.originalValue = $scope.value;
					// Switch out of edit mode
					$scope.unedit();
				}

				$scope.cancel = function() {
					// Restore the original value
					$scope.value = $scope.originalValue;
					// Switch out of edit mode
					$scope.unedit();
				}
			},
			// Use an isolated scope
			scope: {
				item: '=',
				value: '=',
				valueType: '=',
				// Optional step attribute for number fields
				step: '=',
				onSubmit: '&onSubmit'
			},
			templateUrl: 'js/angular-onms-elementList-editInPlace.html',
			transclude: true
		};
	})

	.filter('stripTags', function() {
		return function(input) {
			if (typeof input === 'string') {
				return input.replace(/<(?:.|\n)*?>/gm, '');
			} else {
				return input;
			}
		}
	})

	/**
	 * Generic list controller
	 */
	.controller('QuickSearchCtrl', ['$scope', '$location', '$window', '$log', '$filter', 'alarmFactory', 'eventFactory', function($scope, $location, $window, $log, $filter, alarmFactory, eventFactory) {
		$log.debug('QuickSearchCtrl initializing...');

		$scope.defaults = {
			q: '',
			alarms: true,
			events: true,
			notifications: true,
			nodes: true
		}

		//var initialLimit = typeof $location.search().limit === 'undefined' ? $scope.defaults.limit : (Number($location.search().limit) > 0 ? Number($location.search().limit) : $scope.defaults.limit);

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

		// Add the search clause to the list of clauses
		$scope.addSearchClause = function(clause) {
			if(angular.isDate(clause.value)) {
				// Returns a value in yyyy-MM-ddTHH:mm:ss.sssZ format
				// Unfortunately, I don't think CXF will like this because
				// it includes the millisecond portion of the date.
				//clause.value = new Date(clause.value).toISOString();

				clause.value = $filter('date')(new Date(clause.value), ISO_8601_DATE_FORMAT);
			}

			// Make sure the clause isn't already in the list of search clauses
			if ($scope.getSearchClause(clause) != null) {
				return;
			}

			// TODO: Add validation?
			$scope.query.searchClauses.push(angular.copy(clause));
			$scope.query.searchParam = toFiql($scope.query.searchClauses);
			$scope.refresh();
		}

		$scope.getSearchClause = function(clause) {
			for (var i = 0; i < $scope.query.searchClauses.length; i++) {
				if ($scope.clauseEquals(clause, $scope.query.searchClauses[i])) {
					return $scope.query.searchClauses[i];
				}
			}
			return null;
		}

		$scope.clauseEquals = function(a, b) {
			if (
				a.property === b.property &&
				a.operator === b.operator &&
				a.value === b.value
			) {
				return true;
			} else {
				return false;
			}
		}

		// Convert an epoch timestamp into String format before adding the search clause
		$scope.addEpochTimestampSearchClause = function(clause) {
			clause.value = $filter('date')(clause.value, ISO_8601_DATE_FORMAT);
			$scope.addSearchClause(clause);
		}

		// Remove a search clause from the list of clauses
		$scope.removeSearchClause = function(clause) {
			// TODO: Add validation?
			$scope.query.searchClauses.splice($scope.query.searchClauses.indexOf(clause), 1);
			$scope.query.searchParam = toFiql($scope.query.searchClauses);
			$scope.refresh();
		}

		$scope.removeSearchClauses = function(clauses) {
			for (var i = 0; i < clauses.length; i++) {
				var index = $scope.query.searchClauses.indexOf(clauses[i]);
				if (index >= 0) {
					$scope.query.searchClauses.splice(index, 1);
				}
			}
			$scope.query.searchParam = toFiql($scope.query.searchClauses);
			$scope.refresh();
		}

		// Replace a search clause with a new clause
		$scope.replaceSearchClause = function(oldClause, newClause) {
			if(angular.isDate(newClause.value)) {
				// Returns a value in yyyy-MM-ddTHH:mm:ss.sssZ format
				// Unfortunately, I don't think CXF will like this because
				// it includes the millisecond portion of the date.
				//clause.value = new Date(clause.value).toISOString();

				newClause.value = $filter('date')(new Date(newClause.value), ISO_8601_DATE_FORMAT);
			}

			// TODO: Add validation?
			var scopeOldClause = $scope.getSearchClause(oldClause);
			var scopeNewClause = $scope.getSearchClause(newClause);
			if (scopeOldClause == null) {
				if (scopeNewClause == null) {
					// If the old clause is not present, simply add the new clause
					$scope.addSearchClause(newClause);
				} else {
					// If the old clause is not present and the new clause is already
					// present, then do nothing
				}
			} else {
				if (scopeNewClause == null) {
					// If the old clause is present and the new clause is not, replace
					// the values inside the old clause and then refresh
					scopeOldClause.property = newClause.property;
					scopeOldClause.operator = newClause.operator;
					scopeOldClause.value = newClause.value;

					$scope.query.searchParam = toFiql($scope.query.searchClauses);
					$scope.refresh();
				} else {
					// If the old clause is present and the new clause is present,
					// then just remove the old clause (as if it had been replaced by
					// the already-existing new clause)
					$scope.removeSearchClause(oldClause);
				}
			}
		}

		// Clear the current search
		$scope.clearSearch = function() {
			if ($scope.query.searchClauses.length > 0) {
				$scope.query.searchClauses = [];
				$scope.query.searchParam = '';
				$scope.refresh();
			}
		}

		// Change the sorting of the table
		$scope.changeOrderBy = function(property) {
			if ($scope.query.orderBy === property) {
				// TODO: Figure out if we should reset limit/offset here also
				// If the property is already selected then reverse the sorting
				$scope.query.order = ($scope.query.order === 'asc' ? 'desc' : 'asc');
			} else {
				// TODO: Figure out if we should reset limit/offset here also
				$scope.query.orderBy = property;
				$scope.query.order = $scope.defaults.order;
			}
			$scope.refresh();
		}

		$scope.setOffset = function(offset) {
			offset = normalizeOffset(offset, $scope.query.maxOffset, $scope.query.limit);

			if ($scope.query.offset !== offset) {
				$scope.query.offset = offset;
				$scope.refresh();
			}
		}

		$scope.setLimit = function(limit) {
			if (limit < 1) {
				$scope.query.newLimit = $scope.query.limit;
				// TODO: Throw a validation error
				return;
			}
			if ($scope.query.limit !== limit) {
				$scope.query.limit = limit;
				$scope.query.offset = normalizeOffset($scope.query.offset, $scope.query.maxOffset, $scope.query.limit);
				$scope.refresh();
			}
		}

		// Override this to implement updates to an object
		$scope.refresh = function() {

			/*
			 * TODO: Inspect search string and add IPLIKE, integer, enum 
			 * constraints if the value is appropriate.
			 */

			var assetFields = [
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

			var categoryFields = [
				"category.description",
				"category.name"
			];

			var distPollerFields = [
				"distPoller.label",
				"distPoller.location"
			];

			var ipInterfaceFields = [
				'ipInterface.ipHostName'
			];

			var locationFields = [
				"location.locationName",
				"location.monitoringArea"
//				"location.priority"
			];

			var nodeFields = [
				"node.foreignId",
				"node.foreignSource",
				"node.label",
//				"node.labelSource",
				"node.netBiosDomain",
				"node.netBiosName",
				"node.operatingSystem",
				"node.sysContact",
				"node.sysDescription",
				"node.sysLocation",
				"node.sysName",
				"node.sysObjectId",
//				"node.type"
			];

			var serviceTypeFields = [
				"serviceType.name"
			];

			var alarmFields = [
//				"alarm.alarmType",
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
//				"lastEvent.eventTTicketState",
				"lastEvent.eventUei",
//				"lastEvent.ifIndex",
//				"lastEvent.ipAddr",
//				"snmpInterface.ifAdminStatus",
//				"snmpInterface.ifIndex",
//				"snmpInterface.ifOperStatus",
//				"snmpInterface.ifSpeed",
//				"snmpInterface.netMask"
			];
			// Add the asset fields
			Array.prototype.push.apply(alarmFields, assetFields);
			// Add the category fields
			Array.prototype.push.apply(alarmFields, categoryFields);
			// Add the distPoller fields
			Array.prototype.push.apply(alarmFields, distPollerFields);
			// Add the ipInterface fields
			Array.prototype.push.apply(alarmFields, ipInterfaceFields);
			// Add the location fields
			Array.prototype.push.apply(alarmFields, locationFields);
			// Add the node fields
			Array.prototype.push.apply(alarmFields, nodeFields);
			// Add the serviceType fields
			Array.prototype.push.apply(alarmFields, serviceTypeFields);

			var eventFields = [
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
//				"event.eventTTicketState",
				"event.eventUei",
//				"event.ifIndex",
//				"event.ipAddr"
			];
			// Add the asset fields
			Array.prototype.push.apply(eventFields, assetFields);
			// Add the category fields
			Array.prototype.push.apply(eventFields, categoryFields);
			// Add the distPoller fields
			Array.prototype.push.apply(eventFields, distPollerFields);
			// Add the ipInterface fields
			Array.prototype.push.apply(alarmFields, ipInterfaceFields);
			// Add the location fields
			Array.prototype.push.apply(eventFields, locationFields);
			// Add the node fields
			Array.prototype.push.apply(eventFields, nodeFields);
			// Add the serviceType fields
			Array.prototype.push.apply(eventFields, serviceTypeFields);

			if (typeof $scope.query.q === 'string' && $scope.query.q.length > 0) {
				$scope.nodes = [
					{ "ipAddress": "192.168.1.1", "nodeLabel": "nas.local" },
					{ "ipAddress": "192.168.1.9", "nodeLabel": "server.local" }
				];

				$scope.nodes = $scope.nodes.filter(function(item) {
					var keys = Object.keys(item);
					for (var i = 0; i < keys.length; i++) {
						var value = item[keys[i]];
						if (value.toLowerCase().match($scope.query.q.toLowerCase()) != null) {
							return true;
						}
					};
					return false;
				});

				var searchClauses = [];
				for (var i = 0; i < alarmFields.length; i++) {
					searchClauses.push({
						'property': alarmFields[i],
						'operator': 'EQ',
						'value': '*' + $scope.query.q + '*' // Substring query
					});
				}

				// Fetch all of the items
				alarmFactory.query(
					{
						_s: toFiql(searchClauses),
						limit: 10
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
					}
				);

				searchClauses = [];
				for (var i = 0; i < eventFields.length; i++) {
					searchClauses.push({
						'property': eventFields[i],
						'operator': 'EQ',
						'value': '*' + $scope.query.q + '*' // Substring query
					});
				}

				// Fetch all of the items
				eventFactory.query(
					{
						_s: toFiql(searchClauses),
						limit: 10
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
					}
				);
			} else {
				$scope.alarms = [];
				$scope.events = [];
				$scope.nodes = [];
			}
		}

		// Override this to implement updates to an object
		$scope.update = function() {
			$log.warn("You need to override $scope.$parent.update() in your controller");
		}

		// Override this to implement deletions
		$scope.deleteItem = function(item) {
			$log.warn("You need to override $scope.$parent.deleteItem() in your controller");
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
