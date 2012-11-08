/**
 * 
 */
package org.opennms.features.topology.api.support;

import com.vaadin.data.Container;

/**
 * This listener responds to events from to {@link TopologyComponent} that
 * indicate that the user has selected a vertex or edge in the graph.
 */
public interface SelectionListener{
    public void onSelectionUpdate(Container container);
}
