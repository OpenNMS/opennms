/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: September 24, 2009
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.provision;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.dao.db.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.db.OpenNMSConfigurationExecutionListener;
import org.opennms.netmgt.dao.db.TemporaryDatabaseExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Test the user stories/use cases associated with the Link Adapter.
 * 
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({
    OpenNMSConfigurationExecutionListener.class,
    TemporaryDatabaseExecutionListener.class,
    DependencyInjectionTestExecutionListener.class
})
@ContextConfiguration(locations= {
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/provisiond-extensions.xml"
})
@JUnitTemporaryDatabase()
public class SimplerProvisioningAdapterTest {
    
    public static String NAME = "MyProvisioningAdapter";
    
    CountDownLatch addLatch = new CountDownLatch(1);
    CountDownLatch deleteLatch = new CountDownLatch(1);
    CountDownLatch updateLatch = new CountDownLatch(1);
    CountDownLatch configChangeLatch = new CountDownLatch(1);

    // From applicationContext-dao.xml
    @Autowired
    private TransactionTemplate m_txTemplate;

    class MyProvisioningAdapter extends SimplerQueuedProvisioningAdapter {
        
        public MyProvisioningAdapter() {
            super(NAME);
            setTemplate(m_txTemplate);
        }

        @Override
        public void doAddNode(int nodeid) {
            addLatch.countDown();
        }

        @Override
        public void doDeleteNode(int nodeid) {
            deleteLatch.countDown();
        }

        @Override
        public void doNotifyConfigChange(int nodeid) {
            configChangeLatch.countDown();
        }

        @Override
        public void doUpdateNode(int nodeid) {
            updateLatch.countDown();
        }
        
    }

    private MyProvisioningAdapter m_adapter;

    @Before
    public void setUp() {
        assertNotNull(m_txTemplate);
        m_adapter = new MyProvisioningAdapter();
        m_adapter.init();
    }
    
    @Test
    public void dwoGetName() {
        assertEquals(NAME, m_adapter.getName());
    }

    @Test
    public void dwoAddNodeCallsDoAddNode() throws InterruptedException {

        m_adapter.addNode(1);
        assertTrue(addLatch.await(2, TimeUnit.SECONDS));

    }

    @Test
    public void dwoDeleteNodeCallsDoDeleteNode() throws InterruptedException {

        m_adapter.deleteNode(1);
        assertTrue(deleteLatch.await(2, TimeUnit.SECONDS));

    }

    @Test
    public void dwoUpdateNodeCallsDoUpdateNode() throws InterruptedException {

        m_adapter.updateNode(1);
        assertTrue(updateLatch.await(2, TimeUnit.SECONDS));

    }

    @Test
    public void dwoNotifyConfigChangeCallsDoNotifyConfigChange() throws InterruptedException {

        m_adapter.nodeConfigChanged(1);
        assertTrue(configChangeLatch.await(2, TimeUnit.SECONDS));

    }
}
