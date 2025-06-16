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
package org.opennms.netmgt.dao.api;

import java.util.Map;
import java.util.Set;

import org.opennms.netmgt.model.OnmsAttribute;
import org.opennms.netmgt.model.ResourcePath;

/**
 * This interface is used to abstract the I/O operations used to walk the resource tree
 * and retrieve or manipulate resource details.
 *
 * @author jwhite
 * @see org.opennms.netmgt.dao.api.ResourceDao
 * @see org.opennms.netmgt.model.ResourcePath
 */
public interface ResourceStorageDao {

    /**
     * Verifies if a path contains one or more metrics
     * at the given depth.
     * <p>
     * For example, assume we are working with file-system paths,
     * and we have the following file on disk /a/b/c/some.metric.
     * The function should behave as follows:
     * <ul>
     * <li> exists('/a/b/c', 0) -> true
     * <li> exists('/a/b', 1) -> true
     * <li> exists('/a/b', 2) -> false
     * </ul>
     *
     * @param path resource path used as the root of the check
     * @param depth a non-negative integer
     * @return true if one or more metrics exist, false otherwise
     */
    public boolean exists(ResourcePath path, int depth);

    /**
     * Verifies if a path contains one or more metrics within
     * the given depth.
     * <p>
     * For example, assume we are working with file-system paths,
     * and we have the following file on disk /a/b/c/some.metric.
     * The function should behave as follows:
     * <ul>
     * <li> exists('/a/b/c', 0) -> true
     * <li> exists('/a/b', 1) -> true
     * <li> exists('/a/b', 2) -> true
     * <li> exists('/a', 1) -> false
     * </ul>
     *
     * @param path resource path used as the root of the check
     * @param depth a non-negative integer
     * @return true if one or more metrics exist, false otherwise
     */
    public boolean existsWithin(ResourcePath path, int depth);

    /**
     * Retrieves the set of child paths one level bellow the given
     * path which contain one or more metrics at the given depth.
     * <p>
     * For example, assume we are working with file-system paths,
     * and we have the following file on disk /a/b/c/some.metric.
     * The function should behave as follows:
     * <ul>
     * <li> children('/a/b', 1) -> {'/a/b/c'}
     * <li> children('/a/b', 2) -> {}
     * <li> children('/a', 2) -> {'b'}
     * </ul>
     *
     * @param path resource path used as the root of the check
     * @param depth a positive integer
     * @return the set of child paths containing metrics
     */
    public Set<ResourcePath> children(ResourcePath path, int depth);

    /**
     * Deletes all of the resources and metric bellow
     * the given path.
     *
     * Returns true if the operation succeeded.
     */
    public boolean delete(ResourcePath path);

    /**
     * Retrieves the set of (resource-level) attributes stored at the given path.
     */
    public Set<OnmsAttribute> getAttributes(ResourcePath path);

    /**
     * Sets the specified (resource-level) attribute at the given path.
     * Should not be called directly but only through the BasePersister.
     */
    void setStringAttribute(ResourcePath path, String key, String value);

    /**
     * Returns the value for the given (resource-level) attribute, or null if it does not exist.
     */
    public String getStringAttribute(ResourcePath path, String key);

    /**
     * Returns the value for the given (resource-level) attribute, or null if it does not exist.
     */
    public Map<String, String> getStringAttributes(ResourcePath path);

    /**
     * Maps the given metric names to the their associated resources names.
     * The resource names are relative to the given path.
     *
     * When persisting to .rrd of .jrb files with storeByGroup enabled,
     * this is used to map the data sources names (metrics) to associated
     * .rrd files (resource names).
     *
     * Other strategies that can infer this information at runtime may chose
     * to ignore calls to this method.
     *
     * @param path parent resource path
     * @param metricsNameToResourceNames metric to resource mappings
     */
    public void updateMetricToResourceMappings(ResourcePath path, Map<String, String> metricsNameToResourceNames);

    /**
     * Retrieves the meta-data stored at the given path.
     */
    public Map<String,String> getMetaData(ResourcePath path);
}
