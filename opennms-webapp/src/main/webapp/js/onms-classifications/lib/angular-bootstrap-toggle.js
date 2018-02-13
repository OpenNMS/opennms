(function () {
    'use strict';

    angular.module('ui.toggle', [])

        .value('$toggleSuppressError', false)

        .constant('toggleConfig', {
            /**
             * This object defines supported toggle widget attributes and their default values.
             * Angular's ngClick and ngDisabled are handled separately. Search code below.
             */
            /**
             * This version simulates checkbox functionality which can have either true or false value.
             * User-defined values are not supported.
             */
            'btnCheckboxFalse': false,
            'btnCheckboxTrue' : true,
            /**
             * Type: string/html
             * Default: "On"
             * Description: Text of the on toggle
             */
            on: 'On',
            /**
             * Type: string/html
             * Default: "Off"
             * Description: Text of the off toggle
             */
            off: 'Off',
            /**
             * Type: string
             * Default: ''
             * Description: Allows to specify one of the standarg bootstrap's button sizes (class).
             * Possible values are btn-lg, btn-sm, btn-xs.
             */
            size: '',
            /**
             * Type: string
             * Default: "btn-primary"
             * Description: Class for "on" state from one of standard bootstrap button types.
             * Possible values: btn-default, btn-primary, btn-success, btn-info, btn-warning, btn-danger
             */
            onstyle: 'btn-primary',
            /**
             * Type: string
             * Default: "btn-default"
             * Description: Class for "off" state from one of standard bootstrap button types.
             * Possible values: btn-default, btn-primary,btn- success, btn-info, btn-warning, btn-danger
             */
            offstyle: 'btn-default',
            /**
             * Type: JSON string
             * Default: ''
             * Description: Allows to pass user-defined style to the toggle's first immediate child (first DIV inside
             * <toggle ...> which is what you actually see as widget's outer container).
             * This can be used to alter widget's appearance. Use with caution! Note that "width" and "height" values
             * will be overwritten by either auto-calculated values or used-specified values from "width" and "height"
             * attributes.
             * Example: <toggle ... toggle-style="{'border': '1px dashed #f00'}">
             */
            toggleStyle: '',
            /**
             * Type: string
             * Default: ''
             * Description: Allows to force width and height to specified value. Use css notation such as 50px, 1%. etc.
             * This is useful when you have a group of toggles with different text in the lables and, therefore,
             * would never line-up to the same width.
             * Example: <toggle ... width="90px">
             */
            width : '',
            height: '',
            /**
             * Type: boolean
             * Default: false
             * Description: Defines "disabled" attribute for the <toggle> directive itself. The ng-disabled dirrective
             * manipulates this attribute, plus there is additional code that propagates its value to child elements.
             * Applying "disabled" to <toggle> itself apparently does nothing, but when its value is propagated to
             * two child <label> elements, it allows us to disable the widget.
             * Note that attribute "diasbled" is not the same as ng-disabled Angular directive. In most cases, you should
             * use <toggle ... ng-disabled="expression"> (not <toggle ... disabled="{{expression}}">) for this to work
             * properly.
             * [Per HTML specs, the "disabled" property does not need a value. Just mentioning it is enough. Angular will,
             * however, also add the value "disabled" (< ... disabled="disabled">)]
             */
            disabled: false,
        })

        .controller('ToggleController',
            ['$scope', '$attrs', '$interpolate', '$log', 'toggleConfig', '$toggleSuppressError',
                function ($scope, $attrs, $interpolate, $log, toggleConfig, $toggleSuppressError) {
                    var self = this;
                    var labels, spans, divs;
                    var ngModelCtrl = {$setViewValue: angular.noop};
                    var toggleConfigKeys = Object.keys(toggleConfig);

                    // Configuration attributes
                    angular.forEach( toggleConfigKeys, function (k, i) {
                        if (angular.isDefined($attrs[k])) {
                            /*
                             if (i < toggleConfigKeys.length) {
                             self[k] = $interpolate($attrs[k])($scope.$parent);
                             } else {
                             self[k] = $scope.$parent.$eval($attrs[k]);
                             }
                             */
                            switch ( typeof toggleConfig[k] ) {
                                case 'string':
                                    self[k] = $interpolate($attrs[k])($scope.$parent);
                                    break;
                                case 'function':
                                    // TBD
                                    break;
                                default:
                                    self[k] = $scope.$parent.$eval($attrs[k]);
                            }
                        } else {    // use default from toggleConfig
                            self[k] = toggleConfig[k];
                        }
                    });

                    this.init = function (ngModelCtrl_) {
                        ngModelCtrl = ngModelCtrl_;

                        labels = self.element.find('label');
                        spans  = self.element.find('span');
                        divs   = self.element.find('div');
                        // ^-- divs[0] is the DIV that has class="toggle btn"
                        //     divs[1] is a child of [0] and has class="toggle-group"

                        // Set wigget's visible text such as On/Off or Enable/Disable
                        angular.element(labels[0]).html(self.on);
                        angular.element(labels[1]).html(self.off);

                        self.computeStyle();

                        ngModelCtrl.$render = function () {
                            self.toggle();
                        }

                        // ng-change (for optional onChange event handler)
                        if (angular.isDefined($attrs.ngChange)) {
                            ngModelCtrl.$viewChangeListeners.push(function () {
                                $scope.$eval($attrs.ngChange);
                            });
                        }
                    };

                    this.computeStyle = function () {
                        // Set wigget's disabled state.
                        // This action is unrelated to computing the style, but this function is the right place for it.
                        // The property must be propagated to lables and span inside the toggle-group container. This
                        // triggers .btn[disabled] style (cursor: not-allowed; opacity: 0.65;) but it does not prohibit
                        // the click event. Click event is handled in .onSwitch().
                        angular.element(labels[0]).attr('disabled', self.disabled);
                        angular.element(labels[1]).attr('disabled', self.disabled);
                        angular.element( spans[0]).attr('disabled', self.disabled);

                        // Build an object for widget's ng-style
                        $scope.wrapperStyle = (self.toggleStyle) ? $scope.$parent.$eval(self.toggleStyle) : {};

                        if (self.width) {
                            $scope.wrapperStyle.width = self.width;
                        } else {
                            // INCORRECT MATH - spans[0] overlaps two side-by-side LABEL's. Half of its width should not be included in the total.
                            //var wrapperComputedWidth = Math.max(labels[0].offsetWidth, labels[1].offsetWidth) + (spans[0].offsetWidth / 2);
                            var wrapperComputedWidth = Math.max(labels[0].offsetWidth, labels[1].offsetWidth);
                            var wrapperWidth = divs[0].offsetWidth;

                            if (wrapperWidth < wrapperComputedWidth) {
                                $scope.wrapperStyle.width = wrapperComputedWidth + 'px';
                            } else {
                                $scope.wrapperStyle.width = wrapperWidth + 'px';
                            }
                        }

                        if (self.height) {
                            $scope.wrapperStyle.height = self.height;
                        } else {
                            var wrapperComputedHeight = Math.max(labels[0].offsetHeight, labels[1].offsetHeight);
                            var wrapperHeight = divs[1].offsetHeight;

                            if (wrapperHeight < wrapperComputedHeight && self.size !== 'btn-xs' && self.size !== 'btn-sm') {
                                $scope.wrapperStyle.height = wrapperComputedHeight + 'px';
                            } else {
                                $scope.wrapperStyle.height = wrapperHeight + 'px';
                            }
                        }

                        // Build arrays that will be passed to widget's ng-class.
                        $scope.onClass     = [self.onstyle , self.size, 'toggle-on'];
                        $scope.offClass    = [self.offstyle, self.size, 'toggle-off'];
                        $scope.handleClass = [self.size , 'toggle-handle'];
                    };

                    this.toggle = function () {
                        if (angular.isDefined(ngModelCtrl.$viewValue)) {
                            if (ngModelCtrl.$viewValue) {
                                $scope.wrapperClass = [self.onstyle, self.size, self.style];
                            } else {
                                $scope.wrapperClass = [self.offstyle, 'off ', self.size, self.style];
                            }
                        } else {
                            $scope.wrapperClass = [self.offstyle, 'off ', self.size, self.style];
                        }
                    };

                    $scope.onSwitch = function (evt) {
                        if (self.disabled) {    // prevent changing .$viewValue if .disabled == true
                            return false;
                        } else {
                            ngModelCtrl.$setViewValue(!ngModelCtrl.$viewValue);
                            ngModelCtrl.$render();
                        }
                        return true;
                    };

                    // Watchable data attributes
                    angular.forEach(['ngModel'], function (key) {
                        var watch = $scope.$parent.$watch($attrs[key], function (value) {
                            ngModelCtrl.$render();
                        });
                        $scope.$parent.$on('$destroy', function () {
                            watch();
                        });
                    });

                    angular.forEach( toggleConfigKeys, function (k, i) {
                        $attrs.$observe(k, function (v) {
                            if (self[k] !== v) {
                                self[k] = v;
                                self.computeStyle();
                            }
                        });
                    });
                }])

        .directive('toggle', function () {
                return {
                    restrict: 'E',
                    transclude: true,
                    template: '<div class="toggle btn" ng-class="wrapperClass" ng-style="wrapperStyle" ng-click="onSwitch($event)">' +
                    '<div class="toggle-group">' +
                    '<label class="btn" ng-class="onClass"></label>' +
                    '<label class="btn active" ng-class="offClass"></label>' +
                    '<span class="btn btn-default" ng-class="handleClass"></span>' +
                    '</div>' +
                    '</div>',
                    scope: {
                        ngModel: '='
                    },
                    require: ['toggle', 'ngModel'],
                    controller: 'ToggleController',
                    controllerAs: 'toggle',
                    compile: function (element, attrs, transclude) {
                        return {
                            pre: function (scope, element, attrs, ctrls) {
                                var toggleCtrl = ctrls[0], ngModelCtrl = ctrls[1];
                                toggleCtrl.element = element;
                                toggleCtrl.init(ngModelCtrl);
                            },
                            post: function () {}
                        }
                    }
                };
            }
        );
})();
