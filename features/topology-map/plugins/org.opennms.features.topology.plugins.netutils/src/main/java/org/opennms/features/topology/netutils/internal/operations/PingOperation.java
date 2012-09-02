package org.opennms.features.topology.netutils.internal.operations;

import java.util.List;

import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.netutils.internal.Node;
import org.opennms.features.topology.netutils.internal.PingWindow;

import com.vaadin.data.Item;
import com.vaadin.data.Property;

public class PingOperation implements Operation {

	private String pingURL;

	public boolean display(final List<Object> targets, final OperationContext operationContext) {
	    return true;
	}

	public boolean enabled(final List<Object> targets, final OperationContext operationContext) {
	    if (targets == null || targets.size() < 2) return true;
	    return false;
	}

	public Undoer execute(final List<Object> targets, final OperationContext operationContext) {
	    String ipAddr = "";
	    String label = "";
	    int nodeID = -1;

            if (targets != null) {
                for (final Object target : targets) {
                    final Item vertexItem = operationContext.getGraphContainer().getVertexItem(target);
                    if (vertexItem != null) {
                        final Property ipAddrProperty = vertexItem.getItemProperty("ipAddr");
                        ipAddr = ipAddrProperty == null ? "" : (String) ipAddrProperty.getValue();
                        final Property labelProperty = vertexItem.getItemProperty("label");
                        label = labelProperty == null ? "" : (String) labelProperty.getValue();
                        final Property nodeIDProperty = vertexItem.getItemProperty("nodeID");
                        nodeID = nodeIDProperty == null ? -1 : (Integer) nodeIDProperty.getValue();
                    }
                }
            }
            final Node node = new Node(nodeID, ipAddr, label);
            operationContext.getMainWindow().addWindow(new PingWindow(node, getPingURL()));
            return null;
	}

	public String getId() {
	    return "ping";
	}

	public void setPingURL(final String url) {
	    pingURL = url;
	}

	public String getPingURL() {
	    return pingURL;
	}

}
