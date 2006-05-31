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
package org.opennms.netmgt.dao.jdbc.notification;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.opennms.netmgt.dao.jdbc.Cache;
import org.opennms.netmgt.dao.jdbc.JdbcSet;
import org.opennms.netmgt.dao.jdbc.outage.LazyOutage;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsNotification;
import org.opennms.netmgt.model.OnmsOutage;
import org.springframework.jdbc.object.MappingSqlQuery;

public class NotificationMappingQuery extends MappingSqlQuery {

    public NotificationMappingQuery(DataSource ds, String clause) {
        super(ds, "SELECT " + 
             "n.textMsg as textMsg, " +
             "n.subject as subject, " +
             "n.pageTime as pageTime, " +
             "n.respondTime as respondTime, " +
             "n.answeredby as answeredby, " +
             "n.nodeID as nodeID, " +
             "n.interfaceID as interfaceID, " +
             "n.serviceID as serviceID, " +
             "n.queueID as queueID, " +
             "n.eventID as eventID, " +
             "n.notifyID as notifyID " + clause);
    }
    
    public DataSource getDataSource() {
        return getJdbcTemplate().getDataSource();
    }

    public Object mapRow(ResultSet rs, int rowNumber) throws SQLException {
        final Integer id = (Integer) rs.getObject("notifyID");

        LazyNotification notification = (LazyNotification)Cache.obtain(OnmsNotification.class, id);
        notification.setLoaded(true);
        
        notification.setTextMsg(rs.getString("textMsg"));
        notification.setSubject(rs.getString("subject"));
        notification.setNumericMsg(rs.getString("numericMsg"));
        notification.setPageTime(rs.getTimestamp("pageTime"));
        notification.setRespondTime(rs.getTimestamp("respondTime"));
        notification.setAnsweredBy(rs.getString("answeredBy"));
        notification.setQueueId(rs.getString("queueID"));
        
        Integer nodeId = new Integer(rs.getInt("nodeID"));
        OnmsNode node = (OnmsNode)Cache.obtain(OnmsNode.class, nodeId);
        notification.setNode(node);
        
        Integer eventId = new Integer(rs.getInt("eventID"));
        OnmsEvent event = (OnmsEvent)Cache.obtain(OnmsEvent.class, eventId);
        notification.setEvent(event);
        
        Integer serviceId = new Integer(rs.getInt("serviceId"));
        OnmsMonitoredService service = (OnmsMonitoredService)Cache.obtain(OnmsMonitoredService.class, serviceId);
        notification.setService(service);

        notification.setDirty(false);
        return notification;
    }
    
    public OnmsNotification findUnique() {
        return findUnique((Object[])null);
    }
    
    public OnmsNotification findUnique(Object obj) {
        return findUnique(new Object[] { obj });
    }

    public OnmsNotification findUnique(Object[] objs) {
        List notifs = execute(objs);
        if (notifs.size() > 0)
            return (OnmsNotification) notifs.get(0);
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