/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
const bootbox = require('bootbox');

require('./Requisitions');

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
      * @param {object} requisition The requisition object
      * @param {function} successHandler The function to call after a successful synchronization
      * @param {function} errorHandler The function to call when something went wrong.
      */
      synchronize: function(requisition, errorHandler) {
        /**
        * @param {object} requisition The requisition object
        * @param {string} rescanExisting true to perform a full scan, false to only add/remove nodes without scan, dbonly for all DB operations without scan
        */
        var doSynchronize = function(requisition, rescanExisting) {
          RequisitionsService.startTiming();
          RequisitionsService.synchronizeRequisition(requisition.foreignSource, rescanExisting).then(
            function() { // success
              growl.success('The import operation has been started for ' + _.escape(requisition.foreignSource) + ' (rescanExisting? ' + rescanExisting + ')<br/>Use <b>refresh</b> to update the deployed statistics');
              requisition.setDeployed(true);
            },
            errorHandler
          );
        };
        bootbox.prompt({
            title: 'Synchronize Requisition  ' +  _.escape(requisition.foreignSource),
            message: '<p><b>Choose a scan option: </b></p>',
            inputType: 'radio',
            inputOptions: [
            {
                text: 'Scan all nodes',
                value: 'true',
            },
            {
                text: 'Scan added nodes only',
                value: 'false',
            },
            {
                text: 'No scanning',
                value: 'dbonly',
            }
            ],
            buttons: {
                    confirm: {
                        label: 'Synchronize',
                    },
                    cancel: {
                        label: 'Cancel',
                    }
                },
            swapButtonOrder: 'true',
            callback: function (result) {
                if (result !== null) {
                    doSynchronize(requisition, result);
                }
            }
        });
      }
    };
  }]);

}());
