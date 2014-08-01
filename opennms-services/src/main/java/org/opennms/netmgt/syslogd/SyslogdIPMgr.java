package org.opennms.netmgt.syslogd;

import java.sql.SQLException;

public interface SyslogdIPMgr {

	void dataSourceSync() throws SQLException;
	
	long getNodeId(String ipAddr);
	
	long setNodeId(String ipAddr, long nodeId);
	
	long removeNodeId(String ipAddr);
	
	long longValue(Long result);
}
