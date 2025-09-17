/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
const angular = require('vendor/angular-js');
require('../../lib/onms-http');
require('angular-ui-router');
require('angular-bootstrap-confirm');

const confirmPopoverTemplate = require('../onms-classifications//views/modals/popover.html');
const indexTemplate  = require('./index.html');
const grafanaTemplate  = require('./grafana/grafana.html');
const grafanaModalTemplate = require('./grafana/grafana-modal.html');


(function() {
    'use strict';

    const MODULE_NAME = 'onms.endpoints';

    angular.module(MODULE_NAME, [
        'angular-loading-bar',
        'ngResource',
        'ui.bootstrap',
        'ui.router',
        'onms.http',
        'onms.default.apps',
        'mwl.confirm',
    ])
        .run(function(confirmationPopoverDefaults) {
            confirmationPopoverDefaults.templateUrl = confirmPopoverTemplate;
        })
        .config( ['$locationProvider', function ($locationProvider) {
            $locationProvider.hashPrefix('!');
            $locationProvider.html5Mode(false);
        }])
        .config(['$stateProvider', '$urlRouterProvider', function ($stateProvider, $urlRouterProvider) {
            $stateProvider
                .state('endpoints', {
                    url: '/endpoints',
                    controller: 'EndpointsController',
                    templateUrl: indexTemplate
                })
                .state('endpoints.grafana', {
                    url: '/grafana',
                    controller: 'GrafanaEndpointsController',
                    templateUrl: grafanaTemplate
                })
                ;
            $urlRouterProvider.otherwise('endpoints/grafana');
        }])
        .factory('GrafanaEndpointsService', function($resource) {
            return $resource('rest/endpoints/grafana/:id', {id: '@id'},
                {
                    'get':          { method: 'GET'  },
                    'create':       { method: 'POST' },
                    'update':       { method: 'PUT' },
                    'list':         { method: 'GET', isArray: true },
                    'delete':       { method: 'DELETE', params: {id: -1 /* force to -1 to prevent accidentally deleting all endpoints */ } },
                    'verify':       { method: 'POST', params: {} /* resets id */, url: 'rest/endpoints/grafana/verify'},
                }
            );
        })
        .controller('EndpointsController', ['$scope', function($scope) {
            $scope.globalError = undefined;
            $scope.types = [
                {id: 'grafana', label: 'Grafana'},
            ];

            $scope.handleGlobalError = function(errorResponse) {
                // eslint-disable-next-line no-console
                console.log('An unexpected error occurred', errorResponse);
                $scope.globalError = 'An unexpected error occurred: ' + errorResponse.statusText + '(' + errorResponse.status + ')';
                $scope.globalErrorDetails = JSON.stringify(errorResponse, null, 2);
            };
        }])
        .controller('GrafanaEndpointsController', ['$scope', '$http', '$uibModal', 'GrafanaEndpointsService', function($scope, $http, $uibModal, GrafanaEndpointsService) {
            $scope.refresh = function() {
                $scope.endpoints = [];

                GrafanaEndpointsService.list(function(response) {
                    if (response && Array.isArray(response)) {
                        $scope.endpoints = response;
                        $scope.endpoints.forEach(function(item) {
                           item.revealApiKey = false;
                        });
                    }
                }, function(response) {
                    $scope.handleGlobalError(response);
                });
            };

            $scope.openModal = function(endpoint) {
                return $uibModal.open({
                    backdrop: false,
                    controller: 'GrafanaEndpointModalController',
                    templateUrl: grafanaModalTemplate,
                    size: 'lg',
                    resolve: {
                        endpoint: function() {
                            return endpoint;
                        }
                    }
                });
            };

            $scope.deleteEndpoint = function(deleteMe) {
                GrafanaEndpointsService.delete({id: deleteMe.id} /* we only need the id */, function(response) {
                    $scope.refresh();
                }, function(response) {
                    $scope.handleGlobalError(response);
                });
            };

            $scope.editEndpoint = function(endpoint) {
                const clone = angular.copy(endpoint);
                const modalInstance = $scope.openModal(clone);
                modalInstance.result.then(function () {
                    $scope.refresh();
                },
                function() {
                    $scope.refresh();
                });
            };

            $scope.addNewEndpoint = function() {
                const modalInstance = $scope.openModal();
                modalInstance.closed.then(function () {
                    $scope.refresh(); // Success
                }, function() {
                    $scope.refresh(); // Failure
                });
            };

            $scope.refresh();
        }])
        .controller('GrafanaEndpointModalController', ['$scope', '$uibModalInstance', '$sce', 'GrafanaEndpointsService', 'endpoint', function($scope, $uibModalInstance, $sce, GrafanaEndpointsService, endpoint) {
            $scope.uidRegex = '[a-zA-Z0-9]+[a-zA-Z0-9_-]*';
            $scope.endpoint = endpoint || {revealApiKey: false};
            $scope.buttonName = $scope.endpoint.id ? 'Update' : 'Create';
            $scope.verification = {
                state: undefined,
                result: undefined
            };
            $scope.error = {};
            $scope.defaultReadTimeout = 30;
            $scope.defaultConnectTimeout = 30;

            const handleErrorResponse = function(response) {
                if (response.status === 400 && response.data) {
                    const errorObject = response.data;
                    if (errorObject.context && errorObject.message) {
                        $scope.error[errorObject.context] = errorObject.message;
                    } else if (errorObject.context) {
                        $scope.error[errorObject.context] = 'Invalid value';
                    } else {
                        $scope.error['entity'] = 'The endpoint is not valid. Details were not provided';
                    }
                } else {
                    $scope.handleGlobalError(response);
                }
            };

            $scope.verify = function() {
                $scope.verification = {
                    state: 'running',
                    result: undefined
                };

                const endpoint = {
                    apiKey: $scope.endpoint.apiKey,
                    url: $scope.endpoint.url,
                    readTimeout: $scope.endpoint.readTimeout,
                    connectTimeout: $scope.endpoint.connectTimeout
                };
                GrafanaEndpointsService.verify(endpoint, function(response) {
                    $scope.verification.state = 'success';
                }, function(response) {
                    $scope.verification.state = 'failure';
                    if (response.status === 400 && response.data && response.data.message) {
                        $scope.verification.result = $sce.trustAsHtml(response.data.message);
                    } else {
                        $scope.verification.result = 'Something went wrong';
                    }
                });
            };

            $scope.save = function() {
                $scope.error = {};
                $scope.verification.state = undefined;
                $scope.verification.result = undefined;

                // Close modal afterwards
                const closeCallback = function() {
                    $uibModalInstance.close();
                };
                const object = {
                    id: $scope.endpoint.id,
                    uid: $scope.endpoint.uid,
                    url: $scope.endpoint.url,
                    apiKey: $scope.endpoint.apiKey,
                    description: $scope.endpoint.description,
                    connectTimeout: $scope.endpoint.connectTimeout,
                    readTimeout: $scope.endpoint.readTimeout
                };
                if ($scope.endpoint.id) {
                    GrafanaEndpointsService.update(object, closeCallback, handleErrorResponse);
                } else {
                    GrafanaEndpointsService.create(object, closeCallback, handleErrorResponse);
                }
            };

            $scope.cancel = function() {
                $uibModalInstance.dismiss('Cancelled by User');
            };
        }])
    ;
}());
