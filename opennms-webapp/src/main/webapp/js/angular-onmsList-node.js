(function() {
    'use strict';

    var MODULE_NAME = 'onmsList.node';

    // $filters that can be used to create human-readable versions of filter values
    angular.module('nodeListFilters', [ 'onmsListFilters' ])
        .filter('property', function() {
            return function(input) {
                switch (input) {
                    case 'severity':
                        return 'Severity';
                }
                // If no match, return the input
                return input;
            }
        })
        .filter('value', function($filter) {
            return function(input, property) {
                switch (property) {
                    // There is no special formatting
                }
                return input;
            }
        });

    angular.module(MODULE_NAME, [ 'ngResource', 'onmsList', 'nodeListFilters' ])
        .factory('Nodes', function($resource, $log, $http, $location) {
            return $resource(BASE_REST_URL + '/status/nodes/:type', {},
                {
                    'query': {
                        method: 'GET',
                        isArray: true,
                        // Append a transformation that will unwrap the item array
                        transformResponse: appendTransform($http.defaults.transformResponse, function(data, headers, status) {
                            if (status === 204) { // No content
                                return [];
                            }
                            return data.nodes;
                        })
                    }
                }
            );
        })

        .controller('NodesListCtrl', ['$scope', '$location', '$window', '$log', '$filter', 'Nodes', function($scope, $location, $window, $log, $filter, Nodes) {
            $log.debug('NodesListCtrl initializing...');

            // Ensure type is either alarms or outages, otherwise fall back to "alarms"
            $scope.type = $location.search().type;
            if ($scope.type === undefined || $scope.type != "alarms" && $scope.type != "outages") {
                $scope.type = "alarms";
            }
            $scope.setType = function(newType) {
                $scope.type = newType;
                $scope.$parent.refresh();
            };

            $scope.$parent.defaults.orderBy = 'severity';
            $scope.$parent.defaults.order = 'desc';
            $scope.$parent.query.orderBy = 'severity';
            $scope.$parent.query.order = 'desc';

            // Reload all resources via REST
            $scope.$parent.refresh = function() {
                // Fetch all of the items
                Nodes.query(
                    {
                        _s: $scope.$parent.query.searchParam,
                        limit: $scope.$parent.query.limit,
                        offset: $scope.$parent.query.offset,
                        orderBy: $scope.$parent.query.orderBy,
                        order: $scope.$parent.query.order,
                        type: $scope.type
                    },
                    function(value, headers) {
                        $scope.$parent.items = value;

                        var contentRange = parseContentRange(headers('Content-Range'));
                        $scope.$parent.query.lastOffset = contentRange.end;
                        // Subtract 1 from the value since offsets are zero-based
                        $scope.$parent.query.maxOffset = contentRange.total - 1;
                        $scope.$parent.query.offset = normalizeOffset(contentRange.start, $scope.$parent.query.maxOffset, $scope.$parent.query.limit);
                    },
                    function(response) {
                        switch(response.status) {
                            case 404:
                                // If we didn't find any elements, then clear the list
                                $scope.$parent.items = [];
                                $scope.$parent.query.lastOffset = 0;
                                $scope.$parent.query.maxOffset = -1;
                                $scope.$parent.setOffset(0);
                                break;
                            case 401:
                            case 403:
                                // Handle session timeout by reloading page completely
                                $window.location.href = $location.absUrl();
                                break;
                        }
                    }
                );
            };

            // Update the severity
            $scope.setSeverityFilter = function(searchClause) {
                $scope.$parent.query.searchClauses = [];
                $scope.$parent.query.searchParam = '';
                $scope.$parent.addSearchClause(searchClause);
            };

            // set clause
            if ($scope.$parent.query.searchClauses.length > 0) {
                var clause = $scope.$parent.query.searchClauses[0];
                var clone = {
                    property: clause.property,
                    value: clause.value,
                    operator: clause.operator
                };
                $scope.clause = clone;
            }

            // Refresh the item list;
            $scope.$parent.refresh();

            $log.debug('NodesListCtrl initialized');
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
