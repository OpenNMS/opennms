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
