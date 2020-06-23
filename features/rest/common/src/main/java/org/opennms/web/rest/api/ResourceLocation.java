/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015-2015 The OpenNMS Group, Inc.
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

package org.opennms.web.rest.api;

import java.util.Objects;

/**
 * Points to the resource location of a certain resource to not have a client worry about where to find an element, e.g.
 * http://localhost:8980/opennms/api/v2/business-services/1. The ResourceLocation always assumes a valid result with HTTP-Method: GET.
 */
public class ResourceLocation {

    private ApiVersion version;
    private String path;

    public ResourceLocation() {

    }

    public ResourceLocation(ApiVersion version, String... path) {
        this.version = Objects.requireNonNull(version);
        setPath(path);
    }

    private void setPath(String... path) {
        Objects.requireNonNull(path);
        final StringBuilder pathBuilder = new StringBuilder();
        for (String eachPath : path) {
            pathBuilder.append(eachPath);
            if (!eachPath.endsWith("/")) {
                pathBuilder.append("/");
            }
        }
        this.path = pathBuilder.toString();
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder(version.getContextPath());
        builder.append(path);
        String pathString = builder.toString();
        if (pathString.endsWith("/")) {
            pathString = pathString.substring(0, pathString.length()-1);
        }
        return pathString;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ResourceLocation other = (ResourceLocation) obj;
        final boolean equals = Objects.equals(version, other.version) && Objects.equals(path, other.path);
        return equals;
    }

    public static ResourceLocation parse(String locationString) {
        ApiVersion version = Objects.requireNonNull(getApiVersion(locationString));
        String path = locationString.replaceFirst(version.getContextPath(), "");
        return new ResourceLocation(version, path);
    }

    private static ApiVersion getApiVersion(String locationString) {
        for (ApiVersion eachVersion : ApiVersion.values()) {
            if (locationString.startsWith(eachVersion.getContextPath())) {
                return eachVersion;
            }
        }
        return null;
    }
}
