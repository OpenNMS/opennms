import ClockMode from './scripts/ClockMode';
import Time from './scripts/Time';
import ScheduleOptions from './scripts/ScheduleOptions';
import Types from './scripts/Types';
import Weekdays from './scripts/Weekdays.js';

const scheduleEditorTemplate = require('./templates/schedule-editor.tpl.html');
const intervalInputTemplate = require('./templates/interval-input.tpl.html');
const timeInputTemplate = require('./templates/time-input.tpl.html');

const MODULE_NAME = 'onms.schedule.editor';

angular.module(MODULE_NAME, [])
    .directive('onmsScheduleEditor', function() {
        return {
            restrict: 'E',
            templateUrl: scheduleEditorTemplate,
            scope: {
                id: '=?id',
                options: '=ngModel'
            },
            link: function(scope, element, attrs) {
                scope.id = scope.id || 'schedule';
                scope.options.type = scope.options.type || Types.DAILY;
                scope.options = new ScheduleOptions(scope.options);
                scope.weekdays = Weekdays.all;

                // Updates the cron expression on each option change
                scope.verifyInput = function() {
                    try {
                        scope.errors = undefined;
                        scope.options.errors = scope.errors;
                        scope.options.to.error = undefined;
                        scope.options.getCronExpression();
                    } catch (e) {
                        if (e.context === 'to') {
                            scope.options.to.error = e.message;
                            scope.errors = { to: e.message };
                        } else if (e.context) {
                            scope.errors = {};
                            scope.errors[e.context] = e.message;
                        } else {
                            console.log('Unexpected error occurred', e);
                        }
                        scope.options.errors = scope.errors;
                    }
                };

                // Ensure we validate and calculate the cron expression after any change
                scope.$watchGroup([
                    'options.type',
                    'options.interval',
                    'options.dayOfMonth',
                    'options.dayOfWeek',
                    'options.weekOfMonth',
                    'options.dayOfMonthToggle',
                    'options.cronExpression'
                ], scope.verifyInput);
                scope.$watchCollection('options.at', scope.verifyInput);
                scope.$watchCollection('options.from', scope.verifyInput);
                scope.$watchCollection('options.to', scope.verifyInput);
                scope.$watchCollection('options.daysOfWeek', scope.verifyInput);

                // Change the dayOfMonthToggle if any value was changed from the other "group"
                scope.$watch('options.dayOfMonth', function(newValue, oldValue) {
                    if (newValue !== oldValue && scope.options.dayOfMonthToggle !== 'dayOfMonth') {
                        scope.options.dayOfMonthToggle = 'dayOfMonth';
                    }
                });
                scope.$watchGroup(['options.dayOfWeek', 'options.weekOfMonth'], function(newValue, oldValue) {
                    if ((newValue[0] !== oldValue[0] || newValue[1] !== oldValue[1])
                            && scope.options.dayOfMonthToggle !== 'dayOfWeek') {
                        scope.options.dayOfMonthToggle = 'dayOfWeek';
                    }
                });
            }
        }
    })

    .directive('onmsScheduleInterval', function() {
        return {
            restrict: 'E',
            templateUrl: intervalInputTemplate,
            scope: {
                value: '=ngModel'
            },
        }
    })

    .directive('onmsTimeInput', [function() {
        return {
            restrict: 'E',
            templateUrl: timeInputTemplate,
            scope: {
                // The model which is used for rendering.
                // Must have hours, minutes and suffix property.
                // If suffix is not set, 24 hours format is assumed
                model: '=ngModel',

                // Defines if the time uses 24 hours format, or am/pm
                mode: '=?mode',

                // in case you don't want to show all minutes,
                // you can define the step size e.g. 5
                // to only show 0, 5, 10, etc.
                // Default is 5
                minutesStep: '=?minutesStep',

                // If you want to disable minute input
                disableMinutes: '=?disableMinutes'
            },
            link: function(scope, element, attrs) {
                // Set default options, if not defined
                if (scope.model.options === undefined) {
                    scope.options = {
                        mode: ClockMode.HALF_CLOCK_SYSTEM,
                        disableMinutes: false,
                        minutesStep: 5,
                    }
                } else {
                    scope.options = scope.model.options;
                }
                if (typeof scope.disableMinutes === 'boolean') {
                    scope.options.disableMinutes = scope.disableMinutes;
                }
                if (scope.mode === ClockMode.FULL_CLOCK_SYSTEM || scope.mode === ClockMode.HALF_CLOCK_SYSTEM) {
                    scope.options.mode = scope.mode;
                }
                if (typeof scope.minutesStep === 'number') {
                    scope.options.minutesStep = scope.minutesStep;
                }

                // Enforce either the defined rendered mode, or if not provided the mode of the model
                scope.options.mode = scope.options.mode || scope.model.mode;
                if (scope.options.mode !== ClockMode.FULL_CLOCK_SYSTEM && scope.options.mode !== ClockMode.HALF_CLOCK_SYSTEM) {
                    scope.options.mode = ClockMode.FULL_CLOCK_SYSTEM;
                }
                if (typeof scope.options.disableMinutes !== 'boolean') {
                    scope.options.disableMinutes = false;
                }
                if (typeof scope.options.minutesStep !== 'number') {
                    scope.options.minutesStep = 5;
                }

                // Verify model
                if (!(scope.model instanceof Time)) {
                    scope.model = new Time(scope.model);
                }

                // Enforce correct time visualization
                scope.model.convert(scope.options.mode);

                var initMinutes = function() {
                    scope.minutes = [];
                    for (var i=0; i<60; i+=scope.options.minutesStep) {
                        scope.minutes.push(i);
                    }
                    // Ensure the defined minutes are available
                    if (scope.minutes.indexOf(scope.model.minutes) === -1) {
                        scope.minutes.push(scope.model.minutes);
                    }
                    scope.minutes.sort(function(left, right) {
                        return parseInt(left, 10) - parseInt(right, 10);
                    });
                };
                var initHours = function() {
                    scope.hours = [];
                    var start = scope.model.mode === ClockMode.HALF_CLOCK_SYSTEM ? 1: 0;
                    var end = scope.model.mode === ClockMode.HALF_CLOCK_SYSTEM ? 12 : 23;
                    for (var i=start; i<=end; i++) {
                        scope.hours.push(i);
                    }
                };

                // Input variables
                initMinutes();
                initHours();

                // When minutesStep changed, update minutes values
                scope.$watch('options.minutesStep', function(newValue) {
                    initMinutes();
                });
                // When the mode changed, convert
                scope.$watch('options.mode', function(newValue, oldValue) {
                    if (newValue !== oldValue) {
                        // convert time
                        scope.model.convert(scope.options.mode);

                        // Reset hours as they are different for each mode
                        initHours();
                    }
                });
            }
        }
    }])
    ;
