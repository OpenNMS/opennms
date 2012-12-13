/**
 * 
 */
package org.opennms.features.topology.plugins.ncs;

import org.opennms.features.topology.api.GraphContainer;


/**
 * This listener responds to events from to {@link TopologyComponent} that
 * indicate that the user has selected a vertex or edge in the graph.
 */
public interface SelectionListener{
    public void onSelectionUpdate(GraphContainer graphContainer);
}
