package org.opennms.netmgt.correlation;

public interface StateKeeper {
	
	State getStateForNode(Integer nodeId);
	void setStateForNode(Integer nodeId, State state);
	void removeStateForNode(int nodeId);
	

}
