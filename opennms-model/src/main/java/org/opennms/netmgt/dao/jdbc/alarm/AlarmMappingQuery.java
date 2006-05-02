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
package org.opennms.netmgt.dao.jdbc.alarm;

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

public class AlarmMappingQuery extends MappingSqlQuery {

    public AlarmMappingQuery(DataSource ds, String clause) {
        super(ds, "SELECT " + 
        		"a.eventUei as eventUei, " + 
        		"a.dpName as dpName, " + 
        		"a.nodeID as nodeID, " + 
        		"a.ipaddr as ipaddr, " + 
        		"a.serviceID as serviceID, " + 
        		"a.reductionKey as reductionKey, " + 
        		"a.alarmType as alarmType, " + 
        		"a.counter as counter, " + 
        		"a.severity as severity, " + 
        		"a.lastEventID as lastEventID, " + 
        		"a.firstEventTime as firstEventTime, " + 
        		"a.lastEventTime as lastEventTime, " + 
        		"a.description as description, " + 
        		"a.logMsg as logMsg, " + 
        		"a.operInstruct as operInstruct, " + 
        		"a.tticketID as tticketID, " + 
        		"a.tticketState as tticketState, " + 
        		"a.mouseOverText as mouseOverText, " + 
        		"a.suppressedUntil as suppressedUntil, " + 
        		"a.suppressedUser as suppressedUser, " + 
        		"a.suppressedTime as suppressedTime, " + 
        		"a.alarmAckUser as alarmAckUser, " + 
        		"a.alarmAckTime as alarmAckTime, " + 
        		"a.clearUei as clearUei, " +
			"a.alarmID as alarmID" +clause);
    }
    
    public DataSource getDataSource() {
        return getJdbcTemplate().getDataSource();
    }

    public Object mapRow(ResultSet rs, int rowNumber) throws SQLException {
        final Integer id = (Integer) rs.getObject("alarmid");

        LazyAlarm alarm = (LazyAlarm)Cache.obtain(OnmsAlarm.class, id);
        alarm.setLoaded(true);
        
        Integer nodeId = new Integer(rs.getInt("nodeID"));
        OnmsNode node = (OnmsNode)Cache.obtain(OnmsNode.class, nodeId);
        alarm.setNodeId(node);
        
        String dpName = rs.getString("dpName");
        OnmsDistPoller distPoller = (OnmsDistPoller)Cache.obtain(OnmsDistPoller.class, dpName);
        alarm.setDistPoller(distPoller);

        Integer eventId = new Integer(rs.getInt("lastEventId"));
        OnmsEvent event = (OnmsEvent)Cache.obtain(OnmsEvent.class, eventId);
        alarm.setLastEvent(event);
        
        Integer serviceId = new Integer(rs.getInt("serviceId"));
        OnmsMonitoredService service = (OnmsMonitoredService)Cache.obtain(OnmsMonitoredService.class, serviceId);
        alarm.setService(service);

        alarm.setAlarmAckTime(rs.getTimestamp("alarmAckTime"));
        alarm.setAlarmAckUser(rs.getString("alarmackuser"));
        alarm.setAlarmType(new Integer(rs.getInt("alarmType")));
        alarm.setClearUei(rs.getString("clearUei"));
        alarm.setCounter(new Integer(rs.getInt("counter")));
        alarm.setDescription(rs.getString("description"));
        alarm.setFirstEventTime(rs.getTimestamp("firstEventTime"));
        alarm.setIpAddr(rs.getString("ipAddr"));
        alarm.setLastEventTime(rs.getTimestamp("lastEventTime"));
        alarm.setLogMsg(rs.getString("logMsg"));
        alarm.setMouseOverText(rs.getString("mouseOverText"));
        alarm.setOperInstruct(rs.getString("operInstruct"));
        alarm.setReductionKey(rs.getString("reductionKey"));
        alarm.setSeverity(new Integer(rs.getInt("severity")));
        alarm.setSuppressedTime(rs.getTimestamp("suppressedTime"));
        alarm.setSuppressedUntil(rs.getTimestamp("suppressedUntil"));
        alarm.setSuppressedUser(rs.getString("suppressedUser"));
        alarm.setTTicketId(rs.getString("tticketId"));
        alarm.setTTicketState(new Integer(rs.getInt("tticketState")));
        alarm.setUei(rs.getString("eventUei"));
        
        alarm.setDirty(false);
        return alarm;
    }
    
    public OnmsAlarm findUnique() {
        return findUnique((Object[])null);
    }
    
    public OnmsAlarm findUnique(Object obj) {
        return findUnique(new Object[] { obj });
    }

    public OnmsAlarm findUnique(Object[] objs) {
        List events = execute(objs);
        if (events.size() > 0)
            return (OnmsAlarm) events.get(0);
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