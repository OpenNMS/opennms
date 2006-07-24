package org.opennms.web.svclayer;

import java.util.Collection;

import org.opennms.secret.model.GraphDefinition;

public interface PerfGraphBuilderService {
	
	GraphDefinition	createGraphDefinition();
	
	void saveGraphDefinition(String graphDefId);
	
	GraphDefinition getGraphDefinition(String graphDefId);
	
	void addAttributeToGraphDefinition(String attributeId, String graphDefId);
	
	Palette getAttributePalette(int nodeId); 

	byte[] getGraph(String graphDefId);
	
}
