/*global bootbox:true */

/**
* @author Alejandro Galue <agalue@opennms.org>
* @copyright 2014 The OpenNMS Group, Inc.
*/

(function() {

  'use strict';

  angular.module('onms-requisitions')

  /**
  * @ngdoc service
  * @name SynchronizeService
  * @module onms-requisitions
  *
  * @requires RequisitionsService The requisitions service
  * @requires growl The growl plugin for instant notifications
  *
  * @description The SynchronizeService provides a way to request a requisition synchronization asking the user how the scan process will be processed.
  */
  .factory('SynchronizeService', ['RequisitionsService', 'growl', function(RequisitionsService, growl) {
    return {
      /**
      * @description Requests the synchronization/import of a requisition on the server
      *
      * A dialog box is displayed to request to the user if the scan phase should be triggered or not.
      *
      * @name SynchronizeService:synchronize
      * @ngdoc method
      * @methodOf SynchronizeService
      * @param {string} foreignSource The name of the requisition
      * @param {function} errorHandler The function to call when something went wrong.
      */
      synchronize: function(foreignSource, errorHandler) {
        /**
        * @param {string} foreignSource The name of the requisition
        * @param {string} rescanExisting true to perform a full scan, false to only add/remove nodes without scan, dbonly for all DB operations without scan
        */
        var doSynchronize = function(foreignSource, rescanExisting) {
          RequisitionsService.startTiming();
          RequisitionsService.synchronizeRequisition(foreignSource, rescanExisting).then(
            function() { // success
              growl.success('The import operation has been started for ' + foreignSource + ' (rescanExisting? ' + rescanExisting + ')');
            },
            errorHandler
          );
        };
        bootbox.dialog({
          message: 'Do you want to rescan existing nodes ?<br/><hr/>' +
                   'Choose <b>yes</b> to synchronize all the nodes with the database executing the scan phase.<br/>' +
                   'Choose <b>no</b> to synchronize only the new and deleted nodes with the database executing the scan phase only for new nodes.<br/>' +
                   'Choose <b>dbonly</b> to synchronize all the nodes with the database skipping the scan phase.<br/>' +
                   'Choose <b>cancel</b> to abort the request.',
          title: 'Synchronize Requisition ' + foreignSource,
          buttons: {
            success: {
              label: 'Yes',
              className: 'btn-success',
              callback: function() {
                doSynchronize(foreignSource, 'true');
              }
            },
            warning: {
              label: 'DB Only',
              className: 'btn-warning',
              callback: function() {
                doSynchronize(foreignSource, 'dbonly');
              }
            },
            danger: {
              label: 'No',
              className: 'btn-danger',
              callback: function() {
                doSynchronize(foreignSource, 'false');
              }
            },
            main: {
              label: 'Cancel',
              className: 'btn-default'
            }
          }
        });
      }
    };
  }]);

}());
