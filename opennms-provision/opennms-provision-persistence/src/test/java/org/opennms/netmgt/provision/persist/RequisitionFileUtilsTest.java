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
package org.opennms.netmgt.provision.persist;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.provision.persist.requisition.Requisition;

public class RequisitionFileUtilsTest {
    private FilesystemForeignSourceRepository m_repository;
    private Path m_requisitionDirectory;
    private String m_importDirectory;
    private String m_foreignSourceDirectory;

    @Before
    public void createTestRepository() throws Exception {
        m_requisitionDirectory = Files.createTempDirectory("RequisitionFileUtilsTest");
        if (m_requisitionDirectory.toFile().exists()) {
            FileUtils.deleteDirectory(m_requisitionDirectory.toFile());
        }


        final FilesystemForeignSourceRepository fsr = new FilesystemForeignSourceRepository();
        m_importDirectory = new File(m_requisitionDirectory.toFile(), "imports").getPath();
        m_foreignSourceDirectory = new File(m_requisitionDirectory.toFile(), "foreign-sources").getPath();

        fsr.setRequisitionPath(m_importDirectory);
        fsr.setForeignSourcePath(m_foreignSourceDirectory);
        fsr.afterPropertiesSet();

        fsr.save(new Requisition("test"));
        m_repository = fsr;
    }

    @After
    public void destroyTestRepository() throws Exception {
        FileUtils.deleteDirectory(m_requisitionDirectory.toFile());
    }

    @Test
    public void testCreateTemporaryRequisition() throws Exception {
        final File file = RequisitionFileUtils.createSnapshot(m_repository, "test", new Date());
        assertNotNull(file);
        assertTrue(file.getPath().contains(m_importDirectory + File.separator + "test"));
        assertTrue(file.getParentFile().getName().equals("imports"));
        assertTrue(file.getName().matches("test\\.xml\\.\\d+"));

        final List<File> snapshots = RequisitionFileUtils.findSnapshots(m_repository, "test");
        assertNotNull(snapshots);
        assertEquals(1, snapshots.size());
        Thread.sleep(1);
        RequisitionFileUtils.createSnapshot(m_repository, "test", new Date());
        assertEquals(2, RequisitionFileUtils.findSnapshots(m_repository, "test").size());

        m_repository.save(new Requisition("test2"));
        RequisitionFileUtils.createSnapshot(m_repository, "test2", new Date());
        assertEquals(1, RequisitionFileUtils.findSnapshots(m_repository, "test2").size());
        assertEquals(2, RequisitionFileUtils.findSnapshots(m_repository, "test").size());
    }

    @Test
    public void testDeleteSnapshots() throws Exception {
        RequisitionFileUtils.createSnapshot(m_repository, "test", new Date());
        Thread.sleep(1);
        RequisitionFileUtils.createSnapshot(m_repository, "test", new Date());
        Thread.sleep(1);
        RequisitionFileUtils.createSnapshot(m_repository, "test", new Date());

        List<File> snapshots = RequisitionFileUtils.findSnapshots(m_repository, "test");
        assertNotNull(snapshots);
        assertEquals(3, snapshots.size());

        RequisitionFileUtils.deleteAllSnapshots(m_repository);

        snapshots = RequisitionFileUtils.findSnapshots(m_repository, "test");
        assertEquals(0, snapshots.size());
    }
}
