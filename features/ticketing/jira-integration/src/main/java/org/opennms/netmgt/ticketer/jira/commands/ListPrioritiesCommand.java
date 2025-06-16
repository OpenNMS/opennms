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
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Priority;

/**
 * <p>This command implements the Apache Karaf 3 and Apache Karaf 4 shell APIs.
 * Once the Karaf 4 commands work, the deprecated Karaf 3 annotations should 
 * be removed:</p>
 * <ul>
 * <li>{@link org.apache.karaf.shell.commands.Command}</li>
 * <li>{@link org.apache.karaf.shell.console.OsgiCommandSupport}</li>
 * </ul>
 */
@Command(scope = "opennms", name = "jira-list-priorities", description="Uses the JIRA ReST API to list all priorities")
@org.apache.karaf.shell.commands.Command(scope = "opennms", name = "jira-list-priorities", description="Uses the JIRA ReST API to list all priorities")
@Service
public class ListPrioritiesCommand extends AbstractJiraCommand implements Action {

    @Override
    protected void doExecute(JiraRestClient jiraRestClient) throws Exception {
        Iterable<Priority> priorities = jiraRestClient.getMetadataClient().getPriorities().get();
        if (!priorities.iterator().hasNext()) {
            System.out.println("No priorities found");
            return;
        }

        System.out.println(String.format(DEFAULT_ROW_FORMAT, "Id", "Name", "Description"));
        for (Priority eachPriority : priorities) {
            System.out.println(
                    String.format(
                            DEFAULT_ROW_FORMAT,
                            eachPriority.getId(),
                            eachPriority.getName(),
                            removeNewLines(eachPriority.getDescription())));
        }
    }
}
