'use strict';

const angular = require('vendor/angular-js');

angular.module('onms.http', [])
    .factory('InterceptorService',['$q', '$rootScope', function($q, $rootScope) {
        return {
            responseError: function (rejection) {
                if (rejection.status === 401) {
                    if (rejection.config && rejection.config.url
                        && (rejection.config.url.startsWith("rest/")
                            || rejection.config.url.startsWith("api/v2/"))
                    ) {
                        console.log("Login Required", rejection, rejection.headers);
                        $rootScope.$emit('loginRequired');
                    }
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
    .run(['$rootScope', function($rootScope) {
        $rootScope.$on('loginRequired', function() {
            var baseTags = document.getElementsByTagName('base');
            if (baseTags && baseTags.length > 0 && baseTags[0].href) {
                window.location.href = baseTags[0].href + 'login.jsp?session_expired=true';
            } else {
                console.log("Login is required, but cannot forward to login page due to missing base tag.");
            }
        });
    }])
;

module.exports = angular;