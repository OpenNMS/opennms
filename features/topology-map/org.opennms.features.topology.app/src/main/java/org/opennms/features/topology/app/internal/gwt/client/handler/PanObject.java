package org.opennms.features.topology.app.internal.gwt.client.handler;

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
		super(svgTopologyMap, draggableElement, containerElement);

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
				", " + boundaryX + 
				", " + boundaryY + ")";
		
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