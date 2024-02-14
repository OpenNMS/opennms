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
package org.opennms.netmgt.ticketer.jira.commands;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.opennms.api.integration.ticketing.PluginException;
import org.opennms.core.mate.api.EntityScopeProvider;
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

    protected EntityScopeProvider entityScopeProvider;

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

    public void setEntityScopeProvider(EntityScopeProvider entityScopeProvider) {
        this.entityScopeProvider = entityScopeProvider;
    }

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
        return JiraTicketerPlugin.getConfig(entityScopeProvider);
    }

    abstract protected void doExecute(JiraRestClient jiraRestClient) throws Exception;

    protected static String removeNewLines(String input) {
        return input.replaceAll(System.getProperty("line.separator"), "");
    }
}
