package org.opennms.features.vaadin.nodemaps.internal;

import java.util.List;

import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.topo.VertexRef;

public class NodeMapQueryLink implements Operation {

    @Override
    public String getId() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public Undoer execute(final List<VertexRef> targets, final OperationContext operationContext) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public boolean display(final List<VertexRef> targets, final OperationContext operationContext) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public boolean enabled(final List<VertexRef> targets, final OperationContext operationContext) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

}
