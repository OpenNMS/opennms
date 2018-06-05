"use strict";

angular.module("ui.checkbox", []).directive("checkbox", function() {
	return {
		scope: {},
		require: "ngModel",
		restrict: "E",
		replace: "true",
		template: "<button type=\"button\" ng-style=\"stylebtn\" class=\"btn btn-default\" ng-class=\"{'btn-xs': size==='default', 'btn-sm': size==='large', 'btn-lg': size==='largest', 'checked': checked===true}\">" +
			"<span ng-style=\"styleicon\" class=\"glyphicon\" ng-class=\"{'glyphicon-ok': checked===true, 'glyphicon-minus': checked===undefined}\"></span>" +
			"</button>",
		compile: function compile(elem, attrs, transclude) {
			if(attrs.ngClass !== undefined) {
				attrs.ngClass = attrs.ngClass.replace(/}\s*{/g, ", ");
			}

			return function(scope, elem, attrs, modelCtrl) {
				scope.size = "default";
				// Default Button Styling
				scope.stylebtn = {};
				// Default Checkmark Styling
				scope.styleicon = {"width": "10px", "left": "-1px"};
				// If size is undefined, Checkbox has normal size (Bootstrap 'xs')
				if(attrs.large !== undefined) {
					scope.size = "large";
					scope.stylebtn = {"padding-top": "2px", "padding-bottom": "2px", "height": "30px"};
					scope.styleicon = {"width": "8px", "left": "-5px", "font-size": "17px"};
				}
				if(attrs.larger !== undefined) {
					scope.size = "larger";
					scope.stylebtn = {"padding-top": "2px", "padding-bottom": "2px", "height": "34px"};
					scope.styleicon = {"width": "8px", "left": "-8px", "font-size": "22px"};
				}
				if(attrs.largest !== undefined) {
					scope.size = "largest";
					scope.stylebtn = {"padding-top": "2px", "padding-bottom": "2px", "height": "45px"};
					scope.styleicon = {"width": "11px", "left": "-11px", "font-size": "30px"};
				}
				var indeterminate = false;
				if(attrs.indeterminate === "true") {
					indeterminate = true;
				}

				var trueValue = true;
				var falseValue = false;
				var indeterminateValue = undefined;

				// If defined set true value
				if(attrs.ngTrueValue !== undefined) {
					trueValue = attrs.ngTrueValue;
				}
				// If defined set false value
				if(attrs.ngFalseValue !== undefined) {
					falseValue = attrs.ngFalseValue;
				}
				// If defined set indeterminate value
				if(attrs.ngIndeterminateValue !== undefined) {
					indeterminateValue = attrs.ngIndeterminateValue;
				}

				// Check if name attribute is set and if so add it to the DOM element
				if(scope.name !== undefined) {
					elem.name = scope.name;
				}

				// Update element when model changes
				scope.$watch(function() {
					if(modelCtrl.$modelValue === trueValue || modelCtrl.$modelValue === true) {
						modelCtrl.$setViewValue(trueValue);
					} else if(indeterminate === true && (modelCtrl.$modelValue === indeterminateValue || modelCtrl.$modelValue === undefined)) {
						modelCtrl.$setViewValue(indeterminateValue);
					} else {
						modelCtrl.$setViewValue(falseValue);
					}
					return modelCtrl.$modelValue;
				}, function(newVal, oldVal) {
					if(indeterminate === true && modelCtrl.$modelValue === indeterminateValue) {
						scope.checked = undefined;
					} else {
						scope.checked = modelCtrl.$modelValue === trueValue;
					}
				}, true);

				// On click swap value and trigger onChange function
				elem.bind("click", function() {
					scope.$apply(function() {
						if(indeterminate === true) {
							if(modelCtrl.$modelValue === falseValue) {
								modelCtrl.$setViewValue(trueValue);
							} else if(modelCtrl.$modelValue === trueValue) {
								modelCtrl.$setViewValue(indeterminateValue);
							} else {
								modelCtrl.$setViewValue(falseValue);
							}
						} else {
							if(modelCtrl.$modelValue === falseValue) {
								modelCtrl.$setViewValue(trueValue);
							} else {
								modelCtrl.$setViewValue(falseValue);
							}
						}
					});
				});
			};
		}
	};
});