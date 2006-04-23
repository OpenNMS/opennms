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
package org.opennms.netmgt.dao.jdbc.outage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.opennms.netmgt.config.DataSourceFactory;
import org.opennms.netmgt.dao.IpInterfaceDao;
import org.opennms.netmgt.dao.jdbc.Cache;
import org.opennms.netmgt.dao.jdbc.IpInterfaceDaoJdbc;
import org.opennms.netmgt.dao.jdbc.JdbcSet;
import org.opennms.netmgt.dao.jdbc.ipif.IpInterfaceId;
import org.opennms.netmgt.dao.jdbc.ipif.LazyIpInterface;
import org.opennms.netmgt.dao.jdbc.monsvc.LazyMonitoredService;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsOutage;
import org.springframework.jdbc.object.MappingSqlQuery;

public class OutageMappingQuery extends MappingSqlQuery {

    public OutageMappingQuery(DataSource ds, String clause) {
        super(ds, "SELECT " + 
        		"o.outageID as outageID, \n" + 
        		"o.svcLostEventID as svcLostEventID, \n" + 
        		"o.svcRegainedEventID as svcRegainedEventID, \n" + 
        		"o.nodeID as nodeID, \n" + 
        		"o.ipAddr as ipAddr, \n" + 
        		"o.serviceID as serviceID, \n" + 
        		"o.ifLostService as ifLostService, \n" + 
        		"o.ifRegainedService as ifRegainedService " +clause);
    }
    
    public DataSource getDataSource() {
        return getJdbcTemplate().getDataSource();
    }

    public Object mapRow(ResultSet rs, int rowNumber) throws SQLException {
        final Integer id = (Integer) rs.getObject("outageid");

        LazyOutage outage = (LazyOutage)Cache.obtain(OnmsOutage.class, id);
        outage.setLoaded(true);
        
        Integer nodeId = new Integer(rs.getInt("nodeID"));
        OnmsNode node = (OnmsNode)Cache.obtain(OnmsNode.class, nodeId);
        String ipAddr = rs.getString("ipAddr");
        IpInterfaceDao ifDao = new IpInterfaceDaoJdbc(DataSourceFactory.getInstance());
        OnmsIpInterface ipIf = ifDao.get(node, ipAddr);
        
        Integer serviceId = new Integer(rs.getInt("serviceID"));
        LazyMonitoredService svc = (LazyMonitoredService)Cache.obtain(OnmsMonitoredService.class, serviceId);
        outage.setMonitoredService(svc);

        IpInterfaceId key = new IpInterfaceId(ipIf.getNode().getId(), ipIf.getIpAddress(), ipIf.getIfIndex());
    	LazyIpInterface iface = (LazyIpInterface)Cache.obtain(OnmsIpInterface.class, key);
        outage.setIpInteface(iface);
    	
        Integer eventId = new Integer(rs.getInt("svcLostEventID"));
        OnmsEvent event = (OnmsEvent)Cache.obtain(OnmsEvent.class, eventId);
        outage.setEventBySvcLostEvent(event);
        
        eventId = new Integer(rs.getInt("svcRegainedEventID"));
        event = (OnmsEvent)Cache.obtain(OnmsEvent.class, eventId);        
        outage.setEventBySvcRegainedEvent(event);
        
        outage.setIfLostService(rs.getTimestamp("ifLostService"));
        outage.setIfRegainedService(rs.getTimestamp("ifRegainedService"));    	
        
        outage.setDirty(false);
        return outage;
    }
    
    public OnmsOutage findUnique() {
        return findUnique((Object[])null);
    }
    
    public OnmsOutage findUnique(Object obj) {
        return findUnique(new Object[] { obj });
    }

    public OnmsOutage findUnique(Object[] objs) {
        List events = execute(objs);
        if (events.size() > 0)
            return (OnmsOutage) events.get(0);
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