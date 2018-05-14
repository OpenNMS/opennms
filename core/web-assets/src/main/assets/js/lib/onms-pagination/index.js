const angular = require('vendor/angular-js');
const paginationTemplate = require('./pagination-toolbar.html');

const MODULE_NAME = 'onms.pagination';

angular.module(MODULE_NAME, [ 'ui.bootstrap' ])
    .directive('pagination', function() {
        return {
            restrict: 'E',
            scope: {
                model: '=model',
                position: '@position',
                onChangeCallback: '=onChange'
            },
            link: function(scope, element, attrs) {
                if (scope.model === undefined) { throw new Error('No model defined.'); }
                if (scope.model.page === undefined) { throw new Error('No attribute model.page defined'); }
                if (scope.model.totalItems === undefined) { throw new Error('No attribute model.totalItems defined'); }
                if (scope.model.limit === undefined) { throw new Error('No attribute model.limit defined'); }

                var currentPage = scope.model.page;
                scope.onChange = function() {
                    if (currentPage !== scope.model.page) {
                        currentPage = scope.model.page;
                        if (scope.onChangeCallback) {
                            scope.onChangeCallback();
                        }
                    }
                };
            },
            transclude: true,
            templateUrl: paginationTemplate
        }
    });
