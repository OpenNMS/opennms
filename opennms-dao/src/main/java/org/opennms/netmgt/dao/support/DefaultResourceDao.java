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

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import org.apache.commons.lang.CharEncoding;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.api.CollectdConfigFactory;
import org.opennms.netmgt.config.api.ResourceTypesDao;
import org.opennms.netmgt.config.collectd.Package;
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
import org.opennms.netmgt.model.ResourceId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

    /**
     * Largest depth at which we will find node related metrics:
     * [0] will catch node-level resources
     * [1] will catch interface-level resources
     * [2] will catch generic index type resources
     */
    public static final int MAXIMUM_NODE_METRIC_RESOURCE_DEPTH = 2;

    private ResourceStorageDao m_resourceStorageDao;
    private NodeDao m_nodeDao;
    private LocationMonitorDao m_locationMonitorDao;
    private IpInterfaceDao m_ipInterfaceDao;
    private CollectdConfigFactory m_collectdConfig;
    private ResourceTypesDao m_resourceTypesDao;
    private Date m_lastUpdatedResourceTypesConfig;

    private Map<String, OnmsResourceType> m_resourceTypes = Maps.newHashMap();
    private NodeResourceType m_nodeResourceType;

    /**
     * <p>Constructor for DefaultResourceDao.</p>
     */
    public DefaultResourceDao() {
    }

    public ResourceTypesDao getResourceTypesDao() {
        return m_resourceTypesDao;
    }

    public void setResourceTypesDao(ResourceTypesDao resourceTypesDao) {
        m_resourceTypesDao = Objects.requireNonNull(resourceTypesDao);
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
        
        if (m_resourceTypesDao == null) {
            throw new IllegalStateException("resourceTypesDao property has not been set");
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

        InterfaceSnmpResourceType intfResourceType = new InterfaceSnmpResourceType(m_resourceStorageDao);
        resourceTypes.put(intfResourceType.getName(), intfResourceType);

        resourceType = new InterfaceSnmpByIfIndexResourceType(intfResourceType);
        resourceTypes.put(resourceType.getName(), resourceType);

        resourceType = new ResponseTimeResourceType(m_resourceStorageDao, m_ipInterfaceDao);
        resourceTypes.put(resourceType.getName(), resourceType);

        resourceType = new DistributedStatusResourceType(m_resourceStorageDao, m_locationMonitorDao);
        resourceTypes.put(resourceType.getName(), resourceType);

        resourceTypes.putAll(GenericIndexResourceType.createTypes(m_resourceTypesDao.getResourceTypes(), m_resourceStorageDao));

        m_nodeResourceType = new NodeResourceType(this, m_nodeDao);
        resourceTypes.put(m_nodeResourceType.getName(), m_nodeResourceType);
        // Add 'nodeSource' as an alias to for the 'node' resource type to preserve backwards compatibility
        // See NMS-8404 for details
        resourceTypes.put("nodeSource", m_nodeResourceType);

        if (isDomainResourceTypeUsed()) {
            LOG.debug("One or more packages are configured with storeByIfAlias=true. Enabling the domain resource type.");
            resourceType = new DomainResourceType(this, m_resourceStorageDao);
            resourceTypes.put(resourceType.getName(), resourceType);
        } else {
            LOG.debug("No packages are configured with storeByIfAlias=true. Excluding the domain resource type.");
        }

        m_resourceTypes = resourceTypes;
        m_lastUpdatedResourceTypesConfig = m_resourceTypesDao.getLastUpdate();
    }

    /** {@inheritDoc} */
    @Override
    public Collection<OnmsResourceType> getResourceTypes() {
        if (isResourceTypesConfigChanged()) {
            LOG.debug("The resource type configuration has been changed, reloading resource types.");
            initResourceTypes();
        }
        return m_resourceTypes.values();
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly=true)
    public List<OnmsResource> findTopLevelResources() {
        // Retrieve all top-level resources by passing null as the parent
        final List<OnmsResource> resources = m_resourceTypes.values().stream()
                .distinct()
                .map(type -> type.getResourcesForParent(null))
                .flatMap(List::stream)
                .filter(this::hasAnyChildResources)
                .collect(Collectors.toList());
        return resources;
    }

    /**
     * Used to determine whether or not the given (parent) resource
     * has any child resources.
     */
    protected boolean hasAnyChildResources(OnmsResource resource) {
        // The order of the resource types matter here since we want to
        // check for the types are most likely occur first.
        return getResourceTypes().stream()
                .anyMatch(t -> t.isResourceTypeOnParent(resource));
    }

    /**
     * Fetch a specific resource by string ID.
     *
     * @return Resource or null if resource cannot be found.
     * @throws IllegalArgumentException When the resource ID string does not match the expected regex pattern
     * @throws ObjectRetrievalFailureException If any exceptions are thrown while searching for the resource
     */
    @Override
    @Transactional(readOnly=true)
    public OnmsResource getResourceById(final ResourceId id) {
        if (id == null) {
            return null;
        }

        try {
            return getChildResource(id.parent != null ? this.getResourceById(id.parent) : null,
                                    id.type,
                                    id.name);

        } catch (final Throwable e) {
            LOG.warn("Could not get resource for resource ID \"{}\"", id, e);
            return null;
        }
    }

    /**
     * Creates a resource for the given node using the most
     * appropriate type.
     */
    @Override
    public OnmsResource getResourceForNode(OnmsNode node) {
        Assert.notNull(node, "node argument must not be null");
        return m_nodeResourceType.createResourceForNode(node);
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

    @Override
    public boolean deleteResourceById(final ResourceId resourceId) {
        final OnmsResource resource = this.getResourceById(resourceId);
        if (resource == null) {
            return false;
        }

        return deleteResource(resource, true);
    }

    public boolean deleteResource(final OnmsResource resource, boolean recursive) {
        boolean result = false;

        if (recursive) {
            for (final OnmsResource childResource : resource.getChildResources()) {
                result = deleteResource(childResource, recursive) || result;
            }
        }

        result = m_resourceStorageDao.delete(resource.getPath()) || result;

        return result;
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

    private boolean isResourceTypesConfigChanged() {
        Date current = m_resourceTypesDao.getLastUpdate();
        if (current.after(m_lastUpdatedResourceTypesConfig)) {
            m_lastUpdatedResourceTypesConfig = current;
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

    /**
     * For performance reasons, we only enable the {@link DomainResourceType} if
     * one or more packages use it. Here we iterator over all of defined packages,
     * return true if a package uses the domain types, and false otherwise.
     */
    private boolean isDomainResourceTypeUsed() {
        for (Package pkg : m_collectdConfig.getCollectdConfig().getPackages()) {
            if ("true".equalsIgnoreCase(pkg.getStoreByIfAlias())) {
                return true;
            }
        }
        return false;
    }
}
