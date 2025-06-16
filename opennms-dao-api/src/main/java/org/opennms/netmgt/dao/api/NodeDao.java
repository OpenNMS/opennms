/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.dao.api;

import java.net.InetAddress;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.SurveillanceStatus;
import org.springframework.stereotype.Repository;

@Repository
/**
 * <p>NodeDao interface.</p>
 */
public interface NodeDao extends LegacyOnmsDao<OnmsNode, Integer> {
	
    /**
     * Get a node based on it's node ID or foreignSource:foreignId
     *
     * @param lookupCriteria the criteria, either the node ID, or a colon-separated string of foreignSource:foreignId
     * @return the node
     */
    OnmsNode get(String lookupCriteria);

    /**
     * Retrieves all of node id/label tuples.
     *
     * Can be used as a lightweight alternative to findAll().
     *
     * @return a {@link java.util.Map} containing all node ids and their associated labels.
     */
    Map<Integer, String> getAllLabelsById();

    /**
     * Light weight call to simply get the node label without loading the entire node.
     * 
     * @param id
     * @return A String representing the provisioned label for the node.  Returns null if not found.
     */
    String getLabelForId(Integer id);

    /**
     * Light weight call to simply get the node location without loading the entire node.
     *
     * @param id
     * @return A String representing the provisioned label for the node.  Returns null if not found.
     */
    String getLocationForId(Integer id);

    /**
     * <p>findByLabel</p>
     *
     * @param label a {@link java.lang.String} object.
     * @return a {@link java.util.Collection} object.
     */
    List<OnmsNode> findByLabel(String label);
    
    /**
     * <p>findByLabel</p>
     *
     * @param label a {@link java.lang.String} object.
     * @param location a {@link java.lang.String} object.
     * @return a {@link java.util.Collection} object.
     */
    List<OnmsNode> findByLabelForLocation(String label, String location);
    
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
     * <p>getForeignIdsPerForeignSourceMap</p>
     *
     * @return a {@link java.util.Map} object.
     */
    Map<String, Set<String>> getForeignIdsPerForeignSourceMap();

    /**
     * <p>getForeignIdsPerForeignSource</p>
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @return a {@link java.util.Set} object.
     */
    Set<String> getForeignIdsPerForeignSource(String foreignSource);

    /**
     * <p>findAllByVarCharAssetColumn</p>
     *
     * @param columnName a {@link java.lang.String} object.
     * @param columnValue a {@link java.lang.String} object.
     * @return a {@link java.util.Collection} object.
     */
    List<OnmsNode> findAllByVarCharAssetColumn(String columnName, String columnValue);
    
    /**
     * <p>findAllByVarCharAssetColumnCategoryList</p>
     *
     * @param columnName a {@link java.lang.String} object.
     * @param columnValue a {@link java.lang.String} object.
     * @param categories a {@link java.util.Collection} object.
     * @return a {@link java.util.Collection} object.
     */
    List<OnmsNode> findAllByVarCharAssetColumnCategoryList(String columnName, String columnValue,
            Collection<OnmsCategory> categories);
    
    /**
     * <p>findByCategory</p>
     *
     * @param category a {@link org.opennms.netmgt.model.OnmsCategory} object.
     * @return a {@link java.util.Collection} object.
     */
    List<OnmsNode> findByCategory(OnmsCategory category);
    
    /**
     * <p>findAllByCategoryList</p>
     *
     * @param categories a {@link java.util.Collection} object.
     * @return a {@link java.util.Collection} object.
     */
    List<OnmsNode> findAllByCategoryList(Collection<OnmsCategory> categories);

    /**
     * <p>findAllByCategoryLists</p>
     *
     * @param rowCatNames a {@link java.util.Collection} object.
     * @param colCatNames a {@link java.util.Collection} object.
     * @return a {@link java.util.Collection} object.
     */
    List<OnmsNode> findAllByCategoryLists(Collection<OnmsCategory> rowCatNames, Collection<OnmsCategory> colCatNames);
    
    /**
     * Returns a list of nodes ordered by label.
     *
     * @return a {@link java.util.List} object.
     */
    @Override
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
     * <p>findByForeignId</p>
     *
     * @param foreignId a {@link java.lang.String} object.
     * @return a {@link java.util.List} object.
     */
    List<OnmsNode> findByForeignId(String foreignId);
    
    /**
     * <p>findByForeignIdForLocation</p>
     *
     * @param foreignId a {@link java.lang.String} object.
     * @param location a {@link java.lang.String} object.
     * @return a {@link java.util.List} object.
     */
    List<OnmsNode> findByForeignIdForLocation(String foreignId, String location);
    

    List<OnmsNode> findByIpAddressAndService(InetAddress ipAddress, String serviceName);

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

    /**
     * Retrieves the number of nodes for each sysOid.
     *
     * @return a {@link java.util.Map} containing the number of nodes for each sysOid
     */
    Map<String, Long> getNumberOfNodesBySysOid();

    SurveillanceStatus findSurveillanceStatusByCategoryLists(Collection<OnmsCategory> rowCategories, Collection<OnmsCategory> columnCategories);

    Integer getNextNodeId (Integer nodeId);

    Integer getPreviousNodeId (Integer nodeId);

    void markHavingFlows(final Collection<Integer> ingressIds, final Collection<Integer> egressIds);

    List<OnmsNode> findAllHavingFlows();

    List<OnmsNode> findAllHavingIngressFlows();

    List<OnmsNode> findAllHavingEgressFlows();

    OnmsNode getDefaultFocusPoint();

    List<OnmsNode> findNodeWithMetaData(final String context, final String key, final String value, final boolean matchEnumeration);

    default List<OnmsNode> findNodeWithMetaData(final String context, final String key, final String value) {
        return findNodeWithMetaData(context, key, value, false);
    }

    /**
     * Returns all OnmsNodes that have a sysname that matches remSysName of an lldp link that is related to the given
     * node. Used to retrieve all OnmsNodes that need to be accessed when finding the lldp links of a node.
     */
    List<OnmsNode> findBySysNameOfLldpLinksOfNode(int nodeId);
}
