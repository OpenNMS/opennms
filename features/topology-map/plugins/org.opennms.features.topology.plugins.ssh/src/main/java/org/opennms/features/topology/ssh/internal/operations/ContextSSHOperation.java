package org.opennms.features.topology.ssh.internal.operations;

import java.util.List;

import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.ssh.internal.AuthWindow;

import com.vaadin.data.Item;

public class ContextSSHOperation implements Operation {

	public Undoer execute(List<Object> targets, OperationContext operationContext) {
		//Default server info
		String host = "debian.opennms.org";
		int port = 22;

		if (targets != null) {
			for(Object target : targets) {
				host = (String) operationContext.getGraphContainer().getVertexItem(target).getItemProperty("host").getValue();
				port = (Integer) operationContext.getGraphContainer().getVertexItem(target).getItemProperty("port").getValue();
			}
		}
		operationContext.getMainWindow().addWindow(new AuthWindow(host, port));
		return null;
	}

	public boolean display(List<Object> targets, OperationContext operationContext) {
		return true;
	}

	public boolean enabled(List<Object> targets, OperationContext operationContext) {
		if (targets != null) {
			if(targets.size() == 1) {
				return true;
			}
			for(Object target : targets) {
				Object itemId = target;
				Item vertexItem = operationContext.getGraphContainer().getVertexItem(itemId);
				if(vertexItem.getItemProperty("host").getValue() != null) {

				}
			}
		}
		return true;
	}

	public String getId() {
		return "ssh";
	}

}
