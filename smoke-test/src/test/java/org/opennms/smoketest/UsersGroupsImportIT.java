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
package org.opennms.smoketest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.opennms.smoketest.containers.OpenNMSContainer;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.testcontainers.containers.Container.ExecResult;
import org.testcontainers.utility.MountableFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Runs bin/import-users-groups inside the container, while OpenNMS runs, with a users.xml and a
 * groups.xml: a dry run writes nothing, the import creates the user and the group, and an overwrite
 * that nobody can confirm stops.
 */
public class UsersGroupsImportIT {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final String USER = "importme";
    private static final String GROUP = "ImportGroup";
    private static final String USERS_FILE = "/tmp/users.xml";
    private static final String GROUPS_FILE = "/tmp/groups.xml";
    // one JAX-RS client for every request of the run, closed when the class is done
    private static final Client CLIENT = ClientBuilder.newClient();

    @ClassRule
    public static final OpenNMSStack stack = OpenNMSStack.MINIMAL;

    // the fixture names are unique to this test; the entries are removed before and after, so the
    // test can run again on a stack where it ran already
    @Before
    @After
    public void deleteFixtureEntries() {
        // the group first: the user is a member of it
        request("groups/" + GROUP).delete().close();
        request("users/" + USER).delete().close();
    }

    @Test
    public void importsUsersAndGroupsWhileOpenNMSRuns() throws Exception {
        final OpenNMSContainer opennms = stack.opennms();
        opennms.copyFileToContainer(MountableFile.forClasspathResource("usersgroups-import/users.xml"), USERS_FILE);
        opennms.copyFileToContainer(MountableFile.forClasspathResource("usersgroups-import/groups.xml"), GROUPS_FILE);

        // a dry run reports the existing admin and writes nothing
        final ExecResult dryRun = importUsersGroups("--dry-run", "--users", USERS_FILE, "--groups", GROUPS_FILE);
        assertEquals(dryRun.getStdout() + dryRun.getStderr(), 0, dryRun.getExitCode());
        assertThat(dryRun.getStdout(), containsString("Existing entries that would be skipped:"));
        assertThat(dryRun.getStdout(), matchesPattern("(?s).*\\n {2}users: [^\\n]*\\badmin\\b.*"));
        assertThat(dryRun.getStdout(), containsString("Dry run: rolled back, nothing was written."));
        assertEquals(404, request("users/" + USER).get().getStatus());
        assertEquals(404, request("groups/" + GROUP).get().getStatus());

        // the import creates the user and the group, with its members, and skips the existing admin
        final ExecResult imported = importUsersGroups("--users", USERS_FILE, "--groups", GROUPS_FILE);
        assertEquals(imported.getStdout() + imported.getStderr(), 0, imported.getExitCode());
        assertThat(imported.getStdout(), containsString("The import was committed."));
        final Response user = request("users/" + USER).get();
        assertEquals(200, user.getStatus());
        assertEquals(USER, JSON.readTree(user.readEntity(String.class)).get("userId").asText());
        final Response group = request("groups/" + GROUP).get();
        assertEquals(200, group.getStatus());
        final List<String> members = new ArrayList<>();
        JSON.readTree(group.readEntity(String.class)).get("users").forEach((JsonNode member) -> members.add(member.asText()));
        assertThat(members, hasItems(USER, "admin"));

        // the overwrite of the entries that exist now needs a confirmation, and execInContainer has no console
        final ExecResult overwrite = importUsersGroups("--overwrite", "--users", USERS_FILE, "--groups", GROUPS_FILE);
        assertEquals(overwrite.getStdout() + overwrite.getStderr(), 3, overwrite.getExitCode());
        assertThat(overwrite.getStderr(), containsString("There is no console to confirm the overwrite"));
    }

    private ExecResult importUsersGroups(final String... arguments) throws Exception {
        // the script runs as RUNAS (default opennms, or what etc/opennms.conf sets) and refuses to run as
        // another user, which is what execInContainer may be; run it as the exec user, whoever that is
        final StringBuilder command = new StringBuilder("RUNAS=$(id -un) /opt/opennms/bin/import-users-groups");
        for (final String argument : arguments) {
            command.append(' ').append(argument);
        }
        return stack.opennms().execInContainer("sh", "-c", command.toString());
    }

    @AfterClass
    public static void closeClient() {
        CLIENT.close();
    }

    private static Invocation.Builder request(final String path) {
        final String auth = Base64.getEncoder().encodeToString((OpenNMSContainer.ADMIN_USER + ":" + OpenNMSContainer.ADMIN_PASSWORD).getBytes());
        return CLIENT.target(stack.opennms().getBaseUrlExternal() + "opennms/api/v2/" + path)
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Basic " + auth);
    }
}
