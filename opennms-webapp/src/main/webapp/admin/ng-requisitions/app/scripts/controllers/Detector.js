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
  * @requires $modalInstance Angular modal instance
  * @requires RequisitionsService The Requisitions Servive
  * @requires EmptyTypeaheadService The empty typeahead Service
  * @requires detector Requisition detector object
  *
  * @description The controller for manage the modal dialog for add/edit requisition detectors
  */
  .controller('DetectorController', ['$scope', '$modalInstance', 'RequisitionsService', 'EmptyTypeaheadService', 'detector', function($scope, $modalInstance, RequisitionsService, EmptyTypeaheadService, detector) {

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
    * @description fieldComparator method from EmptyTypeaheadService
    *
    * @ngdoc method
    * @name DetectorController#fieldComparator
    * @methodOf AssetController
    */
    $scope.fieldComparator = EmptyTypeaheadService.fieldComparator;

    /**
    * @description onFocus method from EmptyTypeaheadService
    *
    * @ngdoc method
    * @name DetectorController#onFocus
    * @methodOf AssetController
    */
    $scope.onFocus = EmptyTypeaheadService.onFocus;

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
          if (param.key == availParam.key) {
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
      console.log($scope.detector);
      $modalInstance.close($scope.detector);
    };

    /**
    * @description Cancels the current operation
    *
    * @name DetectorController:cancel
    * @ngdoc method
    * @methodOf DetectorController
    */
    $scope.cancel = function () {
      $modalInstance.dismiss('cancel');
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
    * @description Sets the detector class after choosing an implementation if it is different than the existing one.
    * Otherwise, leaves the detector class unchanged.
    *
    * The parameters list of the current detector is reinitialized after changing the class.
    *
    * @name DetectorController:setClassForName
    * @ngdoc method
    * @methodOf DetectorController
    * @param {object} selectedDetector the detector to be used as a reference
    */
    $scope.setClassForName = function(selectedDetector) {
      if (selectedDetector && $scope.detector.class != selectedDetector.class) {
        $scope.detector.class = selectedDetector.class;
        $scope.detector.parameter = [];
        $scope.availableParameters = selectedDetector.parameters;
      }
    };

    /**
    * @description Sets the detector name after choosing an implementation if it has not been set.
    * Otherwise, leaves the detector name unchanged.
    *
    * @name DetectorController:setNameForClass
    * @ngdoc method
    * @methodOf DetectorController
    * @param {object} selectedDetector the detector to be used as a reference
    */
    $scope.setNameForClass = function(selectedDetector) {
      if (selectedDetector && !$scope.detector.name) {
        $scope.detector.name = selectedDetector.name;
      }
    };

    // Initialization

    RequisitionsService.getAvailableDetectors().then(function(detectors) {
      $scope.availableDetectors = detectors;
      if ($scope.detector.class != null) {
        angular.forEach(detectors, function(detector) {
          if (detector.class == $scope.detector.class) {
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
