package org.opennms.features.topology.app.internal.gwt.client;

import com.vaadin.client.communication.RpcProxy;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.AbstractComponentConnector;
import com.vaadin.shared.ui.Connect;

@Connect(org.opennms.features.topology.app.internal.TopologyComponent.class)
public class TopologyComponentConnector extends AbstractComponentConnector{

   TopologyComponentServerRpc m_rpc = RpcProxy.create(TopologyComponentServerRpc.class, this);
   @Override
   public VTopologyComponent getWidget() {
       VTopologyComponent widget = (VTopologyComponent) super.getWidget();
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
