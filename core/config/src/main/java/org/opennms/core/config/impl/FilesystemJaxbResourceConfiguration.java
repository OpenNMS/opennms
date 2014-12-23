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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.opennms.core.config.api.ConfigurationResourceException;
import org.opennms.core.xml.JaxbUtils;

import com.google.common.base.Charsets;

public class FilesystemJaxbResourceConfiguration<T> extends AbstractPathCachingConfigurationResource<T> {
    private Class<T> m_class;

    public FilesystemJaxbResourceConfiguration(final Class<T> clazz, final Path path) {
        super(path);
        m_class = clazz;
    }

    protected Class<T> getClassType() {
        return m_class;
    }

    @Override
    public T load() throws ConfigurationResourceException {
        final Class<T> classType = getClassType();
        final Path path = getPath();
        try {
            return JaxbUtils.unmarshal(classType, path.toFile());
        } catch (final Exception e) {
            throw new ConfigurationResourceException("Failed to unmarshal " + path + " to class " + classType, e);
        }
    }

    @Override
    public void save(final T config) throws ConfigurationResourceException {
        final Path p = getPath();
        if (!(Files.isWritable(p))) {
            throw new ConfigurationResourceException("Resource " + p + " is not writable!");
        }
        try {
            final String output = JaxbUtils.marshal(config);
            Files.write(p, output.getBytes(Charsets.UTF_8), StandardOpenOption.TRUNCATE_EXISTING);
        } catch (final IOException e) {
            throw new ConfigurationResourceException("Failed to write to " + p, e);
        } catch (final Exception e) {
            throw new ConfigurationResourceException("Failed to marshal configuration " + getClassType() + " to resource " + p, e);
        }
    }
}
