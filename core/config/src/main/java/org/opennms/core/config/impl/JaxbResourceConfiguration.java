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
