/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.svclayer;

import java.util.Collection;
import java.util.Map;

import org.opennms.netmgt.provision.persist.requisition.Requisition;

/**
 * <p>ManualProvisioningService interface.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 * @since 1.8.1
 */
public interface ManualProvisioningService {
    
    /**
     * <p>getProvisioningGroupNames</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    Collection<String> getProvisioningGroupNames();
    
    /**
     * <p>getProvisioningGroup</p>
     *
     * @param name a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.persist.requisition.Requisition} object.
     */
    Requisition getProvisioningGroup(String name);
    
    /**
     * <p>createProvisioningGroup</p>
     *
     * @param name a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.persist.requisition.Requisition} object.
     */
    Requisition createProvisioningGroup(String name);

    /**
     * <p>getServiceTypeNames</p>
     *
     * @param groupName finds the services that are defined in the foreignsource for this group
     * @return a {@link java.util.Collection} object.
     */
    Collection<String> getServiceTypeNames(String groupName);

    /**
     * <p>saveProvisioningGroup</p>
     *
     * @param groupName a {@link java.lang.String} object.
     * @param groupData a {@link org.opennms.netmgt.provision.persist.requisition.Requisition} object.
     * @return a {@link org.opennms.netmgt.provision.persist.requisition.Requisition} object.
     */
    Requisition saveProvisioningGroup(String groupName, Requisition groupData);
    
    /**
     * <p>addNewNodeToGroup</p>
     *
     * @param groupName a {@link java.lang.String} object.
     * @param nodeLabel a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.persist.requisition.Requisition} object.
     */
    Requisition addNewNodeToGroup(String groupName, String nodeLabel);
    
    /**
     * <p>addCategoryToNode</p>
     *
     * @param groupName a {@link java.lang.String} object.
     * @param pathToNode a {@link java.lang.String} object.
     * @param categoryName a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.persist.requisition.Requisition} object.
     */
    Requisition addCategoryToNode(String groupName, String pathToNode, String categoryName);
    
    /**
     * <p>addAssetFieldToNode</p>
     *
     * @param groupName a {@link java.lang.String} object.
     * @param pathToNode a {@link java.lang.String} object.
     * @param fieldName a {@link java.lang.String} object.
     * @param fieldValue a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.persist.requisition.Requisition} object.
     */
    Requisition addAssetFieldToNode(String groupName, String pathToNode, String fieldName, String fieldValue);
    
    /**
     * <p>addInterfaceToNode</p>
     *
     * @param groupName a {@link java.lang.String} object.
     * @param pathToNode a {@link java.lang.String} object.
     * @param ipAddr a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.persist.requisition.Requisition} object.
     */
    Requisition addInterfaceToNode(String groupName, String pathToNode, String ipAddr);
    
    /**
     * <p>addServiceToInterface</p>
     *
     * @param groupName a {@link java.lang.String} object.
     * @param pathToInterface a {@link java.lang.String} object.
     * @param serviceName a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.persist.requisition.Requisition} object.
     */
    Requisition addServiceToInterface(String groupName, String pathToInterface, String serviceName);
    
    /**
     * <p>deletePath</p>
     *
     * @param groupName a {@link java.lang.String} object.
     * @param pathToDelete a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.persist.requisition.Requisition} object.
     */
    Requisition deletePath(String groupName, String pathToDelete);
    
    /**
     * <p>importProvisioningGroup</p>
     *
     * @param groupName a {@link java.lang.String} object.
     */
    void importProvisioningGroup(String groupName);

    /**
     * <p>getAllGroups</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    Collection<Requisition> getAllGroups();

    /**
     * <p>deleteProvisioningGroup</p>
     *
     * @param groupName a {@link java.lang.String} object.
     */
    void deleteProvisioningGroup(String groupName);

    /**
     * <p>deleteAllNodes</p>
     *
     * @param groupName a {@link java.lang.String} object.
     */
    void deleteAllNodes(String groupName);

    /**
     * <p>getGroupDbNodeCounts</p>
     *
     * @return a java$util$Map object.
     */
    Map<String, Integer> getGroupDbNodeCounts();

    /**
     * <p>getNodeCategoryNames</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    Collection<String> getNodeCategoryNames();

    /**
     * <p>getAssetFieldNames</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    Collection<String> getAssetFieldNames();

}
