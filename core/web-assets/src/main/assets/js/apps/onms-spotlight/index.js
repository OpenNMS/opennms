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

            $scope.handleGlobalError = function(errorResponse) {
                $scope.globalError = "An unexpected error occurred: " + errorResponse.statusText;
                $scope.globalErrorDetails = JSON.stringify(errorResponse, null, 2);
            };

            $scope.query = "";
            $scope.searchExecutedOnce = false;
            $scope.results = {};
            $scope.data = {
                "id": [1,2,3,4,5],
                "project": "wewe2012",
                "date": "2013-02-26",
                "description": "ewew",
                "eet_no": "ewew",
            };
            console.log($scope.data);

            $scope.search = function() {
                console.log("Search method invoked");
                if ($scope.query.length >= 1) {
                    SearchResource.query(
                            {
                                '_s' : $scope.query
                            },
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
                            });
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
        }])
    ;
}());
