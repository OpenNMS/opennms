/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2013 The OpenNMS Group, Inc.
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
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.model.OnmsAttribute;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.OnmsResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>NodeSourceResourceType class.</p>
 */
public class NodeSourceResourceType implements OnmsResourceType {
    
    private static final Logger LOG = LoggerFactory.getLogger(NodeSourceResourceType.class);
    
    private static final Set<OnmsAttribute> s_emptyAttributeSet = Collections.unmodifiableSet(new HashSet<OnmsAttribute>());
    private ResourceDao m_resourceDao;
    private NodeDao m_nodeDao;

    /**
     * <p>Constructor for NodeSourceResourceType.</p>
     *
     * @param resourceDao a {@link org.opennms.netmgt.dao.api.ResourceDao} object.
     * @param nodeDao 
     */
    public NodeSourceResourceType(ResourceDao resourceDao, NodeDao nodeDao) {
        m_resourceDao = resourceDao;
        m_nodeDao = nodeDao;
    }

    /**
     * <p>getLabel</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getLabel() {
        return "Foreign Source";
    }

    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getName() {
        return "nodeSource";
    }

    /** {@inheritDoc} */
    @Override
    public List<OnmsResource> getResourcesForNodeSource(String nodeSource, int nodeId) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public List<OnmsResource> getResourcesForNode(int nodeId) {
        return null;
    }
    
    /** {@inheritDoc} */
    @Override
    public List<OnmsResource> getResourcesForDomain(String domain) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isResourceTypeOnNodeSource(String nodeSource, int nodeId) {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isResourceTypeOnNode(int nodeId) {
        return false;
    }
    
    /** {@inheritDoc} */
    @Override
        public boolean isResourceTypeOnDomain(String domain) {
                return false;
        }


    /** {@inheritDoc} */
    @Override
    public String getLinkForResource(OnmsResource resource) {
        String ident[] = resource.getName().split(":");
        int nodeId = m_nodeDao.findByForeignId(ident[0], ident[1]).getId();
        return "element/node.jsp?node=" + nodeId;
    }

    /**
     * <p>createChildResource</p>
     *
     * @param nodeSource a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.OnmsResource} object.
     */
    public OnmsResource createChildResource(String nodeSource) {
        String[] ident = nodeSource.split(":");
        String label = ident[0] + ":" + m_nodeDao.findByForeignId(ident[0], ident[1]).getLabel();
        NodeSourceChildResourceLoader loader = new NodeSourceChildResourceLoader(nodeSource);
        OnmsResource resource = new OnmsResource(nodeSource, label, this, s_emptyAttributeSet, new LazyList<OnmsResource>(loader));
        loader.setParent(resource);
        
        return resource;
    }

    public class NodeSourceChildResourceLoader implements LazyList.Loader<OnmsResource> {
        private String m_nodeSource;
        private OnmsResource m_parent;
        
        private int nodeSourceToNodeId() {
                String[] ident = m_nodeSource.split(":");
            return m_nodeDao.findByForeignId(ident[0], ident[1]).getId();
        }
        
        public NodeSourceChildResourceLoader(String nodeSource) {
            m_nodeSource = nodeSource;
        }
        
        public void setParent(OnmsResource parent) {
            m_parent = parent;
        }

        @Override
        public List<OnmsResource> load() {
            List<OnmsResource> children = new LinkedList<OnmsResource>();

            for (OnmsResourceType resourceType : getResourceTypesForNodeSource(m_nodeSource)) {
                for (OnmsResource resource : resourceType.getResourcesForNodeSource(m_nodeSource, nodeSourceToNodeId())) {
                    resource.setParent(m_parent);
                    children.add(resource);
                    LOG.debug("load: adding resource {}", resource.toString());
                }
            }

            return children;
        }
        
        private Collection<OnmsResourceType> getResourceTypesForNodeSource(String nodeSource) {
            Collection<OnmsResourceType> resourceTypes = new LinkedList<OnmsResourceType>();
            for (OnmsResourceType resourceType : m_resourceDao.getResourceTypes()) {
                if (resourceType.isResourceTypeOnNodeSource(nodeSource, nodeSourceToNodeId())) {
                    resourceTypes.add(resourceType);
                    LOG.debug("getResourceTypesForNodeSource: adding type {}", resourceType.getName());
                }
            }
            return resourceTypes;
        }
    }


}
