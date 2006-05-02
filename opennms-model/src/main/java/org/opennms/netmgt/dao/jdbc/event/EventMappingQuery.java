//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.dao.jdbc.event;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.opennms.netmgt.dao.jdbc.Cache;
import org.opennms.netmgt.dao.jdbc.JdbcSet;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.springframework.jdbc.object.MappingSqlQuery;

public class EventMappingQuery extends MappingSqlQuery {

    public EventMappingQuery(DataSource ds, String clause) {
        super(ds, "SELECT e.eventID as eventID, \n" + 
        		"e.eventUei as eventUei, \n" + 
        		"e.nodeID as nodeID, \n" + 
        		"e.eventTime as eventTime, \n" + 
        		"e.eventHost as eventHost, \n" + 
        		"e.eventSource as eventSource, \n" + 
        		"e.ipAddr as ipAddr, \n" + 
        		"e.eventDpName as eventDpName, \n" + 
        		"e.eventSnmphost as eventSnmphost, \n" + 
        		"e.serviceID as serviceID, \n" + 
        		"e.eventSnmp as eventSnmp, \n" + 
        		"e.eventParms as eventParms, \n" + 
        		"e.eventCreateTime as eventCreateTime, \n" + 
        		"e.eventDescr as eventDescr, \n" + 
        		"e.eventLoggroup as eventLoggroup, \n" + 
        		"e.eventLogmsg as eventLogmsg, \n" + 
        		"e.eventSeverity as eventSeverity, \n" + 
        		"e.eventPathOutage as eventPathOutage, \n" + 
        		"e.eventCorrelation as eventCorrelation, \n" + 
        		"e.eventSuppressedCount as eventSuppressedCount, \n" + 
        		"e.eventOperInstruct as eventOperInstruct, \n" + 
        		"e.eventAutoAction as eventAutoAction, \n" + 
        		"e.eventOperAction as eventOperAction, \n" + 
        		"e.eventOperActionMenuText as eventOperActionMenuText, \n" + 
        		"e.eventNotification as eventNotification, \n" + 
        		"e.eventTticket as eventTticket, \n" + 
        		"e.eventTticketState as eventTticketState, \n" + 
        		"e.eventForward as eventForward, \n" + 
        		"e.eventMouseOverText as eventMouseOverText, \n" + 
        		"e.eventLog as eventLog, \n" + 
        		"e.eventDisplay as eventDisplay, \n" + 
        		"e.eventAckUser as eventAckUser, \n" + 
        		"e.eventAckTime as eventAckTime, \n" + 
        		"e.alarmID as alarmID \n" +clause);
    }
    
    public DataSource getDataSource() {
        return getJdbcTemplate().getDataSource();
    }

    public Object mapRow(ResultSet rs, int rowNumber) throws SQLException {
        final Integer id = (Integer) rs.getObject("eventid");

        LazyEvent event = (LazyEvent)Cache.obtain(OnmsEvent.class, id);
        event.setLoaded(true);
        
        Integer nodeId = new Integer(rs.getInt("nodeID"));
        OnmsNode node = (OnmsNode)Cache.obtain(OnmsNode.class, nodeId);
        event.setNode(node);
        
        String dpName = rs.getString("eventDpName");
        OnmsDistPoller distPoller = (OnmsDistPoller)Cache.obtain(OnmsDistPoller.class, dpName);
        event.setDistPoller(distPoller);

        
//        Integer alarmId = new Integer(rs.getInt("alarmId"));
//        OnmsAlarm alarm = (OnmsAlarm)Cache.obtain(OnmsAlarm.class, alarmId);
//        event.setAlarm(alarm);
        
        Integer serviceId = new Integer(rs.getInt("serviceId"));
        OnmsMonitoredService service = (OnmsMonitoredService)Cache.obtain(OnmsMonitoredService.class, serviceId);
        event.setService(service);

        event.setEventUei(rs.getString("eventUei"));
        event.setEventTime(rs.getDate("eventTime"));
        event.setEventHost(rs.getString("eventHost"));
        event.setEventSource(rs.getString("eventSource"));
        event.setIpAddr(rs.getString("ipAddr"));
        event.setEventSnmpHost(rs.getString("eventSnmphost"));
        event.setEventSnmp(rs.getString("eventSnmp"));
        event.setEventParms(rs.getString("eventParms"));
        event.setEventCreateTime(rs.getDate("eventCreateTime"));
        event.setEventDescr(rs.getString("eventDescr"));
        event.setEventLogGroup(rs.getString("eventLoggroup"));
        event.setEventLogMsg(rs.getString("eventLogmsg"));
        event.setEventSeverity(new Integer(rs.getInt("eventSeverity")));
        event.setEventPathOutage(rs.getString("eventPathOutage"));
        event.setEventCorrelation(rs.getString("eventCorrelation"));
        event.setEventSuppressedCount(new Integer(rs.getInt("eventSuppressedCount")));
        event.setEventOperInstruct(rs.getString("eventOperInstruct"));
        event.setEventAutoAction(rs.getString("eventAutoAction"));
        event.setEventOperAction(rs.getString("eventOperAction"));
        event.setEventOperActionMenuText(rs.getString("eventOperActionMenuText"));
        event.setEventNotification(rs.getString("eventNotification"));
        event.setEventTTicket(rs.getString("eventTticket"));
        event.setEventTTicketState(new Integer(rs.getInt("eventTticketState")));
        event.setEventForward(rs.getString("eventForward"));
        event.setEventMouseOverText(rs.getString("eventMouseOverText"));
        event.setEventLog(rs.getString("eventLog"));
        event.setEventDisplay(rs.getString("eventDisplay"));
        event.setEventAckUser(rs.getString("eventAckUser"));
        event.setEventAckTime(rs.getDate("eventAckTime"));
        
        event.setDirty(false);
        return event;
    }
    
    public OnmsEvent findUnique() {
        return findUnique((Object[])null);
    }
    
    public OnmsEvent findUnique(Object obj) {
        return findUnique(new Object[] { obj });
    }

    public OnmsEvent findUnique(Object[] objs) {
        List events = execute(objs);
        if (events.size() > 0)
            return (OnmsEvent) events.get(0);
        else
            return null;
    }
    
    public Set findSet() {
        return findSet((Object[])null);
    }
    
    public Set findSet(Object obj) {
        return findSet(new Object[] { obj });
    }
    
    public Set findSet(Object[] objs) {
        List events = execute(objs);
        Set results = new JdbcSet(events);
        return results;
    }
    
}