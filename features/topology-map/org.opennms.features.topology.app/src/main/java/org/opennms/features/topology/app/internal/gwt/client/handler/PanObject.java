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

import org.opennms.features.topology.app.internal.gwt.client.GWTVertex;
import org.opennms.features.topology.app.internal.gwt.client.d3.D3;
import org.opennms.features.topology.app.internal.gwt.client.map.SVGTopologyMap;
import org.opennms.features.topology.app.internal.gwt.client.svg.SVGElement;
import org.opennms.features.topology.app.internal.gwt.client.svg.SVGGElement;
import org.opennms.features.topology.app.internal.gwt.client.svg.SVGMatrix;
import org.opennms.features.topology.app.internal.gwt.client.svg.SVGPoint;
import org.opennms.features.topology.app.internal.gwt.client.svg.SVGRect;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Event;

public class PanObject extends DragObject{
	/**
     * 
     */
    private SVGMatrix m_stateTf;
	private SVGPoint m_stateOrigin;

	public PanObject(SVGTopologyMap svgTopologyMap, Element draggableElement, Element containerElement) {
		super(svgTopologyMap, draggableElement, containerElement, D3.d3().selectAll(GWTVertex.VERTEX_CLASS_NAME));

		SVGGElement g = draggableElement.cast();
		m_stateTf = g.getCTM().inverse();

		m_stateOrigin = getEventPoint(D3.getEvent()).matrixTransform(m_stateTf); 

	}

	@Override
	public void move() {
		Event event = D3.getEvent().cast();
		SVGPoint p = getEventPoint(event).matrixTransform(m_stateTf);

		SVGElement svg = getContainerElement().cast();
		SVGGElement g = getDraggableElement().cast();
		SVGRect gBox = g.getBBox();

		SVGMatrix m = m_stateTf.inverse().translate(p.getX() - m_stateOrigin.getX(), p.getY() - m_stateOrigin.getY() );

		double mapWidth = gBox.getWidth() * m.getA();
		double mapHeight = gBox.getHeight() * m.getA();

		double boundaryX = calculateBoundsX(mapWidth, svg.getOffsetWidth(), m.getE());

		double boundaryY = calculateBoundsY(mapHeight, svg.getOffsetHeight(), m.getF());

		String matrixTransform = "matrix(" + m.getA() +
				", " + m.getB() +
				", " + m.getC() + 
				", " + m.getD() +
				", " + m.getE() + 
				", " + m.getF() + ")";
		
		getDraggableElement().setAttribute("transform", matrixTransform);
//
//		//Updating the reference map
//		//TODO: this needs to be reworked a little its off
//		double viewPortWidth = (getContainerElement().getOffsetWidth() / m.getA()) * 0.4;
//		double viewPortHeight = (getContainerElement().getOffsetHeight() / m.getA()) * 0.4;
//		
//		getSvgTopologyMap().getReferenceViewPort().setAttribute("width", "" + viewPortWidth);
//		getSvgTopologyMap().getReferenceViewPort().setAttribute("height", "" + viewPortHeight);
//		getSvgTopologyMap().getReferenceViewPort().setAttribute("x", "" + (-boundaryX * 0.4));
//		getSvgTopologyMap().getReferenceViewPort().setAttribute("y", "" + (-boundaryY * 0.4));
	}

	private double calculateBoundsY(double mapHeight, int offsetHeight,
			double y) {
		double boundaryY;
		if(mapHeight > offsetHeight) {
			boundaryY = Math.min(0, Math.max(offsetHeight - mapHeight, y));
		}else {
			boundaryY = Math.max(0, Math.min(offsetHeight - mapHeight, y));
		}
		return boundaryY;
	}

	private double calculateBoundsX(double mapWidth, int offsetWidth, double x) {
		double boundaryX;
		if(mapWidth > offsetWidth) {
			return Math.min(0, Math.max(offsetWidth - mapWidth, x));
		}else {
			return Math.max(0, Math.min(offsetWidth - mapWidth, x));
		}
	}
}