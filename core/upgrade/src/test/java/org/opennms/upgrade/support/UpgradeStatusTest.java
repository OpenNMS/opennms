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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class UpgradeStatusTest {
    @Rule
    public TemporaryFolder m_folder = new TemporaryFolder();

    @Test
    public void testWasExecutedById() throws Exception {
        // the installer and the default-users seed ask about a task they do not run themselves
        final File statusFile = m_folder.newFile(UpgradeStatus.STATUS_FILE);
        Files.writeString(statusFile.toPath(), "UsersGroupsXmlMigratorOffline=Tue Oct 14 10\\:00\\:00 UTC 2026\n");
        final UpgradeStatus status = new UpgradeStatus(statusFile);
        assertTrue(status.wasExecuted("UsersGroupsXmlMigratorOffline"));
        assertFalse(status.wasExecuted("DefaultUsersSeedOffline"));
    }

    @Test
    public void testMissingFileMeansNothingWasExecuted() throws Exception {
        assertFalse(new UpgradeStatus(new File(m_folder.getRoot(), UpgradeStatus.STATUS_FILE)).wasExecuted("UsersGroupsXmlMigratorOffline"));
    }
}
