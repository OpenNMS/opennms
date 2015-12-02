/*global bootbox:true, RequisitionNode:true */

/**
* @author Alejandro Galue <agalue@opennms.org>
* @copyright 2014 The OpenNMS Group, Inc.
*/

(function() {

  'use strict';

  angular.module('onms-requisitions')

  /**
  * @ngdoc controller
  * @name NodeController
  * @module onms-requisitions
  *
  * @requires $scope Angular local scope
  * @requires $routeParams Angular route params
  * @requires $window Document window
  * @requires $modal Angular modal
  * @requires RequisitionsService The requisitions service
  * @requires EmptyTypeaheadService The empty typeahead Service
  * @requires growl The growl plugin for instant notifications
  *
  * @description The controller for manage requisitioned nodes (add/edit the nodes on a specific requisition)
  */
  .controller('NodeController', ['$scope', '$routeParams', '$window', '$modal', 'RequisitionsService', 'EmptyTypeaheadService', 'growl', function($scope, $routeParams, $window, $modal, RequisitionsService, EmptyTypeaheadService, growl) {

    /**
    * @description The timing status.
    *
    * @ngdoc property
    * @name NodeController#timingStatus
    * @propertyOf NodeController
    * @returns {object} The timing status object
    */
    $scope.timingStatus = RequisitionsService.getTiming();

    /**
    * @description The foreign source (a.k.a the name of the requisition).
    * The default value is obtained from the $routeParams.
    *
    * @ngdoc property
    * @name NodeController#foreignSource
    * @propertyOf NodeController
    * @returns {string} The foreign source
    */
    $scope.foreignSource = $routeParams.foreignSource;

    /**
    * @description The foreign ID
    * The default value is obtained from the $routeParams.
    * For new nodes, the content must be '__new__'.
    *
    * @ngdoc property
    * @name NodeController#foreignId
    * @propertyOf NodeController
    * @returns {string} The foreign ID
    */
    $scope.foreignId = $routeParams.foreignId;

    /**
    * @description The isNew flag
    *
    * @ngdoc property
    * @name NodeController#isNew
    * @propertyOf NodeController
    * @returns {boolean} true, if the foreign ID is equal to '__new__'
    */
    $scope.isNew = $scope.foreignId === '__new__';

    /**
    * @description The node object
    *
    * @ngdoc property
    * @name NodeController#node
    * @propertyOf NodeController
    * @returns {object} The node object
    */
    $scope.node = {};

    /**
    * @description The available configured categories
    *
    * @ngdoc property
    * @name NodeController#availableCategories
    * @propertyOf NodeController
    * @returns {array} The categories
    */
    $scope.availableCategories = [];

    /**
    * @description The list of black-listed foreign IDs.
    * The foreignId must be unique within the requisition.
    * For an existing node, the foreignId should not be changed.
    * For new nodes, the foreignId must be validated.
    *
    * @ngdoc property
    * @name NodeController#foreignIdBlackList
    * @propertyOf NodeController
    * @returns {array} The list of black-listed foreign IDs.
    */
    $scope.foreignIdBlackList = [];

    /**
    * @description fieldComparator method from EmptyTypeaheadService
    *
    * @ngdoc method
    * @name NodeController#fieldComparator
    * @methodOf AssetController
    */
    $scope.fieldComparator = EmptyTypeaheadService.fieldComparator;

    /**
    * @description onFocus method from EmptyTypeaheadService
    *
    * @ngdoc method
    * @name NodeController#onFocus
    * @methodOf AssetController
    */
    $scope.onFocus = EmptyTypeaheadService.onFocus;

    /**
    * @description Goes to specific URL warning about changes if exist.
    *
    * @name NodeController:goTo
    * @ngdoc method
    * @methodOf NodeController
    * @param {string} url The URL to go
    */
    $scope.goTo = function(url) {
      var doGoTo = function() {
        $window.location.href = url;
      };
      if (this.nodeForm.$dirty) {
        bootbox.dialog({
          message: 'There are changes on the current node. Are you sure you want to cancel ?',
          title: 'Cancel Changes',
          buttons: {
            success: {
              label: 'Yes',
              className: 'btn-danger',
              callback: doGoTo
            },
            main: {
              label: 'No',
              className: 'btn-default'
            }
          }
        });
      } else {
        doGoTo();
      }
    };

    /**
    * @description Goes back to requisitions list (navigation)
    *
    * @name NodeController:goTop
    * @ngdoc method
    * @methodOf NodeController
    */
    $scope.goTop = function() {
      $scope.goTo('#/requisitions');
    };

    /**
    * @description Goes back to requisition editor (navigation)
    *
    * @name NodeController:goBack
    * @ngdoc method
    * @methodOf NodeController
    */
    $scope.goBack = function() {
      $scope.goTo('#/requisitions/' + $scope.foreignSource);
    };

    /**
    * @description Shows an error to the user
    *
    * @name NodeController:errorHandler
    * @ngdoc method
    * @methodOf NodeController
    * @param {string} message The error message
    */
    $scope.errorHandler = function(message) {
      growl.error(message, {ttl: 10000});
    };

    /**
    * @description Generates a foreign Id
    *
    * @name NodeController:generateForeignId
    * @ngdoc method
    * @methodOf NodeController
    * @param {object} the form object associated with the foreignId
    */
    $scope.generateForeignId = function(formObj) {
      $scope.node.foreignId = new Date().getTime() + '';
      formObj.$invalid = false;
    };

    /**
    * @description Shows the dialog for add/edit an asset field
    *
    * @name NodeController:save
    * @ngdoc method
    * @methodOf NodeController
    * @param {integer} index The index of the asset to be edited
    * @param {boolean} isNew true, if the asset is new
    */
    $scope.editAsset = function(index, isNew) {
      var form = this.nodeForm;
      var assetToEdit = $scope.node.assets[index];
      var assetsBlackList = [];
      angular.forEach($scope.node.assets, function(asset) {
        assetsBlackList.push(asset.name);
      });

      var modalInstance = $modal.open({
        backdrop: 'static',
        controller: 'AssetController',
        templateUrl: 'views/asset.html',
        resolve: {
          asset: function() { return angular.copy(assetToEdit); },
          assetsBlackList: function() { return assetsBlackList; }
        }
      });

      modalInstance.result.then(function(result) {
        angular.copy(result, assetToEdit);
        form.$dirty = true;
      }, function() {
        if (isNew) {
          $scope.node.assets.pop();
        }
      });
    };

    /**
    * @description Removes an asset from the local node
    *
    * @name NodeController:save
    * @ngdoc method
    * @methodOf NodeController
    * @param {integer} index The index of the asset to be removed
    */
    $scope.removeAsset = function(index) {
      $scope.node.assets.splice(index, 1);
      this.nodeForm.$dirty = true;
    };

    /**
    * @description Adds a new asset to the local node
    *
    * @name NodeController:save
    * @ngdoc method
    * @methodOf NodeController
    */
    $scope.addAsset = function() {
      $scope.editAsset($scope.node.addNewAsset(), true);
    };

    /**
    * @description Shows a modal dialog for add/edit an interface
    *
    * @name NodeController:save
    * @ngdoc method
    * @methodOf NodeController
    * @param {integer} index The index of the interface to be edited
    * @param {boolean} isNew true, if the interface is new
    */
    $scope.editInterface = function(index, isNew) {
      var form = this.nodeForm;
      var intfToEdit = $scope.node.interfaces[index];
      var foreignSource = $scope.foreignSource;
      var foreignId = $scope.foreignId;
      var ipBlackList = [];
      angular.forEach($scope.node.interfaces, function(intf) {
        ipBlackList.push(intf.ipAddress);
      });

      var modalInstance = $modal.open({
        backdrop: 'static',
        controller: 'InterfaceController',
        templateUrl: 'views/interface.html',
        resolve: {
          foreignId: function() { return foreignId; },
          foreignSource: function() { return foreignSource; },
          requisitionInterface: function() { return angular.copy(intfToEdit); },
          ipBlackList: function() { return ipBlackList; }
        }
      });

      modalInstance.result.then(function(result) {
        angular.copy(result, intfToEdit);
        form.$dirty = true;
      }, function() {
        if (isNew) {
          $scope.node.interfaces.pop();
        }
      });
    };

    /**
    * @description Removes an interface from the local node
    *
    * @name NodeController:save
    * @ngdoc method
    * @methodOf NodeController
    * @param {integer} index The index of the interface to be removed
    */
    $scope.removeInterface = function(index) {
      $scope.node.interfaces.splice(index, 1);
      this.nodeForm.$dirty = true;
    };

    /**
    * @description Adds a new interface to the local node
    *
    * @name NodeController:save
    * @ngdoc method
    * @methodOf NodeController
    */
    $scope.addInterface = function() {
      $scope.editInterface($scope.node.addNewInterface(), true);
    };

    /**
    * @description Removes a category from the local node
    *
    * @name NodeController:save
    * @ngdoc method
    * @methodOf NodeController
    * @param {integer} index The index of the category to be removed
    */
    $scope.removeCategory = function(index) {
      $scope.node.categories.splice(index, 1);
      this.nodeForm.$dirty = true;
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
      this.nodeForm.$dirty = true;
    };

    /**
    * @description Saves the local node on the server
    *
    * @name NodeController:save
    * @ngdoc method
    * @methodOf NodeController
    */
    $scope.save = function() {
      var form = this.nodeForm;
      RequisitionsService.startTiming();
      RequisitionsService.saveNode($scope.node).then(
        function() { // success
          growl.success('The node ' + $scope.node.nodeLabel + ' has been saved.');
          form.$dirty = false;
        },
        $scope.errorHandler
      );
    };

    /**
    * @description Refresh the local node from the server
    *
    * @name NodeController:save
    * @ngdoc method
    * @methodOf NodeController
    */
    $scope.refresh = function() {
      growl.success('Retrieving node ' + $scope.foreignId + ' from requisition ' + $scope.foreignSource + '...');
      RequisitionsService.getNode($scope.foreignSource, $scope.foreignId).then(
        function(node) { // success
          $scope.node = node;
        },
        $scope.errorHandler
      );
    };

    /**
    * @description Get the unused available categories
    *
    * @name NodeController:getAvailableCategories
    * @ngdoc method
    * @methodOf NodeController
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
    * @description Gets the primary IP address
    *
    * @name NodeController:getPrimaryAddress
    * @ngdoc method
    * @methodOf NodeController
    * @returns {string} the primary IP address or 'N/A' if it doesn't exist.
    */
    $scope.getPrimaryAddress = function() {
      var ip = $scope.node.getPrimaryIpAddress();
      return ip == null ? "N/A" : ip;
    }

    // Initialization of the node's page for either adding a new node or editing an existing node

    if ($scope.isNew) {
      $scope.node = new RequisitionNode($scope.foreignSource, {});
    } else {
      $scope.refresh();
    }

    // Initialize categories
    RequisitionsService.getAvailableCategories().then(
      function(categories) { // success
        $scope.availableCategories = categories;
      },
      $scope.errorHandler
    );

    // Initialize foreign-id black list (thanks to the cache, this call is not expensive)
    RequisitionsService.getRequisition($scope.foreignSource).then(
      function(requisition) {
        angular.forEach(requisition.nodes, function(node) {
          $scope.foreignIdBlackList.push(node.foreignId);
        });
      },
      $scope.errorHandler
    );

  }]);

}());
