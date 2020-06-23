const angular = require('vendor/angular-js');
const elementList = require('../onms-elementList/lib/elementList');
require('../../lib/onms-http');
require('angular-bootstrap-toggle/dist/angular-bootstrap-toggle');
require('angular-bootstrap-toggle/dist/angular-bootstrap-toggle.css');
require('angular-ui-router');

const indexTemplate  = require('./views/index.html');
const configTemplate  = require('./views/config.html');

// Dynamically referenced
const nominatimTemplate  = require('./views/config/nominatim.html');
const googleTemplate  = require('./views/config/google.html');
const mapquestTemplate  = require('./views/config/mapquest.html');

(function() {
    'use strict';

    var MODULE_NAME = 'onms.geoservice';

    angular.module(MODULE_NAME, [
            'angular-loading-bar',
            'angular-growl',
            'ngResource',
            'ui.router',
            'ui.toggle',
            'onms.http',
            'onms.elementList'
        ])
        .config(['$locationProvider', function ($locationProvider) {
            $locationProvider.hashPrefix('!');
            $locationProvider.html5Mode(false);
        }])
        .config(['growlProvider', function(growlProvider) {
            growlProvider.globalTimeToLive({success: 2000, error: 5000, warning: 3000, info: 4000});
            growlProvider.globalPosition('bottom-center');
        }])
        .config(['$stateProvider', '$urlRouterProvider', function ($stateProvider, $urlRouterProvider) {
            $stateProvider
                .state('geocoding', {
                    url: '/geocoding',
                    controller: 'GeocoderController',
                    templateUrl: indexTemplate
                })
                .state('geocoding.config', {
                    templateUrl: configTemplate,
                    url: '/config',
                    controller: 'GeocoderConfigController'
                })

                .state('geocoding.details', {
                    templateUrl: function($stateParams) {
                        return require('./views/config/' + $stateParams.id + '.html');
                    },
                    url: '/:id',
                    controller: 'GeocoderDetailsController'
                })
            ;
            $urlRouterProvider.otherwise('/geocoding/config');
        }])

        .filter('capitalize', function() {
            return function(input) {
                return input ? input.charAt(0).toUpperCase() + input.substr(1).toLowerCase() : '';
            }
        })

        .factory('GeocodingConfigService', /* @ngInject */ function($resource) {
            return $resource('api/v2/geocoding/config', {}, {
                'get':      { method: 'GET' },
                'update':   { method: 'POST'}
            });
        })

        .factory('GeocodingGeocoderService', /* @ngInject */ function($resource) {
            return $resource('api/v2/geocoding/geocoders/:id', {id: '@id'},
                {
                    'list': { method: 'GET', isArray: true },
                    'update': { method: 'POST' }
                }
            );
        })

        .controller('GeocoderController', ['$scope', '$http', '$sce', 'GeocodingConfigService', 'GeocodingGeocoderService', function($scope, $http, $sce, GeocodingConfigService, GeocodingGeocoderService) {

            $scope.handleGlobalError = function(errorResponse) {
                $scope.globalError = 'An unexpected error occurred: ' + errorResponse.statusText;
                $scope.globalErrorDetails = JSON.stringify(errorResponse, null, 2);
            };

            $scope.refreshTabs = function() {
                $scope.config = {
                    enabled: false,
                    activeGeocoderId: undefined
                };
                $scope.geocoders = [];
                $scope.globalError = undefined;

                return GeocodingConfigService.get(function (configResponse) {
                    $scope.config = configResponse;
                }, function (errorResponse) {
                    $scope.handleGlobalError(errorResponse);
                }).$promise
                    .then(function () {
                        return GeocodingGeocoderService.list(function (response) {
                            $scope.geocoders = response;
                            $scope.geocoders.forEach(function (item) {
                                item.name = item.id;
                                item.active = $scope.config.activeGeocoderId === item.id;
                            });
                            $scope.geocoders.sort(function (a, b) {
                                return a.name.localeCompare(b.name);
                            });
                        }, function (errorResponse) {
                            $scope.handleGlobalError(errorResponse);
                        }).$promise;
                    });
            };
        }])

        .controller('GeocoderConfigController', ['$scope', 'growl', function($scope, growl) {
            $scope.onGeocoderChange = function(selection) {
                $scope.config.activeGeocoderId = selection.active ? selection.id : undefined;
                $scope.config.$update(function(response) {
                    growl.success('Changes saved successfully');
                    $scope.geocoders.forEach(function(item) {
                        item.active = $scope.config.activeGeocoderId === item.id;
                    });
                }, function(response) {
                    $scope.handleGlobalError(response);
                });
            };

            $scope.refreshTabs();
        }])

        .controller('GeocoderDetailsController', ['$scope', '$stateParams', 'growl', function($scope, $stateParams, growl) {
            $scope.geocoders = [];
            $scope.geocoder = undefined;
            $scope.configError = {};
            $scope.manualValidation = {
                'mapquest': function() {
                    return true;
                },
                'google': function() {
                    return true;
                },
                'nominatim': function() {
                    // Ensure that either referer or userAgent are defined
                    $scope.configError['userAgent'] = undefined;
                    $scope.configError['referer'] = undefined;

                    if (!$scope.geocoder.config.userAgent && !$scope.geocoder.config.referer) {
                        $scope.configError['userAgent'] = 'User Agent or Referer must be set';
                        $scope.configError['referer'] = $scope.configError['userAgent'];
                        return false;
                    }
                    return true;
                }
            };

            $scope.handleGeocoderConfigErrors = function(errorObject) {
                if (errorObject.context && errorObject.message) {
                    $scope.configError[errorObject.context] = errorObject.message;
                } else if (errorObject.context) {
                    $scope.configError[errorObject.context] = 'Invalid value';
                } else {
                    growl.error('The configuration is not valid. Details were not provided');
                }
            };

            $scope.save = function() {
                if ($scope.geocoder) {
                    if (!$scope.validateFieldsManually()) {
                        return;
                    }
                    // Now update
                    $scope.configError = {};
                    $scope.geocoder.$update(function () {
                        growl.success('Changes saved successfully.');
                        $scope.form.$setPristine();

                        // The data cannot be reloaded from the backend as the bundles will be refreshed and the
                        // ReST service will most like not be available. To avoid 404s, we assume on success everything is fine
                        // as otherwise the response would be a 400 with information what exactly is not okay
                        $scope.geocoder.error = undefined;
                    }, function (response) {
                        if (response.status === 400 && response.data) {
                            $scope.handleGeocoderConfigErrors(response.data);
                        } else {
                            $scope.handleGlobalError(response);
                        }
                    });
                }
            };

            // Some fields need manual validation
            $scope.validateFieldsManually = function() {
               var manualValidation = $scope.manualValidation[$scope.geocoder.id];
               if (manualValidation) {
                   return manualValidation();
               }
               return true;
            };

            // Refresh data
            $scope.refreshTabs().then(function(geocoders) {
                    var matchingGeocoders = geocoders.filter(function(item) {
                        return item.id === $stateParams.id;
                    });
                    if (matchingGeocoders.length > 0) {
                        $scope.geocoder = matchingGeocoders[0];
                        if ($scope.geocoder.error) {
                            $scope.handleGeocoderConfigErrors($scope.geocoder.error);
                        }
                    }
            });
        }])
    ;
}());
