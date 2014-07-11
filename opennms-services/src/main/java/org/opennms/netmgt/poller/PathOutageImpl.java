package org.opennms.netmgt.poller;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface PathOutageImpl {

	List<String[]> getAllCriticalPaths() throws SQLException;
	
	String getPrettyCriticalPath(int nodeID) throws SQLException;
	
	String[] getCriticalPath(int nodeId) throws SQLException;
	
	List<String> getNodesInPath(String criticalPathIp, String criticalPathServiceName) throws SQLException;
	
	String[] getLabelAndStatus(String nodeIDStr, Connection conn) throws SQLException;
	
	String[] getCriticalPathData(String criticalPathIp, String criticalPathServiceName) throws SQLException;
	
	
}
