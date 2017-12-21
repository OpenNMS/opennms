/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.dao.support;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.opennms.core.collections.LazySet;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.MonitoringLocationUtils;
import org.opennms.netmgt.dao.api.ResourceStorageDao;
import org.opennms.netmgt.model.OnmsAttribute;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.OnmsResourceType;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.model.ResourceTypeUtils;
import org.springframework.orm.ObjectRetrievalFailureException;

/**
 * Response time resources are stored in paths like:
 *   response/${ipaddr}/ds.rrd
 *
 */
public final class ResponseTimeResourceType implements OnmsResourceType {

    private final ResourceStorageDao m_resourceStorageDao;
    private final IpInterfaceDao m_ipInterfaceDao;

    /**
     * <p>Constructor for ResponseTimeResourceType.</p>
     *
     * @param resourceStorageDao a {@link org.opennms.netmgt.dao.api.ResourceStorageDao} object.
     * @param nodeDao a {@link org.opennms.netmgt.dao.api.NodeDao} object.
     */
    public ResponseTimeResourceType(final ResourceStorageDao resourceStorageDao, final IpInterfaceDao ipInterfaceDao) {
        m_resourceStorageDao = resourceStorageDao;
        m_ipInterfaceDao = ipInterfaceDao;
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
    public String getLinkForResource(final OnmsResource resource) {
        return "element/interface.jsp?node=" + resource.getParent().getName() + "&intf=" + resource.getName();
    }

    @Override
    public boolean isResourceTypeOnParent(OnmsResource parent) {
        return getResourcesForParent(parent, true).size() > 0;
    }

    @Override
    public List<OnmsResource> getResourcesForParent(OnmsResource parent) {
        return getResourcesForParent(parent, false);
    }

    @Override
    public OnmsResource getChildByName(OnmsResource parent, String ipAddress) {
        // Grab the node entity
        final OnmsNode node = ResourceTypeUtils.getNodeFromResource(parent);

        // Determine the location name
        final String locationName = MonitoringLocationUtils.getLocationNameOrNullIfDefault(node);

        // Grab the interface
        final OnmsIpInterface matchingIf = m_ipInterfaceDao.get(node, ipAddress);
        if (matchingIf == null) {
            throw new ObjectRetrievalFailureException(OnmsIpInterface.class, "No interface with ipAddr "
                    + ipAddress + " could be found on node with id " + node.getId());
        }

        // Verify the path
        final ResourcePath path = getInterfacePath(locationName, ipAddress);
        if (!m_resourceStorageDao.exists(path, 0)) {
            throw new ObjectRetrievalFailureException(OnmsResource.class, "No metrics found in parent path '" + parent.getPath() + "'");
        }

        // Create the resource
        final OnmsResource resource = createResource(locationName, matchingIf, ipAddress, path);
        resource.setParent(parent);
        return resource;
    }

    private List<OnmsResource> getResourcesForParent(OnmsResource parent, boolean stopAfterFirst) {
        if (!NodeResourceType.isNode(parent)) {
            return Collections.emptyList();
        }

        // Grab the node entity
        final OnmsNode node = ResourceTypeUtils.getNodeFromResource(parent);

        // Determine the location name
        final String locationName = MonitoringLocationUtils.getLocationNameOrNullIfDefault(node);

        // Verify the existence of the individual interfaces
        final LinkedList<OnmsResource> resources = new LinkedList<>();
        for (final OnmsIpInterface i : node.getIpInterfaces()) {
            String ipAddr = InetAddressUtils.str(i.getIpAddress());

            final ResourcePath path = getInterfacePath(locationName, ipAddr);
            if (m_resourceStorageDao.exists(path, 0)) {
                resources.add(createResource(locationName, i, ipAddr, path));
                if (stopAfterFirst) {
                    break;
                }
            }
        }
        return resources;
    }

    private OnmsResource createResource(final String location, final OnmsIpInterface ipInterface, final String ipAddr, final ResourcePath path) {
    	final LazyResourceAttributeLoader loader = new LazyResourceAttributeLoader(m_resourceStorageDao, path);
    	final Set<OnmsAttribute> set = new LazySet<OnmsAttribute>(loader);
        final OnmsResource resource = new OnmsResource(ipAddr, ipAddr, this, set, path);
    	resource.setEntity(ipInterface);
        return resource;
    }

    private static ResourcePath getInterfacePath(final String location, final String ipAddr) {
        if (location == null) {
            return new ResourcePath(ResourceTypeUtils.RESPONSE_DIRECTORY, ipAddr);
        } else {
            return new ResourcePath(ResourceTypeUtils.RESPONSE_DIRECTORY, ResourcePath.sanitize(location), ipAddr);
        }
    }


}
