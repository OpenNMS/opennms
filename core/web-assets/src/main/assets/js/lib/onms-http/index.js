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
                window.location.href = baseTags[0].href + 'login.jsp?session_expired=true';
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