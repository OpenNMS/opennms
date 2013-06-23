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
        RequisitionFileUtils.createSnapshot(m_repository, "test", new Date());
        RequisitionFileUtils.createSnapshot(m_repository, "test", new Date());
        
        List<File> snapshots = RequisitionFileUtils.findSnapshots(m_repository, "test");
        assertNotNull(snapshots);
        assertEquals(3, snapshots.size());

        RequisitionFileUtils.deleteAllSnapshots(m_repository);
        
        snapshots = RequisitionFileUtils.findSnapshots(m_repository, "test");
        assertEquals(0, snapshots.size());
    }
}
