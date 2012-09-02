package org.opennms.features.topology.app.internal.gwt.client.handler;

import java.util.ArrayList;
import java.util.List;

import org.opennms.features.topology.app.internal.gwt.client.GWTVertex;
import org.opennms.features.topology.app.internal.gwt.client.d3.D3;
import org.opennms.features.topology.app.internal.gwt.client.d3.D3Events.Handler;
import org.opennms.features.topology.app.internal.gwt.client.map.SVGTopologyMap;
import org.opennms.features.topology.app.internal.gwt.client.svg.ClientRect;
import org.opennms.features.topology.app.internal.gwt.client.svg.SVGElement;
import org.opennms.features.topology.app.internal.gwt.client.svg.SVGRect;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.ToggleButton;

public class MarqueeSelectHandler implements DragBehaviorHandler{

    public class Interval{
        
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
    private ToggleButton m_toggle;
    private SVGTopologyMap m_svgTopologyMap;
    
    public MarqueeSelectHandler(SVGTopologyMap topologyMap) {
        m_svgTopologyMap = topologyMap;
    }
    
    @Override
    public void onDragStart(Element elem) {
        if(!m_dragging) {
            m_dragging = true;
            
            SVGElement svg = m_svgTopologyMap.getSVGElement();
            ClientRect rect = svg.getBoundingClientRect();
            m_offsetX = rect.getLeft();
            m_offsetY = rect.getTop();
            
            m_x1 = D3.getEvent().getClientX() - m_offsetX;
            m_y1 = D3.getEvent().getClientY() - m_offsetY;
            
            setMarquee(m_x1, m_y1, 0, 0);
            D3.d3().select(m_svgTopologyMap.getMarqueeElement()).attr("display", "inline");
        }
    }

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
        D3.d3().select(m_svgTopologyMap.getMarqueeElement()).attr("display", "none");
        
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
        D3.d3().select(m_svgTopologyMap.getMarqueeElement()).attr("x", x).attr("y", y).attr("width", width).attr("height", height);
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
                    D3.d3().select(elem).style("stroke", "blue");
                }else {
                    vertex.setSelected(false);
                    D3.d3().select(elem).style("stroke", "none");
                }
                
            }
        });
        
    }

    private boolean inSelection(SVGElement elem) {
        SVGElement marquee = m_svgTopologyMap.getMarqueeElement().cast();
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

    @Override
    public ToggleButton getToggleBtn() {
        if(m_toggle == null) {
            m_toggle = new ToggleButton("Select", "Select");
        }
        return m_toggle;
    }

    
}