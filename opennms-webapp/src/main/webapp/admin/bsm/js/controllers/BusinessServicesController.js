(function () {
    'use strict';

    var MODULE_NAME = 'businessServices';

    var bsCrudApp = angular.module(MODULE_NAME, ['ngResource', 'ngRoute']);

    bsCrudApp.config(function ($routeProvider) {
        $routeProvider
                .when('/',
                        {
                            controller: 'BusinessServicesListController',
                            templateUrl: 'admin/bsm/views/list.html'
                        })
                .when('/edit/:id',
                        {
                            controller: 'BusinessServicesEditController',
                            templateUrl: 'admin/bsm/views/edit.html'
                        })
                .otherwise({redirectTo:'/'});
    });

    angular.element(document).ready(function () {
        console.log('Bootstrapping ' + MODULE_NAME);
        angular.bootstrap(document, [MODULE_NAME]);
    });
}());