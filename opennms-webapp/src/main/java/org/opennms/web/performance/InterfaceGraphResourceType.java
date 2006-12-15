package org.opennms.web.performance;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.opennms.core.utils.LazySet;
import org.opennms.netmgt.utils.RrdFileConstants;
import org.opennms.web.graph.GraphModelFoo;
import org.opennms.web.graph.PrefabGraph;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.orm.ObjectRetrievalFailureException;

public class InterfaceGraphResourceType implements GraphResourceType {

    private PerformanceModel m_performanceModel;

    public InterfaceGraphResourceType(PerformanceModel performanceModel) {
        m_performanceModel = performanceModel;
    }

    public String getName() {
        return "interface";
    }
    
    public String getLabel() {
        return "Interface";
    }
    
    public boolean isResourceTypeOnNode(int nodeId) {
        /*
        try {
            return m_performanceModel.getQueryableInterfacesForNode(nodeId).size() > 0;
        } catch (DataAccessException e) {
            return false;
        }
        */
        return isResourceTypeOnParentResource(Integer.toString(nodeId));
    }
    
    private boolean isResourceTypeOnParentResource(String parentResource) {
        File parent = getParentResourceDirectory(parentResource, false);
        if (!parent.isDirectory()) {
            return false;
        }
        
        File[] intfFiles = parent.listFiles(RrdFileConstants.INTERFACE_DIRECTORY_FILTER); 
        return intfFiles.length > 0;
    }
    
    private File getParentResourceDirectory(String parentResource, boolean verify) {
        File snmp = new File(m_performanceModel.getRrdDirectory(verify), PerformanceModel.SNMP_DIRECTORY);
        
        File parent = new File(snmp, parentResource);
        if (verify && !parent.isDirectory()) {
            throw new ObjectRetrievalFailureException(File.class, "No parent resource directory exists for " + parentResource + ": " + parent);
        }
        
        return parent;
    }
        
    private File getResourceDirectory(String parentResource, String intf, boolean verify) {
        File parent = getParentResourceDirectory(parentResource, verify);
        
        File intfDir = new File(parent, intf);
        if (verify && !parent.isDirectory()) {
            throw new ObjectRetrievalFailureException(File.class, "No interface directory exists for " + intf + ": " + intfDir);
        }
        
        return intfDir;
    }
    
    public List<GraphResource> getResourcesForNode(int nodeId) {
        ArrayList<DefaultGraphResource> resources =
            new ArrayList<DefaultGraphResource>();

        /*
        List<String> ifaces = 
            m_performanceModel.getQueryableInterfacesForNode(nodeId);

        for (String iface : ifaces) {
            DefaultGraphResource resource =
                getResourceByNodeAndInterface(nodeId, iface);
            resources.add(resource);
        }
            */
        File parent = getParentResourceDirectory(Integer.toString(nodeId), true);
        File[] intfDirs = parent.listFiles(RrdFileConstants.INTERFACE_DIRECTORY_FILTER);

        for (File intfDir : intfDirs) {
            DefaultGraphResource resource =
                getResourceByNodeAndInterface(nodeId, intfDir.getName());
            resources.add(resource);
        }
        
        return DefaultGraphResource.sortIntoGraphResourceList(resources);
    }
    
    public DefaultGraphResource getResourceByNodeAndInterface(int nodeId,
            String intf) throws DataAccessException {
        String label;
        try {
            label = m_performanceModel.getHumanReadableNameForIfLabel(nodeId,
                                                                      intf);
        } catch (SQLException e) {
            SQLErrorCodeSQLExceptionTranslator translator =
                new SQLErrorCodeSQLExceptionTranslator();
            throw translator.translate("Getting human readable name for "
                                       + "interface label", null, e);
        }

        Set<GraphAttribute> set =
            new LazySet<GraphAttribute>(new AttributeLoader(getResourceDirectory(Integer.toString(nodeId), intf, true)));
        return new DefaultGraphResource(intf, label, set);
    }
    
    /*
    private File getResourceDirectory(String node, String intf, boolean verify) {
        File snmp = new File(m_performanceModel.getRrdDirectory(verify), PerformanceModel.SNMP_DIRECTORY);
        
        File nodeDir = new File(snmp, node);
        if (verify && !nodeDir.isDirectory()) {
            throw new ObjectRetrievalFailureException(File.class, "No node directory exists for " + node + ": " + nodeDir);
        }
        
        File intfDir = new File(node, intf);
        if (verify && !intfDir.isDirectory()) {
            throw new ObjectRetrievalFailureException(File.class, "No interface directory exists for interface " + intf + " on " + node + ": " + intfDir);
        }
        
        return intfDir;
    }
    */
    
    public class AttributeLoader implements LazySet.Loader<GraphAttribute> {
        private File m_intfDir;

        public AttributeLoader(File intfDir) {
            m_intfDir = intfDir;
        }

        public Set<GraphAttribute> load() {
            List<String> dataSources =
                m_performanceModel.getDataSourcesInDirectory(m_intfDir);
            Set<GraphAttribute> attributes =
                new HashSet<GraphAttribute>(dataSources.size());
            
            for (String dataSource : dataSources) {
                attributes.add(new RrdGraphAttribute(dataSource));
            }
            
            return attributes;
        }
        
    }

    public String getRelativePathForAttribute(String resourceParent, String resource, String attribute) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(PerformanceModel.SNMP_DIRECTORY);
        buffer.append(File.separator);
        buffer.append(resourceParent);
        buffer.append(File.separator);
        buffer.append(resource);
        buffer.append(File.separator);
        buffer.append(attribute);
        buffer.append(RrdFileConstants.getRrdSuffix());
        return buffer.toString();
    }

    /**
     * This resource type is never available for domains.
     * Only the interface resource type is available for domains.
     */
    public boolean isResourceTypeOnDomain(String domain) {
        return m_performanceModel.getQueryableInterfacesForDomain(domain).size() > 0;
    }
    
    public List<GraphResource> getResourcesForDomain(String domain) {
        ArrayList<GraphResource> graphResources =
            new ArrayList<GraphResource>();

        List<String> ifaces = 
            m_performanceModel.getQueryableInterfacesForDomain(domain);
        for (String iface : ifaces) {
            graphResources.add(getResourceByDomainAndInterface(domain, iface));
        }

        return graphResources;
    }

    private GraphResource getResourceByDomainAndInterface(String domain, String intf) {
        Set<GraphAttribute> set =
            new LazySet<GraphAttribute>(new AttributeLoader(getResourceDirectory(domain, intf, true)));
        return new DefaultGraphResource(intf, intf, set);
    }

    public List<PrefabGraph> getAvailablePrefabGraphs(Set<GraphAttribute> attributes) {
        PrefabGraph[] graphs =
            m_performanceModel.getQueriesByResourceTypeAttributes(getName(), attributes);
        List<PrefabGraph> graphList = Arrays.asList(graphs);
        
        // Doh... this doesn't work.  See bug #1703.
        /*
         * Remove any items that have external values, particularly due to
         * ifSpeed.
         */
        /*
        ListIterator<PrefabGraph> iterator = graphList.listIterator();
        while (iterator.hasNext()) {
            PrefabGraph graph = iterator.next();
            
            String[] external = graph.getExternalValues();
            if (external != null && external.length > 0) {
                iterator.remove();
                continue;
            }
        }
        */
        
        return graphList;
    }

    public GraphModelFoo getModel() {
        return m_performanceModel;
    }
    
    public PrefabGraph getPrefabGraph(String name) {
        return m_performanceModel.getQuery(name);
    }

    public String getGraphType() {
        return "performance";
    }
}
