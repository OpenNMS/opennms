package org.opennms.features.topology.app.internal.gwt.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.communication.RpcProxy;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.AbstractComponentConnector;
import com.vaadin.shared.ui.Connect;

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
       
       getWidget().updateGraph(getConnection(), getState());

   }

}
