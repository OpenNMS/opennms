package org.opennms.features.topology.ssh.internal.operations;

import java.util.List;

import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.ssh.internal.AuthWindow;

public class GeneralSSHOperation implements Operation {

	public Undoer execute(List<Object> targets, OperationContext operationContext) {
		operationContext.getMainWindow().addWindow(new AuthWindow("", 0));
		return null;
	}

	public boolean display(List<Object> targets, OperationContext operationContext) {
		return true;
	}

	public boolean enabled(List<Object> targets, OperationContext operationContext) {
		return true;
	}

	public String getId() {
		return "ssh";
	}

}
