package org.opennms.netmgt.dao.support;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.opennms.core.utils.LazyList;
import org.opennms.netmgt.dao.ResourceDao;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.OnmsAttribute;
import org.opennms.netmgt.model.OnmsResourceType;
import org.opennms.netmgt.model.OnmsNode;

public class NodeResourceType implements OnmsResourceType {
    private static final Set<OnmsAttribute> s_emptyAttributeSet = Collections.unmodifiableSet(new HashSet<OnmsAttribute>());
    private ResourceDao m_resourceDao;

    public NodeResourceType(ResourceDao resourceDao) {
        m_resourceDao = resourceDao;
    }
    
    public String getLabel() {
        return "Node";
    }

    public String getName() {
        return "node";
    }

    public String getRelativePathForAttribute(String resourceParent, String resource, String attribute) {
        throw new UnsupportedOperationException("method not implemented");
    }

    public List<OnmsResource> getResourcesForDomain(String domain) {
        throw new UnsupportedOperationException("method not implemented");
    }

    public List<OnmsResource> getResourcesForNode(int nodeId) {
        throw new UnsupportedOperationException("method not implemented");
    }

    public boolean isResourceTypeOnDomain(String domain) {
        return false;
    }

    public boolean isResourceTypeOnNode(int nodeId) {
        return false;
    }

    public String getLinkForResource(OnmsResource resource) {
        return "element/node.jsp?node=" + resource.getName();
    }
    
    public OnmsResource createChildResource(OnmsNode node) {
        NodeChildResourceLoader loader = new NodeChildResourceLoader(node.getId());
        OnmsResource r = new OnmsResource(node.getId().toString(), node.getLabel(), this, s_emptyAttributeSet, new LazyList<OnmsResource>(loader));
        loader.setParent(r);

        return r;
    }
    
    
    private class NodeChildResourceLoader implements LazyList.Loader<OnmsResource> {
        private int m_nodeId;
        private OnmsResource m_parent;
        
        public NodeChildResourceLoader(int nodeId) {
            m_nodeId = nodeId;
        }
        
        public void setParent(OnmsResource parent) {
            m_parent = parent;
        }

        public List<OnmsResource> load() {
            List<OnmsResource> children = new LinkedList<OnmsResource>();

            for (OnmsResourceType resourceType : getResourceTypesForNode(m_nodeId)) {
                for (OnmsResource resource : resourceType.getResourcesForNode(m_nodeId)) {
                    resource.setParent(m_parent);
                    children.add(resource);
                }
            }

            return children;
        }
        
        private Collection<OnmsResourceType> getResourceTypesForNode(int nodeId) {
            Collection<OnmsResourceType> resourceTypes = new LinkedList<OnmsResourceType>();
            for (OnmsResourceType resourceType : m_resourceDao.getResourceTypes()) {
                if (resourceType.isResourceTypeOnNode(nodeId)) {
                    resourceTypes.add(resourceType);
                }
            }
            return resourceTypes;
        }
        
        
    }
}