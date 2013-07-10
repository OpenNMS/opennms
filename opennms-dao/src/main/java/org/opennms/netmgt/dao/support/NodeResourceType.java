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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.opennms.core.utils.LazyList;
import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.model.OnmsAttribute;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.OnmsResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeResourceType implements OnmsResourceType {
    
    private static final Logger LOG = LoggerFactory.getLogger(NodeResourceType.class);
    
    /** Constant <code>s_emptyAttributeSet</code> */
    protected static final Set<OnmsAttribute> s_emptyAttributeSet = Collections.unmodifiableSet(new HashSet<OnmsAttribute>());
    protected ResourceDao m_resourceDao;

    /**
     * <p>Constructor for NodeResourceType.</p>
     *
     * @param resourceDao a {@link org.opennms.netmgt.dao.api.ResourceDao} object.
     */
    public NodeResourceType(ResourceDao resourceDao) {
        m_resourceDao = resourceDao;
    }
    
    /**
     * <p>getLabel</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getLabel() {
        return "Node";
    }

    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getName() {
        return "node";
    }

    /** {@inheritDoc} */
    @Override
    public List<OnmsResource> getResourcesForDomain(String domain) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public List<OnmsResource> getResourcesForNode(int nodeId) {
        return null;
    }
    
    /** {@inheritDoc} */
    @Override
       public List<OnmsResource> getResourcesForNodeSource(String nodeSource, int nodeId) {
           return null;
       }

    /** {@inheritDoc} */
    @Override
    public boolean isResourceTypeOnDomain(String domain) {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isResourceTypeOnNode(int nodeId) {
        return false;
    }
    
    /** {@inheritDoc} */
    @Override
       public boolean isResourceTypeOnNodeSource(String nodeSource, int nodeId) {
           return false;
       }

    /** {@inheritDoc} */
    @Override
    public String getLinkForResource(OnmsResource resource) {
        return "element/node.jsp?node=" + resource.getName();
    }
    
    /**
     * <p>createChildResource</p>
     *
     * @param node a {@link org.opennms.netmgt.model.OnmsNode} object.
     * @return a {@link org.opennms.netmgt.model.OnmsResource} object.
     */
    public OnmsResource createChildResource(OnmsNode node) {
        NodeChildResourceLoader loader = new NodeChildResourceLoader(node.getId());
        OnmsResource r = new OnmsResource(node.getId().toString(), node.getLabel(), this, s_emptyAttributeSet, new LazyList<OnmsResource>(loader));
        r.setEntity(node);
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

        @Override
        public List<OnmsResource> load() {
            List<OnmsResource> children = new LinkedList<OnmsResource>();

            for (OnmsResourceType resourceType : getResourceTypesForNode(m_nodeId)) {
                for (OnmsResource resource : resourceType.getResourcesForNode(m_nodeId)) {
                    resource.setParent(m_parent);
                    children.add(resource);
                    LOG.debug("load: adding resource {}", resource.toString());
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
