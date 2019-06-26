const angular = require('vendor/angular-js');
require('../../lib/onms-http');
require('angular-bootstrap-toggle/dist/angular-bootstrap-toggle');
require('angular-bootstrap-toggle/dist/angular-bootstrap-toggle.css');
require('angular-ui-router');

const quickSearchTemplate  = require('./quicksearch.html');

(function() {
    'use strict';

    var MODULE_NAME = 'onms.spotlight';

    angular.module(MODULE_NAME, [
        'angular-loading-bar',
        'ngResource',
        'ui.router',
        'onms.http',
    ])
        .directive('onmsSpotlight', function() {
            return {
                restrict: 'E',
                transclude: false,
                templateUrl: quickSearchTemplate,
                controller: 'QuickSearchController'
            };
        })
        .factory('SearchResource', function($resource) {
            return $resource('api/v2/spotlight', {}, {
                'query':      { method: 'GET', isArray: true },
            });
        })
        .controller('QuickSearchController', ['$scope', 'SearchResource', '$timeout', function($scope, SearchResource, $timeout) {
            console.log("QuickSearchController invoked");
            $scope.query = '';
            $scope.results = {};
            $scope.performSearchExecuted = false;
            $scope.showLoadingIndicator = false;
            $scope.showLoadingIndicatorDelay = 250;
            $scope.performSearchDelay = 100; // in ms
            $scope.performSearchPromise = undefined;
            $scope.showLoadingIndicatorPromise = undefined;

            $scope.onQueryChange = function() {
                if ($scope.query.length == 0) {
                    $scope.results = [];
                    $scope.performSearchExecuted = false;
                    console.log("Query reset");
                    return;
                }
                if ($scope.query.length < 3) {
                    return;
                }

                // Stop any previous loading
                if ($scope.performSearchPromise) {
                    $timeout.cancel($scope.performSearchPromise);
                }
                // Start timeout before actually searching, this will allow for not invoking when the user
                // is still typing. Fiddle with $scope.loadingDelay to make it resolve faster
                $scope.performSearchPromise = $timeout(function() {
                    // Stop any previously started delay
                    if ($scope.showLoadingIndicatorPromise) {
                        $timeout.cancel($scope.showLoadingIndicatorPromise);
                    }
                    // Kick of loading indicator
                    $scope.showLoadingIndicatorPromise = $timeout(function() {
                        $scope.showLoadingIndicator = true;
                    }, $scope.showLoadingIndicatorDelay);

                    // Kick of the search
                    $scope.performSearch($scope.query).$promise.then(function(x) {
                        $scope.showLoadingIndicator = false;
                        $timeout.cancel($scope.showLoadingIndicatorPromise);
                    }, function(error) {
                        // TODO MVR add error handling
                        console.log("ERROR", error);
                        $scope.showLoadingIndicator = false;
                        $timeout.cancel($scope.showLoadingIndicatorPromise);
                    });

                }, $scope.performSearchDelay);
            };

            $scope.performSearch = function(query) {
                return SearchResource.query({'_s' : query},
                    function(data) {
                        console.log('Search result', data);
                        var results = [];
                        var lastContext;

                        data.forEach(function(item) {
                            // Create a "menu seperator"
                            if (lastContext === undefined || item.context !== lastContext.context) {
                                lastContext = {
                                    context: item.context,
                                    label: item.context,
                                    group: true,
                                    count: 0
                                };
                                results.push(lastContext);
                            }

                            // An item cannot be a group
                            item.group = false;

                            // Now add the item
                            results.push(item);

                            // TODO MVR we first create this, and now we undo this, should be different
                            var matches = item.matches;
                            item.matches = [];
                            matches.forEach(function(eachMatch) {
                                eachMatch.values.forEach(function(eachValue) {
                                    item.matches.push({
                                        id: eachMatch.id,
                                        label: eachMatch.label,
                                        value: eachValue
                                    });
                                });
                            });
                            lastContext.count++;
                        });
                        $scope.performSearchExecuted = true;
                        $scope.results = results;
                    },
                    function(error) {
                        // TODO MVR error handling
                        console.log('ERROR', error);
                    }
                );
            };

            function hashCode(s) {
                var h = 0;
                for(var i = 0; i < s.length; i++) {
                    h = Math.imul(31, h) + s.charCodeAt(i) | 0;
                }
                return h;
            }

            $scope.classes = ['primary', 'secondary', 'info', 'dark'];
            $scope.getClassForMatch = function(match) {
                var hash = hashCode(match.id);
                var index = Math.abs(hash) % $scope.classes.length;
                return 'badge-' + $scope.classes[index];
            }
        }])
    ;
}());
