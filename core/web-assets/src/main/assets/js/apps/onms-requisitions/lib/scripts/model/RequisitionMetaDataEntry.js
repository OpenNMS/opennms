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
/**
* @author Jesse White <jesse@opennms.org>
* @copyright 2019 The OpenNMS Group, Inc.
*/
const MetaDataConstants = require('./MetaDataConstants');
const Scope = MetaDataConstants.Scope;

/**
* @ngdoc object
* @name RequisitionMetaDataEntry
* @module onms-requisitions
* @constructor
*/
const RequisitionMetaDataEntry = function RequisitionMetaDataEntry() {
  'use strict';

  const self = this;

  // Default to the 'node' scope
  self.scope = Scope.NODE;

  // Must be set to the related RequisitionInterface object when the scope is set to 'interface' or 'service'
  self.scoped_interface = null;

  // Must be set to the related RequisitionService object when the scope is set to 'service'
  self.scoped_service = null;

  // Default to using the 'requisition' context
  self.context = MetaDataConstants.RequisitionContext;

  self.key = null;

  self.value = null;

  self.displayScope = function() {
    if (self.scope === Scope.INTERFACE) {
      return 'Interface';
    } else if (self.scope === Scope.SERVICE) {
      return 'Service';
    }
    return 'Node';
  };

  self.displayInterface = function() {
    if (self.scoped_interface !== null) {
      return self.scoped_interface.ipAddress;
    }
    return null;
  };

  self.displayService = function() {
    if (self.scoped_service !== null) {
      return self.scoped_service.name;
    }
    return null;
  };

  self.className = 'RequisitionMetaDataEntry';

  return self;
};

module.exports = RequisitionMetaDataEntry;
