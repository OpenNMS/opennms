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
