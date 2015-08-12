/**
* @author Alejandro Galue <agalue@opennms.org>
* @copyright 2014 The OpenNMS Group, Inc.
*/

(function() {

  'use strict';

  angular.module('onms-requisitions')

  /**
  * @ngdoc controller
  * @name ForeignSourceController
  * @module onms-requisitions
  *
  * @requires $scope Angular local scope
  * @requires $routeParams Angular route parameters
  * @requires $window Document window
  * @requires $modal Angular modal
  * @requires RequisitionsService The requisitions service
  * @requires EmptyTypeaheadService The empty typeahead Service
  * @requires growl The growl plugin for instant notifications
  *
  * @description The controller for manage foreign source definitions (i.e. policies and detectors)
  */
  .controller('ForeignSourceController', ['$scope', '$routeParams', '$window', '$modal', 'RequisitionsService', 'EmptyTypeaheadService', 'growl', function($scope, $routeParams, $window, $modal, RequisitionsService, EmptyTypeaheadService, growl) {

    /**
    * @description The foreign source (a.k.a the name of the requisition).
    * The default value is obtained from the $routeParams.
    *
    * @ngdoc property
    * @name ForeignSourceController#foreignSource
    * @propertyOf ForeignSourceController
    * @returns {string} The foreign source
    */
    $scope.foreignSource = $routeParams.foreignSource;

    /**
    * @description The foreign source definition object
    *
    * @ngdoc property
    * @name ForeignSourceController#foreignSourceDef
    * @propertyOf ForeignSourceController
    * @returns {object} The foreign source definition
    */
    $scope.foreignSourceDef = {};

    /**
    * @description fieldComparator method from EmptyTypeaheadService
    *
    * @ngdoc method
    * @name ForeignSourceController#fieldComparator
    * @methodOf AssetController
    */
    $scope.fieldComparator = EmptyTypeaheadService.fieldComparator;

    /**
    * @description onFocus method from EmptyTypeaheadService
    *
    * @ngdoc method
    * @name ForeignSourceController#onFocus
    * @methodOf AssetController
    */
    $scope.onFocus = EmptyTypeaheadService.onFocus;

    /**
    * @description Goes back to requisition editor (navigation)
    *
    * @name ForeignSourceController:goBack
    * @ngdoc method
    * @methodOf ForeignSourceController
    */
    $scope.goBack = function() {
      var doGoBack = function() {
        if ($scope.foreignSource == 'default') {
          $window.location.href = '#/requisitions';
        } else {
          $window.location.href = '#/requisitions/' + $scope.foreignSource;
        }
      }
      if (this.fsForm.$dirty) {
        bootbox.dialog({
          message: 'There are changes on the current requisition. Are you sure you want to cancel ?',
          title: 'Cancel Changes',
          buttons: {
            success: {
              label: 'Yes',
              className: 'btn-danger',
              callback: doGoBack
            },
            main: {
              label: 'No',
              className: 'btn-default'
            }
          }
        });
      } else {
        doGoBack();
      }
    };

    /**
    * @description Shows an error to the user
    *
    * @name ForeignSourceController:errorHandler
    * @ngdoc method
    * @methodOf ForeignSourceController
    * @param {string} message The error message
    */
    $scope.errorHandler = function(message) {
      growl.error(message, {ttl: 10000});
    };

    /**
    * @description Opens the modal window to add/edit a policy
    *
    * @name ForeignSourceController:editPolicy
    * @ngdoc method
    * @methodOf ForeignSourceController
    * @param {integer} index The index of the policy to edit
    * @param {boolean} isNew true, if the policy is new
    */
    $scope.editPolicy = function(index, isNew) {
      var form = this.fsForm;
      var policyToEdit = $scope.foreignSourceDef.policies[index];
      $modal.open({
        backdrop: true,
        controller: 'PolicyController',
        templateUrl: 'views/policy.html',
        resolve: {
          policy: function() { return angular.copy(policyToEdit); }
        }
      }).result.then(function(result) {
        angular.copy(result, policyToEdit);
        form.$dirty = true;
      }, function() {
        if (isNew) {
          $scope.foreignSourceDef.policies.pop();
        }
      });
    };

    /**
    * @description Removes a policy
    *
    * @name ForeignSourceController:removePolicy
    * @ngdoc method
    * @methodOf ForeignSourceController
    * @param {integer} index The index of the policy to remove
    */
    $scope.removePolicy = function(index) {
      $scope.foreignSourceDef.policies.splice(index, 1);
      this.fsForm.$dirty = true;
    };

    /**
    * @description Adds a new policy
    *
    * @name ForeignSourceController:addPolicy
    * @ngdoc method
    * @methodOf ForeignSourceController
    */
    $scope.addPolicy = function() {
      $scope.foreignSourceDef.policies.push({ 'name': '', 'class': '', 'parameter': [] });
      $scope.editPolicy($scope.foreignSourceDef.policies.length - 1, true);
    };

    /**
    * @description Opens the modal window to add/edit a detector
    *
    * @name ForeignSourceController:editDetector
    * @ngdoc method
    * @methodOf ForeignSourceController
    * @param {integer} index The index of the detector to edit
    * @param {boolean} isNew true, if the detector is new
    */
    $scope.editDetector = function(index, isNew) {
      var form = this.fsForm;
      var detectorToEdit = $scope.foreignSourceDef.detectors[index];
      $modal.open({
        backdrop: true,
        controller: 'DetectorController',
        templateUrl: 'views/detector.html',
        resolve: {
          detector: function() { return angular.copy(detectorToEdit); }
        }
      }).result.then(function(result) {
        angular.copy(result, detectorToEdit);
        form.$dirty = true;
      }, function() {
        if (isNew) {
          $scope.foreignSourceDef.detectors.pop();
        }
      });
    };

    /**
    * @description Removes a detector
    *
    * @name ForeignSourceController:removeDetector
    * @ngdoc method
    * @methodOf ForeignSourceController
    * @param {integer} index The index of the detector to remove
    */
    $scope.removeDetector = function(index) {
      $scope.foreignSourceDef.detectors.splice(index, 1);
      this.fsForm.$dirty = true;
    };

    /**
    * @description Adds a new detector
    *
    * @name ForeignSourceController:addDetector
    * @ngdoc method
    * @methodOf ForeignSourceController
    */
    $scope.addDetector = function() {
      $scope.foreignSourceDef.detectors.push({ 'name': '', 'class': '', 'parameter': [] });
      $scope.editDetector($scope.foreignSourceDef.detectors.length - 1, true);
    };

    /**
    * @description Saves the local foreign source on the server
    *
    * @name ForeignSourceController:save
    * @ngdoc method
    * @methodOf ForeignSourceController
    */
    $scope.save = function() {
      var form = this.fsForm;
      RequisitionsService.saveForeignSourceDefinition($scope.foreignSourceDef).then(
        function() { // success
          growl.success('The definition for the requisition ' + $scope.foreignSource + ' has been saved.');
          form.$dirty = false;
        },
        $scope.errorHandler
      );
    };

    /**
    * @description Refreshes the local node from the server
    *
    * @name ForeignSourceController:refresh
    * @ngdoc method
    * @methodOf ForeignSourceController
    */
    $scope.refresh = function() {
      growl.success('Retrieving definition for requisition ' + $scope.foreignSource + '...');
      RequisitionsService.getForeignSourceDefinition($scope.foreignSource).then(
        function(data) { // success
          $scope.foreignSourceDef = data;
        },
        $scope.errorHandler
      );
    };

    // Initialization

    if ($scope.foreignSource) {
      $scope.refresh();
    }
  }]);

}());
