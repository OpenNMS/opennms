package org.opennms.features.topology.api.topo;

public interface Vertex extends VertexRef {
	
	String getLabel();
	
	String getToolipText();
	
	String getIconKey();
	
	String getStyleName();
	
}
