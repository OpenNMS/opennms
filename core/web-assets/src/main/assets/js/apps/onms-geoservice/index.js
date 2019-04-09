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
            'ngResource',
            'ui.router',
            'ui.toggle',
            'onms.http',
            'onms.elementList',
        ])
        .config( ['$locationProvider', function ($locationProvider) {
            $locationProvider.hashPrefix('!');
            $locationProvider.html5Mode(false);
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
                    controller: 'GeocodingDetailsController'
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

        // TODO MVR error handling for all rest requests not implemented
        .controller('GeocoderController', ['$scope', '$http', '$sce', 'GeocodingConfigService', 'GeocodingGeocoderService', function($scope, $http, $sce, GeocodingConfigService, GeocodingGeocoderService) {
            $scope.config = {
                enabled : false,
                activeGeocoderId: undefined
            };
            $scope.geocoders = [];

            $scope.refreshTabs = function() {
                GeocodingConfigService.get(function(configResponse) {
                    $scope.config = configResponse;
                    $scope.config.enabled = $scope.config.enabled === true || $scope.config.enabled === 'true'; // Convert to boolean
                    $scope.config.activeGeocoderId = $scope.config.enabled ? $scope.config.activeGeocoderId : undefined; // ensure it is not set if disabled

                    console.log("CONFIG", $scope.config);
                    GeocodingGeocoderService.list(function(response) {
                        console.log(response);
                        if (response) {
                            // Remove noop
                            $scope.geocoders = response.filter(function(item) {
                                return item.id !== 'noop'
                            });
                            $scope.geocoders.forEach(function(item) {
                                item.name = item.id;
                            });
                        }
                    }, function(response) {
                        console.log("ERROR2", response);
                    });

                }, function(errorResponse) {
                   console.log("ERROR", errorResponse);
                });
            };

            $scope.refreshTabs();
        }])

        // TODO MVR error handling for all rest requests not implemented
        .controller('GeocoderConfigController', ['$scope', '$http', '$sce', 'GeocodingGeocoderService', function($scope, $http, $sce, GeocodingGeocoderService) {
            $scope.save = function() {
                console.log("Saved", $scope.config);
                // TODO MVR only do stuff, if we actually made any changes

                $scope.config.$update({}, function(response) {
                    console.log("SAVED SUCCESSFUL", response);
                    $scope.refreshTabs();
                }, function(response) {
                    console.log("SAVED FAILED", response);
                });
            };

            $scope.onGeocoderChange = function(selection) {
                // $scope.config = {};
                // $scope.config = $scope.geocoders.filter(function(item) {
                //     return item.id == selection;
                // })[0].config;
                // $scope.activeGeocoder = selection;
            };

            // $scope.refresh = function() {
            //     GeocodingGeocoderService.list(function(response) {
            //         console.log(response);
            //         $scope.config = {};
            //         if (response) {
            //             // Remove noop
            //             $scope.geocoders = response.filter(function(item) {
            //                return item.id !== 'noop'
            //             });
            //             $scope.geocoders.forEach(function(item) {
            //                 item.name = item.id;
            //             });
            //             var activeGeocoder = response.filter(function(item) {
            //                 return item.active === true;
            //             })[0];
            //             $scope.toggleState((activeGeocoder && activeGeocoder.id !== 'noop') || false);
            //         }
            //     }, function(response) {
            //         console.log("ERROR", response);
            //     });
            // };
            //
            // $scope.refresh();

        }])

        .controller('GeocodingDetailsController', ['$scope', '$stateParams', function($scope, $stateParams) {
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
                        $scope.success = "Changes saved successfully."
                    }, function (response) {
                        console.log("ERROR", response);
                        if (response.status === 400 && response.data) {
                            $scope.configError[response.data.context] = response.data.message ? response.data.message : "Invalid value";
                            console.log("WIUWIU", $scope.configError);
                        } else {
                            $scope.error = "An unexpected error occurred while saving the changes.";
                        }
                    });
                }
            };

            // Some fields need manual validation
            // TODO MVR only relevant for nominatim
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

        .controller('TODOController', function($scope) {
            $scope.addressQuery = "";

            $scope.getIframeSrc = function() {
                var iframeSrc = "http://maps.google.com/maps?output=embed&z=15&t=m&q=loc:" + encodeURIComponent($scope.resolvedAddress.latitude) +  "+" + encodeURIComponent($scope.resolvedAddress.longitude);
                return $sce.trustAsResourceUrl(iframeSrc);
            };

            $scope.verify = function() {
                if ($scope.addressQuery !== '') {
                    $scope.resolvedAddress = undefined;
                    // Simple GET request example:
                    $http({
                        method: 'GET',
                        url: 'api/v2/geocoding/resolve',
                        params: {query: $scope.addressQuery}
                    }).then(function successCallback(response) {
                        console.log("SUCCESS", response);
                        $scope.resolvedAddress = response.data;
                        // this callback will be called asynchronously
                        // when the response is available
                    }, function errorCallback(response) {
                        // called asynchronously if an error occurs
                        // or server returns response with an error status.
                        console.log("ERROR", response);
                    });
                }
            };
        })
    ;
}());
