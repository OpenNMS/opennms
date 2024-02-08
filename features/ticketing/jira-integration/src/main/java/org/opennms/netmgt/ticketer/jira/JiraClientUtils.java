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
