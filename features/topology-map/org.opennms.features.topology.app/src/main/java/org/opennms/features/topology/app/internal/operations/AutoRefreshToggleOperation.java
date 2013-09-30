package org.opennms.features.topology.app.internal.operations;

import org.opennms.features.topology.api.*;
import org.opennms.features.topology.api.topo.VertexRef;

import java.util.List;
import java.util.Map;

public class AutoRefreshToggleOperation extends AbstractCheckedOperation {

    @Override
    public boolean display(List<VertexRef> targets, OperationContext operationContext) {
        return operationContext.getGraphContainer().hasAutoRefreshSupport();
    }

    @Override
    protected boolean enabled(GraphContainer container) {
        return true;
    }

    @Override
    public String getId() {
        return getClass().getSimpleName();
    }

    @Override
    protected boolean isChecked(GraphContainer container) {
        if (container.hasAutoRefreshSupport()) {
            return container.getAutoRefreshSupport().isEnabled();
        }
        return false;
    }

    @Override
    public Undoer execute(final List<VertexRef> targets, final OperationContext operationContext) {
       return execute(operationContext.getGraphContainer());
    }

    @Override
    public void applyHistory(GraphContainer container, Map<String, String> settings) {
        if (container.getAutoRefreshSupport() != null) {
            String autoRefreshEnabledString = settings.get(getClass().getName());
            boolean autoRefreshEnabled = Boolean.valueOf(autoRefreshEnabledString);
            container.getAutoRefreshSupport().setEnabled(autoRefreshEnabled);
        }
        execute(container);
    }

    private Undoer execute(final GraphContainer container) {
        if (container.hasAutoRefreshSupport()) {
            container.getAutoRefreshSupport().toggle();
        }

        return new Undoer() {
            @Override
            public void undo(OperationContext operationContext) {
                execute(operationContext.getGraphContainer()); // toggle again
            }
        };
    }
}
