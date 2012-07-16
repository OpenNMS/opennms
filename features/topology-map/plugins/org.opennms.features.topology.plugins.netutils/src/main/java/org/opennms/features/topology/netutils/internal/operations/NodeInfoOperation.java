package org.opennms.features.topology.netutils.internal.operations;

import java.util.List;

import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.netutils.internal.Node;
import org.opennms.features.topology.netutils.internal.NodeInfoWindow;
import org.opennms.features.topology.api.OperationContext;

public class NodeInfoOperation implements Operation {

	/*Test Data*/
	private Node testNode1 = new Node(9,"172.20.1.10","Cartman");
	private Node testNode2 = new Node(43, "172.20.1.14", "Butters");
	
	public boolean display(List<Object> targets, OperationContext operationContext) {
		// TODO Auto-generated method stub
		return true;
	}

	public boolean enabled(List<Object> targets, OperationContext operationContext) {
		// TODO Auto-generated method stub
		return true;
	}

	public Undoer execute(List<Object> targets, OperationContext operationContext) {
		try {
			operationContext.getMainWindow().addWindow(new NodeInfoWindow(testNode1));
		} catch (Exception e) { e.printStackTrace(); }
		return null;
	}

	public String getId() {
		// TODO Auto-generated method stub
		return "nodeInfo";
	}

}
