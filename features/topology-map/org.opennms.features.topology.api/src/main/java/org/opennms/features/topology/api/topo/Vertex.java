package org.opennms.features.topology.api.topo;

import com.vaadin.data.Item;

public interface Vertex extends VertexRef {
	
	String getKey();
	
	Object getItemId();
	
	Item getItem();
	
	String getLabel();
	
	String getTooltipText();
	
	String getIconKey();
	
	String getStyleName();
	
}
