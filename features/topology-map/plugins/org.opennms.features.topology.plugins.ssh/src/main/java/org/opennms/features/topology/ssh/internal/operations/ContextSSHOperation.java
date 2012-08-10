package org.opennms.features.topology.ssh.internal.operations;

import java.util.List;

import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.ssh.internal.AuthWindow;

import com.vaadin.data.Item;
import com.vaadin.data.Property;

public class ContextSSHOperation implements Operation {

	public Undoer execute(List<Object> targets, OperationContext operationContext) {
		String ipAddr = "";
		int port = 0;

		if (targets != null) {
			for(Object target : targets) {
				Item vertexItem = operationContext.getGraphContainer().getVertexItem(target);
				if (vertexItem != null) {
					Property ipAddrProperty = vertexItem.getItemProperty("ipAddr");
					ipAddr = ipAddrProperty == null ? "" : (String) ipAddrProperty.getValue();
					//Property portProperty = operationContext.getGraphContainer().getVertexItem(target).getItemProperty("port");
					port = 22; //portProperty == null ? -1 : (Integer) portProperty.getValue();
				}
			}
		}
		operationContext.getMainWindow().addWindow(new AuthWindow(ipAddr, port));
		return null;
	}

	public boolean display(List<Object> targets, OperationContext operationContext) {
		String ipAddr = "";
		int port = -1;
		if (targets != null) {
			List<Object> selectedVertices = operationContext.getGraphContainer().getSelectedVertices();
			if (selectedVertices.size() > 0) return false;
			for(Object target : targets) {
				Item vertexItem = operationContext.getGraphContainer().getVertexItem(target);
				if (vertexItem != null) {
					Property ipAddrProperty = vertexItem.getItemProperty("ipAddr");
					ipAddr = ipAddrProperty == null ? "" : (String) ipAddrProperty.getValue();
					//Property portProperty = operationContext.getGraphContainer().getVertexItem(target).getItemProperty("port");
					//portProperty == null ? -1 : (Integer) portProperty.getValue();
				}
			}
		}
		if ("".equals(ipAddr) || port < 0) return false;
		return true;
	}

	public boolean enabled(List<Object> targets, OperationContext operationContext) {
		return true;
	}

	public String getId() {
		return "contextSSH";
	}

}
