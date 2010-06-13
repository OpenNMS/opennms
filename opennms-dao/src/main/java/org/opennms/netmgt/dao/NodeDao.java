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
package org.opennms.netmgt.dao;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.springframework.stereotype.Repository;

@Repository
public interface NodeDao extends OnmsDao<OnmsNode, Integer> {
	
    /**
     * Get a node based on it's node ID or foreignSource:foreignId
     * @param lookupCriteria the criteria, either the node ID, or a colon-separated string of foreignSource:foreignId
     * @return the node
     */
    OnmsNode get(String lookupCriteria);
    
    Collection<OnmsNode> findByLabel(String label);
    
    Collection<OnmsNode> findNodes(OnmsDistPoller dp);
    
    OnmsNode getHierarchy(Integer id);
    
    Map<String, Integer> getForeignIdToNodeIdMap(String foreignSource);
    
    Collection<OnmsNode> findAllByVarCharAssetColumn(String columnName, String columnValue);
    
    Collection<OnmsNode> findAllByVarCharAssetColumnCategoryList(String columnName, String columnValue,
            Collection<OnmsCategory> categories);
    
    Collection<OnmsNode> findByCategory(OnmsCategory category);
    
    Collection<OnmsNode> findAllByCategoryList(Collection<OnmsCategory> categories);

    Collection<OnmsNode> findAllByCategoryLists(Collection<OnmsCategory> rowCatNames, Collection<OnmsCategory> colCatNames);
    
    /**
     * Returns a list of nodes ordered by label.
     */
    List<OnmsNode> findAll();

    List<OnmsNode> findByForeignSource(String foreignSource);
    
    OnmsNode findByForeignId(String foreignSource, String foreignId);

    int getNodeCountForForeignSource(String groupName);
    
    List<OnmsNode> findAllProvisionedNodes();
    
    List<OnmsIpInterface> findObsoleteIpInterfaces(Integer nodeId, Date scanStamp);

    void deleteObsoleteInterfaces(Integer nodeId, Date scanStamp);

    void updateNodeScanStamp(Integer nodeId, Date scanStamp);

    Collection<Integer> getNodeIds();

    List<OnmsNode> findByForeignSourceAndIpAddress(String foreignSource, String ipAddress);
}
