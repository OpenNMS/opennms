package org.opennms.features.topology.netutils.internal.operations;

import java.util.List;

import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.netutils.internal.Node;
import org.opennms.features.topology.netutils.internal.NodeInfoWindow;
import org.opennms.features.topology.api.OperationContext;

import com.vaadin.data.Item;
import com.vaadin.data.Property;

public class ContextNodeInfoOperation implements Operation {

	private String nodeInfoFilter;

	public boolean display(List<Object> targets, OperationContext operationContext) {
		int nodeID = -1;
		if (targets != null) {
			List<Object> selectedVertices = operationContext.getGraphContainer().getSelectedVertices();
			if (selectedVertices.size() > 0) return false;
			for(Object target : targets) {
				Item vertexItem = operationContext.getGraphContainer().getVertexItem(target);
				if (vertexItem != null) {
					Property nodeIDProperty = vertexItem.getItemProperty("nodeID");
					nodeID = nodeIDProperty == null ? -1 : (Integer)nodeIDProperty.getValue();
				}
			}
		}
		if (nodeID < 0) return false;
		return true;
	}

	public boolean enabled(List<Object> targets, OperationContext operationContext) {
		return true;
	}

	public Undoer execute(List<Object> targets, OperationContext operationContext) {
		String label = "";
		int nodeID = -1;

		try {
			if (targets != null) {
				for(Object target : targets) {
					Property labelProperty = operationContext.getGraphContainer().getVertexItem(target).getItemProperty("label");
					label = labelProperty == null ? "" : (String) labelProperty.getValue();
					Property nodeIDProperty = operationContext.getGraphContainer().getVertexItem(target).getItemProperty("nodeID");
					nodeID = nodeIDProperty == null ? -1 : (Integer) nodeIDProperty.getValue();
				}
			}
			Node node = new Node(nodeID, null, label);
			operationContext.getMainWindow().addWindow(new NodeInfoWindow(node, getNodeInfoFilter()));
		} catch (Exception e) {e.printStackTrace();}
		return null;
	}

	public String getId() {
		return "contextNodeInfo";
	}

	public String getNodeInfoFilter() {
		return nodeInfoFilter;
	}

	public void setNodeInfoFilter(String nodeInfoFilter) {
		this.nodeInfoFilter = nodeInfoFilter;
	}

}
