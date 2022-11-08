/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.core.schema;

import liquibase.resource.ResourceAccessor;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Set;

public class ExistingResourceAccessor implements ResourceAccessor {
    private final Resource m_resource;

    public ExistingResourceAccessor() {
        m_resource = null;
    }

    public ExistingResourceAccessor(final Resource resource) {
        m_resource = resource;
    }

    @Override
    public Set<InputStream> getResourcesAsStream(String path) throws IOException {
        if (m_resource == null) {
            return Collections.emptySet();
        } else {
            return Collections.singleton(m_resource.createRelative(path).getInputStream());
        }
    }

    @Override
    public Set<String> list(String relativeTo, String path, boolean includeFiles, boolean includeDirectories, boolean recursive) throws IOException {
        throw new UnsupportedOperationException("Unnecessary; not used in temporary database resource access.");
    }

    @Override
    public ClassLoader toClassLoader() {
        throw new UnsupportedOperationException("Unnecessary; not used in temporary database resource access.");
    }

    @Override
    public String toString() {
        return m_resource == null ? "null" : m_resource.toString();
    }
}
