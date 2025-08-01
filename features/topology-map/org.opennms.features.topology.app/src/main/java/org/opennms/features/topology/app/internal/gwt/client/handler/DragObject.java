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