/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.netmgt.provision.service;

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
public interface ProvisionService {

    /**
     * Clear the Hibernate object cache. This is used to clear the object
     * cache created by Hibernate. This is needed so large imports don't end
     * up caching the entire database when it has no intention of using a
     * node's data again. This is needed only to help memory performance.
     */
    public abstract void clearCache();

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
    public abstract OnmsDistPoller createDistPollerIfNecessary(String dpName, String dpAddr);

    /**
     * Update the database entry for the given node. The node supplied is used
     * to update the database. Entries that have been change in the node are
     * copied into the database. It is assumed that the node passed in has
     * been previously loaded from the database and modified.
     * 
     * @param node
     *            The node that has been updated and should be written to the
     *            database
     */
    @Transactional
    public abstract void updateNode(OnmsNode node);
    
    @Transactional
    public abstract OnmsNode updateNodeAttributes(OnmsNode node);
    
    @Transactional
    public abstract OnmsIpInterface updateIpInterfaceAttributes(Integer nodeId, OnmsIpInterface ipInterface);
    
    @Transactional
    public OnmsSnmpInterface updateSnmpInterfaceAttributes(Integer nodeId, OnmsSnmpInterface snmpInterface);

    @Transactional
    public abstract OnmsMonitoredService addMonitoredService(Integer ipInterfaceId, String svcName);

    @Transactional
    public abstract OnmsMonitoredService addMonitoredService(Integer nodeId, String ipAddress, String serviceName);
    
    @Transactional
    public abstract OnmsNode getRequisitionedNode(String foreignSource, String foreignId);

    /**
     * Delete the indicated node form the database.
     */
    @Transactional
    public abstract void deleteNode(Integer nodeId);

    /**
     * Insert the provided node into the database
     */
    @Transactional
    public abstract void insertNode(OnmsNode node);

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
    public abstract OnmsServiceType createServiceTypeIfNecessary(String serviceName);

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
    public abstract OnmsCategory createCategoryIfNecessary(String name);

    /**
     * Creates a map of foreignIds to nodeIds for all nodes that have the indicated foreignSorce.
     */
    @Transactional(readOnly = true)
    public abstract Map<String, Integer> getForeignIdToNodeIdMap(String foreignSource);

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
    public abstract void setNodeParentAndDependencies(
            String foreignSource, String foreignId, 
            String parentForeignId, String parentNodeLabel
           );
    
    /**
     * Returns a list of scheduled nodes.
     * 
     * @return
     */
    public abstract List<NodeScanSchedule> getScheduleForNodes();
    
    public abstract NodeScanSchedule getScheduleForNode(int nodeId, boolean force);
    
    public abstract void setForeignSourceRepository(ForeignSourceRepository foriengSourceRepository);

    /**
     * @param foreignSource
     * @param resource
     * @return
     */
    public abstract Requisition loadRequisition(Resource resource);

    public abstract List<ServiceDetector> getDetectorsForForeignSource(String foreignSource);
    
    public abstract List<NodePolicy> getNodePoliciesForForeignSource(String foreignSourceName);
    
    public abstract List<IpInterfacePolicy> getIpInterfacePoliciesForForeignSource(String foreignSourceName);
    
    public abstract List<SnmpInterfacePolicy> getSnmpInterfacePoliciesForForeignSource(String foreignSourceName);

    @Transactional
    public abstract void updateNodeScanStamp(Integer nodeId, Date scanStamp);

    @Transactional
    public abstract void deleteObsoleteInterfaces(Integer nodeId, Date scanStamp);

    public abstract OnmsIpInterface setPrimaryInterfaceIfNoneSet(OnmsMonitoredService svc);

    public abstract OnmsIpInterface getPrimaryInterfaceForNode(OnmsNode node);

    public abstract OnmsNode createUndiscoveredNode(String ipAddress);

    public abstract OnmsNode getNode(Integer nodeId);



}