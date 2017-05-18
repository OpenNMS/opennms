(function() {
    'use strict';

    var MODULE_NAME = 'onms.status';

    var toList = function(severityFilter) {
        var list = [];
        for (var property in severityFilter) {
            if (severityFilter.hasOwnProperty(property)) {
                var propertyValue = severityFilter[property];
                if (propertyValue === true) {
                    list.push(property);
                }
            }
        }
        return list;
    };

    angular.module(MODULE_NAME, [ 'ngRoute', 'ngResource', 'ui.checkbox', 'ui.bootstrap', 'onmsList' ])
        .filter('severity', function() {
            return function(input) {
                input = input || '';
                var out = '';
                if (input.length > 0) {
                    out = input.charAt(0).toUpperCase();
                }
                if (input.length > 1) {
                    out = out + input.substr(1, input.length).toLowerCase();
                }
                return out;
            }
        })
        .directive('filterControls', function() {
            return {
                restrict: 'E',
                templateUrl: 'status/template/filter.html',
                transclude: true,
            }
        })
        .directive('severityFilter', function() {
            return {
                restrict: 'E',
                scope: {
                    severity: '=model',
                },
                templateUrl: 'status/template/severity-filter.html',
                link: function($scope, elem, attr, ctrl) {
                    if ($scope.severity == null) {
                        $scope.severity = {};
                    }
                }
            }
        })
        .directive('pagination', function() {
            return {
                restrict: 'E',
                scope: {
                    query: '=model',
                },
                transclude: true,
                templateUrl: 'status/template/pagination-toolbar.html',
                link: function($scope, elem, attr, ctrl) {
                    $scope.setOffset = function(offset) {
                        offset = normalizeOffset(offset, $scope.query.maxOffset, $scope.query.limit);

                        if ($scope.query.offset !== offset) {
                            $scope.query.offset = offset;
                            $scope.refresh();
                        }
                    }
                }
            }
        })

        .factory('StatusService', function($resource, $log, $http, $location) {
            return $resource('api/v2/status/:type/:strategy', {},
                {
                    'query': { method: 'GET' }
                }
            );
        })

        .controller('StatusController', ['$scope', '$location', '$window', '$log', 'StatusService', function($scope, $location, $window, $log, StatusService) {

            $scope.setStrategy = function (newStrategy) {
                if (angular.equals($scope.query.strategy, newStrategy) == false) {
                    $scope.query.strategy = newStrategy;
                    $scope.refresh();
                }
            };

            $scope.setType = function (newType) {
                if (angular.equals($scope.query.type, newType) == false) {
                    $scope.query.type = newType;
                    $scope.refresh();
                }
            };

            $scope.resetFilter = function() {
                $scope.severityFilter = {};
            };

            $scope.updateFilterAndRefreshIfNecessary = function() {
                var newList = toList($scope.severityFilter);
                var oldList = $scope.query.severityFilter;

                // Otherwise only update if the severityFilter changed
                if (angular.equals(oldList, newList) === false) {
                    $scope.refresh();
                }
            };

            $scope.loadData = function(itemTransformer) {
                var parameters = $scope.query || {};

                parameters.severityFilter = [];
                for (var property in $scope.severityFilter) {
                    if ($scope.severityFilter.hasOwnProperty(property)) {
                        var propertyValue = $scope.severityFilter[property];
                        if (propertyValue === true) {
                            parameters.severityFilter.push(property);
                        }
                    }
                }
                $scope.query.severityFilter = parameters.severityFilter;

                StatusService.query(
                    {
                        severityFilter: parameters.severityFilter || [],
                        limit: parameters.limit || 20,
                        offset: (parameters.page -1) * parameters.limit || 0,
                        orderBy: parameters.orderBy,
                        order: parameters.order,
                        type: parameters.type,
                        strategy: parameters.strategy
                    },
                    function(data, headers) {
                        if (itemTransformer != null) {
                            $scope.items = itemTransformer(data);
                        } else {
                            $scope.items = data;
                        }

                        var contentRange = parseContentRange(headers('Content-Range'));
                        $scope.query.totalItems = contentRange.total;
                    },
                    function(response) {
                        switch(response.status) {
                            case 404:
                                // If we didn't find any elements, then clear the list
                                $scope.items = [];
                                $scope.query.lastOffset = 0;
                                $scope.query.maxOffset = 0;
                                $scope.query.offset = 0;
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

            $scope.refresh = function() {
                $log.info("Please overwrite in child controller");
            };

            // Default values
            $scope.items = [];
            $scope.query = {
                severityFilter: [],
                page: 1,
                limit: 20,
                totalItems: 0,
                orderBy: 'severity',
                order: 'desc'
            };
            $scope.query.strategy = $location.search().strategy;
            $scope.query.type = $location.search().type;
            $scope.severityFilter = {};

            // Update severityFilter based on url query
            if ($location.search().severityFilter != undefined) {
                $scope.severityFilter[$location.search().severityFilter] = true;
            }

            $scope.$watch('severityFilter', function() {
                $scope.updateFilterAndRefreshIfNecessary();
            }, true);
        }])

        .controller('BusinessServiceStatusController', ['$scope', '$controller', '$log', function($scope, $controller, $log) {
            $controller('StatusController', {$scope: $scope});
            $scope.refresh = function() {
                $scope.loadData(function(data) {
                    return data.businessservices;
                });
            };
            $scope.refresh();
        }])

        .controller('ApplicationStatusController', ['$scope', '$controller', '$log', function($scope, $controller, $log) {
            $controller('StatusController', {$scope: $scope});
            $scope.refresh = function() {
                $scope.loadData(function(data) {
                    return data.applications;
                });
            };

            $scope.refresh();
        }])

        .controller('NodeStatusController', ['$scope', '$controller', '$location', '$log', function($scope, $controller, $location, $log) {
            $controller('StatusController', {$scope: $scope});

            // Ensure type is either alarms or outages, otherwise fall back to "alarms"
            if ($scope.query.strategy === undefined || $scope.query.strategy != "alarms" && $scope.query.strategy != "outages") {
                $log.warn("Strategy was neither alarms nor outages. Falling back to alarms");
                $scope.query.strategy = "alarms";
            }

            $scope.refresh = function() {
                $scope.loadData(function(data) {
                    return data.nodes;
                });
            };

            $scope.refresh();
        }])

    ;

    angular.element(document).ready(function() {
        angular.bootstrap(document, [MODULE_NAME]);
    });
}());
