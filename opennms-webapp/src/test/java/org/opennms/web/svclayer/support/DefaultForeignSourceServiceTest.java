package org.opennms.web.svclayer.support;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.provision.persist.FilesystemForeignSourceRepository;


public class DefaultForeignSourceServiceTest {
    private FilesystemForeignSourceRepository m_active;
    private FilesystemForeignSourceRepository m_pending;
    private ForeignSourceService m_service;

    @Before
    public void setUp() {
        FileUtils.deleteQuietly(new File("target/foreign-sources"));
        FileUtils.deleteQuietly(new File("target/imports"));

        m_active  = new FilesystemForeignSourceRepository();
        m_active.setForeignSourcePath("target/foreign-sources/deployed");
        m_active.setRequisitionPath("target/imports/deployed");

        m_pending = new FilesystemForeignSourceRepository();
        m_pending.setForeignSourcePath("target/foreign-sources");
        m_pending.setRequisitionPath("target/imports");
        
        m_service = new DefaultForeignSourceService();
        m_service.setActiveForeignSourceRepository(m_active);
        m_service.setPendingForeignSourceRepository(m_pending);
    }

    @Test
    public void integrationTest() throws Exception {
        assertTrue(m_active.getForeignSources().isEmpty());
        assertTrue(m_pending.getForeignSources().isEmpty());

        assertEquals(0, m_service.getAllForeignSources());
    }
}
