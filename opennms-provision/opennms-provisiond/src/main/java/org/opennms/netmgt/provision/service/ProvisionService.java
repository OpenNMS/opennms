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
package org.opennms.netmgt.provision.service;

import java.net.InetAddress;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsMetaData;
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
import org.opennms.netmgt.snmp.SnmpProfileMapper;
import org.opennms.netmgt.snmp.proxy.LocationAwareSnmpClient;
import org.springframework.core.io.Resource;
import org.springframework.transaction.annotation.Transactional;

import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;

/*
 * ProvisionService
 * @author brozow
 */
public interface ProvisionService {

    String NODE_ID = "nodeId";

    String LOCATION = "location";

    String IP_ADDRESS = "ipAddress";

    String FOREIGN_ID = "foreignId";

    String FOREIGN_SOURCE = "foreignSource";

    String DETECTOR_NAME = "detectorName";

    String ABORT = "abort";

    String ERROR = "error";


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
    void updateNode(OnmsNode node, String rescanExisting, String monitorKey);
    
    @Transactional
    OnmsNode updateNodeAttributes(OnmsNode node);
   
    @Transactional
    OnmsNode getDbNodeInitCat(Integer nodeId);
    
    @Transactional
    OnmsIpInterface updateIpInterfaceAttributes(Integer nodeId, OnmsIpInterface ipInterface, String monitorKey);
    
    @Transactional
    OnmsSnmpInterface updateSnmpInterfaceAttributes(Integer nodeId, OnmsSnmpInterface snmpInterface);

    @Transactional
    OnmsMonitoredService addMonitoredService(Integer nodeId, String ipAddress, String serviceName, String monitorKey, List<OnmsMetaData> metaData);

    @Transactional
    OnmsMonitoredService updateMonitoredServiceState(Integer nodeId, String ipAddress, String serviceName);

    @Transactional
    OnmsNode getRequisitionedNode(String foreignSource, String foreignId);

    /**
     * Delete the indicated node from the database.
     */
    @Transactional
    void deleteNode(Integer nodeId);

    @Transactional
    void deleteInterface(Integer nodeId, String ipAddr);

    /**
     * Delete the indicated service from the database.
     * 
     * If the service is the last service on the interface, delete the interface as well.
     * If the interface is the last interface on the node, delete the node as well.
     * 
     * @param nodeId the node containing the service
     * @param addr the IP address containing the service
     * @param service the service to delete
     * @param ignoreUnmanaged if true, cascade delete the containing interface if only unmanaged services remain
     */
    @Transactional
    void deleteService(Integer nodeId, InetAddress addr, String service, boolean ignoreUnmanaged);

    /**
     * Insert the provided node into the database
     */
    @Transactional
    void insertNode(OnmsNode node, String monitorKey);

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
     * @return an OnmsCategory that represents the given name, if none existed
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
    List<NodeScanSchedule> getScheduleForNodes(String monitorKey);

    NodeScanSchedule getScheduleForNode(int nodeId, boolean force, String monitorKey);
    
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
    OnmsNode createUndiscoveredNode(String ipAddress, String foreignSource, String location, String monitorKey);

    @Transactional
    OnmsNode getNode(Integer nodeId);

    public HostnameResolver getHostnameResolver();
    public void setHostnameResolver(final HostnameResolver resolver);

    LocationAwareDetectorClient getLocationAwareDetectorClient();

    LocationAwareSnmpClient getLocationAwareSnmpClient();

    LocationAwareDnsLookupClient getLocationAwareDnsLookupClient();

    SnmpProfileMapper getSnmpProfileMapper();

    public void setSnmpProfileMapper(SnmpProfileMapper snmpProfileMapper);

    public void setTracer(Tracer tracer);

    public Span buildAndStartSpan(String name, SpanContext spanContext);
}
