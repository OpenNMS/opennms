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
const curlModalTemplate  = require('./curl-modal.html');

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
        .controller('ListController', ['$scope', '$http', '$timeout', '$uibModal', function ($scope, $http, $timeout, $uibModal) {
            $scope.filter = 0; // filter for enabled and reloadable
            $scope.daemons = [];

            $scope.lookupDaemonState = function (daemon) {
                console.log("Checking daemon state");
                if (daemon.reloadCount > 5) {
                    daemon.isReloading = false;
                    daemon.reloadState = "Unknown";
                    return;
                }
                daemon.reloadCount = daemon.reloadCount + 1;

                $http.get('rest/daemons/reload/' + daemon.name + '/', {timeout: 1000})
                    .then(function (response) {
                        if (response.data.reloadState === "Success") {
                            daemon.isReloading = false;
                            daemon.reloadState = response.data.reloadState;
                        } else if (response.data.reloadState === "Failed") {
                            daemon.isReloading = false;
                            daemon.reloadState = response.data.reloadState;
                        } else {
                            $timeout($scope.lookupDaemonState, 1000, true, daemon);
                        }
                    }, function() {
                        $timeout($scope.lookupDaemonState, 1000, true, daemon);
                    });
            };

            $scope.reloadDaemon = function (daemon) {
                // Update state
                var now = new Date();
                daemon.reloadTime = now.getTime() + (now.getTimezoneOffset() * 60000);
                daemon.isReloading = true;
                daemon.reloadCount = 0;
                daemon.reloadState = "Reloading";

                // Trigger reload
                $http.post('rest/daemons/reload/' + daemon.name + '/', {}, {timeout : 5000});

                // Check each seconds for an update
                $timeout($scope.lookupDaemonState, 1000, true, daemon);
            };

            $scope.showCurlCommand = function(daemon) {
                var modalInstance = $uibModal.open({
                    size: 'lg',
                    templateUrl: curlModalTemplate,
                    controller: function($scope) {
                        $scope.serverUrl = location.protocol + '//' + location.hostname+(location.port ? ':'+location.port: '');
                        $scope.daemon = daemon;
                    },
                });
            };

            $scope.refreshDaemonList = function () {
                $http.get('rest/daemons')
                    .then(function (response) {
                        $scope.error = undefined;
                        $scope.daemons = response.data
                            .filter(
                                function (element) {
                                    // internal elements should not be shown to the user in any case
                                    if ($scope.filter == 0) {
                                        return !element.internal && element.enabled && element.reloadable;
                                    }
                                    return !element.internal;
                                });
                    },
                    function(response) {
                        $scope.error = "An unexpected error occurred while refreshing the daemon list.";
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