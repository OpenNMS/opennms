//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc. All rights
// reserved.
// OpenNMS(R) is a derivative work, containing both original code, included
// code and modified
// code that was published under the GNU General Public License. Copyrights
// for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2007 Apr 10: Cleanup imports. - dj@opennms.org
//
// Original code base Copyright (C) 1999-2001 Oculan Corp. All rights
// reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//    
// For more information contact:
// OpenNMS Licensing <license@opennms.org>
// http://www.opennms.org/
// http://www.opennms.com/
//

package org.opennms.netmgt.dao.hibernate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.springframework.util.StringUtils;

/**
 * @author Ted Kazmark
 * @author David Hustace
 */
public class NodeDaoHibernate extends AbstractDaoHibernate<OnmsNode, Integer>
        implements NodeDao {

    public NodeDaoHibernate() {
        super(OnmsNode.class);
    }

    public OnmsNode get(String lookupCriteria) {
        if (lookupCriteria.contains(":")) {
            String[] criteria = lookupCriteria.split(":");
            return findByForeignId(criteria[0], criteria[1]);
        }
        return get(Integer.parseInt(lookupCriteria));
    }

    public Collection<OnmsNode> findNodes(final OnmsDistPoller distPoller) {
        return find("from OnmsNode where distPoller = ?", distPoller);
    }

    public OnmsNode getHierarchy(Integer id) {
        OnmsNode node = findUnique(
                          "select distinct n from OnmsNode as n "
                                  + "left join fetch n.assetRecord "
                                  + "where n.id = ?", id);
        
        initialize(node.getIpInterfaces());
        for (OnmsIpInterface i : node.getIpInterfaces()) {
            initialize(i.getMonitoredServices());
        }
        
        initialize(node.getSnmpInterfaces());
        for (OnmsSnmpInterface i : node.getSnmpInterfaces()) {
            initialize(i.getIpInterfaces());
        }
        
        return node;

    }

    public Collection<OnmsNode> findByLabel(String label) {
        return find("from OnmsNode as n where n.label = ?", label);
    }

    public Collection<OnmsNode> findAllByVarCharAssetColumn(
            String columnName, String columnValue) {
        return find("from OnmsNode as n where n.assetRecord." + columnName
                + " = ?", columnValue);
    }

    public Collection<OnmsNode> findAllByVarCharAssetColumnCategoryList(
            String columnName, String columnValue,
            Collection<OnmsCategory> categories) {
    	
        return find("select distinct n from OnmsNode as n "
        		+ "join n.categories as c "
                + "left join fetch n.assetRecord "
                + "left join fetch n.ipInterfaces as iface "
                + "left join fetch iface.monitoredServices as monSvc "
                + "left join fetch monSvc.serviceType "
                + "left join fetch monSvc.currentOutages "
                + "where n.assetRecord." + columnName + " = ? "
                + "and c.name in ("+categoryListToNameList(categories)+")", columnValue);
    }

    public Collection<OnmsNode> findByCategory(OnmsCategory category) {
        return find("select distinct n from OnmsNode as n "
                    + "join n.categories c "
                    + "left join fetch n.assetRecord "
                    + "left join fetch n.ipInterfaces as iface "
                    + "left join fetch iface.monitoredServices as monSvc "
                    + "left join fetch monSvc.serviceType "
                    + "left join fetch monSvc.currentOutages "
                    + "where c.name = ?",
                    category.getName());
    }

	private String categoryListToNameList(Collection<OnmsCategory> categories) {
		List<String> categoryNames = new ArrayList<String>();
    	for (OnmsCategory category : categories) {
			categoryNames.add(category.getName());
		}
		return StringUtils.collectionToDelimitedString(categoryNames, ", ", "'", "'");
	}
        
        

    public Collection<OnmsNode> findAllByCategoryList(
            Collection<OnmsCategory> categories) {
        return find("select distinct n from OnmsNode as n "
                + "join n.categories c " 
                + "left join fetch n.assetRecord "
                + "left join fetch n.ipInterfaces as iface "
                + "left join fetch iface.monitoredServices as monSvc "
                + "left join fetch monSvc.serviceType "
                + "left join fetch monSvc.currentOutages "
                + "where c.name in ("+categoryListToNameList(categories)+")"
                + "and n.type != 'D'");
    }

    public Collection<OnmsNode> findAllByCategoryLists( Collection<OnmsCategory> rowCatNames, Collection<OnmsCategory> colCatNames) {
    	
    	HashSet<OnmsNode> rowNodes = new HashSet<OnmsNode>(findAllByCategoryList(rowCatNames));
    	HashSet<OnmsNode> colNodes = new HashSet<OnmsNode>(findAllByCategoryList(colCatNames));
    	
    	HashSet<OnmsNode> results = new HashSet<OnmsNode>(rowNodes);
    	results.retainAll(colNodes);
    	
    	return results;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Integer> getForeignIdToNodeIdMap(String foreignSource) {
        List<Object[]> pairs = getHibernateTemplate().find("select n.id, n.foreignId from OnmsNode n where n.foreignSource = ?", foreignSource);
        Map<String, Integer> foreignIdMap = new HashMap<String, Integer>();
        for (Object[] pair : pairs) {
            foreignIdMap.put((String)pair[1], (Integer)pair[0]);
        }
        return foreignIdMap;
    }

    public List<OnmsNode> findByForeignSource(String foreignSource) {
        return find("from OnmsNode n where n.foreignSource = ?", foreignSource);
    }

    public OnmsNode findByForeignId(String foreignSource, String foreignId) {
        return findUnique("from OnmsNode n where n.foreignSource = ? and n.foreignId = ?", foreignSource, foreignId);
    }
    
    public List<OnmsNode> findByForeignSourceAndIpAddress(String foreignSource, String ipAddress) {
        if (foreignSource == null) {
            return find("select distinct n from OnmsNode n join n.ipInterfaces as ip where n.foreignSource is NULL and ip.ipAddress = ?", ipAddress);
        } else {
            return find("select distinct n from OnmsNode n join n.ipInterfaces as ip where n.foreignSource = ? and ip.ipAddress = ?", foreignSource, ipAddress);
        }
    }

    public int getNodeCountForForeignSource(String foreignSource) {
        return queryInt("select count(*) from OnmsNode as n where n.foreignSource = ?", foreignSource);
    }
    
    public List<OnmsNode> findAll() {
        return find("from OnmsNode order by label");
    }
    
    public List<OnmsNode> findAllProvisionedNodes() {
        return find("from OnmsNode n where n.foreignSource is not null");
    }
    
    public List<OnmsIpInterface> findObsoleteIpInterfaces(Integer nodeId, Date scanStamp) {
        return findObjects(OnmsIpInterface.class, "from OnmsIpInterface iface where iface.node.id = ? and (iface.ipLastCapsdPoll is null or iface.ipLastCapsdPoll < ?)", nodeId, scanStamp);
    }

    public void deleteObsoleteInterfaces(Integer nodeId, Date scanStamp) {
        getHibernateTemplate().bulkUpdate("delete from OnmsIpInterface iface where iface.node.id = ? and (iface.ipLastCapsdPoll is null or iface.ipLastCapsdPoll < ?)", new Object[] { nodeId, scanStamp });
        getHibernateTemplate().bulkUpdate("delete from OnmsSnmpInterface iface where iface.node.id = ? and (iface.lastCapsdPoll is null or iface.lastCapsdPoll < ?)", new Object[] { nodeId, scanStamp });
    }

    public void updateNodeScanStamp(Integer nodeId, Date scanStamp) {
        OnmsNode n = get(nodeId);
        n.setLastCapsdPoll(scanStamp);
        update(n);
    }

    public Collection<Integer> getNodeIds() {
        return findObjects(Integer.class, "select distinct n.id from OnmsNode as n where n.type != 'D'");
    }

    
    


}