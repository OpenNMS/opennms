const angular = require('vendor/angular-js');
require('../../lib/onms-http');
require('angular-ui-router');

const indexTemplate  = require('./index.html');
const onlineTemplate  = require('./online-report.html');
const templatesTemplate  = require('./templates.html');
const persistedtTemplate  = require('./persisted.html');
const schedulesTemplate  = require('./schedules.html');

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
                .state('report.templates.online', {
                    url: ':id/online',
                    controller: 'ReportOnlineController',
                    templateUrl: onlineTemplate
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
            $scope.users =  [{id: 'admin', label:'Administrator'}, {id:'user', label:'Normal User'}, {id: 'report_designer', label:'Report Designer'}];
            UserService.whoami(function(user) {
                console.log("User has been loaded", user);
                $scope.userInfo = user;
                $scope.currentUser = $scope.users[0];
                $scope.onChange();
            });

            $scope.onChange = function() {
                console.log("onChange invoked...");

                $scope.userInfo.id = $scope.currentUser.id;
                $scope.userInfo.name = $scope.currentUser.label;
                $scope.userInfo.comment = undefined;
                $scope.userInfo.email = $scope.currentUser.id + "@opennms.org";
                $scope.userInfo.roles = [];

                if ($scope.currentUser.id === "admin") {
                    $scope.userInfo.roles.push("ROLE_ADMIN");
                }
                if ($scope.currentUser.id === "user") {
                    $scope.userInfo.roles.push("ROLE_USER");
                }
                if ($scope.currentUser.id === "report_designer") {
                    $scope.userInfo.roles.push("ROLE_USER");
                    $scope.userInfo.roles.push("ROLE_REPORT_DESIGNER");
                }
                console.log("user changed", $scope.userInfo);
            };
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
        .controller('ReportOnlineController', ['$scope', '$http', '$window', '$stateParams', 'ReportsService', function($scope, $http, $window, $stateParams, ReportsService) {
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
