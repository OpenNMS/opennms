package org.opennms.features.topology.plugins.topo.linkd.internal.operations;

import java.util.List;

import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.plugins.topo.linkd.internal.LinkdTopologyProvider;

public class OpenOperation implements Operation {

    LinkdTopologyProvider m_linkdTopologyProvider;
    public OpenOperation(LinkdTopologyProvider topologyProveder) {
        m_linkdTopologyProvider=topologyProveder;
    }

    @Override
    public Undoer execute(List<Object> targets, OperationContext operationContext) {
        if (targets == null || targets.isEmpty() ) {
        	log("no target, loading null provider");
            m_linkdTopologyProvider.load(null);
        } else {
        	log("loading " + targets.get(0));
            m_linkdTopologyProvider.load((String) targets.get(0));
        }
        log("finished loading");
        return null;
    }

    private void log(final String string) {
		System.err.println(string);
	}

	@Override
    public boolean display(List<Object> targets,
            OperationContext operationContext) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean enabled(List<Object> targets,
            OperationContext operationContext) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String getId() {
        // TODO Auto-generated method stub
        return null;
    }

}
