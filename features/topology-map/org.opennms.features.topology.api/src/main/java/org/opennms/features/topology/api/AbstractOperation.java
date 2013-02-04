package org.opennms.features.topology.api;

import java.util.List;

import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;
import org.slf4j.LoggerFactory;

public abstract class AbstractOperation implements Operation {

    @Override
    public boolean display(final List<VertexRef> targets, final OperationContext operationContext) {
        return true;
    }

    @Override
    public boolean enabled(final List<VertexRef> targets, final OperationContext operationContext) {
        if (targets == null || targets.size() < 2) {
            for (final VertexRef target : targets) {
                final Integer nodeValue = getNodeIdValue(operationContext, target);
                if (nodeValue != null && nodeValue > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    protected static String getLabelValue(final OperationContext operationContext, final VertexRef target) {
        return getVertexItem(operationContext, target).getLabel();
    }

    protected static String getIpAddrValue(final OperationContext operationContext, final VertexRef target) {
        return getVertexItem(operationContext, target).getIpAddress();
    }

    protected static Integer getNodeIdValue(final OperationContext operationContext, final VertexRef target) {
        return getVertexItem(operationContext, target).getNodeID();
    }

	protected static Vertex getVertexItem(final OperationContext operationContext, final VertexRef target) {
		Vertex vertex = operationContext.getGraphContainer().getBaseTopology().getVertex(target);
		if (vertex == null) {
			LoggerFactory.getLogger(AbstractOperation.class).debug("Null vertex found for vertex reference: {}:{}", target.getNamespace(), target.getId());
			return null;
		} else {
			return vertex;
		}
	}
}
