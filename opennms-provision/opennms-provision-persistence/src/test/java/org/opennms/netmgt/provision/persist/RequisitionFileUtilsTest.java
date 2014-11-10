/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.persist;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.provision.persist.requisition.Requisition;

public class RequisitionFileUtilsTest {
    private FilesystemForeignSourceRepository m_repository;
    
    @Before
    public void createTestRepository() throws Exception {
        final File requisitionDirectory = new File("target/RequisitionFileUtilsTest");
        if (requisitionDirectory.exists()) {
            FileUtils.deleteDirectory(requisitionDirectory);
        }
        
        final FilesystemForeignSourceRepository fsr = new FilesystemForeignSourceRepository();
        fsr.setForeignSourcePath("target/RequisitionFileUtilsTest/foreign-sources");
        fsr.setRequisitionPath("target/RequisitionFileUtilsTest/imports");
        fsr.afterPropertiesSet();
        
        fsr.save(new Requisition("test"));
        m_repository = fsr;
    }
    
    @Test
    public void testCreateTemporaryRequisition() throws Exception {
        final File file = RequisitionFileUtils.createSnapshot(m_repository, "test", new Date());
        assertNotNull(file);
        assertTrue(file.getPath().contains("target/RequisitionFileUtilsTest/imports/test"));
        assertTrue(file.getPath().matches(".*target/RequisitionFileUtilsTest/imports/test.xml.\\d+"));
        
        final List<File> snapshots = RequisitionFileUtils.findSnapshots(m_repository, "test");
        assertNotNull(snapshots);
        assertEquals(1, snapshots.size());
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
