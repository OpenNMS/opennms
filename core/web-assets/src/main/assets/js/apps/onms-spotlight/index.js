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
        .config(['$locationProvider', function ($locationProvider) {
            $locationProvider.hashPrefix('!');
            $locationProvider.html5Mode(false);
        }])
        .config(['$stateProvider', '$urlRouterProvider', function ($stateProvider, $urlRouterProvider) {
            $stateProvider
                .state('quicksearch', {
                    url: '/quicksearch',
                    controller: 'QuickSearchController',
                    templateUrl: quickSearchTemplate
                })
            ;
            $urlRouterProvider.otherwise('quicksearch');
        }])

        .factory('SearchResource', function($resource) {
            return $resource('api/v2/spotlight', {}, {
                'query':      { method: 'GET', isArray: true },
            });
        })

        .controller('QuickSearchController', ['$scope', 'SearchResource', function($scope, SearchResource) {
            $scope.query = "";
            $scope.searchExecutedOnce = false;
            $scope.results = {};
            $scope.loading = false;

            $scope.search = function() {
                console.log("Search method invoked");
                if ($scope.query.length >= 3) {
                    $scope.loading = true;
                    return SearchResource.query({'_s' : $scope.query},
                        function(data) {
                            console.log("Search result", data);
                            $scope.searchExecutedOnce = true;
                            $scope.emptyResult = data.length === 0;
                            $scope.results = {};

                            data.forEach(function(item) {
                                if (!$scope.results[item.context]) {
                                    $scope.results[item.context] = [];
                                }
                                // Group by context
                                $scope.results[item.context].push(item);

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
                                })
                            });
                            $scope.loading = false;
                            console.log("Search grouped", $scope.results);
                        },
                        function(error) {
                            console.log("ERROR", error);
                        }
                    );
                } else {
                    console.log("Reset Search");
                    $scope.results = {};
                }

            };

            function hashCode(s) {
                for(var i = 0, h = 0; i < s.length; i++)
                    h = Math.imul(31, h) + s.charCodeAt(i) | 0;
                return h;
            }

            $scope.classes = ['primary', 'secondary', 'info', 'dark'];
            $scope.getClassesForMatch = function(match) {
                var hash = hashCode(match.id);
                var index = hash % $scope.classes.length;
                return "badge-" + $scope.classes[index];
            }
        }])
    ;
}());
