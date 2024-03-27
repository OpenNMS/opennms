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
package org.opennms.features.topology.plugins.topo.graphml;

public interface GraphMLProperties {
    String ID = "id";
    String DESCRIPTION = "description";
    String NAMESPACE = "namespace";
    String ICON_KEY = "iconKey";
    String IP_ADDRESS = "ipAddr";
    String LABEL = "label";
    String LOCKED = "locked";
    String NODE_ID = "nodeID";
    String FOREIGN_SOURCE = "foreignSource";
    String FOREIGN_ID = "foreignID";
    String SELECTED = "selected";
    String STYLE_NAME = "styleName";
    String TOOLTIP_TEXT = "tooltipText";
    String X = "x";
    String Y = "y";
    String PREFERRED_LAYOUT = "preferred-layout";
    String FOCUS_STRATEGY = "focus-strategy";
    String FOCUS_IDS = "focus-ids";
    String SEMANTIC_ZOOM_LEVEL = "semantic-zoom-level";
    String VERTEX_STATUS_PROVIDER = "vertex-status-provider";
    String LEVEL = "level";
    String EDGE_PATH_OFFSET = "edge-path-offset";
    String BREADCRUMB_STRATEGY = "breadcrumb-strategy";
}
