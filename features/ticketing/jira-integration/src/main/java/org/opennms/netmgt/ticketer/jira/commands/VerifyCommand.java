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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.opennms.api.integration.ticketing.PluginException;
import org.opennms.core.mate.api.EntityScopeProvider;
import org.opennms.netmgt.ticketer.jira.Config;
import org.opennms.netmgt.ticketer.jira.JiraClientUtils;
import org.opennms.netmgt.ticketer.jira.JiraConnectionFactory;
import org.opennms.netmgt.ticketer.jira.JiraTicketerPlugin;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.CimFieldInfo;
import com.atlassian.jira.rest.client.api.domain.CimProject;
import com.atlassian.jira.rest.client.api.domain.ServerInfo;

@Command(scope = "opennms", name = "jira-verify", description="Verifies the current configuration")
@org.apache.karaf.shell.commands.Command(scope = "opennms", name = "jira-verify", description="Verifies the current configuration")
@Service
public class VerifyCommand extends OsgiCommandSupport implements Action {

    protected EntityScopeProvider entityScopeProvider;

    private Config config;

    @Option(name="-f", aliases="--field", description="Verifies the existence of the defined field(s).", required = false, multiValued = true)
    String[] field;

    @Override
    public Object execute() throws Exception {
        config = JiraTicketerPlugin.getConfig(entityScopeProvider);
        JiraRestClient connection = null;
        // Validate all settings
        try {
            System.out.println("Verifing Jira Ticketer Plugin...");
            validateConfiguration();
            connection = verifyConnection();
            validateProjectKey(connection, config.getProjectKey());
            validateIssueType(connection);
            verifyCustomFields(connection);
            return null;
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    public void setEntityScopeProvider(EntityScopeProvider entityScopeProvider) {
        this.entityScopeProvider = entityScopeProvider;
    }

    @Override
    @Deprecated
    protected final Object doExecute() throws Exception {
        return execute();
    }

    private void verifyCustomFields(JiraRestClient client) {
        System.out.println();
        System.out.println("Verifiing custom fields...");

        if (field == null ||field.length == 0) {
            System.out.println("No custom fields configured");
            System.out.println("OK");
            return;
        }

        final Set<String> customFieldsSet = Arrays.stream(field).collect(Collectors.toSet());
        try {
            final List<CimProject> cimProjects = JiraClientUtils.getIssueMetaData(client, "projects.issuetypes.fields", config.getIssueTypeId(), config.getProjectKey());
            if (!cimProjects.iterator().hasNext()
                    && !cimProjects.iterator().next().getIssueTypes().iterator().hasNext()
                    && !cimProjects.iterator().next().getIssueTypes().iterator().next().getFields().isEmpty()) {
                throw new RuntimeException("There are custom fields defined, but none where found in jira");
            }
            final Map<String, CimFieldInfo> fieldInfoMap = cimProjects.iterator().next().getIssueTypes().iterator().next().getFields();

            boolean overall = true;
            for (String customField : customFieldsSet) {
                boolean found = false;
                for (Map.Entry<String, CimFieldInfo> entry : fieldInfoMap.entrySet()) {
                    if (customField.equals(entry.getValue().getId())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    System.out.println("Custom field '" + customField + "' expected but not found in jira");
                }
                overall &= found;
            }
            if (!overall) {
                System.out.println("The jira integration will work, but not all custom fields will be mapped");
            } else {
                System.out.println("OK");
            }

        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Could not read data from jira", e);
        }
    }

    private void validateConfiguration() {
        System.out.println();
        System.out.println("Validating configuration...");
        config.validateRequiredProperties();

        // Verify that the issue type is a number
        try {
            long issueTypeId = config.getIssueTypeId();
            if (issueTypeId < 0) {
                throw new RuntimeException("The issue type id must be >= 0, but was " + issueTypeId);
            }
        } catch (NumberFormatException nfe) {
            throw new RuntimeException(nfe);
        }
        System.out.println("OK");
    }

    private void validateIssueType(JiraRestClient client) {
        System.out.println();
        System.out.println("Validating issue type with name '" + config.getIssueTypeId() + "' for project with key '" + config.getProjectKey() + "'...");
        try {
            List<CimProject> cimProjects = JiraClientUtils.getIssueMetaData(client, null, config.getIssueTypeId(), config.getProjectKey());
            if (!cimProjects.iterator().hasNext() || !cimProjects.iterator().next().getIssueTypes().iterator().hasNext()) {
                throw new RuntimeException("No issue type with id '" + config.getIssueTypeId() + "' found for project with key '" + config.getProjectKey() + "'");
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Could not retrieve issue type with id '" + config.getIssueTypeId() + "' from with key = '" + config.getProjectKey() + "' from jira", e);
        }
        System.out.println("OK");
    }

    private JiraRestClient verifyConnection() {
        final String host = config.getHost();
        final String username = config.getUsername();
        final String password = config.getPassword();

        try {
            System.out.println();
            System.out.println("Try connecting to jira server " + host + " with username: '" + username + "' and password: '" + password +"'...");

            final JiraRestClient connection = JiraConnectionFactory.createConnection(host, username, password);
            final ServerInfo serverInfo = connection.getMetadataClient().getServerInfo().get();

            System.out.println("Successfully connected to jira instance at " + host);
            System.out.println("Server Info:" + serverInfo.toString());
            System.out.println("OK");
            return connection;
        } catch (PluginException | InterruptedException | ExecutionException e) {
            throw new RuntimeException("Could not connect to jira server", e);
        }
    }

    private static void validateProjectKey(JiraRestClient client, String projectKey) {
        System.out.println("Validating project with key '" + projectKey + "'...");
        try {
            if (client.getProjectClient().getProject(projectKey).get() == null) {
                throw new RuntimeException("Project with key = '" + projectKey + "' does not exist");
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Could not retrieve project data for project with key '" + projectKey + "' from jira", e);
        }
        System.out.println("OK");
    }

}
