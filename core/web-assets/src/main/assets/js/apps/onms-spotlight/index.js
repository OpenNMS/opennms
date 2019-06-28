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
                'query':      { method: 'GET', isArray: true, cancellable: true },
            });
        })
        .controller('QuickSearchController', ['$scope', 'SearchResource', '$timeout', '$resource', function($scope, SearchResource, $timeout, $resource) {
            console.log("QuickSearchController invoked");
            $scope.query = '';
            $scope.results = {};
            $scope.performSearchExecuted = false;
            $scope.showLoadingIndicator = false;
            $scope.showLoadingIndicatorDelay = 250;
            $scope.performSearchDelay = 500; // in ms
            $scope.performSearchPromise = undefined;
            $scope.performSearchHandle = undefined;
            $scope.showLoadingIndicatorPromise = undefined;

            $scope.resetQuery = function() {
                console.log("Reset input search query");
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
                    $scope.performSearchHandle = SearchResource.query({'_s' : $scope.query},
                        function(data) {
                            console.log('Search result', data);
                            $scope.cancelRequest();
                            $scope.performSearchExecuted = true;

                            var results = [];
                            data.forEach(function(eachResult) {
                                // Create the header
                                results.push({
                                        context: eachResult.context.name,
                                        // Make the label have an s at the end if it has multiple items
                                        label: eachResult.results.length > 1 ? eachResult.context.name + 's' : eachResult.context.name,
                                        group: true,
                                        count: eachResult.results.length,
                                        totalCount: eachResult.totalCount
                                    }
                                );

                                eachResult.results.forEach(function(item) {
                                    item.group = false; // result cannot be a group
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
                                });
                            });
                            $scope.results = results;
                        },
                        function(response) {
                            if (response.status >= 0) {
                                // TODO MVR error handling
                                console.log('ERROR', error);
                                $scope.cancelRequest();
                            } else {
                                console.log("CANCELLED");
                            }
                        }
                    );
                }, $scope.performSearchDelay);
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
