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
  * @name QuickAddNodeController
  * @module onms-requisitions
  *
  * @requires $scope Angular local scope
  * @requires foreignSources The list of available requisitions (a.k.a. foreign source)
  * @requires RequisitionsService The requisitions service
  * @requires growl The growl plugin for instant notifications
  *
  * @description The controller for manage the modal dialog for quick add a node to an existing requisition.
  */
  .controller('QuickAddNodeController', ['$scope', 'foreignSources', 'RequisitionsService', 'growl', function($scope, foreignSources, RequisitionsService, growl) {

    /**
    * @description The available foreign sources
    *
    * @ngdoc property
    * @name QuickAddNodeController#foreignSources
    * @propertyOf QuickAddNodeController
    * @returns {array} List of available foreign sources
    */
    $scope.foreignSources = [];

    /**
    * @description The available configured categories
    *
    * @ngdoc property
    * @name QuickAddNodeController#availableCategories
    * @propertyOf QuickAddNodeController
    * @returns {array} The categories
    */
    $scope.availableCategories = [];

    /**
    * @description The available access methods
    *
    * @ngdoc property
    * @name QuickAddNodeController#availableAccessMethods
    * @propertyOf QuickAddNodeController
    * @returns {array} The access methods
    */
    $scope.availableAccessMethods = [ 'RSH', 'SSH', 'Telnet' ];

    /**
    * @description The source object that contains all the required information for the new node
    *
    * @ngdoc property
    * @name QuickAddNodeController#node
    * @propertyOf QuickAddNodeController
    * @returns {object} The source object
    */
    $scope.node = new QuickNode();

    /**
    * @description Provision the current node
    *
    * @name QuickAddNodeController:provision
    * @ngdoc method
    * @methodOf QuickAddNodeController
    */
    $scope.provision = function() {
      growl.warning('The node ' + $scope.node.nodeLabel + ' will be added to ' + $scope.node.foreignSource + '. Please wait...');
      RequisitionsService.quickAddNode($scope.node).then(
        function() { // success
          growl.success('The node ' + $scope.node.nodeLabel + ' has been added to ' + $scope.node.foreignSource);
        },
        $scope.errorHandler
      );
    };

   /**
    * @description Get the unused available categories
    *
    * @name QuickAddNodeController:getAvailableCategories
    * @ngdoc method
    * @methodOf QuickAddNodeController
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
    * @name QuickAddNodeController:removeCategory
    * @ngdoc method
    * @methodOf QuickAddNodeController
    * @param {integer} index The index of the category to be removed
    */
    $scope.removeCategory = function(index) {
      $scope.node.categories.splice(index, 1);
      this.quickAddNodeForm.$dirty = true;
    };

    /**
    * @description Adds a new category to the local node
    *
    * @name QuickAddNodeController:addCategory
    * @ngdoc method
    * @methodOf QuickAddNodeController
    */
    $scope.addCategory = function() {
      $scope.node.addNewCategory();
      this.quickAddNodeForm.$dirty = true;
    };

    /**
    * @description Checks if the form is valid or not
    *
    * @name QuickAddNodeController:isInvalid
    * @ngdoc method
    * @methodOf QuickAddNodeController
    * @returns {boolean} true if the form is invalid.
    */
    $scope.isInvalid = function() {
      if (this.quickAddNodeForm == null
        || this.quickAddNodeForm.foreignSource == null
        || this.quickAddNodeForm.ipAddress == null
        || this.quickAddNodeForm.nodeLabel == null) {
        return true;
      }
      return this.quickAddNodeForm.foreignSource.$invalid
        || this.quickAddNodeForm.ipAddress.$invalid
        || this.quickAddNodeForm.nodeLabel.$invalid;
    }

    /**
    * @description Shows an error to the user
    *
    * @name QuickAddNodeController:errorHandler
    * @ngdoc method
    * @methodOf QuickAddNodeController
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

    // Initialize requisitions
    // TODO Implement a ReST end point for getting requisition names (it will be less expensive)
    if (foreignSources == null) {
      RequisitionsService.getRequisitions().then(
        function(data) { // success
          angular.forEach(data.requisitions, function(r) {
            $scope.foreignSources.push(r.foreignSource);
          });
        },
        $scope.errorHandler
      );
    } else {
      $scope.foreignSources = foreignSources;
    }

  }]);

}());
