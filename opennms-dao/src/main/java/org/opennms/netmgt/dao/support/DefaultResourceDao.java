//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2009 Jan 26: added getResourceListById and getChildResourceList - part of ksc performance improvement. - ayres@opennms.org
// 2008 Oct 22: Update to use new getResourceById/loadResourceById methods. - dj@opennms.org
// 2007 Sep 09: Catch DataAccessException in getResourceById and throw as a ObjectRetrievalFailureException with the resource ID. - dj@opennms.org
// 2007 May 12: Clean up imports. - dj@opennms.org
// 2007 Apr 05: Add public constant for the strings.properties file name. - dj@opennms.org
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.netmgt.dao.support;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.opennms.core.utils.IntSet;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.CollectdConfigFactory;
import org.opennms.netmgt.config.DataCollectionConfig;
import org.opennms.netmgt.config.StorageStrategy;
import org.opennms.netmgt.dao.LocationMonitorDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.ResourceDao;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsLocationMonitor;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.OnmsResourceType;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.util.Assert;

/**
 * Encapsulates all SNMP performance reporting for the web user interface.
 *
 * @author <a href="mailto:seth@opennms.org">Seth Leger </a>
 * @author <a href="mailto:larry@opennms.org">Lawrence Karnowski </a>
 * @author <a href="http://www.opennms.org">OpenNMS </a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:seth@opennms.org">Seth Leger </a>
 * @author <a href="mailto:larry@opennms.org">Lawrence Karnowski </a>
 * @author <a href="http://www.opennms.org">OpenNMS </a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:seth@opennms.org">Seth Leger </a>
 * @author <a href="mailto:larry@opennms.org">Lawrence Karnowski </a>
 * @author <a href="http://www.opennms.org">OpenNMS </a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:seth@opennms.org">Seth Leger </a>
 * @author <a href="mailto:larry@opennms.org">Lawrence Karnowski </a>
 * @author <a href="http://www.opennms.org">OpenNMS </a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 */
public class DefaultResourceDao implements ResourceDao, InitializingBean {
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

    private NodeDao m_nodeDao;
    private LocationMonitorDao m_locationMonitorDao;
    private File m_rrdDirectory;
    private CollectdConfigFactory m_collectdConfig;
    private DataCollectionConfig m_dataCollectionConfig;

    private Map<String, OnmsResourceType> m_resourceTypes;
    private NodeResourceType m_nodeResourceType;
    private DomainResourceType m_domainResourceType;
    
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
    public File getRrdDirectory() {
        return m_rrdDirectory;
    }
    
    /** {@inheritDoc} */
    public File getRrdDirectory(boolean verify) {
        if (verify && !getRrdDirectory().isDirectory()) {
            throw new ObjectRetrievalFailureException("RRD directory does not exist: " + getRrdDirectory().getAbsolutePath(), getRrdDirectory());
        }
        
        return getRrdDirectory();
    }

    /**
     * <p>getDataCollectionConfig</p>
     *
     * @return a {@link org.opennms.netmgt.config.DataCollectionConfig} object.
     */
    public DataCollectionConfig getDataCollectionConfig() {
        return m_dataCollectionConfig;
    }

    /**
     * <p>setDataCollectionConfig</p>
     *
     * @param dataCollectionConfig a {@link org.opennms.netmgt.config.DataCollectionConfig} object.
     */
    public void setDataCollectionConfig(DataCollectionConfig dataCollectionConfig) {
        m_dataCollectionConfig = dataCollectionConfig;
    }
    
    /**
     * <p>getNodeDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.NodeDao} object.
     */
    public NodeDao getNodeDao() {
        return m_nodeDao;
    }

    /**
     * <p>setNodeDao</p>
     *
     * @param nodeDao a {@link org.opennms.netmgt.dao.NodeDao} object.
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
     * @return a {@link org.opennms.netmgt.dao.LocationMonitorDao} object.
     */
    public LocationMonitorDao getLocationMonitorDao() {
        return m_locationMonitorDao;
    }
    
    /**
     * <p>setLocationMonitorDao</p>
     *
     * @param locationMonitorDao a {@link org.opennms.netmgt.dao.LocationMonitorDao} object.
     */
    public void setLocationMonitorDao(LocationMonitorDao locationMonitorDao) {
        m_locationMonitorDao = locationMonitorDao;
    }

    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.io.IOException if any.
     */
    public void afterPropertiesSet() throws IOException {
        if (m_rrdDirectory == null) {
            throw new IllegalStateException("rrdDirectory property has not been set");
        }
        
        if (m_collectdConfig == null) {
            throw new IllegalStateException("collectdConfig property has not been set");
        }
        
        if (m_dataCollectionConfig == null) {
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

        m_resourceTypes = resourceTypes;
    }

    private Map<String, GenericIndexResourceType> getGenericIndexResourceTypes() {
        Map<String, GenericIndexResourceType> resourceTypes;
        resourceTypes = new LinkedHashMap<String, GenericIndexResourceType>();

        Map<String, org.opennms.netmgt.config.datacollection.ResourceType> configuredResourceTypes =
            m_dataCollectionConfig.getConfiguredResourceTypes();
        for (org.opennms.netmgt.config.datacollection.ResourceType resourceType : configuredResourceTypes.values()) {
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
    public Collection<OnmsResourceType> getResourceTypes() {
        return m_resourceTypes.values();
    }
    
    /** {@inheritDoc} */
    public OnmsResource getResourceById(String id) {
        try {
            return loadResourceById(id);
        } catch (ObjectRetrievalFailureException e) {
            return null;
        }
    }

    /** {@inheritDoc} */
    public OnmsResource loadResourceById(String id) {
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
            } catch (DataAccessException e) {
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
        
        return resource;
    }

    /** {@inheritDoc} */
    public List<OnmsResource> getResourceListById(String id) {
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
            } catch (DataAccessException e) {
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
    protected OnmsResource getTopLevelResource(String resourceType, String resource) {
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
        if (log().isDebugEnabled()) {
            log().debug("DefaultResourceDao: getChildResourceList for " + parentResource.toString());
        }
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
    public List<OnmsResource> findNodeResources() {
        List<OnmsResource> resources = new LinkedList<OnmsResource>();

        IntSet snmpNodes = findSnmpNodeDirectories(); 
        Set<String> responseTimeInterfaces =
            findChildrenMatchingFilter(new File(getRrdDirectory(), RESPONSE_DIRECTORY), RrdFileConstants.INTERFACE_DIRECTORY_FILTER);
        Set<String> distributedResponseTimeInterfaces =
            findChildrenChildrenMatchingFilter(new File(new File(getRrdDirectory(), RESPONSE_DIRECTORY), "distributed"), RrdFileConstants.INTERFACE_DIRECTORY_FILTER);

        List<OnmsNode> nodes = m_nodeDao.findAll();
        IntSet nodesFound = new IntSet();
        for (OnmsNode node : nodes) {
            if (nodesFound.contains(node.getId())) {
                continue;
            }

            boolean found = false;
            if (snmpNodes.contains(node.getId())) {
                found = true;
            } else if (responseTimeInterfaces.size() > 0 || distributedResponseTimeInterfaces.size() > 0) {
                for (OnmsIpInterface ip : node.getIpInterfaces()) {
                    if (responseTimeInterfaces.contains(ip.getIpAddress()) || distributedResponseTimeInterfaces.contains(ip.getIpAddress())) {
                        found = true;
                        break;
                    }
                }
            }

            if (found) {
                resources.add(m_nodeResourceType.createChildResource(node));
                nodesFound.add(node.getId());
            }
        }

        return resources;
    }
    
    /**
     * Returns a list of resources for domains.
     *
     * @return a {@link java.util.List} object.
     */
    public List<OnmsResource> findDomainResources() {
        List<OnmsResource> resources = new LinkedList<OnmsResource>();
        
        File snmp = new File(getRrdDirectory(), SNMP_DIRECTORY);

        // Get all of the non-numeric directory names in the RRD directory; these
        // are the names of the domains that have performance data
        File[] domainDirs = snmp.listFiles(RrdFileConstants.DOMAIN_DIRECTORY_FILTER);

        if (domainDirs != null && domainDirs.length > 0) {
            for (File domainDir : domainDirs) {
                if (m_collectdConfig.domainExists(domainDir.getName())
                        || m_collectdConfig.packageExists(domainDir.getName())) {
                    resources.add(m_domainResourceType.createChildResource(domainDir.getName()));
                }
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
        String[] ident = resource.split(":");

        OnmsNode node = m_nodeDao.findByForeignId(ident[0], ident[1]);
        if (node == null) {
            throw new ObjectRetrievalFailureException(OnmsNode.class, resource, "Top-level resource of resource type node could not be found: " + resource, null);
        }

        OnmsResource onmsResource = getResourceForNode(node);

        return onmsResource;
    }

    /**
     * <p>getDomainEntityResource</p>
     *
     * @param domain a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.OnmsResource} object.
     */
    protected OnmsResource getDomainEntityResource(String domain) {
        if (!m_collectdConfig.domainExists(domain)
                && !m_collectdConfig.packageExists(domain)) {
            throw new ObjectRetrievalFailureException(OnmsResource.class, domain, "Domain not found as a configured domain or package in collectd configuration", null);
        }
        
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

    private Set<String> findChildrenMatchingFilter(File directory, FileFilter filter) {
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
    private Set<String> findChildrenChildrenMatchingFilter(File directory, FileFilter filter) {
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

    /** {@inheritDoc} */
    public OnmsResource getResourceForIpInterface(OnmsIpInterface ipInterface) {
        Assert.notNull(ipInterface, "ipInterface argument must not be null");
        Assert.notNull(ipInterface.getNode(), "getNode() on ipInterface must not return null");
        
        return getChildResourceForNode(ipInterface.getNode(), "responseTime", ipInterface.getIpAddress());
    }

    /** {@inheritDoc} */
    public OnmsResource getResourceForIpInterface(OnmsIpInterface ipInterface, OnmsLocationMonitor locMon) {
        Assert.notNull(ipInterface, "ipInterface argument must not be null");
        Assert.notNull(locMon, "locMon argument must not be null");
        Assert.notNull(ipInterface.getNode(), "getNode() on ipInterface must not return null");
        
        return getChildResourceForNode(ipInterface.getNode(), "distributedStatus", locMon.getId() + File.separator + ipInterface.getIpAddress());
    }
    
    /**
     * <p>findTopLevelResources</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<OnmsResource> findTopLevelResources() {
        List<OnmsResource> resources = new ArrayList<OnmsResource>();
        resources.addAll(findNodeResources());
        resources.addAll(findDomainResources());
        return resources;
    }
    private ThreadCategory log() {
        return ThreadCategory.getInstance();
    }
}
