'use strict';

angular.module('onms.http', [])
    .config(['$httpProvider',
        function ($httpProvider) {
            $httpProvider.defaults.headers.common['X-Requested-With'] = 'XMLHttpRequest';
        }
    ])
;
