/*global RequisitionNode:true */

/**
* @author Alejandro Galue <agalue@opennms.org>
* @copyright 2014 The OpenNMS Group, Inc.
*/

(function() {

  'use strict';

  angular.module('onms-requisitions')

  /**
  * @ngdoc controller
  * @name QuickAddNodeStandaloneController
  * @module onms-requisitions
  *
  * @requires $scope Angular local scope
  * @requires RequisitionsService The requisitions service
  * @requires growl The growl plugin for instant notifications
  *
  * @description The controller for manage the modal dialog for quick add a node to an existing requisition.
  */
  .controller('QuickAddNodeStandaloneController', ['$scope', 'RequisitionsService', 'growl', function($scope, RequisitionsService, growl) {

    /**
    * @description The available foreign sources
    *
    * @ngdoc property
    * @name QuickAddNodeStandaloneController#foreignSources
    * @propertyOf QuickAddNodeStandaloneController
    * @returns {array} List of available foreign sources
    */
    $scope.foreignSources = [];

    /**
    * @description The available configured categories
    *
    * @ngdoc property
    * @name QuickAddNodeStandaloneController#availableCategories
    * @propertyOf QuickAddNodeStandaloneController
    * @returns {array} The categories
    */
    $scope.availableCategories = [];

    /**
    * @description The available access methods
    *
    * @ngdoc property
    * @name QuickAddNodeStandaloneController#availableAccessMethods
    * @propertyOf QuickAddNodeStandaloneController
    * @returns {array} The access methods
    */
    $scope.availableAccessMethods = [ 'RSH', 'SSH', 'Telnet' ];

    /**
    * @description The source object that contains all the required information for the new node
    *
    * @ngdoc property
    * @name QuickAddNodeStandaloneController#node
    * @propertyOf QuickAddNodeStandaloneController
    * @returns {object} The source object
    */
    $scope.node = new QuickNode();

    /**
    * @description Provision the current node
    *
    * @name QuickAddNodeStandaloneController:provision
    * @ngdoc method
    * @methodOf QuickAddNodeStandaloneController
    */
    $scope.provision = function() {
        growl.warning('The node ' + node.nodeLabel + ' will be added to ' + node.foreignSource + '. Please wait...');
        RequisitionsService.quickAddNode(node).then(
          function() { // success
            growl.success('The node ' + node.nodeLabel + ' has been added to ' + node.foreignSource);
          },
          $scope.errorHandler
        );
    };

   /**
    * @description Get the unused available categories
    *
    * @name QuickAddNodeStandaloneController:getAvailableCategories
    * @ngdoc method
    * @methodOf QuickAddNodeStandaloneController
    * @returns {array} the unused available categories
    */
    $scope.getAvailableCategories = function() {
      var categories = [];
      angular.forEach($scope.availableCategories, function(category) {
        var found = false;
        angular.forEach($scope.node.categories, function(c) {
          if (c.name == category) {
            found = true;
          }
        });
        if (!found) {
          categories.push(category);
        }
      });
      return categories;
    };

    /**
    * @description Removes a category from the local node
    *
    * @name QuickAddNodeStandaloneController:removeCategory
    * @ngdoc method
    * @methodOf QuickAddNodeStandaloneController
    * @param {integer} index The index of the category to be removed
    */
    $scope.removeCategory = function(index) {
      $scope.node.categories.splice(index, 1);
      this.quickAddNodeForm.$dirty = true;
    };

    /**
    * @description Adds a new category to the local node
    *
    * @name QuickAddNodeStandaloneController:addCategory
    * @ngdoc method
    * @methodOf QuickAddNodeStandaloneController
    */
    $scope.addCategory = function() {
      $scope.node.addNewCategory();
      this.quickAddNodeForm.$dirty = true;
    };

    /**
    * @description Checks if the form is valid or not
    *
    * @name QuickAddNodeStandaloneController:isInvalid
    * @ngdoc method
    * @methodOf QuickAddNodeStandaloneController
    * @returns {boolean} true if the form is invalid.
    */
    $scope.isInvalid = function() {
      return this.quickAddNodeForm.foreignSource.$invalid
      || this.quickAddNodeForm.ipAddress.$invalid
      || this.quickAddNodeForm.nodeLabel.$invalid;
    }

    /**
    * @description Shows an error to the user
    *
    * @name QuickAddNodeStandaloneController:errorHandler
    * @ngdoc method
    * @methodOf QuickAddNodeStandaloneController
    * @param {string} message The error message
    */
    $scope.errorHandler = function(message) {
      growl.error(message, {ttl: 10000});
    };

    // Initialize categories
    RequisitionsService.getAvailableCategories().then(
      function(categories) { // success
        $scope.availableCategories = categories;
      },
      $scope.errorHandler
    );

    // Initialize categories
    RequisitionsService.getRequisitions().then(
      function(data) { // success
        angular.forEach(data.requisitions, function(r) {
          console.log(r.foreignSource);
          $scope.foreignSources.push(r.foreignSource);
        });
      },
      $scope.errorHandler
    );

  }]);

}());
