package org.opennms.netmgt.config;

import javax.sql.DataSource;

import org.opennms.netmgt.mock.EventAnticipator;
import org.opennms.netmgt.mock.MockEventIpcManager;
import org.opennms.netmgt.mock.OpenNMSTestCase;
import org.opennms.netmgt.mock.OutageAnticipator;
import org.opennms.test.mock.MockLogAppender;

public class DataSourceFactoryTest extends OpenNMSTestCase {

	public DataSource m_testDb = new DataSourceFactory();
    private EventAnticipator m_anticipator;
    private OutageAnticipator m_outageAnticipator;
    private MockEventIpcManager m_eventMgr;
	

	protected void setUp() throws Exception {

		super.setUp();
        MockLogAppender.setupLogging();

        m_eventMgr = new MockEventIpcManager();
        m_eventMgr.setEventWriter(m_db);
        m_eventMgr.setEventAnticipator(m_anticipator);
        m_eventMgr.addEventListener(m_outageAnticipator);
        m_eventMgr.setSynchronous(true);

	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testSecondDatabase() throws Exception {
        DataSourceFactory.getInstance();
        
        DataSourceFactory.setInstance("test2", m_testDb);
        
        m_testDb.setLoginTimeout(5);
        
        assertEquals(5, DataSourceFactory.getInstance("test2").getLoginTimeout());
	}
	
	
}
