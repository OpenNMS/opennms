/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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
