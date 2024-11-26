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
package org.opennms.netmgt.collection.support.builder;

import static org.opennms.netmgt.collection.api.CollectionResource.RESOURCE_TYPE_LATENCY;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.opennms.core.utils.LocationUtils;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.model.ResourcePath;

import com.google.common.base.MoreObjects;

public class LatencyTypeResource extends AbstractResource {

    private final String serviceName;
    private final String ipAddress;
    private final String location;
    private final Map<String, String> tags = new HashMap<>();
    private final Map<String, String> serviceParams = new HashMap<>();

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

    public String getIpAddress() {
        return ipAddress;
    }

    @Override
    public Map<String, String> getTags() {
        return tags;
    }

    public void addTag(String name, String value) {
        this.tags.put(name, value);
    }

    @Override
    public Map<String, String> getServiceParams() {
        return serviceParams;
    }


    public void addServiceParam(String name, String value) {
        this.serviceParams.put(name, value);
    }
}
