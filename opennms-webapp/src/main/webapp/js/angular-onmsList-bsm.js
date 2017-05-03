(function() {
    'use strict';

    var MODULE_NAME = 'onmsList.bsm';

    // $filters that can be used to create human-readable versions of filter values
    angular.module('bsmListFilters', [ 'onmsListFilters' ])
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

    angular.module(MODULE_NAME, [ 'ngResource', 'onmsList', 'bsmListFilters' ])
        .factory('BusinessServices', function($resource, $log, $http, $location) {
            return $resource(BASE_REST_URL + '/status/business-services/:id', {},
                {
                    'query': {
                        method: 'GET',
                        isArray: true,
                        // Append a transformation that will unwrap the item array
                        transformResponse: appendTransform($http.defaults.transformResponse, function(data, headers, status) {
                            if (status === 204) { // No content
                                return [];
                            }
                            return data.businessservices;
                        })
                    }
                }
            );
        })

        .controller('BusinessServicesListCtrl', ['$scope', '$location', '$window', '$log', '$filter', 'BusinessServices', function($scope, $location, $window, $log, $filter, BusinessServices) {
            $log.debug('BusinessServicesListCtrl initializing...');

            // Set the default sort and set it on $scope.$parent.query
            $scope.$parent.defaults.orderBy = 'id';
            $scope.$parent.query.orderBy = 'id';

            // $scope.newItem = {};

            // Reload all resources via REST
            $scope.$parent.refresh = function() {
                // Fetch all of the items
                BusinessServices.query(
                    {
                        _s: $scope.$parent.query.searchParam,
                        limit: $scope.$parent.query.limit,
                        offset: $scope.$parent.query.offset,
                        orderBy: $scope.$parent.query.orderBy,
                        order: $scope.$parent.query.order
                    },
                    function(value, headers) {
                        console.log(value);
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


            // Refresh the item list;
            $scope.$parent.refresh();

            $log.debug('BusinessServicesListCtrl initialized');
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
