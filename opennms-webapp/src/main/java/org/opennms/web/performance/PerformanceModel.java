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
// 02 Oct 2005: Use File.separator to join file path components instead of "/". -- DJ Gregor
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
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.Integer;
import java.lang.reflect.UndeclaredThrowableException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.resource.Vault;
import org.opennms.core.utils.IntSet;
import org.opennms.netmgt.collectd.CollectionResource;
import org.opennms.netmgt.collectd.StorageStrategy;
import org.opennms.netmgt.config.CollectdConfigFactory;
import org.opennms.netmgt.config.DataCollectionConfigFactory;
import org.opennms.netmgt.config.datacollection.ResourceType;
import org.opennms.netmgt.utils.IfLabel;
import org.opennms.netmgt.utils.RrdFileConstants;
import org.opennms.web.Util;
import org.opennms.web.graph.PrefabGraph;
import org.opennms.web.graph.GraphModel;
import org.opennms.web.graph.GraphModelAbstract;
import org.opennms.web.graph.GraphModelAbstract.QueryableNode;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.orm.ObjectRetrievalFailureException;

/**
 * Encapsulates all SNMP performance reporting for the web user interface.
 * 
 * @author <a href="mailto:seth@opennms.org">Seth Leger </a>
 * @author <a href="mailto:larry@opennms.org">Lawrence Karnowski </a>
 * @author <a href="http://www.opennms.org">OpenNMS </a>
 */
public class PerformanceModel extends GraphModelAbstract {
    public static final String RRDTOOL_GRAPH_PROPERTIES_FILENAME =
        File.separator + "etc" + File.separator + "snmp-graph.properties";

    public static final String INTERFACE_GRAPH_TYPE = "interface";

    public static final String NODE_GRAPH_TYPE = "node";

    protected String defaultReport;

    private Map<String, GraphResourceType> m_resourceTypes;

    /**
     * Create a new instance.
     * 
     * @param homeDir
     *            the OpenNMS home directory, see {@link Vault#getHomeDir
     *            Vault.getHomeDir}.
     */
    public PerformanceModel(String homeDir) throws IOException {
	loadProperties(homeDir, RRDTOOL_GRAPH_PROPERTIES_FILENAME);
        initResourceTypes();
    }

    private void initResourceTypes() {
        Map<String, GraphResourceType> resourceTypes;
        resourceTypes = new LinkedHashMap<String, GraphResourceType>();
        GraphResourceType resourceType;
        
        resourceType = new NodeGraphResourceType(this);
        resourceTypes.put(resourceType.getName(), resourceType);
        
        resourceType = new InterfaceGraphResourceType(this);
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

    public List<String> getDataSourceList(String nodeId, String intf,
                                          boolean includeNodeQueries) {
        if (nodeId == null || intf == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        File nodeDir = new File(getRrdDirectory(), nodeId);
        File intfDir = new File(nodeDir, intf);

	ArrayList<String> dataSources = new ArrayList<String>();

        if (includeNodeQueries) {
            dataSources.addAll(getDataSourceList(nodeId));
        }

	dataSources.addAll(getDataSourcesInDirectory(intfDir));

	return dataSources;
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
        // Get all of the numeric directory names in the RRD directory; these
        // are the nodeids of the nodes that have performance data
        File[] nodeDirs =
	    getRrdDirectory().listFiles(RrdFileConstants.NODE_DIRECTORY_FILTER);

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

        // Get all of the non-numeric directory names in the RRD directory; these
        // are the names of the domains that have performance data
        File[] domainDirs = getRrdDirectory().listFiles(RrdFileConstants.DOMAIN_DIRECTORY_FILTER);

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

    public File getRrdDirectory(boolean verify) {
        File rrdDirectory = getRrdDirectory();
        
        if (verify && !rrdDirectory.isDirectory()) {
            throw new IllegalArgumentException("RRD directory does not exist");
        }
        
        return rrdDirectory;
    }

    public File getNodeDirectory(int nodeId, boolean verify) {
        return getNodeDirectory(Integer.toString(nodeId), verify);
    }
    
    public File getNodeDirectory(String nodeId, boolean verify) {
        File nodeDirectory = new File(getRrdDirectory(verify), nodeId);

        if (verify && !nodeDirectory.isDirectory()) {
            throw new IllegalArgumentException("No node directory exists for node " + nodeId + ": " + nodeDirectory);
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
        File domainDir = new File(getRrdDirectory(), domain);

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

    public boolean isQueryableNode(int nodeId) {
        return isQueryableNode(String.valueOf(nodeId));
    }

    public boolean isQueryableNode(String nodeId) {
        if (nodeId == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        File nodeDir = new File(getRrdDirectory(), nodeId);
	return RrdFileConstants.isValidRRDNodeDir(nodeDir);
    }

    public boolean isQueryableInterface(int nodeId, String ifLabel) {
        if (ifLabel == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

	File intfDir = new File(getRrdDirectory(),
				String.valueOf(nodeId) + File.separator
				+ ifLabel);
	return RrdFileConstants.isValidRRDInterfaceDir(intfDir);
    }

    public boolean isQueryableInterface(String nodeId, String ifLabel) {
        if (nodeId == null || ifLabel == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        return isQueryableInterface(Integer.parseInt(nodeId), ifLabel);
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
            if (a.isResourceTypeOnNode(nodeId)) {
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
    
    public List<GraphResource> getResourcesForNodeResourceType(int nodeId, String resourceTypeName) {
        GraphResourceType resourceType = getResourceTypeByName(resourceTypeName);

        if (!resourceType.isResourceTypeOnNode(nodeId)) {
            throw new ObjectRetrievalFailureException(GraphResourceType.class, resourceTypeName, "Resource type is not on node " + nodeId, null);
        }

        return resourceType.getResourcesForNode(nodeId);
    }
    
    public GraphResource getResourceForNodeResourceResourceType(int nodeId, String resourceName, String resourceTypeName) {
        List<GraphResource> resources = getResourcesForNodeResourceType(nodeId, resourceTypeName);
        
        GraphResource resource = null;
        for (GraphResource a : resources) {
            if (resourceName.equals(a.getName())) {
                resource = a;
            }
        }
        
        if (resource == null) {
            throw new ObjectRetrievalFailureException(GraphResourceType.class, resourceName, "Resource of resource type '" + resourceTypeName + "' is not on node " + nodeId, null);
        }
        
        return resource;
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

    public PrefabGraph[] getQueriesByResourceTypeAttributes(String resourceType,
            Set<GraphAttribute> attributes) {

        List<PrefabGraph> returnList = new LinkedList<PrefabGraph>();

        PrefabGraph[] queries = getQueries();

        Set<String> availDataSourceList = new HashSet<String>(attributes.size());
        for (GraphAttribute attribute : attributes) {
            availDataSourceList.add(attribute.getName());
        }

        for (PrefabGraph query : queries) {
            if (!resourceType.equals(query.getType())) {
                continue;
            }
            
            List requiredList = Arrays.asList(query.getColumns());

            if (availDataSourceList.containsAll(requiredList)) {
                if (query.getExternalValues().length == 0) {
                    returnList.add(query);
                }
            }
        }

        PrefabGraph[] availQueries = (PrefabGraph[])
        returnList.toArray(new PrefabGraph[returnList.size()]);

        return availQueries;
    }

    public String getRelativePathForAttribute(String resourceType, int nodeId, String resource, String attribute) {
        GraphResourceType rt = getResourceTypeByName(resourceType);
        return rt.getRelativePathForAttribute(nodeId, resource, attribute);
    }
}
