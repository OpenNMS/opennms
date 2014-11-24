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
 * Additional permission under GNU AGPL version 3 section 7
 *
 * If you modify this Program, or any covered work, by linking or
 * combining it with SBLIM (or a modified version of that library),
 * containing parts covered by the terms of the Eclipse Public License,
 * the licensors of this Program grant you additional permission to
 * convey the resulting work. {Corresponding Source for a non-source
 * form of such a combination shall include the source code for the
 * parts of SBLIM used as well as that of the covered work.}
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ParameterMap;
import org.opennms.netmgt.collectd.vmware.cim.VmwareCimCollectionAttributeType;
import org.opennms.netmgt.collectd.vmware.cim.VmwareCimCollectionResource;
import org.opennms.netmgt.collectd.vmware.cim.VmwareCimCollectionSet;
import org.opennms.netmgt.collectd.vmware.cim.VmwareCimMultiInstanceCollectionResource;
import org.opennms.netmgt.collection.api.AttributeGroupType;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionException;
import org.opennms.netmgt.collection.api.CollectionInitializationException;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.ServiceCollector;
import org.opennms.netmgt.config.vmware.cim.Attrib;
import org.opennms.netmgt.config.vmware.cim.VmwareCimCollection;
import org.opennms.netmgt.config.vmware.cim.VmwareCimGroup;
import org.opennms.netmgt.dao.VmwareCimDatacollectionConfigDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.protocols.vmware.VmwareViJavaAccess;
import org.sblim.wbem.cim.CIMObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vmware.vim25.HostRuntimeInfo;
import com.vmware.vim25.HostSystemPowerState;
import com.vmware.vim25.mo.HostSystem;

public class VmwareCimCollector implements ServiceCollector {

    /**
     * logging for VMware CIM data collection
     */
    private final Logger logger = LoggerFactory.getLogger("OpenNMS.VMware." + VmwareCimCollector.class.getName());

    /**
     * the attribute groups
     */
    private Map<String, AttributeGroupType> m_groupTypeList = new HashMap<String, AttributeGroupType>();

    /**
     * the attribute types
     */
    private Map<String, VmwareCimCollectionAttributeType> m_attribTypeList = new HashMap<String, VmwareCimCollectionAttributeType>();

    /**
     * the node dao object for retrieving assets
     */
    private NodeDao m_nodeDao = null;

    /**
     * the config dao
     */
    VmwareCimDatacollectionConfigDao m_vmwareCimDatacollectionConfigDao;

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

        if (m_vmwareCimDatacollectionConfigDao == null) {
            m_vmwareCimDatacollectionConfigDao = BeanUtils.getBean("daoContext", "vmwareCimDatacollectionConfigDao", VmwareCimDatacollectionConfigDao.class);
        }

        if (m_nodeDao == null) {
            logger.error("vmwareCimDatacollectionConfigDao should be a non-null value.");
        }

        initializeRrdRepository();
    }

    /**
     * Initializes the Rrd repository.
     */
    private void initializeRrdRepository() {
        logger.debug("initializeRrdRepository: Initializing RRD repo from VmwareCimCollector...");
        initializeRrdDirs();
    }

    /**
     * Initializes the Rrd directories.
     */
    private void initializeRrdDirs() {
        final File f = new File(m_vmwareCimDatacollectionConfigDao.getRrdPath());
        if (!f.isDirectory() && !f.mkdirs()) {
            throw new RuntimeException("Unable to create RRD file repository.  Path doesn't already exist and could not make directory: " + m_vmwareCimDatacollectionConfigDao.getRrdPath());
        }
    }

    /**
     * Initializes the attribute group list for a given collection name.
     *
     * @param collection the collection's name
     */
    private void loadAttributeGroupList(final VmwareCimCollection collection) {
        for (final VmwareCimGroup vpm : collection.getVmwareCimGroup()) {
            final AttributeGroupType attribGroupType1 = new AttributeGroupType(vpm.getName(), AttributeGroupType.IF_TYPE_ALL);
            m_groupTypeList.put(vpm.getName(), attribGroupType1);
        }
    }

    /**
     * Initializes the attribute type list for a given collection name.
     *
     * @param collection the collection's name
     */
    private void loadAttributeTypeList(final VmwareCimCollection collection) {
        for (final VmwareCimGroup vpm : collection.getVmwareCimGroup()) {
            for (final Attrib attrib : vpm.getAttrib()) {
                final AttributeGroupType attribGroupType = m_groupTypeList.get(vpm.getName());
                final VmwareCimCollectionAttributeType attribType = new VmwareCimCollectionAttributeType(attrib, attribGroupType);
                m_attribTypeList.put(attrib.getName(), attribType);
            }
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

        final VmwareCimCollection collection = m_vmwareCimDatacollectionConfigDao.getVmwareCimCollection(collectionName);

        String vmwareManagementServer = (String) parameters.get("vmwareManagementServer");
        String vmwareManagedObjectId = (String) parameters.get("vmwareManagedObjectId");

        if (vmwareManagementServer == null || vmwareManagedObjectId == null) {
            return null;
        } else {
            if ("".equals(vmwareManagementServer) || "".equals(vmwareManagedObjectId)) {
                return null;
            }
        }

        // Load the attribute group types.
        loadAttributeGroupList(collection);

        // Load the attribute types.
        loadAttributeTypeList(collection);

        VmwareCimCollectionSet collectionSet = new VmwareCimCollectionSet();

        collectionSet.setCollectionTimestamp(new Date());

        collectionSet.setStatus(ServiceCollector.COLLECTION_FAILED);

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
            logger.warn("Error connecting VMware management server '{}': '{}' exception: {} cause: '{}'", vmwareManagementServer, e.getMessage(), e.getClass().getName(), e.getCause());
            return collectionSet;
        } catch (RemoteException e) {
            logger.warn("Error connecting VMware management server '{}': '{}' exception: {} cause: '{}'", vmwareManagementServer, e.getMessage(), e.getClass().getName(), e.getCause());
            return collectionSet;
        }

        HostSystem hostSystem = vmwareViJavaAccess.getHostSystemByManagedObjectId(vmwareManagedObjectId);

        String powerState = null;

        if (hostSystem == null) {
            logger.debug("hostSystem=null");
        } else {
            HostRuntimeInfo hostRuntimeInfo = hostSystem.getRuntime();

            if (hostRuntimeInfo == null) {
                logger.debug("hostRuntimeInfo=null");
            } else {
                HostSystemPowerState hostSystemPowerState = hostRuntimeInfo.getPowerState();
                if (hostSystemPowerState == null) {
                    logger.debug("hostSystemPowerState=null");
                } else {
                    powerState = hostSystemPowerState.toString();
                }
            }
        }

        logger.debug("The power state for host system '{}' is '{}'", vmwareManagedObjectId, powerState);

        if ("poweredOn".equals(powerState)) {
            HashMap<String, List<CIMObject>> cimObjects = new HashMap<String, List<CIMObject>>();

            for (final VmwareCimGroup vmwareCimGroup : collection.getVmwareCimGroup()) {

                String cimClass = vmwareCimGroup.getCimClass();

                if (!cimObjects.containsKey(cimClass)) {
                    List<CIMObject> cimList = null;
                    try {
                        cimList = vmwareViJavaAccess.queryCimObjects(hostSystem, cimClass, InetAddressUtils.str(agent.getAddress()));
                    } catch (Exception e) {
                        logger.warn("Error retrieving CIM values from host system '{}'. Error message: '{}'", vmwareManagedObjectId, e.getMessage());
                        return collectionSet;
                    } finally {
                        vmwareViJavaAccess.disconnect();
                    }
                    cimObjects.put(cimClass, cimList);
                }

                final List<CIMObject> cimList = cimObjects.get(cimClass);

                if (cimList == null) {
                    logger.warn("Error getting objects of CIM class '{}' from host system '{}'", cimClass, vmwareManagedObjectId);
                    continue;

                }

                String keyAttribute = vmwareCimGroup.getKey();
                String attributeValue = vmwareCimGroup.getValue();
                String instanceAttribute = vmwareCimGroup.getInstance();

                for (CIMObject cimObject : cimList) {
                    boolean addObject = false;

                    if (keyAttribute != null && attributeValue != null) {
                        String cimObjectValue = vmwareViJavaAccess.getPropertyOfCimObject(cimObject, keyAttribute);

                        if (attributeValue.equals(cimObjectValue)) {
                            addObject = true;
                        } else {
                            addObject = false;
                        }
                    } else {
                        addObject = true;
                    }

                    if (addObject) {
                        String instance = vmwareViJavaAccess.getPropertyOfCimObject(cimObject, instanceAttribute);
                        VmwareCimCollectionResource vmwareCollectionResource = new VmwareCimMultiInstanceCollectionResource(agent, instance, vmwareCimGroup.getResourceType());

                        for (Attrib attrib : vmwareCimGroup.getAttrib()) {
                            final VmwareCimCollectionAttributeType attribType = m_attribTypeList.get(attrib.getName());
                            vmwareCollectionResource.setAttributeValue(attribType, vmwareViJavaAccess.getPropertyOfCimObject(cimObject, attrib.getName()));
                            logger.debug("Storing multi instance value " + attrib.getName() + "[" + instance + "]='" + vmwareViJavaAccess.getPropertyOfCimObject(cimObject, attrib.getName()) + "' for node " + agent.getNodeId());
                        }
                        collectionSet.getCollectionResources().add(vmwareCollectionResource);
                    }
                }
            }
            collectionSet.setStatus(ServiceCollector.COLLECTION_SUCCEEDED);
        }

        vmwareViJavaAccess.disconnect();

        return collectionSet;

    }


    /**
     * Returns the Rrd repository for this object.
     *
     * @param collectionName the collection's name
     * @return the Rrd repository
     */
    @Override
    public RrdRepository getRrdRepository(final String collectionName) {
        return m_vmwareCimDatacollectionConfigDao.getRrdRepository(collectionName);
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
