/*global Requisition:true, bootbox:true */

/**
* @author Alejandro Galue <agalue@opennms.org>
* @copyright 2014 The OpenNMS Group, Inc.
*/

(function() {

  'use strict';

  angular.module('onms-requisitions')

  /**
  * @ngdoc controller
  * @name RequisitionController
  * @module onms-requisitions
  *
  * @description The controller for manage a single requisition (add/edit)
  *
  * @requires $scope Angular local scope
  * @requires $filter Angular filter
  * @requires $cookies Angular cookies
  * @requires $window Document window
  * @requires $routeParams Angular route parameters
  * @requires RequisitionsService The requisitions service
  * @requires SynchronizeService The synchronize service
  * @requires growl The growl plugin for instant notifications
  */
  .controller('RequisitionController', ['$scope', '$filter', '$cookies', '$window', '$routeParams', 'RequisitionsService', 'SynchronizeService', 'growl', function($scope, $filter, $cookies, $window, $routeParams, RequisitionsService, SynchronizeService, growl) {

    /**
    * @description The timing status.
    *
    * @ngdoc property
    * @name RequisitionController#timingStatus
    * @propertyOf RequisitionController
    * @returns {object} The timing status object
    */
    $scope.timingStatus = RequisitionsService.getTiming();

    /**
    * @description The foreign source (a.k.a the name of the requisition).
    * The default value is obtained from the $routeParams.
    *
    * @ngdoc property
    * @name RequisitionController#foreignSource
    * @propertyOf RequisitionController
    * @returns {string} The foreign source
    */
    $scope.foreignSource = $routeParams.foreignSource;

    /**
    * @description The requisition object
    *
    * @ngdoc property
    * @name RequisitionController#requisition
    * @propertyOf RequisitionController
    * @returns {object} The requisition object
    */
    $scope.requisition = new Requisition({});

    /**
    * @description The filtered list of nodes
    *
    * @ngdoc property
    * @name RequisitionController#filteredNodes
    * @propertyOf RequisitionController
    * @returns {array} The filtered array
    */
    $scope.filteredNodes = [];

    /**
    * @description The amount of items per page for pagination (defaults to 10)
    *
    * @ngdoc property
    * @name RequisitionController#pageSize
    * @propertyOf RequisitionController
    * @returns {integer} The page size
    */
    $scope.pageSize = 10;

    /**
    * @description The maximum size of pages for pagination (defaults to 5)
    *
    * @ngdoc property
    * @name RequisitionController#maxSize
    * @propertyOf RequisitionController
    * @returns {integer} The maximum size
    */
    $scope.maxSize = 5;

    /**
    * @description The total amount of items for pagination (defaults to 0)
    *
    * @ngdoc property
    * @name RequisitionController#totalItems
    * @propertyOf RequisitionController
    * @returns {integer} The total items
    */
    $scope.totalItems = 0;

    /**
    * @description Goes back to requisitions list (navigation)
    *
    * @name RequisitionController:goBack
    * @ngdoc method
    * @methodOf RequisitionController
    */
    // FIXME Should be called getTop to be consistent with the rest of the controllers
    $scope.goBack = function() {
      $window.location.href = '#/requisitions';
    };

    /**
    * @description Goes to the edition page for the foreign source definition of the requisition (navigation)
    *
    * @name RequisitionController:editForeignSource
    * @ngdoc method
    * @methodOf RequisitionController
    */
    $scope.editForeignSource = function() {
      $window.location.href = '#/requisitions/' + encodeURIComponent($scope.foreignSource) + '/foreignSource';
    };

    /**
    * @description Shows an error to the user
    *
    * @name RequisitionController:errorHandler
    * @ngdoc method
    * @methodOf RequisitionController
    * @param {string} message The error message
    */
    $scope.errorHandler = function(message) {
      growl.error(message, {ttl: 10000});
    };

    /**
    * @description Requests the synchronization/import of a requisition on the server
    *
    * A dialog box is displayed to request to the user if the scan phase should be triggered or not.
    *
    * @name RequisitionController:synchronize
    * @ngdoc method
    * @methodOf RequisitionController
    */
    $scope.synchronize = function() {
      SynchronizeService.synchronize($scope.requisition, $scope.errorHandler);
    };

    /**
    * @description Returns the vertical layout suffix for nodes if enabled
    *
    * @name RequisitionController:getVerticalLayout
    * @ngdoc method
    * @methodOf RequisitionController
    * @returns {string} URL suffix for vertical layout if enabled.
    */
    $scope.getVerticalLayout = function() {
      var isVertical = $cookies.get('use_requisitions_node_vertical_layout');
      return isVertical == 'true' ? '/vertical' : '';
    };

    /**
    * @description Goes to the page for adding a new node to the requisition (navigation)
    *
    * @name RequisitionController:addNode
    * @ngdoc method
    * @methodOf RequisitionController
    */
    $scope.addNode = function() {
      $window.location.href = '#/requisitions/' + encodeURIComponent($scope.foreignSource) + '/nodes/__new__' + $scope.getVerticalLayout();
    };

    /**
    * @description Goes to the page for editing an existing node of the requisition (navigation)
    * @description
    *
    * @name RequisitionController:editNode
    * @ngdoc method
    * @methodOf RequisitionController
    * @param {object} The node's object to edit
    */
    $scope.editNode = function(node) {
      $window.location.href = '#/requisitions/' + encodeURIComponent($scope.foreignSource) + '/nodes/' + encodeURIComponent(node.foreignId) + $scope.getVerticalLayout();
    };

    /**
    * @description Deletes a node from the requisition on the server and refresh the local nodes list
    *
    * @name RequisitionController:deleteNode
    * @ngdoc method
    * @methodOf RequisitionController
    * @param {object} The node's object to delete
    */
    $scope.deleteNode = function(node) {
      bootbox.confirm('Are you sure you want to remove the node ' + node.nodeLabel + '?', function(ok) {
        if (ok) {
          RequisitionsService.startTiming();
          RequisitionsService.deleteNode(node).then(
            function() { // success
              growl.success('The node ' + node.nodeLabel + ' has been deleted.');
            },
            $scope.errorHandler
          );
        }
      });
    };

   /**
    * @description Updates the pagination variables for the nodes.
    *
    * @name RequisitionController:updateFilteredNodes
    * @ngdoc method
    * @methodOf RequisitionController
    */
    $scope.updateFilteredNodes = function() {
      $scope.currentPage = 1;
      $scope.totalItems = $scope.filteredNodes.length;
      $scope.numPages = Math.ceil($scope.totalItems / $scope.pageSize);
    };

    /**
    * @description Refreshes the deployed statistics for the requisition from the server
    *
    * @name RequisitionController:refreshDeployedStats
    * @ngdoc method
    * @methodOf RequisitionController
    */
    $scope.refreshDeployedStats = function() {
      RequisitionsService.startTiming();
      RequisitionsService.updateDeployedStatsForRequisition($scope.requisition).then(
        function() { // success
          growl.success('The deployed statistics has been updated.');
        },
        $scope.errorHandler
      );
    };

    /**
    * @description Refreshes the currently loaded requisition from the server
    *
    * @name RequisitionController:refreshRequisition
    * @ngdoc method
    * @methodOf RequisitionController
    */
    $scope.refreshRequisition = function() {
      bootbox.confirm('Are you sure you want to reload the requisition?<br/>All current changes will be lost.', function(ok) {
        if (ok) {
          RequisitionsService.startTiming();
          $scope.requisition = new Requisition({});
          RequisitionsService.removeRequisitionFromCache();
          $scope.initialize(function() {
            $scope.refreshDeployedStats();
          });
        }
      });
    };

    /**
    * @description Initializes the local requisition from the server
    *
    * @name RequisitionController:initialize
    * @ngdoc method
    * @methodOf RequisitionController
    * @param {function} customHandler An optional method to be called after the initialization is done.
    */
    $scope.initialize = function(customHandler) {
      growl.success('Retrieving requisition ' + $scope.foreignSource + '...');
      RequisitionsService.getRequisition($scope.foreignSource).then(
        function(requisition) { // success
          $scope.requisition = requisition;
          $scope.filteredNodes = requisition.nodes;
          $scope.updateFilteredNodes();
          if (customHandler != null) {
            customHandler();
          }
        },
        $scope.errorHandler
      );
    };

    /**
    * @description Watch for filter changes in order to update the nodes list and updates the pagination control
    *
    * @name RequisitionController:reqFilter
    * @ngdoc event
    * @methodOf RequisitionController
    */
    $scope.$watch('reqFilter', function() {
      $scope.filteredNodes = $filter('filter')($scope.requisition.nodes, $scope.reqFilter);
      $scope.updateFilteredNodes();
    });

    // Initialization

    if ($scope.foreignSource) {
      $scope.initialize();
    }

  }]);

}());
