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
/* eslint no-console: 0 */

const GraphContainers = require('apps/onms-graph');

if (!window.org_opennms_features_vaadin_components_graph_InlineGraphContainer) {
  window.org_opennms_features_vaadin_components_graph_InlineGraphContainer = function InlineGraphContainer() {
    var e = this.getElement();

    console.log('inlinegraphcontainer: registering state change');
    this.onStateChange = function onStateChange() {
      console.log('inlinegraphcontainer: state change triggered', this.getState());
      // Globals
      window.onmsGraphContainers = {
        'baseHref': this.getState().baseHref,
        'engine': this.getState().engine
      };

      GraphContainers.render();
    };
  };

  console.log('init: inline-graphcontainer');
}

module.exports = window.org_opennms_features_vaadin_components_graph_InlineGraphContainer;
