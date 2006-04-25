/**
 * 
 */
package org.opennms.netmgt.dao.jdbc.outage;

import javax.sql.DataSource;

import org.opennms.netmgt.model.OnmsOutage;

public class OutageUpdate extends OutageSaveOrUpdate {

    // DO NOT CHANGE THIS STATEMENT UNLESS YOU CHANGE THE INSERT STATEMENT AND THE
    // PARAMETERS IN OutageSaveOrUpdate
    private static final String updateStmt = "update outages set " +
    "svcLostEventID = ?, svcRegainedEventID = ?, nodeID = ?, ipAddr = ?, " + 
    "serviceID = ?, ifLostService = ?, ifRegainedService = ? " + 
    "where outageID = ?";

    public OutageUpdate(DataSource ds) {
        super(ds, updateStmt);
    }
    
    public int doUpdate(OnmsOutage outage) {
        return persist(outage);
    }
    
}