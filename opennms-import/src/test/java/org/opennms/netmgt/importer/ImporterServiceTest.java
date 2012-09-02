/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.importer;

import static org.junit.Assert.assertTrue;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.utils.BeanUtils;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.eventd.mock.MockEventIpcManager;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-importer.xml",
        "classpath:/META-INF/opennms/smallEventConfDao.xml",
        "classpath:/importerServiceTest.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class ImporterServiceTest implements InitializingBean {
    @Autowired
    private MockEventIpcManager m_eventIpcMgr;
    @Autowired
    private ImporterService m_daemon;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
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
