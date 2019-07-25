const angular = require('vendor/angular-js');
require('../../lib/onms-http');
require('../../lib/onms-datetimepicker');
require('../../lib/onms-schedule-editor');
require('angular-ui-router');
require('angular-bootstrap-confirm');

const indexTemplate  = require('./index.html');
const templatesTemplate  = require('./templates.html');
const persistedtTemplate  = require('./persisted.html');
const schedulesTemplate  = require('./schedules.html');
const detailsTemplate  = require('./details.html');
const successModalTemplate  = require('./modals/success-modal.html');
const errorModalTemplate  = require('./modals/error-modal.html');

const confirmTopoverTemplate = require('../onms-classifications/views/modals/popover.html');

(function() {
    'use strict';

    var MODULE_NAME = 'onms.reports';

    angular.module(MODULE_NAME, [
            'angular-loading-bar',
            'ngResource',
            'ui.router',
            'mwl.confirm',
            'onms.http',
            'onms.datetimepicker',
            'onms.schedule.editor'
        ])
        .config( ['$locationProvider', function ($locationProvider) {
            $locationProvider.hashPrefix('!');
            $locationProvider.html5Mode(false);
        }])
        .run(function(confirmationPopoverDefaults) {
            confirmationPopoverDefaults.templateUrl = confirmTopoverTemplate;
        })
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
                .state('report.templates.details', {
                    url: '/:id?online',
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
        .factory('ReportTemplateResource', function($resource) {
            return $resource('rest/reports/:id', {id: '@id'},
                {
                    'list':             { method: 'GET', isArray: true },
                    'get':              { method: 'GET' },
                    'listScheduled':    { method: 'GET', isArray:true, url:'rest/reports/scheduled'}
                }
            );
        })
        .factory('ReportScheduleResource', function($resource) {
            return $resource('rest/reports/scheduled/:id', {id: '@id'},
                {
                    'list':             { method: 'GET', isArray: true },
                    'get':              { method: 'GET' },
                    'delete':           { method: 'DELETE', params: {id: -1} /* to prevent accidentally deleting all */ },
                    'deleteAll':        { method: 'DELETE'},
                }
            );
        })
        .factory('ReportStorageResource', function($resource) {
            return $resource('rest/reports/persisted/:id', {id: '@id'},
                {
                    'list':             { method: 'GET', isArray: true },
                    'get':              { method: 'GET' },
                    'delete':           { method: 'DELETE', params: {id: -1} /* to prevent accidentally deleting all */ },
                    'deleteAll':        { method: 'DELETE'},
                }
            );
        })
        .factory('WhoamiResource', function($resource) {
            return $resource('rest/whoami', {}, {'whoami': {method: 'GET'}});
        })
        .factory('GrafanaResource', function($resource) {
            return $resource('rest/endpoints/grafana/:id', {id: '@id'},
                {
                    'get':          { method: 'GET' },
                    'list':         { method: 'GET', isArray:true },
                    'dashboards':   { method: 'GET', isArray:true, url: 'rest/endpoints/grafana/:uid/dashboards', params: {uid: '@uid'} },
                });
        })
        .factory('UserService', function(WhoamiResource) {
            return {
                whoami: function(successCallback, errorCallback) {
                    return WhoamiResource.whoami(function(data) {
                        var user = {
                            id: data['id'],
                            name: data['fullName'],
                            email: data['email'],
                            roles: data['roles'],

                            isAdmin: function() {
                                return this.roles.indexOf("ROLE_ADMIN") >= 0;
                            },

                            isReportDesigner: function() {
                                return this.roles.indexOf("ROLE_REPORT_DESIGNER") >= 0;
                            }
                        };
                        if (successCallback) {
                            successCallback(user);
                        }
                    }, function(error) {
                        if (errorCallback) {
                            errorCallback(error);
                        }
                    });
                }
            };  
        })
        // TODO MVR verify global error handling is the way to go here for all error responses. Maybe we need a little bit more differentiated
        .controller('ReportsController', ['$scope', '$http', 'UserService', function($scope, $http, UserService) {
            $scope.fetchUserInfo = function() {
                UserService.whoami(function(user) {
                    $scope.userInfo = user;
                }, function(error) {
                    $scope.handleGlobalError(error);
                });
            };

            $scope.handleGlobalError = function(errorResponse) {
                console.log("An unexpected error occurred", errorResponse);
                $scope.globalError = "An unexpected error occurred: " + errorResponse.statusText + "(" + errorResponse.status + ")";
                $scope.globalErrorDetails = JSON.stringify(errorResponse, null, 2);
            };

            $scope.fetchUserInfo();
        }])
        .controller('ReportTemplatesController', ['$scope', '$http', 'ReportTemplateResource', function($scope, $http, ReportTemplateResource) {
            $scope.refresh = function() {
                $scope.reports = [];
                $scope.globalError = undefined;

                ReportTemplateResource.list(function(response) {
                    if (response && Array.isArray(response)) {
                        $scope.reports = response;
                    }
                }, function(errorResponse) {
                    $scope.handleGlobalError(errorResponse);
                })
            };

            $scope.refresh();

        }])
        .controller('ReportDetailController', ['$scope', '$http', '$window', '$state', '$stateParams', '$uibModal', 'ReportTemplateResource', 'GrafanaResource', function($scope, $http, $window, $state, $stateParams, $uibModal, ReportTemplateResource, GrafanaResource) {
            $scope.report = {
                id : $stateParams.id,
                format: 'PDF',
                online: $stateParams.online === 'true',
            };

            $scope.options = {
                deliverReport: !$scope.report.online,
                scheduleReport: false,
                scheduleOptions: {cronExpresison: '0 */5 * * * ?'}, // TODO MVR default value for this should be what ???

                isExecuteReport: function() {
                    return !this.deliverReport && !this.scheduleReport;
                },

                isDeliverReport: function() {
                    return this.deliverReport && !this.scheduleReport;
                },

                isScheduleReport: function() {
                    return this.deliverReport && this.scheduleReport;
                },

                getType: function() {
                    if (this.isScheduleReport()) {
                        return 'schedule';
                    }
                    if (this.isDeliverReport()) {
                        return 'deliver';
                    }
                    return 'online';
                }
            };
            $scope.parametersByName = {};

            $scope.loadDetails = function() {
                $scope.loading = true;
                $scope.surveillanceCategories = [];
                $scope.categories = [];
                $scope.formats = [];
                $scope.report.format = "PDF";
                $scope.parameters = [];
                $scope.parametersByName = {};
                $scope.endpoints = [];
                $scope.dashboards = [];
                $scope.deliveryOptions = undefined;
                $scope.selected = {
                    endpoint: undefined,
                    dashboard: undefined
                };

                var requestParameters = {
                    id: $scope.report.id,
                    userId: $scope.userInfo.id
                };

                ReportTemplateResource.get(requestParameters, function(response) {
                    $scope.loading = false;
                    $scope.surveillanceCategories = response.surveillanceCategories;
                    $scope.categories = response.categories;
                    $scope.formats = response.formats.map(function(item) {
                        return item.name
                    });
                    $scope.parameters = response.parameters;
                    $scope.deliveryOptions = response.deliveryOptions || {};
                    $scope.deliveryOptions.format = $scope.report.format;

                    // In order to have the ui look the same as before, just order the parameters
                    var order = ['string', 'integer', 'float', 'double', 'date'];
                    $scope.parameters.sort(function(left, right) {
                        return order.indexOf(left.type) - order.indexOf(right.type);
                    });

                    // Pre processing of parameters
                    $scope.parameters.forEach(function(parameter) {
                        // Apply default values for categories
                        if (parameter.inputType === 'reportCategorySelector') {
                            parameter.value = $scope.surveillanceCategories[0];
                        }
                        if (parameter.inputType === 'onmsCategorySelector') {
                            parameter.value = $scope.categories[0];
                        }

                        // Hide certain items
                        parameter.hidden = parameter.name === 'GRAFANA_ENDPOINT_UID' || parameter.name === 'GRAFANA_DASHBOARD_UID';

                        // index parameters
                        $scope.parametersByName[parameter.name] = parameter;
                    });

                    $scope.parameters.filter(function(parameter) {
                        return parameter.type === 'date'
                    }).forEach(function(parameter) {
                        parameter.internalFormat = 'YYYY-MM-DD HH:mm'; // TODO MVR use user time zone
                        parameter.internalLocale = 'en'; // TODO MVR use user locale
                        parameter.internalValue = moment(parameter.date, parameter.internalFormat).hours(parameter.hours).minutes(parameter.minutes).toDate();
                    });

                    if ($scope.isGrafanaReport()) {
                        $scope.loadEndpoints();
                    }
                }, function(response) {
                    $scope.loading = false;
                    $scope.handleGlobalError(response);
                });
            };

            $scope.endpointChanged = function() {
                $scope.dashboards = [];
                $scope.selected.dashboard = undefined;
                GrafanaResource.dashboards({uid: $scope.selected.endpoint.uid}, function(dashboards) {
                   $scope.dashboards = dashboards;
                   if ($scope.dashboards.length > 0) {
                       $scope.selected.dashboard = $scope.dashboards[0];
                   }
                }, function(errorResponse) {
                    $scope.handleGlobalError(errorResponse);
                });
            };

            $scope.loadEndpoints = function() {
                GrafanaResource.list(function(endpoints) {
                    $scope.endpoints = endpoints;
                    $scope.endpoints.forEach(function(item) {
                        item.label = item.uid;
                        if (item.description) {
                            item.label += " - " + item.description;
                        }
                    });
                    if ($scope.endpoints.length > 0) {
                        $scope.selected.endpoint = $scope.endpoints[0];
                        $scope.endpointChanged();
                    }
                }, function(errorResponse) {
                    $scope.handleGlobalError(errorResponse);
                });
            };

            $scope.isGrafanaReport = function() {
                return $scope.parametersByName['GRAFANA_ENDPOINT_UID'] && $scope.parametersByName['GRAFANA_DASHBOARD_UID'] || false;
            };

            $scope.isGrafanaReady = function() {
                return $scope.selected && $scope.selected.endpoint && $scope.selected.dashboard || false;
            };

            $scope.fillParameters = function() {
                if ($scope.isGrafanaReport()) {
                    $scope.parametersByName['GRAFANA_ENDPOINT_UID'].value = $scope.selected.endpoint.uid;
                    $scope.parametersByName['GRAFANA_DASHBOARD_UID'].value = $scope.selected.dashboard.uid;
                }

                // Set the date value
                $scope.parameters.filter(function(parameter) {
                    return parameter.type === 'date';
                }).forEach(function(p) {
                    var momentDate = moment(p.internalValue, p.internalFormat);
                    p.date = moment(p.internalValue, p.internalFormat).format('YYYY-MM-DD');
                    p.hours = momentDate.hours();
                    p.minutes = momentDate.minutes();
                });
            };

            // TODO MVR use ReportTemplateResource for this, but somehow only $http works :-/
            $scope.runReport = function() {
                $http({
                    method: 'POST',
                    url: 'rest/reports/' + $stateParams.id,
                    data:  {id:$scope.report.id, parameters: $scope.parameters, format: $scope.report.format},
                    responseType:  'arraybuffer'
                }).then(function (response) {
                        console.log("SUCCESS", response);
                        var data = response.data;
                        var fileBlob = new Blob([data], {type: $scope.report.format === 'PDF' ? 'application/pdf' : 'text/csv'});
                        var fileURL = URL.createObjectURL(fileBlob);
                        var contentDisposition = response.headers("Content-Disposition");
                        // var filename = (contentDisposition.split(';')[1].trim().split('=')[1]).replace(/"/g, '');
                        console.log(contentDisposition);
                        var filename = $stateParams.id + '.' + $scope.report.format.toLowerCase();

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

            $scope.showSuccessModal = function(scope) {
                return $uibModal.open({
                    templateUrl: successModalTemplate,
                    backdrop: 'static',
                    keyboard: false,
                    size: 'md',
                    controller: function($scope, type) {
                        $scope.type = type;
                        $scope.goToSchedules = function() {
                            $scope.$close();
                            $state.go('report.schedules');
                        };
                        $scope.goToPersisted = function() {
                            $scope.$close();
                            $state.go('report.persisted');
                        }
                    },
                    resolve: {
                        type: function() {
                            return scope.options.getType();
                        }
                    },
                });
            };

            $scope.showErrorModal = function(scope, errorResponse) {
                var modal = $uibModal.open({
                    templateUrl: errorModalTemplate,
                    backdrop: 'static',
                    keyboard: false,
                    size: 'md',
                    controller: function($scope, errorResponse, type) {
                        $scope.type = type;
                        if (errorResponse && errorResponse.data && errorResponse.data.message) {
                            $scope.errorMessage = errorResponse.data.message;
                        }
                    },
                    resolve: {
                        type: function() {
                            return scope.options.getType();
                        },
                        errorResponse: function() {
                            return errorResponse;
                        }
                    },
                });
                return modal;
            };

            $scope.deliverReport = function() {
                $http({
                    method: 'POST',
                    url: 'rest/reports/persisted',
                    data: {
                        id: $scope.report.id,
                        parameters: $scope.parameters,
                        format: $scope.report.format,
                        deliveryOptions: $scope.deliveryOptions
                    }
                }).then(function(response) {
                    $scope.showSuccessModal($scope);
                }, function(errorResponse) {
                    $scope.showErrorModal($scope, errorResponse);
                })
            };

            $scope.scheduleReport = function() {
                $http({
                    method: 'POST',
                    url: 'rest/reports/scheduled',
                    data: {
                        id: $scope.report.id,
                        parameters: $scope.parameters,
                        format: $scope.report.format,
                        deliveryOptions: $scope.deliveryOptions,
                        cronExpression: $scope.options.cronExpression,
                    }
                }).then(function(response) {
                    $scope.showSuccessModal($scope);
                }, function(errorResponse) {
                    $scope.showErrorModal($scope, errorResponse);
                })
            };

            $scope.execute = function() {
                // Before sending the report we must replace the values of some parameters
                // e.g. the Endpoint UID or Dashboard UID
                $scope.fillParameters();

                if ($scope.report.online && $scope.options.isExecuteReport()) {
                    $scope.runReport();
                }
                if ($scope.options.isDeliverReport()) {
                    $scope.deliverReport();
                }
                if ($scope.options.isScheduleReport()) {
                    $scope.scheduleReport();
                }
            };

            // We wait for the userInfo to be set, otherwise loading
            // cannot be performed as we don't have a user id
            $scope.$watch("userInfo", function(newVal, oldVal) {
                if (newVal) {
                    $scope.loadDetails();
                }
            });

            // Ensure the format matches
            $scope.$watch('report.format', function(newVal) {
                console.log(newVal);
                if ($scope.deliveryOptions) {
                    $scope.deliveryOptions.format = newVal;
                }
            });
        }])
        .controller('ReportSchedulesController', ['$scope', '$http', '$window', '$stateParams', 'ReportScheduleResource', function($scope, $http, $window, $stateParams, ReportScheduleResource) {
            $scope.scheduledReports = [];
            $scope.refresh = function() {
                ReportScheduleResource.list(function(data) {
                    $scope.scheduledReports = data;
                }, function(response) {
                    $scope.handleGlobalError(response);
                });
            };

            $scope.deleteAll = function() {
                ReportScheduleResource.deleteAll({}, function(response) {
                   $scope.refresh();
                }, function(response) {
                    $scope.handleGlobalError(response);
                });
            };

            $scope.delete = function(schedule) {
                ReportScheduleResource.delete({id: schedule.triggerName || -1}, function(response) {
                    $scope.refresh();
                }, function(response) {
                    $scope.handleGlobalError(response);
                })
            };

            $scope.refresh();
        }])
        .controller('ReportStorageController', ['$scope', '$http', '$window', '$stateParams', 'ReportStorageResource', function($scope, $http, $window, $stateParams, ReportStorageResource) {
            $scope.persistedReports = [];
            $scope.refresh = function() {
                ReportStorageResource.list(function(data) {
                    $scope.persistedReports = data;
                }, function(response) {
                    $scope.handleGlobalError(response);
                });
            };

            $scope.deleteAll = function() {
                ReportStorageResource.deleteAll({}, function(response) {
                    $scope.refresh();
                }, function(response) {
                    $scope.handleGlobalError(response);
                });
            };

            $scope.delete = function(report) {
                ReportStorageResource.delete({id: report.id || -1}, function(response) {
                    $scope.refresh();
                }, function(response) {
                    $scope.handleGlobalError(response);
                })
            };

            $scope.refresh();
        }])
    ;
}());
