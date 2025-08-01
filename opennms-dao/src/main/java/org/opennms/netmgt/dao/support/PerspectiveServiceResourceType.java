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
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

public abstract class PerspectiveServiceResourceType implements OnmsResourceType {

    private final ResourceStorageDao resourceStorageDao;

    private final ServiceResourceType serviceType;

    public PerspectiveServiceResourceType(final ResourceStorageDao resourceStorageDao, final ServiceResourceType serviceType) {
        this.resourceStorageDao = resourceStorageDao;
        this.serviceType = serviceType;
    }

    @Override
    public String getLinkForResource(final OnmsResource resource) {
        return null;
    }

    @Override
    public boolean isResourceTypeOnParent(final OnmsResource parent) {
        if (!NodeResourceType.isNode(parent)) {
            return false;
        }

        return !this.getResourcesForParent(parent).isEmpty();
    }

    @Override
    public List<OnmsResource> getResourcesForParent(final OnmsResource parent) {
        if (parent == null) {
            return Collections.emptyList();
        }

        final OnmsNode node = ResourceTypeUtils.getNodeFromResource(parent);
        final String residentLocation = MonitoringLocationUtils.getLocationNameOrNullIfDefault(node);

        return node.getIpInterfaces().stream()
                   .map(OnmsIpInterface::getIpAddress)
                   .map(InetAddressUtils::str)
                   .flatMap(ipAddress -> {
                       final ResourcePath basePath = this.serviceType.getInterfacePath(residentLocation, ipAddress);
                       return this.resourceStorageDao.children(new ResourcePath(basePath, "perspective"), 1).stream()
                                                     .map(path -> createResource(ipAddress, path.getName(), path));
                   })
                   .collect(Collectors.toList());
    }

    @Override
    public OnmsResource getChildByName(final OnmsResource parent, final String ipWithPerspective) {
        final int splitIndex = ipWithPerspective.indexOf('@');
        if (splitIndex == -1) {
            return null;
        }

        final String ipAddress = ipWithPerspective.substring(0, splitIndex);
        final String perspectiveLocation = ipWithPerspective.substring(splitIndex + 1);

        final OnmsNode node = ResourceTypeUtils.getNodeFromResource(parent);
        final String residentLocation = MonitoringLocationUtils.getLocationNameOrNullIfDefault(node);

        final ResourcePath basePath = this.serviceType.getInterfacePath(residentLocation, ipAddress);

        final OnmsResource resource = createResource(ipAddress, perspectiveLocation, new ResourcePath(basePath, "perspective", perspectiveLocation));
        resource.setParent(parent);
        return resource;
    }

    private OnmsResource createResource(final String ipAddress, final String perspectiveLocation, final ResourcePath path) {
        final LazyResourceAttributeLoader loader = new LazyResourceAttributeLoader(this.resourceStorageDao, path);
        final Set<OnmsAttribute> set = new LazySet<>(loader);

        return new OnmsResource(String.format("%s@%s", ipAddress, perspectiveLocation),
                                String.format("%s for %s from %s", this.getLabel(), ipAddress, perspectiveLocation), this, set, path);
    }
}
