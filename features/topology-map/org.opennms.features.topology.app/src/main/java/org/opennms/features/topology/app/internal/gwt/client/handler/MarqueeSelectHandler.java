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

import java.util.ArrayList;
import java.util.List;

import org.opennms.features.topology.app.internal.gwt.client.GWTVertex;
import org.opennms.features.topology.app.internal.gwt.client.VTopologyComponent.TopologyViewRenderer;
import org.opennms.features.topology.app.internal.gwt.client.d3.D3;
import org.opennms.features.topology.app.internal.gwt.client.d3.D3Events.Handler;
import org.opennms.features.topology.app.internal.gwt.client.map.SVGTopologyMap;
import org.opennms.features.topology.app.internal.gwt.client.svg.ClientRect;
import org.opennms.features.topology.app.internal.gwt.client.svg.SVGElement;
import org.opennms.features.topology.app.internal.gwt.client.svg.SVGMatrix;
import org.opennms.features.topology.app.internal.gwt.client.svg.SVGRect;
import org.opennms.features.topology.app.internal.gwt.client.view.TopologyView;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Element;

public class MarqueeSelectHandler implements DragBehaviorHandler{

    public static class Interval{
        
        private int m_lo;
        public int getLo() {
            return m_lo;
        }
    
        private int m_hi;
    
        public int getHi() {
            return m_hi;
        }
    
        public Interval(int lo, int hi) {
            m_lo = Math.min(lo, hi);
            m_hi = Math.max(lo, hi);
        }
        
        public boolean contains(int value) {
            return m_lo <= value && value <= m_hi;
        }
    }

    public static String DRAG_BEHAVIOR_KEY = "marqueeHandler";
    private int m_x1;
    private int m_y1;
    private int m_offsetX;
    private int m_offsetY;
    private SVGTopologyMap m_svgTopologyMap;
    private TopologyView<TopologyViewRenderer> m_topologyView;
    
    public MarqueeSelectHandler(SVGTopologyMap topologyMap, TopologyView<TopologyViewRenderer> topologyView) {
        m_svgTopologyMap = topologyMap;
        m_topologyView = topologyView;
    }
    
    @Override
    public void onDragStart(Element elem) {
        SVGElement svg = m_topologyView.getSVGElement().cast();
        SVGMatrix rect = svg.getScreenCTM();

        m_offsetX = (int) rect.getE();
        m_offsetY = (int) rect.getF();
        consoleDebug(rect);
        consoleDebug("m_offsetX: " + m_offsetX + " m_offsetY: " + m_offsetY);
        m_x1 = D3.getEvent().getClientX() - m_offsetX;
        m_y1 = D3.getEvent().getClientY() - m_offsetY;

        setMarquee(m_x1, m_y1, 0, 0);
        D3.d3().select(m_topologyView.getMarqueeElement()).attr("display", "inline");

        D3.getEvent().stopPropagation();
        D3.getEvent().preventDefault();
    }
    
    public final native void consoleDebug(Object log)/*-{
        $wnd.console.log(log);
    }-*/;

    @Override
    public void onDrag(Element elem) {
        int clientX = D3.getEvent().getClientX() - m_offsetX;
        int clientY = D3.getEvent().getClientY() - m_offsetY;
        setMarquee(
            Math.min(m_x1, clientX), Math.min(m_y1, clientY),
            Math.abs(m_x1 - clientX), Math.abs(m_y1 - clientY)
        );
        selectVertices();

        D3.getEvent().stopPropagation();
        D3.getEvent().preventDefault();
    }

    @Override
    public void onDragEnd(Element elem) {
        setMarqueeVisible(false);

        final List<String> vertIds = new ArrayList<>();
        m_svgTopologyMap.selectAllVertexElements().each(new Handler<GWTVertex>() {

            @Override
            public void call(GWTVertex vert, int index) {
                if (vert.isSelected()) {
                    vertIds.add(vert.getId());
                }
            }
        });
        
        m_svgTopologyMap.setVertexSelection(vertIds);

        D3.getEvent().stopPropagation();
        D3.getEvent().preventDefault();
    }
    
    private void setMarquee(int x, int y, int width, int height) {
        D3.d3().select(m_topologyView.getMarqueeElement()).attr("x", x).attr("y", y).attr("width", width).attr("height", height);
    }

    private void setMarqueeVisible(boolean visible) {
        D3.d3().select(m_topologyView.getMarqueeElement()).attr("display", visible ? "inline" : "none");
    }

    private void selectVertices() {
        // We use the svgIconOverlay to determine the selection and ignore the status border, the label
        // and possible invisible elements, such as status badge, navigate to target etc, as the vertex is
        // slightly greater than the svgIconOverlay and the user probably uses the icon itself to make the selection.
        D3 iconOverlay = m_svgTopologyMap.selectAllVertexElements().select(".svgIconOverlay");

        JsArray<JsArray<SVGElement>> selection = iconOverlay.cast();
        final JsArray<SVGElement> elemArray = selection.get(0);
        iconOverlay.each(new Handler<GWTVertex>() {

            @Override
            public void call(GWTVertex vertex, int index) {
                SVGElement elem = elemArray.get(index).cast();
                boolean selected = inSelection(elem);
                vertex.setSelected(selected);
            }
        });
    }

    private boolean inSelection(SVGElement elem) {
        SVGElement marquee = m_topologyView.getMarqueeElement().cast();
        SVGRect mBBox = marquee.getBBox();
        ClientRect elemClientRect = elem.getBoundingClientRect();
        
        Interval marqueeX = new Interval(mBBox.getX(), mBBox.getX() + mBBox.getWidth());
        Interval marqueeY = new Interval(mBBox.getY(), mBBox.getY() + mBBox.getHeight());
        
        int left = elemClientRect.getLeft() - m_offsetX;
        int top = elemClientRect.getTop() - m_offsetY;
        Interval vertexX = new Interval(left, left + elemClientRect.getWidth());
        Interval vertexY = new Interval(top, top + elemClientRect.getHeight());
        
        return marqueeX.contains(vertexX.getLo()) &&
               marqueeX.contains(vertexX.getHi()) &&
               marqueeY.contains(vertexY.getLo()) &&
               marqueeY.contains(vertexY.getHi());
    }

}