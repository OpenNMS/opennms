package org.opennms.features.topology.api;

import java.util.List;

public interface CheckedOperation extends Operation {
    public boolean isChecked(List<Object> targets, OperationContext operationContext);
}
