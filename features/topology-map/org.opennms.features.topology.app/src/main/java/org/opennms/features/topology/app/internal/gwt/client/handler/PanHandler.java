package org.opennms.features.topology.app.internal.gwt.client.handler;

import org.opennms.features.topology.app.internal.gwt.client.map.SVGTopologyMap;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.ToggleButton;

public class PanHandler implements DragBehaviorHandler{
    public static String DRAG_BEHAVIOR_KEY = "panHandler";
    private ToggleButton m_toggle;
    protected PanObject m_panObject;
    SVGTopologyMap m_svgTopologyMap;
    
    public PanHandler(SVGTopologyMap topologyMap) {
        m_svgTopologyMap = topologyMap;
    }
    
    @Override
    public void onDragStart(Element elem) {
        m_panObject = new PanObject(m_svgTopologyMap, m_svgTopologyMap.getSVGViewPort(), m_svgTopologyMap.getSVGElement());
    }

    @Override
    public void onDrag(Element elem) {
        m_panObject.move();
    }

    @Override
    public void onDragEnd(Element elem) {
        m_panObject = null;
    }

    @Override
    public ToggleButton getToggleBtn() {
        if(m_toggle == null) {
            m_toggle = new ToggleButton("Pan");
        }
        return m_toggle;
    }

    
}