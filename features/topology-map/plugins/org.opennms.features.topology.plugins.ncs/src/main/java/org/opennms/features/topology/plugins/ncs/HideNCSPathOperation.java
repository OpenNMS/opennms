package org.opennms.features.topology.plugins.ncs;

import java.util.Collections;
import java.util.List;

import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.plugins.ncs.NCSPathEdgeProvider.NCSServicePathCriteria;

public class HideNCSPathOperation implements Operation {

    @Override
    public Undoer execute(List<VertexRef> targets, OperationContext operationContext) {
        if(operationContext.getGraphContainer().getCriteria("ncsPath") != null) {
            operationContext.getGraphContainer().setCriteria(new NCSServicePathCriteria(Collections.<Edge>emptyList()));
        }
        
        return null;
    }

    @Override
    public boolean display(List<VertexRef> targets, OperationContext operationContext) {
        NCSServicePathCriteria ncsPathCriteria = (NCSServicePathCriteria) operationContext.getGraphContainer().getCriteria("ncsPath");
        if(ncsPathCriteria != null && ncsPathCriteria.size() > 0) {
            return true;
        }
        
        return false;
    }

    @Override
    public boolean enabled(List<VertexRef> targets, OperationContext operationContext) {
        NCSServicePathCriteria ncsPathCriteria = (NCSServicePathCriteria) operationContext.getGraphContainer().getCriteria("ncsPath");
        if(ncsPathCriteria != null && ncsPathCriteria.size() > 0) {
            return true;
        }
        return false;
    }

    @Override
    public String getId() {
        return null;
    }

}
