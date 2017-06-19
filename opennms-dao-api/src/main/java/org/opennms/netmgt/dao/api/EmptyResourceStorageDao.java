/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.opennms.netmgt.model.OnmsAttribute;
import org.opennms.netmgt.model.ResourcePath;

public class EmptyResourceStorageDao implements ResourceStorageDao {

    @Override
    public boolean exists(ResourcePath path, int depth) {
        return false;
    }

    @Override
    public boolean existsWithin(ResourcePath path, int depth) {
        return false;
    }

    @Override
    public Set<ResourcePath> children(ResourcePath path, int depth) {
        return Collections.emptySet();
    }

    @Override
    public boolean delete(ResourcePath path) {
        return false;
    }

    @Override
    public Set<OnmsAttribute> getAttributes(ResourcePath path) {
        return Collections.emptySet();
    }

    @Override
    public void setStringAttribute(ResourcePath path, String key, String value) {
        // pass
    }

    @Override
    public String getStringAttribute(ResourcePath path, String key) {
        return null;
    }

    @Override
    public Map<String, String> getStringAttributes(ResourcePath path) {
        return Collections.emptyMap();
    }

    @Override
    public void updateMetricToResourceMappings(ResourcePath path, Map<String, String> metricsNameToResourceNames) {
        // pass
    }

    @Override
    public Map<String, String> getMetaData(ResourcePath path) {
        return Collections.emptyMap();
    }

}
