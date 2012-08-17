package org.opennms.features.topology.app.internal.gwt.client.handler;

import org.opennms.features.topology.app.internal.gwt.client.d3.D3;
import org.opennms.features.topology.app.internal.gwt.client.d3.D3Transform;
import org.opennms.features.topology.app.internal.gwt.client.map.SVGTopologyMap;
import org.opennms.features.topology.app.internal.gwt.client.svg.SVGElement;
import org.opennms.features.topology.app.internal.gwt.client.svg.SVGPoint;

import com.google.gwt.core.client.JsArrayInteger;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.user.client.Window;

public class DragObject{
	/**
     * 
     */
    private final SVGTopologyMap m_svgTopologyMap;
    private Element m_containerElement;
	private Element m_draggableElement;
	private int m_startX;
	private int m_startY;
	private D3Transform m_transform;

	public DragObject(SVGTopologyMap svgTopologyMap, Element draggableElement, Element containerElement) {

		m_svgTopologyMap = svgTopologyMap;
        m_draggableElement = draggableElement;
		m_containerElement = containerElement;

		//User m_vertexgroup because this is what we scale instead of every vertex element
		m_transform = D3.getTransform(D3.d3().select(getSvgTopologyMap().getVertexGroup()).attr("transform"));
		
		if(containerElement == null) {
		    Window.alert("element: " + containerElement + " is null");
		}
		
		JsArrayInteger position = D3.getMouse(containerElement);
		m_startX = (int) (position.get(0) / m_transform.getScale().get(0));
		m_startY = (int) (position.get(1) / m_transform.getScale().get(1));
	}

	public Element getContainerElement() {
		return m_containerElement;
	}

	public Element getDraggableElement() {
		return m_draggableElement;
	}

	public int getCurrentX() {
		JsArrayInteger position = D3.getMouse(m_containerElement);
		return (int) (position.get(0) / m_transform.getScale().get(0));
	}

	public int getCurrentY() {
		JsArrayInteger position = D3.getMouse(m_containerElement);
		return (int) (position.get(1) / m_transform.getScale().get(1));
	}

	public int getStartX() {
		return m_startX;
	}

	public int getStartY() {
		return m_startY;
	}

	public void move() {
		getSvgTopologyMap().repaintNow();
	}

	protected SVGPoint getEventPoint(NativeEvent event) {
		SVGElement svg = getSvgTopologyMap().getSVGElement();
		SVGPoint p = svg.createSVGPoint();
		p.setX(event.getClientX());
		p.setY(event.getClientY());
		return p;
	}

    public SVGTopologyMap getSvgTopologyMap() {
        return m_svgTopologyMap;
    }

}