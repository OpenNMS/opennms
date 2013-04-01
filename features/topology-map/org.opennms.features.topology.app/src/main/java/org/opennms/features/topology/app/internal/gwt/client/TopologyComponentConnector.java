package org.opennms.features.topology.app.internal.gwt.client;

import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.AbstractComponentConnector;
import com.vaadin.shared.ui.Connect;

@Connect(org.opennms.features.topology.app.internal.TopologyComponent.class)
public class TopologyComponentConnector extends AbstractComponentConnector{

   @Override
   public VTopologyComponent getWidget() {
       return (VTopologyComponent) super.getWidget();
   }
   
   @Override
   public TopologyComponentState getState() {
       return (TopologyComponentState) super.getState();
   }
   
   @Override
   public void onStateChanged(StateChangeEvent event) {
       super.onStateChanged(event);
       
       
   }

}
