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
package org.opennms.features.jest.client.credentials;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.opennms.core.mate.api.EntityScopeProvider;
import org.opennms.core.mate.api.Interpolator;
import org.opennms.core.mate.api.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

public class CredentialsParser {

    private static final Logger LOG = LoggerFactory.getLogger(CredentialsParser.class);

    public Map<AuthScope, Credentials> parse(final List<CredentialsScope> credentialsScopes, final EntityScopeProvider entityScopeProvider) {
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
                    final Scope scvScope = entityScopeProvider.getScopeForScv();
                    credentialsMap.put(
                            new AuthScope(httpHost),
                            new UsernamePasswordCredentials(Interpolator.interpolate(scope.getUsername(), scvScope).output, Interpolator.interpolate(scope.getPassword(), scvScope).output));
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
