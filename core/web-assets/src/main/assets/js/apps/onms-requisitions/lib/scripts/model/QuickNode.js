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
const RequisitionNode = require('./RequisitionNode');

/**
* @author Alejandro Galue <agalue@opennms.org>
* @copyright 2014 The OpenNMS Group, Inc.
*/

/**
* @ngdoc object
* @name QuickNode
* @module onms-requisitions
* @constructor
*/
const QuickNode = function QuickNode() {

  'use strict';

  // eslint-disable-next-line @typescript-eslint/no-this-alias
  const self = this;

  self.foreignSource = null;
  self.foreignId = String(new Date().getTime());
  self.nodeLabel = null;
  self.ipAddress = null;
  self.snmpCommunity = 'public';
  self.snmpVersion = 'v2c';
  self.noSnmp = false;
  self.deviceUsername = null;
  self.devicePassword = null;
  self.enablePassword = null;
  self.accessMethod = null;
  self.autoEnable = false;
  self.categories = [];

  /**
  * @description Creates a new RequisitionNode object based on the current settings.
  *
  * @name QuickNode:createRequisitionedNode
  * @ngdoc method
  * @methodOf QuickNode
  * @returns {object} the new RequisitionNode Object
  */
  self.createRequisitionedNode = function() {
    const reqNode = new RequisitionNode(self.foreignSource, {
      'foreign-id': self.foreignId,
      'node-label': self.nodeLabel,
      'interface': [{
        'ip-addr': self.ipAddress,
        'snmp-primary': self.noSnmp ? 'N' : 'P'
      }],
      'category': self.categories
    }, false);
    if (self.deviceUsername && self.deviceUsername.trim() !== '') {
      reqNode.assets.push({'name': 'username', 'value': self.deviceUsername});
    }
    if (self.devicePassword && self.devicePassword.trim() !== '') {
      reqNode.assets.push({'name': 'password', 'value': self.devicePassword});
    }
    if (self.enablePassword && self.enablePassword.trim() !== '') {
      reqNode.assets.push({'name': 'enable', 'value': self.enablePassword});
    }
    if (self.accessMethod && self.accessMethod.trim() !== '') {
      reqNode.assets.push({'name': 'connection', 'value': self.accessMethod});
    }
    if (self.autoEnable) {
      reqNode.assets.push({'name': 'autoenable', 'value': 'A'});
    }
    return reqNode;
  };

  /**
  * @description Adds a new category to the node
  *
  * @name QuickNode:addNewCategory
  * @ngdoc method
  * @methodOf QuickNode
  * @returns {object} the new service Object
  */
  self.addNewCategory = function() {
    self.categories.push({
      name: ''
    });
    return self.categories.length -1;
  };

  self.className = 'QuickNode';

  return self;
}

module.exports = QuickNode;