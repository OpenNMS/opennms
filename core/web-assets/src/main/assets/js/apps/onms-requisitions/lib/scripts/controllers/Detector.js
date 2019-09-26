require('../services/Requisitions');

/**
* @author Alejandro Galue <agalue@opennms.org>
* @copyright 2014 The OpenNMS Group, Inc.
*/

(function() {

  'use strict';

  angular.module('onms-requisitions')

  /**
  * @ngdoc controller
  * @name DetectorController
  * @module onms-requisitions
  *
  * @requires $scope Angular local scope
  * @requires $uibModalInstance Angular UI modal instance
  * @requires RequisitionsService The Requisitions Servive
  * @requires detector Requisition detector object
  *
  * @description The controller for manage the modal dialog for add/edit requisition detectors
  */
  .controller('DetectorController', ['$scope', '$uibModalInstance', 'RequisitionsService', 'detector', function($scope, $uibModalInstance, RequisitionsService, detector) {

    /**
    * @description The detector object
    *
    * @ngdoc property
    * @name DetectorController#detector
    * @propertyOf DetectorController
    * @returns {object} The detector object
    */
    $scope.detector = detector;

    /**
    * @description The available detectors object
    *
    * @ngdoc property
    * @name DetectorController#availableDetectors
    * @propertyOf DetectorController
    * @returns {array} The detectors list
    */
    $scope.availableDetectors = [];

    /**
    * @description The available parameters/attributes for the selected detector
    *
    * @ngdoc property
    * @name DetectorController#availableParameters
    * @propertyOf DetectorController
    * @returns {array} The parameters list
    */
    $scope.availableParameters = [];

    /**
    * @description Gets the available parameters not being used by the detector
    *
    * @name DetectorController:getAvailableParameters
    * @ngdoc method
    * @methodOf DetectorController
    */
    $scope.getAvailableParameters = function() {
      var params = [];
      angular.forEach($scope.availableParameters, function(availParam) {
        var found = false;
        angular.forEach($scope.detector.parameter, function(param) {
          if (param.key === availParam.key) {
            found = true;
          }
        });
        if (!found) {
          params.push(availParam);
        }
      });
      return params;
    };

    /**
    * @description Saves the current detector
    *
    * @name DetectorController:save
    * @ngdoc method
    * @methodOf DetectorController
    */
    $scope.save = function () {
      $uibModalInstance.close($scope.detector);
    };

    /**
    * @description Cancels the current operation
    *
    * @name DetectorController:cancel
    * @ngdoc method
    * @methodOf DetectorController
    */
    $scope.cancel = function () {
      $uibModalInstance.dismiss('cancel');
    };

    /**
    * @description Adds a new empty parameter to the current detector
    *
    * @name DetectorController:addParameter
    * @ngdoc method
    * @methodOf DetectorController
    */
    $scope.addParameter = function() {
      $scope.detector.parameter.push({ 'key': '', 'value': '' });
    };

    /**
    * @description Removes a parameter from the current detector
    *
    * @name DetectorController:removeParameter
    * @ngdoc method
    * @methodOf DetectorController
    * @param {integer} index The index of the parameter to remove
    */
    $scope.removeParameter = function(index) {
      $scope.detector.parameter.splice(index, 1);
    };

    /**
    * @description Update available detector parameters after changing the detector class.
    *
    * @name DetectorController:updateAvailableParameters
    * @ngdoc method
    * @methodOf DetectorController
    * @param {object} policyConfig the configuration of the selected policy
    */
    $scope.updateAvailableParameters = function(selectedDetector) {
      if (!selectedDetector) {
        return;
      }
      $scope.detector.parameter = [];
      angular.forEach($scope.availableDetectors, function(detector) {
        if (detector.class === selectedDetector.class) {
          $scope.availableParameters = detector.parameters;
        }
      });
    };

    // Initialization

    RequisitionsService.getAvailableDetectors().then(function(detectors) {
      $scope.availableDetectors = detectors;
      if ($scope.detector.class) {
        angular.forEach(detectors, function(detector) {
          if (detector.class === $scope.detector.class) {
            $scope.availableParameters = detector.parameters;
            var orderedParams = [];
            for (var i=0; i<detector.parameters.length; i++) {
              var pkey = detector.parameters[i].key;
              for (var j=0; j<$scope.detector.parameter.length; j++) {
                var p = $scope.detector.parameter[j];
                if (p.key === pkey) {
                  orderedParams.push(p);
                }
              }
            }
            $scope.detector.parameter = orderedParams;
          }
        });
      }
    });

  }]);

}());
