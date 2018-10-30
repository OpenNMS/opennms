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
        'angular-loading-bar',
        'ngResource',
        'ui.router',
        'ui.bootstrap',
        'ui.checkbox',
        'ui.toggle',
        'onms.http',
        'onms.elementList',
        'mwl.confirm',
        'onms.pagination'
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
            $scope.serverUrl = location.protocol + '//' + location.hostname+(location.port ? ':'+location.port: '');


            $scope.daemons = [];


            $scope.LookupDaemonState = function (daemon) {
                if (daemon.reloadCount > 5) {
                    daemon.isReloading = false;
                    daemon.reloadNote = "Undefined";
                    daemon.reloadIconClass = "glyphicon glyphicon-question-sign";
                    return;
                }
                daemon.reloadCount = daemon.reloadCount + 1;

                $http.get('rest/daemons/checkReloadState/' + daemon.name + '/').then(function (response) {
                    if (response.data.reloadState === "Success") {
                        daemon.isReloading = false;
                        daemon.reloadNote = response.data.reloadState;
                        daemon.reloadIconClass = "glyphicon glyphicon glyphicon-ok-sign";
                    } else if (response.data.reloadState === "Failed") {
                        daemon.isReloading = false;
                        daemon.reloadNote = response.data.reloadState;
                        daemon.reloadIconClass = "glyphicon glyphicon glyphicon-remove-sign";
                    } else {
                        $timeout($scope.LookupDaemonState, 1000, true, daemon);
                    }
                });
            };

            $scope.doTheCopy = function (daemon) {
                //MAGIC
            };

            $scope.reloadPressed = function (daemon) {
                $http.post('rest/daemons/reload/' + daemon.name + '/')
                    .success(function (data, status) {
                        if (status === 204) {
                            var now = new Date();
                            daemon.reloadTime = now.getTime() + (now.getTimezoneOffset() * 60000);
                            daemon.showReloadState = true;
                            daemon.isReloading = true;
                            daemon.reloadCount = 0;
                            daemon.reloadNote = "Reloading...";
                            daemon.reloadIconClass = "fa fa-spinner fa-spin";
                            $timeout($scope.LookupDaemonState, 1000, true, daemon);
                        }
                    });
            };

            $scope.refreshDaemonList = function () {
                $http.get('rest/daemons').then(function (response) {
                    $scope.daemons = response.data
                        .filter(
                            function (element) {
                                return !element.internal;
                            });
                });
            };

            $scope.refreshDaemonList();
        }])
    ;
}());