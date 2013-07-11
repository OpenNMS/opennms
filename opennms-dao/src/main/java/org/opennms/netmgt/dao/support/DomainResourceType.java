/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.OnmsResourceType;

/**
 * <p>DomainResourceType class.</p>
 */
public class DomainResourceType implements OnmsResourceType {
    private static final Set<OnmsAttribute> s_emptyAttributeSet = Collections.unmodifiableSet(new HashSet<OnmsAttribute>());
    private ResourceDao m_resourceDao;

    /**
     * <p>Constructor for DomainResourceType.</p>
     *
     * @param resourceDao a {@link org.opennms.netmgt.dao.api.ResourceDao} object.
     */
    public DomainResourceType(ResourceDao resourceDao) {
        m_resourceDao = resourceDao;
    }
    
    /**
     * <p>getLabel</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getLabel() {
        return "Domain";
    }

    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getName() {
        return "domain";
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
        // Need a search for hosts in a domain. The present nodeList capability won't support it.
        // Just return null for now
        return null;
    }
    
    /**
     * <p>createChildResource</p>
     *
     * @param domain a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.OnmsResource} object.
     */
    public OnmsResource createChildResource(String domain) {
        DomainChildResourceLoader loader = new DomainChildResourceLoader(domain);
        OnmsResource resource = new OnmsResource(domain, domain, this, s_emptyAttributeSet, new LazyList<OnmsResource>(loader));
        loader.setParent(resource);
        
        return resource;
    }
    
    public class DomainChildResourceLoader implements LazyList.Loader<OnmsResource> {
        private String m_domain;
        private OnmsResource m_parent;
        
        public DomainChildResourceLoader(String domain) {
            m_domain = domain;
        }
        
        public void setParent(OnmsResource parent) {
            m_parent = parent;
        }

        @Override
        public List<OnmsResource> load() {
            List<OnmsResource> children = new LinkedList<OnmsResource>();

            for (OnmsResourceType resourceType : getResourceTypesForDomain(m_domain)) {
                for (OnmsResource resource : resourceType.getResourcesForDomain(m_domain)) {
                    resource.setParent(m_parent);
                    children.add(resource);
                }
            }

            return children;
        }
        
        private Collection<OnmsResourceType> getResourceTypesForDomain(String domain) {
            Collection<OnmsResourceType> resourceTypes = new LinkedList<OnmsResourceType>();
            for (OnmsResourceType resourceType : m_resourceDao.getResourceTypes()) {
                if (resourceType.isResourceTypeOnDomain(domain)) {
                    resourceTypes.add(resourceType);
                }
            }
            return resourceTypes;
        }
    }
}
