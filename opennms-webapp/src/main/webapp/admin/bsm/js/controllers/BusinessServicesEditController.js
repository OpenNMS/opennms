(function () {
    'use strict';

    angular.module('businessServices')
        .controller('BusinessServicesEditController', ['$scope', '$location', '$window', '$log', '$filter', '$routeParams', 'BusinessServices', function ($scope, $location, $window, $log, $filter, $routeParams, BusinessServices) {
            $scope.bsToEdit = BusinessServices.get({id: $routeParams.id});
            $scope.bsUpdate = function () {
                if ($scope.bsToEdit.name !== "") {
                    $scope.updateBS = new BusinessServices({
                        id: $scope.bsToEdit.id,
                        name: $scope.bsToEdit.name
                    }).$update()
                        .then(function () {
                            $location.path('/');
                        })
                        .catch(function (req) {
                            $log.warn("update of BusinessService failed: " + req);
                        });
                }
            };

            $scope.bsCancel = function () {
                $location.path('/');
            };

        }]);
}());