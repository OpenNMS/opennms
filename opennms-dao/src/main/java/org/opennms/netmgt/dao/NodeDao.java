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
/**
 * <p>NodeDao interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface NodeDao extends OnmsDao<OnmsNode, Integer> {
	
    /**
     * Get a node based on it's node ID or foreignSource:foreignId
     *
     * @param lookupCriteria the criteria, either the node ID, or a colon-separated string of foreignSource:foreignId
     * @return the node
     */
    OnmsNode get(String lookupCriteria);
    
    /**
     * <p>findByLabel</p>
     *
     * @param label a {@link java.lang.String} object.
     * @return a {@link java.util.Collection} object.
     */
    Collection<OnmsNode> findByLabel(String label);
    
    /**
     * <p>findNodes</p>
     *
     * @param dp a {@link org.opennms.netmgt.model.OnmsDistPoller} object.
     * @return a {@link java.util.Collection} object.
     */
    Collection<OnmsNode> findNodes(OnmsDistPoller dp);
    
    /**
     * <p>getHierarchy</p>
     *
     * @param id a {@link java.lang.Integer} object.
     * @return a {@link org.opennms.netmgt.model.OnmsNode} object.
     */
    OnmsNode getHierarchy(Integer id);
    
    /**
     * <p>getForeignIdToNodeIdMap</p>
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @return a {@link java.util.Map} object.
     */
    Map<String, Integer> getForeignIdToNodeIdMap(String foreignSource);
    
    /**
     * <p>findAllByVarCharAssetColumn</p>
     *
     * @param columnName a {@link java.lang.String} object.
     * @param columnValue a {@link java.lang.String} object.
     * @return a {@link java.util.Collection} object.
     */
    Collection<OnmsNode> findAllByVarCharAssetColumn(String columnName, String columnValue);
    
    /**
     * <p>findAllByVarCharAssetColumnCategoryList</p>
     *
     * @param columnName a {@link java.lang.String} object.
     * @param columnValue a {@link java.lang.String} object.
     * @param categories a {@link java.util.Collection} object.
     * @return a {@link java.util.Collection} object.
     */
    Collection<OnmsNode> findAllByVarCharAssetColumnCategoryList(String columnName, String columnValue,
            Collection<OnmsCategory> categories);
    
    /**
     * <p>findByCategory</p>
     *
     * @param category a {@link org.opennms.netmgt.model.OnmsCategory} object.
     * @return a {@link java.util.Collection} object.
     */
    Collection<OnmsNode> findByCategory(OnmsCategory category);
    
    /**
     * <p>findAllByCategoryList</p>
     *
     * @param categories a {@link java.util.Collection} object.
     * @return a {@link java.util.Collection} object.
     */
    Collection<OnmsNode> findAllByCategoryList(Collection<OnmsCategory> categories);

    /**
     * <p>findAllByCategoryLists</p>
     *
     * @param rowCatNames a {@link java.util.Collection} object.
     * @param colCatNames a {@link java.util.Collection} object.
     * @return a {@link java.util.Collection} object.
     */
    Collection<OnmsNode> findAllByCategoryLists(Collection<OnmsCategory> rowCatNames, Collection<OnmsCategory> colCatNames);
    
    /**
     * Returns a list of nodes ordered by label.
     *
     * @return a {@link java.util.List} object.
     */
    List<OnmsNode> findAll();

    /**
     * <p>findByForeignSource</p>
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @return a {@link java.util.List} object.
     */
    List<OnmsNode> findByForeignSource(String foreignSource);
    
    /**
     * <p>findByForeignId</p>
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @param foreignId a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.OnmsNode} object.
     */
    OnmsNode findByForeignId(String foreignSource, String foreignId);

    /**
     * <p>getNodeCountForForeignSource</p>
     *
     * @param groupName a {@link java.lang.String} object.
     * @return a int.
     */
    int getNodeCountForForeignSource(String groupName);
    
    /**
     * <p>findAllProvisionedNodes</p>
     *
     * @return a {@link java.util.List} object.
     */
    List<OnmsNode> findAllProvisionedNodes();
    
    /**
     * <p>findObsoleteIpInterfaces</p>
     *
     * @param nodeId a {@link java.lang.Integer} object.
     * @param scanStamp a {@link java.util.Date} object.
     * @return a {@link java.util.List} object.
     */
    List<OnmsIpInterface> findObsoleteIpInterfaces(Integer nodeId, Date scanStamp);

    /**
     * <p>deleteObsoleteInterfaces</p>
     *
     * @param nodeId a {@link java.lang.Integer} object.
     * @param scanStamp a {@link java.util.Date} object.
     */
    void deleteObsoleteInterfaces(Integer nodeId, Date scanStamp);

    /**
     * <p>updateNodeScanStamp</p>
     *
     * @param nodeId a {@link java.lang.Integer} object.
     * @param scanStamp a {@link java.util.Date} object.
     */
    void updateNodeScanStamp(Integer nodeId, Date scanStamp);

    /**
     * <p>getNodeIds</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    Collection<Integer> getNodeIds();

    /**
     * <p>findByForeignSourceAndIpAddress</p>
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @param ipAddress a {@link java.lang.String} object.
     * @return a {@link java.util.List} object.
     */
    List<OnmsNode> findByForeignSourceAndIpAddress(String foreignSource, String ipAddress);
}
