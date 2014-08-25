package org.opennms.netmgt.utils;

import java.sql.Connection;
import java.sql.SQLException;

import org.opennms.netmgt.model.OnmsNode.NodeLabelSource;

public interface NodeLabel {

	String getLabel();
	
	NodeLabelSource getSource();
	
	NodeLabelJDBCImpl retrieveLabel(final int nodeID) throws SQLException;
	
	NodeLabelJDBCImpl retrieveLabel(int nodeID, Connection dbConnection) throws SQLException;
	
	void assignLabel(final int nodeID, final NodeLabelJDBCImpl nodeLabel) throws SQLException;
	
	void assignLabel(final int nodeID, NodeLabelJDBCImpl nodeLabel, final Connection dbConnection) throws SQLException;
	
	NodeLabelJDBCImpl computeLabel(final int nodeID) throws SQLException;
	
	NodeLabelJDBCImpl computeLabel(final int nodeID, final Connection dbConnection) throws SQLException;
	
	String toString();
}
