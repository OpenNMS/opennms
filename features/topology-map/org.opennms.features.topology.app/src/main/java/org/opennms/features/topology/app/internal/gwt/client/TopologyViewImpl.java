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
