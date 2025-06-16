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
