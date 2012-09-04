package org.opennms.features.topology.plugins.topo.linkd.internal.operations;

import java.util.List;

import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.TopologyProvider;

public class OpenOperation implements Operation {

    TopologyProvider m_topologyProvider;
    
    public OpenOperation(TopologyProvider topologyProvider) {
        m_topologyProvider=topologyProvider;
    }

    @Override
    public Undoer execute(List<Object> targets, OperationContext operationContext) {
        if (targets == null || targets.isEmpty() ) {
            log("loading topology");
            m_topologyProvider.load(null);
        }
        return null;
    }

    private void log(final String string) {
		System.err.println(getId()+": "+ string);
	}

	@Override
    public boolean display(List<Object> targets, OperationContext operationContext) {
        return true;
    }

    @Override
    public boolean enabled(List<Object> targets, OperationContext operationContext) {
    	return true;
    }

    @Override
    public String getId() {
    	return "LinkdTopologyProviderOpenOperation";
    }

}
