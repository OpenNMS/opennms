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

package org.opennms.plugins.elasticsearch.rest.template;

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
            final String template = getResource(resourceWithSuffix);
            if (template != null) {
                LOG.info("Using template with resource name: {}", resource);
                return template;
            }
             LOG.debug("No template found with resource name: {}", resource);
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
