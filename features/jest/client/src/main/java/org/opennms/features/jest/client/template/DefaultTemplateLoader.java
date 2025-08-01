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
package org.opennms.features.jest.client.template;

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.ByteStreams;

public class DefaultTemplateLoader implements TemplateLoader {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultTemplateLoader.class);

    @Override
    public String load(Version serverVersion, String resource) throws IOException {
        // Attempt to find a template that ends in .es#.json where # is the major version
        // of the ES server. If no template is found for the current major version, try the previous,
        // and so on. If no template is found then attempt to look it up without the version in the suffix.
        for (int i = serverVersion.getMajor(); i >= 0; i--) {
            final String versionSuffix = i == 0 ? "" : String.format(".es%d", i);
            final String resourceWithSuffix = String.format("%s%s.json", resource, versionSuffix);
            LOG.debug("Attempting to find template with resource name: {} (requested: {})", resourceWithSuffix, resource);
            final String template = getResource(resourceWithSuffix);
            if (template != null) {
                LOG.info("Using template with resource name: {} (requested: {})", resourceWithSuffix, resource);
                return template;
            }
            LOG.debug("No template found with resource name: {} (requested: {})", resourceWithSuffix, resource);
        }
        throw new NullPointerException(String.format("No template found for server version %s and resource %s.",
                serverVersion, resource));
    }

    protected String getResource(String resource) throws IOException {
        try (InputStream inputStream = getResourceAsStream(resource)) {
            if (inputStream == null) {
                return null;
            }
            // Read template
            final byte[] bytes = new byte[inputStream.available()];
            ByteStreams.readFully(inputStream, bytes);
            return new String(bytes);
        }
    }

    protected InputStream getResourceAsStream(String resource) throws IOException {
        return getClass().getResourceAsStream(resource);
    }
}
