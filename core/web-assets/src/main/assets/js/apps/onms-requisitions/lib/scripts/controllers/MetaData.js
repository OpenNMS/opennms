require('../services/Requisitions');

(function() {

  'use strict';

  angular.module('onms-requisitions')

  /**
  * @ngdoc controller
  * @name MetaDataController
  * @module onms-requisitions
  *
  * @requires $scope Angular local scope
  * @requires $uibModalInstance Angular UI modal instance
  * @requires RequisitionsService The Requisitions Servive
  * @requires metaData Meta-Data entry object
  * @requires metadataBlackList The black list of metaData fields
  *
  * @description The controller for manage the modal dialog for add/edit metaData entries of requisitioned nodes
  */
  .controller('MetaDataController', ['$scope', '$uibModalInstance', 'RequisitionsService', 'entry', 'keyBlackList', function($scope, $uibModalInstance, RequisitionsService, entry, keyBlackList) {

    /**
    * @description The entry object
    *
    * @ngdoc property
    * @name MetaDataController#entry
    * @propertyOf MetaDataController
    * @returns {object} The metaData entry object
    */
    $scope.entry = entry;

    /**
    * @description The black list of meta-data key fields.
    *
    * @ngdoc property
    * @name MetaDataController#keyBlackList
    * @propertyOf MetaDataController
    * @returns {array} The black list of keys.
    */
    $scope.keyBlackList = keyBlackList;

    /**
    * @description Saves the current meta-data entry
    *
    * @name MetaDataController:save
    * @ngdoc method
    * @methodOf MetaDataController
    */
    $scope.save = function() {
      $uibModalInstance.close($scope.entry);
    };

    /**
    * @description Cancels current operation
    *
    * @name MetaDataController:cancel
    * @ngdoc method
    * @methodOf MetaDataController
    */
    $scope.cancel = function() {
      $uibModalInstance.dismiss('cancel');
    };
  }]);

}());
