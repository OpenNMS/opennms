(function () {
    'use strict';
    angular.module('businessServices').factory('BusinessServices', function ($resource, $log, $http) {
        return $resource('api/v2/business-services/:id', {id: '@id'},
            {
                'query': {
                    method: 'GET',
                    isArray: true,
                    // Append a transformation that will unwrap the item array
                    transformResponse: appendTransform($http.defaults.transformResponse, function (data, headers, status) {
                        // Always return the data as an array
                        return angular.isArray(data['business-service']) ? data['business-service'] : [data['business-service']];
                    })
                },
                'update': {
                    method: 'PUT',
                    headers: {
                        'Content-Type': 'application/json'
                    }
                }
            }
        );
    });

    /**
     * Function used to append an extra transformer to the default $http transforms.
     */
    function appendTransform(defaultTransform, transform) {
        defaultTransform = angular.isArray(defaultTransform) ? defaultTransform : [defaultTransform];
        return defaultTransform.concat(transform);
    }
}());