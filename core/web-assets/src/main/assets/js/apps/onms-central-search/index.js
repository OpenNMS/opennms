const angular = require('vendor/angular-js');
require('../../lib/onms-http');
require('angular-bootstrap-toggle/dist/angular-bootstrap-toggle');
require('angular-bootstrap-toggle/dist/angular-bootstrap-toggle.css');
require('angular-ui-router');

const quickSearchTemplate  = require('./quicksearch.html');

const globalErrorHandling = function(scope, errorResponse) {
    if (errorResponse.data) {
        scope.error = errorResponse.data;
    } else {
        scope.error = "An unexpected error occurred while handling the request";
    }
};

(function() {
    'use strict';

    var MODULE_NAME = 'onms.central.search';

    angular.module(MODULE_NAME, [
        'angular-loading-bar',
        'ngResource',
        'ui.router',
        'onms.http',
    ])
        .directive('onmsCentralSearch', function() {
            return {
                restrict: 'E',
                transclude: false,
                templateUrl: quickSearchTemplate,
                controller: 'QuickSearchController'
            };
        })
        .factory('SearchResource', function($resource) {
            return $resource('api/v2/search', {}, {
                'query':      { method: 'GET', isArray: true, cancellable: true },
            });
        })
        .controller('QuickSearchController', ['$scope', 'SearchResource', '$timeout', '$document', function($scope, SearchResource, $timeout, $document) {
            var KeyCodes = {
                ENTER: 13,
                SHIFT: 16,
                ESC: 27,
                KEY_LEFT: 37,
                KEY_UP: 38,
                KEY_RIGHT: 39,
                KEY_DOWN: 40,
            };

            var Types = {
                Group: 'Group',
                Item: 'Item',
                More: 'More'
            };

            $scope.query = '';
            $scope.results = {};
            $scope.performSearchExecuted = false;
            $scope.showLoadingIndicator = false;
            $scope.showLoadingIndicatorDelay = 250;
            $scope.performSearchDelay = 500; // in ms
            $scope.performSearchPromise = undefined;
            $scope.performSearchHandle = undefined;
            $scope.showLoadingIndicatorPromise = undefined;
            $scope.shiftLastPressed = undefined;
            $scope.selectedIndex = 0;

            $document.bind('mousedown', function(event) {
                var isChild = $('#onms-search-form').has(event.target).length > 0;
                var isSelf = $('#onms-search-form').is(event.target);
                var isInside = isChild || isSelf;
                if (!isInside) {
                    $timeout(function() {
                        $scope.resetQuery();
                        $scope.cancelRequest();
                    });
                }
            });

            $document.bind('keyup', function(e) {
                // Search Focus Field
                $timeout(function() {
                    if (e.keyCode === KeyCodes.SHIFT && new Date() - $scope.shiftLastPressed <= 350) {
                        angular.element('#onms-search-query').focus();
                        angular.element('#onms-search-query').select();
                        $scope.shiftLastPressed = undefined;
                    } else if(e.keyCode === KeyCodes.SHIFT) {
                        $scope.shiftLastPressed = new Date();
                    }

                    // Reset Search
                    if (e.keyCode === KeyCodes.ESC) {
                        $scope.resetQuery();
                        $scope.cancelRequest();
                    }
                });
            });

            $document.bind('keydown', function(e) {
                $timeout(function() {
                    if ($scope.results.length > 0) {
                        var element = document.getElementById('onms-search-result-item-' + $scope.selectedIndex);
                        if (e.keyCode === KeyCodes.KEY_UP || e.keyCode === KeyCodes.KEY_DOWN) {
                            $scope.navigateSearchResult(e.keyCode);

                            // Ideally we would use scrollToView(), but that will also scroll the body, which
                            // results in the header scrolling down slightly, which looks weird when using the search
                            // So instead scrolling is implemented manually
                            var parentComponent = document.getElementById('onms-search-result');
                            var parentHeight = parentComponent.clientHeight;
                            var resultHeight = element.clientHeight;
                            var resultOffset = element.offsetTop;
                            var padding = 25;

                            // Scroll down
                            if (resultOffset + resultHeight + padding >= parentHeight + parentComponent.scrollTop) {
                                parentComponent.scrollTop = resultOffset;
                            }
                            // Scroll up
                            if (parentComponent.scrollTop !== 0
                                && parentComponent.scrollTop > resultOffset - resultHeight) {
                                parentComponent.scrollTop = resultOffset - resultHeight;
                            }
                        }
                        if (e.keyCode === KeyCodes.ENTER) {
                            if ($scope.results[$scope.selectedIndex].type === Types.More) {
                                // Ensure next action is run in angular context
                                // Do not use angular.$apply here, as it may fail on angular sites,
                                // such as the requisition ui
                                $timeout(function() {
                                    angular.element(element).triggerHandler('click');
                                }, 0);
                            } else {
                                $scope.resetQuery();
                                $scope.cancelRequest();
                                element.click();
                            }
                        }
                    }
                });
            });

            $scope.navigateSearchResult = function(keyCode) {
                $scope.results[$scope.selectedIndex].selected = false;
                switch(keyCode) {
                    case KeyCodes.KEY_UP:
                        $scope.selectedIndex--;
                        break;
                    case KeyCodes.KEY_DOWN:
                        $scope.selectedIndex++;
                        break;
                    default:
                        break;
                }
                if ($scope.selectedIndex < 1) {
                    $scope.selectedIndex = 1;
                }
                if ($scope.selectedIndex >= $scope.results.length) {
                    $scope.selectedIndex = $scope.results.length - 1;
                }
                if ($scope.results[$scope.selectedIndex].type === Types.Group) {
                    $scope.navigateSearchResult(keyCode); // Skip group element
                } else {
                    $scope.results[$scope.selectedIndex].selected = true;
                }
            };

            $scope.resetQuery = function() {
                $scope.query = '';
                $scope.results = [];
                $scope.performSearchExecuted = false;
                if ($scope.performSearchHandle) {
                    $scope.performSearchHandle.$cancelRequest();
                }
            };

            $scope.cancelRequest = function() {
                if ($scope.performSearchHandle) {
                    $scope.performSearchHandle.$cancelRequest();
                }
                $scope.showLoadingIndicator = false;
                $timeout.cancel($scope.showLoadingIndicatorPromise);
            };

            // Ensure there is no difference between selected and mouseover
            $scope.select = function(index) {
                var selectIndex = index || 1;
                if ($scope.selectedIndex >= 1) {
                    $scope.results[$scope.selectedIndex].selected = false;
                }
                $scope.selectedIndex = selectIndex;
                $scope.results[$scope.selectedIndex].selected = true;
            };

            $scope.onQueryChange = function() {
                if ($scope.query.length == 0) {
                    $scope.resetQuery();
                    return;
                }
                if ($scope.query.length < 3) {
                    return;
                }

                // Stop any previous loading
                $timeout.cancel($scope.performSearchPromise);
                $scope.results = [];
                $scope.performSearchExecuted = false;

                // Start timeout before actually searching, this will allow for not invoking when the user
                // is still typing. Fiddle with $scope.loadingDelay to make it resolve faster
                $scope.performSearchPromise = $timeout(function() {
                    // Stop any previously started delay
                    $timeout.cancel($scope.showLoadingIndicatorPromise);

                    // Kick of loading indicator
                    $scope.showLoadingIndicatorPromise = $timeout(function() {
                        $scope.showLoadingIndicator = true;
                    }, $scope.showLoadingIndicatorDelay);

                    // Cancel any previous request
                    if ($scope.performSearchHandle) {
                        $scope.performSearchHandle.$cancelRequest();
                    }

                    // Kick of the search
                    $scope.error = undefined;
                    $scope.performSearchHandle = SearchResource.query({'_s' : $scope.query},
                        function(data) {
                            $scope.cancelRequest();
                            $scope.performSearchExecuted = true;

                            var results = [];
                            data.forEach(function(eachResult) {
                                // Create the header
                                results.push({
                                        context: eachResult.context.name,
                                        // Make the label have an s at the end if it has multiple items
                                        label: eachResult.results.length > 1 ? eachResult.context.name + 's' : eachResult.context.name,
                                        type: Types.Group,
                                        count: eachResult.results.length,
                                        more: eachResult.more
                                    }
                                );

                                eachResult.results.forEach(function(item) {
                                    item.type = Types.Item;
                                    results.push(item);
                                });

                                if (eachResult.more === true) {
                                    var showMoreElement = {
                                        context: eachResult.context.name,
                                        count: eachResult.results.length,
                                        type: Types.More,
                                        loadMore: function() {
                                            $scope.error = undefined;
                                            SearchResource.query({'_s': $scope.query, '_l': this.count + 10, '_c' : this.context}, function(response) {
                                                var endIndex = $scope.results.indexOf(showMoreElement);

                                                // The result is context focused, so there is only one search result anyways
                                                var searchResult = response[0];
                                                var results = searchResult.results.slice(endIndex - 1); // Remove first elements, as they are already being showed
                                                results.forEach(function(item, i) {
                                                    // Add item
                                                    item.type = Types.Item;
                                                    $scope.results.splice(endIndex + i, 0, item);
                                                    showMoreElement.count++;
                                                });
                                                // Toggle Selection
                                                showMoreElement.selected = false;
                                                $scope.results[$scope.selectedIndex].selected = true;

                                                // Hide element
                                                if (searchResult.more === false) {
                                                    $scope.results.splice($scope.results.indexOf(showMoreElement), 1);
                                                }
                                            }, function(response) {
                                                $scope.performSearchExecuted = true;
                                                globalErrorHandling($scope, response);
                                            });
                                        },
                                        selected: false
                                    };
                                    results.push(showMoreElement);
                                }
                            });
                            $scope.results = results;
                            if ($scope.results.length != 0) {
                                $scope.selectedIndex = 1;
                                $scope.results[$scope.selectedIndex].selected = true;
                            }
                        },
                        function(response) {
                            if (response.status >= 0) {
                                $scope.performSearchExecuted = true;
                                globalErrorHandling($scope, response);
                                $scope.cancelRequest();
                            } else {
                                // Request cancelled
                            }
                        }
                    );
                }, $scope.performSearchDelay);
            };
        }])
    ;
}());
