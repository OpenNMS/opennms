const angular = require('vendor/angular-js');
require('../../lib/onms-http');
require('angular-ui-router');
require('angular-bootstrap-confirm');

const confirmPopoverTemplate = require('../onms-classifications//views/modals/popover.html');
const indexTemplate  = require('./index.html');
const grafanaTemplate  = require('./grafana/grafana.html');
const grafanaModalTemplate = require('./grafana/grafana-modal.html');
const elasticTemplate  = require('./elastic/elastic.html');


(function() {
    'use strict';

    var MODULE_NAME = 'onms.endpoints';

    angular.module(MODULE_NAME, [
        'angular-loading-bar',
        'ngResource',
        'ui.bootstrap',
        'ui.router',
        'onms.http',
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
                // TODO MVR remove later
                .state('endpoints.elastic', {
                    url: '/eastic',
                    templateUrl: elasticTemplate,
                });
            $urlRouterProvider.otherwise('endpoints/grafana');
        }])
        .factory('GrafanaEndpointsService', function($resource) {
            return $resource('rest/endpoints/grafana/:id', {id: '@id'},
                {
                    'get':          { method: 'GET'  },
                    'create':       { method: 'POST' },
                    'update':       { method: 'PUT' },
                    'list':         { method: 'GET', isArray: true },
                    'delete':       { method: 'DELETE'},
                    'verify':       { method: 'POST', params: {} /* resets id */, url: 'rest/endpoints/grafana/verify'},
                }
            );
        })
        .controller('EndpointsController', ['$scope', function($scope) {
            $scope.types = [
                {id: 'grafana', label: 'Grafana'},
                // {id: 'elastic', label: 'ElasticSearch'}
            ]
            // TODO MVR handle error
            $scope.globalError = undefined;

            $scope.handleGlobalError = function() {
                // TODO MVR implement me
            }
        }])
        .controller('GrafanaEndpointsController', ['$scope', '$http', '$uibModal', 'GrafanaEndpointsService', function($scope, $http, $uibModal, GrafanaEndpointsService) {
            $scope.refresh = function() {
                $scope.endpoints = [];


                GrafanaEndpointsService.list(function(response) {
                    console.log("SUCCESS", response);
                    if (response && Array.isArray(response)) {
                        $scope.endpoints = response;
                        $scope.endpoints.forEach(function(item) {
                           item.revealApiKey = false;
                        });
                    }

                }, function(response) {
                    // TODO MVR handle error
                    console.log("ERROR", response);
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
                GrafanaEndpointsService.delete(deleteMe, function(response) {
                    $scope.refresh();
                }, function(response) {
                    // TODO MVR handle error
                   console.log("ERROR", response);
                });
            };

            $scope.editEndpoint = function(endpoint) {
                // TODO MVR editing should actually be cancellable, etc.
                var modalInstance = $scope.openModal(endpoint);
                modalInstance.result.then(function () {
                    $scope.refresh();
                },
                function() {
                    $scope.refresh();
                });
            };

            $scope.verifyEndpoint = function(endpoint) {
                $scope.xxx = undefined;
                $scope.yyy = true;
                GrafanaEndpointsService.verify({uid: endpoint.uid}, function(response) {
                    $scope.xxx = true;
                    $scope.yyy = false;
                }, function(response) {
                    $scope.xxx = false;
                    $scope.yyy = false;
                });
            };

            $scope.addNewEndpoint = function() {
                var modalInstance = $scope.openModal();
                modalInstance.closed.then(function () {
                    $scope.refresh(); // TODO MVR success??
                }, function() {
                    $scope.refresh(); // Failure
                });
            };

            $scope.refresh();
        }])
        .controller('GrafanaEndpointModalController', ['$scope', '$uibModalInstance', 'GrafanaEndpointsService', 'endpoint', function($scope, $uibModalInstance, GrafanaEndpointsService, endpoint) {
            $scope.endpoint = endpoint || {revealApiKey: false};
            $scope.buttonName = $scope.endpoint.id ? 'Update' : 'Create';
            $scope.verifyResult = undefined;
            $scope.error = {};

            var handleErrorResponse = function(response) {
                if (response.status === 400 && response.data) {
                    var errorObject = response.data;
                    if (errorObject.context && errorObject.message) {
                        $scope.error[errorObject.context] = errorObject.message;
                    } else if (errorObject.context) {
                        $scope.error[errorObject.context] = 'Invalid value';
                    } else {
                        growl.error('The configuration is not valid. Details were not provided');
                    }
                } else {
                    $scope.handleGlobalError(response);
                }
            };

            $scope.verify = function() {
                $scope.verifyResult = undefined;
                GrafanaEndpointsService.verify({apiKey: $scope.endpoint.apiKey, url: $scope.endpoint.url}, function(response) {
                    $scope.verifyResult = { type: 'success', message: 'Everything is awesome \\o/'};
                }, function(response) {
                    // TODO MVR add error handling
                    console.log("ERROR. OH NO", response);
                    $scope.verifyResult = { type: 'danger', message: 'Something went wrong'};
                });
            };

            $scope.save = function() {
                $scope.error = {};

                // Close modal afterwards
                var closeCallback = function() {
                    $uibModalInstance.close();
                };
                var object = {
                    id: $scope.endpoint.id,
                    uid: $scope.endpoint.uid,
                    url: $scope.endpoint.url,
                    apiKey: $scope.endpoint.apiKey,
                    description: $scope.endpoint.description
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
