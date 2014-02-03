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
