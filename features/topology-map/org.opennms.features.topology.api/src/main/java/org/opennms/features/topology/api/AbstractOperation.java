package org.opennms.features.topology.api;

import java.util.List;

import com.vaadin.data.Item;
import com.vaadin.data.Property;

public abstract class AbstractOperation implements Operation {

    @Override
    public Undoer execute(final List<Object> targets, final OperationContext operationContext) {
        return null;
    }

    @Override
    public boolean display(final List<Object> targets, final OperationContext operationContext) {
        return true;
    }

    @Override
    public boolean enabled(final List<Object> targets, final OperationContext operationContext) {
        if (targets == null || targets.size() < 2) {
            for (final Object target : targets) {
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

    protected String getLabelValue(final OperationContext operationContext, final Object target) {
        return getVertexPropertyValue(operationContext, target, "label", String.class);
    }

    protected String getIpAddrValue(final OperationContext operationContext, final Object target) {
        return getVertexPropertyValue(operationContext, target, "ipAddr", String.class);
    }

    protected Integer getNodeIdValue(final OperationContext operationContext, final Object target) {
        return getVertexPropertyValue(operationContext, target, "nodeID", Integer.class);
    }

    protected <T> T getVertexPropertyValue(final OperationContext operationContext, final Object target, final Object id, final Class<T> clazz) {
        return getPropertyValue(operationContext.getGraphContainer().getVertexItem(target), id, clazz);
    }

    @SuppressWarnings("unchecked")
    protected <T> T getPropertyValue(final Item item, final Object id, final Class<T> clazz) {
        if (item == null) return null;

        final Property prop = item.getItemProperty(id);
        if (prop == null) return null;

        final Class<?> type = prop.getType();
        if (type == null) return null;

        final Object value = prop.getValue();
        if (value == null) return null;

        if (!type.isAssignableFrom(clazz)) {
            System.err.println("Warning: expected " + id + " of type " + clazz + ", but got " + type + " instead.");
            return null;
        }

        return (T) value;
    }

}
