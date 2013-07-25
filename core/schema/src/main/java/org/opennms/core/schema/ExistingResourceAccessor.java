/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.core.schema;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

import liquibase.resource.ResourceAccessor;

import org.springframework.core.io.Resource;

public class ExistingResourceAccessor implements ResourceAccessor {
    private final Resource m_resource;

    public ExistingResourceAccessor() {
        m_resource = null;
    }

    public ExistingResourceAccessor(final Resource resource) {
        m_resource = resource;
    }

    @Override
    public InputStream getResourceAsStream(final String file) throws IOException {
        if (m_resource == null) return null;
        return m_resource.createRelative(file).getInputStream();
    }

    @Override
    public Enumeration<URL> getResources(final String packageName) throws IOException {
        throw new UnsupportedOperationException("Not yet implemented!");
        /*
        final Vector<URL> resources = new Vector<URL>();
        if (m_resource != null) {
            resources.add(m_resource.getURI().toURL());
        }
        return resources.elements();
        */
    }

    @Override
    public ClassLoader toClassLoader() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public String toString() {
        return m_resource.toString();
    }
}
