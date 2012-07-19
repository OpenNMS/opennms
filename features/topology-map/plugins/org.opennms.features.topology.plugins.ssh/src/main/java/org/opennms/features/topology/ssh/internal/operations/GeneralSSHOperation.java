package org.opennms.features.topology.ssh.internal.operations;

import java.util.List;

import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.ssh.internal.AuthWindow;

import com.vaadin.data.Item;

public class GeneralSSHOperation implements Operation {

	public Undoer execute(List<Object> targets, OperationContext operationContext) {
		operationContext.getMainWindow().addWindow(new AuthWindow("", 0));
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
