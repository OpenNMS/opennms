/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.utils.BeanUtils;
import org.opennms.netmgt.provision.persist.foreignsource.ForeignSource;
import org.opennms.netmgt.provision.persist.foreignsource.PluginConfig;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.provision.persist.requisition.RequisitionInterface;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.UrlResource;
import org.springframework.test.context.ContextConfiguration;


@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/testForeignSourceContext.xml"
})
@JUnitConfigurationEnvironment
public class FusedForeignSourceRepositoryTest implements InitializingBean {
    @Autowired
    @Qualifier("pending")
    private ForeignSourceRepository m_pending;
    
    @Autowired
    @Qualifier("deployed")
    private ForeignSourceRepository m_active;
    
    @Autowired
    @Qualifier("fused")
    private ForeignSourceRepository m_repository;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Before
    public void setUp() {
        MockLogAppender.setupLogging();

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
    }

    @Test
    public void integrationTest() {
        /*
         * First, the user creates a requisition in the UI, or RESTful
         * interface.
         */
        Requisition pendingReq = new Requisition("test");
        RequisitionNode node = new RequisitionNode();
        node.setForeignId("1");
        node.setNodeLabel("node label");
        RequisitionInterface iface = new RequisitionInterface();
        iface.setIpAddr("192.168.0.1");
        node.putInterface(iface);
        pendingReq.putNode(node);
        m_pending.save(pendingReq);

        /* 
         * Then, the user makes a foreign source configuration to go along
         * with that requisition.
         */
        ForeignSource pendingSource = m_repository.getForeignSource("test");
        assertTrue(pendingSource.isDefault());
        pendingSource.setDetectors(new ArrayList<PluginConfig>());
        m_pending.save(pendingSource);

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
        assertEquals("the requisitions should match too", activeReq, pendingReq);
        
        /*
         * Since it's been officially deployed, the requisition and foreign
         * source should no longer be in the pending repo.
         */
        System.err.println("requisition = " + m_pending.getRequisition("test"));
        assertNull("the requisition should be null in the pending repo", m_pending.getRequisition("test"));
        assertTrue("the foreign source should be default since there's no specific in the pending repo", m_pending.getForeignSource("test").isDefault());
    }
}
