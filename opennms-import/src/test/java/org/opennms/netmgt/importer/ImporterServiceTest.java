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

import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.dao.db.AbstractTransactionalTemporaryDatabaseSpringContextTests;
import org.opennms.netmgt.importer.jmx.ImporterService;
import org.opennms.netmgt.importer.jmx.ImporterServiceMBean;
import org.opennms.netmgt.mock.MockEventIpcManager;
import org.opennms.netmgt.utils.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.test.DaoTestConfigBean;
import org.opennms.test.mock.MockLogAppender;

public class ImporterServiceTest extends AbstractTransactionalTemporaryDatabaseSpringContextTests {
    private MockEventIpcManager m_eventIpcMgr;

    public ImporterServiceTest() {
        DaoTestConfigBean bean = new DaoTestConfigBean();
        bean.setRelativeHomeDirectory("src/test/opennms-home");
        bean.afterPropertiesSet();
    }

    @Override
    protected String[] getConfigLocations() {
        return new String[] {
                "classpath:META-INF/opennms/mockEventIpcManager.xml",
                "classpath:META-INF/opennms/eventIpcManager-factoryInit.xml"
        };
    }

    @Override
    protected void onSetUpInTransactionIfEnabled() throws Exception {
        super.onSetUpInTransactionIfEnabled();
        
        MockLogAppender.setupLogging();
    }

    public void testSchedule() throws Exception {
        anticipateEvent(createEvent(EventConstants.IMPORT_STARTED_UEI));
        anticipateEvent(createEvent(EventConstants.IMPORT_SUCCESSFUL_UEI));
        
        ImporterServiceMBean mbean = new ImporterService();
        mbean.init();
        mbean.start();
        
        Thread.sleep(60000);
        
        mbean.stop();
        
        verifyAnticipated();
    }

    private void verifyAnticipated() {
        m_eventIpcMgr.getEventAnticipator().resetUnanticipated();
        m_eventIpcMgr.getEventAnticipator().verifyAnticipated(0, 0, 0, 0, 0);
    }

    public Event createEvent(String uei) {
        return new EventBuilder(uei, "ModelImporter").getEvent();
    }

    private void anticipateEvent(Event e) {
        m_eventIpcMgr.getEventAnticipator().anticipateEvent(e);
    }

    public void setEventIpcManager(MockEventIpcManager eventIpcMgr) {
        m_eventIpcMgr = eventIpcMgr;
    }
}
