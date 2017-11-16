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

package org.opennms.plugins.elasticsearch.rest.credentials;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.List;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CredentialsParser {

    private static final Logger LOG = LoggerFactory.getLogger(CredentialsParser.class);

    public List<CredentialsDTO> parse(Dictionary<String, Object> properties) {
        if (properties.isEmpty()) {
            return Collections.emptyList();
        }

        final List<CredentialsDTO> credentialsList = new ArrayList<>();
        final List<String> keys = Collections.list(properties.keys());
        for (String originalKey : keys) {
            // Flatten key
            final String flattenedKey = flattenKey(originalKey);

            // Retrieve value
            Object object = properties.get(originalKey);
            if (object == null || !(object instanceof String)) {
                LOG.warn("Detected non string property for key '{}'. Skipping.", originalKey);
                continue;
            }
            final String value = (String) object;

            // Parse properties
            final String[] usernamePassword = value.split(":");
            final String[] hostPort = flattenedKey.split(":");

            // Verify correct format
            if (usernamePassword.length != 2) {
                LOG.warn("Could not determine username:password from value '{}' for key '{}'. Ignoring", value, originalKey);
                continue;
            }
            if (hostPort.length != 2) {
                LOG.warn("could not determine host:port from key '{}'", originalKey);
                continue;
            }

            // Finally set it
            try {
                // Try parsing port
                final int port = Integer.parseInt(hostPort[1]);

                // Set credentials
                final CredentialsDTO credentials = new CredentialsDTO(
                        new AuthScope(hostPort[0], port),
                        new UsernamePasswordCredentials(usernamePassword[0], usernamePassword[1])
                );

                credentialsList.add(credentials);
            } catch (NumberFormatException ex) {
                LOG.error("Defined port " + hostPort[1] + " is not a valid number. Skipping username:password definition for " + originalKey, ex);
                continue;
            }
        }

        return credentialsList;
    }

    // remove http:// or https:// if provided
    private static String flattenKey(String key) {
        if (key.startsWith("http://")) {
            key = key.substring("http://".length());
        }
        if (key.startsWith("https://")) {
            key = key.substring("https://".length());
        }
        return key;
    }
}
