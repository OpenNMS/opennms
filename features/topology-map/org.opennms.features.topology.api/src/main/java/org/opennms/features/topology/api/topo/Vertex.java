package org.opennms.features.topology.api.topo;

public interface Vertex extends VertexRef {
	
	String getLabel();
	
	void setLabel(String label);
	
	String getTooltipText();
	
	String getIconKey();
	
	String getStyleName();
	
}
