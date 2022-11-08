import ReportDetails from './ReportDetails';
import ErrorResponse from '../../lib/onms-http/ErrorResponse';
import Types from '../../lib/onms-schedule-editor/scripts/Types';
import moment from 'moment';
require('moment-timezone');

const hash = require('hash.js');
const angular = require('vendor/angular-js');
require('../../lib/onms-http');
require('../../lib/onms-pagination');
require('../../lib/onms-datetimepicker');
require('../../lib/onms-schedule-editor');
require('angular-ui-router');
require('angular-bootstrap-confirm');
require('ui-select-bs4');
require('ui-select-bs4/dist/select.css');

const elementList = require('../onms-elementList/lib/elementList');
const indexTemplate  = require('./index.html');
const templatesTemplate  = require('./templates.html');
const persistedtTemplate  = require('./persisted.html');
const schedulesTemplate  = require('./schedules.html');
const detailsTemplate  = require('./details.html');
const editScheduleModalTemplate  = require('./modals/schedule-edit-modal.html');

const reportDetailsTemplate = require('./report-details.html');
const confirmTopoverTemplate = require('../onms-classifications/views/modals/popover.html');

const handleReportError = function(response, report, optionalCallbackIfNoContextError) {
    if (report && response) {
        const errorResponse = new ErrorResponse(response);
        if (errorResponse.isBadRequest()) {
            const contextError = errorResponse.asContextError();
            report.setErrors(contextError);
        } else if (optionalCallbackIfNoContextError) {
            optionalCallbackIfNoContextError(response);
        }
    }
};

const handleGrafanaError = function(response, report, optionalCallbackIfNoContextError) {
    // In case the dashboards could not be loaded, it may be due to
    // an issue with talking to Grafana itself.
    const errorResponse = new ErrorResponse(response);
    if (errorResponse.isContextError()) {
        const contextError = errorResponse.asContextError();
        if (contextError.context === 'entity') {
            contextError.context = 'GRAFANA_ENDPOINT_UID';
        }
        report.setErrors(contextError);
    }  else {
        handleReportError(response, report, optionalCallbackIfNoContextError);
    }
};

(function() {
    'use strict';

    var MODULE_NAME = 'onms.reports';

    angular.module(MODULE_NAME, [
            'angular-loading-bar',
            'ngResource',
            'ngSanitize',
            'ui.router',
            'ui.select',
            'mwl.confirm',
            'onms.http',
            'onms.default.apps',
            'onms.datetimepicker',
            'onms.schedule.editor',
            'onms.pagination'
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
        .directive('onmsReportDetails', ['GrafanaResource', '$q', '$http', function (GrafanaResource, $q, $http) {
            const getDashboardDetails = (uid, dashboardId) => {
                return $http.get('rest/endpoints/grafana/' + uid + '/dashboards/' + dashboardId).then((res) => res.data);
            };

            return {
                restrict: 'E',
                templateUrl: reportDetailsTemplate,
                scope: {
                    report: '=?ngModel',
                    options: '=?options',
                    onInvalidChange: '&?onInvalidChange',
                    onGlobalError: '&onGlobalError'
                },
                link: function (scope, element, attrs) {
                    scope.oldTimeZone = undefined;

                    scope.endpoints = [];
                    scope.dashboards = [];
                    scope.selected = {
                        endpoint: undefined,
                        dashboard: undefined
                    };
                    scope.onInvalidChange = scope.onInvalidChange || function(invalidState) {}; // eslint-disable-line @typescript-eslint/no-empty-function
                    scope.onDateParamStateChange = function(state, date, parameter) {
                        scope.$evalAsync(() => {
                            if (date && parameter) {
                                parameter.internalValue = date;
                            }
                            scope.onInvalidChange({invalidState: state});
                        });
                    };

                    scope.paramHash = (parameter) => {
                        const ret = hash.sha256();
                        if (parameter) {
                            ret.update(parameter.type);
                            ret.update(parameter.name);
                            ret.update(parameter.value);
                            ret.update(parameter.internalValue);
                            ret.update(parameter.internalFormat);
                        }
                        return ret.digest('hex');
                    };

                    scope.timezoneChanged = () => {
                        const before = scope.oldTimeZone;
                        const after = scope.report && scope.report.parametersByName['timezone'] ? scope.report.parametersByName['timezone'].value : undefined;

                        if (before && after && before !== after) {
                            // timezone has changed, adjust existing date/time values to match

                            scope.report.parameters.forEach((parameter, index) => {
                                if (parameter.type === 'date') {
                                    const oldDate = moment.tz(parameter.internalValue, before);
                                    const newDate = oldDate.clone().tz(after);
                                    if (newDate.isValid()) {
                                        parameter.internalValue = newDate.format(parameter.internalFormat);
                                        parameter.date = newDate.format('YYYY-MM-DD');
                                        parameter.hours = newDate.hours();
                                        parameter.minutes = newDate.minutes();
                                    } else {
                                        // eslint-disable-next-line no-console
                                        console.error('timezoneChanged(): new date is invalid?!');
                                    }

                                    // re-hydrate this parameter because the datetime picker isn't refreshing itself properly
                                    scope.report.parameters[index] = scope.report.parametersByName[parameter.name] = Object.assign({}, parameter);
                                }
                            });
                        }

                        // done processing, update state for next time
                        if (scope.report && scope.report.parametersByName['timezone']) {
                            scope.report.parametersByName['timezone'].value = after;
                        }
                        scope.oldTimeZone = after;
                        scope.report.validateTimezone();
                    };

                    scope.endpointChanged = function () {
                        scope.dashboards = [];
                        scope.selected.dashboard = undefined;
                        scope.report.resetErrors();
                        if (scope.report.parametersByName['timezone']) {
                            scope.report.parametersByName['timezone'].value = undefined;
                            scope.timezoneChanged();
                        }
                        GrafanaResource.dashboards({uid: scope.selected.endpoint.uid}, function (dashboards) {
                            scope.dashboards = dashboards;
                            if (scope.dashboards.length > 0) {
                                scope.selected.dashboard = scope.dashboards[0];
                            }
                        }, function (response) {
                            handleGrafanaError(response, scope.report, () => scope.onGlobalError({response: response}));
                        });
                    };

                    scope.loadEndpoints = function () {
                        scope.report.resetErrors();
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
                            handleGrafanaError(errorResponse, scope.report, () => scope.onGlobalError({response: errorResponse}));
                        });
                    };

                    // Ensure the format matches
                    scope.$watch('report.format', function (newVal) {
                        if (scope.report.deliveryOptions) {
                            scope.report.deliveryOptions.format = newVal;
                        }
                    });

                    scope.$watch('report.scheduleOptions.type', function() {
                        // Reset cronExpression issue, if we changed the type as the message may be outdated
                        if (scope.report.scheduleOptions.type !== Types.CUSTOM && scope.report.errors.cronExpression) {
                            scope.report.errors.cronExpression = undefined;
                        }
                    });

                    scope.$watchCollection('report.parameters', function(newVal, oldVal) {
                        if (oldVal.length === 0 && newVal.length !== 0) {
                            if (scope.report.isGrafanaReport()) {
                                scope.loadEndpoints();
                            }
                        }
                    });

                    scope.$watchCollection('selected', (newSelected) => {
                        const deferred = $q.defer();

                        if (newSelected
                            && newSelected.endpoint
                            && newSelected.endpoint.uid
                            && newSelected.dashboard
                            && newSelected.dashboard.uid
                        ) {
                            const selectedDashboard = newSelected.dashboard;
                            // the dashboard object you get from search doesn't include the full
                            // dashboard data; query the dashboard and grab the timezone from it
                            getDashboardDetails(newSelected.endpoint.uid, selectedDashboard.uid).then((newDashboard) => {
                                if (newDashboard
                                    && newDashboard.timezone !== undefined
                                    && selectedDashboard.timezone !== newDashboard.timezone
                                ) {
                                    selectedDashboard.timezone = newDashboard.timezone;
                                    scope.timezoneChanged();
                                }
                            }).finally(() => {
                                deferred.resolve(selectedDashboard);
                            });
                        } else {
                            // endpoint and/or dashboard are missing UIDs?  assume we can't do anything yet
                            deferred.resolve((newSelected && newSelected.dashboard) ? newSelected.dashboard : undefined);
                        }
                        deferred.promise.finally(() => {
                            scope.report.updateGrafanaParameters(newSelected);
                        })
                    });

                    scope.$watch('reportForm.$invalid', function(newVal, oldVal) {
                        if (scope.onInvalidChange) {
                            scope.onInvalidChange({invalidState: newVal});
                        }
                    });
                }
            }
        }])
        .directive('multiEmails', function() {
            const EMAIL_REGEXP = /^[-!#$%&'*+/0-9=?A-Z^_a-z{|}~](\.?[-!#$%&'*+/0-9=?A-Z^_a-z{|}~])*@[a-zA-Z](-?[a-zA-Z0-9])*(\.[a-zA-Z](-?[a-zA-Z0-9])*)+$/;
            return {
                restrict: 'A',
                require: 'ngModel',
                link: function (scope, element, attrs, ctrl) {
                    if (ctrl && ctrl.$validators.email) {
                        ctrl.$validators.email = function(modelValue) {
                            if (angular.isDefined(modelValue)) {
                                const isValidEmails = ctrl.$isEmpty(modelValue) || modelValue.split(',').every(
                                    function (email) {
                                        return EMAIL_REGEXP.test(email.trim());
                                    }
                                );
                                return isValidEmails;
                            }
                            return false;
                        };
                    }
                }
            };
        })
        .controller('ReportsController', ['$scope', '$rootScope', '$http', 'UserService', function($scope, $rootScope, $http, UserService) {
            $scope.fetchUserInfo = function() {
                UserService.whoami(function(user) {
                    $scope.userInfo = user;
                }, function(errorResponse) {
                    $scope.setGlobalError(errorResponse);
                });
            };
            $scope.fetchUserInfo();
            $scope.setGlobalError = function(errorResponse) {
                $scope.globalError = 'An unexpected error occurred: ' + errorResponse.status + ' ' + errorResponse.statusText;
                $scope.globalErrorDetails = JSON.stringify(errorResponse, null, 2);
            };
            $scope.globalError = undefined;
            $scope.globalErrorDetails = undefined;
        }])
        .controller('ReportTemplatesController', ['$scope', '$http', 'ReportTemplateResource', function($scope, $http, ReportTemplateResource) {
            $scope.refresh = function() {
                $scope.reports = [];

                ReportTemplateResource.list(function(response) {
                    if (response && Array.isArray(response)) {
                        $scope.reports = response;
                    }
                }, function(errorResponse) {
                    $scope.setGlobalError(errorResponse);
                })
            };

            $scope.refresh();

        }])
        .controller('ReportDetailController', ['$scope', '$http', '$window', '$state', '$stateParams', '$uibModal', 'ReportTemplateResource', function($scope, $http, $window, $state, $stateParams, $uibModal, ReportTemplateResource) {
            $scope.meta = {
                reportId: $stateParams.id,
                online: $stateParams.online === 'true'
            };

            $scope.report = new ReportDetails({id: $scope.meta.reportId, scope: $scope});
            $scope.options = {};
            $scope.loading = false;
            $scope.reportForm = { $invalid: false };
            $scope.onReportFormInvalidStateChange = function(invalidState) {
                $scope.reportForm.$invalid = invalidState;
            };

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

                const requestParameters = {
                    id: $scope.report.id,
                    userId: $scope.userInfo.id
                };

                ReportTemplateResource.get(requestParameters, function(response) {
                    $scope.loading = false;
                    $scope.report = new ReportDetails(Object.assign(response, {scope: $scope}));
                }, function(response) {
                    $scope.loading = false;
                    $scope.setGlobalError(response);
                });
            };

            $scope.runReport = function() {
                $scope.report.resetErrors();
                // eslint-disable-next-line no-console
                console.debug('running report:', $scope.report);
                $http({
                    method: 'POST',
                    url: 'rest/reports/' + $stateParams.id,
                    data:  { id:$scope.report.id, parameters: $scope.report.parameters, format: $scope.report.format},
                    responseType:  'arraybuffer'
                }).then(function (response) {
                        var data = response.data;
                        var fileBlob = new Blob([data], {type: $scope.report.format === 'PDF' ? 'application/pdf' : 'text/csv'});
                        var fileURL = URL.createObjectURL(fileBlob);
                        var contentDisposition = response.headers("Content-Disposition");
                        // var filename = (contentDisposition.split(';')[1].trim().split('=')[1]).replace(/"/g, '');
                        var filename = $stateParams.id + '.' + $scope.report.format.toLowerCase();

                        var a = document.createElement('a');
                        document.body.appendChild(a);
                        a.style = 'display: none';
                        a.href = fileURL;
                        a.download = filename;
                        a.click();
                        window.URL.revokeObjectURL(fileURL);
                        document.body.removeChild(a);
                    },
                    function(response) {
                        if (response.status === 400) {
                            // content Type is 'arraybuffer', so first convert to json
                            const bodyAsString = String.fromCharCode.apply(null, new Uint8Array(response.data));
                            const bodyAsJson = JSON.parse(bodyAsString);
                            response.data = bodyAsJson;
                            handleReportError(response, $scope.report, (response) => $scope.setGlobalError(response));
                        } else {
                            $scope.setGlobalError(response);
                        }
                    });
            };

            $scope.deliverReport = function() {
                $scope.report.resetErrors();
                $http({
                    method: 'POST',
                    url: 'rest/reports/persisted',
                    data: {
                        id: $scope.report.id,
                        parameters: $scope.report.parameters,
                        format: $scope.report.format,
                        deliveryOptions: $scope.report.deliveryOptions
                    }
                }).then(function() {
                    $scope.deliverySuccess = true;
                }, function(response) {
                    handleReportError(response, $scope.report, (response) => $scope.setGlobalError(response));
                })
            };

            $scope.scheduleReport = function() {
                $scope.report.resetErrors();
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
                    $scope.scheduleSuccess = true;
                }, function(response) {
                    handleReportError(response, $scope.report, (response) => $scope.setGlobalError(response));
                })
            };

            $scope.execute = function() {
                $scope.deliverySuccess = false;
                $scope.scheduleSuccess = false;
                $scope.$evalAsync(() => {
                    $scope.report.updateDateParameters();
                });
                $scope.$evalAsync(() => {
                    if ($scope.meta.online && !$scope.options.deliverReport && !$scope.options.scheduleReport) {
                        $scope.runReport();
                    }
                    if ($scope.options.deliverReport && !$scope.options.scheduleReport) {
                        $scope.deliverReport();
                    }
                    if ($scope.options.deliverReport && $scope.options.scheduleReport) {
                        $scope.scheduleReport();
                    }
                });
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
            $scope.pagination = { page: 1, limit: 20, totalItems : 0, offset: 0 };

            $scope.refresh = function() {
                const parameters = $scope.pagination || {};
                ReportScheduleResource.list({
                    limit: parameters.limit || 20,
                    offset: (parameters.page -1) * parameters.limit || 0,
                }, function(data, headers) {
                    $scope.scheduledReports = data;
                    const contentRange = elementList.parseContentRange(headers('Content-Range'));
                    $scope.pagination.totalItems = contentRange.total;
                }, function(response) {
                    $scope.setGlobalError(response);
                });
            };

            $scope.deleteAll = function() {
                ReportScheduleResource.deleteAll({}, function(response) {
                   $scope.pagination.page = 1; // go back to page 1
                   $scope.refresh();
                }, function(response) {
                    $scope.setGlobalError(response);
                });
            };

            $scope.delete = function(schedule) {
                ReportScheduleResource.delete({id: schedule.triggerName || -1}, function(response) {
                    // If we deleted the last report on this page
                    if ($scope.scheduledReports.length === 1 && $scope.pagination.page > 1) {
                        $scope.pagination.page--; // go a page back
                    }
                    $scope.refresh();
                }, function(response) {
                    $scope.setGlobalError(response);
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
                        },
                        setGlobalError: function() {
                            return $scope.setGlobalError;
                        }
                    }

                });

                modalInstance.result.then(function() {
                    $scope.refresh();
                });
            };

            $scope.refresh();
        }])
        .controller('ScheduleEditController', ['$http', '$q', '$scope', '$timeout', 'userInfo', 'meta', 'setGlobalError', 'ReportScheduleResource', function($http, $q, $scope, $timeout, userInfo, meta, setGlobalError, ReportScheduleResource) {
            $scope.meta = meta;
            $scope.userInfo = userInfo;
            $scope.report = null;
            $scope.options = {};
            $scope.loading = false;
            $scope.reportForm = { $invalid : false };

            const getReportDetails = (reportId) => {
                return $http.get('rest/reports/scheduled/' + reportId).then((res) => res.data);
            };

            $scope.onReportFormInvalidStateChange = function(invalidState) {
                $scope.reportForm.$invalid = invalidState;
            };

            $scope.setGlobalError = function(response) {
                setGlobalError(response);
            };

            $scope.loadDetails = function() {
                if ($scope.loading) {
                    return;
                }
                $scope.loading = true;

                $scope.$evalAsync(() => {
                    $scope.selected = {
                        endpoint: undefined,
                        dashboard: undefined
                    };

                    $scope.options = {
                        hideEndpointsChooser: true,        // endpoint should not be changed when editing
                        showReportFormatOptions: false,    // Options are not shown, as we are editing a schedule
                        showDeliveryOptions: true,         // always show when editing
                        showDeliveryOptionsToggle: false,  // Toggling is disabled
                        showScheduleOptions: true,         // always show when editing
                        showScheduleOptionsToggle: false, // Toggling is disabled
                        deliverReport: true,        // when editing schedule and delivery is enabled
                        scheduleReport: true,       // when editing schedule and delivery is enabled
                        canEditTriggerName: false,  // When in edit mode, the trigger name should be unique
                    };

                    getReportDetails($scope.meta.triggerName).then((reportData) => {
                        $scope.report = new ReportDetails(Object.assign(reportData, {scope: $scope}));
                    }).catch((err) => {
                        $scope.setGlobalError(err);
                        $scope.$close();
                    }).finally(() => {
                        $scope.loading = false;
                    });
                });
            };

            $scope.update = function() {
                if (!$scope.report) {
                    return $q.reject('report not initialized');
                }
                $scope.report.resetErrors();
                const data = {
                    id: $scope.report.id,
                    triggerName: $scope.meta.triggerName,
                    parameters: $scope.report.parameters,
                    format: $scope.report.format,
                    deliveryOptions: $scope.report.deliveryOptions,
                    cronExpression: $scope.report.scheduleOptions.getCronExpression(),
                };
                return ReportScheduleResource.update(data).$promise.catch((err) => {
                    // eslint-disable-next-line no-console
                    console.error(err);
                    handleReportError(err, $scope.report, () => {
                        $scope.setGlobalError(err);
                    });
                }).finally(() => {
                    $scope.$close();
                });
            };

            $scope.loadDetails();
        }])
        .controller('ReportStorageController', ['$scope', '$http', '$window', '$stateParams', 'ReportStorageResource', function($scope, $http, $window, $stateParams, ReportStorageResource) {
            $scope.persistedReports = [];
            $scope.pagination = { page: 1, limit: 20, totalItems : 0, offset: 0 };

            $scope.refresh = function() {
                const parameters = $scope.pagination || {};
                ReportStorageResource.list({
                    limit: parameters.limit || 20,
                    offset: (parameters.page -1) * parameters.limit || 0,
                }, function(data, headers) {
                    $scope.persistedReports = data;
                    const contentRange = elementList.parseContentRange(headers('Content-Range'));
                    $scope.pagination.totalItems = contentRange.total;
                }, function(response) {
                    $scope.setGlobalError(response);
                });
            };

            $scope.deleteAll = function() {
                ReportStorageResource.deleteAll({}, function(response) {
                    $scope.pagination.page = 1; // go back to page 1
                    $scope.refresh();
                }, function(response) {
                    $scope.setGlobalError(response);
                });
            };

            $scope.delete = function(report) {
                ReportStorageResource.delete({id: report.id || -1}, function(response) {
                    // If we deleted the last report on this page
                    if ($scope.persistedReports.length === 1 && $scope.pagination.page > 1) {
                        $scope.pagination.page--; // go back a page
                    }
                    $scope.refresh();
                }, function(response) {
                    $scope.setGlobalError(response);
                })
            };

            $scope.refresh();
        }])
    ;
}());
