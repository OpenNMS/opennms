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
import org.opennms.features.topology.app.internal.gwt.client.VTopologyComponent.TopologyViewRenderer;
import org.opennms.features.topology.app.internal.gwt.client.d3.D3;
import org.opennms.features.topology.app.internal.gwt.client.svg.SVGGElement;
import org.opennms.features.topology.app.internal.gwt.client.svg.SVGMatrix;
import org.opennms.features.topology.app.internal.gwt.client.svg.SVGPoint;
import org.opennms.features.topology.app.internal.gwt.client.view.TopologyView;

import com.google.gwt.user.client.Event;

public class PanObject extends DragObject{
	/**
     * 
     */
    private SVGMatrix m_stateTf;
	private SVGPoint m_stateOrigin;

	public PanObject(TopologyView<TopologyViewRenderer> topologyView) {
		super(topologyView, topologyView.getSVGViewPort(), topologyView.getSVGElement(), D3.d3().selectAll(GWTVertex.VERTEX_CLASS_NAME));

		SVGGElement g = topologyView.getSVGViewPort().cast();
		m_stateTf = g.getCTM().inverse();

		SVGPoint eventPoint = getEventPoint(D3.getEvent());
        m_stateOrigin = topologyView.getPoint((int)eventPoint.getX(), (int)eventPoint.getY()); 
	}

	@Override
	public void move() {
		Event event = D3.getEvent().cast();
		SVGPoint eventPoint = getEventPoint(event);
        SVGPoint p = eventPoint.matrixTransform(m_stateTf);

		SVGMatrix m = m_stateTf.inverse().translate(p.getX() - m_stateOrigin.getX(), p.getY() - m_stateOrigin.getY() );

		String matrixTransform = "matrix(" + m.getA() +
				", " + m.getB() +
				", " + m.getC() + 
				", " + m.getD() +
				", " + m.getE() + 
				", " + m.getF() + ")";
		getDraggableElement().setAttribute("transform", matrixTransform);
	}
	
}
