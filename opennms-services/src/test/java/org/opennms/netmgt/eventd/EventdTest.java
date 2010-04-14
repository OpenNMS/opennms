//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2008 Jan 27: Move alarm-specific tests to JdbcAlarmWriterTest. - dj@opennms.org
// 2008 Jan 26: Don't call methods directly on Eventd to send events
//              (they are moving, anyway)--use EventIpcManager.
// 2008 Jan 26: Add some @Override annotations, kill main method. - dj@opennms.org
// 2008 Jan 23: Add test for mapping from servicename to serviceId and
//              persistence of events.serviceID. - dj@opennms.org
// 2008 Jan 08: Make tests happy with EventConfigurationManager to
//              EventConfDao rework. - dj@opennms.org
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
//   OpenNMS Licensing       <license@opennms.org>
//   http://www.opennms.org/
//   http://www.opennms.com/
//
package org.opennms.netmgt.eventd;

import org.opennms.netmgt.mock.MockEventUtil;
import org.opennms.netmgt.mock.MockInterface;
import org.opennms.netmgt.mock.MockNode;
import org.opennms.netmgt.mock.MockService;
import org.opennms.netmgt.mock.OpenNMSTestCase;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.AlarmData;
import org.opennms.test.DaoTestConfigBean;
import org.opennms.test.mock.MockLogAppender;
import org.springframework.jdbc.core.JdbcTemplate;

public class EventdTest extends OpenNMSTestCase {

    @Override
    protected void setUp() throws Exception {
        DaoTestConfigBean daoTestConfig = new DaoTestConfigBean();
        daoTestConfig.setRelativeHomeDirectory("src/test/resources");
        daoTestConfig.afterPropertiesSet();

        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        m_eventd.stop();
        MockLogAppender.assertNoWarningsOrGreater();
    }

    public void testPersistEvent() throws Exception {

        assertEquals(0, m_db.countRows("select * from events"));

        MockNode node = m_network.getNode(1);
        sendNodeDownEvent(null, node);
        sleep(1000);
        assertEquals(1, m_db.countRows("select * from events"));

    }

    /**
     * Test that eventd's service ID lookup works properly.
     */
    public void testPersistEventWithService() throws Exception {

        assertEquals(0, m_db.countRows("select * from events"));

        MockNode node = m_network.getNode(1);
        MockInterface intf = node.getInterface("192.168.1.1");
        MockService svc = intf.getService("ICMP");
        sendServiceDownEvent(null, svc);

        sleep(1000);
        assertEquals("event count", 1, m_db.countRows("select * from events"));
        assertNotSame("service ID for event", 0, new JdbcTemplate(m_db.getDataSource()).queryForInt("select serviceID from events"));
    }


    /**
     * @param reductionKey
     * @param node
     */
    private void sendNodeDownEvent(String reductionKey, MockNode node) {
        EventBuilder e = MockEventUtil.createNodeDownEventBuilder("Test", node);

        if (reductionKey != null) {
            AlarmData data = new AlarmData();
            data.setAlarmType(1);
            data.setReductionKey(reductionKey);
            e.setAlarmData(data);
        } else {
            e.setAlarmData(null);
        }

        e.setLogDest("logndisplay");
        e.setLogMessage("testing");
        
        m_eventdIpcMgr.sendNow(e.getEvent());
    }

    /**
     * @param reductionKey
     */
    private void sendServiceDownEvent(String reductionKey, MockService svc) {
        EventBuilder e = MockEventUtil.createServiceUnresponsiveEventBuilder("Test", svc, "Not responding");

        if (reductionKey != null) {
            AlarmData data = new AlarmData();
            data.setAlarmType(1);
            data.setReductionKey(reductionKey);
            e.setAlarmData(data);
        } else {
            e.setAlarmData(null);
        }

        e.setLogDest("logndisplay");
        e.setLogMessage("testing");
        
        m_eventdIpcMgr.sendNow(e.getEvent());
    }


}
