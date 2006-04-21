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
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
package org.opennms.netmgt.dao.jdbc.ipif;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.opennms.netmgt.dao.jdbc.Cache;
import org.opennms.netmgt.dao.jdbc.JdbcSet;
import org.opennms.netmgt.dao.jdbc.LazySet;
import org.opennms.netmgt.dao.jdbc.monsvc.FindByInterfaceWithIfIndex;
import org.opennms.netmgt.dao.jdbc.monsvc.FindByInterfaceWithNulIfIndex;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.springframework.jdbc.object.MappingSqlQuery;

public class IpInterfaceMappingQuery extends MappingSqlQuery {

    public IpInterfaceMappingQuery(DataSource ds, String clause) {
        super(ds, "SELECT " +
        		"ipInterface.nodeID as ipInterface_nodeID, " +
        		"ipInterface.ipAddr as ipInterface_ipAddr, " +
        		"ipInterface.ifIndex as ipInterface_ifIndex, " +
        		"ipInterface.ipHostname as ipInterface_ipHostname, " +
        		"ipInterface.isManaged as ipInterface_isManaged, " +
        		"ipInterface.ipStatus as ipInterface_ipStatus, " +
        		"ipInterface.ipLastCapsdPoll as ipInterface_ipLastCapsdPoll, " +
        		"ipInterface.isSnmpPrimary as ipInterface_isSnmpPrimary " +
        		clause);
    }
    
    public DataSource getDataSource() {
        return getJdbcTemplate().getDataSource();
    }

   public Object mapRow(ResultSet rs, int rowNumber) throws SQLException {
    	
    	Integer nodeId = (Integer)rs.getObject("ipInterface_nodeID");  //nodeID                  integer,
    	String ipAddr = rs.getString("ipInterface_ipAddr");            //ipAddr                  varchar(16) not null,
		Integer ifIndex = (Integer)rs.getObject("ipInterface_ifIndex");//ifIndex                 integer,
		
		final IpInterfaceId key = new IpInterfaceId(nodeId, ipAddr, ifIndex);
    	
    	LazyIpInterface iface = (LazyIpInterface)Cache.obtain(OnmsIpInterface.class, key);
    	iface.setLoaded(true);
        iface.setIpHostName(rs.getString("ipInterface_ipHostname"));              //ipHostname              varchar(256),
        iface.setIsManaged(rs.getString("ipInterface_isManaged"));                //isManaged               char(1),
        iface.setIpStatus(((Integer)rs.getObject("ipInterface_ipStatus")));       //ipStatus                integer,
        iface.setIpLastCapsdPoll(rs.getTimestamp("ipInterface_ipLastCapsdPoll")); //ipLastCapsdPoll         timestamp without time zone,
        iface.setIsSnmpPrimary(rs.getString("ipInterface_isSnmpPrimary"));        //isSnmpPrimary           char(1),
        
        LazySet.Loader svcLoader = new LazySet.Loader() {
			public Set load() {
				if (key.getIfIndex() == null)
					return new FindByInterfaceWithNulIfIndex(getDataSource()).findSet(new Object[] { key.getNodeId(), key.getIpAddr() });
				else
					return new FindByInterfaceWithIfIndex(getDataSource()).findSet(new Object[] { key.getNodeId(), key.getIpAddr(), key.getIfIndex()});
			}
        };
        iface.setMonitoredServices(new LazySet(svcLoader));
        iface.setDirty(false);
        return iface;
    }
    

    public OnmsIpInterface findUnique() {
        return findUnique((Object[])null);
    }
    
    public OnmsIpInterface findUnique(Object obj) {
        return findUnique(new Object[] { obj });
    }

    public OnmsIpInterface findUnique(Object o1, Object o2) {
        return findUnique(new Object[] { o1, o2 });
    }

    public OnmsIpInterface findUnique(Object o1, Object o2, Object o3) {
        return findUnique(new Object[] { o1, o2, o3 });
    }

    public OnmsIpInterface findUnique(Object[] objs) {
        List nodes = execute(objs);
        if (nodes.size() > 0)
            return (OnmsIpInterface) nodes.get(0);
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
        List nodes = execute(objs);
        Set results = new JdbcSet(nodes);
        return results;
    }
    
}