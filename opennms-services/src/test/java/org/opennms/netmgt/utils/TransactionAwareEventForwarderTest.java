/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.utils;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.TransactionAwareEventForwarder;
import org.opennms.netmgt.dao.mock.EventAnticipator;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/org/opennms/netmgt/utils/applicationContext-testTAEventForwarderTest.xml",
        "classpath:META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
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
        BeanUtils.assertAutowiring(this);
    }

    @After
    public void verifyAnticipated() {
        getEventAnticipator().verifyAnticipated(1000, 0, 0, 0, 0);
    }

    @Test
    public void testSendEventsOnCommit() {
        m_transTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            public void doInTransactionWithoutResult(TransactionStatus status) {
                Event event = sendEvent();
                getEventAnticipator().anticipateEvent(event);
                event = sendEvent();
                getEventAnticipator().anticipateEvent(event);
                event = sendEvent();
                getEventAnticipator().anticipateEvent(event);
                event = sendEvent();
                getEventAnticipator().anticipateEvent(event);
            }
        });
    }

    @Test
    public void testSendEventsOnRollback() {
        m_transTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            public void doInTransactionWithoutResult(TransactionStatus status) {
                sendEvent();
                status.setRollbackOnly();
            }
        });
    }


    @Test
    public void testTwoTransactions() {
        m_transTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            public void doInTransactionWithoutResult(TransactionStatus status) {
                Event event = sendEvent();
                getEventAnticipator().anticipateEvent(event);
            }
        });

        m_transTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            public void doInTransactionWithoutResult(TransactionStatus status) {
                Event event = sendEvent();
                getEventAnticipator().anticipateEvent(event);
            }
        });
    }

    @Test
    public void testCommitRollbackCommit() {
        m_transTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            public void doInTransactionWithoutResult(TransactionStatus status) {
                Event event = sendEvent();
                getEventAnticipator().anticipateEvent(event);
            }
        });

        m_transTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            public void doInTransactionWithoutResult(TransactionStatus status) {
                // Doesn't matter how many events we send here, they're
                // all gonna get rolled back
                sendEvent();
                sendEvent();
                sendEvent();
                sendEvent();
                status.setRollbackOnly();
            }
        });

        m_transTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            public void doInTransactionWithoutResult(TransactionStatus status) {
                Event event = sendEvent();
                getEventAnticipator().anticipateEvent(event);
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
