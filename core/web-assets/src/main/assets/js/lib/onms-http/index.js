'use strict';

const angular = require('vendor/angular-js');

angular.module('onms.http', [])
    .config(['$httpProvider',
        function ($httpProvider) {
            $httpProvider.defaults.headers.common['X-Requested-With'] = 'XMLHttpRequest';
        }
    ])
;

module.exports = angular;