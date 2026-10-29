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
package org.opennms.upgrade.support;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** The checks that come before the database: none of these runs reaches the DataSource, which is null. */
public class UsersGroupsImportToolTest {

    @Rule
    public TemporaryFolder m_home = new TemporaryFolder();

    private final ByteArrayOutputStream m_out = new ByteArrayOutputStream();
    private final ByteArrayOutputStream m_err = new ByteArrayOutputStream();
    private File m_usersXml;

    @Before
    public void setUp() throws Exception {
        m_usersXml = m_home.newFile("users.xml");
        Files.writeString(m_usersXml.toPath(), "<userinfo xmlns=\"http://xmlns.opennms.org/xsd/users\"/>");
    }

    @Test
    public void testNoFileIsUsageError() {
        assertEquals(2, run());
        assertTrue(err(), err().contains("Usage: import-users-groups"));
        assertEquals("", out());
    }

    @Test
    public void testUnknownOptionIsUsageError() {
        assertEquals(2, run("--users", m_usersXml.getPath(), "--force"));
        assertTrue(err(), err().contains("--force"));
        assertTrue(err(), err().contains("Usage: import-users-groups"));
    }

    @Test
    public void testOptionWithoutFileIsUsageError() {
        assertEquals(2, run("--users"));
        assertTrue(err(), err().contains("--users"));
    }

    @Test
    public void testMissingFileIsAnError() {
        assertEquals(1, run("--users", "/no/such.xml"));
        assertTrue(err(), err().contains("/no/such.xml"));
        assertTrue(err(), !err().contains("Usage"));
    }

    @Test
    public void testHelpPrintsUsage() {
        assertEquals(0, run("--help"));
        assertTrue(out(), out().contains("Usage: import-users-groups"));
        assertTrue(out(), out().contains("--no-decode"));
        assertEquals("", err());
    }

    @Test
    public void testXmlStoreIsRefused() throws Exception {
        final File etc = m_home.newFolder("etc");
        Files.writeString(new File(etc, "opennms.properties").toPath(), "org.opennms.usermgmt.store=xml\n");

        assertEquals(1, run("--users", m_usersXml.getPath()));
        assertTrue(err(), err().contains("org.opennms.usermgmt.store=xml"));
    }

    private int run(final String... args) {
        final UsersGroupsImportTool tool = new UsersGroupsImportTool(null, m_home.getRoot(), question -> {
            throw new AssertionError("asked " + question);
        }, new PrintStream(m_out, true, StandardCharsets.UTF_8), new PrintStream(m_err, true, StandardCharsets.UTF_8));
        return tool.run(args);
    }

    private String out() {
        return m_out.toString(StandardCharsets.UTF_8);
    }

    private String err() {
        return m_err.toString(StandardCharsets.UTF_8);
    }
}
