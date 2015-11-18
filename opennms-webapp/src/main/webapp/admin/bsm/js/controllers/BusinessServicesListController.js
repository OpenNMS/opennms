(function () {
    'use strict';

    angular.module('businessServices')
        .controller('BusinessServicesListController', ['$scope', '$location', '$window', '$log', '$filter', 'BusinessServices', function ($scope, $location, $window, $log, $filter, BusinessServices) {
            $log.debug('BusinessServicesListController initializing...');
            // Fetch all of the items
            var listBS = function () {
                $log.debug("listBS called");

                BusinessServices.query(
                    {
                        limit: 0,
                        orderBy: 'name',
                        order: 'asc'
                    },
                    function (value, headers) {
                        $scope.items = value;
                    },
                    function (response) {
                        switch (response.status) {
                            case 404:
                                // If we didn't find any elements, then clear the list
                                $scope.items = [];
                                break;
                            case 401:
                            case 403:
                                // Handle session timeout by reloading page completely
                                $window.location.href = $location.absUrl();
                                break;
                        }
                    }
                );
            };
            listBS();

            $scope.bsCreate = function () {
                if ($scope.name.length > 0) {
                    $scope.newBS = new BusinessServices({name: $scope.name}).$save()
                        .then(function (newBS) {
                            listBS();
                            $scope.name = "";
                        })
                        .catch(function (req) {
                            $log.warn("create new BusinessService failed: " + req);
                        });
                }
            };

            $scope.bsDelete = function (id) {
                $log.debug("bsDelete");
                $log.debug("bsDelete id: " + id);
                new BusinessServices().$delete({id: id})
                    .then(function (id) {
                        listBS();
                    })
                    .catch(function (req) {
                        $log.warn("delete BusinessService failed: " + req);
                    });
            };

            $scope.goEdit = function (id) {
                $location.path('edit/' + id);
            };

        }]);
}());