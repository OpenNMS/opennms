/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.provision.persist.foreignsource.ForeignSource;
import org.opennms.netmgt.provision.persist.foreignsource.PluginConfig;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.provision.persist.requisition.RequisitionInterface;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.UrlResource;

public class FusedForeignSourceRepositoryTest extends ForeignSourceRepositoryTestCase {
    @Autowired
    @Qualifier("pending")
    private ForeignSourceRepository m_pending;
    
    @Autowired
    @Qualifier("deployed")
    private ForeignSourceRepository m_active;
    
    @Autowired
    @Qualifier("fused")
    private ForeignSourceRepository m_repository;

    @Before
    public void setUp() throws IOException {
        System.err.println("setUp()");
        /* 
         * since we share the filesystem with other tests, best
         * to make sure it's totally clean here.
         */
        for (final ForeignSource fs : m_pending.getForeignSources()) {
            m_pending.delete(fs);
        }
        for (final ForeignSource fs : m_active.getForeignSources()) {
            m_active.delete(fs);
        }
        for (final Requisition r : m_pending.getRequisitions()) {
            m_pending.delete(r);
        }
        for (final Requisition r : m_active.getRequisitions()) {
            m_active.delete(r);
        }
        
        FileUtils.deleteDirectory(new File("target/opennms-home/etc/imports/pending"));

        m_pending.flush();
        m_active.flush();
    }

    @After
    public final void tearDown() {
        /* 
         * since we share the filesystem with other tests, best
         * to make sure it's totally clean here.
         */
        for (ForeignSource fs : m_pending.getForeignSources()) {
            m_pending.delete(fs);
        }
        for (ForeignSource fs : m_active.getForeignSources()) {
            m_active.delete(fs);
        }
        for (Requisition r : m_pending.getRequisitions()) {
            m_pending.delete(r);
        }
        for (Requisition r : m_active.getRequisitions()) {
            m_active.delete(r);
        }
        m_pending.flush();
        m_active.flush();
    }

    @Test
    public void simpleSnapshotTest() throws URISyntaxException {
        Requisition pendingReq = createRequisition();
        m_pending.save(pendingReq);
        m_pending.flush();
        pendingReq = m_pending.getRequisition(pendingReq.getForeignSource());
        final File pendingSnapshot = RequisitionFileUtils.createSnapshot(m_pending, pendingReq.getForeignSource(), pendingReq.getDate());

        m_repository.importResourceRequisition(new FileSystemResource(pendingSnapshot));

        assertFalse(pendingSnapshot.exists());
        final URL pendingUrl = m_pending.getRequisitionURL(pendingReq.getForeignSource());
        assertNull(pendingUrl);
    }

    @Test
    public void multipleSnapshotTest() throws URISyntaxException, InterruptedException {
        Requisition pendingReq = createRequisition();
        m_pending.save(pendingReq);
        m_pending.flush();
        final String foreignSource = pendingReq.getForeignSource();
        pendingReq = m_pending.getRequisition(foreignSource);
        final File pendingSnapshotA = RequisitionFileUtils.createSnapshot(m_pending, foreignSource, pendingReq.getDate());

        // Now, start a new pending update after the original snapshot is "in progress"
        pendingReq.updateDateStamp();
        m_pending.save(pendingReq);
        m_pending.flush();

        final File pendingSnapshotB = RequisitionFileUtils.createSnapshot(m_pending, foreignSource, pendingReq.getDate());

        // "import" the A snapshot
        m_repository.importResourceRequisition(new FileSystemResource(pendingSnapshotA));

        assertFalse(pendingSnapshotA.exists());
        assertTrue(pendingSnapshotB.exists());

        // since there's still a newer snapshot in-progress, the pending test.xml should not have been deleted yet
        URL pendingUrl = m_pending.getRequisitionURL(foreignSource);
        assertNotNull(pendingUrl);
        assertTrue(new File(pendingUrl.toURI()).exists());

        // then, "import" the B snapshot
        final Requisition bReq = m_repository.importResourceRequisition(new FileSystemResource(pendingSnapshotB));
        
        assertFalse(pendingSnapshotA.exists());
        assertFalse(pendingSnapshotB.exists());

        // now the pending test.xml should be gone
        pendingUrl = m_pending.getRequisitionURL(foreignSource);
        assertNull(pendingUrl);
        
        // the last (B) pending import should match the deployed
        final Requisition deployedRequisition = m_active.getRequisition(foreignSource);
        assertEquals(deployedRequisition.getDate().getTime(), bReq.getDate().getTime());
    }

    @Test
    public void integrationTest() {
        /*
         * First, the user creates a requisition in the UI, or RESTful
         * interface.
         */
        Requisition pendingReq = createRequisition();
        m_pending.save(pendingReq);
        m_pending.flush();

        /* 
         * Then, the user makes a foreign source configuration to go along
         * with that requisition.
         */
        ForeignSource pendingSource = m_repository.getForeignSource("test");
        assertTrue(pendingSource.isDefault());
        pendingSource.setDetectors(new ArrayList<PluginConfig>());
        m_pending.save(pendingSource);
        m_pending.flush();

        /*
         * Now we got an import event, so we import that requisition file,
         * and save it.  The ForeignSource in the pending repository should
         * match the one in the active one, now.
         */
        Requisition activeReq = m_repository.importResourceRequisition(new UrlResource(m_pending.getRequisitionURL("test")));
        ForeignSource activeSource = m_active.getForeignSource("test");
        // and the foreign source should be the same as the one we made earlier, only this time it's active
        
        assertEquals(activeSource.getName(), pendingSource.getName());
        assertEquals(activeSource.getDetectorNames(), pendingSource.getDetectorNames());
        assertEquals(activeSource.getScanInterval(), pendingSource.getScanInterval());
        assertRequisitionsMatch("active and pending requisitions should match", activeReq, pendingReq);
        
        /*
         * Since it's been officially deployed, the requisition and foreign
         * source should no longer be in the pending repo.
         */
        assertNull("the requisition should be null in the pending repo", m_pending.getRequisition("test"));
        assertTrue("the foreign source should be default since there's no specific in the pending repo", m_pending.getForeignSource("test").isDefault());
    }

    private Requisition createRequisition() {
        Requisition pendingReq = new Requisition("test");
        RequisitionNode node = new RequisitionNode();
        node.setForeignId("1");
        node.setNodeLabel("node label");
        RequisitionInterface iface = new RequisitionInterface();
        iface.setIpAddr("192.168.0.1");
        node.putInterface(iface);
        pendingReq.putNode(node);
        return pendingReq;
    }
}
