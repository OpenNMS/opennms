const angular = require('vendor/angular-js');
require('../../lib/onms-http');
require('angular-ui-router');

const indexTemplate  = require('./index.html');
const templatesTemplate  = require('./templates.html');
const persistedtTemplate  = require('./persisted.html');
const schedulesTemplate  = require('./schedules.html');
const detailsTemplate  = require('./details.html');

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
                .state('report', {
                    url: '/report',
                    controller: 'ReportsController',
                    templateUrl: indexTemplate
                })
                .state('report.templates', {
                    url: '/templates',
                    controller: 'ReportTemplatesController',
                    templateUrl: templatesTemplate
                })
                .state('report.details', {
                    url: '/:id?type',
                    controller: 'ReportDetailController',
                    templateUrl: detailsTemplate,
                    resolve: {

                    }
                })
                .state('report.schedules', {
                    url: '/schedules',
                    controller: 'ReportSchedulesController',
                    templateUrl: schedulesTemplate
                })
                .state('report.persisted', {
                    url: '/persisted',
                    controller: 'ReportStorageController',
                    templateUrl: persistedtTemplate
                })
            ;
            $urlRouterProvider.otherwise('/report/templates');
        }])
        .factory('ReportsService', function($resource) {
            return $resource('rest/reports/:id', {id: '@id'},
                {
                    'list':             { method: 'GET', isArray: true },
                    'get':              { method: 'GET' },
                    'listScheduled':    { method: 'GET', isArray:true, url:'rest/reports/scheduled'}
                }
            );
        })
        .factory('ReportScheduleService', function($resource) {
            return $resource('rest/reports/scheduled/:id', {id: '@id'},
                {
                    'list':             { method: 'GET', isArray: true },
                    'get':              { method: 'GET' },
                    'delete':           { method: 'DELETE', params: {id: -1} /* to prevent accidentally deleting all */ },
                    'deleteAll':        { method: 'DELETE'},
                }
            );
        })
        .factory('ReportStorageService', function($resource) {
            return $resource('rest/reports/persisted/:id', {id: '@id'},
                {
                    'list':             { method: 'GET', isArray: true },
                    'get':              { method: 'GET' },
                    'delete':           { method: 'DELETE', params: {id: -1} /* to prevent accidentally deleting all */ },
                    'deleteAll':        { method: 'DELETE'},
                }
            );
        })
        .factory('UserService', function($resource) {
            var userResource = $resource('rest/users/whoami', {}, {'whoami': {method: 'GET' }});
            return {
                whoami: function(callback) {
                    return userResource.whoami(function(data) {
                       var user = {
                            id: data['user-id'],
                            name: data['full-name'],
                            email: data['email'],
                            roles: data['role'],

                            isAdmin: function() {
                                return this.roles.indexOf("ROLE_ADMIN") >= 0;
                            },

                            isReportDesigner: function() {
                                return this.roles.indexOf("ROLE_REPORT_DESIGNER") >= 0;
                            }
                        };
                       callback(user);
                    }, function(error) {
                        console.log("GLOBAL ERROR", error);
                    });
                }
            };
        })
        .controller('ReportsController', ['$scope', '$http', 'UserService', function($scope, $http, UserService) {
            UserService.whoami(function(user) {
                $scope.userInfo = user;
            });
        }])
        .controller('ReportTemplatesController', ['$scope', '$http', 'ReportsService', function($scope, $http, ReportsService) {
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
        .controller('ReportDetailController', ['$scope', '$http', '$window', '$state', '$stateParams', 'ReportsService', function($scope, $http, $window, $state, $stateParams, ReportsService) {
            $scope.type = $stateParams.type;
            $scope.reportId = $stateParams.id;

            // TODO MVR this is a duplicate of ReportOnlineController
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
                $scope.deliveryOptions = undefined;
                $scope.cronExpression = "0 */5 * * * ?";

                // TODO MVR when invoking the page without navigating properly, the userInfo is not set :(

                var requestParameters = {
                    id: $scope.reportId,
                    adhoc: $scope.type === 'online',
                    userId: $scope.type !== 'online' ? $scope.userInfo.id : undefined
                };

                ReportsService.get(requestParameters, function(response) {
                    $scope.loading = false;
                    $scope.surveillanceCategories = response.surveillanceCategories;
                    $scope.categories = response.categories;
                    $scope.formats = response.formats;
                    $scope.parameters = response.parameters;
                    $scope.deliveryOptions = response.deliveryOptions || {};
                    $scope.deliveryOptions.format = $scope.reportFormat;

                    console.log("XXXX", $scope.deliveryOptions);

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
            // TODO MVR it seems always pdf is generated even if csv was selected
            $scope.runReport = function() {
                $http({
                    method: 'POST',
                    url: 'rest/reports/' + $stateParams.id,
                    data:  {id:$scope.reportId, parameters: $scope.parameters, format: $scope.reportFormat},
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

            $scope.deliverReport = function() {
                // TODO MVR this is probably not the most elegant way of doing this (-:
                // $scope.deliveryOptions.format = $scope.reportFormat;
                $http({
                    method: 'POST',
                    url: 'rest/reports/persisted',
                    data: {
                        id: $scope.reportId,
                        parameters: $scope.parameters,
                        format: $scope.reportFormat,
                        deliveryOptions: $scope.deliveryOptions
                    }
                }).then(function(response) {
                    console.log("SUCCESS", response);
                }, function(response) {
                    console.log("ERROR", response);
                })
            };

            $scope.scheduleReport = function() {
                // TODO MVR this is probably not the most elegant way of doing this (-:
                // $scope.deliveryOptions.format = $scope.reportFormat;
                $http({
                    method: 'POST',
                    url: 'rest/reports/scheduled',
                    data: {
                        id: $scope.reportId,
                        parameters: $scope.parameters,
                        format: $scope.reportFormat,
                        deliveryOptions: $scope.deliveryOptions,
                        cronExpression: $scope.cronExpression
                    }
                }).then(function(response) {
                    console.log("SUCCESS", response);
                }, function(response) {
                    console.log("ERROR", response);
                })
            };

            $scope.execute = function() {
                console.log("EXECUTING ... :)", $scope);
                if ($scope.type === 'online') {
                    $scope.runReport();
                }
                if ($scope.type === 'schedule') {
                    $scope.scheduleReport();
                }
                if ($scope.type === 'deliver') {
                    $scope.deliverReport();
                }
            };

            $scope.loadDetails();
        }])
        .controller('ReportSchedulesController', ['$scope', '$http', '$window', '$stateParams', 'ReportScheduleService', function($scope, $http, $window, $stateParams, ReportScheduleService) {
            $scope.scheduledReports = [];

            console.log("schedule controller");
            $scope.refresh = function() {
                ReportScheduleService.list(function(data) {
                    $scope.scheduledReports = data;
                }, function(response) {
                    console.log("ERROR", response);
                });
            };

            $scope.deleteAll = function() {
                ReportScheduleService.deleteAll({}, function(response) {
                   $scope.refresh();
                }, function(response) {
                    console.log("ERROR", response);
                });
            };

            $scope.delete = function(schedule) {
                ReportScheduleService.delete({id: schedule.triggerName || -1}, function(response) {
                    $scope.refresh();
                }, function(response) {
                    console.log("ERROR", response);
                })
            };

            $scope.refresh();
        }])
        .controller('ReportStorageController', ['$scope', '$http', '$window', '$stateParams', 'ReportStorageService', function($scope, $http, $window, $stateParams, ReportStorageService) {
            $scope.persistedReports = [];

            console.log("persisted controller");
            $scope.refresh = function() {
                ReportStorageService.list(function(data) {
                    $scope.persistedReports = data;
                }, function(response) {
                    console.log("ERROR", response);
                });
            };

            $scope.deleteAll = function() {
                ReportStorageService.deleteAll({}, function(response) {
                    $scope.refresh();
                }, function(response) {
                    console.log("ERROR", response);
                });
            };

            $scope.delete = function(report) {
                ReportStorageService.delete({id: report.id || -1}, function(response) {
                    $scope.refresh();
                }, function(response) {
                    console.log("ERROR", response);
                })
            };


            $scope.refresh();
        }])
    ;
}());
