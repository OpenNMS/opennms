/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.core.config.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.apache.commons.io.IOUtils;
import org.opennms.core.config.api.ConfigurationResource;
import org.opennms.core.config.api.ConfigurationResourceException;
import org.opennms.core.xml.JaxbUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.WritableResource;

public class JaxbResourceConfiguration<T> implements ConfigurationResource<T> {
    private Class<T> m_class;
    private Resource m_resource;

    public JaxbResourceConfiguration(final Class<T> clazz, final Resource resource) {
        m_class = clazz;
        m_resource = resource;
    }

    protected Class<T> getClassType() {
        return m_class;
    }
    
    protected Resource getResource() {
        return m_resource;
    }

    public T get() throws ConfigurationResourceException {
        final Class<T> classType = getClassType();
        final Resource resource = getResource();
        try {
            return JaxbUtils.unmarshal(classType, resource);
        } catch (final Exception e) {
            throw new ConfigurationResourceException("Failed to unmarshal " + resource + " to class " + classType, e);
        }
    }
    
    public void save(final T config) throws ConfigurationResourceException {
        final Resource r = getResource();
        if (!(r instanceof WritableResource)) {
            throw new ConfigurationResourceException("Resource " + r + " is not writable!");
        }
        final WritableResource resource = (WritableResource)r;
        OutputStream os = null;
        OutputStreamWriter osw = null;
        try {
            os = resource.getOutputStream();
            osw = new OutputStreamWriter(os);
            JaxbUtils.marshal(config, osw);
        } catch (final IOException e) {
            throw new ConfigurationResourceException("Failed to write to " + r, e);
        } catch (final Exception e) {
            throw new ConfigurationResourceException("Failed to marshal configuration " + getClassType() + " to resource " + r, e);
        } finally {
            IOUtils.closeQuietly(osw);
            IOUtils.closeQuietly(os);
        }
    }
}
