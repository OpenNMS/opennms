package org.opennms.netmgt.dao.support;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Category;
import org.opennms.core.utils.LazySet;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.dao.ResourceDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.model.OnmsAttribute;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.OnmsResourceType;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.RrdGraphAttribute;
import org.springframework.orm.ObjectRetrievalFailureException;

public class ResponseTimeResourceType implements OnmsResourceType {
    private ResourceDao m_resourceDao;
    private NodeDao m_nodeDao;
    
    public ResponseTimeResourceType(ResourceDao resourceDao, NodeDao nodeDao) {
        m_resourceDao = resourceDao;
        m_nodeDao = nodeDao;
    }
    
    public String getLabel() {
        return "Response Time";
    }

    public String getName() {
        return "responseTime";
    }

    public String getRelativePathForAttribute(String resourceParent,
            String resource, String attribute) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(DefaultResourceDao.RESPONSE_DIRECTORY);
        buffer.append(File.separator);
        buffer.append(resource);
        buffer.append(File.separator);
        buffer.append(attribute);
        buffer.append(RrdFileConstants.getRrdSuffix());
        return buffer.toString();
    }

    @SuppressWarnings("unchecked")
    public List<OnmsResource> getResourcesForDomain(String domain) {
        return Collections.EMPTY_LIST;
    }
    
    public List<OnmsResource> getResourcesForNode(int nodeId) {
        LinkedList<OnmsResource> resources = new LinkedList<OnmsResource>();
        
        OnmsNode node = m_nodeDao.get(nodeId);
        if (node == null) {
            throw new ObjectRetrievalFailureException(OnmsNode.class, nodeId, "Could not find node for node Id " + nodeId, null);
        }
        
        for (OnmsIpInterface i : node.getIpInterfaces()) {
            String ipAddr = i.getIpAddress();

            File iface = getInterfaceDirectory(ipAddr, false);
            
            if (iface.isDirectory()) {
                resources.add(createResource(ipAddr));
            }
        }

        return resources;
    }

    private File getInterfaceDirectory(String ipAddr, boolean verify) {
        File response = new File(m_resourceDao.getRrdDirectory(verify), DefaultResourceDao.RESPONSE_DIRECTORY);
        
        File intfDir = new File(response, ipAddr);
        if (verify && !intfDir.isDirectory()) {
            throw new ObjectRetrievalFailureException(File.class, "No interface directory exists for " + ipAddr + ": " + intfDir);
        }

        return intfDir;
    }
    
    private OnmsResource createResource(String intf) {
        String label = intf;
        String resource = intf;

        Set<OnmsAttribute> set =
            new LazySet<OnmsAttribute>(new AttributeLoader(intf));
        return new OnmsResource(resource, label, this, set);
    }


    public boolean isResourceTypeOnDomain(String domain) {
        return false;
    }

    public boolean isResourceTypeOnNode(int nodeId) {
        return getResourcesForNode(nodeId).size() > 0;
    }

    private Category log() {
        return ThreadCategory.getInstance();
    }

    public class AttributeLoader implements LazySet.Loader<OnmsAttribute> {
        private String m_intf;

        public AttributeLoader(String intf) {
            m_intf = intf;
        }

        public Set<OnmsAttribute> load() {
            File directory = getInterfaceDirectory(m_intf, true);
            log().debug("lazy-loading attributes for resource \"" + m_intf
                        + "\" from directory " + directory);
            List<String> dataSources =
                ResourceTypeUtils.getDataSourcesInDirectory(directory);

            Set<OnmsAttribute> attributes =
                new HashSet<OnmsAttribute>(dataSources.size());
            
            for (String dataSource : dataSources) {
                log().debug("Found data source \"" + dataSource + "\" on "
                            + m_intf);
                            
                attributes.add(new RrdGraphAttribute(dataSource));
            }
            
            return attributes;
        }
    }

    public String getLinkForResource(OnmsResource resource) {
        return "element/interface.jsp?node=" + resource.getParent().getName() + "&intf=" + resource.getName();
    }
}
