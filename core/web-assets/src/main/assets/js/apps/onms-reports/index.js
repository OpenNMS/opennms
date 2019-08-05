import ScheduleOptions from '../../lib/onms-schedule-editor/scripts/ScheduleOptions';
import ReportDetails from './ReportDetails';

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
const editScheduleModalTemplate  = require('./modals/schedule-edit-modal.html');

const reportDetailsTemplate = require('./report-details.html');

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
            return $resource('rest/reports/scheduled/:id', {id: '@triggerName'},
                {
                    'list':             { method: 'GET', isArray: true },
                    'get':              { method: 'GET', isArray: false },
                    'delete':           { method: 'DELETE', params: {triggerName: -1} /* to prevent accidentally deleting all */ },
                    'deleteAll':        { method: 'DELETE'},
                    'update':           { method: 'PUT'}
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
        .directive('onmsReportDetails', ['GrafanaResource', function (GrafanaResource) {
            return {
                restrict: 'E',
                templateUrl: reportDetailsTemplate,
                scope: {
                    report: '=?ngModel',
                    options: '=?options'
                },
                link: function (scope, element, attrs) {
                    scope.endpoints = [];
                    scope.dashboards = [];
                    scope.selected = {
                        endpoint: undefined,
                        dashboard: undefined
                    };

                    scope.endpointChanged = function () {
                        scope.dashboards = [];
                        scope.selected.dashboard = undefined;
                        GrafanaResource.dashboards({uid: scope.selected.endpoint.uid}, function (dashboards) {
                            scope.dashboards = dashboards;
                            if (scope.dashboards.length > 0) {
                                scope.selected.dashboard = scope.dashboards[0];
                            }
                        }, function (errorResponse) {
                            console.log('ERROR', errorResponse); // TODO MVR add global error handling
                            // scope.handleGlobalError(errorResponse);
                        });
                    };

                    scope.loadEndpoints = function () {
                        GrafanaResource.list(function (endpoints) {
                            scope.endpoints = endpoints;
                            scope.endpoints.forEach(function (item) {
                                item.label = item.uid;
                                if (item.description) {
                                    item.label += " - " + item.description;
                                }
                            });
                            if (scope.endpoints.length > 0) {
                                scope.selected.endpoint = scope.endpoints[0];
                                scope.endpointChanged();
                            }
                        }, function (errorResponse) {
                            console.log('ERROR', errorResponse); // TODO MVR add global error handling
                            // scope.handleGlobalError(errorResponse);
                        });
                    };

                    scope.isGrafanaReady = function () {
                        return scope.selected && scope.selected.endpoint && scope.selected.dashboard || false;
                    };

                    // Ensure the format matches
                    scope.$watch('report.format', function (newVal) {
                        if (scope.report.deliveryOptions) {
                            scope.report.deliveryOptions.format = newVal;
                        }
                    });

                    scope.$watchCollection('report.parameters', function(newVal, oldVal) {
                        if (oldVal.length === 0 && newVal.length !== 0) {
                            if (scope.report.isGrafanaReport()) {
                                scope.loadEndpoints();
                            }
                        }
                        scope.report.updateParameters(scope.selected);
                    });
                }
            }
        }])
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
            $scope.meta = {
                reportId: $stateParams.id,
                online: $stateParams.online === 'true'
            };

            $scope.report = new ReportDetails({id: $scope.meta.reportId});
            $scope.options = {};
            $scope.loading = false;

            $scope.loadDetails = function() {
                $scope.loading = true;
                $scope.selected = {
                    endpoint: undefined,
                    dashboard: undefined
                };

                $scope.options = {
                    showReportFormatOptions: $scope.meta.online,
                    showDeliveryOptions: $scope.userInfo.isReportDesigner() || $scope.userInfo.isAdmin(),
                    showDeliveryOptionsToggle: true && $scope.meta.online,
                    showScheduleOptions: $scope.userInfo.isReportDesigner() || $scope.userInfo.isAdmin(),
                    showScheduleOptionsToggle: true,
                    deliverReport: !$scope.meta.online,
                    scheduleReport: false,
                    canEditTriggerName: true
                };

                console.log($scope.options);

                const requestParameters = {
                    id: $scope.report.id,
                    userId: $scope.userInfo.id
                };

                ReportTemplateResource.get(requestParameters, function(response) {
                    $scope.loading = false;
                    $scope.report = new ReportDetails(response);
                }, function(response) {
                    $scope.loading = false;
                    $scope.handleGlobalError(response);
                });
            };

            // TODO MVR use ReportTemplateResource for this, but somehow only $http works :-/
            $scope.runReport = function() {
                $http({
                    method: 'POST',
                    url: 'rest/reports/' + $stateParams.id,
                    data:  { id:$scope.report.id, parameters: $scope.report.parameters, format: $scope.report.format},
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
                    controller: function($scope, options) {
                        $scope.options = options;
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
                        options: function() {
                            return scope.options;
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
                    controller: function($scope, errorResponse, options) {
                        $scope.options = options;
                        if (errorResponse && errorResponse.data && errorResponse.data.message) {
                            $scope.errorMessage = errorResponse.data.message;
                        }
                    },
                    resolve: {
                        options: function() {
                            return scope.options;
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
                        parameters: $scope.report.parameters,
                        format: $scope.report.format,
                        deliveryOptions: $scope.report.deliveryOptions
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
                        parameters: $scope.report.parameters,
                        format: $scope.report.format,
                        deliveryOptions: $scope.report.deliveryOptions,
                        cronExpression: $scope.report.scheduleOptions.getCronExpression(),
                    }
                }).then(function(response) {
                    $scope.showSuccessModal($scope);
                }, function(errorResponse) {
                    $scope.showErrorModal($scope, errorResponse);
                })
            };

            $scope.execute = function() {
                console.log($scope.report);
                if ($scope.meta.online && !$scope.options.deliverReport && !$scope.options.scheduleReport) {
                    $scope.runReport();
                }
                if ($scope.options.deliverReport && !$scope.options.scheduleReport) {
                    $scope.deliverReport();
                }
                if ($scope.options.deliverReport && $scope.options.scheduleReport) {
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
        }])
        .controller('ReportSchedulesController', ['$scope', '$uibModal', 'ReportScheduleResource', function($scope, $uibModal, ReportScheduleResource) {
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

            $scope.edit = function(triggerName, reportId) {
                const modalInstance = $uibModal.open({
                    templateUrl: editScheduleModalTemplate,
                    backdrop: 'static',
                    keyboard: false,
                    size: 'lg',
                    controller: 'ScheduleEditController',
                    resolve: {
                        userInfo: function() {
                            return $scope.userInfo;
                        },
                        meta: function() {
                            return {
                                reportId: reportId,
                                online: false,
                                triggerName: triggerName
                            }
                        }
                    }

                });

                modalInstance.result.then(function() {
                    $scope.refresh();
                });
            };

            $scope.refresh();
        }])
        .controller('ScheduleEditController', ['$scope', 'userInfo', 'meta', 'ReportScheduleResource', function($scope, userInfo, meta, ReportScheduleResource) {
            $scope.meta = meta;
            $scope.userInfo = userInfo;
            $scope.report = new ReportDetails({id: $scope.meta.reportId});
            $scope.options = {};
            $scope.loading = false;

            $scope.loadDetails = function() {
                $scope.loading = true;
                $scope.selected = {
                    endpoint: undefined,
                    dashboard: undefined
                };

                $scope.options = {
                    showReportFormatOptions: false,    // Options are not shown, as we are editing a schedule
                    showDeliveryOptions: true,         // always show when editing
                    showDeliveryOptionsToggle: false,  // Toggling is disabled
                    showScheduleOptions: true,         // always show when editing
                    showScheduleOptionsToggle: false, // Toggling is disabled
                    deliverReport: true,        // when editing schedule and delivery is enabled
                    scheduleReport: true,       // when editing schedule and delivery is enabled
                    canEditTriggerName: false,  // When in edit mode, the trigger name should be unique
                };

                console.log($scope.meta);
                ReportScheduleResource.get({id: $scope.meta.triggerName}, function(response) {
                    $scope.loading = false;
                    $scope.report = new ReportDetails(response);
                }, function(response) {
                    $scope.loading = false;
                    $scope.handleGlobalError(response);
                });
            };

            $scope.update = function() {
                const data = {
                    id: $scope.report.id,
                    triggerName: $scope.meta.triggerName,
                    parameters: $scope.report.parameters,
                    format: $scope.report.format,
                    deliveryOptions: $scope.report.deliveryOptions,
                    cronExpression: $scope.report.scheduleOptions.getCronExpression(),
                };
                ReportScheduleResource.update(data, function(response) {
                  // TODO MVR user feedback?
                 $scope.$close();
              }, function(response) {
                    // TODO MVR we should implement proper error handling here, but this requires better errors on backend first
                    if (response && response.data && response.data.message) {
                        $scope.error = response.data.message;
                    } else {
                        $scope.error = "An unexpected error occurred";
                    }
              });
            };

            $scope.loadDetails();
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
