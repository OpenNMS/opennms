// noinspection JSAnnotator
const angular = require('vendor/angular-js');
const elementList = require('../onms-elementList/lib/elementList');
require('../../lib/onms-pagination');
require('../../lib/onms-http');
require('angular-bootstrap-confirm');
require('angular-bootstrap-toggle/dist/angular-bootstrap-toggle');
require('angular-bootstrap-toggle/dist/angular-bootstrap-toggle.css');
require('angular-ui-router');

const indexTemplate = require('./index.html');

(function () {
    'use strict';

    var MODULE_NAME = 'onms.daemons';

    angular.module(MODULE_NAME, [
        'ui.router',
        'ui.bootstrap',
        'ui.toggle',
        'mwl.confirm',
        'onms.http'
    ])
        .config(['$locationProvider', function ($locationProvider) {
            $locationProvider.hashPrefix('!');
            $locationProvider.html5Mode(false);
        }])
        .config(['$stateProvider', function ($stateProvider) {
            $stateProvider
                .state('daemons', {
                    url: '',
                    controller: 'ListController',
                    templateUrl: indexTemplate
                });
        }])
        .controller('ListController', ['$scope', '$http', '$timeout', function ($scope, $http, $timeout) {

            // This is used for the curl command-string in the view
            $scope.serverUrl = location.protocol + '//' + location.hostname+(location.port ? ':'+location.port: '');
            $scope.filter = 0; // filter for enabled and reloadable
            $scope.daemons = [];

            $scope.lookupDaemonState = function (daemon) {
                if (daemon.reloadCount > 5) {
                    daemon.isReloading = false;
                    daemon.reloadState = "Undefined";
                    return;
                }
                daemon.reloadCount = daemon.reloadCount + 1;

                $http.get('rest/daemons/reload/' + daemon.name + '/').then(function (response) {
                    if (response.data.reloadState === "Success") {
                        daemon.isReloading = false;
                        daemon.reloadState = response.data.reloadState;
                    } else if (response.data.reloadState === "Failed") {
                        daemon.isReloading = false;
                        daemon.reloadState = response.data.reloadState;
                    } else {
                        $timeout($scope.lookupDaemonState, 1000, true, daemon);
                    }
                });
            };

            $scope.reloadDaemon = function (daemon) {
                $http.post('rest/daemons/reload/' + daemon.name + '/')
                    .success(function (data, status) {
                        if (status === 204) {
                            var now = new Date();
                            daemon.reloadTime = now.getTime() + (now.getTimezoneOffset() * 60000);
                            daemon.isReloading = true;
                            daemon.reloadCount = 0;
                            daemon.reloadState = "Reloading";
                            $timeout($scope.lookupDaemonState, 1000, true, daemon);
                        }
                    });
            };

            $scope.refreshDaemonList = function () {
                $http.get('rest/daemons').then(function (response) {
                    $scope.daemons = response.data
                        .filter(
                            function (element) {
                                if ($scope.filter == 0) {
                                    return !element.internal && element.enabled && element.reloadable;
                                }
                                return !element.internal;
                            });
                });
            };

            $scope.refreshDaemonList();

            // If the filter changes, we refresh the daemon list
            $scope.$watchCollection('filter', function () {
                $scope.refreshDaemonList();
            });
        }])
    ;
}());