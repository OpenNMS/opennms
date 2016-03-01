/* global bootbox:true */

/**
* @author Alejandro Galue <agalue@opennms.org>
* @copyright 2014 The OpenNMS Group, Inc.
*/

(function() {

  'use strict';

  angular.module('onms-requisitions')

  /**
  * @ngdoc controller
  * @name RequisitionsController
  * @module onms-requisitions
  *
  * @description The controller for manage all the requisitions (list/add/remove/synchronize)
  *
  * @requires $scope Angular local scope
  * @requires $filter Angular filter
  * @requires $window Document window
  * @requires $uibModal Angular UI modal
  * @requires RequisitionsService The requisitions service
  * @requires SynchronizeService The synchronize service
  * @requires growl The growl plugin for instant notifications
  */
  .controller('RequisitionsController', ['$scope', '$filter', '$window', '$uibModal', 'RequisitionsService', 'SynchronizeService', 'growl', function($scope, $filter, $window, $uibModal, RequisitionsService, SynchronizeService, growl) {

    /**
    * @description The timing status.
    *
    * @ngdoc property
    * @name RequisitionsController#timingStatus
    * @propertyOf RequisitionsController
    * @returns {object} The timing status object
    */
    $scope.timingStatus = RequisitionsService.getTiming();

    /**
    * @description The load flag.
    *
    * @ngdoc property
    * @name RequisitionsController#loaded
    * @propertyOf RequisitionsController
    * @returns {boolean} True, if the requisitions have been loaded.
    */
    $scope.loaded = false;

    /**
    * @description The requisitions list
    *
    * @ngdoc property
    * @name RequisitionsController#requisitions
    * @propertyOf RequisitionsController
    * @returns {array} The requisitions array
    */
    $scope.requisitions = [];

    /**
    * @description The filtered version of the requisitions list
    *
    * @ngdoc property
    * @name RequisitionsController#filteredRequisitions
    * @propertyOf RequisitionsController
    * @returns {array} The filtered array
    */
    $scope.filteredRequisitions = [];

    /**
    * @description The amount of items per page for pagination (defaults to 10)
    *
    * @ngdoc property
    * @name RequisitionsController#pageSize
    * @propertyOf RequisitionsController
    * @returns {integer} The page size
    */
    $scope.pageSize = 10;

    /**
    * @description The maximum size of pages for pagination (defaults to 5)
    *
    * @ngdoc property
    * @name RequisitionControllers#maxSize
    * @propertyOf RequisitionsController
    * @returns {integer} The maximum size
    */
    $scope.maxSize = 5;

    /**
    * @description The total amount of items for pagination (defaults to 0)
    *
    * @ngdoc property
    * @name RequisitionControllers#maxSize
    * @propertyOf RequisitionsController
    * @returns {integer} The total items
    */
    $scope.totalItems = 0;

    /**
    * @description Shows an error to the user
    *
    * @name RequisitionsController:errorHandler
    * @ngdoc method
    * @methodOf RequisitionsController
    * @param {string} message The error message
    */
    $scope.errorHandler = function(message) {
      growl.error(message, {ttl: 10000});
    };

    /**
    * @description Returns the index of a requisition
    *
    * @name RequisitionsController:indexOfRequisition
    * @ngdoc method
    * @methodOf RequisitionsController
    * @param {string} foreignSource The name of the requisition
    */
    $scope.indexOfRequisition = function(foreignSource) {
      for(var i = 0; i < $scope.requisitions.length; i++) {
        if ($scope.requisitions[i].foreignSource === foreignSource) {
          return i;
        }
      }
      return -1;
    };

    /**
    * @description Quick add a new node
    *
    * @name RequisitionsController:quickAddNode
    * @ngdoc method
    * @methodOf RequisitionsController
    */
    $scope.quickAddNode = function() {
      var availableForeignSources = [];
      angular.forEach($scope.requisitions, function(r) {
        availableForeignSources.push(r.foreignSource);
      });
      var modalInstance = $uibModal.open({
        backdrop: 'static',
        controller: 'QuickAddNodeController',
        templateUrl: 'views/quick-add-node.html',
        resolve: {
          foreignSources: function() { return availableForeignSources; }
        }
      });
      modalInstance.result.then(function(node) {
        if (node.noSnmp == false && node.snmpCommunity != '') {
          RequisitionsService.startTiming();
          RequisitionsService.updateSnmpCommunity(node.ipAddress, node.snmpCommunity, node.snmpVersion).then(
            function() { // success
              $scope.addNode(node);
            },
            $scope.errorHandler
          );
        } else {
          $scope.addNode(node);
        }
      });
    };

    /**
    * @description Add a new node to an existing requisition
    *
    * @name RequisitionsController:addNode
    * @ngdoc method
    * @methodOf RequisitionsController
    * @param {object} node the QuickNode object
    */
    $scope.addNode = function(quickNode) {
      var node = quickNode.createRequisitionedNode();
      RequisitionsService.startTiming();
      RequisitionsService.saveNode(node).then(
        function() { // success
          growl.success('The node ' + node.nodeLabel + ' has been saved.');
          RequisitionsService.synchronizeRequisition(node.foreignSource,  'false').then(
            function() {
              growl.success('The requisition ' + node.foreignSource + ' has been synchronized.');
            },
            $scope.errorHandler
          );
        },
        $scope.errorHandler
      );
    };

    /**
    * @description Clones the detectors and policies of a specific requisition
    *
    * @name RequisitionsController:clone
    * @ngdoc method
    * @methodOf RequisitionsController
    * @param {string} foreignSource The name of the requisition
    */
    $scope.clone = function(foreignSource) {
      var availableForeignSources = [];
      angular.forEach($scope.requisitions, function(r) {
        if (r.foreignSource !== foreignSource) {
          availableForeignSources.push(r.foreignSource);
        }
      });
      var modalInstance = $uibModal.open({
        backdrop: 'static',
        controller: 'CloneForeignSourceController',
        templateUrl: 'views/clone-foreignsource.html',
        resolve: {
          foreignSource: function() { return foreignSource; },
          availableForeignSources: function() { return availableForeignSources; }
        }
      });
      modalInstance.result.then(function(targetForeignSource) {
        // FIXME Requires work, even if it works
        RequisitionsService.startTiming();
        RequisitionsService.getForeignSourceDefinition(foreignSource).then(
          function(r) { // success
            r.name = targetForeignSource;
            RequisitionsService.saveForeignSourceDefinition(r).then(
              function() { // success
                growl.success('The foreign source definition for ' + foreignSource + ' has been cloned to ' + targetForeignSource);
              },
              $scope.errorHandler
            );
          },
          $scope.errorHandler
        );
      });
    };

    /**
    * @description Adds a new requisition on the server.
    *
    * A dialog box will be displayed to request the name of the requisition to the user.
    *
    * @name RequisitionsController:add
    * @ngdoc method
    * @methodOf RequisitionsController
    */
    $scope.add = function() {
      bootbox.prompt('Please enter the name for the new requisition', function(foreignSource) {
        if (foreignSource) {
          RequisitionsService.addRequisition(foreignSource).then(
            function(r) { // success
              growl.success('The requisition ' + r.foreignSource + ' has been created.');
            },
            $scope.errorHandler
          );
        }
      });
    };

    /**
    * @description Edits the foreign source definition of an existing requisition
    *
    * @name RequisitionsController:editForeignSource
    * @ngdoc method
    * @methodOf RequisitionsController
    * @param {string} foreignSource The name of the requisition
    */
    $scope.editForeignSource = function(foreignSource) {
      $window.location.href = '#/requisitions/' + foreignSource + '/foreignSource';
    };

    /**
    * @description Goes to the edit page of an existing requisition (navigation)
    *
    * @name RequisitionsController:edit
    * @ngdoc method
    * @methodOf RequisitionsController
    * @param {string} foreignSource The name of the requisition
    */
    $scope.edit = function(foreignSource) {
      $window.location.href = '#/requisitions/' + foreignSource;
    };

    /**
    * @description Requests the synchronization/import of a requisition on the server
    *
    * A dialog box is displayed to request to the user if the scan phase should be triggered or not.
    *
    * @name RequisitionsController:synchronize
    * @ngdoc method
    * @methodOf RequisitionsController
    * @param {string} foreignSource The name of the requisition
    */
    $scope.synchronize = function(foreignSource) {
      SynchronizeService.synchronize(foreignSource, $scope.errorHandler);
    };

    /**
    * @description Removes all the nodes form the requisition on the server
    *
    * @name RequisitionsController:removeAllNodes
    * @ngdoc method
    * @methodOf RequisitionsController
    * @param {string} foreignSource The name of the requisition
    */
    $scope.removeAllNodes = function(foreignSource) {
      bootbox.confirm('Are you sure you want to remove all the nodes from ' + foreignSource + '?', function(ok) {
        if (ok) {
          RequisitionsService.startTiming();
          RequisitionsService.removeAllNodesFromRequisition(foreignSource).then(
            function() { // success
              growl.success('All the nodes from ' + foreignSource + ' have been removed');
            },
            $scope.errorHandler
          );
        }
      });
    };

    /**
    * @description Removes a requisition on the server
    *
    * @name RequisitionsController:delete
    * @ngdoc method
    * @methodOf RequisitionsController
    * @param {string} foreignSource The name of the requisition
    */
    $scope.delete = function(foreignSource) {
      bootbox.confirm('Are you sure you want to remove the requisition ' + foreignSource + '?', function(ok) {
        if (ok) {
          RequisitionsService.startTiming();
          RequisitionsService.deleteRequisition(foreignSource).then(
            function() { // success
              growl.success('The requisition ' + foreignSource + ' has been deleted.');
            },
            $scope.errorHandler
          );
        }
      });
    };

    /**
    * @description Edits the default foreign source definition (navigation)
    *
    * @name RequisitionsController:editDefaultForeignSource
    * @ngdoc method
    * @methodOf RequisitionsController
    */
    $scope.editDefaultForeignSource = function() {
      $window.location.href = '#/requisitions/default/foreignSource';
    };

    /**
    * @description Resets the default set of detectors and policies
    *
    * @name RequisitionsController:resetDefaultForeignSource
    * @ngdoc method
    * @methodOf RequisitionsController
    */
    $scope.resetDefaultForeignSource = function() {
      bootbox.confirm('Are you sure you want to reset the default foreign source definition ?', function(ok) {
        if (ok) {
          RequisitionsService.startTiming();
          RequisitionsService.deleteForeignSourceDefinition('default').then(
            function() { // success
              growl.success('The default foreign source definition has been reseted.');
              $scope.initialize();
            },
            $scope.errorHandler
          );
        }
      });
    };

    /**
    * @description Refreshes the local requisitions list from the server
    *
    * @name RequisitionsController:refresh
    * @ngdoc method
    * @methodOf RequisitionsController
    */
    $scope.refresh = function() {
      growl.success('Refreshing requisitions...');
      RequisitionsService.clearRequisitionsCache();
      $scope.requisitions = [];
      $scope.initialize();
    };

   /**
    * @description Updates the pagination variables for the requisitions.
    *
    * @name RequisitionsController:updateFilteredNodes
    * @ngdoc method
    * @methodOf RequisitionsController
    */
    $scope.updateFilteredRequisitions = function() {
      $scope.currentPage = 1;
      $scope.totalItems = $scope.filteredRequisitions.length;
      $scope.numPages = Math.ceil($scope.totalItems / $scope.pageSize);
    };

    /**
    * @description Initializes the local requisitions list from the server
    *
    * @name RequisitionsController:initialize
    * @ngdoc method
    * @methodOf RequisitionsController
    */
    $scope.initialize = function() {
      $scope.loaded = false;
      RequisitionsService.getRequisitions().then(
        function(data) { // success
          $scope.requisitions = data.requisitions;
          $scope.filteredRequisitions = data.requisitions;
          $scope.updateFilteredRequisitions();
          $scope.loaded = true;
          growl.success('Loaded ' + data.requisitions.length + ' requisitions...');
        },
        $scope.errorHandler
      );
    };

    /**
    * @description Watch for filter changes in order to update the requisitions list and updates the pagination control
    *
    * @name RequisitionsController:reqFilter
    * @ngdoc event
    * @methodOf RequisitionsController
    */
    $scope.$watch('reqFilter', function() {
      $scope.filteredRequisitions = $filter('filter')($scope.requisitions, $scope.reqFilter);
      $scope.updateFilteredRequisitions();
    });

    // Initialization

    if ($scope.filteredRequisitions.length === 0) {
      $scope.initialize();
    }

  }]);

}());
