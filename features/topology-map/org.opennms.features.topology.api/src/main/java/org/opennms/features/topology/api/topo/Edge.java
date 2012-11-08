package org.opennms.features.topology.api.topo;

public interface Edge extends EdgeRef {
	
	public ConnectorRef getSource();
	
	public ConnectorRef getTarget();
	
	public String getLabel();
	
	public String getTooltipText();

	public String getStyleName();

}
