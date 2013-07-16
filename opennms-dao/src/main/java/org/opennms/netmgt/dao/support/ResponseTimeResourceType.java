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

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.LazySet;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.model.OnmsAttribute;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.OnmsResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.ObjectRetrievalFailureException;

/**
 * <p>ResponseTimeResourceType class.</p>
 */
public class ResponseTimeResourceType implements OnmsResourceType {
    
    private static final Logger LOG = LoggerFactory.getLogger(ResponseTimeResourceType.class);
    
    private ResourceDao m_resourceDao;
    private NodeDao m_nodeDao;
    
    /**
     * <p>Constructor for ResponseTimeResourceType.</p>
     *
     * @param resourceDao a {@link org.opennms.netmgt.dao.api.ResourceDao} object.
     * @param nodeDao a {@link org.opennms.netmgt.dao.api.NodeDao} object.
     */
    public ResponseTimeResourceType(final ResourceDao resourceDao, final NodeDao nodeDao) {
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
        return "Response Time";
    }

    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getName() {
        return "responseTime";
    }

    /** {@inheritDoc} */
    @Override
    public List<OnmsResource> getResourcesForDomain(final String domain) {
        List<OnmsResource> empty = Collections.emptyList();
        return empty;
    }
    
    /** {@inheritDoc} */
    @Override
    public List<OnmsResource> getResourcesForNode(final int nodeId) {
    	final LinkedList<OnmsResource> resources = new LinkedList<OnmsResource>();
        
    	final OnmsNode node = m_nodeDao.get(nodeId);
        if (node == null) {
            throw new ObjectRetrievalFailureException(OnmsNode.class, nodeId, "Could not find node for node Id " + nodeId, null);
        }
        
        for (final OnmsIpInterface i : node.getIpInterfaces()) {
            String ipAddr = InetAddressUtils.str(i.getIpAddress());

            final File iface = getInterfaceDirectory(ipAddr, false);
            
            if (iface.isDirectory()) {
                resources.add(createResource(i));
            }
        }

        return resources;
    }

    private File getInterfaceDirectory(final String ipAddr, final boolean verify) {
    	final File response = new File(m_resourceDao.getRrdDirectory(verify), DefaultResourceDao.RESPONSE_DIRECTORY);
        
    	final File intfDir = new File(response, ipAddr);
        if (verify && !intfDir.isDirectory()) {
            throw new ObjectRetrievalFailureException(File.class, "No interface directory exists for " + ipAddr + ": " + intfDir);
        }

        return intfDir;
    }
    
    private String getRelativeInterfacePath(final String ipAddr) {
        return DefaultResourceDao.RESPONSE_DIRECTORY + File.separator + ipAddr;
    }
    
    private OnmsResource createResource(final OnmsIpInterface ipInterface) {
    	final String intf = InetAddressUtils.str(ipInterface.getIpAddress());
    	final String label = intf;
    	final String resource = intf;

    	final Set<OnmsAttribute> set = new LazySet<OnmsAttribute>(new AttributeLoader(intf));
    	final OnmsResource r = new OnmsResource(resource, label, this, set);
        r.setEntity(ipInterface);
        return r;
    }


    /** {@inheritDoc} */
    @Override
    public boolean isResourceTypeOnDomain(final String domain) {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isResourceTypeOnNode(final int nodeId) {
        return getResourcesForNode(nodeId).size() > 0;
    }

    public class AttributeLoader implements LazySet.Loader<OnmsAttribute> {
        private String m_intf;

        public AttributeLoader(final String intf) {
            m_intf = intf;
        }

        @Override
        public Set<OnmsAttribute> load() {
            LOG.debug("lazy-loading attributes for response time resource '{}'", m_intf);
            return ResourceTypeUtils.getAttributesAtRelativePath(m_resourceDao.getRrdDirectory(), getRelativeInterfacePath(m_intf));
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getLinkForResource(final OnmsResource resource) {
        return "element/interface.jsp?node=" + resource.getParent().getName() + "&intf=" + resource.getName();
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean isResourceTypeOnNodeSource(String nodeSource, int nodeId) {
        return getResourcesForNodeSource(nodeSource, nodeId).size() > 0;
    }
    
    /** {@inheritDoc} */
    @Override
    public List<OnmsResource> getResourcesForNodeSource(String nodeSource, int nodeId) {
        return getResourcesForNode(nodeId);
    }
}
