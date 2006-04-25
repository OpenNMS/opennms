/**
 * 
 */
package org.opennms.netmgt.dao.jdbc.outage;

import javax.sql.DataSource;

import org.opennms.netmgt.model.OnmsOutage;

public class OutageSave extends OutageSaveOrUpdate {
    
    // DO NOT CHANGE THIS STATEMENT UNLESS YOU CHANGE THE UPDATE STATEMENT AND THE
    // PARAMETERS IN OutageSaveOrUpdate
    private static final String insertStmt = "insert into outages (" +
    "svcLostEventID, svcRegainedEventID, nodeID, ipAddr, " + 
    "serviceID, ifLostService, ifRegainedService, outageID) " + 
    "values (" + 
    "?, ?, ?, ?, " + 
    "?, ?, ?, ? )";
    

    public OutageSave(DataSource ds) {
        super(ds, insertStmt);
    }
    
    public int doInsert(OnmsOutage outage) {
        return persist(outage);
    }

    
}