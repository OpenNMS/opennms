package org.opennms.netmgt.utils;

import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.dao.db.AbstractTransactionalTemporaryDatabaseSpringContextTests;
import org.opennms.netmgt.mock.EventAnticipator;
import org.opennms.netmgt.mock.MockEventIpcManager;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.test.DaoTestConfigBean;

public class TransactionAwareEventIpcManagerProxyTest extends AbstractTransactionalTemporaryDatabaseSpringContextTests {
    
    private TransactionAwareEventIpcManagerProxy m_proxy;
    private int m_eventNumber = 1;
    private MockEventIpcManager m_eventIpcManager;
    
    public TransactionAwareEventIpcManagerProxyTest() {
        super();
        
        DaoTestConfigBean bean = new DaoTestConfigBean();
        bean.afterPropertiesSet();
    }

    @Override
    protected String[] getConfigLocations() {
        return new String[] {
          "META-INF/opennms/applicationContext-dao.xml",
          "META-INF/opennms/applicationContext-daemon.xml",
          "org/opennms/netmgt/utils/applicationContext-testTAEventIpcMgrTest.xml",
          "META-INF/opennms/mockEventIpcManager.xml",
          "META-INF/opennms/smallEventConfDao.xml"
        };
    }
    
    public void setTransactionAwareEventIpcManagerProxy(TransactionAwareEventIpcManagerProxy proxy) {
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
