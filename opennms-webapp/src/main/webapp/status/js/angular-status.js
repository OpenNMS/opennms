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

    angular.module(MODULE_NAME, [ 'angular-loading-bar', 'ngRoute', 'ngResource', 'ui.checkbox', 'ui.bootstrap', 'onms.elementList' ])
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

            $scope.changeOrderBy = function(newOrderBy) {
                if ($scope.query.orderBy == newOrderBy) {
                    if ($scope.query.order == "asc") {
                        $scope.query.order = "desc";
                    } else {
                        $scope.query.order = "asc";
                    }
                } else {
                    $scope.query.orderBy = newOrderBy;
                    $scope.query.order = "asc";
                }
                $scope.refresh();
            };

            $scope.updateFilterAndRefreshIfNecessary = function() {
                var newList = toList($scope.severityFilter);
                var oldList = $scope.query.severityFilter;

                // Otherwise only update if the severityFilter changed
                if (angular.equals(oldList, newList) === false) {
                    $scope.query.page = 1;
                    $scope.refresh();
                }
            };

            $scope.updateLocation = function() {
                // Update URL parameters to make refresh work
                var location = $location.search();
                location['severityFilter'] =  $scope.query.severityFilter;
                location['strategy'] = $scope.query.strategy;
                $location.search(location);
            };

            $scope.loadData = function(itemTransformer) {
                var parameters = $scope.query || {};

                // update severity filter
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

                $scope.updateLocation();

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
                            case 500:
                                // on error, simply reset everything
                            case 404:
                                // If we didn't find any elements, then clear the list
                                $scope.items = [];
                                $scope.query.page = 1;
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
                var severityFilter = $location.search().severityFilter;
                if (typeof severityFilter === 'string') {
                    severityFilter = [severityFilter];
                }
                angular.forEach(severityFilter, function(severity) {
                    $scope.severityFilter[severity] = true;
                });
            }

            $scope.$watch('severityFilter', function() {
                $scope.updateFilterAndRefreshIfNecessary();
            }, true);

            $scope.$watch('query.page', function(newOffset, oldOffset) {
                if (newOffset !== oldOffset) {
                    $scope.refresh();
                }
            });
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
