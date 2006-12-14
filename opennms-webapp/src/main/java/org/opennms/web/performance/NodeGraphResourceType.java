package org.opennms.web.performance;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.opennms.netmgt.utils.RrdFileConstants;
import org.opennms.web.graph.GraphModel;
import org.opennms.web.graph.PrefabGraph;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.ObjectRetrievalFailureException;

public class NodeGraphResourceType implements GraphResourceType {

    private PerformanceModel m_performanceModel;

    public NodeGraphResourceType(PerformanceModel performanceModel) {
        m_performanceModel = performanceModel;
    }

    public String getName() {
        return "node";
    }
    
    public String getLabel() {
        return "Node";
    }
    
    public boolean isResourceTypeOnNode(int nodeId) {
        /*
         *  XXX this should be based on the code in
         *  PerformanceModel.getQueryableNodes().  For now, we just return true
         *  if the node directory exists.
         */
        //return m_performanceModel.getNodeDirectory(nodeId, false).isDirectory();
        return getResourceDirectory(nodeId, false).isDirectory();
    }
    
    public File getResourceDirectory(int nodeId, boolean verify) {
        File snmp = new File(m_performanceModel.getRrdDirectory(verify), PerformanceModel.SNMP_DIRECTORY);
        
        File node = new File(snmp, Integer.toString(nodeId));
        if (verify && !node.isDirectory()) {
            throw new ObjectRetrievalFailureException(File.class, "No node directory exists for node " + nodeId + ": " + node);
        }
        
        return node;
    }
    
    public List<GraphResource> getResourcesForNode(int nodeId) {
        ArrayList<GraphResource> graphResources =
            new ArrayList<GraphResource>();

        List<String> dataSources =
            m_performanceModel.getDataSourcesInDirectory(getResourceDirectory(nodeId, true));
        Set<GraphAttribute> attributes =
            new HashSet<GraphAttribute>(dataSources.size());
        
        for (String dataSource : dataSources) {
            attributes.add(new RrdGraphAttribute(dataSource));
        }
        
        GraphResource resource =
            new DefaultGraphResource("", "Node-level Performance Data",
                                     attributes);
        graphResources.add(resource);
        return graphResources;
    }

    public String getRelativePathForAttribute(String resourceParent, String resource, String attribute) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(PerformanceModel.SNMP_DIRECTORY);
        buffer.append(File.separator);
        buffer.append(resourceParent);
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
        return false;
    }

    @SuppressWarnings("unchecked")
    public List<GraphResource> getResourcesForDomain(String domain) {
        return Collections.EMPTY_LIST;
    }

    public List<PrefabGraph> getAvailablePrefabGraphs(Set<GraphAttribute> attributes) {
        PrefabGraph[] graphs =
            m_performanceModel.getQueriesByResourceTypeAttributes(getName(), attributes);
        return Arrays.asList(graphs);
    }
    
    public GraphModel getModel() {
        return m_performanceModel;
    }
    
    public PrefabGraph getPrefabGraph(String name) {
        return m_performanceModel.getQuery(name);
    }

    public String getGraphType() {
        return "performance";
    }
}
