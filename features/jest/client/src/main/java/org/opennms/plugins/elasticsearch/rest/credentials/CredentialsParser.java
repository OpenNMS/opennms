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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

public class CredentialsParser {

    private static final Logger LOG = LoggerFactory.getLogger(CredentialsParser.class);

    public Map<AuthScope, Credentials> parse(final List<CredentialsScope> credentialsScopes) {
        final Map<AuthScope, Credentials> credentialsMap = new HashMap<>();
        if (credentialsScopes != null) {
            for (CredentialsScope scope : credentialsScopes) {
                // Verify username/password
                if (Strings.isNullOrEmpty(scope.getUsername()) || Strings.isNullOrEmpty(scope.getPassword())) {
                    LOG.warn("Found incomplete credentials (username={}, password={}) for host {}. Username or password is empty/null. Ignoring.",
                            scope.getUsername(),
                            scope.getPassword() != null ? scope.getPassword().isEmpty() ? "" : "******" : null, // Censor password if provided
                            scope.getUrl());
                    continue;
                }
                // Verify URL
                if (Strings.isNullOrEmpty(scope.getUrl())) {
                    LOG.warn("No url specified. Ignoring.");
                    continue;
                }

                // Try parsing url
                final String urlString = fixUrl(scope.getUrl());
                try {
                    final URL url = new URL(urlString);
                    final HttpHost httpHost = new HttpHost(url.getHost(), url.getPort(), url.getProtocol());
                    credentialsMap.put(
                            new AuthScope(httpHost),
                            new UsernamePasswordCredentials(scope.getUsername(), scope.getPassword()));
                } catch (MalformedURLException ex) {
                    LOG.error("Defined url is invalid: {}", ex.getMessage());
                }
            }
        }
        return credentialsMap;
    }

    private static String fixUrl(String input) {
        if (!input.startsWith("http://") && !input.startsWith("https://")) {
            return "http://" + input;
        }
        return input;
    }
}
