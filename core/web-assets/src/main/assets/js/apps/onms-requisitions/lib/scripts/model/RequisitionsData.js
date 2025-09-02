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
* @author Alejandro Galue <agalue@opennms.org>
* @copyright 2014 The OpenNMS Group, Inc.
*/

/**
* @ngdoc object
* @name RequisitionsData
* @module onms-requisitions
* @constructor
*/
const RequisitionsData = function RequisitionsData() {
  'use strict';

  // eslint-disable-next-line @typescript-eslint/no-this-alias
  const self = this;

  /**
   * @description The configured requisitions.
   * @ngdoc property
   * @name RequisitionsData#requisitions
   * @propertyOf RequisitionsData
   * @returns {array} The requisitions array
   */
  self.requisitions = [];

  /**
  * @description Gets the array possition for a particular node
  *
  * @name RequisitionsData:indexOf
  * @ngdoc method
  * @param {string} foreignSource The foreign source (a.k.a requisition name)
  * @methodOf RequisitionsData
  * @returns {integer} the index (-1 if the foreign source doesn't exist)
  */
  self.indexOf = function(foreignSource) {
    for(let i = 0; i < self.requisitions.length; i++) {
      if (self.requisitions[i].foreignSource === foreignSource) {
        return i;
      }
    }
    return -1;
  };

  /**
  * @description Gets the requisition object for a given foreign source.
  *
  * @name RequisitionsData:getRequisition
  * @ngdoc method
  * @param {string} foreignSource The foreign source (a.k.a requisition name)
  * @methodOf RequisitionsData
  * @returns {object} the requisition object.
  */
  self.getRequisition = function(foreignSource) {
    const idx = self.indexOf(foreignSource);
    return idx < 0 ? null : self.requisitions[idx];
  };

  /**
  * @description Adds or replaces a requisition object.
  *
  * @name RequisitionsData:setRequisition
  * @ngdoc method
  * @param {object} requisition The Requisition object
  * @methodOf RequisitionsData
  */
  self.setRequisition = function(requisition) {
    const idx = self.indexOf(requisition.foreignSource);
    if (idx < 0) {
      self.requisitions.push(requisition);
    } else {
      self.requisitions[idx] = requisition;
    }
  };

  self.className = 'RequisitionsData';

  return self;
}

module.exports = RequisitionsData;