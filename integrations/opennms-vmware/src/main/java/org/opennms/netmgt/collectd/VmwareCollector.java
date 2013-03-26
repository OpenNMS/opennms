/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collectd;

import com.vmware.vim25.mo.ManagedEntity;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.utils.BeanUtils;
import org.opennms.core.utils.ParameterMap;
import org.opennms.protocols.vmware.VmwareViJavaAccess;
import org.opennms.netmgt.collectd.vmware.vijava.*;
import org.opennms.netmgt.config.collector.AttributeGroupType;
import org.opennms.netmgt.config.collector.CollectionSet;
import org.opennms.netmgt.config.vmware.vijava.Attrib;
import org.opennms.netmgt.config.vmware.vijava.VmwareCollection;
import org.opennms.netmgt.config.vmware.vijava.VmwareGroup;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.VmwareDatacollectionConfigDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.RrdRepository;
import org.opennms.netmgt.model.events.EventProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
    private final Logger logger = LoggerFactory.getLogger("OpenNMS.VMware." + VmwareCollector.class.getName());

    /**
     * the node dao object for retrieving assets
     */
    private NodeDao m_nodeDao = null;

    /**
     * the config dao
     */
    private VmwareDatacollectionConfigDao m_vmwareDatacollectionConfigDao;

    /**
     * Initializes this instance with a given parameter map.
     *
     * @param parameters the parameter map to use
     * @throws CollectionInitializationException
     *
     */
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

        initDatabaseConnectionFactory();
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
     * Initializes the database connection factory.
     */
    private void initDatabaseConnectionFactory() {
        try {
            DataSourceFactory.init();
        } catch (final Exception e) {
            logger.error("initDatabaseConnectionFactory: Error initializing DataSourceFactory. Error message: '{}'", e.getMessage());
            throw new UndeclaredThrowableException(e);
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
    public void release() {
    }

    /**
     * This method is used for cleanup for a given collection agent.
     *
     * @param agent the collection agent
     */
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

        VmwareCollectionSet collectionSet = new VmwareCollectionSet(agent);

        collectionSet.setCollectionTimestamp(new Date());

        collectionSet.setStatus(ServiceCollector.COLLECTION_FAILED);

        VmwareViJavaAccess vmwareViJavaAccess = null;

        try {
            vmwareViJavaAccess = new VmwareViJavaAccess(vmwareManagementServer);
        } catch (MarshalException e) {
            logger.warn("Error initialising VMware connection to '{}': '{}'", vmwareManagementServer, e.getMessage());
            return collectionSet;
        } catch (ValidationException e) {
            logger.warn("Error initialising VMware connection to '{}': '{}'", vmwareManagementServer, e.getMessage());
            return collectionSet;
        } catch (IOException e) {
            logger.warn("Error initialising VMware connection to '{}': '{}'", vmwareManagementServer, e.getMessage());
            return collectionSet;
        }

        try {
            vmwareViJavaAccess.connect();
        } catch (MalformedURLException e) {
            logger.warn("Error connecting VMware management server '{}': '{}'", vmwareManagementServer, e.getMessage());
            return collectionSet;
        } catch (RemoteException e) {
            logger.warn("Error connecting VMware management server '{}': '{}'", vmwareManagementServer, e.getMessage());
            return collectionSet;
        }

        ManagedEntity managedEntity = vmwareViJavaAccess.getManagedEntityByManagedObjectId(vmwareManagedObjectId);

        VmwarePerformanceValues vmwarePerformanceValues = null;

        try {
            vmwarePerformanceValues = vmwareViJavaAccess.queryPerformanceValues(managedEntity);
        } catch (RemoteException e) {
            logger.warn("Error retrieving performance values from VMware management server '" + vmwareManagementServer + "' for managed object '" + vmwareManagedObjectId + "'", e.getMessage());

            vmwareViJavaAccess.disconnect();

            return collectionSet;
        }

        for (final VmwareGroup vmwareGroup : collection.getVmwareGroup()) {
            final AttributeGroupType attribGroupType = new AttributeGroupType(vmwareGroup.getName(), "all");

            if ("node".equalsIgnoreCase(vmwareGroup.getResourceType())) {
                // single instance value

                VmwareCollectionResource vmwareCollectionResource = new VmwareSingleInstanceCollectionResource(agent);

                for (Attrib attrib : vmwareGroup.getAttrib()) {

                    if (vmwarePerformanceValues.hasInstances(attrib.getName())) {
                        // warning
                        logger.warn("Warning! Found multi instance value '{}' defined as single instance attribute for node '{}'", attrib.getName(), agent.getNodeId());
                    } else {
                        final VmwareCollectionAttributeType attribType = new VmwareCollectionAttributeType(attrib, attribGroupType);
                        logger.debug("Storing single instance value " + attrib.getName() + "='" + vmwarePerformanceValues.getValue(attrib.getName()) + "for node " + agent.getNodeId());
                        vmwareCollectionResource.setAttributeValue(attribType, String.valueOf(vmwarePerformanceValues.getValue(attrib.getName())));
                    }
                }

                collectionSet.getResources().add(vmwareCollectionResource);
            } else {
                // multi instance value

                Set<String> instanceSet = null;

                HashMap<String, VmwareMultiInstanceCollectionResource> resources = new HashMap<String, VmwareMultiInstanceCollectionResource>();

                for (Attrib attrib : vmwareGroup.getAttrib()) {
                    if (!vmwarePerformanceValues.hasInstances(attrib.getName())) {
                        // warning
                        logger.warn("Warning! Found single instance value '{}' defined as multi instance attribute for node {}", attrib.getName(), agent.getNodeId());
                    } else {

                        instanceSet = vmwarePerformanceValues.getInstances(attrib.getName());

                        for (String instance : instanceSet) {
                            if (!resources.containsKey(instance)) {
                                resources.put(instance, new VmwareMultiInstanceCollectionResource(agent, instance, vmwareGroup.getResourceType()));
                            }

                            logger.debug("Storing multi instance value " + attrib.getName() + "[" + instance + "]='" + vmwarePerformanceValues.getValue(attrib.getName()) + "' for node " +
                                    agent.getNodeId());
                        }
                    }
                }

                if (instanceSet != null) {
                    final Attrib attrib = new Attrib();
                    attrib.setName(vmwareGroup.getResourceType() + "Name");
                    attrib.setAlias(vmwareGroup.getResourceType() + "Name");
                    attrib.setType("String");

                    for (String instance : instanceSet) {
                        final VmwareCollectionAttributeType attribType = new VmwareCollectionAttributeType(attrib, attribGroupType);
                        //TODO indigo: check if instance / instance is right
                        logger.debug("Storing multi instance value " + attrib.getName() + "[" + instance + "]='" + instance + "' for node " + agent.getNodeId());
                        resources.get(instance).setAttributeValue(attribType, instance);
                    }

                    for (String instance : resources.keySet()) {
                        collectionSet.getResources().add(resources.get(instance));
                    }
                }
            }
        }

        collectionSet.setStatus(ServiceCollector.COLLECTION_SUCCEEDED);

        vmwareViJavaAccess.disconnect();

        return collectionSet;
    }

    /**
     * Returns the Rrd repository for this object.
     *
     * @param collectionName the collection's name
     * @return the Rrd repository
     */
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
