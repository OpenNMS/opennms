package org.opennms.netmgt.model;

public class PathOutage {
	private int nodeId;
	private String criticalPathIp;
	private String criticalPathServiceName;
	
	//Operations
	public String toString() {
		StringBuffer result = new StringBuffer(50);
		result.append("pathOutage { nodeID: ");
		result.append(nodeId);
		result.append(", criticalPathIp: ");
		result.append(criticalPathIp);
		result.append(", criticalPathServiceName: ");
		result.append(criticalPathServiceName);
		result.append(" }");
		return result.toString();
	}

	public String getCriticalPathIp() {
		return criticalPathIp;
	}

	public void setCriticalPathIp(String criticalPathIp) {
		this.criticalPathIp = criticalPathIp;
	}

	public String getCriticalPathServiceName() {
		return criticalPathServiceName;
	}

	public void setCriticalPathServiceName(String criticalPathServiceName) {
		this.criticalPathServiceName = criticalPathServiceName;
	}

	public int getNodeId() {
		return nodeId;
	}

	public void setNodeId(int nodeId) {
		this.nodeId = nodeId;
	}
}
