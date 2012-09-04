package org.opennms.features.topology.plugins.topo.linkd.internal.operations;

import java.util.List;

import org.opennms.features.topology.api.CheckedOperation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.plugins.topo.linkd.internal.LinkdTopologyProvider;

public class HideNodesWithoutLinksOperation implements CheckedOperation {

    LinkdTopologyProvider m_topologyProvider;
    public HideNodesWithoutLinksOperation(LinkdTopologyProvider topologyProvider) {
        m_topologyProvider=topologyProvider;
    }

    @Override
    public Undoer execute(List<Object> targets,
            OperationContext operationContext) {
        log("found addNodeWithoutLinks: " + m_topologyProvider.isAddNodeWithoutLink());
        m_topologyProvider.setAddNodeWithoutLink(!m_topologyProvider.isAddNodeWithoutLink());
        log("switched addNodeWithoutLinks to: " + m_topologyProvider.isAddNodeWithoutLink());
        log("loading topology");
        m_topologyProvider.load(null);
        return null;
    }

    @Override
    public boolean display(List<Object> targets,
            OperationContext operationContext) {
        return true;
    }

    @Override
    public boolean enabled(List<Object> targets,
            OperationContext operationContext) {
        return true;
    }

    @Override
    public String getId() {
        return "LinkdTopologyProviderHidesNodesWithoutLinksOperation";
    }

    @Override
    public boolean isChecked(List<Object> targets,
            OperationContext operationContext) {
        return !m_topologyProvider.isAddNodeWithoutLink();
    }

    private void log(final String string) {
        System.err.println(getId()+": "+ string);
    }

}
