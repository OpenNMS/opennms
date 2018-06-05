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
import org.apache.karaf.shell.api.action.lifecycle.Service;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.BasicProject;

/**
 * <p>This command implements the Apache Karaf 3 and Apache Karaf 4 shell APIs.
 * Once the Karaf 4 commands work, the deprecated Karaf 3 annotations should 
 * be removed:</p>
 * <ul>
 * <li>{@link org.apache.karaf.shell.commands.Command}</li>
 * <li>{@link org.apache.karaf.shell.console.OsgiCommandSupport}</li>
 * </ul>
 */
@Command(scope = "jira", name = "list-projects", description="Uses the JIRA ReST API to determine all existing projects")
@org.apache.karaf.shell.commands.Command(scope = "jira", name = "list-projects", description="Uses the JIRA ReST API to determine all existing projects")
@Service
public class ListProjectsCommand extends AbstractJiraCommand implements Action {

    @Override
    protected void doExecute(JiraRestClient jiraRestClient) throws Exception {
        Iterable<BasicProject> basicProjects = jiraRestClient.getProjectClient().getAllProjects().get();
        if (!basicProjects.iterator().hasNext()) {
            System.out.println("No projects available or visible to the current user");
        } else {
            System.out.println(String.format(DEFAULT_ROW_FORMAT, "Key", "Name", "Description"));
            for (BasicProject eachProject : basicProjects) {
                String description = jiraRestClient.getProjectClient().getProject(eachProject.getKey()).get().getDescription();
                System.out.println(
                        String.format(DEFAULT_ROW_FORMAT, eachProject.getKey(), eachProject.getName(), description == null ? "" : removeNewLines(description)));
            }
        }
    }
}
