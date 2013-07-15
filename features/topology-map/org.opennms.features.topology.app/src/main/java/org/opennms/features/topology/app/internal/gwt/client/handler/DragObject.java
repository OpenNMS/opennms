/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.topology.app.internal.gwt.client.handler;

import java.util.HashMap;
import java.util.Map;

import org.opennms.features.topology.app.internal.gwt.client.GWTVertex;
import org.opennms.features.topology.app.internal.gwt.client.VTopologyComponent.TopologyViewRenderer;
import org.opennms.features.topology.app.internal.gwt.client.d3.D3;
import org.opennms.features.topology.app.internal.gwt.client.d3.D3Events.Handler;
import org.opennms.features.topology.app.internal.gwt.client.d3.D3Transform;
import org.opennms.features.topology.app.internal.gwt.client.svg.SVGElement;
import org.opennms.features.topology.app.internal.gwt.client.svg.SVGPoint;
import org.opennms.features.topology.app.internal.gwt.client.view.TopologyView;

import com.google.gwt.core.client.JsArrayInteger;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.touch.client.Point;

public class DragObject{
    
	/**
     * 
     */
    private final TopologyView<TopologyViewRenderer> m_svgTopologyMap;
    private Element m_containerElement;
	private Element m_draggableElement;
	private int m_startX;
	private int m_startY;
	private D3Transform m_transform;
	private D3 m_selection;
	private Map<String, Point> m_startPosition = new HashMap<String, Point>();

	public DragObject(TopologyView<TopologyViewRenderer> svgTopologyMap, Element draggableElement, Element containerElement, D3 selection) {
	    
		m_svgTopologyMap = svgTopologyMap;
        m_draggableElement = draggableElement;
		m_containerElement = containerElement;
		m_selection = selection;
		
		m_selection.each(new Handler<GWTVertex>() {

            @Override
            public void call(GWTVertex vertex, int index) {
                Point p = new Point(vertex.getX(), vertex.getY());
                m_startPosition.put(vertex.getId(), p);
            }
        });
		
		//User m_vertexgroup because this is what we scale instead of every vertex element
		m_transform = D3.getTransform(D3.d3().select(getTopologyView().getVertexGroup()).attr("transform"));
		
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
	    
	    final int deltaX = getCurrentX() - getStartX();
	    final int deltaY = getCurrentY() - getStartY();

	    m_selection.each(new Handler<GWTVertex>() {

            @Override
            public void call(GWTVertex vertex, int index) {
                if(m_startPosition.containsKey(vertex.getId())) {
                    Point p = m_startPosition.get(vertex.getId());
                    
                    vertex.setX( (int) (p.getX() + deltaX) );
                    vertex.setY( (int) (p.getY() + deltaY));
                }
            }
        });
	    
	}
	
	public final native void consoleLog(Object object) /*-{
	    $wnd.console.log(object);
	}-*/;

	protected SVGPoint getEventPoint(NativeEvent event) {
		SVGElement svg = getTopologyView().getSVGElement().cast();
		SVGPoint p = svg.createSVGPoint();
		p.setX(event.getClientX());
		p.setY(event.getClientY());
		return p;
	}

    public TopologyView<TopologyViewRenderer> getTopologyView() {
        return m_svgTopologyMap;
    }

    public String[] getDraggedVertices() {
        return m_startPosition.keySet().toArray(new String[] {});
    }

}