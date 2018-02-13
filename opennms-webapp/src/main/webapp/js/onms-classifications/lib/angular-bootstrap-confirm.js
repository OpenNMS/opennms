/**
 * angular-bootstrap-confirm - Displays a bootstrap confirmation popover when clicking the given element.
 * @version v2.5.1
 * @link https://github.com/mattlewis92/angular-bootstrap-confirm
 * @license MIT
 */
(function webpackUniversalModuleDefinition(root, factory) {
    if(typeof exports === 'object' && typeof module === 'object')
        module.exports = factory(require("angular"), require("angular-sanitize"));
    else if(typeof define === 'function' && define.amd)
        define(["angular", "angular-sanitize"], factory);
    else if(typeof exports === 'object')
        exports["angularBootstrapConfirmModuleName"] = factory(require("angular"), require("angular-sanitize"));
    else
        root["angularBootstrapConfirmModuleName"] = factory(root["angular"], root["angular-sanitize"]);
})(this, function(__WEBPACK_EXTERNAL_MODULE_1__, __WEBPACK_EXTERNAL_MODULE_3__) {
    return /******/ (function(modules) { // webpackBootstrap
        /******/ 	// The module cache
        /******/ 	var installedModules = {};

        /******/ 	// The require function
        /******/ 	function __webpack_require__(moduleId) {

            /******/ 		// Check if module is in cache
            /******/ 		if(installedModules[moduleId])
            /******/ 			return installedModules[moduleId].exports;

            /******/ 		// Create a new module (and put it into the cache)
            /******/ 		var module = installedModules[moduleId] = {
                /******/ 			exports: {},
                /******/ 			id: moduleId,
                /******/ 			loaded: false
                /******/ 		};

            /******/ 		// Execute the module function
            /******/ 		modules[moduleId].call(module.exports, module, module.exports, __webpack_require__);

            /******/ 		// Flag the module as loaded
            /******/ 		module.loaded = true;

            /******/ 		// Return the exports of the module
            /******/ 		return module.exports;
            /******/ 	}


        /******/ 	// expose the modules object (__webpack_modules__)
        /******/ 	__webpack_require__.m = modules;

        /******/ 	// expose the module cache
        /******/ 	__webpack_require__.c = installedModules;

        /******/ 	// __webpack_public_path__
        /******/ 	__webpack_require__.p = "";

        /******/ 	// Load entry module and return exports
        /******/ 	return __webpack_require__(0);
        /******/ })
    /************************************************************************/
    /******/ ([
        /* 0 */
        /***/ function(module, exports, __webpack_require__) {

            'use strict';

            var angular = __webpack_require__(1);
            var defaultPopoverTemplate = __webpack_require__(2);
            __webpack_require__(3);
            __webpack_require__(1);
            var DEFAULT_POPOVER_URL = 'angular-bootstrap-confirm.html';

            module.exports = angular

                .module('mwl.confirm', [
                    'ngSanitize',
                    'ui.bootstrap.position'
                ])

                .run(["$templateCache", function($templateCache) {
                    $templateCache.put(DEFAULT_POPOVER_URL, defaultPopoverTemplate);
                }])

                .controller('PopoverConfirmCtrl', ["$scope", "$rootScope", "$element", "$attrs", "$compile", "$document", "$window", "$timeout", "$injector", "$templateRequest", "$parse", "$log", "$animate", "confirmationPopoverDefaults", function($scope, $rootScope, $element, $attrs, $compile, $document, $window, $timeout,
                                                                                                                                                                                                                                                        $injector, $templateRequest, $parse, $log, $animate, confirmationPopoverDefaults) {
                    var vm = this;
                    vm.defaults = confirmationPopoverDefaults;
                    vm.$attrs = $attrs;
                    var positionServiceName = $injector.has('$uibPosition') ? '$uibPosition' : '$position';
                    var positionService = $injector.get(positionServiceName);
                    var templateUrl = $attrs.templateUrl || confirmationPopoverDefaults.templateUrl;
                    var popoverScope = $rootScope.$new(true);
                    var animation = vm.animation = $attrs.animation === 'true' || confirmationPopoverDefaults.animation;
                    popoverScope.vm = vm;

                    function assignOuterScopeValue(attributeName, value) {
                        var scopeName = $attrs[attributeName];
                        if (angular.isDefined(scopeName)) {
                            if ($parse(scopeName).assign) {
                                $parse(scopeName).assign($scope, value);
                            } else {
                                $log.warn('Could not set value of ' + attributeName + ' to ' + value + '. This is normally because the value is not a variable.');
                            }
                        }
                    }

                    function evaluateOuterScopeValue(scopeName, defaultValue, locals) {
                        if (angular.isDefined(scopeName)) {
                            return $parse(scopeName)($scope, locals);
                        } else {
                            return defaultValue;
                        }
                    }

                    var popoverLoaded = $templateRequest(templateUrl).then(function(template) {
                        var popover = angular.element(template);
                        popover.css('display', 'none');
                        $compile(popover)(popoverScope);
                        $document.find('body').append(popover);
                        return popover;
                    });

                    vm.isVisible = false;

                    function positionPopover() {
                        popoverLoaded.then(function(popover) {
                            var position = positionService.positionElements($element, popover, $attrs.placement || vm.defaults.placement, true);
                            position.top += 'px';
                            position.left += 'px';
                            popover.css(position);
                        });
                    }

                    function applyFocus() {
                        var buttonToFocus = $attrs.focusButton || vm.defaults.focusButton;
                        if (buttonToFocus) {
                            popoverLoaded.then(function(popover) {
                                var targetButtonClass = buttonToFocus + '-button';
                                popover[0].getElementsByClassName(targetButtonClass)[0].focus();
                            });
                        }
                    }

                    function showPopover() {
                        if (!vm.isVisible && !evaluateOuterScopeValue($attrs.isDisabled, false)) {
                            popoverLoaded.then(function(popover) {
                                popover.css({display: 'block'});
                                if (animation) {
                                    $animate.addClass(popover, 'in');
                                }
                                positionPopover();
                                applyFocus();
                                vm.isVisible = true;
                                assignOuterScopeValue('isOpen', true);
                            });
                        }
                    }

                    function hidePopover() {
                        if (vm.isVisible) {
                            popoverLoaded.then(function(popover) {
                                if (animation) {
                                    $animate.removeClass(popover, 'in');
                                }
                                popover.css({display: 'none'});
                                vm.isVisible = false;
                                assignOuterScopeValue('isOpen', false);
                            });
                        }
                    }

                    function togglePopover() {
                        if (!vm.isVisible) {
                            showPopover();
                        } else {
                            hidePopover();
                        }
                    }

                    function documentClick(event) {
                        popoverLoaded.then(function(popover) {
                            if (vm.isVisible && !popover[0].contains(event.target) && !$element[0].contains(event.target)) {
                                hidePopover();
                            }
                        });
                    }

                    vm.showPopover = showPopover;
                    vm.hidePopover = hidePopover;
                    vm.togglePopover = togglePopover;

                    vm.onConfirm = function(callbackLocals) {
                        evaluateOuterScopeValue($attrs.onConfirm, null, callbackLocals);
                    };

                    vm.onCancel = function(callbackLocals) {
                        evaluateOuterScopeValue($attrs.onCancel, null, callbackLocals);
                    };

                    $scope.$watch($attrs.isOpen, function(newIsOpenValue) {
                        $timeout(function() { //timeout required so that documentClick() event doesn't fire and close it
                            if (newIsOpenValue) {
                                showPopover();
                            } else {
                                hidePopover();
                            }
                        });
                    });

                    $element.bind('click', togglePopover);

                    $window.addEventListener('resize', positionPopover);

                    $document.bind('click', documentClick);
                    $document.bind('touchend', documentClick);

                    $scope.$on('$destroy', function() {
                        popoverLoaded.then(function(popover) {
                            popover.remove();
                            $element.unbind('click', togglePopover);
                            $window.removeEventListener('resize', positionPopover);
                            $document.unbind('click', documentClick);
                            $document.unbind('touchend', documentClick);
                            popoverScope.$destroy();
                        });
                    });

                }])

                .directive('mwlConfirm', function() {

                    return {
                        restrict: 'A',
                        controller: 'PopoverConfirmCtrl'
                    };

                })

                .value('confirmationPopoverDefaults', {
                    confirmText: 'Confirm',
                    cancelText: 'Cancel',
                    confirmButtonType: 'success',
                    cancelButtonType: 'default',
                    placement: 'top',
                    focusButton: null,
                    templateUrl: DEFAULT_POPOVER_URL,
                    hideConfirmButton: false,
                    hideCancelButton: false,
                    animation: false
                })

                .name;


            /***/ },
        /* 1 */
        /***/ function(module, exports) {

            module.exports = __WEBPACK_EXTERNAL_MODULE_1__;

            /***/ },
        /* 2 */
        /***/ function(module, exports) {

            module.exports = "<div\n  class=\"popover\"\n  ng-class=\"[vm.$attrs.placement || vm.defaults.placement, 'popover-' + (vm.$attrs.placement || vm.defaults.placement), vm.$attrs.popoverClass || vm.defaults.popoverClass, {fade: vm.animation}]\">\n  <div class=\"popover-arrow arrow\"></div>\n  <h3 class=\"popover-title\" ng-bind-html=\"vm.$attrs.title\"></h3>\n  <div class=\"popover-content\">\n    <p ng-bind-html=\"vm.$attrs.message\"></p>\n    <div class=\"row\">\n      <div\n        class=\"col-xs-6\"\n        ng-if=\"!vm.$attrs.hideConfirmButton && !vm.defaults.hideConfirmButton\"\n        ng-class=\"{'col-xs-offset-3': vm.$attrs.hideCancelButton || vm.defaults.hideCancelButton}\">\n        <button\n          class=\"btn btn-block confirm-button\"\n          ng-class=\"'btn-' + (vm.$attrs.confirmButtonType || vm.defaults.confirmButtonType)\"\n          ng-click=\"vm.onConfirm(); vm.hidePopover()\"\n          ng-bind-html=\"vm.$attrs.confirmText || vm.defaults.confirmText\">\n        </button>\n      </div>\n      <div\n        class=\"col-xs-6\"\n        ng-if=\"!vm.$attrs.hideCancelButton && !vm.defaults.hideCancelButton\"\n        ng-class=\"{'col-xs-offset-3': vm.$attrs.hideConfirmButton || vm.defaults.hideConfirmButton}\">\n        <button\n          class=\"btn btn-block cancel-button\"\n          ng-class=\"'btn-' + (vm.$attrs.cancelButtonType || vm.defaults.cancelButtonType)\"\n          ng-click=\"vm.onCancel(); vm.hidePopover()\"\n          ng-bind-html=\"vm.$attrs.cancelText || vm.defaults.cancelText\">\n        </button>\n      </div>\n    </div>\n  </div>\n</div>\n"

            /***/ },
        /* 3 */
        /***/ function(module, exports) {

            module.exports = __WEBPACK_EXTERNAL_MODULE_3__;

            /***/ }
        /******/ ])
});
;