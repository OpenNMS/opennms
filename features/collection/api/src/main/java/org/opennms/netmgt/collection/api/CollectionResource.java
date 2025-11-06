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
package org.opennms.netmgt.collection.api;

import org.opennms.netmgt.collection.support.DefaultTimeKeeper;
import org.opennms.netmgt.model.ResourcePath;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>CollectionResource interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface CollectionResource extends ResourceIdentifier, CollectionVisitable, Persistable {

    public static final String RESOURCE_TYPE_NODE = "node";
    public static final String RESOURCE_TYPE_IF = "if";
    public static final String RESOURCE_TYPE_LATENCY = "latency";

    // service property which will control addition of custom tags for time series data
    public static final String INTERFACE_INFO_IN_TAGS = "interface-info-in-tags";

    /**
     * <p>rescanNeeded</p>
     *
     * @return a boolean.
     */
    boolean rescanNeeded();
    
    /**
     * Returns a string which indicates what type of resource.
     * Will be one of
     *          "node" for the node level resource
     *          "if" for network interface resources
     *          "*" for all other resource types defined in the relevant config files, e.g. hrStorage
     *
     * @return a {@link java.lang.String} object.
     */
    String getResourceTypeName();
    
    /**
     * Returns the name of the parent resource.
     * 
     * @return a {@link java.lang.String} object.
     */
    ResourcePath getParent();
    
    /**
     * Returns the name of the instance this {@link CollectionResource} represents. For node level resources, this will be null
     * to indicate the default instance. For interface level resources, some label unique to the node (ifIndex probably).
     * For Generic resources (e.g. the SNMP {@link GenericIndexResource}), this will be some identifying label, probably the index in the table.
     * This value is used by the {@link StorageStrategy} implementations to figure out the label for the resource which 
     * is used in constructing its RRD directory.
     *
     * @return a {@link java.lang.String} object.
     */
    String getInstance();

    /**
     * Returns the unmodified instance string this {@link CollectionResource} represents.
     *
     * @return a {@link java.lang.String} object.
     */
    String getUnmodifiedInstance();

    /**
     * Returns a unique label for each resource depending on resource type.
     * This label is the same label used when constructing the resource ID.
     *
     * @return a {@link java.lang.String} object.
     */
    String getInterfaceLabel();

    /**
     * Returns a not-null {@link TimeKeeper} instance when this resource requires to use a special timestamp when updating RRDs.
     * If the resource doesn't need a special {@link TimeKeeper} it should return null.
     * 
     * @return a {@link org.opennms.netmgt.collection.api.TimeKeeper} object or null to indicate that {@link DefaultTimeKeeper} should be used.
     */
    TimeKeeper getTimeKeeper();

    // Can be used to add additional tags required by TimeSeries like Prometheus
    default Map<String, String> getTags() {
        return Collections.emptyMap();
    }

    // Can be used to forward service parameters to persistence layer
    default Map<String, String> getServiceParams() {
        return Collections.emptyMap();
    }

}
