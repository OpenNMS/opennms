/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collectd;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.utils.ParameterMap;
import org.opennms.netmgt.collectd.vmware.vijava.VmwarePerformanceValues;
import org.opennms.netmgt.collection.api.AttributeType;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionException;
import org.opennms.netmgt.collection.api.CollectionInitializationException;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.ServiceCollector;
import org.opennms.netmgt.collection.support.builder.CollectionSetBuilder;
import org.opennms.netmgt.collection.support.builder.CollectionStatus;
import org.opennms.netmgt.collection.support.builder.GenericTypeResource;
import org.opennms.netmgt.collection.support.builder.NodeLevelResource;
import org.opennms.netmgt.collection.support.builder.Resource;
import org.opennms.netmgt.config.api.ResourceTypesDao;
import org.opennms.netmgt.config.datacollection.ResourceType;
import org.opennms.netmgt.config.vmware.vijava.Attrib;
import org.opennms.netmgt.config.vmware.vijava.VmwareCollection;
import org.opennms.netmgt.config.vmware.vijava.VmwareGroup;
import org.opennms.netmgt.dao.VmwareDatacollectionConfigDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.protocols.vmware.VmwareViJavaAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vmware.vim25.mo.ManagedEntity;

/**
 * The Class VmwareCollector
 * <p/>
 * This class is used to collect data from a Vmware vCenter server.
 *
 * @author Christian Pape <Christian.Pape@informatik.hs-fulda.de>
 */
public class VmwareCollector implements ServiceCollector {

    /**
     * logging for VMware data collection
     */
    private final Logger logger = LoggerFactory.getLogger(VmwareCollector.class);

    /**
     * the node dao object for retrieving assets
     */
    private NodeDao m_nodeDao = null;

    /**
     * the config dao
     */
    private VmwareDatacollectionConfigDao m_vmwareDatacollectionConfigDao;

    private ResourceTypesDao m_resourceTypesDao;

    /**
     * Initializes this instance with a given parameter map.
     *
     * @param parameters the parameter map to use
     * @throws CollectionInitializationException
     *
     */
    @Override
    public void initialize(Map<String, String> parameters) throws CollectionInitializationException {

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
        
        if (m_resourceTypesDao == null) {
            m_resourceTypesDao = BeanUtils.getBean("daoContext", "resourceTypesDao", ResourceTypesDao.class);
        }

        initializeRrdRepository();
    }

    /**
     * Initializes the Rrd repository.
     */
    private void initializeRrdRepository() {
        logger.debug("initializeRrdRepository: Initializing RRD repo from VmwareCollector...");
        initializeRrdDirs();
    }

    /**
     * Initializes the Rrd directories.
     */
    private void initializeRrdDirs() {
        final File f = new File(m_vmwareDatacollectionConfigDao.getRrdPath());
        if (!f.isDirectory() && !f.mkdirs()) {
            throw new RuntimeException("Unable to create RRD file repository.  Path doesn't already exist and could not make directory: " + m_vmwareDatacollectionConfigDao.getRrdPath());
        }
    }

    /**
     * Initializes this instance for a given collection agent and a parameter map.
     *
     * @param agent      the collection agent
     * @param parameters the parameter map
     * @throws CollectionInitializationException
     *
     */
    @Override
    public void initialize(CollectionAgent agent, Map<String, Object> parameters) throws CollectionInitializationException {
        OnmsNode onmsNode = m_nodeDao.get(agent.getNodeId());

        // retrieve the assets and
        String vmwareManagementServer = onmsNode.getAssetRecord().getVmwareManagementServer();
        String vmwareManagedEntityType = onmsNode.getAssetRecord().getVmwareManagedEntityType();
        String vmwareManagedObjectId = onmsNode.getForeignId();

        parameters.put("vmwareManagementServer", vmwareManagementServer);
        parameters.put("vmwareManagedEntityType", vmwareManagedEntityType);
        parameters.put("vmwareManagedObjectId", vmwareManagedObjectId);
    }

    /**
     * This method is used for cleanup.
     */
    @Override
    public void release() {
    }

    /**
     * This method is used for cleanup for a given collection agent.
     *
     * @param agent the collection agent
     */
    @Override
    public void release(CollectionAgent agent) {
    }

    /**
     * This method collect the data for a given collection agent.
     *
     * @param agent      the collection agent
     * @param eproxy     the event proxy
     * @param parameters the parameters map
     * @return the generated collection set
     * @throws CollectionException
     */
    @Override
    public CollectionSet collect(CollectionAgent agent, EventProxy eproxy, Map<String, Object> parameters) throws CollectionException {

        String collectionName = ParameterMap.getKeyedString(parameters, "collection", ParameterMap.getKeyedString(parameters, "vmware-collection", null));

        final VmwareCollection collection = m_vmwareDatacollectionConfigDao.getVmwareCollection(collectionName);

        String vmwareManagementServer = (String) parameters.get("vmwareManagementServer");
        String vmwareManagedObjectId = (String) parameters.get("vmwareManagedObjectId");

        if (vmwareManagementServer == null || vmwareManagedObjectId == null) {
            return null;
        } else {
            if ("".equals(vmwareManagementServer) || "".equals(vmwareManagedObjectId)) {
                return null;
            }
        }

        CollectionSetBuilder builder = new CollectionSetBuilder(agent);
        builder.withStatus(CollectionStatus.FAILED);

        VmwareViJavaAccess vmwareViJavaAccess = null;

        try {
            vmwareViJavaAccess = new VmwareViJavaAccess(vmwareManagementServer);
            int timeout = ParameterMap.getKeyedInteger(parameters, "timeout", -1);
            if (timeout > 0) {
                if (!vmwareViJavaAccess.setTimeout(timeout)) {
                    logger.warn("Error setting connection timeout for VMware management server '{}'", vmwareManagementServer);
                }
            }
        } catch (MarshalException e) {
            logger.warn("Error initialising VMware connection to '{}': '{}'", vmwareManagementServer, e.getMessage());
            return builder.build();
        } catch (ValidationException e) {
            logger.warn("Error initialising VMware connection to '{}': '{}'", vmwareManagementServer, e.getMessage());
            return builder.build();
        } catch (IOException e) {
            logger.warn("Error initialising VMware connection to '{}': '{}'", vmwareManagementServer, e.getMessage());
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
                                final ResourceType resourceType = m_resourceTypesDao.getResourceTypeByName(vmwareGroup.getResourceType());
                                if (resourceType == null) {
                                    throw new CollectionException("No resource type found with name '" + vmwareGroup.getResourceType() + "'.");
                                }
                                resources.put(instance, new GenericTypeResource(nodeResource, resourceType, instance));
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

    public void setResourceTypesDao(ResourceTypesDao resourceTypesDao) {
        m_resourceTypesDao = Objects.requireNonNull(resourceTypesDao);
    }
}
