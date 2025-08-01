/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.dao.support;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
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

public abstract class ServiceResourceType implements OnmsResourceType {

    private final ResourceStorageDao m_resourceStorageDao;
    private final IpInterfaceDao m_ipInterfaceDao;

    public ServiceResourceType(final ResourceStorageDao resourceStorageDao, final IpInterfaceDao ipInterfaceDao) {
        m_resourceStorageDao = resourceStorageDao;
        m_ipInterfaceDao = ipInterfaceDao;
    }

    protected abstract String getDirectory();

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
        final OnmsResource resource = new OnmsResource(ipAddr, String.format("%s for %s", this.getLabel(), ipAddr), this, set, path);
    	resource.setEntity(ipInterface);
        return resource;
    }

    public ResourcePath getInterfacePath(final String location, final String ipAddr) {
        if (location == null) {
            return new ResourcePath(this.getDirectory(), ipAddr);
        } else {
            return new ResourcePath(this.getDirectory(), ResourcePath.sanitize(location), ipAddr);
        }
    }


}
