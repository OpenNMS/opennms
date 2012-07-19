package org.opennms.features.topology.netutils.internal.operations;

import java.util.List;

import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.netutils.internal.Node;
import org.opennms.features.topology.netutils.internal.TracerouteWindow;

public class TracerouteOperation implements Operation {

	/*Test Data*/
	private Node testNode1 = new Node(9,"172.20.1.10","Cartman");
	private Node testNode2 = new Node(43, "172.20.1.14", "Butters");
	
	private String tracerouteURL;
	
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
			operationContext.getMainWindow().addWindow(new TracerouteWindow(testNode1, getTracerouteURL()));
		} catch (Exception e) { e.printStackTrace(); }
		return null;
	}

	public String getId() {
		// TODO Auto-generated method stub
		return "traceroute";
	}
	
	public void setTracerouteURL(String url) {
		tracerouteURL = url;
	}
	
	public String getTracerouteURL() {
		return tracerouteURL;
	}

}
