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

import java.util.Comparator;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.netmgt.ticketer.jira.JiraClientUtils;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

/**
 * <p>This command implements the Apache Karaf 3 and Apache Karaf 4 shell APIs.
 * Once the Karaf 4 commands work, the deprecated Karaf 3 annotations should 
 * be removed:</p>
 * <ul>
 * <li>{@link org.apache.karaf.shell.commands.Command}</li>
 * <li>{@link org.apache.karaf.shell.console.OsgiCommandSupport}</li>
 * </ul>
 */
@Command(scope = "opennms", name = "jira-list-issue-types", description="Uses the JIRA ReST API to list all issue types")
@org.apache.karaf.shell.commands.Command(scope = "opennms", name = "jira-list-issue-types", description="Uses the JIRA ReST API to list all issue types")
@Service
public class ListIssueTypesCommand extends AbstractJiraCommand implements Action {

    @Option(name="-k", aliases="--project-key", description="The project to limit the issue types for. If defined it overwrites the default defined in the jira ticketing plugin configuration.")
    String projectKey;

    @Override
    protected void doExecute(JiraRestClient jiraRestClient) throws Exception {
        final String theProjectKey = Strings.isNullOrEmpty(projectKey) ? getConfig().getProjectKey() : projectKey;
        final Iterable<? extends IssueType> issueTypes = JiraClientUtils.getIssueTypes(jiraRestClient, theProjectKey);
        if (!issueTypes.iterator().hasNext()) {
            if (Strings.isNullOrEmpty(theProjectKey)) {
                System.out.println("No issue types found. The user making the ReST call may not have sufficient permissions.");
            } else {
                System.out.println("No issue types found for project with key '" + theProjectKey + "' found. The user making the ReST call may not have sufficient permissions.");
            }
        } else {
            System.out.println(String.format(DEFAULT_ROW_FORMAT, "Id", "Name", "Description"));
            Lists.newArrayList(issueTypes).stream()
                    .sorted(Comparator.comparing(IssueType::getId))
                    .forEach(issueType -> System.out.println(String.format(DEFAULT_ROW_FORMAT, issueType.getId(), issueType.getName(), removeNewLines(issueType.getDescription()))));
        }
    }


}
