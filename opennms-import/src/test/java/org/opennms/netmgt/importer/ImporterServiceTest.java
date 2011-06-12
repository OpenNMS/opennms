//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2008 Feb 10: Created a separate test case for JMX tests using default configs. - dj@opennms.org
// 2007 Aug 25: Use AbstractTransactionalTemporaryDatabaseSpringContextTests
//              and new Spring context files. - dj@opennms.org
// 2007 Jun 24: Organize imports. - dj@opennms.org
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.importer;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.dao.db.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.db.OpenNMSConfigurationExecutionListener;
import org.opennms.netmgt.dao.db.TemporaryDatabase;
import org.opennms.netmgt.dao.db.TemporaryDatabaseExecutionListener;
import org.opennms.netmgt.mock.MockEventIpcManager;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.test.mock.MockLogAppender;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({
    OpenNMSConfigurationExecutionListener.class,
    TemporaryDatabaseExecutionListener.class,
    DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class
})
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-importer.xml",
        "classpath:/META-INF/opennms/smallEventConfDao.xml",
        "classpath:/importerServiceTest.xml"
})
@JUnitTemporaryDatabase(tempDbClass=TemporaryDatabase.class)
public class ImporterServiceTest implements InitializingBean {
    @Autowired
    private MockEventIpcManager m_eventIpcMgr;
    @Autowired
    private ImporterService m_daemon;

    public void afterPropertiesSet() throws Exception {
        assertNotNull(m_eventIpcMgr);
        assertNotNull(m_daemon);
    }

    @Before
    public void onSetUpInTransactionIfEnabled() throws Exception {
        Properties logConfig = new Properties();
        
        logConfig.put("log4j.logger.org.exolab.castor", "INFO");
        logConfig.put("log4j.logger.org.hibernate", "INFO");
        logConfig.put("log4j.logger.org.hibernate.SQL", "DEBUG");
        logConfig.put("log4j.logger.org.springframework", "INFO");

        MockLogAppender.setupLogging(logConfig);
    }

    @Test
    public void testSchedule() throws Exception {
        expectImportStarted();
        
        m_daemon.start();

        // we wait a while here because the start up time could be long
        // this will end as soon as the event is received so no harm in waiting
        waitForImportStarted(300000);

        expectImportSuccessful();

        m_daemon.destroy();
        
        // this will end as soon as the event is received so no harm in waiting
        waitForImportSuccessful(300000);
    }

    private void expectImportSuccessful() {
        anticipateEvent(createEvent(EventConstants.IMPORT_SUCCESSFUL_UEI));
    }

    private void expectImportStarted() {
        anticipateEvent(createEvent(EventConstants.IMPORT_STARTED_UEI));
    }
    
    private void waitForImportStarted(long timeout) {
        assertTrue("Failed to receive importStarted event after waiting "+timeout+" millis", m_eventIpcMgr.getEventAnticipator().waitForAnticipated(timeout).size() == 0);
    }

    private void waitForImportSuccessful(long timeout) {
        assertTrue("Failed to receive importSuccessful event after waiting "+timeout+" millis", m_eventIpcMgr.getEventAnticipator().waitForAnticipated(timeout).size() == 0);
    }

    public Event createEvent(String uei) {
        return new EventBuilder(uei, "ModelImporter").getEvent();
    }

    private void anticipateEvent(Event e) {
        anticipateEvent(e, false);
    }
    private void anticipateEvent(Event e, boolean checkUnanticipatedList) {
        m_eventIpcMgr.getEventAnticipator().anticipateEvent(e, checkUnanticipatedList);
    }
}
