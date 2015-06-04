/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.support;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang.CharEncoding;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.api.CollectdConfigFactory;
import org.opennms.netmgt.config.api.DataCollectionConfigDao;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.LocationMonitorDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.dao.api.ResourceStorageDao;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsLocationMonitor;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.OnmsResourceType;
import org.opennms.netmgt.model.ResourceTypeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.util.Assert;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;

/**
 * Retrieves and enumerates elements from the resource tree.
 *
 * This class is responsible for maintaining the list of
 * resource types and coordinating amongst these.
 *
 * All resource type specific logic should be contained in
 * the resource type implementations rather than this class.
 *
 * @author <a href="mailto:jesse@opennms.org">Jesse White</a>
 * @author <a href="mailto:seth@opennms.org">Seth Leger </a>
 * @author <a href="mailto:larry@opennms.org">Lawrence Karnowski </a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class DefaultResourceDao implements ResourceDao, InitializingBean {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultResourceDao.class);

    private static final Pattern RESOURCE_ID_PATTERN = Pattern.compile("([^\\[]+)\\[([^\\]]*)\\](?:\\.|$)");

    private ResourceStorageDao m_resourceStorageDao;
    private NodeDao m_nodeDao;
    private LocationMonitorDao m_locationMonitorDao;
    private IpInterfaceDao m_ipInterfaceDao;
    private CollectdConfigFactory m_collectdConfig;
    private DataCollectionConfigDao m_dataCollectionConfigDao;
    private Date m_lastUpdateDataCollectionConfig;

    private Map<String, OnmsResourceType> m_resourceTypes = Maps.newHashMap();
    private NodeResourceType m_nodeResourceType;
    private NodeSourceResourceType m_nodeSourceResourceType;

    /**
     * <p>Constructor for DefaultResourceDao.</p>
     */
    public DefaultResourceDao() {
    }

    /**
     * <p>getDataCollectionConfig</p>
     *
     * @return a {@link org.opennms.netmgt.config.api.DataCollectionConfigDao} object.
     */
    public DataCollectionConfigDao getDataCollectionConfigDao() {
        return m_dataCollectionConfigDao;
    }

    /**
     * <p>setDataCollectionConfig</p>
     *
     * @param dataCollectionConfigDao a {@link org.opennms.netmgt.config.api.DataCollectionConfigDao} object.
     */
    public void setDataCollectionConfigDao(DataCollectionConfigDao dataCollectionConfigDao) {
        m_dataCollectionConfigDao = dataCollectionConfigDao;
    }
    
    /**
     * <p>getNodeDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.api.NodeDao} object.
     */
    public NodeDao getNodeDao() {
        return m_nodeDao;
    }

    /**
     * <p>setNodeDao</p>
     *
     * @param nodeDao a {@link org.opennms.netmgt.dao.api.NodeDao} object.
     */
    public void setNodeDao(NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }
    
    /**
     * <p>getCollectdConfig</p>
     *
     * @return a {@link org.opennms.netmgt.config.CollectdConfigFactory} object.
     */
    public CollectdConfigFactory getCollectdConfig() {
        return m_collectdConfig;
    }

    /**
     * <p>setCollectdConfig</p>
     *
     * @param collectdConfig a {@link org.opennms.netmgt.config.CollectdConfigFactory} object.
     */
    public void setCollectdConfig(CollectdConfigFactory collectdConfig) {
        m_collectdConfig = collectdConfig;
    }
    
    /**
     * <p>getLocationMonitorDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.api.LocationMonitorDao} object.
     */
    public LocationMonitorDao getLocationMonitorDao() {
        return m_locationMonitorDao;
    }
    
    /**
     * <p>setLocationMonitorDao</p>
     *
     * @param locationMonitorDao a {@link org.opennms.netmgt.dao.api.LocationMonitorDao} object.
     */
    public void setLocationMonitorDao(LocationMonitorDao locationMonitorDao) {
        m_locationMonitorDao = locationMonitorDao;
    }

    public IpInterfaceDao getIpInterfaceDao() {
        return m_ipInterfaceDao;
    }

    public void setIpInterfaceDao(IpInterfaceDao ipInterfaceDao) {
        m_ipInterfaceDao = ipInterfaceDao;
    }

    public void setResourceStorageDao(ResourceStorageDao resourceStorageDao) {
        m_resourceStorageDao = resourceStorageDao;

        // The resource types depend on the resource resolver so
        // we reinitialize these when it changes
        if (m_resourceTypes.size() > 0) {
            initResourceTypes();
        }
    }

    public ResourceStorageDao getResourceStorageDao() {
        return m_resourceStorageDao;
    }

    /**
     * <p>afterPropertiesSet</p>
     */
    @Override
    public void afterPropertiesSet() {

        if (m_collectdConfig == null) {
            throw new IllegalStateException("collectdConfig property has not been set");
        }
        
        if (m_dataCollectionConfigDao == null) {
            throw new IllegalStateException("dataCollectionConfig property has not been set");
        }

        if (m_nodeDao == null) {
            throw new IllegalStateException("nodeDao property has not been set");
        }

        if (m_locationMonitorDao == null) {
            throw new IllegalStateException("locationMonitorDao property has not been set");
        }

        if (m_resourceStorageDao == null) {
            throw new IllegalStateException("resourceStorageDao property has not been set");
        }

        initResourceTypes();
    }

    private void initResourceTypes() {
        final Map<String, OnmsResourceType> resourceTypes = Maps.newLinkedHashMap();
        OnmsResourceType resourceType;

        resourceType = new NodeSnmpResourceType(m_resourceStorageDao);
        resourceTypes.put(resourceType.getName(), resourceType);

        resourceType = new InterfaceSnmpResourceType(m_resourceStorageDao);
        resourceTypes.put(resourceType.getName(), resourceType);

        resourceType = new ResponseTimeResourceType(m_resourceStorageDao, m_ipInterfaceDao);
        resourceTypes.put(resourceType.getName(), resourceType);

        resourceType = new DistributedStatusResourceType(m_resourceStorageDao, m_locationMonitorDao);
        resourceTypes.put(resourceType.getName(), resourceType);

        resourceTypes.putAll(GenericIndexResourceType.createTypes(m_dataCollectionConfigDao.getConfiguredResourceTypes(), m_resourceStorageDao));

        m_nodeResourceType = new NodeResourceType(this, m_nodeDao);
        resourceTypes.put(m_nodeResourceType.getName(), m_nodeResourceType);

        resourceType = new DomainResourceType(this, m_resourceStorageDao);
        resourceTypes.put(resourceType.getName(), resourceType);

        m_nodeSourceResourceType = new NodeSourceResourceType(this, m_nodeDao);
        resourceTypes.put(m_nodeSourceResourceType.getName(), m_nodeSourceResourceType);

        m_resourceTypes = resourceTypes;
        m_lastUpdateDataCollectionConfig = m_dataCollectionConfigDao.getLastUpdate();
    }

    /** {@inheritDoc} */
    @Override
    public Collection<OnmsResourceType> getResourceTypes() {
        if (isDataCollectionConfigChanged()) {
            LOG.debug("The data collection configuration has been changed, reloading resource types.");
            initResourceTypes();
        }
        return m_resourceTypes.values();
    }

    /** {@inheritDoc} */
    @Override
    public List<OnmsResource> findTopLevelResources() {
        // Retrieve all top-level resources by passing null as the parent
        final List<OnmsResource> resources = m_resourceTypes.values().parallelStream()
                .map(type -> type.getResourcesForParent(null))
                .flatMap(rs -> rs.stream())
                .collect(Collectors.toList());

        // Handle node resources separately since their effective
        // type (node vs nodeSource) depends on various factors.
        // See getResourceForNode() for details.
        resources.addAll(findNodeResources());

        return resources;
    }

    /**
     * Returns the set of node resources for all nodes in
     * the database that:
     *   1) Are not deleted
     *   2) Have one or more child resources
     */
    protected List<OnmsResource> findNodeResources() {
        return m_nodeDao.findAll().parallelStream()
            // Only return non-deleted nodes - see NMS-2977
            .filter(node -> node.getType() == null || !node.getType().equals("D"))
            .map(node -> getResourceForNode(node))
            .filter(resource -> resource.getChildResources().size() > 0)
            .collect(Collectors.toList());
    }

    /**
     * Fetch a specific resource by string ID.
     *
     * @return Resource or null if resource cannot be found.
     * @throws IllegalArgumentException When the resource ID string does not match the expected regex pattern
     * @throws ObjectRetrievalFailureException If any exceptions are thrown while searching for the resource
     */
    @Override
    public OnmsResource getResourceById(String id) {
        OnmsResource resource = null;

        Matcher m = RESOURCE_ID_PATTERN.matcher(id);
        StringBuffer sb = new StringBuffer();

        while (m.find()) {
            String resourceTypeName = DefaultResourceDao.decode(m.group(1));
            String resourceName = DefaultResourceDao.decode(m.group(2));

            try {
                resource = getChildResource(resource, resourceTypeName, resourceName);
            } catch (Throwable e) {
                LOG.warn("Could not get resource for resource ID \"{}\"", id, e);
                return null;
            }

            m.appendReplacement(sb, "");
        }

        m.appendTail(sb);

        if (sb.length() > 0) {
            LOG.warn("Resource ID '{}' does not match pattern '{}' at '{}'", id, RESOURCE_ID_PATTERN, sb);
            return null;
        } else {
            return resource;
        }
    }

    /**
     * Creates a resource for the given node using the most
     * appropriate type.
     */
    @Override
    public OnmsResource getResourceForNode(OnmsNode node) {
        Assert.notNull(node, "node argument must not be null");

        // Only attempt to create nodeSource types if storeByFs is enabled
        boolean createUsingNodeSourceType = ResourceTypeUtils.isStoreByForeignSource();

        // Don't create using a nodeSource if either the fs or fid are missing
        if (createUsingNodeSourceType && (node.getForeignSource() == null || node.getForeignId() == null)) {
            createUsingNodeSourceType = false;
        }

        // If the nodeSource directory does not exist, but the node directory does
        // then create the resource using the node type instead of the nodeSource type
        if (createUsingNodeSourceType) {
            final boolean nodeSourcePathExists = m_resourceStorageDao.exists(m_nodeSourceResourceType.getResourcePathForNode(node));
            if (!nodeSourcePathExists) {
                final boolean nodePathExists = m_resourceStorageDao.exists(m_nodeResourceType.getResourcePathForNode(node));
                createUsingNodeSourceType = !nodePathExists;
            }
        }

        // Create the resource
        if (createUsingNodeSourceType) {
            return m_nodeSourceResourceType.createResourceForNode(node);
        } else {
            return m_nodeResourceType.createResourceForNode(node);
        }
    }

    /**
     * @return OnmsResource for the <code>distributedStatus</code> resource on the interface or 
     * null if the <code>distributedStatus</code> resource cannot be found for the given IP interface.
     */ 
    @Override
    public OnmsResource getResourceForIpInterface(OnmsIpInterface ipInterface, OnmsLocationMonitor locMon) {
        Assert.notNull(ipInterface, "ipInterface argument must not be null");
        Assert.notNull(locMon, "locMon argument must not be null");
        Assert.notNull(ipInterface.getNode(), "getNode() on ipInterface must not return null");
        
        final String ipAddress = InetAddressUtils.str(ipInterface.getIpAddress());
        final OnmsResource nodeResource = getResourceForNode(ipInterface.getNode());
        return getChildResource(nodeResource, DistributedStatusResourceType.TYPE_NAME, DistributedStatusResourceType.getResourceName(locMon.getId(), ipAddress));
    }

    /**
     * <p>getChildResource</p>
     *
     * @param parentResource a {@link org.opennms.netmgt.model.OnmsResource} object.
     * @param resourceType a {@link java.lang.String} object.
     * @param resource a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.OnmsResource} object.
     */
    protected OnmsResource getChildResource(OnmsResource parentResource, String resourceType, String resource) {
        final OnmsResourceType targetType = m_resourceTypes.get(resourceType);
        if (targetType == null) {
            throw new ObjectRetrievalFailureException(OnmsResource.class, resourceType + "/" + resource,
                    "Unsupported resource type: " + resourceType, null);
        }
        
        final OnmsResource childResource = targetType.getChildByName(parentResource, resource);
        if (childResource != null) {
            LOG.debug("getChildResource: returning resource {}", childResource);
            return childResource;
        }

        throw new ObjectRetrievalFailureException(OnmsResource.class, resourceType + "/" + resource, "Could not find child resource '"
                                      + resource + "' with resource type '" + resourceType + "' on resource '" + resource + "'", null);
    }

    private boolean isDataCollectionConfigChanged() {
        Date current = m_dataCollectionConfigDao.getLastUpdate();
        if (current.after(m_lastUpdateDataCollectionConfig)) {
            m_lastUpdateDataCollectionConfig = current;
            return true;
        }
        return false;
    }

    /**
     * Encapsulate the deprecated decode method to fix it in one place.
     *
     * @param string
     *            string to be decoded
     * @return decoded string
     */
    public static String decode(String string) {
        try {
            return URLDecoder.decode(string, CharEncoding.UTF_8);
        } catch (UnsupportedEncodingException e) {
            // UTF-8 should *never* throw this
            throw Throwables.propagate(e);
        }
    }
}
