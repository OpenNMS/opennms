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

import java.util.Objects;

import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.model.ResourcePath;

public class PerspectiveResponseTimeResource extends AbstractResource {

    private final String location;
    private final String address;
    private final String service;

    public PerspectiveResponseTimeResource(final String location,
                                           final String address,
                                           final String service) {
        this.location = Objects.requireNonNull(location);
        this.address = Objects.requireNonNull(address);
        this.service = Objects.requireNonNull(service);
    }

    @Override
    public Resource getParent() {
        return null;
    }

    @Override
    public String getTypeName() {
        return CollectionResource.RESOURCE_TYPE_IF;
    }

    @Override
    public String getInstance() {
        return String.format("%s[%s]@%s", this.address, this.service, this.location);
    }

    @Override
    public String getUnmodifiedInstance() {
        return this.getInstance();
    }

    @Override
    public String getLabel(final CollectionResource resource) {
        return this.service;
    }

    @Override
    public ResourcePath getPath(final CollectionResource resource) {
        return ResourcePath.get("perspective", this.location);
    }
}
