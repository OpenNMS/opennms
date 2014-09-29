package org.opennms.features.topology.plugins.ncs;

import org.opennms.features.topology.api.AbstractCheckedOperation;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.topo.EdgeStatusProvider;
import org.opennms.features.topology.api.topo.VertexRef;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class NCSStatusToggleOperation extends AbstractCheckedOperation {

    private EdgeStatusProvider m_ncsEdgeStatusProvider;

    @Override
    public void applyHistory(GraphContainer container, Map<String, String> settings) {
        Set<EdgeStatusProvider> edgeStatusProviders = container.getEdgeStatusProviders();

        String historyValue = settings.get(getClass().getName());
        boolean statusEnabled = Boolean.TRUE.toString().equals(historyValue);
        if (statusEnabled) {
            if (!edgeStatusProviders.contains(m_ncsEdgeStatusProvider)) {
                edgeStatusProviders.add(m_ncsEdgeStatusProvider);
            }
        } else {
            if (edgeStatusProviders.contains(m_ncsEdgeStatusProvider)) {
                edgeStatusProviders.remove(m_ncsEdgeStatusProvider);
            }
        }
    }

    @Override
    public Undoer execute(List<VertexRef> targets, OperationContext operationContext) {
        toggle(operationContext.getGraphContainer());
        return new Undoer() {
            @Override
            public void undo(OperationContext operationContext) {
                toggle(operationContext.getGraphContainer());
            }
        };
    }

    private void toggle(GraphContainer graphContainer) {
        if (graphContainer.getEdgeStatusProviders().contains(m_ncsEdgeStatusProvider)) {
            graphContainer.getEdgeStatusProviders().remove(m_ncsEdgeStatusProvider);
        } else {
            graphContainer.getEdgeStatusProviders().add(m_ncsEdgeStatusProvider);
        }

        graphContainer.redoLayout();
    }

    @Override
    public boolean display(List<VertexRef> targets, OperationContext operationContext) {
        return true;
    }

    @Override
    public String getId() {
        return getClass().getSimpleName();
    }

    @Override
    protected boolean isChecked(GraphContainer container) {
        Set<EdgeStatusProvider> edgeStatusProviders = container.getEdgeStatusProviders();
        return edgeStatusProviders.contains(m_ncsEdgeStatusProvider);
    }

    public EdgeStatusProvider getNcsEdgeStatusProvider() {
        return m_ncsEdgeStatusProvider;
    }

    public void setNcsEdgeStatusProvider(EdgeStatusProvider ncsEdgeStatusProvider) {
        m_ncsEdgeStatusProvider = ncsEdgeStatusProvider;
    }
}
