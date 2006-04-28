/**
 * 
 */
package org.opennms.netmgt.dao.jdbc.agent;

import javax.sql.DataSource;

import org.opennms.netmgt.model.OnmsNode;

public class AgentSave extends AgentSaveOrUpdate {
    
    // DO NOT CHANGE THIS STATEMENT UNLESS YOU CHANGE THE UPDATE STATEMENT AND THE
    // PARAMETERS IN NodeSaveOrUpdate
    private static final String insertStmt =
        "insert into node (dpName, nodeCreateTime, nodeParentID, nodeType, nodeSysOid, nodeSysName, nodeSysDescription, nodeSysLocation, nodeSysContact, nodeLabel, nodeLabelSource, nodeNetBiosName, nodeDomainName, operatingSystem, lastCapsdPoll, nodeid) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    public AgentSave(DataSource ds) {
        super(ds, insertStmt);
    }
    
    public int doInsert(OnmsNode node) {
        return persist(node);
    }

    
}