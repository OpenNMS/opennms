//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2006 Sep 25: fix for bug 1651
// 2006 Aug 24: Better error messages and allow config to be passed in via an
//              InputStream. - dj@opennms.org
// 2005 Oct 02: Use File.separator to join file path components instead of "/". -- DJ Gregor
//
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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

package org.opennms.web.performance;

import java.io.File;
import java.io.IOException;
import java.lang.Integer;
import java.lang.reflect.UndeclaredThrowableException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.resource.Vault;
import org.opennms.core.utils.IntSet;
import org.opennms.netmgt.collectd.StorageStrategy;
import org.opennms.netmgt.config.CollectdConfigFactory;
import org.opennms.netmgt.config.DataCollectionConfigFactory;
import org.opennms.netmgt.config.datacollection.ResourceType;
import org.opennms.netmgt.utils.RrdFileConstants;
import org.opennms.web.graph.GraphResourceDao;
import org.opennms.web.graph.PrefabGraph;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.orm.ObjectRetrievalFailureException;

/**
 * Encapsulates all SNMP performance reporting for the web user interface.
 * 
 * @author <a href="mailto:seth@opennms.org">Seth Leger </a>
 * @author <a href="mailto:larry@opennms.org">Lawrence Karnowski </a>
 * @author <a href="http://www.opennms.org">OpenNMS </a>
 */
public class PerformanceModel extends GraphResourceDao {
    public static final String INTERFACE_GRAPH_TYPE = "interface";

    public static final String RESPONSE_DIRECTORY = "response";
    public static final String SNMP_DIRECTORY = "snmp";

    private Map<String, GraphResourceType> m_resourceTypes;

    public PerformanceModel() throws IOException {
        initResourceTypes();
    }

    private void initResourceTypes() throws IOException {
        Map<String, GraphResourceType> resourceTypes;
        resourceTypes = new LinkedHashMap<String, GraphResourceType>();
        GraphResourceType resourceType;
        
        resourceType = new NodeGraphResourceType(this);
        resourceTypes.put(resourceType.getName(), resourceType);
        
        resourceType = new InterfaceGraphResourceType(this);
        resourceTypes.put(resourceType.getName(), resourceType);
        
        resourceType = new ResponseTimeGraphResourceType(this);
        resourceTypes.put(resourceType.getName(), resourceType);
        
        resourceType = new DistributedStatusGraphResourceType(this);
        resourceTypes.put(resourceType.getName(), resourceType);
        
        resourceTypes.putAll(getGenericIndexGraphResourceTypes());

        m_resourceTypes = resourceTypes;
    }

    private Map<String, GenericIndexGraphResourceType> getGenericIndexGraphResourceTypes() {
        Map<String, GenericIndexGraphResourceType> resourceTypes;
        resourceTypes = new LinkedHashMap<String, GenericIndexGraphResourceType>();
        
        try {
            DataCollectionConfigFactory.init();
        } catch (Exception e) {
            throw new DataAccessResourceFailureException("Could not initialize DataCollectionConfigFactory", e);
        }
        Map<String, ResourceType> configuredResourceTypes =
            DataCollectionConfigFactory.getInstance().getConfiguredResourceTypes();
        for (ResourceType resourceType : configuredResourceTypes.values()) {
            String className = resourceType.getStorageStrategy().getClazz();
            Class cinst;
            try {
                cinst = Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new ObjectRetrievalFailureException(StorageStrategy.class,
                                                          className,
                                                          "Could not load class",
                                                          e);
            }
            StorageStrategy storageStrategy;
            try {
                storageStrategy = (StorageStrategy) cinst.newInstance();
            } catch (InstantiationException e) {
                throw new ObjectRetrievalFailureException(StorageStrategy.class,
                                                          className,
                                                          "Could not instantiate",
                                                          e);
            } catch (IllegalAccessException e) {
                throw new ObjectRetrievalFailureException(StorageStrategy.class,
                                                          className,
                                                          "Could not instantiate",
                                                          e);
            }
            
            storageStrategy.setResourceTypeName(resourceType.getName());
            
            GenericIndexGraphResourceType graphResourceType =
                new GenericIndexGraphResourceType(this,
                                                  resourceType.getName(),
                                                  resourceType.getLabel(),
                                                  storageStrategy);
            resourceTypes.put(graphResourceType.getName(), graphResourceType);
        }
        return resourceTypes;
    }

    /**
     * Returns a list of data structures representing the nodes that have SNMP
     * performance data collected.
     * 
     * <p>
     * First the list of RRD files is collected. From those filenames, the IP
     * address is extracted from each. A list of unique IP addresses is created,
     * discarding the duplicates. At the same time, a mapping of unique IP
     * address to RRD files is created. Then a database call is made to
     * determine the node identifier and human-readable label for each node
     * containing the IP addresses. From that list, an array of data structures,
     * <code>QueryableNode</code>s, are created.
     * </p>
     */
    public QueryableNode[] getQueryableNodes() throws SQLException {
        File snmp = new File(getRrdDirectory(), SNMP_DIRECTORY);

        // Get all of the numeric directory names in the RRD directory; these
        // are the nodeids of the nodes that have performance data
        File[] nodeDirs =
	    snmp.listFiles(RrdFileConstants.NODE_DIRECTORY_FILTER);

        if (nodeDirs == null || nodeDirs.length == 0) {
	    return new QueryableNode[0];
	}

	List<QueryableNode> nodeList = new LinkedList<QueryableNode>();

	// Construct a set containing the nodeIds that are queryable
        IntSet queryableIds = new IntSet();
	for (int i = 0; i < nodeDirs.length; i++) {
	    String fileName = nodeDirs[i].getName();
	    int nodeId = Integer.parseInt(fileName);
	    queryableIds.add(nodeId);
	}

	// create the main stem of the select statement
	StringBuffer select = new StringBuffer("SELECT DISTINCT NODEID, NODELABEL FROM NODE WHERE NODETYPE != 'D' ORDER BY NODELABEL");
	
	Connection conn = Vault.getDbConnection();

	Statement stmt = null;
	ResultSet rs = null;
	try {
	    stmt = conn.createStatement();
	    rs = stmt.executeQuery(select.toString());

	    while (rs.next()) {
		int nodeId = rs.getInt("nodeid");

		if (queryableIds.contains(nodeId)) {
		    String nodeLabel = rs.getString("nodeLabel");
		    nodeList.add(new QueryableNode(nodeId, nodeLabel));
		}
	    }
	} finally {
	    if (rs != null)
		rs.close();
	    if (stmt != null)
		stmt.close();
	    Vault.releaseDbConnection(conn);
	}

	return nodeList.toArray(new QueryableNode[nodeList.size()]);
    }

    /**
     * Returns a list of data structures representing the domains that have SNMP
     * performance data collected.
     *
     * <p>
     * First the list of RRD directories is collected. From those directories,
     * those that are not integers are selected, and verified by comparing
     * with the collectd configuration.
     * </p>
     */
    public String[] getQueryableDomains() {
        List<String> domainList = new LinkedList<String>();
        
        File snmp = new File(getRrdDirectory(), SNMP_DIRECTORY);

        // Get all of the non-numeric directory names in the RRD directory; these
        // are the names of the domains that have performance data
        File[] domainDirs = snmp.listFiles(RrdFileConstants.DOMAIN_DIRECTORY_FILTER);

        if (domainDirs != null && domainDirs.length > 0) {

            try {
                CollectdConfigFactory.init();

                for(int i = 0; i < domainDirs.length; i++) {
                    if(CollectdConfigFactory.getInstance().domainExists(domainDirs[i].getName()) || CollectdConfigFactory.getInstance().packageExists(domainDirs[i].getName())) {
                        domainList.add(domainDirs[i].getName());
                    }
                }
            } catch (IOException iE) {
                throw new UndeclaredThrowableException(iE);
            } catch (MarshalException mE) {
                throw new UndeclaredThrowableException(mE);
            } catch  (ValidationException vE) {
                throw new UndeclaredThrowableException(vE);
            }
        }
        Iterator<String> iter = domainList.iterator();
        String[] domains = new String[domainList.size()];
        for (int i = 0; i < domainList.size(); i++) {
            domains[i] = iter.next();
        }

        return domains;
    }

    public File getNodeDirectory(int nodeId, boolean verify) {
        return getNodeDirectory(Integer.toString(nodeId), verify);
    }
    
    public File getNodeDirectory(String nodeId, boolean verify) throws ObjectRetrievalFailureException {
        File nodeDirectory = new File(getRrdDirectory(verify), nodeId);

        if (verify && !nodeDirectory.isDirectory()) {
            throw new ObjectRetrievalFailureException(File.class, "No node directory exists for node " + nodeId + ": " + nodeDirectory);
        }
        
        return nodeDirectory;
    }
    
    public ArrayList<String> getQueryableInterfacesForNode(int nodeId) {
        return getQueryableInterfacesForNode(String.valueOf(nodeId));
    }

    public ArrayList<String> getQueryableInterfacesForNode(String nodeId) {
        if (nodeId == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        ArrayList<String> intfs = new ArrayList<String>();
        File nodeDir = getNodeDirectory(nodeId, true);

	File[] intfDirs =
	    nodeDir.listFiles(RrdFileConstants.INTERFACE_DIRECTORY_FILTER);

        if (intfDirs != null && intfDirs.length > 0) {
            intfs.ensureCapacity(intfDirs.length);
            for (int i = 0; i < intfDirs.length; i++) {
		intfs.add(intfDirs[i].getName());
	    }
        }

        return intfs;
    }

    public ArrayList<String> getQueryableInterfacesForDomain(String domain) {
        if (domain == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        ArrayList<String> intfs = new ArrayList<String>();
        File snmp = new File(getRrdDirectory(), SNMP_DIRECTORY);
        File domainDir = new File(snmp, domain);

        if (!domainDir.exists() || !domainDir.isDirectory()) {
            throw new IllegalArgumentException("No such directory: " + domainDir);
        }

        File[] intfDirs = domainDir.listFiles(RrdFileConstants.INTERFACE_DIRECTORY_FILTER);

        if (intfDirs != null && intfDirs.length > 0) {
            intfs.ensureCapacity(intfDirs.length);
            for (int i = 0; i < intfDirs.length; i++) {
                intfs.add(intfDirs[i].getName());
            }
        }

        return intfs;
    }

    public boolean encodeNodeIdInRRDParm() {
        return true;
    }

    public String getType() {
        return "performance";
    }

    /**
     * Return a human-readable description (usually an IP address or hostname)
     * for the interface given.
     */
    public String getHumanReadableNameForIfLabel(int nodeId, String ifLabel)
		throws SQLException {
	return getHumanReadableNameForIfLabel(nodeId, ifLabel, true);
    }
    
    public Collection<GraphResourceType> getResourceTypesForNode(int nodeId) {
        Collection<GraphResourceType> in = m_resourceTypes.values();
        Collection<GraphResourceType> out = new LinkedList<GraphResourceType>();
        for (GraphResourceType a : in) {
            boolean onNode = a.isResourceTypeOnNode(nodeId);
            if (log().isDebugEnabled()) {
                log().debug("Resource " + a.getName() + " is on node "
                            + nodeId + ": " + onNode);
            }
            if (onNode) {
                out.add(a);
            }
        }
        return out;
    }
    
    public Collection<GraphResourceType> getResourceTypesForDomain(String domain) {
        Collection<GraphResourceType> in = m_resourceTypes.values();
        Collection<GraphResourceType> out = new LinkedList<GraphResourceType>();
        for (GraphResourceType a : in) {
            if (a.isResourceTypeOnDomain(domain)) {
                out.add(a);
            }
        }
        return out;
    }
    
    public GraphResourceType getResourceTypeByName(String name) {
        GraphResourceType resourceType = m_resourceTypes.get(name);
        if (resourceType == null) {
            throw new ObjectRetrievalFailureException(GraphResourceType.class,
                                                      name);
        }
        
        return resourceType;
    }
    
    public Map<GraphResourceType,List<GraphResource>> getResourceForNode(int nodeId) {
        Collection<GraphResourceType> in = getResourceTypesForNode(nodeId);
        Map<GraphResourceType,List<GraphResource>> out =
            new LinkedHashMap<GraphResourceType,List<GraphResource>>();
        for (GraphResourceType a : in) {
            out.put(a, a.getResourcesForNode(nodeId));
        }
        return out;
    }
    
    public Map<GraphResourceType,List<GraphResource>> getResourceForDomain(String domain) {
        Collection<GraphResourceType> in = getResourceTypesForDomain(domain);
        Map<GraphResourceType,List<GraphResource>> out =
            new LinkedHashMap<GraphResourceType,List<GraphResource>>();
        for (GraphResourceType a : in) {
            out.put(a, a.getResourcesForDomain(domain));
        }
        return out;
    }

    public String getRelativePathForAttribute(String resourceType, String resourceParent, String resource, String attribute) {
        GraphResourceType rt = getResourceTypeByName(resourceType);
        return rt.getRelativePathForAttribute(resourceParent, resource, attribute);
    }

    public PrefabGraph[] getPrefabGraphs(String parentResourceType,
            String parentResource,
            String resourceType,
            String resource) {
        GraphResource r;
        
        if ("node".equals(parentResourceType)) {
            int nodeId;
            try {
                nodeId = Integer.parseInt(parentResource);
            } catch (NumberFormatException e) {
                throw new ObjectRetrievalFailureException("Parent resources of resource type node is not numeric: " + parentResource, parentResource);
            }
            r = getResourceForNodeResourceResourceType(nodeId, resource, resourceType);
        } else if ("domain".equals(parentResourceType)) {
            r = getResourceForDomainResourceResourceType(parentResource, resource, resourceType);
        } else {
            throw new ObjectRetrievalFailureException("Does not support parent resource type '" + parentResourceType + "'", parentResourceType);
        }
        
        Set<GraphAttribute> attributes = r.getAttributes();
        
        PrefabGraph[] availablePrefabGraphs = getQueries();
        return getQueriesByResourceTypeAttributes(resourceType, attributes, availablePrefabGraphs);
    }
}
