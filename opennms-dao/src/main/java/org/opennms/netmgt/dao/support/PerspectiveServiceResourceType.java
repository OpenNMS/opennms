/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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
