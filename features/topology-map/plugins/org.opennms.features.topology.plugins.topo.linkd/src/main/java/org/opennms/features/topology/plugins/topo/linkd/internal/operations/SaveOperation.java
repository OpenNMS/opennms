package org.opennms.features.topology.plugins.topo.linkd.internal.operations;

import java.util.List;

import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.plugins.topo.linkd.internal.LinkdTopologyProvider;

public class SaveOperation implements Operation {

    LinkdTopologyProvider m_linkdTopologyProvider;
    public SaveOperation(LinkdTopologyProvider topologyProveder) {
        m_linkdTopologyProvider=topologyProveder;
    }

    @Override
    public Undoer execute(List<Object> targets,
            OperationContext operationContext) {
        if (targets != null && !targets.isEmpty() )
            m_linkdTopologyProvider.save((String) targets.get(0));
        return null;
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
