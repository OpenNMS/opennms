const angular = require('vendor/angular-js');
const elementList = require('../onms-elementList/lib/elementList');
require('../../lib/onms-http');
require('angular-bootstrap-toggle/dist/angular-bootstrap-toggle');
require('angular-bootstrap-toggle/dist/angular-bootstrap-toggle.css');
require('angular-ui-router');

const indexTemplate  = require('./index.html');

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
                .state('home', {
                    url: '/',
                    controller: 'GeoServiceController',
                    templateUrl: indexTemplate
                });
            $urlRouterProvider.otherwise('/');
        }])
        .factory('GeocodingService', function($resource) {
            return $resource('api/v2/geocoding/:id', {id: '@id'},
                {
                    'list': { method: 'GET', isArray: true },
                    'update': { method: 'POST' }
                }
            );
        })

        // TODO MVR error handling for all rest requests not implemented
        .controller('GeoServiceController', ['$scope', '$http', '$sce', 'GeocodingService', function($scope, $http, $sce, GeocodingService) {
            console.log("Bruder muss los");
            $scope.activeGeocoder = undefined; // TODO MVR should be the object and not the id!
            $scope.geocoders = [];
            $scope.config = {};
            $scope.active = false;
            $scope.addressQuery = "";

            $scope.save = function() {
                console.log("Saved", $scope.config);
                // TODO MVR only do stuff, if we actually made any changes

                var geocoder = $scope.geocoders.filter(function(item) {
                    return item.id == $scope.activeGeocoder;
                })[0];
                console.log("Found geocoder to invoke save on", geocoder);
                geocoder.$update($scope.config, function() {
                    console.log("save invoked");
                    // $scope.refresh();
                });
            };

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

            $scope.toggleState = function(active) {
                console.log("Toggle State", active);
                $scope.active = active;
                if ($scope.active === true) {
                    // Select active geocoder
                    $scope.geocoders.forEach(function(item) {
                        if (item.active === true) {
                            $scope.activeGeocoder = item.id;
                        }
                    });
                    if ($scope.activeGeocoder === undefined) {
                        $scope.activeGeocoder = $scope.geocoders[0].id;
                    }

                    // Get config
                    $scope.config = $scope.geocoders.filter(function(item) {
                        return item.id == $scope.activeGeocoder;
                    })[0].config;
                } else {
                    $scope.activeGeocoder = undefined;
                    $scope.config = {};
                }
            };

            $scope.onGeocoderChange = function(selection) {
                $scope.config = {};
                $scope.config = $scope.geocoders.filter(function(item) {
                    return item.id == selection;
                })[0].config;
                $scope.activeGeocoder = selection;
            };

            $scope.refresh = function() {
                GeocodingService.list(function(response) {
                    console.log(response);
                    $scope.config = {};
                    if (response) {
                        // Remove noop
                        $scope.geocoders = response.filter(function(item) {
                           return item.id !== 'noop'
                        });
                        $scope.geocoders.forEach(function(item) {
                            item.name = item.id;
                        });
                        var activeGeocoder = response.filter(function(item) {
                            return item.active === true;
                        })[0];
                        $scope.toggleState((activeGeocoder && activeGeocoder.id !== 'noop') || false);
                    }
                }, function(response) {
                    console.log("ERROR", response);
                });
            };

            $scope.refresh();

        }])
    ;
}());
