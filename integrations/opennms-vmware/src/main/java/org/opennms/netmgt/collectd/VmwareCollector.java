/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collectd;

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.opennms.core.spring.BeanUtils;
import org.opennms.core.utils.ParameterMap;
import org.opennms.netmgt.collectd.vmware.vijava.VmwarePerformanceValues;
import org.opennms.netmgt.collection.api.AbstractRemoteServiceCollector;
import org.opennms.netmgt.collection.api.AttributeType;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionException;
import org.opennms.netmgt.collection.api.CollectionInitializationException;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.CollectionStatus;
import org.opennms.netmgt.collection.support.builder.CollectionSetBuilder;
import org.opennms.netmgt.collection.support.builder.DeferredGenericTypeResource;
import org.opennms.netmgt.collection.support.builder.NodeLevelResource;
import org.opennms.netmgt.collection.support.builder.Resource;
import org.opennms.netmgt.config.vmware.VmwareServer;
import org.opennms.netmgt.config.vmware.vijava.Attrib;
import org.opennms.netmgt.config.vmware.vijava.VmwareCollection;
import org.opennms.netmgt.config.vmware.vijava.VmwareGroup;
import org.opennms.netmgt.dao.VmwareConfigDao;
import org.opennms.netmgt.dao.VmwareDatacollectionConfigDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.protocols.vmware.VmwareViJavaAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.vmware.vim25.mo.ManagedEntity;

/**
 * The Class VmwareCollector
 * <p/>
 * This class is used to collect data from a Vmware vCenter server.
 *
 * @author Christian Pape <Christian.Pape@informatik.hs-fulda.de>
 */
public class VmwareCollector extends AbstractRemoteServiceCollector {

    /**
     * logging for VMware data collection
     */
    private static final Logger logger = LoggerFactory.getLogger(VmwareCollector.class);

    private static final String VMWARE_COLLECTION_KEY = "vmwareCollection";
    private static final String VMWARE_MGMT_SERVER_KEY = "vmwareManagementServer";
    private static final String VMWARE_MGED_OBJECT_ID_KEY = "vmwareManagedObjectId";
    private static final String VMWARE_SERVER_KEY = "vmwareServer";

    private static final Map<String, Class<?>> TYPE_MAP = Collections.unmodifiableMap(Stream.of(
            new SimpleEntry<>(VMWARE_COLLECTION_KEY, VmwareCollection.class),
            new SimpleEntry<>(VMWARE_SERVER_KEY, VmwareServer.class))
            .collect(Collectors.toMap((e) -> e.getKey(), (e) -> e.getValue())));

    /**
     * the node dao object for retrieving assets
     */
    private NodeDao m_nodeDao = null;

    /**
     * the config dao
     */
    private VmwareDatacollectionConfigDao m_vmwareDatacollectionConfigDao;

    /**
     * the config dao
     */
    private VmwareConfigDao m_vmwareConfigDao = null;

    public VmwareCollector() {
        super(TYPE_MAP);
    }

    /**
     * Initializes this instance with a given parameter map.
     *
     * @param parameters the parameter map to use
     * @throws CollectionInitializationException
     *
     */
    @Override
    public void initialize() throws CollectionInitializationException {

        if (m_nodeDao == null) {
            m_nodeDao = BeanUtils.getBean("daoContext", "nodeDao", NodeDao.class);
        }

        if (m_nodeDao == null) {
            logger.error("Node dao should be a non-null value.");
        }

        if (m_vmwareDatacollectionConfigDao == null) {
            m_vmwareDatacollectionConfigDao = BeanUtils.getBean("daoContext", "vmwareDatacollectionConfigDao", VmwareDatacollectionConfigDao.class);
        }

        if (m_vmwareDatacollectionConfigDao == null) {
            logger.error("vmwareDatacollectionConfigDao should be a non-null value.");
        }

        if (m_vmwareConfigDao == null) {
            m_vmwareConfigDao = BeanUtils.getBean("daoContext", "vmwareConfigDao", VmwareConfigDao.class);
        }
    }

    @Override
    public Map<String, Object> getRuntimeAttributes(CollectionAgent agent, Map<String, Object> parameters) {
        final Map<String, Object> runtimeAttributes = new HashMap<>();
        final OnmsNode onmsNode = m_nodeDao.get(agent.getNodeId());
        if (onmsNode == null) {
            throw new IllegalArgumentException(String.format("VmwareCollector: No node found with id: %d", agent.getNodeId()));
        }

        // retrieve the assets
        final String vmwareManagementServer = onmsNode.getAssetRecord().getVmwareManagementServer();
        if (Strings.isNullOrEmpty(vmwareManagementServer)) {
            throw new IllegalArgumentException(String.format("VmwareCollector: No management server is set on node with id %d.",  onmsNode.getId()));
        }
        runtimeAttributes.put(VMWARE_MGMT_SERVER_KEY, vmwareManagementServer);

        final String vmwareManagedObjectId = onmsNode.getForeignId();
        if (Strings.isNullOrEmpty(vmwareManagedObjectId)) {
            throw new IllegalArgumentException(String.format("VmwareCollector: No foreign id is set on node with id %d.",  onmsNode.getId()));
        }
        runtimeAttributes.put(VMWARE_MGED_OBJECT_ID_KEY, vmwareManagedObjectId);

        // retrieve the collection
        final String collectionName = ParameterMap.getKeyedString(parameters, "collection", ParameterMap.getKeyedString(parameters, "vmware-collection", null));
        final VmwareCollection collection = m_vmwareDatacollectionConfigDao.getVmwareCollection(collectionName);
        if (collection == null) {
            throw new IllegalArgumentException(String.format("VmwareCollector: No collection found with name '%s'.",  collectionName));
        }
        runtimeAttributes.put(VMWARE_COLLECTION_KEY, collection);

        // retrieve the server configuration
        final Map<String, VmwareServer> serverMap = m_vmwareConfigDao.getServerMap();
        if (serverMap == null) {
            throw new IllegalStateException(String.format("VmwareCollector: Error getting vmware-config.xml's server map."));
        }
        final VmwareServer vmwareServer = serverMap.get(vmwareManagementServer);
        if (vmwareServer == null) {
            throw new IllegalStateException(String.format("VmwareCollector: Error getting credentials for VMware management server: %s", vmwareManagementServer));
        }
        runtimeAttributes.put(VMWARE_SERVER_KEY, vmwareServer);
        return runtimeAttributes;
    }

    /**
     * This method collect the data for a given collection agent.
     *
     * @param agent      the collection agent
     * @param parameters the parameters map
     * @return the generated collection set
     * @throws CollectionException
     */
    @Override
    public CollectionSet collect(CollectionAgent agent, Map<String, Object> parameters) throws CollectionException {
        final VmwareCollection collection = (VmwareCollection) parameters.get(VMWARE_COLLECTION_KEY);
        final String vmwareManagementServer = (String) parameters.get(VMWARE_MGMT_SERVER_KEY);
        final String vmwareManagedObjectId = (String) parameters.get(VMWARE_MGED_OBJECT_ID_KEY);
        final VmwareServer vmwareServer = (VmwareServer) parameters.get(VMWARE_SERVER_KEY);

        CollectionSetBuilder builder = new CollectionSetBuilder(agent);
        builder.withStatus(CollectionStatus.FAILED);

        VmwareViJavaAccess vmwareViJavaAccess = new VmwareViJavaAccess(vmwareServer);
        int timeout = ParameterMap.getKeyedInteger(parameters, "timeout", -1);
        if (timeout > 0) {
            if (!vmwareViJavaAccess.setTimeout(timeout)) {
                logger.warn("Error setting connection timeout for VMware management server '{}'", vmwareManagementServer);
            }
        }

        if (collection.getVmwareGroup().length < 1) {
            logger.info("No groups to collect. Returning empty collection set.");
            builder.withStatus(CollectionStatus.SUCCEEDED);
            return builder.build();
        }

        try {
            vmwareViJavaAccess.connect();
        } catch (MalformedURLException e) {
            logger.warn("Error connecting VMware management server '{}': '{}' exception: {} cause: '{}'", vmwareManagementServer, e.getMessage(), e.getClass().getName(), e.getCause());
            return builder.build();
        } catch (RemoteException e) {
            logger.warn("Error connecting VMware management server '{}': '{}' exception: {} cause: '{}'", vmwareManagementServer, e.getMessage(), e.getClass().getName(), e.getCause());
            return builder.build();
        }

        ManagedEntity managedEntity = vmwareViJavaAccess.getManagedEntityByManagedObjectId(vmwareManagedObjectId);

        VmwarePerformanceValues vmwarePerformanceValues = null;

        try {
            vmwarePerformanceValues = vmwareViJavaAccess.queryPerformanceValues(managedEntity);
        } catch (RemoteException e) {
            logger.warn("Error retrieving performance values from VMware management server '" + vmwareManagementServer + "' for managed object '" + vmwareManagedObjectId + "'", e.getMessage());

            vmwareViJavaAccess.disconnect();

            return builder.build();
        }

        for (final VmwareGroup vmwareGroup : collection.getVmwareGroup()) {
            final NodeLevelResource nodeResource = new NodeLevelResource(agent.getNodeId());

            if ("node".equalsIgnoreCase(vmwareGroup.getResourceType())) {
                // single instance value

                for (Attrib attrib : vmwareGroup.getAttrib()) {
                    if (!vmwarePerformanceValues.hasSingleValue(attrib.getName())) {
                        // warning
                        logger.debug("Warning! No single value for '{}' defined as single instance attribute for node {}", attrib.getName(), agent.getNodeId());
                    } else {
                        final Long value = vmwarePerformanceValues.getValue(attrib.getName());
                        logger.debug("Storing single instance value {}='{}' for node {}",
                                attrib.getName(), value, agent.getNodeId());

                        final AttributeType type = attrib.getType();
                        if (type.isNumeric()) {
                            builder.withNumericAttribute(nodeResource, vmwareGroup.getName(), attrib.getAlias(), value, type);
                        } else {
                            builder.withStringAttribute(nodeResource, vmwareGroup.getName(), attrib.getAlias(), String.valueOf(value));
                        }
                    }
                }
            } else {
                // multi instance value

                final Set<String> instanceSet = new TreeSet<>();

                final HashMap<String, Resource> resources = new HashMap<>();

                for (Attrib attrib : vmwareGroup.getAttrib()) {

                    if (!vmwarePerformanceValues.hasInstances(attrib.getName())) {
                        // warning
                        logger.debug("Warning! No multi instance value for '{}' defined as multi instance attribute for node {}", attrib.getName(), agent.getNodeId());
                    } else {

                        Set<String> newInstances = vmwarePerformanceValues.getInstances(attrib.getName());

                        for (String instance : newInstances) {
                            if (!instanceSet.contains(instance)) {
                                resources.put(instance, new DeferredGenericTypeResource(nodeResource, vmwareGroup.getResourceType(), instance));
                                instanceSet.add(instance);
                            }

                            final AttributeType type = attrib.getType();
                            final Long value = vmwarePerformanceValues.getValue(attrib.getName(), instance);
                            logger.debug("Storing multi instance value {}[{}='{}' for node {}",
                                    attrib.getName(), instance, value, agent.getNodeId());
                            if (type.isNumeric()) {
                                builder.withNumericAttribute(resources.get(instance), vmwareGroup.getName(), attrib.getAlias(), value, type);
                            } else {
                                builder.withStringAttribute(resources.get(instance), vmwareGroup.getName(), attrib.getAlias(), Long.toString(value));
                            }
                        }
                    }
                }

                for (String instance : instanceSet) {
                    logger.debug("Storing multi instance value {}[{}='{}' for node {}",
                            vmwareGroup.getResourceType() + "Name", instance, instance, agent.getNodeId());
                    builder.withStringAttribute(resources.get(instance), vmwareGroup.getName(), vmwareGroup.getResourceType() + "Name", instance);
                }
            }
        }

        builder.withStatus(CollectionStatus.SUCCEEDED);

        vmwareViJavaAccess.disconnect();

        return builder.build();
    }

    /**
     * Returns the Rrd repository for this object.
     *
     * @param collectionName the collection's name
     * @return the Rrd repository
     */
    @Override
    public RrdRepository getRrdRepository(final String collectionName) {
        return m_vmwareDatacollectionConfigDao.getRrdRepository(collectionName);
    }

    /**
     * Sets the NodeDao object for this instance.
     *
     * @param nodeDao the NodeDao object to use
     */
    public void setNodeDao(NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }
}
