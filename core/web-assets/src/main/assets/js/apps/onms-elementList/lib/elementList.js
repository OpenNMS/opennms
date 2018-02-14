const MODULE_NAME = 'onms.elementList';

/**
 * ISO-8601 date format string.
 */
window.ISO_8601_DATE_FORMAT = 'yyyy-MM-ddTHH:mm:ss.sssZ';
window.ISO_8601_DATE_FORMAT_WITHOUT_MILLIS = 'yyyy-MM-ddTHH:mm:ssZ';

const angular = require('vendor/angular-js');
const jQuery = require('vendor/jquery-js');
require('vendor/bootstrap-js');

const editListInPlaceTemplate = require('../templates/angular-onms-elementList-editListInPlace.html');
const editMapInPlaceTemplate = require('../templates/angular-onms-elementList-editMapInPlace.html');

/**
 * Function used to append an extra transformer to the default $http transforms.
 */
const appendTransform = (defaultTransform, transform) => {
	const t = angular.isArray(defaultTransform) ? defaultTransform : [ defaultTransform ];
	return t.concat(transform);
};

/**
 * Convert from a clause into a FIQL query string.
 */
const toFiql = (clauses) => {
	let first = true;
	let fiql = '';
	for (let i = 0; i < clauses.length; i++) {
		if (!first) {
			fiql += ';';
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
		default:
			// otherwise, do nothing
			break;
		}

		fiql += escapeSearchValue(clauses[i].value);

		first = false;
	}
	return fiql;
};

/**
 * Convert from a FIQL query string into separate clause objects.
 * This only works for simple queries composed of multiple AND (';')
 * clauses.
 * 
 * TODO: Expand this to cover more FIQL syntax
 */
const fromFiql = (fiql) => {
	let statements = fiql.split(';');
	let segments = [];
	let clauses = [];
	for (let i = 0; i < statements.length; i++) {
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
};


/**
 * Parse an HTTP Content-Range header into the start, end, and total fields.
 * The header should be in a format like: "items 0-14/28".
 * 
 * @param contentRange String from the Content-Range header
 */
const parseContentRange = (contentRange) => {
	if (!contentRange) {
		return {start: 0, end: 0, total: 0};
	}
	// Example: items 0-14/28
	const pattern = /items\s+?(\d+)\s*-\s*(\d+)\s*\/\s*(\d+)/;
	return {
		start: Number(contentRange.replace(pattern, '$1')),
		end: Number(contentRange.replace(pattern, '$2')),
		total: Number(contentRange.replace(pattern, '$3'))
	};
};

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
 const escapeSearchValue = (value) => {
    if (typeof value === 'string') {
        return value
            .replace('!', '%21')
            .replace('$', '%24')
            .replace('\'', '%27')
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
    }
    return value;
};

const normalizeOffset = (offset, maxOffset, limit) => {
	let newOffset = offset;

	// Offset of the last page
	let lastPageOffset;
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
};

/* // BMR: this doesn't seem to actually be used anywhere
String.prototype.endsWith = function(suffix) {
	return this.indexOf(suffix, this.length - suffix.length) !== -1;
};
*/

// $filters that can be used to create human-readable versions of filter values
angular.module('onmsListFilters', [])
.filter('operator', function() {
	return function(input, value) {
		// See if the string contains a wildcard
		var fuzzy = (typeof value === 'string' && value.indexOf('*') > -1);

		switch (input) {
		case 'EQ':
			return fuzzy ? 'is like' : 'equals';
		case 'NE':
			return fuzzy ? 'is not like' : 'does not equal';
		case 'LT':
			return 'is less than';
		case 'LE':
			return 'is less than or equal';
		case 'GT':
			return 'is greater than';
		case 'GE':
			return 'is greater than or equal';
		default:
			// If no match, return the input
			return input;
		}
	}
})
.filter('isNotEmpty', function() {
	return function(input) {
		return input &&
			(typeof input === 'string' ? input.trim() !== '' : true);
	}
})
;

// List module
angular.module(MODULE_NAME, [])

.config(function($locationProvider) {
	$locationProvider.html5Mode({
		// Use HTML5 
		enabled: true,
		// Don't rewrite all <a> links on the page
		rewriteLinks: false
	});
})

.directive('onmsListEditInPlace', function() {
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
		templateUrl: editListInPlaceTemplate,
		transclude: true
	};
})

.directive('onmsListEditListInPlace', function($window) {
	return {
		controller: function($scope) {
			$scope.editing = false;

			// Start editing the value
			$scope.edit = function() {
				$scope.editing = true;
			}

			// Stop editing the value
			$scope.unedit = function() {
				// Undo any edits
				$scope.newValue = null;
				$scope.editing = false;
			}

			$scope.onKeyup = function(event) {
				switch(event.keyCode) {
				// If the user types Enter, then submit the edit
				case 13: $scope.add($scope.values, $scope.newValue); break;
				// If the user types ESC, then abort the edit
				case 27: $scope.unedit(); break;
				// otherwise, do nothing
				default: break;
				}
			}

			$scope.confirmAndRemove = function(items, item) {
				// Splice the value out of the array
				if ($window.confirm('Are you sure you want to remove "' + item + '"?')) {
					items.splice(items.indexOf(item), 1);
					$scope.onEdit();
				}
			}

			$scope.add = function(items, item) {
				items.push(item);
				items.sort();
				// TODO: Handle update failures
				$scope.onEdit();
				// Switch out of edit mode
				$scope.unedit();
			}
		},
		// Use an isolated scope
		scope: {
			values: '=',
			valueType: '=',
			// Optional step attribute for number fields
			step: '=',
			onEdit: '&onEdit'
		},
		templateUrl: editListInPlaceTemplate,
		transclude: true
	};
})

.directive('onmsListEditMapInPlace', function($window) {
	return {
		controller: function($scope) {
			$scope.editing = false;

			// Start editing the value
			$scope.edit = function() {
				$scope.editing = true;
			}

			// Stop editing the value
			$scope.unedit = function() {
				// Undo any edits
				$scope.newKey = null;
				$scope.newValue = null;
				$scope.editing = false;
			}

			$scope.onKeyup = function(event) {
				switch(event.keyCode) {
				// If the user types Enter, then submit the edit
				case 13: $scope.add($scope.values, $scope.newKey, $scope.newValue); break;
				// If the user types ESC, then abort the edit
				case 27: $scope.unedit(); break;
				// otherwise, do nothing
				default: break;
				}
			}

			$scope.confirmAndRemove = function(items, key) {
				// Splice the value out of the array
				if ($window.confirm('Are you sure you want to remove "' + key + '"?')) {
					delete items[key];
					$scope.onEdit();
				}
			}

			$scope.add = function(items, key, value) {
				items[key] = value;
				// TODO: Handle update failures
				$scope.onEdit();
				// Switch out of edit mode
				$scope.unedit();
			}
		},
		// Use an isolated scope
		scope: {
			values: '=',
			keyType: '=',
			valueType: '=',
			// Optional step attribute for number fields
			step: '=',
			onEdit: '&onEdit'
		},
		templateUrl: editMapInPlaceTemplate,
		transclude: true
	};
})

/**
 * Generic list controller
 */
.controller('ListCtrl', ['$scope', '$location', '$window', '$log', '$filter', function($scope, $location, $window, $log, $filter) {
	$log.debug('ListCtrl initializing...');

	$scope.defaults = {
		_s: '',
		searchClauses: [],
		limit: 20,
		offset: 0,
		orderBy: '',
		order: 'asc'
	}

	var initialLimit = typeof $location.search().limit === 'undefined' ? $scope.defaults.limit : (Number($location.search().limit) > 0 ? Number($location.search().limit) : $scope.defaults.limit);

	// Restore any query parameters that you can from the 
	// query string, blank out the rest
	$scope.query = {
		lastOffset: 0,
		maxOffset: 0,

		searchParam: typeof $location.search()._s === 'undefined' ? $scope.defaults._s : $location.search()._s,
		searchClauses: typeof $location.search()._s === 'undefined' ? $scope.defaults.searchClauses : fromFiql($location.search()._s),
		limit: initialLimit,
		newLimit: initialLimit,
		offset: typeof $location.search().offset === 'undefined' ? $scope.defaults.offset : (Number($location.search().offset) > 0 ? normalizeOffset(Number($location.search().offset), Number.MAX_VALUE, initialLimit) : $scope.defaults.offset),

		// TODO: Validate that the orderBy is in a list of supported properties
		orderBy: typeof $location.search().orderBy === 'undefined' ? $scope.defaults.orderBy : $location.search().orderBy,
		order: typeof $location.search().order === 'undefined' ? $scope.defaults.order : ($location.search().order === 'asc' ? 'asc' : 'desc')
	};

	// Sync the query hash with the $location query string
	$scope.$watch('query', function() {
		var queryParams = angular.copy($scope.query);

		// Delete derived values that we don't need in the query string
		delete queryParams.searchClauses;
		delete queryParams.newLimit;
		delete queryParams.lastOffset;
		delete queryParams.maxOffset;

		// Rename searchParam to _s
		queryParams._s = (queryParams.searchParam === '' ? null : queryParams.searchParam);
		delete queryParams.searchParam;

		// Delete any parameters that have default or blank values
		if (queryParams.limit === $scope.defaults.limit || queryParams.limit === '') { delete queryParams.limit; }
		if (queryParams.offset === $scope.defaults.offset || queryParams.offset === '') { delete queryParams.offset; }
		if (queryParams.orderBy === $scope.defaults.orderBy || queryParams.orderBy === '') { delete queryParams.orderBy; }
		if (queryParams.order === $scope.defaults.order || queryParams.order === '') { delete queryParams.order; }
		if (queryParams._s === $scope.defaults._s || queryParams._s === '') { delete queryParams._s; }

		$location.search(queryParams);
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
		if ($scope.getSearchClause(clause)) {
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
		return a.property === b.property &&
			a.operator === b.operator &&
			a.value === b.value;
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
		if (!scopeOldClause) {
			if (!scopeNewClause) {
				// If the old clause is not present, simply add the new clause
				$scope.addSearchClause(newClause);
			} else {
				// If the old clause is not present and the new clause is already
				// present, then do nothing
			}
		} else {
			if (!scopeNewClause) {
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
		const o = normalizeOffset(offset, $scope.query.maxOffset, $scope.query.limit);

		if ($scope.query.offset !== o) {
			$scope.query.offset = o;
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
		$log.warn('You need to override $scope.$parent.refresh() in your controller');
	}

	// Override this to implement updates to an object
	$scope.update = function() {
		$log.warn('You need to override $scope.$parent.update() in your controller');
	}

	// Override this to implement deletions
	$scope.deleteItem = function(item) {
		$log.warn('You need to override $scope.$parent.deleteItem() in your controller');
	}

	$log.debug('ListCtrl initialized');
}])

.run(['$rootScope', '$log', function($rootScope, $log) {
	$log.debug('Finished initializing ' + MODULE_NAME);
}])

;

/*
angular.element(document).ready(function() {
	console.log('Bootstrapping ' + MODULE_NAME);
	angular.bootstrap(document, [MODULE_NAME]);
});
*/

module.exports = {
	appendTransform: appendTransform,
	toFiql: toFiql,
	fromFiql: fromFiql,
	parseContentRange: parseContentRange,
	escapeSearchValue: escapeSearchValue,
	normalizeOffset: normalizeOffset
};
