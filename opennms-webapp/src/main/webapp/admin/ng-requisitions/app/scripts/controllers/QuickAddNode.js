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
  * @requires $uibModalInstance Angular UI modal instance
  * @requires foreignSources The list of available requisitions (a.k.a. foreign source)
  * @requires RequisitionsService The requisitions service
  *
  * @description The controller for manage the modal dialog for quick add a node to an existing requisition.
  */
  .controller('QuickAddNodeController', ['$scope', '$uibModalInstance', 'foreignSources', 'RequisitionsService', function($scope, $uibModalInstance, foreignSources, RequisitionsService) {

    /**
    * @description The available foreign sources
    *
    * @ngdoc property
    * @name QuickAddNodeController#foreignSources
    * @propertyOf QuickAddNodeController
    * @returns {array} List of available foreign sources
    */
    $scope.foreignSources = foreignSources;

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
      $uibModalInstance.close($scope.node);
    };

    /**
    * @description Cancels current operation
    *
    * @name QuickAddNodeController:cancel
    * @ngdoc method
    * @methodOf QuickAddNodeController
    */
    $scope.cancel = function() {
      $uibModalInstance.dismiss('cancel');
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
    * @name NodeController:save
    * @ngdoc method
    * @methodOf NodeController
    */
    $scope.addCategory = function() {
      $scope.node.addNewCategory();
      this.quickAddNodeForm.$dirty = true;
    };

    // Initialize categories
    RequisitionsService.getAvailableCategories().then(
      function(categories) { // success
        $scope.availableCategories = categories;
      },
      $scope.errorHandler
    );

  }]);

}());
