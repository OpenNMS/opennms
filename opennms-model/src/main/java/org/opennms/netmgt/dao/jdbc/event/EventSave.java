/**
 * 
 */
package org.opennms.netmgt.dao.jdbc.event;

import javax.sql.DataSource;

import org.opennms.netmgt.model.OnmsEvent;

public class EventSave extends EventSaveOrUpdate {
    
    // DO NOT CHANGE THIS STATEMENT UNLESS YOU CHANGE THE UPDATE STATEMENT AND THE
    // PARAMETERS IN EventSaveOrUpdate
    private static final String insertStmt = "insert into event (" +
    		"eventID, eventUei, nodeID, eventTime, " + 
    		"eventHost, eventSource, ipAddr, eventDpName, eventSnmphost, serviceID, eventSnmp, eventParms, " + 
    		"eventCreateTime, eventDescr, eventLoggroup, eventLogmsg, eventSeverity, eventPathOutage, " + 
    		"eventCorrelation, eventSuppressedCount, eventOperInstruct, eventAutoAction, eventOperAction, " + 
    		"eventOperActionMenuText, eventNotification, eventTticket, eventTticketState, eventForward, " + 
    		"eventMouseOverText, eventLog, eventDisplay, eventAckUser, eventAckTime, alarmID, " +
    		"values (" + 
    		"?, ?, ?, ?, " +
    		"?, ?, ?, ?, ?, ?, ?, ?, " +
    		"?, ?, ?, ?, ?, ?, " +
    		"?, ?, ?, ?, ?, " +
    		"?, ?, ?, ?, ?, " +
    		"?, ?, ?, ?, ?, ?)";

    public EventSave(DataSource ds) {
        super(ds, insertStmt);
    }
    
    public int doInsert(OnmsEvent event) {
        return persist(event);
    }

    
}