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
package org.opennms.netmgt.dao.jdbc.agent;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.opennms.netmgt.dao.jdbc.Cache;
import org.opennms.netmgt.dao.jdbc.JdbcSet;
import org.opennms.netmgt.model.OnmsAgent;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsServiceType;
import org.springframework.jdbc.object.MappingSqlQuery;

public class AgentMappingQuery extends MappingSqlQuery {
	
	// NODE ALL Classes the extend this must joing node, ipInterface, and ifservices also the 'where' must include isSnmpPrimary = 'P'

    public AgentMappingQuery(DataSource ds, String clause) {
        super(ds, "SELECT node.nodeid as agentNodeid, node.nodeSysOid as agentSysOid, node.nodeSysName as agentSysName, node.nodeSysDescription as agentSysDescription, node.nodeSysLocation as agentSysLocation, node.nodeSysContact as agentSysContact, ipInterface.ipAddr as agentAddress, ifservices.serviceid as agentServiceId "+clause);
    }
    
    public DataSource getDataSource() {
        return getJdbcTemplate().getDataSource();
    }

    public Object mapRow(ResultSet rs, int rowNumber) throws SQLException {
    	
    	// FIXME: Right now an agent is also an SnmpAgent.  That's not correct for
    	// JMX agents especially when we put the datacollection config references here.
    	// Need to provide different subclasses of agent based on ServiceType.
    	//
    	// This may not be the optimum way to implemenet this.  more to come later
    	
    	
    	// NOTE: At the moment this is no agent table.  These agents are 'derived' from a
    	// join on the node, ipinterface, ifservices tables where isSnmpPrimary = 'P'.  
    	// An agent is defined a collectable service responding on the primary interface for a node.
    	// the id of the agent is the nodeid of the node it belongs too.  This definition currently
    	// implies that there can be at most one agent of a given serviceType on a node. 
    
        final Integer id = (Integer) rs.getObject("agentNodeid");
        
        LazyAgent agent = (LazyAgent)Cache.obtain(OnmsAgent.class, id);
        agent.setLoaded(true);
        
        OnmsNode node = (OnmsNode)Cache.obtain(OnmsNode.class, id);
        agent.setNode(node);	//        "node.nodeid as agentNodeid, " +
        
        Integer svcId = (Integer) rs.getObject("agentServiceId");
        OnmsServiceType svcType = (OnmsServiceType)Cache.obtain(OnmsServiceType.class, svcId);
        agent.setServiceType(svcType); // "ifservices.serviceid as agentServiceId"
        
        agent.setSysObjectId(rs.getString("agentSysOid"));	//        "node.nodeSysOid as agentSysOid, " +
        agent.setSysName(rs.getString("agentSysName"));	//        "node.nodeSysName as agentSysName, " +
        agent.setSysDescription(rs.getString("agentSysDescription"));	//        "node.nodeSysDescription as agentSysDescription, " +
        agent.setSysLocation(rs.getString("agentSysLocation"));	//        "node.nodeSysLocation as agentSysLocation, " +
        agent.setSysContact(rs.getString("agentSysContact"));	//        "node.nodeSysContact as agentSysContact, " +
        agent.setIpAddress(rs.getString("agentAddress"));	//        "ipInterface.ipAddr as agentAddress, " +

        // TODO: add code to load/lazyLoad the SnmpAgentConfig
        
        // TODO: add code load/lazyLoad the DataCollection Attributes associated with this agent
        
//        LazySet.Loader ifLoader = new LazySet.Loader() {
//
//			public Set load() {
//				return new FindByNode(getDataSource()).findSet(id);
//			}
//        	
//        };
//        
//        agent.setIpInterfaces(new LazySet(ifLoader));
//        
//        LazySet.Loader catLoader = new LazySet.Loader() {
//            public Set load() {
//                return new FindCategoriesByNode(getDataSource()).findSet(id);
//            }
//        };
//        agent.setCategories(new LazySet(catLoader));
//        
//        LazySet.Loader snmpIfLoader = new LazySet.Loader() {
//        	public Set load() {
//        		return new org.opennms.netmgt.dao.jdbc.snmpif.FindByNode(getDataSource()).findSet(id);
//        	}
//        };
//        agent.setSnmpInterfaces(new LazySet(snmpIfLoader));
        
        agent.setDirty(false);
        return agent;
    }
    
    public OnmsNode findUnique() {
        return findUnique((Object[])null);
    }
    
    public OnmsNode findUnique(Object obj) {
        return findUnique(new Object[] { obj });
    }

    public OnmsNode findUnique(Object[] objs) {
        List nodes = execute(objs);
        if (nodes.size() > 0)
            return (OnmsNode) nodes.get(0);
        else
            return null;
    }
    
    public Set findSet() {
        return findSet((Object[])null);
    }
    
    public Set findSet(Object obj) {
        return findSet(new Object[] { obj });
    }
    
    public Set findSet(Object obj1, Object obj2) {
    	return findSet(new Object[] { obj1, obj2 });
    }
    
    public Set findSet(Object[] objs) {
        List nodes = execute(objs);
        Set results = new JdbcSet(nodes);
        return results;
    }
    
}