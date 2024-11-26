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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.model.ResourcePath;

public interface Resource {

    public Resource getParent();

    /**
     * Returns the type name associated with the resource. Used for thresholding.
     *
     * @return type name
     */
    public String getTypeName();

    /**
     * Returns a unique name for the instance of this resource.
     * Used by the {@link org.opennms.netmgt.collection.support.IndexStorageStrategy}
     *
     * @return instance name
     */
    public String getInstance();

    /**
     * Returns the original unmodified unique name for the instance of this resource.
     * Used by the {@link org.opennms.netmgt.collection.support.ObjectNameStorageStrategy}
     *
     * @return instance name
     */
    public String getUnmodifiedInstance();

    /**
     * Returns a unique label for the instance of this resource.
     * This label is used by threshd to generate a unique id on a per resource basis,
     * grouped by node.
     *
     * See {@link org.opennms.netmgt.threshd.CollectionResourceWrapper}
     *
     * @return a {@link java.lang.String} object.
     */
    public String getLabel(CollectionResource resource);

    /**
     * Retrieves the path of the resource, relative to the repository root.
     *
     * @param resource Used by the {@link GenericTypeResource} in order to determine the instance name.
     * @return relative path
     */
    public ResourcePath getPath(CollectionResource resource);

    /**
     * Returns the {@link Date} to use for attributes associated with this resource.
     *
     * @return a {@link Date} or null if the current time should be used.
     */
    public Date getTimestamp();

    /**
     * <p>
     * Used to lookup additional resource related information that may not
     * have been available when the resource was created, and optionally return a
     * new resource.
     * </p>
     *
     * <p>
     * See {@link DeferredGenericTypeResource#resolve()}.
     * </p>
     *
     * <p>
     * This method should only be called when running in the context of the OpenNMS
     * JVM (and not the Minion).
     * </p>
     *
     * @return possibly a new resource, or this same instance if no resolving was performed
     */
    public Resource resolve();


    default Map<String, String> getTags() {
        return new HashMap<>();
    }

    default Map<String, String> getServiceParams() {
        return new HashMap<>();
    }

}
