/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collection.support.builder;

import static org.opennms.netmgt.collection.api.CollectionResource.RESOURCE_TYPE_LATENCY;

import java.util.Objects;

import org.opennms.core.utils.LocationUtils;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.model.ResourcePath;

import com.google.common.base.MoreObjects;

public class LatencyTypeResource extends AbstractResource {

    private final String serviceName;
    private final String ipAddress;
    private final String location;

    public LatencyTypeResource(String serviceName, String ipAddress, String location) {
        this.serviceName = serviceName;
        this.ipAddress = ipAddress;
        this.location = location;
    }

    @Override
    public Resource getParent() {
        return null;
    }

    @Override
    public String getTypeName() {
        return RESOURCE_TYPE_LATENCY;
    }

    @Override
    public String getInstance() {
        return ipAddress + "[" + serviceName + "]";
    }

    @Override
    public String getUnmodifiedInstance() {
        return getInstance();
    }

    @Override
    public String getLabel(CollectionResource resource) {
        return null;
    }

    @Override
    public ResourcePath getPath(CollectionResource resource) {
        if (LocationUtils.isDefaultLocationName(location)) {
            return ResourcePath.get(ipAddress);
        } else {
            return ResourcePath.get(ResourcePath.sanitize(location), ipAddress);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LatencyTypeResource that = (LatencyTypeResource) o;
        return Objects.equals(serviceName, that.serviceName) &&
                Objects.equals(ipAddress, that.ipAddress) &&
                Objects.equals(location, that.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceName, ipAddress, location);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("serviceName", serviceName)
                .add("ipAddress", ipAddress)
                .add("location", location)
                .toString();
    }
}
