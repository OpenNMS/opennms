package org.opennms.features.topology.api.topo;

public interface Edge extends EdgeRef {
	
	String getKey();
	
	public Object getItemId();
	
	public Connector getSource();
	
	public Connector getTarget();
	
	public String getLabel();
	
	public String getTooltipText();

	public String getStyleName();

}
