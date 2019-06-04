const angular = require('vendor/angular-js');
require('../../lib/onms-http');
require('angular-ui-router');

const indexTemplate  = require('./index.html');
const onlineTemplate  = require('./online-report.html');

(function() {
    'use strict';

    var MODULE_NAME = 'onms.reports';

    angular.module(MODULE_NAME, [
            'angular-loading-bar',
            'ngResource',
            'ui.router',
            'onms.http',
        ])
        .config( ['$locationProvider', function ($locationProvider) {
            $locationProvider.hashPrefix('!');
            $locationProvider.html5Mode(false);
        }])
        .config(['$stateProvider', '$urlRouterProvider', function ($stateProvider, $urlRouterProvider) {
            $stateProvider
                .state('home', {
                    url: '/list',
                    controller: 'ReportsController',
                    templateUrl: indexTemplate
                })
                .state('online', {
                    url: '/:id/online',
                    controller: 'ReportsOnlineController',
                    templateUrl: onlineTemplate
                });
            $urlRouterProvider.otherwise('/list');
        }])
        .factory('ReportsService', function($resource) {
            return $resource('rest/reports/:id', {id: '@id'},
                {
                    'list':      { method: 'GET', isArray: true },
                    'get':       { method: 'GET' },
                }
            );
        })
        .controller('ReportsController', ['$scope', '$http', 'ReportsService', function($scope, $http, ReportsService) {
            $scope.refresh = function() {
                $scope.reports = [];
                $scope.globalError = undefined;

                ReportsService.list(function(response) {
                    console.log("RESULT", response);
                    if (response && Array.isArray(response)) {
                        $scope.reports = response;
                    }
                }, function(errorResponse) {
                    console.log("ERROR", errorResponse);
                    $scope.globalError = "ERROR OCCURRED :(";
                })
            };

            $scope.refresh();
        }])
        .controller('ReportsOnlineController', ['$scope', '$http', '$window', '$stateParams', 'ReportsService', function($scope, $http, $window, $stateParams, ReportsService) {
            $scope.loadDetails = function() {
                $scope.loading = true;
                $scope.loading = false;
                $scope.surveillanceCategories = [];
                $scope.categories = [];
                $scope.formats = [];
                $scope.reportFormat = "PDF";
                $scope.parameters = [];
                $scope.endpoints = [];
                $scope.dashboards = [];

                ReportsService.get({id:$stateParams.id}, function(response) {
                    $scope.loading = false;
                    $scope.surveillanceCategories = response.surveillanceCategories;
                    $scope.categories = response.categories;
                    $scope.formats = response.formats;
                    $scope.parameters = response.parameters;

                    // In order to have the ui look the same as before, just order the parameters
                    var order = ['string', 'integer', 'float', 'double', 'date'];
                    $scope.parameters.sort(function(left, right) {
                        return order.indexOf(left.type) - order.indexOf(right.type);
                    });

                    console.log("SUCCESS", response);
                }, function(response) {
                    $scope.loading = false;
                    console.log("ERROR", response);
                });
            };

            // TODO MVR use ReportsService for this, but somehow only $http works :-/
            $scope.runReport = function() {
                $http({
                    method: 'POST',
                    url: 'rest/reports/' + $stateParams.id,
                    data:  {id:$stateParams.id, parameters: $scope.parameters, format: $scope.reportFormat},
                    responseType:  'arraybuffer'
                }).then(function (response) {
                    console.log("SUCCESS", response);
                    var data = response.data;
                    var fileBlob = new Blob([data], {type: $scope.reportFormat === 'PDF' ? 'application/pdf' : 'text/csv'});
                    var fileURL = URL.createObjectURL(fileBlob);
                    var contentDisposition = response.headers("Content-Disposition");
                    // var filename = (contentDisposition.split(';')[1].trim().split('=')[1]).replace(/"/g, '');
                    console.log(contentDisposition);
                    var filename = $stateParams.id + '.' + $scope.reportFormat.toLowerCase();

                    var a = document.createElement('a');
                    document.body.appendChild(a);
                    a.style = 'display: none';
                    a.href = fileURL;
                    a.download = filename;
                    a.click();
                    window.URL.revokeObjectURL(url);
                    document.body.removeChild(a);
                },
                function(error) {
                    console.log("ERROR", error);
                });
            };

            $scope.loadDetails();

        }])
    ;
}());
