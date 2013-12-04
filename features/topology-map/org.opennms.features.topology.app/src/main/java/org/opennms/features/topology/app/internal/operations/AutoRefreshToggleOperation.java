package org.opennms.features.topology.app.internal.operations;

import org.opennms.features.topology.api.*;
import org.opennms.features.topology.api.topo.VertexRef;

import java.util.Collections;
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
       return toggle(operationContext.getGraphContainer());
    }

    @Override
    public Map<String, String> createHistory(GraphContainer container){
        return Collections.singletonMap(getClass().getName(), Boolean.toString(isChecked(container)));
    }

    @Override
    public void applyHistory(GraphContainer container, Map<String, String> settings) {
        if (container.hasAutoRefreshSupport()) {
            boolean autoRefreshEnabled = Boolean.TRUE.toString().equals(settings.get(getClass().getName()));
            container.getAutoRefreshSupport().setEnabled(autoRefreshEnabled);
        }
    }

    private static Undoer toggle(final GraphContainer container) {
        if (container.hasAutoRefreshSupport()) {
            container.getAutoRefreshSupport().toggle();
            container.redoLayout();
        }

        return new Undoer() {
            @Override
            public void undo(OperationContext operationContext) {
                toggle(operationContext.getGraphContainer()); // toggle again
            }
        };
    }
}
