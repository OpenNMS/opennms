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


import org.apache.felix.gogo.commands.Command;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Priority;

@Command(scope = "jira", name = "list-priorities", description="Uses the JIRA ReST API to list all priorities")
public class ListPrioritiesCommand extends AbstractJiraCommand {

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
