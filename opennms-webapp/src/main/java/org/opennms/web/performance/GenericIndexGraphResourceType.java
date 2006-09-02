package org.opennms.web.performance;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.opennms.netmgt.collectd.StorageStrategy;
import org.opennms.netmgt.dao.jdbc.LazySet;
import org.opennms.netmgt.utils.RrdFileConstants;

public class GenericIndexGraphResourceType implements GraphResourceType {

    private String m_name;
    private String m_label;
    private PerformanceModel m_performanceModel;
    private StorageStrategy m_storageStrategy;

    public GenericIndexGraphResourceType(PerformanceModel performanceModel, String name, String label, StorageStrategy storageStrategy) {
        m_performanceModel = performanceModel;
        m_name = name;
        m_label = label;
        m_storageStrategy = storageStrategy;
    }
    
    public String getName() {
        return m_name;
    }
    
    public String getLabel() {
        return m_label;
    }
    
    public StorageStrategy getStorageStrategy() {
        return m_storageStrategy;
    }
    
    public boolean isResourceTypeOnNode(int nodeId) {
        /*
         *  XXX this should be based on the code in
         *  PerformanceModel.getQueryableNodes().  For now, we just return true.
         */
      return getResourceTypeDirectory(nodeId).isDirectory();
//    File resourceDirectory = new File(m_performanceModel.getNodeDirectory(nodeId, false), getName());
//        return resourceDirectory.isDirectory();
//        return false;
    }
    
    private File getResourceTypeDirectory(int nodeId) {
        return getResourceTypeDirectory(nodeId, false);
    }
    
    private File getResourceTypeDirectory(int nodeId, boolean verify) {
        return new File(m_performanceModel.getNodeDirectory(nodeId, verify),
                        getName());

    }
    
    private File getResourceDirectory(int nodeId, String index) {
        return getResourceDirectory(nodeId, index, false);
    }
    
    private File getResourceDirectory(int nodeId, String index, boolean verify) {
        return new File(getResourceTypeDirectory(nodeId, verify), index);

    }
    
    
    public List<GraphResource> getResourcesForNode(int nodeId) {
        ArrayList<GraphResource> graphResources =
            new ArrayList<GraphResource>();

        List<String> indexes = getQueryableIndexesForNode(nodeId);
        for (String index : indexes) {
            graphResources.add(getResourceByNodeAndIndex(nodeId, index));
        }

        return graphResources;
    }
    
    public List<String> getQueryableIndexesForNode(int nodeId) {
        File nodeDir = getResourceTypeDirectory(nodeId, true);
        
        List<String> indexes = new LinkedList<String>();
        
        File[] indexDirs =
            nodeDir.listFiles(RrdFileConstants.INTERFACE_DIRECTORY_FILTER);

        if (indexDirs == null) {
            return indexes;
        }
        
        for (File indexDir : indexDirs) {
            indexes.add(indexDir.getName());
        }
        
        return indexes;
    }

    
    public GraphResource getResourceByNodeAndIndex(int nodeId,
            String index) {
        
        String label = index;
//            label = m_performanceModel.getHumanReadableNameForIfLabel(nodeId, intf);


        Set<GraphAttribute> set =
            new LazySet(new AttributeLoader(nodeId, index));
        return new DefaultGraphResource(index, label, set);
 
        
//        return new DefaultGraphResource(index, label, attributes);
    }


public class AttributeLoader implements LazySet.Loader {
    
    private int m_nodeId;
    private String m_index;

    public AttributeLoader(int nodeId, String index) {
        m_nodeId = nodeId;
        m_index = index;
    }

    public Set<GraphAttribute> load() {

        File resourceDirectory = getResourceDirectory(m_nodeId, m_index); 
        List<String> dataSources =
            m_performanceModel.getDataSourcesInDirectory(resourceDirectory);

        Set<GraphAttribute> attributes =
            new HashSet<GraphAttribute>(dataSources.size());
        
        for (String dataSource : dataSources) {
            attributes.add(new RrdGraphAttribute(dataSource));
        }

        return attributes;
    }

}


public String getRelativePathForAttribute(String resourceParent, String resource, String attribute) {
    return m_storageStrategy.getRelativePathForAttribute(resourceParent, resource, attribute);
}
    
    /**
     * This resource type is never available for domains.
     * Only the interface resource type is available for domains.
     */
    public boolean isResourceTypeOnDomain(String domain) {
        return false;
    }
    

    public List<GraphResource> getResourcesForDomain(String domain) {
        return Collections.EMPTY_LIST;
    }

}
