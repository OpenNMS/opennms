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

if (!window.org_opennms_features_vaadin_components_graph_GraphContainer) {
  window.org_opennms_features_vaadin_components_graph_GraphContainer = function GraphContainer() {
    const e = this.getElement();

    console.log('graphcontainer: registering state change');
    this.onStateChange = function onStateChange() {
      console.log('graphcontainer: state change triggered', this.getState());

      // Globals
      window.onmsGraphContainers = {
          'baseHref': this.getState().baseHref,
          'engine': this.getState().engine
      };

      // Build the div
      const div = document.createElement('div');
      div.setAttribute('class', 'graph-container');
      div.setAttribute('data-resource-id', this.getState().resourceId);
      div.setAttribute('data-graph-name', this.getState().graphName);
      if (this.getState().start !== undefined && this.getState().start !== null) {
        div.setAttribute('data-graph-start', this.getState().start);
      }
      if (this.getState().end !== undefined && this.getState().end !== null) {
        div.setAttribute('data-graph-end', this.getState().end);
      }
      if (this.getState().widthRatio !== undefined && this.getState().widthRatio !== null) {
        div.setAttribute('data-width-ratio', this.getState().widthRatio);
      }
      if (this.getState().heightRatio !== undefined && this.getState().heightRatio !== null) {
        div.setAttribute('data-height-ratio', this.getState().heightRatio);
      }
      if (this.getState().title !== undefined && this.getState().title !== null) {
        div.setAttribute('data-graph-title', this.getState().title);
      }

      // Remove any existing children
      while (e.firstChild) {
          e.removeChild(e.firstChild);
      }

      // Add our div
      e.appendChild(div);

      GraphContainers.render();
    };
  };
  console.log('init: graphcontainer');
}

module.exports = window.org_opennms_features_vaadin_components_graph_GraphContainer;
