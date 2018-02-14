/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.service;

import java.net.InetAddress;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;
import org.opennms.netmgt.provision.IpInterfacePolicy;
import org.opennms.netmgt.provision.LocationAwareDetectorClient;
import org.opennms.netmgt.provision.LocationAwareDnsLookupClient;
import org.opennms.netmgt.provision.NodePolicy;
import org.opennms.netmgt.provision.SnmpInterfacePolicy;
import org.opennms.netmgt.provision.persist.ForeignSourceRepository;
import org.opennms.netmgt.provision.persist.foreignsource.PluginConfig;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.snmp.proxy.LocationAwareSnmpClient;
import org.springframework.core.io.Resource;
import org.springframework.transaction.annotation.Transactional;

/*
 * ProvisionService
 * @author brozow
 */
public interface ProvisionService {

    boolean isDiscoveryEnabled();
    
    /**
     * Clear the Hibernate object cache. This is used to clear the object
     * cache created by Hibernate. This is needed so large imports don't end
     * up caching the entire database when it has no intention of using a
     * node's data again. This is needed only to help memory performance.
     */
    void clearCache();

    /**
     * Lookup a monitoring location in the database, creating it if necessary. This
     * method looks up the {@link OnmsMonitoringLocation} object with the ID 'locationId' in the
     * database and returns it. If there is no {@link OnmsMonitoringLocation} with that name then
     * one is created using the name provided, saved in the database, and returned.
     *
     * @param locationId
     *            The ID of the {@link OnmsMonitoringLocation} that is needed
     * @return a new {@link OnmsMonitoringLocation} that will be saved to the database when the
     *         transaction is committed.
     */
    @Transactional
    OnmsMonitoringLocation createLocationIfNecessary(String locationId);

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
     *            false, if the node should not be rescanned (perform only add/delete operations on the DB)
     *            dbonly, if the node should not be rescanned (perform all DB operations)
     */
    @Transactional
    void updateNode(OnmsNode node, String rescanExisting);
    
    @Transactional
    OnmsNode updateNodeAttributes(OnmsNode node);
   
    @Transactional
    OnmsNode getDbNodeInitCat(Integer nodeId);
    
    @Transactional
    OnmsIpInterface updateIpInterfaceAttributes(Integer nodeId, OnmsIpInterface ipInterface);
    
    @Transactional
    OnmsSnmpInterface updateSnmpInterfaceAttributes(Integer nodeId, OnmsSnmpInterface snmpInterface);

    @Transactional
    OnmsMonitoredService addMonitoredService(Integer ipInterfaceId, String svcName);

    @Transactional
    OnmsMonitoredService addMonitoredService(Integer nodeId, String ipAddress, String serviceName);

    @Transactional
    OnmsMonitoredService updateMonitoredServiceState(Integer nodeId, String ipAddress, String serviceName);

    @Transactional
    OnmsNode getRequisitionedNode(String foreignSource, String foreignId);

    /**
     * Delete the indicated node form the database.
     */
    @Transactional
    void deleteNode(Integer nodeId);

    @Transactional
    void deleteInterface(Integer nodeId, String ipAddr);

    @Transactional
    void deleteService(Integer nodeId, InetAddress addr, String service);

    /**
     * Insert the provided node into the database
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
     */
    List<NodeScanSchedule> getScheduleForNodes();

    NodeScanSchedule getScheduleForNode(int nodeId, boolean force);
    
    void setForeignSourceRepository(ForeignSourceRepository foriengSourceRepository);

    Requisition loadRequisition(Resource resource);

    List<PluginConfig> getDetectorsForForeignSource(String foreignSource);

    List<NodePolicy> getNodePoliciesForForeignSource(String foreignSourceName);
    
    List<IpInterfacePolicy> getIpInterfacePoliciesForForeignSource(String foreignSourceName);
    
    List<SnmpInterfacePolicy> getSnmpInterfacePoliciesForForeignSource(String foreignSourceName);

    @Transactional
    void updateNodeScanStamp(Integer nodeId, Date scanStamp);

    @Transactional
    void deleteObsoleteInterfaces(Integer nodeId, Date scanStamp);

    @Transactional
    OnmsIpInterface setIsPrimaryFlag(Integer nodeId, String ipAddress);

    @Transactional
    OnmsIpInterface getPrimaryInterfaceForNode(OnmsNode node);

    @Transactional
    OnmsNode createUndiscoveredNode(String ipAddress, String foreignSource, String location);

    @Transactional
    OnmsNode getNode(Integer nodeId);

    public HostnameResolver getHostnameResolver();
    public void setHostnameResolver(final HostnameResolver resolver);

    LocationAwareDetectorClient getLocationAwareDetectorClient();

    LocationAwareSnmpClient getLocationAwareSnmpClient();

    LocationAwareDnsLookupClient getLocationAwareDnsLookupClient();
}
