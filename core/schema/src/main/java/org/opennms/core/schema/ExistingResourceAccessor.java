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
