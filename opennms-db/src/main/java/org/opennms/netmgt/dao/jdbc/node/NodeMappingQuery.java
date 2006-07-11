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
package org.opennms.netmgt.dao.jdbc.node;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.opennms.netmgt.dao.jdbc.Cache;
import org.opennms.netmgt.dao.jdbc.JdbcSet;
import org.opennms.netmgt.dao.jdbc.LazySet;
import org.opennms.netmgt.dao.jdbc.category.FindCategoriesByNode;
import org.opennms.netmgt.dao.jdbc.ipif.FindByNode;
import org.opennms.netmgt.model.OnmsAssetRecord;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.OnmsNode;
import org.springframework.jdbc.object.MappingSqlQuery;

public class NodeMappingQuery extends MappingSqlQuery {

    public NodeMappingQuery(DataSource ds, String clause) {
        super(ds, "SELECT n.nodeid as nodeid, n.dpName as dpName, n.nodeCreateTime as nodeCreateTime, n.nodeParentID as nodeParentID, n.nodeType as nodeType, n.nodeSysOid as nodeSysOid, n.nodeSysName as nodeSysName, n.nodeSysDescription as nodeSysDescription, n.nodeSysLocation as nodeSysLocation, n.nodeSysContact as nodeSysContact, n.nodeLabel as nodeLabel, n.nodeLabelSource as nodeLabelSource, n.nodeNetBiosName as nodeNetBiosName, n.nodeDomainName as nodeDomainName, n.operatingSystem as operatingSystem, n.lastCapsdPoll as lastCapsdPoll "+clause);
    }
    
    public DataSource getDataSource() {
        return getJdbcTemplate().getDataSource();
    }

    public Object mapRow(ResultSet rs, int rowNumber) throws SQLException {
        final Integer id = (Integer) rs.getObject("nodeid");

        LazyNode node = (LazyNode)Cache.obtain(OnmsNode.class, id);
        node.setLoaded(true);
        
        String dpName = rs.getString("dpName");
        OnmsDistPoller distPoller = (OnmsDistPoller)Cache.obtain(OnmsDistPoller.class, dpName);

        node.setDistPoller(distPoller);
        
        OnmsAssetRecord asset = (OnmsAssetRecord)Cache.obtain(OnmsAssetRecord.class, id);
        node.setAssetRecord(asset);
        
        Integer parentId = (Integer)rs.getObject("nodeParentID");
        if (parentId == null) {
        	node.setParent(null);
        } else {
        	OnmsNode parent = (OnmsNode)Cache.obtain(OnmsNode.class, parentId);
        	node.setParent(parent);
        }

        node.setCreateTime(rs.getTime("nodeCreateTime"));
        node.setType(rs.getString("nodeType"));
        node.setSysObjectId(rs.getString("nodeSysOid"));
        node.setSysName(rs.getString("nodeSysName"));
        node.setSysDescription((rs.getString("nodeSysDescription")));
        node.setSysLocation(rs.getString("nodeSysLocation"));
        node.setSysContact(rs.getString("nodeSysContact"));
        node.setLabel(rs.getString("nodeLabel"));
        node.setLabelSource(rs.getString("nodeLabelSource"));
        node.setNetBiosName(rs.getString("nodeNetBiosName"));
        node.setNetBiosDomain(rs.getString("nodeDomainName"));;
        node.setOperatingSystem(rs.getString("operatingSystem"));
        node.setLastCapsdPoll(rs.getTime("lastCapsdPoll"));
        
        LazySet.Loader ifLoader = new LazySet.Loader() {

			public Set load() {
				return new FindByNode(getDataSource()).findSet(id);
			}
        	
        };
        
        node.setIpInterfaces(new LazySet(ifLoader));
        
        LazySet.Loader catLoader = new LazySet.Loader() {
            public Set load() {
                return new FindCategoriesByNode(getDataSource()).findSet(id);
            }
        };
        node.setCategories(new LazySet(catLoader));
        
        LazySet.Loader snmpIfLoader = new LazySet.Loader() {
        	public Set load() {
        		return new org.opennms.netmgt.dao.jdbc.snmpif.FindByNode(getDataSource()).findSet(id);
        	}
        };
        node.setSnmpInterfaces(new LazySet(snmpIfLoader));
        
        node.setDirty(false);
        return node;
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
    
    public Set findSet(Object[] objs) {
        List nodes = execute(objs);
        Set results = new JdbcSet(nodes);
        return results;
    }
    
}