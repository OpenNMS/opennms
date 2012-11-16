package org.opennms.features.topology.api.topo;

public interface Vertex extends VertexRef {
	
	String getKey();
	
	Object getItemId();
	
	String getLabel();
	
	String getTooltipText();
	
	String getIconKey();
	
	String getStyleName();
	
}
