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
package org.opennms.core.password;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opennms.netmgt.config.api.UserMgmtStore;

public class UserMgmtStoreSettingTest {
    @Rule
    public TemporaryFolder m_home = new TemporaryFolder();

    @After
    public void tearDown() {
        System.clearProperty(UserMgmtStore.PROPERTY);
    }

    @Test
    public void testPropertiesDotDWinsOverOpennmsProperties() throws Exception {
        final File etc = m_home.newFolder("etc");
        Files.writeString(etc.toPath().resolve("opennms.properties"), UserMgmtStore.PROPERTY + "=db\n");
        final File d = new File(etc, "opennms.properties.d");
        assertTrue(d.mkdir());
        Files.writeString(d.toPath().resolve("usermgmt.properties"), UserMgmtStore.PROPERTY + "=xml\n");
        assertFalse(UserMgmtStoreSetting.isDatabase(m_home.getRoot()));
    }

    @Test
    public void testWithoutFilesTheDatabaseIsUsed() {
        assertTrue(UserMgmtStoreSetting.isDatabase(m_home.getRoot()));
    }

    @Test
    public void testSystemPropertyWinsOverFiles() throws Exception {
        final File etc = m_home.newFolder("etc");
        Files.writeString(etc.toPath().resolve("opennms.properties"), UserMgmtStore.PROPERTY + "=db\n");
        System.setProperty(UserMgmtStore.PROPERTY, "xml");
        assertFalse(UserMgmtStoreSetting.isDatabase(m_home.getRoot()));
    }
}
