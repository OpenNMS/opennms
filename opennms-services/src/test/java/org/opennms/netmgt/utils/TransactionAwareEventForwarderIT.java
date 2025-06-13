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
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-pinger.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/org/opennms/netmgt/utils/applicationContext-testTAEventForwarderTest.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class TransactionAwareEventForwarderIT implements InitializingBean {

    @Autowired
    private TransactionAwareEventForwarder m_proxy;

    private int m_eventNumber = 1;

    @Autowired
    private MockEventIpcManager m_eventIpcManager;

    @Autowired
    TransactionTemplate m_transTemplate;

    @Override
    public void afterPropertiesSet() throws Exception {
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
        Event event = new EventBuilder(EventConstants.ADD_NODE_EVENT_UEI, "Test")
        .setNodeid(m_eventNumber++)
        .getEvent();

        m_proxy.sendNow(event);

        return event;
    }

    private EventAnticipator getEventAnticipator() {
        return m_eventIpcManager.getEventAnticipator();
    }

}
