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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.atlassian.jira.rest.client.api.GetCreateIssueMetadataOptions;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.CimFieldInfo;
import com.atlassian.jira.rest.client.api.domain.CimProject;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

/**
 * Helper Class.
 *
 * @author mvrueden
 */
public class JiraClientUtils {

    /**
     * Convenient method to get the meta data to create issues.
     * It allows queriing by issueTypeName and projectKey.
     *
     * @param client the {@link JiraRestClient} to make the ReST call.
     * @param expandos The expandos, to expand the json object if required
     * @param issueTypeName Filter by issue type name (e.g. Bug). May be null.
     * @param projectKey Filter by project key (e.g. DUM). May be null.
     * @return The issue meta data for the queried projects.
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public static List<CimProject> getIssueMetaData(JiraRestClient client, String expandos, String issueTypeName, String projectKey) throws ExecutionException, InterruptedException {
        return Lists.newArrayList(client.getIssueClient().getCreateIssueMetadata(
                new GetCreateIssueMetadataOptions(
                        expandos == null ? null : Lists.newArrayList(expandos),
                        issueTypeName == null ? null : Lists.newArrayList(issueTypeName),
                        null, // no issue type ids
                        projectKey == null ? null : Lists.newArrayList(projectKey),
                        null)) // no project ids
                .get());
    }

    /**
     * Does the same as {@link #getIssueMetaData(JiraRestClient, String, String, String)} but filters by issueTypeId instead of issueTypeName.
     *
     * @see #getIssueMetaData(JiraRestClient, String, String, String)
     */
    public static List<CimProject> getIssueMetaData(JiraRestClient client, String expandos, Long issueTypeId, String projectKey) throws ExecutionException, InterruptedException {
        return Lists.newArrayList(client.getIssueClient().getCreateIssueMetadata(
                new GetCreateIssueMetadataOptions(
                        expandos == null ? null : Lists.newArrayList(expandos),
                        null, // no issue type names
                        issueTypeId == null ? null : Lists.newArrayList(issueTypeId),
                        projectKey == null ? null : Lists.newArrayList(projectKey),
                        null)) // no project ids
                .get());
    }

    /**
     * Convenient method to extract all fields from the projects list.
     * This method expects that there is only one project in the projects list.
     *
     * @param cimProjects The projects list. May be null or empty. May not contain more than 1 element.
     * @return The fields if there are any, otherwise an empty list.
     */
    public static Collection<CimFieldInfo> getFields(List<CimProject> cimProjects) {
        if (cimProjects != null
                && !cimProjects.isEmpty()
                && cimProjects.get(0).getIssueTypes().iterator().hasNext()) {
            Map<String, CimFieldInfo> fields = cimProjects.get(0).getIssueTypes().iterator().next().getFields();
            if (fields != null) {
                return fields.values();
            }
        }
        return Lists.newArrayList();
    }

    /**
     * Convenient method to get all issue types of a project.
     *
     * @param client The {@link JiraRestClient}
     * @param projectKey The project key. Is null or empty, all issue types are returned, otherwise issue types for the given projectKey are returned.
     * @return All issue types or issue types for the given projectKey.
     *
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public static Iterable<? extends IssueType> getIssueTypes(JiraRestClient client, String projectKey) throws ExecutionException, InterruptedException {
        if (Strings.isNullOrEmpty(projectKey)) {
            return client.getMetadataClient().getIssueTypes().get();
        }
        final Iterable<CimProject> cimProjects = getIssueMetaData(client, null, (String) null, projectKey);
        if (cimProjects.iterator().hasNext()) {
            return cimProjects.iterator().next().getIssueTypes();
        }
        return Lists.newArrayList();
    }
}
