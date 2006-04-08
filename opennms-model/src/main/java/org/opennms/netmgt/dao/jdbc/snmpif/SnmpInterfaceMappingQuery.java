//
//This file is part of the OpenNMS(R) Application.
//
//OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
//OpenNMS(R) is a derivative work, containing both original code, included code and modified
//code that was published under the GNU General Public License. Copyrights for modified 
//and included code are below.
//
//OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
//Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
//This program is free software; you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation; either version 2 of the License, or
//(at your option) any later version.
//
//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.
//
//You should have received a copy of the GNU General Public License
//along with this program; if not, write to the Free Software
//Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
//For more information contact:
//OpenNMS Licensing       <license@opennms.org>
//http://www.opennms.org/
//http://www.opennms.com/
//
package org.opennms.netmgt.dao.jdbc.snmpif;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.opennms.netmgt.dao.jdbc.Cache;
import org.opennms.netmgt.dao.jdbc.JdbcSet;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.springframework.jdbc.object.MappingSqlQuery;

public class SnmpInterfaceMappingQuery extends MappingSqlQuery {
    
    public SnmpInterfaceMappingQuery(DataSource ds, String clause) {
        
        super(ds, "SELECT nodeid, ipaddr, snmpipadentnetmask" +
                ", snmpphysaddr, snmpifindex, snmpifdescr" +
                ", snmpiftype, snmpifname, snmpifspeed" +
                ", snmpifadminstatus, snmpifoperstatus, snmpifalias "+clause);
    }
    
    public DataSource getDataSource() {
        return getJdbcTemplate().getDataSource();
    }
    
    public Object mapRow(ResultSet rs, int rowNumber) throws SQLException {
        
        Integer nodeId = (Integer)rs.getObject("nodeid");  //nodeid                  integer,
        String ipAddr = rs.getString("ipaddr");            //ipaddr                  varchar(16) not null,
        Integer ifIndex = (Integer)rs.getObject("snmpifindex");//snmpifindex                 integer,
        
        final SnmpInterfaceId key = new SnmpInterfaceId(nodeId, ipAddr, ifIndex);
        
        LazySnmpInterface iface = (LazySnmpInterface)Cache.obtain(OnmsSnmpInterface.class, key);
        iface.setLoaded(true);
        
        iface.setIfAdminStatus((Integer)rs.getObject("snmpifadminstatus"));
        iface.setIfAlias(rs.getString("snmpifalias"));
        iface.setIfDescr(rs.getString("snmpifdescr"));
        iface.setIfName(rs.getString("snmpifname"));
        iface.setIfOperStatus((Integer)rs.getObject("snmpifoperstatus"));
        Long ifSpeed = (rs.getObject("snmpifspeed") == null ? null : new Long(rs.getLong("snmpifspeed")));
		iface.setIfSpeed(ifSpeed);
        iface.setIfType((Integer)rs.getObject("snmpiftype"));
        
        iface.setDirty(false);
        return iface;
    }
    
    
    public OnmsSnmpInterface findUnique() {
        return findUnique((Object[])null);
    }
    
    public OnmsSnmpInterface findUnique(Object obj) {
        return findUnique(new Object[] { obj });
    }
    
    public OnmsSnmpInterface findUnique(Object o1, Object o2) {
        return findUnique(new Object[] { o1, o2 });
    }
    
    public OnmsSnmpInterface findUnique(Object o1, Object o2, Object o3) {
        return findUnique(new Object[] { o1, o2, o3 });
    }
    
    public OnmsSnmpInterface findUnique(Object[] objs) {
        List nodes = execute(objs);
        if (nodes.size() > 0)
            return (OnmsSnmpInterface) nodes.get(0);
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