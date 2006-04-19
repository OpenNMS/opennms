/**
 * 
 */
package org.opennms.netmgt.dao.jdbc.event;

import javax.sql.DataSource;

import org.opennms.netmgt.model.OnmsEvent;

public class EventUpdate extends EventSaveOrUpdate {

    // DO NOT CHANGE THIS STATEMENT UNLESS YOU CHANGE THE INSERT STATEMENT AND THE
    // PARAMETERS IN EventSaveOrUpdate
    private static final String updateStmt = "update events set " +
    		"eventID = ?, eventUei = ?, nodeID = ?, eventTime = ?, eventHost = ?, " + 
    		"eventSource = ?, ipAddr = ?, eventDpName = ?, eventSnmphost = ?, serviceID = ?, " + 
    		"eventSnmp = ?, eventParms = ?, eventCreateTime = ?, eventDescr = ?, eventLoggroup = ?, " + 
    		"eventLogmsg = ?, eventSeverity = ?, eventPathOutage = ?, eventCorrelation = ?, eventSuppressedCount = ?, " + 
    		"eventOperInstruct = ?, eventAutoAction = ?, eventOperAction = ?, eventOperActionMenuText = ?, eventNotification = ?, " + 
    		"eventTticket = ?, eventTticketState = ?, eventForward = ?, eventMouseOverText = ?, eventLog = ?, " + 
    		"eventDisplay = ?, eventAckUser = ?, eventAckTime = ?, alarmID = ? " +
    		"where eventid = ?";

    public EventUpdate(DataSource ds) {
        super(ds, updateStmt);
    }
    
    public int doUpdate(OnmsEvent event) {
        return persist(event);
    }
    
}