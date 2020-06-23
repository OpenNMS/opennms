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

package org.opennms.features.topology.app.internal.gwt.client;

import org.opennms.features.topology.app.internal.gwt.client.VTopologyComponent.GraphUpdateListener;
import org.opennms.features.topology.app.internal.gwt.client.VTopologyComponent.TopologyViewRenderer;
import org.opennms.features.topology.app.internal.gwt.client.svg.SVGElement;
import org.opennms.features.topology.app.internal.gwt.client.svg.SVGGElement;
import org.opennms.features.topology.app.internal.gwt.client.svg.SVGMatrix;
import org.opennms.features.topology.app.internal.gwt.client.svg.SVGPoint;
import org.opennms.features.topology.app.internal.gwt.client.view.TopologyView;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;

public class TopologyViewImpl extends Composite implements TopologyView<TopologyViewRenderer>, GraphUpdateListener {

    private static TopologyViewImplUiBinder uiBinder = GWT.create(TopologyViewImplUiBinder.class);

    interface TopologyViewImplUiBinder extends
            UiBinder<Widget, TopologyViewImpl> {
    }
    
    private Presenter<TopologyViewRenderer> m_presenter;
    
    @UiField
    Element m_svg;

    @UiField
    Element m_svgViewPort;

    @UiField
    Element m_edgeGroup;

    @UiField
    Element m_vertexGroup;

    @UiField
    Element m_referenceMap;

    @UiField
    Element m_referenceMapViewport;

    @UiField
    Element m_referenceMapBorder;
    
    @UiField
    Element m_marquee;
    
    @UiField
    Element m_marginContainer;

    @UiField
    HTMLPanel m_widgetContainer;
    
    public int getLeftMargin() {
        return LEFT_MARGIN;
    }

    public TopologyViewImpl() {
        initWidget(uiBinder.createAndBindUi(this));
    }
    
    @Override
    protected void onLoad() {
        super.onLoad();
        m_widgetContainer.setSize("100%", "100%");
        m_svg.setId("TopologyComponent");
    }

    @Override
    public void setPresenter(Presenter<TopologyViewRenderer> presenter) {
        m_presenter = presenter;
        m_presenter.addGraphUpdateListener(this);
    }

    @Override
    public SVGElement getSVGElement() {
        return m_svg.cast();
    }
    
    @Override
    public SVGGElement getSVGViewPort() {
        return m_svgViewPort.cast();
    }

    @Override
    public Element getEdgeGroup() {
        return m_edgeGroup;
    }

    @Override
    public Element getVertexGroup() {
        return m_vertexGroup;
    }

    @Override
    public Element getMarqueeElement() {
        return m_marquee;
    }

    @Override
    public void onGraphUpdated(GWTGraph graph, GWTBoundingBox oldBBox) {
        if(m_presenter.getViewRenderer() != null){
            m_presenter.getViewRenderer().draw(graph, this, oldBBox);
        }
    }
    
    @Override
    public SVGMatrix calculateNewTransform(GWTBoundingBox bounds) {
        int iconMargin = 50;
        int iconLeftMargin = iconMargin + 50;
        int topMargin = iconMargin + 50;
        
        SVGElement svg = getSVGElement().cast();
        final int svgWidth = getPhysicalWidth(); 
        final int svgHeight = getPhysicalHeight();
        
        double scale = Math.min(svgWidth/((double)bounds.getWidth() + iconLeftMargin), svgHeight/((double)bounds.getHeight() + topMargin));
        scale = scale > 2 ? 2 : scale;
        double translateX =  -bounds.getX();
        double translateY =  -bounds.getY();
        
        double calcY = (svgHeight - (bounds.getHeight()* scale))/2;
        double calcX = (svgWidth - ((bounds.getWidth()) * scale))/2 + getLeftMargin();
        SVGMatrix transform = svg.createSVGMatrix()
                .translate(calcX, calcY)
                .scale(scale)
                .translate(translateX, translateY)
                    ;
        return transform;
    }
    
    @Override
    public SVGPoint getCenterPos(GWTBoundingBox box) {
        SVGGElement g = getSVGViewPort().cast();
        SVGMatrix stateTF = g.getCTM().inverse();
        
        SVGPoint p = getSVGElement().createSVGPoint();
        p.setX(getPhysicalWidth()/2 + getLeftMargin());
        p.setY(getPhysicalHeight()/2);
        
        SVGPoint center = p.matrixTransform(stateTF);
        
        return center;
    }
    
    @Override
    public SVGPoint getPoint(int clientX, int clientY) {
        SVGGElement g = getSVGViewPort().cast();
        SVGMatrix stateTF = g.getCTM().inverse();
        
        SVGPoint p = getSVGElement().createSVGPoint();
        
        p.setX(clientX + getLeftMargin());
        p.setY(clientY);
        
        SVGPoint center = p.matrixTransform(stateTF);
        
        return center;
    }

    @Override
    public int getPhysicalWidth() {
        return getSVGElement().getParentElement().getOffsetWidth() - getLeftMargin();
    }

    @Override
    public int getPhysicalHeight() {
        return getSVGElement().getParentElement().getOffsetHeight();
    }


}
