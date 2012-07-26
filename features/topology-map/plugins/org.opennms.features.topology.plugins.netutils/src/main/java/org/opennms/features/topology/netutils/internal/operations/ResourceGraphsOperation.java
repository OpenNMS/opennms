package org.opennms.features.topology.netutils.internal.operations;

import java.util.List;

import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.netutils.internal.Node;
import org.opennms.features.topology.netutils.internal.ResourceGraphsWindow;

public class ResourceGraphsOperation implements Operation {

	private String resourceGraphsURL;
	private String resourceGraphsFilter;

	public boolean display(List<Object> targets, OperationContext operationContext) {
		int nodeID = 0;

		if (targets != null) {
			for(Object target : targets) {
				nodeID = (Integer) operationContext.getGraphContainer().getVertexItem(target).getItemProperty("nodeID").getValue();
			}
		}
		if (nodeID < 0) return false;
		return true;
	}

	public boolean enabled(List<Object> targets, OperationContext operationContext) {
		int nodeID = 0;

		if (targets != null) {
			for(Object target : targets) {
				nodeID = (Integer) operationContext.getGraphContainer().getVertexItem(target).getItemProperty("nodeID").getValue();
			}
		}
		if (nodeID < 0) return false;
		return true;
	}

	public Undoer execute(List<Object> targets, OperationContext operationContext) {
		//Default server info
		String ipAddr = "";
		String label = "";
		int nodeID = -1;

		try {
			if (targets != null) {
				for(Object target : targets) {
					ipAddr = (String) operationContext.getGraphContainer().getVertexItem(target).getItemProperty("ipAddr").getValue();
					label = (String) operationContext.getGraphContainer().getVertexItem(target).getItemProperty("label").getValue();
					nodeID = (Integer) operationContext.getGraphContainer().getVertexItem(target).getItemProperty("nodeID").getValue();
				}
			} 
			if (nodeID < 0) {
				operationContext.getMainWindow().addWindow(new ResourceGraphsWindow(null, getResourceGraphsURL()));
			} else {
				Node node = new Node(nodeID, ipAddr, label);
				operationContext.getMainWindow().addWindow(new ResourceGraphsWindow(node, getResourceGraphsFilter()));
			}
		} catch (Exception e) {e.printStackTrace();}
		return null;
	}

	public String getId() {
		// TODO Auto-generated method stub
		return "resourceGraphs";
	}

	public String getResourceGraphsURL() {
		return resourceGraphsURL;
	}

	public void setResourceGraphsURL(String resourceGraphsURL) {
		this.resourceGraphsURL = resourceGraphsURL;
	}

	public String getResourceGraphsFilter() {
		return resourceGraphsFilter;
	}

	public void setResourceGraphsFilter(String resourceGraphsFilter) {
		this.resourceGraphsFilter = resourceGraphsFilter;
	}

}
