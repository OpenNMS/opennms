/**
 * 
 */
package org.opennms.netmgt.dao.jdbc.node;

import javax.sql.DataSource;

import org.opennms.netmgt.model.OnmsNode;

public class NodeUpdate extends NodeSaveOrUpdate {

    // DO NOT CHANGE THIS STATEMENT UNLESS YOU CHANGE THE INSERT STATEMENT AND THE
    // PARAMETERS IN NodeSaveOrUpdate
    private static final String updateStmt = "update node set dpName = ?, nodeCreateTime = ?, nodeParentID = ?, nodeType = ?, nodeSysOid = ?, nodeSysName = ?, nodeSysDescription = ?, nodeSysLocation = ?, nodeSysContact = ?, nodeLabel = ?, nodeLabelSource = ?, nodeNetBiosName = ?, nodeDomainName = ?, operatingSystem = ?, lastCapsdPoll = ? where nodeid = ?";

    public NodeUpdate(DataSource ds) {
        super(ds, updateStmt);
    }
    
    public int doUpdate(OnmsNode node) {
        return persist(node);
    }
    
}