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
            'onms.elementList',
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

        .factory('GeocodingConfigService', function($resource) {
            return $resource('api/v2/geocoding/config', {}, {
                'get':      { method: 'GET' },
                'update':   { method: 'POST'}
            });
        })

        .factory('GeocodingGeocoderService', function($resource) {
            return $resource('api/v2/geocoding/geocoders/:id', {id: '@id'},
                {
                    'list': { method: 'GET', isArray: true },
                    'update': { method: 'POST' }
                }
            );
        })

        .controller('GeocoderController', ['$scope', '$http', '$sce', 'GeocodingConfigService', 'GeocodingGeocoderService', function($scope, $http, $sce, GeocodingConfigService, GeocodingGeocoderService) {

            $scope.handleGlobalError = function(errorResponse) {
                $scope.globalError = "An unexpected error occurred: " + errorResponse.statusText;
                $scope.globalErrorDetails = JSON.stringify(errorResponse, null, 2);
            };

            $scope.refreshTabs = function() {
                $scope.config = {
                    enabled: false,
                    activeGeocoderId: undefined
                };
                $scope.geocoders = [];
                $scope.globalError = undefined;

                GeocodingConfigService.get(function (configResponse) {
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

            $scope.refreshTabs();
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
        }])

        .controller('GeocoderDetailsController', ['$scope', '$stateParams', 'growl', function($scope, $stateParams, growl) {
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
                    }, function (response) {
                        if (response.status === 400 && response.data) {
                            if (response.data.context && response.data.message) {
                                $scope.configError[response.data.context] = response.data.message;
                            } else if (response.data.context) {
                                $scope.configError[response.data.context] = 'Invalid value';
                            } else {
                                growl.error('The configuration is not valid. Details were not provided');
                            }
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

            $scope.$watch('geocoders', function(newValue, oldValue) {
                var matchingGeocoders = newValue.filter(function(item) {
                    return item.id === $stateParams.id
                });
                if (matchingGeocoders.length > 0) {
                    $scope.geocoder = matchingGeocoders[0];
                }
            });
            // TODO MVR block as long as geocoder is not initialized

        }])
    ;
}());
