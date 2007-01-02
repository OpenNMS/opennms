package org.opennms.netmgt.dao.support;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.opennms.netmgt.dao.ResourceDao;
import org.opennms.netmgt.model.OnmsAttribute;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.OnmsResourceType;
import org.opennms.netmgt.model.RrdGraphAttribute;
import org.springframework.orm.ObjectRetrievalFailureException;

public class NodeSnmpResourceType implements OnmsResourceType {

    private ResourceDao m_resourceDao;

    public NodeSnmpResourceType(ResourceDao resourceDao) {
        m_resourceDao = resourceDao;
    }

    public String getName() {
        return "nodeSnmp";
    }
    
    public String getLabel() {
        return "SNMP Node Data";
    }
    
    public boolean isResourceTypeOnNode(int nodeId) {
        return getResourceDirectory(nodeId, false).isDirectory();
    }
    
    public File getResourceDirectory(int nodeId, boolean verify) {
        File snmp = new File(m_resourceDao.getRrdDirectory(verify), DefaultResourceDao.SNMP_DIRECTORY);
        
        File node = new File(snmp, Integer.toString(nodeId));
        if (verify && !node.isDirectory()) {
            throw new ObjectRetrievalFailureException(File.class, "No node directory exists for node " + nodeId + ": " + node);
        }
        
        return node;
    }
    
    public List<OnmsResource> getResourcesForNode(int nodeId) {
        ArrayList<OnmsResource> resources =
            new ArrayList<OnmsResource>();

        List<String> dataSources =
            ResourceTypeUtils.getDataSourcesInDirectory(getResourceDirectory(nodeId, true));
        Set<OnmsAttribute> attributes =
            new HashSet<OnmsAttribute>(dataSources.size());
        
        for (String dataSource : dataSources) {
            attributes.add(new RrdGraphAttribute(dataSource));
        }
        
        OnmsResource resource =
            new OnmsResource("", "Node-level Performance Data",
                                     this, attributes);
        resources.add(resource);
        return resources;
    }

    public String getRelativePathForAttribute(String resourceParent, String resource, String attribute) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(DefaultResourceDao.SNMP_DIRECTORY);
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
    public List<OnmsResource> getResourcesForDomain(String domain) {
        return Collections.EMPTY_LIST;
    }

    public String getLinkForResource(OnmsResource resource) {
        return null;
    }
}
