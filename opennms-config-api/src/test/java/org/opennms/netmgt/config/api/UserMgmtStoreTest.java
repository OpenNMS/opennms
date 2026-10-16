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
package org.opennms.netmgt.config.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.util.Properties;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class UserMgmtStoreTest {
    @Rule
    public TemporaryFolder m_home = new TemporaryFolder();

    @After
    public void tearDown() {
        System.clearProperty(UserMgmtStore.PROPERTY);
    }

    @Test
    public void testDefaultIsDatabase() {
        assertTrue(UserMgmtStore.isDatabase(new Properties()));
    }

    @Test
    public void testXml() {
        final Properties p = new Properties();
        p.setProperty(UserMgmtStore.PROPERTY, " xml ");
        assertFalse(UserMgmtStore.isDatabase(p));
    }

    @Test
    public void testUnknownValueFails() {
        final Properties p = new Properties();
        p.setProperty(UserMgmtStore.PROPERTY, "ldap");
        final IllegalStateException e = assertThrows(IllegalStateException.class, () -> UserMgmtStore.isDatabase(p));
        assertEquals("org.opennms.usermgmt.store must be \"db\" or \"xml\", not \"ldap\"", e.getMessage());
    }

    @Test
    public void testHomeReadsPropertiesDotDAfterOpennmsProperties() throws Exception {
        final File etc = m_home.newFolder("etc");
        Files.writeString(etc.toPath().resolve("opennms.properties"), UserMgmtStore.PROPERTY + "=db\n");
        final File d = new File(etc, "opennms.properties.d");
        assertTrue(d.mkdir());
        Files.writeString(d.toPath().resolve("usermgmt.properties"), UserMgmtStore.PROPERTY + "=xml\n");
        assertFalse(UserMgmtStore.isDatabase(m_home.getRoot()));
    }

    @Test
    public void testHomeWithoutFilesIsDatabase() {
        assertTrue(UserMgmtStore.isDatabase(m_home.getRoot()));
    }

    @Test
    public void testSystemPropertyWinsOverFiles() throws Exception {
        final File etc = m_home.newFolder("etc");
        Files.writeString(etc.toPath().resolve("opennms.properties"), UserMgmtStore.PROPERTY + "=db\n");
        System.setProperty(UserMgmtStore.PROPERTY, "xml");
        assertFalse(UserMgmtStore.isDatabase(m_home.getRoot()));
    }
}
