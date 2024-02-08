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
package org.opennms.netmgt.ticketer.jira;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.opennms.api.integration.ticketing.PluginException;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.auth.AnonymousAuthenticationHandler;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.google.common.base.Strings;

/**
 * Factory to create a {@link JiraRestClient}.
 *
 * @author mvrueden
 */
public class JiraConnectionFactory {

    public static JiraRestClient createConnection(String url, String username, String password) throws PluginException {
        try {
            final URI jiraUri = new URL(url).toURI();
            if (Strings.isNullOrEmpty(username)) {
                return new AsynchronousJiraRestClientFactory().create(jiraUri, new AnonymousAuthenticationHandler());
            }
            return new AsynchronousJiraRestClientFactory().createWithBasicHttpAuthentication(jiraUri, username, password);
        } catch (MalformedURLException | URISyntaxException e) {
            throw new PluginException("Failed to parse URL: " + url);
        }
    }
}
