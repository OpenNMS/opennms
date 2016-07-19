/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.TooltipInfo;
import com.vaadin.client.communication.RpcProxy;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.AbstractComponentConnector;
import com.vaadin.shared.ui.Connect;
import org.opennms.features.topology.app.internal.gwt.client.d3.D3;

@Connect(org.opennms.features.topology.app.internal.TopologyComponent.class)
public class TopologyComponentConnector extends AbstractComponentConnector{

   TopologyComponentServerRpc m_rpc = RpcProxy.create(TopologyComponentServerRpc.class, this);

   @Override
   public VTopologyComponent getWidget() {
       return (VTopologyComponent) super.getWidget();
   }

    @Override
    protected Widget createWidget() {
        VTopologyComponent widget = GWT.create(VTopologyComponent.class);
        widget.setComponentServerRpc(m_rpc);
        return widget;
    }

    @Override
   public TopologyComponentState getState() {
       return (TopologyComponentState) super.getState();
   }
   
   @Override
   public void onStateChanged(StateChangeEvent event) {
       super.onStateChanged(event);

       if (event.hasPropertyChanged("SVGDefFiles")) {
           getWidget().injectSVGDefs(getConnection(), getState().getSVGDefFiles());
       }

       if(event.hasPropertyChanged("activeTool")){
           getWidget().setActiveTool(getState().getActiveTool());
       }

       if (event.hasPropertyChanged("physicalWidth")) {
           getWidget().setPhysicalWidth(getState().getPhysicalWidth());
       }

       if (event.hasPropertyChanged("physicalHeight")) {
           getWidget().setPhysicalHeight(getState().getPhysicalHeight());
       }

       getWidget().updateGraph(getConnection(), getState());

   }

    @Override
    public TooltipInfo getTooltipInfo(Element element) {
        TooltipInfo tooltipInfo = null;

        String className = getElementClassName(element);
        if (className == null) {
           // Don't try and find a tooltip
        } else if(className.equals("svgIconOverlay")) {
            GWTVertex vertex = getVertexForSelection(D3.d3().select(element));
            tooltipInfo = new TooltipInfo(vertex.getTooltipText());
        } else if(className.contains("edge")){
            GWTEdge edge = getEdgeForSelection(D3.d3().select(element));
            tooltipInfo = new TooltipInfo(edge.getTooltipText());
        }

        return tooltipInfo;
    }

    private final native GWTEdge getEdgeForSelection(D3 selection) /*-{
        return selection.data()[0];
    }-*/;

    private final native String getElementClassName(Element element)/*-{
        return element.className.baseVal;
    }-*/;

    private final native GWTVertex getVertexForSelection(D3 selection) /*-{
        return selection.data()[0];
    }-*/;

    @Override
    public boolean hasTooltip() {
        return true;
    }

}
