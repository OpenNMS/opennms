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
package org.opennms.features.topology.app.internal.gwt.client.handler;

import java.util.Objects;

import org.opennms.features.topology.app.internal.gwt.client.VTopologyComponent;
import org.opennms.features.topology.app.internal.gwt.client.d3.D3;

import com.google.gwt.dom.client.Element;

public class PanHandler implements DragBehaviorHandler{
    public static String DRAG_BEHAVIOR_KEY = "panHandler";
    protected PanObject m_panObject;
    VTopologyComponent m_topologyComponent;
    
    public PanHandler(VTopologyComponent vTopologyComponent) {
        m_topologyComponent = Objects.requireNonNull(vTopologyComponent);
    }

    @Override
    public void onDragStart(Element elem) {
        m_panObject = new PanObject(m_topologyComponent.getTopologyView());
        D3.getEvent().stopPropagation();
        D3.getEvent().preventDefault();
    }

    @Override
    public void onDrag(Element elem) {
        m_panObject.move();
        D3.getEvent().stopPropagation();
        D3.getEvent().preventDefault();
    }

    @Override
    public void onDragEnd(Element elem) {
        m_panObject = null;
        m_topologyComponent.updateMapPosition();
        D3.getEvent().stopPropagation();
        D3.getEvent().preventDefault();
    }
}