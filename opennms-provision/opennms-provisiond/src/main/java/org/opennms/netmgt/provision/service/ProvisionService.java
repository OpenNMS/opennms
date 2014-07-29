/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.provision.service;

import java.net.InetAddress;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.provision.IpInterfacePolicy;
import org.opennms.netmgt.provision.NodePolicy;
import org.opennms.netmgt.provision.ServiceDetector;
import org.opennms.netmgt.provision.SnmpInterfacePolicy;
import org.opennms.netmgt.provision.persist.ForeignSourceRepository;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.springframework.core.io.Resource;
import org.springframework.transaction.annotation.Transactional;

/*
 * ProvisionService
 * @author brozow
 */
/**
 * <p>ProvisionService interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface ProvisionService {
    
	
    boolean isRequisitionedEntityDeletionEnabled();

    /**
     * <p>isDiscoveryEnabled</p>
     *
     * @return a boolean.
     */
    boolean isDiscoveryEnabled();
    
    /**
     * Clear the Hibernate object cache. This is used to clear the object
     * cache created by Hibernate. This is needed so large imports don't end
     * up caching the entire database when it has no intention of using a
     * node's data again. This is needed only to help memory performance.
     */
    void clearCache();

    /**
     * Lookup a distPoller in the database, creating it if necessary. This
     * method looks up the OnmsDistPoller object with the name 'dpName' in the
     * database and returns it. If there is not distPoller with that name that
     * one is created using the name and the address provided, saved in the
     * database, and returned.
     *
     * @param dpName
     *            The name of the distPoller that is needed
     * @param dpAddr
     *            The address to give the new distPoller if it is necessary to
     *            create one
     * @return a new distPoller that will be saved to the database when the
     *         transaction is committed.
     */
    @Transactional
    OnmsDistPoller createDistPollerIfNecessary(String dpName, String dpAddr);

    /**
     * Update the database entry for the given node. The node supplied is used
     * to update the database. Entries that have been change in the node are
     * copied into the database. It is assumed that the node passed in has
     * been previously loaded from the database and modified.
     *
     * @param node
     *            The node that has been updated and should be written to the
     *            database
     * @param rescanExisting
     *            true, if the node must be rescanned.
     */
    @Transactional
    void updateNode(OnmsNode node, boolean rescanExisting);
    
    /**
     * <p>updateNodeAttributes</p>
     *
     * @param node a {@link org.opennms.netmgt.model.OnmsNode} object.
     * @return a {@link org.opennms.netmgt.model.OnmsNode} object.
     */
    @Transactional
    OnmsNode updateNodeAttributes(OnmsNode node);
   
    /**
     * <p>getDbNodeInitCat</p>
     *
     * @param nodeId a {@link java.lang.Integer} object.
     * @return a {@link org.opennms.netmgt.model.OnmsNode} object.
     */
    @Transactional
    OnmsNode getDbNodeInitCat(Integer nodeId);
    
    /**
     * <p>updateIpInterfaceAttributes</p>
     *
     * @param nodeId a {@link java.lang.Integer} object.
     * @param ipInterface a {@link org.opennms.netmgt.model.OnmsIpInterface} object.
     * @return a {@link org.opennms.netmgt.model.OnmsIpInterface} object.
     */
    @Transactional
    OnmsIpInterface updateIpInterfaceAttributes(Integer nodeId, OnmsIpInterface ipInterface);
    
    /**
     * <p>updateSnmpInterfaceAttributes</p>
     *
     * @param nodeId a {@link java.lang.Integer} object.
     * @param snmpInterface a {@link org.opennms.netmgt.model.OnmsSnmpInterface} object.
     * @return a {@link org.opennms.netmgt.model.OnmsSnmpInterface} object.
     */
    @Transactional
    OnmsSnmpInterface updateSnmpInterfaceAttributes(Integer nodeId, OnmsSnmpInterface snmpInterface);

    /**
     * <p>addMonitoredService</p>
     *
     * @param ipInterfaceId a {@link java.lang.Integer} object.
     * @param svcName a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.OnmsMonitoredService} object.
     */
    @Transactional
    OnmsMonitoredService addMonitoredService(Integer ipInterfaceId, String svcName);

    /**
     * <p>addMonitoredService</p>
     *
     * @param nodeId a {@link java.lang.Integer} object.
     * @param ipAddress a {@link java.lang.String} object.
     * @param serviceName a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.OnmsMonitoredService} object.
     */
    @Transactional
    OnmsMonitoredService addMonitoredService(Integer nodeId, String ipAddress, String serviceName);

    /**
     * <p>updateMonitoredServiceState</p>
     *
     * @param nodeId a {@link java.lang.Integer} object.
     * @param ipAddress a {@link java.lang.String} object.
     * @param serviceName a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.OnmsMonitoredService} object.
     */
    @Transactional
    OnmsMonitoredService updateMonitoredServiceState(Integer nodeId, String ipAddress, String serviceName);

    /**
     * <p>getRequisitionedNode</p>
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @param foreignId a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.OnmsNode} object.
     */
    @Transactional
    OnmsNode getRequisitionedNode(String foreignSource, String foreignId);

    /**
     * Delete the indicated node form the database.
     *
     * @param nodeId a {@link java.lang.Integer} object.
     */
    @Transactional
    void deleteNode(Integer nodeId);

    /**
     * <p>deleteInterface</p>
     *
     * @param nodeId a {@link java.lang.Integer} object.
     * @param ipAddr a {@link java.lang.String} object.
     */
    @Transactional
    void deleteInterface(Integer nodeId, String ipAddr);

    /**
     * <p>deleteService</p>
     *
     * @param nodeId a {@link java.lang.Integer} object.
     * @param addr a {@link java.lang.String} object.
     * @param service a {@link java.lang.String} object.
     */
    @Transactional
    void deleteService(Integer nodeId, InetAddress addr, String service);


    /**
     * Insert the provided node into the database
     *
     * @param node a {@link org.opennms.netmgt.model.OnmsNode} object.
     */
    @Transactional
    void insertNode(OnmsNode node);

    /**
     * Look up the OnmsServiceType with the given name, creating one if it
     * doesn't exist.
     *
     * @param serviceName
     *            the name of the OnmsServiceType to look up
     * @return a OnmsServiceType object with the given name, if none existed
     *         in the database then a new one will been created and saved in
     *         the database.
     */
    @Transactional
    OnmsServiceType createServiceTypeIfNecessary(String serviceName);

    /**
     * Look up the OnmsCategory with the give name, creating one if none
     * exists.
     *
     * @param name
     *            the name of the OnmsCategory to look up
     * @return a OnmsCategor that represents the given name, if none existed
     *         in the database a new one will have been created.
     */
    @Transactional
    OnmsCategory createCategoryIfNecessary(String name);

    /**
     * Creates a map of foreignIds to nodeIds for all nodes that have the indicated foreignSorce.
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @return a {@link java.util.Map} object.
     */
    @Transactional(readOnly = true)
    Map<String, Integer> getForeignIdToNodeIdMap(String foreignSource);

    /**
     * Sets the parent of the node and adds the relationship to the path
     * element for the node. The foreignId is used to reference the node and
     * the parentForeignId and the parentNodeLabel are used to locate the
     * parentNodeId
     *
     * @param foreignSource
     *            the foreignSource to use when looking for the nodeId and
     *            parentNodeId by foreignId.
     * @param foreignId
     *            the foreignId for the node being set
     * @param parentForeignId
     *            the foreignId of the parent node
     * @param parentNodeLabel if the parent node cannot be found using its
     *        foreignId then an attempt to locate it using the its nodeLabel
     *        is made
     */
    @Transactional
    void setNodeParentAndDependencies(
            String foreignSource, String foreignId, 
            String parentForeignSource, String parentForeignId, 
            String parentNodeLabel
           );
    
    /**
     * Returns a list of scheduled nodes.
     *
     * @return a {@link java.util.List} object.
     */
    List<NodeScanSchedule> getScheduleForNodes();
    
    /**
     * <p>getScheduleForNode</p>
     *
     * @param nodeId a int.
     * @param force a boolean.
     * @return a {@link org.opennms.netmgt.provision.service.NodeScanSchedule} object.
     */
    NodeScanSchedule getScheduleForNode(int nodeId, boolean force);
    
    /**
     * <p>setForeignSourceRepository</p>
     *
     * @param foriengSourceRepository a {@link org.opennms.netmgt.provision.persist.ForeignSourceRepository} object.
     */
    void setForeignSourceRepository(ForeignSourceRepository foriengSourceRepository);

    /**
     * <p>loadRequisition</p>
     *
     * @param resource a {@link org.springframework.core.io.Resource} object.
     * @return a {@link org.opennms.netmgt.provision.persist.requisition.Requisition} object.
     */
    Requisition loadRequisition(Resource resource);

    /**
     * <p>getDetectorsForForeignSource</p>
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @return a {@link java.util.List} object.
     */
    List<ServiceDetector> getDetectorsForForeignSource(String foreignSource);
    
    /**
     * <p>getNodePoliciesForForeignSource</p>
     *
     * @param foreignSourceName a {@link java.lang.String} object.
     * @return a {@link java.util.List} object.
     */
    List<NodePolicy> getNodePoliciesForForeignSource(String foreignSourceName);
    
    /**
     * <p>getIpInterfacePoliciesForForeignSource</p>
     *
     * @param foreignSourceName a {@link java.lang.String} object.
     * @return a {@link java.util.List} object.
     */
    List<IpInterfacePolicy> getIpInterfacePoliciesForForeignSource(String foreignSourceName);
    
    /**
     * <p>getSnmpInterfacePoliciesForForeignSource</p>
     *
     * @param foreignSourceName a {@link java.lang.String} object.
     * @return a {@link java.util.List} object.
     */
    List<SnmpInterfacePolicy> getSnmpInterfacePoliciesForForeignSource(String foreignSourceName);

    /**
     * <p>updateNodeScanStamp</p>
     *
     * @param nodeId a {@link java.lang.Integer} object.
     * @param scanStamp a {@link java.util.Date} object.
     */
    @Transactional
    void updateNodeScanStamp(Integer nodeId, Date scanStamp);

    /**
     * <p>deleteObsoleteInterfaces</p>
     *
     * @param nodeId a {@link java.lang.Integer} object.
     * @param scanStamp a {@link java.util.Date} object.
     */
    @Transactional
    void deleteObsoleteInterfaces(Integer nodeId, Date scanStamp);

    /**
     * <p>setIsPrimaryFlag</p>
     *
     * @param nodeId a {@link java.lang.Integer} object.
     * @param ipAddress a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.OnmsIpInterface} object.
     */
    @Transactional
    OnmsIpInterface setIsPrimaryFlag(Integer nodeId, String ipAddress);

    /**
     * <p>getPrimaryInterfaceForNode</p>
     *
     * @param node a {@link org.opennms.netmgt.model.OnmsNode} object.
     * @return a {@link org.opennms.netmgt.model.OnmsIpInterface} object.
     */
    @Transactional
    OnmsIpInterface getPrimaryInterfaceForNode(OnmsNode node);

    /**
     * <p>createUndiscoveredNode</p>
     *
     * @param ipAddress a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.OnmsNode} object.
     */
    @Transactional
    OnmsNode createUndiscoveredNode(String ipAddress);

    /**
     * <p>getNode</p>
     *
     * @param nodeId a {@link java.lang.Integer} object.
     * @return a {@link org.opennms.netmgt.model.OnmsNode} object.
     */
    @Transactional
    OnmsNode getNode(Integer nodeId);




}
