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
package org.opennms.features.topology.app.internal.gwt.client.view;

import org.opennms.features.topology.app.internal.gwt.client.GWTBoundingBox;
import org.opennms.features.topology.app.internal.gwt.client.VTopologyComponent;
import org.opennms.features.topology.app.internal.gwt.client.VTopologyComponent.GraphUpdateListener;
import org.opennms.features.topology.app.internal.gwt.client.svg.SVGElement;
import org.opennms.features.topology.app.internal.gwt.client.svg.SVGGElement;
import org.opennms.features.topology.app.internal.gwt.client.svg.SVGMatrix;
import org.opennms.features.topology.app.internal.gwt.client.svg.SVGPoint;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Widget;

public interface TopologyView<T extends VTopologyComponent.TopologyViewRenderer> {
    
    public static final int LEFT_MARGIN = 60;
    
    public interface Presenter<T>{
        void addGraphUpdateListener(GraphUpdateListener listener);
        T getViewRenderer();
        void onContextMenu(Object element, int x, int y, String type);
        void onMouseWheel(double newScale, int x, int y);
        void onBackgroundClick();
        void onBackgroundDoubleClick(SVGPoint center);
    }
    
    void setPresenter(Presenter<T> presenter);
    Widget asWidget();
    SVGElement getSVGElement();
    SVGGElement getSVGViewPort();
    Element getEdgeGroup();
    Element getVertexGroup();
    Element getMarqueeElement();
    SVGMatrix calculateNewTransform(GWTBoundingBox bound);
    SVGPoint getCenterPos(GWTBoundingBox gwtBoundingBox);
    int getPhysicalWidth();
    int getPhysicalHeight();
    SVGPoint getPoint(int clientX, int clientY);
}
