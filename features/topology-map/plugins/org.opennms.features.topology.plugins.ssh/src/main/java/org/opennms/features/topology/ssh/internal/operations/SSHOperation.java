package org.opennms.features.topology.ssh.internal.operations;

import java.util.List;

import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.ssh.internal.AuthWindow;

import com.vaadin.data.Item;
import com.vaadin.data.Property;

public class SSHOperation implements Operation {

	public Undoer execute(final List<Object> targets, final OperationContext operationContext) {
	    String ipAddr = "";
	    int port = 22;

	    if (targets != null) {
	        for(final Object target : targets) {
	            final Item vertexItem = operationContext.getGraphContainer().getVertexItem(target);
	            if (vertexItem != null) {
	                final Property ipAddrProperty = vertexItem.getItemProperty("ipAddr");
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
	    return true;
	}

	public boolean enabled(final List<Object> targets, final OperationContext operationContext) {
	    if (targets == null || targets.size() < 2) return true;
	    return false;
	}

	public String getId() {
	    return "SSH";
	}

}
