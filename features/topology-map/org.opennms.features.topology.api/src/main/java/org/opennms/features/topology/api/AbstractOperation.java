package org.opennms.features.topology.api;

import java.util.List;

import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.Property;

public abstract class AbstractOperation implements Operation {

    @Override
    public Undoer execute(final List<VertexRef> targets, final OperationContext operationContext) {
        return null;
    }

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

    @Override
    public abstract String getId();

    protected static String getLabelValue(final OperationContext operationContext, final VertexRef target) {
        return getPropertyValue(getVertexItem(operationContext, target), "label", String.class);
    }

    protected static String getIpAddrValue(final OperationContext operationContext, final VertexRef target) {
        return getPropertyValue(getVertexItem(operationContext, target), "ipAddr", String.class);
    }

    protected static Integer getNodeIdValue(final OperationContext operationContext, final VertexRef target) {
        return getPropertyValue(getVertexItem(operationContext, target), "nodeID", Integer.class);
    }

	protected static Item getVertexItem(final OperationContext operationContext, final VertexRef target) {
		Vertex vertex = operationContext.getGraphContainer().getVertex(target);
		if (vertex == null) {
			LoggerFactory.getLogger(AbstractOperation.class).debug("Null vertex found for vertex reference: {}:{}", target.getNamespace(), target.getId());
			return null;
		} else {
			return vertex.getItem();
		}
	}

    @SuppressWarnings("unchecked")
    protected static <T> T getPropertyValue(final Item item, final Object id, final Class<T> clazz) {
        if (item == null) return null;

        final Property prop = item.getItemProperty(id);
        if (prop == null) return null;

        final Class<?> type = prop.getType();
        if (type == null) return null;

        final Object value = prop.getValue();
        if (value == null) return null;

        if (!type.isAssignableFrom(clazz)) {
            LoggerFactory.getLogger(AbstractOperation.class).warn("Expected " + id + " of type " + clazz.getName() + ", but got " + type.getName() + " instead.");
            return null;
        }

        return (T) value;
    }

}
