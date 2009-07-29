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

import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.dao.TransactionAwareEventForwarder;
import org.opennms.netmgt.dao.db.AbstractTransactionalTemporaryDatabaseSpringContextTests;
import org.opennms.netmgt.mock.EventAnticipator;
import org.opennms.netmgt.mock.MockEventIpcManager;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.test.DaoTestConfigBean;

/**
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class TransactionAwareEventForwarderTest extends AbstractTransactionalTemporaryDatabaseSpringContextTests {
    
    private TransactionAwareEventForwarder m_proxy;
    private int m_eventNumber = 1;
    private MockEventIpcManager m_eventIpcManager;
    
    @Override
    protected void setUpConfiguration() throws Exception {
        DaoTestConfigBean daoTestConfig = new DaoTestConfigBean();
        daoTestConfig.afterPropertiesSet();
    }

    @Override
    protected String[] getConfigLocations() {
        return new String[] {
          "META-INF/opennms/applicationContext-dao.xml",
          "classpath*:/META-INF/opennms/component-dao.xml",
          "META-INF/opennms/applicationContext-daemon.xml",
          "org/opennms/netmgt/utils/applicationContext-testTAEventForwarderTest.xml",
          "META-INF/opennms/mockEventIpcManager.xml",
          "META-INF/opennms/smallEventConfDao.xml"
        };
    }
    
    public void setTransactionAwareEventForwarder(TransactionAwareEventForwarder proxy) {
        m_proxy = proxy;
    }
    
    public void testSendEventsOnCommit() {
        
        sendEventAndCommit();
        
        getEventAnticipator().verifyAnticipated(1000, 0, 0, 0, 0);
    }
    
    public void testSendEventsOnRollback() {
        
        sendEventAndRollback();
        
        // we expect no events
        getEventAnticipator().verifyAnticipated(1000, 0, 0, 0, 0);
    }
    

    public void testTwoTransactions() {
        sendEventAndCommit();
        
        this.startNewTransaction();

        sendEventAndCommit();
        
        getEventAnticipator().verifyAnticipated(1000, 0, 0, 0, 0);

    }

    public void testCommitRollbackCommit() {
        
        sendEventAndCommit();
        
        this.startNewTransaction();
        
        sendEventAndRollback();
        
        this.startNewTransaction();
        
        sendEventAndCommit();
        
        getEventAnticipator().verifyAnticipated(1000, 0, 0, 0, 0);
        
    }

    private void sendEventAndCommit() {
        sendEventAndEndTransaction(true);
    }
    
    private void sendEventAndRollback() {
        sendEventAndEndTransaction(false);
    }

    private void sendEventAndEndTransaction(boolean complete) {
        
        Event event = new EventBuilder(EventConstants.ADD_INTERFACE_EVENT_UEI, "Test")
                        .setNodeid(m_eventNumber++)
                        .getEvent();
        
        if (complete) {
            getEventAnticipator().anticipateEvent(event);
        }
        
        m_proxy.sendNow(event);
        
        if (complete) {
            this.setComplete();
        }
        this.endTransaction();
    }
    
    private EventAnticipator getEventAnticipator() {
        return m_eventIpcManager.getEventAnticipator();
    }

    public MockEventIpcManager getEventIpcManager() {
        return m_eventIpcManager;
    }

    public void setEventIpcManager(MockEventIpcManager eventIpcManager) {
        m_eventIpcManager = eventIpcManager;
    }


}
