package org.opennms.features.topology.api.topo;

public interface Edge extends EdgeRef {
	
	public Connector getSource();
	
	public Connector getTarget();
	
	public String getLabel();
	
	public String getTooltipText();

	public String getStyleName();

}
