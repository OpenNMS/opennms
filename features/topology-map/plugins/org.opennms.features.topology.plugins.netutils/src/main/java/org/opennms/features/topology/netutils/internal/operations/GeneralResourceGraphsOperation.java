package org.opennms.features.topology.netutils.internal.operations;

import java.util.List;

import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.netutils.internal.ResourceGraphsWindow;

public class GeneralResourceGraphsOperation implements Operation {

	private String resourceGraphsURL;

	public boolean display(List<Object> targets, OperationContext operationContext) {
		return true;
	}

	public boolean enabled(List<Object> targets, OperationContext operationContext) {
		return true;
	}

	public Undoer execute(List<Object> targets, OperationContext operationContext) {
		try {
			operationContext.getMainWindow().addWindow(new ResourceGraphsWindow(null, getResourceGraphsURL()));
		} catch (Exception e) {e.printStackTrace();}
		return null;
	}

	public String getId() {
		return "generalResourceGraphs";
	}

	public String getResourceGraphsURL() {
		return resourceGraphsURL;
	}

	public void setResourceGraphsURL(String resourceGraphsURL) {
		this.resourceGraphsURL = resourceGraphsURL;
	}

}
