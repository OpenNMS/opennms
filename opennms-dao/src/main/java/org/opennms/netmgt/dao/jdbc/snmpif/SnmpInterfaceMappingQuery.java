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
        
        super(ds, "SELECT " +
        		"snmpinterface.nodeid as snmp_nodeid, " +
        		"snmpinterface.ipaddr as snmp_ipaddr, " +
                "snmpinterface.snmpifindex as snmp_snmpifindex, " +
                "snmpinterface.snmpipadentnetmask as snmp_snmpipadentnetmask, " +
                "snmpinterface.snmpphysaddr as snmp_snmpphysaddr, " +
                "snmpinterface.snmpifdescr as snmp_snmpifdescr, " +
                "snmpinterface.snmpiftype as snmp_snmpiftype, " +
                "snmpinterface.snmpifname as snmp_snmpifname, " +
                "snmpinterface.snmpifspeed as snmp_snmpifspeed, " +
                "snmpinterface.snmpifadminstatus as snmp_snmpifadminstatus, " +
                "snmpinterface.snmpifoperstatus as snmp_snmpifoperstatus, " +
                "snmpinterface.snmpifalias as snmp_snmpifalias " +
                clause);
    }
    
    public DataSource getDataSource() {
        return getJdbcTemplate().getDataSource();
    }
    
    public Object mapRow(ResultSet rs, int rowNumber) throws SQLException {
        
        Integer nodeId = (Integer)rs.getObject("snmp_nodeid");  //nodeid                  integer,
        String ipAddr = rs.getString("snmp_ipaddr");            //ipaddr                  varchar(16) not null,
        Integer ifIndex = (Integer)rs.getObject("snmp_snmpifindex");//snmpifindex                 integer,
        
        final SnmpInterfaceId key = new SnmpInterfaceId(nodeId, ipAddr, ifIndex);
        
        LazySnmpInterface iface = (LazySnmpInterface)Cache.obtain(OnmsSnmpInterface.class, key);
        iface.setLoaded(true);
        
        iface.setNetMask(rs.getString("snmp_snmpipadentnetmask"));//"snmpinterface.snmpipadentnetmask as snmp_snmpipadentnetmask, " +
        iface.setPhysAddr(rs.getString("snmp_snmpphysaddr"));//"snmpinterface.snmpphysaddr as snmp_snmpphysaddr, " +
        iface.setIfDescr(rs.getString("snmp_snmpifdescr"));//"snmpinterface.snmpifdescr as snmp_snmpifdescr, " +
        iface.setIfType((Integer)rs.getObject("snmp_snmpiftype"));//"snmpinterface.snmpiftype as snmp_snmpiftype, " +
        iface.setIfName(rs.getString("snmp_snmpifname"));//"snmpinterface.snmpifname as snmp_snmpifname, " +
        
        long ifSpeed = rs.getLong("snmp_snmpifspeed");
        iface.setIfSpeed(rs.wasNull() ? null : new Long(ifSpeed));//"snmpinterface.snmpifspeed as snmp_snmpifspeed, " +
        iface.setIfAdminStatus((Integer)rs.getObject("snmp_snmpifadminstatus"));//"snmpinterface.snmpifadminstatus as snmp_snmpifadminstatus, " +
        iface.setIfOperStatus((Integer)rs.getObject("snmp_snmpifoperstatus"));//"snmpinterface.snmpifoperstatus as snmp_snmpifoperstatus, " +
        iface.setIfAlias(rs.getString("snmp_snmpifalias"));//"snmpinterface.snmpifalias as snmp_snmpifalias " +

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