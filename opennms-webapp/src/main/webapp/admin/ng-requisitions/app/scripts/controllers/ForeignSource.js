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
  * @requires $routeParams Angular route parameters
  * @requires $window Document window
  * @requires $uibModal Angular UI modal
  * @required filterFilter the Angular filter
  * @requires RequisitionsService The requisitions service
  * @requires growl The growl plugin for instant notifications
  *
  * @description The controller for manage foreign source definitions (i.e. policies and detectors)
  */
  .controller('ForeignSourceController', ['$scope', '$routeParams', '$window', '$uibModal', 'filterFilter', 'RequisitionsService', 'growl', function($scope, $routeParams, $window, $uibModal, filterFilter, RequisitionsService, growl) {

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
    * @description The filteres object (used to track the content of the search fields)
    *
    * @ngdoc property
    * @name ForeignSourceController#filters
    * @propertyOf ForeignSourceController
    * @returns {object} The filteres object
    */
    $scope.filters = { detector: null, policy: null };

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
          $window.location.href = '#/requisitions/' + encodeURIComponent($scope.foreignSource);
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
    * @description Returns the index of a policy
    *
    * @name ForeignSourceController:indexOfPolicy
    * @ngdoc method
    * @methodOf ForeignSourceController
    * @param {object} policy The policy object
    */
    $scope.indexOfPolicy = function(policy) {
      for (var i = 0; i < $scope.foreignSourceDef.policies.length; i++) {
        if ($scope.foreignSourceDef.policies[i].name === policy.name) {
          return i;
        }
      }
      return -1;
    };

    /**
    * @description Returns the index of a detector
    *
    * @name ForeignSourceController:indexOfDetector
    * @ngdoc method
    * @methodOf ForeignSourceController
    * @param {object} policy The detector object
    */
    $scope.indexOfDetector = function(detector) {
      for (var i = 0; i < $scope.foreignSourceDef.detectors.length; i++) {
        if ($scope.foreignSourceDef.detectors[i].name === detector.name) {
          return i;
        }
      }
      return -1;
    };

    /**
    * @description Opens the modal window to add/edit a policy
    *
    * @name ForeignSourceController:editPolicy
    * @ngdoc method
    * @methodOf ForeignSourceController
    * @param {object} policy The policy object to edit
    * @param {boolean} isNew true, if the policy is new
    */
    $scope.editPolicy = function(policy, isNew) {
      var form = this.fsForm;
      $uibModal.open({
        backdrop: 'static',
        keyboard: false,
        controller: 'PolicyController',
        templateUrl: 'views/policy.html',
        resolve: {
          policy: function() { return angular.copy(policy); }
        }
      }).result.then(function(result) {
        angular.copy(result, policy);
        form.$dirty = true;
      }, function() {
        if (isNew) {
          $scope.foreignSourceDef.policies.pop();
        }
      });
    };

    /**
    * @description Opens the modal window to move a policy
    *
    * @name ForeignSourceController:moveDetector
    * @ngdoc method
    * @methodOf ForeignSourceController
    * @param {object} policy The policy object to move
    */
    $scope.movePolicy = function(policy) {
      var form = this.fsForm;
      var pos = $scope.indexOfPolicy(policy);
      var max = $scope.foreignSourceDef.policies.length - 1;
      $uibModal.open({
        backdrop: 'static',
        keyboard: false,
        size: 'sm',
        controller: 'MoveController',
        templateUrl: 'views/move.html',
        resolve: {
          label: function() { return policy.name; },
          position: function() { return pos; },
          maximum: function() { return max; }
        }
      }).result.then(function(dst) {
        form.$dirty = true;
        $scope.foreignSourceDef.policies.splice(pos, 1);
        $scope.foreignSourceDef.policies.splice(dst, 0, policy);

      });
    };

    /**
    * @description Removes a policy
    *
    * @name ForeignSourceController:removePolicy
    * @ngdoc method
    * @methodOf ForeignSourceController
    * @param {object} policy The policy object to remove
    */
    $scope.removePolicy = function(policy) {
      var index = $scope.indexOfPolicy(policy);
      if (index > -1) {
        $scope.foreignSourceDef.policies.splice(index, 1);
        this.fsForm.$dirty = true;
      }
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
      var index = $scope.foreignSourceDef.policies.length - 1;
      $scope.editPolicy($scope.foreignSourceDef.policies[index], true);
    };

    /**
    * @description Opens the modal window to add/edit a detector
    *
    * @name ForeignSourceController:editDetector
    * @ngdoc method
    * @methodOf ForeignSourceController
    * @param {object} detector The detector object to edit
    * @param {boolean} isNew true, if the detector is new
    */
    $scope.editDetector = function(detector, isNew) {
      var form = this.fsForm;
      $uibModal.open({
        backdrop: 'static',
        keyboard: false,
        controller: 'DetectorController',
        templateUrl: 'views/detector.html',
        resolve: {
          detector: function() { return angular.copy(detector); }
        }
      }).result.then(function(result) {
        angular.copy(result, detector);
        form.$dirty = true;
      }, function() {
        if (isNew) {
          $scope.foreignSourceDef.detectors.pop();
        }
      });
    };

    /**
    * @description Opens the modal window to move a detector
    *
    * @name ForeignSourceController:moveDetector
    * @ngdoc method
    * @methodOf ForeignSourceController
    * @param {object} detector The detector object to move
    */
    $scope.moveDetector = function(detector) {
      var form = this.fsForm;
      var pos = $scope.indexOfDetector(detector);
      var max = $scope.foreignSourceDef.detectors.length - 1;
      $uibModal.open({
        backdrop: 'static',
        keyboard: false,
        size: 'sm',
        controller: 'MoveController',
        templateUrl: 'views/move.html',
        resolve: {
          label: function() { return detector.name; },
          position: function() { return pos; },
          maximum: function() { return max; }
        }
      }).result.then(function(dst) {
        form.$dirty = true;
        $scope.foreignSourceDef.detectors.splice(pos, 1);
        $scope.foreignSourceDef.detectors.splice(dst, 0, detector);

      });
    };

    /**
    * @description Removes a detector
    *
    * @name ForeignSourceController:removeDetector
    * @ngdoc method
    * @methodOf ForeignSourceController
    * @param {object} detector The detector object to remove
    */
    $scope.removeDetector = function(detector) {
      var index = $scope.indexOfDetector(detector);
      if (index > -1) {
        $scope.foreignSourceDef.detectors.splice(index, 1);
        this.fsForm.$dirty = true;
      }
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
      var index = $scope.foreignSourceDef.detectors.length - 1;
      $scope.editDetector($scope.foreignSourceDef.detectors[index], true);
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
    * @description Resets to the default set of detectors and policies
    *
    * @name ForeignSourceController:reset
    * @ngdoc method
    * @methodOf ForeignSourceController
    */
    $scope.reset = function() {
      bootbox.confirm('Are you sure you want to reset the foreign source definition to the default ?', function(ok) {
        if (ok) {
          RequisitionsService.startTiming();
          RequisitionsService.deleteForeignSourceDefinition($scope.foreignSource).then(
            function() { // success
              growl.success('The foreign source definition for ' + $scope.foreignSource + 'has been reseted.');
              $scope.initialize();
            },
            $scope.errorHandler
          );
        }
      });
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
    };

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
    $scope.$watch('filters.detector', function() {
      $scope.filteredDetectors = filterFilter($scope.foreignSourceDef.detectors, $scope.filters.detector);
      $scope.updateFilteredDetectors();
    });

    /**
    * @description Watch for filter changes in order to update the policy list and updates the pagination control
    *
    * @name ForeignSourceController:policyFilter
    * @ngdoc event
    * @methodOf ForeignSourceController
    */
    $scope.$watch('filters.policy', function() {
      $scope.filteredPolicies = filterFilter($scope.foreignSourceDef.policies, $scope.filters.policy);
      $scope.updateFilteredPolicies();
    });

    // Initialization

    if ($scope.foreignSource) {
      $scope.initialize();
    }
  }]);

}());
