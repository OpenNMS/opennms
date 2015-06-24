/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
    private boolean m_dragging = false;
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
        if(!m_dragging) {
            m_dragging = true;
            
            SVGElement svg = m_topologyView.getSVGElement().cast();
            SVGMatrix rect = svg.getScreenCTM();
            
            m_offsetX = (int) rect.getE();
            m_offsetY = (int) rect.getF();
            consoleLog(rect);
            consoleLog("m_offsetX: " + m_offsetX + " m_offsetY: " + m_offsetY);
            m_x1 = D3.getEvent().getClientX() - m_offsetX;
            m_y1 = D3.getEvent().getClientY() - m_offsetY;
            
            setMarquee(m_x1, m_y1, 0, 0);
            D3.d3().select(m_topologyView.getMarqueeElement()).attr("display", "inline");
        }
    }
    
    public final native void consoleLog(Object log)/*-{
        $wnd.console.log(log);
    }-*/;

    @Override
    public void onDrag(Element elem) {
        if(m_dragging) {
            int clientX = D3.getEvent().getClientX() - m_offsetX;
            int clientY = D3.getEvent().getClientY() - m_offsetY;
            setMarquee(
                Math.min(m_x1, clientX), Math.min(m_y1, clientY), 
                Math.abs(m_x1 - clientX), Math.abs(m_y1 - clientY)
            );
            selectVertices();
        }
    }

    @Override
    public void onDragEnd(Element elem) {
        m_dragging = false;
        D3.d3().select(m_topologyView.getMarqueeElement()).attr("display", "none");
        
        final List<String> vertIds = new ArrayList<String>();
        m_svgTopologyMap.selectAllVertexElements().each(new Handler<GWTVertex>() {

            @Override
            public void call(GWTVertex vert, int index) {
                if(vert.isSelected()) {
                    vertIds.add(vert.getId());
                }
            }
        });
        
        m_svgTopologyMap.setVertexSelection(vertIds);
    }
    
    private void setMarquee(int x, int y, int width, int height) {
        D3.d3().select(m_topologyView.getMarqueeElement()).attr("x", x).attr("y", y).attr("width", width).attr("height", height);
    }
    
    private void selectVertices() {
        D3 vertices = m_svgTopologyMap.selectAllVertexElements();
        JsArray<JsArray<SVGElement>> selection = vertices.cast();
        
        final JsArray<SVGElement> elemArray = selection.get(0);
        
        vertices.each(new Handler<GWTVertex>() {

            @Override
            public void call(GWTVertex vertex, int index) {
                SVGElement elem = elemArray.get(index).cast();
                
                if(inSelection(elem)) {
                    vertex.setSelected(true);
                }else {
                    vertex.setSelected(false);
                }
                
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