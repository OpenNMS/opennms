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

package org.opennms.netmgt.ticketer.jira.commands;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.opennms.api.integration.ticketing.PluginException;
import org.opennms.netmgt.ticketer.jira.Config;
import org.opennms.netmgt.ticketer.jira.JiraConnectionFactory;
import org.opennms.netmgt.ticketer.jira.JiraTicketerPlugin;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.google.common.base.Strings;

/**
 * <p>This command implements the Apache Karaf 3 and Apache Karaf 4 shell APIs.
 * Once the Karaf 4 commands work, the deprecated Karaf 3 annotations should 
 * be removed:</p>
 * <ul>
 * <li>{@link org.apache.karaf.shell.commands.Command}</li>
 * <li>{@link org.apache.karaf.shell.console.OsgiCommandSupport}</li>
 * </ul>
 */
public abstract class AbstractJiraCommand extends OsgiCommandSupport implements Action {

    protected static final String LINE = "------------------------------";

    protected static final String DEFAULT_ROW_FORMAT = "%-10.10s %-30.30s %-100.100s";

    @Option(name="-h", aliases="--host", description="The jira host to use. If defined it overrides the default defined in the jira ticketer plugin configuration.")
    public String host;

    @Option(name="-u", aliases="--username", description="The user to connect to jira. If defined it overrides the default defined in the jira ticketer plugin configuration.")
    public String username;

    @Option(name="-p", aliases="--password", description="The password to use to connect to jira. If defined it overrides the default defined in the jira ticketer plugin configuration.")
    public String password;

    @Option(name="-a", aliases="--anonymous", description="Defines that no authentication is used. Cannot be used with <username> and <password> alltogether")
    public boolean noAuthentication;

    protected JiraRestClient createJiraClient() throws PluginException {
        final Config config = getConfig();
        final String theHost = Strings.isNullOrEmpty(host) ? config.getHost() : host;
        final String theUser = Strings.isNullOrEmpty(username) ? config.getUsername() : username;
        final String thePassword = Strings.isNullOrEmpty(password) ? config.getPassword() : password;

        if (noAuthentication) {
            return JiraConnectionFactory.createConnection(theHost, null, null);
        }
        return JiraConnectionFactory.createConnection(theHost, theUser, thePassword);
    }

    @Override
    public Object execute() throws Exception {
        JiraRestClient jiraClient = createJiraClient();
        try {
            doExecute(jiraClient);
        } finally {
            jiraClient.close();
        }
        return null;
    }

    @Override
    @Deprecated
    protected final Object doExecute() throws Exception {
        return execute();
    }

    protected Config getConfig() {
        return JiraTicketerPlugin.getConfig();
    }

    abstract protected void doExecute(JiraRestClient jiraRestClient) throws Exception;

    protected static String removeNewLines(String input) {
        return input.replaceAll(System.getProperty("line.separator"), "");
    }
}
