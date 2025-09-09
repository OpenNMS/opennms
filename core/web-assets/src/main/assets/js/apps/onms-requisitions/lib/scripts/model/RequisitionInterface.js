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

const RequisitionService = require('./RequisitionService');

/**
* @ngdoc object
* @name RequisitionInterface
* @module onms-requisitions
* @param {Object} intf an OpenNMS interface JSON object
* @constructor
*/
const RequisitionInterface = function RequisitionInterface(intf) {
  'use strict';

  // eslint-disable-next-line @typescript-eslint/no-this-alias
  const self = this;

  /**
   * @description The IP Address of the interface
   * @ngdoc property
   * @name RequisitionInterface#ipAddress
   * @propertyOf RequisitionInterface
   * @returns {string} The IP Address of the interface
   */
  self.ipAddress = intf['ip-addr'];

  /**
   * @description The description of the interface
   * @ngdoc property
   * @name RequisitionInterface#description
   * @propertyOf RequisitionInterface
   * @returns {string} The description of the interface
   */
  self.description = intf['descr'];

  /**
   * @description The primary flag ('P' for primary, 'S' for secondary or 'N' for None)
   * @ngdoc property
   * @name RequisitionInterface#snmpPrimary
   * @propertyOf RequisitionInterface
   * @returns {string} The primary flag
   */
  self.snmpPrimary = intf['snmp-primary'];

  /**
   * @description The status of the interface (managed or unmanaged)
   * @ngdoc property
   * @name RequisitionInterface#status
   * @propertyOf RequisitionInterface
   * @returns {string} The status
   */
  self.status = 'managed';
  if (intf && intf['status']) {
    self.status = intf['status'] === '1' ? 'managed' : 'unmanaged';
  }

  /**
   * @description The array of services. Each service is an object with a name property, for example: { name: 'ICMP' }
   * @ngdoc property
   * @name RequisitionInterface#services
   * @propertyOf RequisitionInterface
   * @returns {array} The services
   */
  self.services = [];

  angular.forEach(intf['monitored-service'], function(svc) {
    self.services.push(new RequisitionService(svc));
  });

  self.className = 'RequisitionInterface';

  return self;
};

module.exports = RequisitionInterface;
