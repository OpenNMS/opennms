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
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.BasicComponent;
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
@Command(scope = "jira", name = "list-components", description="Uses the JIRA ReST API to list all components")
@org.apache.karaf.shell.commands.Command(scope = "jira", name = "list-components", description="Uses the JIRA ReST API to list all components")
@Service
public class ListComponentsCommand extends AbstractJiraCommand implements Action {

    @Option(name="-k", aliases="--project-key", description="A project key to limit the components to. If defined it overwrites the one defined in the jira ticketer plugin configuration", required = true)
    String projectKey;

    @Override
    protected void doExecute(JiraRestClient jiraRestClient) throws Exception {
        final String theProjectKey = Strings.isNullOrEmpty(projectKey) ? getConfig().getProjectKey() : projectKey;
        final Iterable<BasicComponent> components = jiraRestClient.getProjectClient().getProject(theProjectKey).get().getComponents();
        if (!components.iterator().hasNext()) {
            System.out.println("No components found for project '" + theProjectKey + "'");
            return;
        }
        System.out.println(String.format(DEFAULT_ROW_FORMAT, "Id", "Name", "Description"));
        for (BasicComponent eachComponent : components) {
            System.out.println(
                    String.format(DEFAULT_ROW_FORMAT,
                            eachComponent.getId(),
                            eachComponent.getName(),
                            eachComponent.getDescription() == null ? "" : removeNewLines(eachComponent.getDescription())));
        }
    }
}
