/*global bootbox:true */

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
  * @requires $filter Angular filter
  * @requires $routeParams Angular route parameters
  * @requires $window Document window
  * @requires $modal Angular modal
  * @requires RequisitionsService The requisitions service
  * @requires EmptyTypeaheadService The empty typeahead Service
  * @requires growl The growl plugin for instant notifications
  *
  * @description The controller for manage foreign source definitions (i.e. policies and detectors)
  */
  .controller('ForeignSourceController', ['$scope', '$filter', '$routeParams', '$window', '$modal', 'RequisitionsService', 'EmptyTypeaheadService', 'growl', function($scope, $filter, $routeParams, $window, $modal, RequisitionsService, EmptyTypeaheadService, growl) {

    /**
    * @description The timing status.
    *
    * @ngdoc property
    * @name ForeignSourceController#timingStatus
    * @propertyOf ForeignSourceController
    * @returns {object} The timing status object
    */
    $scope.timingStatus = RequisitionsService.getTiming();

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
    $scope.foreignSourceDef = { detectors: [], policies: [] };

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
    * @description The filtered list of detectors
    *
    * @ngdoc property
    * @name ForeignSourceController#filteredDetectors
    * @propertyOf ForeignSourceController
    * @returns {array} The filtered array
    */
    $scope.filteredDetectors = [];

    /**
    * @description The amount of detectors per page for pagination (defaults to 10)
    *
    * @ngdoc property
    * @name ForeignSourceController#detectorsPageSize
    * @propertyOf ForeignSourceController
    * @returns {integer} The page size
    */
    $scope.detectorsPageSize = 10;

    /**
    * @description The maximum size of detector pages for pagination (defaults to 5)
    *
    * @ngdoc property
    * @name ForeignSourceController#detectorsMaxSize
    * @propertyOf ForeignSourceController
    * @returns {integer} The maximum size
    */
    $scope.detectorsMaxSize = 5;

    /**
    * @description The total amount of detectors for pagination (defaults to 0)
    *
    * @ngdoc property
    * @name ForeignSourceController#detectorsTotalItems
    * @propertyOf ForeignSourceController
    * @returns {integer} The total detectors
    */
    $scope.detectorsTotalItems = 0;

    /**
    * @description The filtered list of policies
    *
    * @ngdoc property
    * @name ForeignSourceController#filteredPolicies
    * @propertyOf ForeignSourceController
    * @returns {array} The filtered array
    */
    $scope.filteredPolicies = [];

    /**
    * @description The amount of policies per page for pagination (defaults to 10)
    *
    * @ngdoc property
    * @name ForeignSourceController#policiesPageSize
    * @propertyOf ForeignSourceController
    * @returns {integer} The page size
    */
    $scope.policiesPageSize = 10;

    /**
    * @description The maximum size of policies pages for pagination (defaults to 5)
    *
    * @ngdoc property
    * @name ForeignSourceController#policiesMaxSize
    * @propertyOf ForeignSourceController
    * @returns {integer} The maximum size
    */
    $scope.policiesMaxSize = 5;

    /**
    * @description The total amount of policies for pagination (defaults to 0)
    *
    * @ngdoc property
    * @name ForeignSourceController#policiesTotalItems
    * @propertyOf ForeignSourceController
    * @returns {integer} The total policies
    */
    $scope.policiesTotalItems = 0;

    /**
    * @description Goes to specific URL warning about changes if exist.
    *
    * @name ForeignSourceController:goTo
    * @ngdoc method
    * @methodOf ForeignSourceController
    * @param {object} handler The goto handler
    */

    $scope.goTo = function(handler) {
      if (this.fsForm.$dirty) {
        bootbox.dialog({
          message: 'There are changes on the current requisition. Are you sure you want to cancel ?',
          title: 'Cancel Changes',
          buttons: {
            success: {
              label: 'Yes',
              className: 'btn-danger',
              callback: handler
            },
            main: {
              label: 'No',
              className: 'btn-default'
            }
          }
        });
      } else {
        handler();
      }
    };

    /**
    * @description Goes back to requisitions list (navigation)
    *
    * @name ForeignSourceController:goTop
    * @ngdoc method
    * @methodOf ForeignSourceController
    */
    $scope.goTop = function() {
      var doGoTop = function() {
        $window.location.href = '#/requisitions';
      };
      $scope.goTo(doGoTop);
    };

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
      };
      $scope.goTo(doGoBack);
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
      RequisitionsService.startTiming();
      RequisitionsService.saveForeignSourceDefinition($scope.foreignSourceDef).then(
        function() { // success
          growl.success('The definition for the requisition ' + $scope.foreignSource + ' has been saved.');
          form.$dirty = false;
        },
        $scope.errorHandler
      );
    };

    /**
    * @description Updates the pagination variables for the policies.
    *
    * @name ForeignSourceController:updateFilteredPolicies
    * @ngdoc method
    * @methodOf ForeignSourceController
    */
    $scope.updateFilteredPolicies = function() {
      $scope.policiesCurrentPage = 1;
      $scope.policiesTotalItems = $scope.filteredPolicies.length;
      $scope.policiesNumPages = Math.ceil($scope.policiesTotalItems / $scope.policiesPageSize);
    };

    /**
    * @description Updates the pagination variables for the detectors.
    *
    * @name ForeignSourceController:updateFilteredDetectors
    * @ngdoc method
    * @methodOf ForeignSourceController
    */
    $scope.updateFilteredDetectors = function() {
      $scope.detectorsCurrentPage = 1;
      $scope.detectorsTotalItems = $scope.filteredDetectors.length;
      $scope.detectorsNumPages = Math.ceil($scope.detectorsTotalItems / $scope.detectorsPageSize);
    }

    /**
    * @description Initialized the local foreign source definition from the server
    *
    * @name ForeignSourceController:initialize
    * @ngdoc method
    * @methodOf ForeignSourceController
    */
    $scope.initialize = function() {
      growl.success('Retrieving definition for requisition ' + $scope.foreignSource + '...');
      RequisitionsService.getForeignSourceDefinition($scope.foreignSource).then(
        function(foreignSourceDef) { // success
          $scope.foreignSourceDef = foreignSourceDef;
          // Updating pagination variables for detectors.
          $scope.filteredDetectors = $scope.foreignSourceDef.detectors;
          $scope.updateFilteredDetectors();
          // Updating pagination variables for policies.
          $scope.filteredPolicies = $scope.foreignSourceDef.policies;
          $scope.updateFilteredPolicies();
        },
        $scope.errorHandler
      );
    };

    /**
    * @description Watch for filter changes in order to update the detector list and updates the pagination control
    *
    * @name ForeignSourceController:detectorFilter
    * @ngdoc event
    * @methodOf ForeignSourceController
    */
    $scope.$watch('detectorFilter', function() {
      $scope.filteredDetectors = $filter('filter')($scope.foreignSourceDef.detectors, $scope.detectorFilter);
      $scope.updateFilteredDetectors();
    });

    /**
    * @description Watch for filter changes in order to update the policy list and updates the pagination control
    *
    * @name ForeignSourceController:policyFilter
    * @ngdoc event
    * @methodOf ForeignSourceController
    */
    $scope.$watch('policyFilter', function() {
      $scope.filteredPolicies = $filter('filter')($scope.foreignSourceDef.policies, $scope.policyFilter);
      $scope.updateFilteredPolicies();
    });

    // Initialization

    if ($scope.foreignSource) {
      $scope.initialize();
    }
  }]);

}());
