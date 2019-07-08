'use strict';

require('expose-loader?moment!moment');
require('vendor/jquery-js');
require('tempusdominus-bootstrap-4/build/js/tempusdominus-bootstrap-4');
require('tempusdominus-bootstrap-4/build/css/tempusdominus-bootstrap-4.css');

const angular = require('vendor/angular-js');
const template  = require('./template.html');

angular.module('onms.datetimepicker', ['ui.bootstrap'])
    .directive('datetimepicker', function($timeout) {
        return {
            restrict: 'E',
            templateUrl: template,
            scope: {
                id: '@?id',
                format: '<?format',
                sideBySide: '@?expand',
                inline: '@?inline',
                locale: '<?locale',
                date: '=?ngModel',
                calendarWeeks: '@?calendarWeeks',
                maxDate: '=?maxDate',
                options: '=?options',
            },
            link: function(scope, element, attrs) {
                // Apply if id is provided
                if (scope.id) {
                    scope.elementId = scope.id;
                } else { // otherwise calculate it
                    scope.id = angular.element('[data-toggle="datetimepicker"]').length;
                    scope.elementId = 'datetimepicker-' + scope.id;
                }

                // Determine the options
                scope.options = scope.options || {};
                scope.options.sideBySide = scope.sideBySide === "true" || false;
                scope.options.inline = scope.inline === "true" || false;
                scope.options.calendarWeeks = scope.calendarWeeks === "true" || false;
                if (scope.locale) {
                    scope.options.locale = scope.locale;
                }
                if (scope.date) {
                    scope.options.defaultDate = scope.date;
                }
                if (scope.maxDate) {
                    scope.options.maxDate = scope.maxDate;
                }
                if (scope.format) {
                    scope.options.format = scope.format;
                }
                scope.options.buttons = {
                    showToday: true,
                    showClose: true
                };

                // Override the clear icon as it is using fa-delete by default which is not available
                scope.options.icons = {
                    clear: "fa fa-trash",
                    close: "fa fa-check",
                };

                // Listen for any changes
                element.bind('change.datetimepicker', function(event) {
                    scope.$apply(function() {
                        scope.date = event.date.toDate();
                    });
                });

                // Component is not yet rendered, so invoke it delayed
                $timeout(function() {
                    angular.element("#" + scope.elementId).datetimepicker(scope.options);
                }, 0);
            }
        }
    })
;

module.exports = angular;