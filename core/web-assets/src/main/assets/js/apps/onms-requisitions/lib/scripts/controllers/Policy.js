require('../services/Requisitions');

/**
* @author Alejandro Galue <agalue@opennms.org>
* @copyright 2014 The OpenNMS Group, Inc.
*/

(function() {

  'use strict';

  const policyParamOptionsView = require('../../views/policy-param.options.html');
  const policyParamStringView = require('../../views/policy-param.string.html');
  const policyParamFixedView = require('../../views/policy-param.fixed.html');
  const policyParamEditableView = require('../../views/policy-param.editable.html');

  angular.module('onms-requisitions')

  /**
  * @ngdoc controller
  * @name PolicyController
  * @module onms-requisitions
  *
  * @requires $scope Angular local scope
  * @requires $uibModalInstance Angular UI modal instance
  * @requires RequisitionsService The Requisitions Servive
  * @requires policy Requisition policy object
  *
  * @description The controller for manage the modal dialog for add/edit requisition policies
  */
  .controller('PolicyController', ['$scope', '$uibModalInstance', 'RequisitionsService', 'policy', function($scope, $uibModalInstance, RequisitionsService, policy) {

    /**
    * @description The policy object
    *
    * @ngdoc property
    * @name PolicyController#policy
    * @propertyOf PolicyController
    * @returns {object} The policy object
    */
    $scope.policy = policy;

    /**
    * @description The available policies array
    *
    * @ngdoc property
    * @name PolicyController#availablePolicies
    * @propertyOf PolicyController
    * @returns {array} The policy list
    */
    $scope.availablePolicies = [];

    /**
    * @description The optional parameters array
    *
    * @ngdoc property
    * @name PolicyController#optionalParameters
    * @propertyOf PolicyController
    * @returns {array} The optional parameters list
    */
    $scope.optionalParameters = [];

    /**
    * @description Saves the current policy
    *
    * @name PolicyController:save
    * @ngdoc method
    * @methodOf PolicyController
    */
    $scope.save = function () {
      $uibModalInstance.close($scope.policy);
    };

    /**
    * @description Cancels the current operation
    *
    * @name PolicyController:cancel
    * @ngdoc method
    * @methodOf PolicyController
    */
    $scope.cancel = function () {
      $uibModalInstance.dismiss('cancel');
    };

    /**
    * @description Adds a new empty parameter to the current policy
    *
    * @name PolicyController:addParameter
    * @ngdoc method
    * @methodOf PolicyController
    */
    $scope.addParameter = function() {
      $scope.policy.parameter.push({ 'key': '', 'value': '' });
    };

    /**
    * @description Removes a parameter from the current policy
    *
    * @name PolicyController:removeParameter
    * @ngdoc method
    * @methodOf PolicyController
    * @param {integer} index The index of the parameter to remove
    */
    $scope.removeParameter = function(index) {
      $scope.policy.parameter.splice(index, 1);
    };

    /**
    * @description Update policy parameters after changing the policy class.
    *
    * @name PolicyController:updatePolicyParameters
    * @ngdoc method
    * @methodOf PolicyController
    * @param {object} policyConfig the configuration of the selected policy
    */
    $scope.updatePolicyParameters = function(policyConfig) {
      if (!policyConfig) {
        return;
      }
      $scope.policy.parameter = [];
      angular.forEach($scope.availablePolicies, function(policy) {
        if (policy.class === policyConfig.class) {
          angular.forEach(policyConfig.parameters, function(param) {
            if (param.required) {
              $scope.policy.parameter.push({ 'key': param.key, 'value': null });
            }
          });
        }
      });
    };

    /**
    * @description Checks if an object is a non empty array
    *
    * @private
    * @name PolicyController:updatePolicyParameters
    * @ngdoc method
    * @methodOf PolicyController
    * @param {object} myArray the object to check
    * @returns {boolean} true, if the object is a non empty array
    */
    $scope.isNonEmptyArray = function(myArray) {
      return myArray.constructor.toString().indexOf('Array') > -1 && myArray.length > 0;
    };

    /**
    * @description Analyzes the local scope of the directive to select the proper HTML template and populate the parameter options.
    *
    * This method expects to obtain the class of the parent policy through the parent scope (that's why the directive should be managed by PolicyController)
    * @name PolicyController:getTemplate
    * @ngdoc method
    * @methodOf PolicyController
    * @param {object} scope The directive scope object
    * @returns {string} The HTML template
    */
    $scope.getTemplate = function(parameter) {
      var selectedPolicyClass = $scope.policy.class;
      $scope.optionalParameters = [];

      for (var i=0; i<$scope.availablePolicies.length; i++) {
        if ($scope.availablePolicies[i].class === selectedPolicyClass) {
          for (var j=0; j<$scope.availablePolicies[i].parameters.length; j++) {
            var paramCfg = $scope.availablePolicies[i].parameters[j];
            if (paramCfg.key === parameter.key) { // Checking current parameter
              if (paramCfg.required) {
                if ($scope.isNonEmptyArray(paramCfg.options)) {
                  return policyParamOptionsView;
                }
                return policyParamStringView;
              }
            }
            if (!paramCfg.required) {
              $scope.optionalParameters.push(paramCfg.key);
            }
          }
        }
      }

      return parameter.key ? policyParamFixedView : policyParamEditableView;
    };

    /**
    * @description Gets the options for a particular parameter
    *
    * @name PolicyController:getParameterOptions
    * @ngdoc method
    * @methodOf PolicyController
    * @param {string} parameterKey The parameter key
    * @returns {array} The parameter options list
    */
    $scope.getParameterOptions = function(parameterKey) {
      for (var i=0; i<$scope.availablePolicies.length; i++) {
        if ($scope.availablePolicies[i].class === $scope.policy.class) {
          for (var j=0; j<$scope.availablePolicies[i].parameters.length; j++) {
            var paramCfg = $scope.availablePolicies[i].parameters[j];
            if (paramCfg.key === parameterKey) { // Checking current parameter
              return paramCfg.options;
            }
          }
        }
      }
      return [];
    };

    /**
    * @description Gets the optional parameters.
    *
    * @name PolicyController:getOptionalParameters
    * @ngdoc method
    * @methodOf PolicyController
    * @returns {array} The optional parameters list
    */
    $scope.getOptionalParameters = function() {
      var params = [];
      angular.forEach($scope.optionalParameters, function(availParam) {
        var found = false;
        angular.forEach($scope.policy.parameter, function(param) {
          if (param.key === availParam) {
            found = true;
          }
        });
        if (!found) {
          params.push(availParam);
        }
      });
      return params;
    };

    // Initialization

    RequisitionsService.getAvailablePolicies().then(function(policies) {
      $scope.availablePolicies = policies;
      angular.forEach(policies, function(policy) {
        if (policy.class === $scope.policy.class) {
          var orderedParams = [];
          for (var i=0; i<policy.parameters.length; i++) {
            var pkey = policy.parameters[i].key;
            for (var j=0; j<$scope.policy.parameter.length; j++) {
              var p = $scope.policy.parameter[j];
              if (p.key === pkey) {
                orderedParams.push(p);
              }
            }
          }
          $scope.policy.parameter = orderedParams;
        }
      });
    });

  }]);

}());
