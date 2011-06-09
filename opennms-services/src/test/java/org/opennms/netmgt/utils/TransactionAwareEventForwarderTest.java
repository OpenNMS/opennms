/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007-2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: October 23, 2007
 *
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
package org.opennms.netmgt.utils;

import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.dao.TransactionAwareEventForwarder;
import org.opennms.netmgt.dao.db.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.db.OpenNMSConfigurationExecutionListener;
import org.opennms.netmgt.dao.db.TemporaryDatabase;
import org.opennms.netmgt.dao.db.TemporaryDatabaseExecutionListener;
import org.opennms.netmgt.mock.EventAnticipator;
import org.opennms.netmgt.mock.MockEventIpcManager;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({
    OpenNMSConfigurationExecutionListener.class,
    TemporaryDatabaseExecutionListener.class,
    DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class
})
@ContextConfiguration(locations={
        "classpath:META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:META-INF/opennms/applicationContext-daemon.xml",
        "classpath:org/opennms/netmgt/utils/applicationContext-testTAEventForwarderTest.xml",
        "classpath:META-INF/opennms/mockEventIpcManager.xml",
        "classpath:META-INF/opennms/smallEventConfDao.xml"
})
@JUnitTemporaryDatabase(tempDbClass=TemporaryDatabase.class)
public class TransactionAwareEventForwarderTest implements InitializingBean {

    @Autowired
    private TransactionAwareEventForwarder m_proxy;

    private int m_eventNumber = 1;

    @Autowired
    private MockEventIpcManager m_eventIpcManager;

    @Autowired
    TransactionTemplate m_transTemplate;

    @Override
    public void afterPropertiesSet() throws Exception {
        assertNotNull(m_proxy);
        assertNotNull(m_transTemplate);
    }
    
    @After
    public void verifyAnticipated() {
        getEventAnticipator().verifyAnticipated(1000, 0, 0, 0, 0);
    }

    @Test
    public void testSendEventsOnCommit() {
        m_transTemplate.execute(new TransactionCallback<Object>() {
            public Object doInTransaction(TransactionStatus status) {
                Event event = sendEvent();
                getEventAnticipator().anticipateEvent(event);
                event = sendEvent();
                getEventAnticipator().anticipateEvent(event);
                event = sendEvent();
                getEventAnticipator().anticipateEvent(event);
                event = sendEvent();
                getEventAnticipator().anticipateEvent(event);
                return null;
            }
        });
    }

    @Test
    public void testSendEventsOnRollback() {
        m_transTemplate.execute(new TransactionCallback<Object>() {
            public Object doInTransaction(TransactionStatus status) {
                sendEvent();
                status.setRollbackOnly();
                return null;
            }
        });
    }


    @Test
    public void testTwoTransactions() {
        m_transTemplate.execute(new TransactionCallback<Object>() {
            public Object doInTransaction(TransactionStatus status) {
                Event event = sendEvent();
                getEventAnticipator().anticipateEvent(event);
                return null;
            }
        });

        m_transTemplate.execute(new TransactionCallback<Object>() {
            public Object doInTransaction(TransactionStatus status) {
                Event event = sendEvent();
                getEventAnticipator().anticipateEvent(event);
                return null;
            }
        });
    }

    @Test
    public void testCommitRollbackCommit() {
        m_transTemplate.execute(new TransactionCallback<Object>() {
            public Object doInTransaction(TransactionStatus status) {
                Event event = sendEvent();
                getEventAnticipator().anticipateEvent(event);
                return null;
            }
        });

        m_transTemplate.execute(new TransactionCallback<Object>() {
            public Object doInTransaction(TransactionStatus status) {
                // Doesn't matter how many events we send here, they're
                // all gonna get rolled back
                sendEvent();
                sendEvent();
                sendEvent();
                sendEvent();
                status.setRollbackOnly();
                return null;
            }
        });

        m_transTemplate.execute(new TransactionCallback<Object>() {
            public Object doInTransaction(TransactionStatus status) {
                Event event = sendEvent();
                getEventAnticipator().anticipateEvent(event);
                return null;
            }
        });
    }

    private Event sendEvent() {
        Event event = new EventBuilder(EventConstants.ADD_INTERFACE_EVENT_UEI, "Test")
        .setNodeid(m_eventNumber++)
        .getEvent();

        m_proxy.sendNow(event);

        return event;
    }

    private EventAnticipator getEventAnticipator() {
        return m_eventIpcManager.getEventAnticipator();
    }

}
