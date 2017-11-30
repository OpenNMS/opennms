/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2015 The OpenNMS Group, Inc.
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
     */
    public void setStringAttribute(ResourcePath path, String key, String value);

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
