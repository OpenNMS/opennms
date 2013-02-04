package org.opennms.features.topology.api;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.opennms.features.topology.api.topo.VertexRef;

public abstract class AbstractCheckedOperation implements CheckedOperation {

	/**
	 * Return true by default.
	 */
	protected boolean enabled(OperationContext context) {
		return true;
	}

	protected boolean isChecked(OperationContext context) {
		return false;
	}

	/**
	 * By default, call {@link #enabled(OperationContext)}
	 */
	@Override
	public boolean enabled(List<VertexRef> vertices, OperationContext context) {
		return enabled(context);
	}

	/**
	 * By default, call {@link #isChecked(OperationContext)}
	 */
	@Override
	public boolean isChecked(List<VertexRef> vertices, OperationContext context) {
		return isChecked(context);
	}

	/**
	 * By default, save the state based on the checked status of the operation,
	 * independent of any currently-selected vertices.
	 */
	@Override
	public Map<String, String> createHistory(OperationContext context) {
		return Collections.singletonMap(this.getClass().getName(), Boolean.toString(isChecked(context)));
	}


	@Override
	public void applyHistory(OperationContext context, Map<String, String> settings) {
		
	}
}
