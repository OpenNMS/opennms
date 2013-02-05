package org.opennms.features.topology.api;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.opennms.features.topology.api.topo.VertexRef;

public abstract class AbstractCheckedOperation implements CheckedOperation {

	/**
	 * Return true by default.
	 */
	protected boolean enabled(GraphContainer container) {
		return true;
	}

	protected boolean isChecked(GraphContainer container) {
		return false;
	}

	/**
	 * By default, call {@link #enabled(OperationContext)}
	 */
	@Override
	public boolean enabled(List<VertexRef> vertices, OperationContext context) {
		return enabled(context.getGraphContainer());
	}

	/**
	 * By default, call {@link #isChecked(OperationContext)}
	 */
	@Override
	public boolean isChecked(List<VertexRef> vertices, OperationContext context) {
		return isChecked(context.getGraphContainer());
	}

	/**
	 * By default, save the state based on the checked status of the operation,
	 * independent of any currently-selected vertices.
	 */
	@Override
	public Map<String, String> createHistory(GraphContainer container) {
		return Collections.singletonMap(this.getClass().getName(), Boolean.toString(isChecked(container)));
	}
}
