package org.opennms.features.vaadin.topology;

import java.util.List;

public interface TopologyContainer {

	public Graph getGraph(int level);

	public void createGroup(List<Vertex> verticesToGroup);

}
