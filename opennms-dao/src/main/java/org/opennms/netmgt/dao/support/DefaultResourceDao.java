/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.support;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.IntSet;
import org.opennms.netmgt.config.CollectdConfigFactory;
import org.opennms.netmgt.config.DataCollectionConfigDao;
import org.opennms.netmgt.config.StorageStrategy;
import org.opennms.netmgt.config.datacollection.ResourceType;
import org.opennms.netmgt.dao.api.LocationMonitorDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsLocationMonitor;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.OnmsResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.util.Assert;

/**
 * Encapsulates all SNMP performance reporting for the web user interface.
 *
 * @author <a href="mailto:seth@opennms.org">Seth Leger </a>
 * @author <a href="mailto:larry@opennms.org">Lawrence Karnowski </a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class DefaultResourceDao implements ResourceDao, InitializingBean {
    
    private static final Logger LOG = LoggerFactory.getLogger(DefaultResourceDao.class);
    /**
     * File name to look for in a resource directory for string attributes.
     */
    public static final String STRINGS_PROPERTIES_FILE_NAME = "strings.properties";

    /** Constant <code>INTERFACE_GRAPH_TYPE="interface"</code> */
    public static final String INTERFACE_GRAPH_TYPE = "interface";

    /** Constant <code>RESPONSE_DIRECTORY="response"</code> */
    public static final String RESPONSE_DIRECTORY = "response";
    /** Constant <code>SNMP_DIRECTORY="snmp"</code> */
    public static final String SNMP_DIRECTORY = "snmp";
    
    public static final String FOREIGN_SOURCE_DIRECTORY = "fs";

    private NodeDao m_nodeDao;
    private LocationMonitorDao m_locationMonitorDao;
    private File m_rrdDirectory;
    private CollectdConfigFactory m_collectdConfig;
    private DataCollectionConfigDao m_dataCollectionConfigDao;

    private Map<String, OnmsResourceType> m_resourceTypes;
    private NodeResourceType m_nodeResourceType;
    private DomainResourceType m_domainResourceType;
    private NodeSourceResourceType m_nodeSourceResourceType;
    
    /**
     * <p>Constructor for DefaultResourceDao.</p>
     */
    public DefaultResourceDao() {
    }

    /**
     * <p>setRrdDirectory</p>
     *
     * @param rrdDirectory a {@link java.io.File} object.
     */
    public void setRrdDirectory(File rrdDirectory) {
        m_rrdDirectory = rrdDirectory;
    }

    /**
     * <p>getRrdDirectory</p>
     *
     * @return a {@link java.io.File} object.
     */
    @Override
    public File getRrdDirectory() {
        return m_rrdDirectory;
    }
    
    /** {@inheritDoc} */
    @Override
    public File getRrdDirectory(boolean verify) {
        if (verify && !getRrdDirectory().isDirectory()) {
            throw new ObjectRetrievalFailureException("RRD directory does not exist: " + getRrdDirectory().getAbsolutePath(), getRrdDirectory());
        }
        
        return getRrdDirectory();
    }

    /**
     * <p>getDataCollectionConfig</p>
     *
     * @return a {@link org.opennms.netmgt.config.DataCollectionConfigDao} object.
     */
    public DataCollectionConfigDao getDataCollectionConfigDao() {
        return m_dataCollectionConfigDao;
    }

    /**
     * <p>setDataCollectionConfig</p>
     *
     * @param dataCollectionConfigDao a {@link org.opennms.netmgt.config.DataCollectionConfigDao} object.
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

    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.io.IOException if any.
     */
    @Override
    public void afterPropertiesSet() throws IOException {
        if (m_rrdDirectory == null) {
            throw new IllegalStateException("rrdDirectory property has not been set");
        }
        
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

        initResourceTypes();
    }
    

    private void initResourceTypes() throws IOException {
        Map<String, OnmsResourceType> resourceTypes;
        resourceTypes = new LinkedHashMap<String, OnmsResourceType>();
        OnmsResourceType resourceType;

        resourceType = new NodeSnmpResourceType(this);
        resourceTypes.put(resourceType.getName(), resourceType);
        
        resourceType = new InterfaceSnmpResourceType(this, m_nodeDao);
        resourceTypes.put(resourceType.getName(), resourceType);
        
        resourceType = new ResponseTimeResourceType(this, m_nodeDao);
        resourceTypes.put(resourceType.getName(), resourceType);
        
        resourceType = new DistributedStatusResourceType(this, m_locationMonitorDao);
        resourceTypes.put(resourceType.getName(), resourceType);

        resourceTypes.putAll(getGenericIndexResourceTypes());
        
        m_nodeResourceType = new NodeResourceType(this);
        resourceTypes.put(m_nodeResourceType.getName(), m_nodeResourceType);
        
        m_domainResourceType = new DomainResourceType(this);
        resourceTypes.put(m_domainResourceType.getName(), m_domainResourceType);
        
        m_nodeSourceResourceType = new NodeSourceResourceType(this, m_nodeDao);
        resourceTypes.put(m_nodeSourceResourceType.getName(), m_nodeSourceResourceType);

        m_resourceTypes = resourceTypes;
    }

    private Map<String, GenericIndexResourceType> getGenericIndexResourceTypes() {
        Map<String, GenericIndexResourceType> resourceTypes;
        resourceTypes = new LinkedHashMap<String, GenericIndexResourceType>();

        Map<String, ResourceType> configuredResourceTypes = m_dataCollectionConfigDao.getConfiguredResourceTypes();
        List<ResourceType> resourceTypeList = new LinkedList<ResourceType>(configuredResourceTypes.values());
        Collections.sort(resourceTypeList, new Comparator<ResourceType>() {
            @Override
            public int compare(ResourceType r0, ResourceType r1) {
                return r0.getLabel().compareTo(r1.getLabel());
            }
        });
        for (ResourceType resourceType : resourceTypeList) {
            String className = resourceType.getStorageStrategy().getClazz();
            Class<?> cinst;
            try {
                cinst = Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new ObjectRetrievalFailureException(StorageStrategy.class, className,
                   "Could not load class '" + className + "' for resource type '" + resourceType.getName() + "'", e);
            }
            StorageStrategy storageStrategy;
            try {
                storageStrategy = (StorageStrategy) cinst.newInstance();
            } catch (InstantiationException e) {
                throw new ObjectRetrievalFailureException(StorageStrategy.class, className,
                    "Could not instantiate class '" + className + "' for resource type '" + resourceType.getName() + "'", e);
            } catch (IllegalAccessException e) {
                throw new ObjectRetrievalFailureException(StorageStrategy.class, className,
                    "Could not instantiate class '" + className + "' for resource type '" + resourceType.getName() + "'", e);
            }
            
            storageStrategy.setResourceTypeName(resourceType.getName());
            
            GenericIndexResourceType genericIndexResourceType =
                new GenericIndexResourceType(this,
                                                  resourceType.getName(),
                                                  resourceType.getLabel(),
                                                  resourceType.getResourceLabel(),
                                                  storageStrategy);
            resourceTypes.put(genericIndexResourceType.getName(), genericIndexResourceType);
        }
        return resourceTypes;
    }
    
    /**
     * <p>getResourceTypes</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    @Override
    public Collection<OnmsResourceType> getResourceTypes() {
        return m_resourceTypes.values();
    }
    
    /**
     * Fetch a specific resource by string ID.
     * @return Resource or null if resource cannot be found.
     * @throws IllegalArgumentException When the resource ID string does not match the expected regex pattern
     * @throws ObjectRetrievalFailureException If any exceptions are thrown while searching for the resource
     */
    @Override
    public OnmsResource getResourceById(String id) {
        OnmsResource resource = null;

        Pattern p = Pattern.compile("([^\\[]+)\\[([^\\]]*)\\](?:\\.|$)");
        Matcher m = p.matcher(id);
        StringBuffer sb = new StringBuffer();

        while (m.find()) {
            String resourceTypeName = DefaultResourceDao.decode(m.group(1));
            String resourceName = DefaultResourceDao.decode(m.group(2));

            try {
                if (resource == null) {
                    resource = getTopLevelResource(resourceTypeName, resourceName);
                } else {
                    resource = getChildResource(resource, resourceTypeName, resourceName);
                }
            } catch (Throwable e) {
                LOG.warn("Could not get resource for resource ID \"{}\"", id, e);
                return null;
            }

            m.appendReplacement(sb, "");
        }

        m.appendTail(sb);

        if (sb.length() > 0) {
            LOG.warn("resource ID '{}' does not match pattern '{}' at '{}'", id, p.toString(), sb);
            return null;
        } else {
            return resource;
        }
    }

    /**
     * Fetch a specific list of resources by string ID.
     * @return Resources or null if resources cannot be found.
     * @throws IllegalArgumentException When the resource ID string does not match the expected regex pattern
     * @throws ObjectRetrievalFailureException If any exceptions are thrown while searching for the resource
     */
    @Override
    public List<OnmsResource> getResourceListById(String id) throws IllegalArgumentException, ObjectRetrievalFailureException {
        OnmsResource topLevelResource = null;

        Pattern p = Pattern.compile("([^\\[]+)\\[([^\\]]*)\\](?:\\.|$)");
        Matcher m = p.matcher(id);
        StringBuffer sb = new StringBuffer();
        
        while (m.find()) {
            String resourceTypeName = DefaultResourceDao.decode(m.group(1));
            String resourceName = DefaultResourceDao.decode(m.group(2));

            try {
                if (topLevelResource == null) {
                    topLevelResource = getTopLevelResource(resourceTypeName, resourceName);
                } else {
                    return getChildResourceList(topLevelResource);
                }
            } catch (Throwable e) {
                throw new ObjectRetrievalFailureException(OnmsResource.class, id, "Could not get resource for resource ID '" + id + "'", e);
            }
            
            m.appendReplacement(sb, "");
        }
        
        m.appendTail(sb);
        
        if (sb.length() > 0) {
            throw new IllegalArgumentException("resource ID '" + id
                                               + "' does not match pattern '"
                                               + p.toString() + "' at '"
                                               + sb + "'");
        }
        return null;
    }
    
    /**
     * <p>getTopLevelResource</p>
     *
     * @param resourceType a {@link java.lang.String} object.
     * @param resource a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.OnmsResource} object.
     */
    protected OnmsResource getTopLevelResource(String resourceType, String resource) throws ObjectRetrievalFailureException {
        if ("node".equals(resourceType)) {
            return getNodeEntityResource(resource);
        } else if ("nodeSource".equals(resourceType)) {
            return getForeignSourceNodeEntityResource(resource);
        } else if ("domain".equals(resourceType)) {
            return getDomainEntityResource(resource);
        } else {
            throw new ObjectRetrievalFailureException("Top-level resource type of '" + resourceType + "' is unknown", resourceType);
        }
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
        for (OnmsResource r : parentResource.getChildResources()) {
            if (resourceType.equals(r.getResourceType().getName())
                    && resource.equals(r.getName())) {
                LOG.debug("getChildResource: returning resource {}", r);
                return r;
            }
        }
        
        throw new ObjectRetrievalFailureException(OnmsResource.class, resourceType + "/" + resource, "Could not find child resource '"
                                      + resource + "' with resource type '" + resourceType + "' on resource '" + resource + "'", null);
    }
    
    /**
     * <p>getChildResourceList</p>
     *
     * @param parentResource a {@link org.opennms.netmgt.model.OnmsResource} object.
     * @return a {@link java.util.List} object.
     */
    protected List<OnmsResource> getChildResourceList(OnmsResource parentResource) {
        LOG.debug("DefaultResourceDao: getChildResourceList for {}", parentResource.toString());
        return parentResource.getChildResources();
    }
    
    /**
     * Returns a list of resources for a node.
     *
     * XXX It does not currently fully check that an IP address that is found to have
     * distributed response time data is in the database on the proper node so it can have false positives.
     *
     * @return a {@link java.util.List} object.
     */
    @Override
    public List<OnmsResource> findNodeResources() {
        List<OnmsResource> resources = new LinkedList<OnmsResource>();

        IntSet snmpNodes = findSnmpNodeDirectories(); 
        Set<String> responseTimeInterfaces = findChildrenMatchingFilter(new File(getRrdDirectory(), RESPONSE_DIRECTORY), RrdFileConstants.INTERFACE_DIRECTORY_FILTER);
        Set<String> distributedResponseTimeInterfaces = findChildrenChildrenMatchingFilter(new File(new File(getRrdDirectory(), RESPONSE_DIRECTORY), "distributed"), RrdFileConstants.INTERFACE_DIRECTORY_FILTER);

        // Only returns non-deleted nodes to fix NMS-2977
        // http://issues.opennms.org/browse/NMS-2977
        Collection<Integer> nodeIds = m_nodeDao.getNodeIds();
        IntSet nodesFound = new IntSet();
        for (Integer nodeId : nodeIds) {
            if (nodesFound.contains(nodeId.intValue())) {
                continue;
            }

            boolean found = false;
            OnmsNode node = m_nodeDao.get(nodeId);
            if (snmpNodes.contains(nodeId)) {
                found = true;
            } else if ((responseTimeInterfaces.size() > 0 || distributedResponseTimeInterfaces.size() > 0) && (node.getForeignSource() == null || node.getForeignId() == null)) {
                for (final OnmsIpInterface ip : m_nodeDao.get(nodeId).getIpInterfaces()) {
                    final String addr = InetAddressUtils.str(ip.getIpAddress());
					if (responseTimeInterfaces.contains(addr) || distributedResponseTimeInterfaces.contains(addr)) {
                        found = true;
                        break;
                    }
                }
            }

            if (found) {
                resources.add(m_nodeResourceType.createChildResource(node));
                nodesFound.add(nodeId);
            }
        }

        return resources;
    }
    
    /**
     * Returns a list of resources for a node in a foreign source.
     *
     * XXX It does not currently fully check that an IP address that is found to have
     * distributed response time data is in the database on the proper node so it can have false positives.
     *
     * @return a {@link java.util.List} object.
     */
    @Override
    public List<OnmsResource> findNodeSourceResources() {
        List<OnmsResource> resources = new LinkedList<OnmsResource>();

        Set<String> nodeSources = findNodeSourceDirectories();
        Set<String> responseTimeInterfaces = findChildrenMatchingFilter(new File(getRrdDirectory(), RESPONSE_DIRECTORY), RrdFileConstants.INTERFACE_DIRECTORY_FILTER);
        Set<String> distributedResponseTimeInterfaces = findChildrenChildrenMatchingFilter(new File(new File(getRrdDirectory(), RESPONSE_DIRECTORY), "distributed"), RrdFileConstants.INTERFACE_DIRECTORY_FILTER);

        List<OnmsNode> nodes = m_nodeDao.findAll();
        IntSet nodesFound = new IntSet();
        for (OnmsNode node : nodes) {
            if (nodesFound.contains(node.getId())) {
                continue;
            }

            boolean found = false;
            if (node.getForeignSource() != null && node.getForeignId() != null) {
                if (nodeSources.contains(node.getForeignSource() + ":" + node.getForeignId())) {
                    found = true;
                } else if (responseTimeInterfaces.size() > 0 || distributedResponseTimeInterfaces.size() > 0) {
                    for (final OnmsIpInterface ip : node.getIpInterfaces()) {
                        final String addr = InetAddressUtils.str(ip.getIpAddress());
                                           if (responseTimeInterfaces.contains(addr) || distributedResponseTimeInterfaces.contains(addr)) {
                            found = true;
                            break;
                        }
                    }
                }
            }

            if (found) {
                resources.add(m_nodeSourceResourceType.createChildResource(node.getForeignSource() + ":" + node.getForeignId()));
                nodesFound.add(node.getId());
                LOG.debug("findNodeSourceResources: adding resource for {}:{}", node.getForeignSource(), node.getForeignId());
            }
        }

        return resources;
    }
    
    /**
     * Returns a list of resources for domains.
     *
     * @return a {@link java.util.List} object.
     */
    @Override
    public List<OnmsResource> findDomainResources() {
        List<OnmsResource> resources = new LinkedList<OnmsResource>();
        
        File snmp = new File(getRrdDirectory(), SNMP_DIRECTORY);

        // Get all of the non-numeric directory names in the RRD directory; these
        // are the names of the domains that have performance data
        File[] domainDirs = snmp.listFiles(RrdFileConstants.DOMAIN_DIRECTORY_FILTER);

        if (domainDirs != null && domainDirs.length > 0) {
            for (File domainDir : domainDirs) {
                resources.add(m_domainResourceType.createChildResource(domainDir.getName()));
            }
        }
        
        return resources;
    }
    
    /**
     * <p>getNodeEntityResource</p>
     *
     * @param resource a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.OnmsResource} object.
     */
    protected OnmsResource getNodeEntityResource(String resource) {
        int nodeId;
        try {
            nodeId = Integer.parseInt(resource);
        } catch (NumberFormatException e) {
            throw new ObjectRetrievalFailureException(OnmsNode.class, resource, "Top-level resource of resource type node is not numeric: " + resource, null);
        }
        
        OnmsNode node = m_nodeDao.get(nodeId);
        if (node == null) {
            throw new ObjectRetrievalFailureException(OnmsNode.class, resource, "Top-level resource of resource type node could not be found: " + resource, null);
        }

        OnmsResource onmsResource = getResourceForNode(node);

        return onmsResource;
    }

    /**
     * <p>getForeignSourceNodeEntityResource</p>
     *
     * @param resource a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.OnmsResource} object.
     */
    protected OnmsResource getForeignSourceNodeEntityResource(String resource) {
        
        File idDir = new File(getRrdDirectory(), SNMP_DIRECTORY + File.separator + ResourceTypeUtils.getRelativeNodeSourceDirectory(resource).toString());
        if (idDir.isDirectory() && RrdFileConstants.NODESOURCE_DIRECTORY_FILTER.accept(idDir)) {
            return m_nodeSourceResourceType.createChildResource(resource);
        } else {
           LOG.debug("resource {} not found by foreign source/foreignId. Trying as a node resource instead...", resource);
           String[] ident = resource.split(":");
           OnmsNode node = m_nodeDao.findByForeignId(ident[0], ident[1]);
           if (node == null) {
                throw new ObjectRetrievalFailureException(OnmsNode.class, resource, "Top-level resource of resource type node could not be found: " + resource, null);
           }
           
           OnmsResource onmsResource = getResourceForNode(node);
           
           return onmsResource;
        }
    }

    /**
     * <p>getDomainEntityResource</p>
     *
     * @param domain a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.OnmsResource} object.
     */
    protected OnmsResource getDomainEntityResource(String domain) {
        
        File directory = new File(getRrdDirectory(), SNMP_DIRECTORY);
        File domainDir = new File(directory, domain);
        if (!domainDir.isDirectory()) {
            throw new ObjectRetrievalFailureException(OnmsResource.class, domain, "Domain not found due to domain RRD directory not existing or not a directory: " + domainDir.getAbsolutePath(), null);
        }
        
        if (!RrdFileConstants.DOMAIN_DIRECTORY_FILTER.accept(domainDir)) {
            throw new ObjectRetrievalFailureException(OnmsResource.class, domain, "Domain not found due to domain RRD directory not matching the domain directory filter: " + domainDir.getAbsolutePath(), null);
        }

        return m_domainResourceType.createChildResource(domain);
    }

    private IntSet findSnmpNodeDirectories() {
        IntSet nodes = new IntSet();
        
        File directory = new File(getRrdDirectory(), SNMP_DIRECTORY);
        File[] nodeDirs = directory.listFiles(RrdFileConstants.NODE_DIRECTORY_FILTER);

        if (nodeDirs == null || nodeDirs.length == 0) {
            return nodes;
        }

        for (File nodeDir : nodeDirs) {
            try {
                int nodeId = Integer.parseInt(nodeDir.getName());
                nodes.add(nodeId);
            } catch (NumberFormatException e) {
                // skip... don't add
            }
        }
        
        return nodes;
    }
    
    /**
     * <p>findNodeSourceDirectories</p>
     *
     * @return a Set<String> of directory names.
     */
    public Set<String> findNodeSourceDirectories() {
       Set<String> nodeSourceDirectories = new HashSet<String>();
       File snmpDir = new File(getRrdDirectory(), SNMP_DIRECTORY);
       File forSrcDir = new File(snmpDir, FOREIGN_SOURCE_DIRECTORY);
       File[] sourceDirs = forSrcDir.listFiles(RrdFileConstants.SOURCE_DIRECTORY_FILTER);
       if (sourceDirs != null) {
           for (File sourceDir : sourceDirs) {
               File [] ids = sourceDir.listFiles(RrdFileConstants.NODESOURCE_DIRECTORY_FILTER);
               for (File id : ids) {
                   nodeSourceDirectories.add(sourceDir.getName() + ":" + id.getName());
               }
           }
       }
       
       return nodeSourceDirectories;
       
    }

    private static Set<String> findChildrenMatchingFilter(File directory, FileFilter filter) {
        Set<String> children = new HashSet<String>();
        
        File[] nodeDirs = directory.listFiles(filter);

        if (nodeDirs == null || nodeDirs.length == 0) {
            return children;
        }

        for (File nodeDir : nodeDirs) {
            children.add(nodeDir.getName());
        }
        
        return children;
    }

    /**
     * 
     * @param directory
     * @param filter
     * @return
     * 
     * XXX should include the location monitor in the returned data
     */
    private static Set<String> findChildrenChildrenMatchingFilter(File directory, FileFilter filter) {
        Set<String> children = new HashSet<String>();
        
        File[] locationMonitorDirs = directory.listFiles();
        if (locationMonitorDirs == null) {
            return children;
        }
        
        for (File locationMonitorDir : locationMonitorDirs) {
            File[] intfDirs = locationMonitorDir.listFiles(filter);

            if (intfDirs == null || intfDirs.length == 0) {
                continue;
            }

            for (File intfDir : intfDirs) {
                children.add(intfDir.getName());
            }
        }
        
        return children;
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
            return URLDecoder.decode(string, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // UTF-8 should *never* throw this
            throw new UndeclaredThrowableException(e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public OnmsResource getResourceForNode(OnmsNode node) {
        Assert.notNull(node, "node argument must not be null");
        
        return m_nodeResourceType.createChildResource(node);
    }

    private OnmsResource getChildResourceForNode(OnmsNode node, String resourceTypeName, String resourceName) {
        OnmsResource nodeResource = getResourceForNode(node);
        if (nodeResource == null) {
            return null;
        }
        
        List<OnmsResource> childResources = nodeResource.getChildResources();

        for (OnmsResource childResource : childResources) {
            if (!resourceTypeName.equals(childResource.getResourceType().getName())) {
                continue;
            }
            
            if (resourceName.equals(childResource.getName())) {
                return childResource;
            }
        }

        return null;
    }

    /**
     * @return OnmsResource for the <code>responseTime</code> resource on the interface or 
     * null if the <code>responseTime</code> resource cannot be found for the given IP interface.
     */ 
    @Override
    public OnmsResource getResourceForIpInterface(OnmsIpInterface ipInterface) {
        Assert.notNull(ipInterface, "ipInterface argument must not be null");
        Assert.notNull(ipInterface.getNode(), "getNode() on ipInterface must not return null");

        final String ipAddress = InetAddressUtils.str(ipInterface.getIpAddress());
		return getChildResourceForNode(ipInterface.getNode(), "responseTime", ipAddress);
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
		return getChildResourceForNode(ipInterface.getNode(), "distributedStatus", locMon.getId() + File.separator + ipAddress);
    }
    
    /**
     * <p>findTopLevelResources</p>
     *
     * @return a {@link java.util.List} object.
     */
    @Override
    public List<OnmsResource> findTopLevelResources() {
        List<OnmsResource> resources = new ArrayList<OnmsResource>();
        resources.addAll(findNodeResources());
        resources.addAll(findNodeSourceResources());
        resources.addAll(findDomainResources());
        return resources;
    }
}
