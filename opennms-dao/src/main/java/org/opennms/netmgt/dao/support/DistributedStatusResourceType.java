/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2015 The OpenNMS Group, Inc.
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

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.opennms.core.collections.LazySet;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.api.LocationMonitorDao;
import org.opennms.netmgt.dao.api.ResourceStorageDao;
import org.opennms.netmgt.model.LocationMonitorIpInterface;
import org.opennms.netmgt.model.OnmsAttribute;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.OnmsResourceType;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.model.ResourceTypeUtils;
import org.springframework.orm.ObjectRetrievalFailureException;

import com.google.common.collect.Lists;

/**
 * Distributed status resources are stored in paths like:
 *   response/distributed/${locationMonitorId}/${ipaddr}/ds.rrd
 *
 */
public class DistributedStatusResourceType implements OnmsResourceType {

    /** Constant <code>DISTRIBUTED_DIRECTORY="distributed"</code> */
    public static final String DISTRIBUTED_DIRECTORY = "distributed";

    public static final String TYPE_NAME = "distributedStatus";

    private final ResourceStorageDao m_resourceStorageDao;
    private final LocationMonitorDao m_locationMonitorDao;
    
    /**
     * <p>Constructor for DistributedStatusResourceType.</p>
     *
     * @param resourceStorageDao a {@link org.opennms.netmgt.dao.api.ResourceStorageDao} object.
     * @param locationMonitorDao a {@link org.opennms.netmgt.dao.api.LocationMonitorDao} object.
     */
    public DistributedStatusResourceType(ResourceStorageDao resourceStorageDao, LocationMonitorDao locationMonitorDao) {
        m_resourceStorageDao = resourceStorageDao;
        m_locationMonitorDao = locationMonitorDao;
    }

    /**
     * <p>getLabel</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getLabel() {
        return "Distributed Status";
    }

    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getName() {
        return TYPE_NAME;
    }

    /** {@inheritDoc} */
    @Override
    public String getLinkForResource(OnmsResource resource) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isResourceTypeOnParent(OnmsResource parent) {
        return getResourcesForParent(parent).size() > 0;
    }

    /** {@inheritDoc} */
    @Override
    public List<OnmsResource> getResourcesForParent(OnmsResource parent) {
        if (!NodeResourceType.isNode(parent)) {
            return Collections.emptyList();
        }

        final OnmsNode node = ResourceTypeUtils.getNodeFromResource(parent);
        final List<OnmsResource> resources = Lists.newLinkedList();
        final Collection<LocationMonitorIpInterface> statuses = m_locationMonitorDao.findStatusChangesForNodeForUniqueMonitorAndInterface(node.getId());

        for (LocationMonitorIpInterface status : statuses) {
            String definitionName = status.getLocationMonitor().getLocation();
            String id = status.getLocationMonitor().getId();
            final OnmsIpInterface ipInterface = status.getIpInterface();
            String ipAddr = InetAddressUtils.str(ipInterface.getIpAddress());

            if (m_resourceStorageDao.exists(getRelativeInterfacePath(id, ipAddr), 0)) {
                resources.add(createResource(definitionName, id, ipAddr));
            }
        }

        return OnmsResource.sortIntoResourceList(resources);
    }

    /** {@inheritDoc} */
    @Override
    public OnmsResource getChildByName(OnmsResource parent, String name) {
        // Load all of the resources and search. This is not the most efficient approach,
        // but resources of this type should be sparse.
        for (OnmsResource resource : getResourcesForParent(parent)) {
            if (resource.getName().equals(name)) {
                resource.setParent(parent);
                return resource;
            }
        }

        throw new ObjectRetrievalFailureException(OnmsResource.class, "No child with name '" + name + "' found on '" + parent + "'");
    }

    private OnmsResource createResource(String definitionName,
            String locationMonitorId, String ipAddress) {
        String monitor = definitionName + "-" + locationMonitorId;
        String label = ipAddress + " from " + monitor;

        final ResourcePath path = getRelativeInterfacePath(locationMonitorId, ipAddress);
        final LazyResourceAttributeLoader loader = new LazyResourceAttributeLoader(m_resourceStorageDao, path);
        final Set<OnmsAttribute> set = new LazySet<OnmsAttribute>(loader);
        return new OnmsResource(getResourceName(locationMonitorId, ipAddress), label, this, set, path);
    }

    protected static String getResourceName(String locationMonitorId, String ipAddress) {
        return String.format("%s%s%s", locationMonitorId, File.separator, ipAddress);
    }

    private static ResourcePath getRelativeInterfacePath(String id, String ipAddr) {
        return new ResourcePath(
                ResourceTypeUtils.RESPONSE_DIRECTORY,
                DISTRIBUTED_DIRECTORY,
                id,
                ipAddr);
    }
}
