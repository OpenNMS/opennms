package org.opennms.features.topology.api.topo;

import com.vaadin.data.Item;

public interface Edge extends EdgeRef {
	
	String getKey();
	
	public Item getItem();
	
	public Connector getSource();
	
	public Connector getTarget();
	
	public String getLabel();
	
	public String getTooltipText();

	public String getStyleName();

}
