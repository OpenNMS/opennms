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
package org.opennms.features.topology.app.internal.gwt.client;

import java.util.List;

import com.vaadin.shared.MouseEventDetails;
import com.vaadin.shared.communication.ServerRpc;

public interface TopologyComponentServerRpc extends ServerRpc {
    
    void doubleClicked(MouseEventDetails eventDetails);
    void deselectAllItems();
    void edgeClicked(String edgeId);
    void backgroundClicked();
    void scrollWheel(double scrollVal, int x, int y);
    void mapPhysicalBounds(int width, int height);
    void marqueeSelection(String[] vertexIds, MouseEventDetails eventDetails);
    void contextMenu(String target, String type, int x, int y);
    void clientCenterPoint(int x, int y);
    void vertexClicked(String vertexId, MouseEventDetails eventDetails, String platform);
    void updateVertices(List<String> vertices);
    void backgroundDoubleClick(double x, double y);
}
