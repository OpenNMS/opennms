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
// 2007 Apr 05: Remove getRelativePathForAttribute and move attribute loading to
//              ResourceTypeUtils.getAttributesAtRelativePath. - dj@opennms.org
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

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.opennms.core.utils.LazySet;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.ResourceDao;
import org.opennms.netmgt.model.OnmsAttribute;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.OnmsResourceType;
import org.springframework.orm.ObjectRetrievalFailureException;

/**
 * <p>ResponseTimeResourceType class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class ResponseTimeResourceType implements OnmsResourceType {
    private ResourceDao m_resourceDao;
    private NodeDao m_nodeDao;
    
    /**
     * <p>Constructor for ResponseTimeResourceType.</p>
     *
     * @param resourceDao a {@link org.opennms.netmgt.dao.ResourceDao} object.
     * @param nodeDao a {@link org.opennms.netmgt.dao.NodeDao} object.
     */
    public ResponseTimeResourceType(ResourceDao resourceDao, NodeDao nodeDao) {
        m_resourceDao = resourceDao;
        m_nodeDao = nodeDao;
    }
    
    /**
     * <p>getLabel</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getLabel() {
        return "Response Time";
    }

    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getName() {
        return "responseTime";
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    public List<OnmsResource> getResourcesForDomain(String domain) {
        return Collections.EMPTY_LIST;
    }
    
    /** {@inheritDoc} */
    public List<OnmsResource> getResourcesForNode(int nodeId) {
        LinkedList<OnmsResource> resources = new LinkedList<OnmsResource>();
        
        OnmsNode node = m_nodeDao.get(nodeId);
        if (node == null) {
            throw new ObjectRetrievalFailureException(OnmsNode.class, nodeId, "Could not find node for node Id " + nodeId, null);
        }
        
        for (OnmsIpInterface i : node.getIpInterfaces()) {
            String ipAddr = i.getIpAddressAsString();

            File iface = getInterfaceDirectory(ipAddr, false);
            
            if (iface.isDirectory()) {
                resources.add(createResource(i));
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
    
    private String getRelativeInterfacePath(String ipAddr) {
        return DefaultResourceDao.RESPONSE_DIRECTORY + File.separator + ipAddr;
    }
    
    private OnmsResource createResource(OnmsIpInterface ipInterface) {
        String intf = ipInterface.getIpAddressAsString();
        String label = intf;
        String resource = intf;

        Set<OnmsAttribute> set = new LazySet<OnmsAttribute>(new AttributeLoader(intf));
        OnmsResource r = new OnmsResource(resource, label, this, set);
        r.setEntity(ipInterface);
        return r;
    }


    /** {@inheritDoc} */
    public boolean isResourceTypeOnDomain(String domain) {
        return false;
    }

    /** {@inheritDoc} */
    public boolean isResourceTypeOnNode(int nodeId) {
        return getResourcesForNode(nodeId).size() > 0;
    }

    private ThreadCategory log() {
        return ThreadCategory.getInstance();
    }

    public class AttributeLoader implements LazySet.Loader<OnmsAttribute> {
        private String m_intf;

        public AttributeLoader(String intf) {
            m_intf = intf;
        }

        public Set<OnmsAttribute> load() {
            log().debug("lazy-loading attributes for response time resource '" + m_intf + "'");
            
            return ResourceTypeUtils.getAttributesAtRelativePath(m_resourceDao.getRrdDirectory(), getRelativeInterfacePath(m_intf));
        }
    }

    /** {@inheritDoc} */
    public String getLinkForResource(OnmsResource resource) {
        return "element/interface.jsp?node=" + resource.getParent().getName() + "&intf=" + resource.getName();
    }
}
