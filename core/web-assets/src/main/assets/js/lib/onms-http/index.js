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
'use strict';

const angular = require('vendor/angular-js');

const permissionDeniedTemplate  = require('./403-permission-denied.html');

angular.module('onms.http', ['ui.bootstrap'])
    .factory('InterceptorService',['$q', '$rootScope', function($q, $rootScope) {
        return {
            responseError: function (rejection) {
                if (rejection.status === 401) {
                    if (rejection.config && rejection.config.url
                        && (rejection.config.url.startsWith('rest/')
                            || rejection.config.url.startsWith('api/v2/'))
                    ) {
                        console.error('Login Required', rejection, rejection.headers); // eslint-disable-line no-console
                        $rootScope.$emit('loginRequired');
                    }
                }
                if (rejection.status === 403) {
                    $rootScope.$emit('permissionDenied');
                }
                return $q.reject(rejection);
            }
        }
    }])
    .config(['$locationProvider', function($locationProvider) {
        $locationProvider.hashPrefix('');
    }])
    .config(['$httpProvider',
        function ($httpProvider) {
            $httpProvider.defaults.headers.common['X-Requested-With'] = 'XMLHttpRequest';
            $httpProvider.interceptors.push('InterceptorService');
        }
    ])
    .run(['$rootScope', '$uibModal', function($rootScope, $uibModal) {
        $rootScope.$on('loginRequired', function() {
            var baseTags = document.getElementsByTagName('base');
            if (baseTags && baseTags.length > 0 && baseTags[0].href) {
                document.headerLogoutForm.submit();
            } else {
                console.warn('Login is required, but cannot forward to login page due to missing base tag.'); // eslint-disable-line no-console
            }
        });

        $rootScope.$on('permissionDenied', function() {
            $uibModal.open({
                templateUrl: permissionDeniedTemplate,
                controller: function($scope, $uibModalInstance) {
                    $scope.reload = function () {
                        $uibModalInstance.dismiss();
                        window.location.reload();
                    };
                },
                size: '',
                backdrop: 'static',
                keyboard  : false
            });
        });
    }])
;

module.exports = angular;