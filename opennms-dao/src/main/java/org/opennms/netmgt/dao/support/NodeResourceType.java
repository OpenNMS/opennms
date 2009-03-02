//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2007 Apr 10: Store the entity. - dj@opennms.org
// 2007 Apr 05: Remove getRelativePathForAttribute. - dj@opennms.org
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
package org.opennms.netmgt.dao.support;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.opennms.core.utils.LazyList;
import org.opennms.netmgt.dao.ResourceDao;
import org.opennms.netmgt.model.OnmsAttribute;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.OnmsResourceType;

public class NodeResourceType implements OnmsResourceType {
    protected static final Set<OnmsAttribute> s_emptyAttributeSet = Collections.unmodifiableSet(new HashSet<OnmsAttribute>());
    protected ResourceDao m_resourceDao;

    public NodeResourceType(ResourceDao resourceDao) {
        m_resourceDao = resourceDao;
    }
    
    public String getLabel() {
        return "Node";
    }

    public String getName() {
        return "node";
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
