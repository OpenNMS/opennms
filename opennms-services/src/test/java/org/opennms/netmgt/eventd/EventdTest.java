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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.opennms.core.concurrent.BarrierSignaler;
import org.opennms.netmgt.mock.MockInterface;
import org.opennms.netmgt.mock.MockNode;
import org.opennms.netmgt.mock.MockEventUtil;
import org.opennms.netmgt.mock.MockService;
import org.opennms.netmgt.mock.OpenNMSTestCase;
import org.opennms.netmgt.xml.event.AlarmData;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Logmsg;
import org.opennms.test.DaoTestConfigBean;
import org.opennms.test.mock.MockLogAppender;
import org.opennms.test.mock.MockUtil;
import org.springframework.jdbc.core.JdbcTemplate;

public class EventdTest extends OpenNMSTestCase {

    public EventdTest() {
        DaoTestConfigBean daoTestConfig = new DaoTestConfigBean();
        daoTestConfig.setRelativeHomeDirectory("src/test/resources");
        daoTestConfig.afterPropertiesSet();
    }

    @Override
    protected void setUp() throws Exception {
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

    public void testPersistAlarm() throws Exception {

        //there should be no alarms in the alarms table
        assertEquals(0, m_db.countRows("select * from alarms"));

        MockNode node = m_network.getNode(1);
        sendNodeDownEvent(null, node);
        sleep(1000);

        //this should be the first occurrence of this alarm
        //there should be 1 alarm now
        assertEquals(1, m_db.countRows("select * from alarms"));

        //this should be the second occurrence and shouldn't create another row
        //there should still be only 1 alarm
        sendNodeDownEvent(null, node);
        sleep(1000);
        assertEquals(1, m_db.countRows("select * from alarms"));

        //this should be a new alarm because of the new key
        //there should now be 2 alarms
        sendNodeDownEvent("DontReduceThis", node);
        sleep(1000);
        assertEquals(2, m_db.countRows("select * from alarms"));

        MockUtil.println("Going for the print of the counter column");

        //TODO: Change this to use Querier class
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = m_db.getConnection();
            stmt = conn.prepareStatement("select reductionKey, sum(counter) from alarms group by reductionKey");
            ResultSet rs = stmt.executeQuery();

            //should be 2 rows
            while( rs.next()) {
                MockUtil.println("count for reductionKey: "+rs.getString(1)+" is: "+rs.getObject(2));
            }

        } finally {
            stmt.close();
            conn.close();
        }
    }

    public void testPersistManyAlarmsAtOnce() throws InterruptedException {

        int numberOfAlarmsToReduce = 10;
        //there should be no alarms in the alarms table
        assertEquals(0, m_db.countRows("select * from alarms"));

        final String reductionKey = "countThese";
        final MockNode node = m_network.getNode(1);

        final long millis = System.currentTimeMillis()+2500;

        final BarrierSignaler signal = new BarrierSignaler(numberOfAlarmsToReduce);

        for (int i=1; i<= numberOfAlarmsToReduce; i++) {
            MockUtil.println("Creating Runnable: "+i+" of "+numberOfAlarmsToReduce+" events to reduce.");

            class EventRunner implements Runnable {
                public void run() {
                    try {
                        while (System.currentTimeMillis() < millis) {
                            try {
                                Thread.sleep(10);
                            } catch (InterruptedException e) {
                                MockUtil.println(e.getMessage());
                            }
                        }
                        sendNodeDownEvent(reductionKey, node);
                    } finally {
                        signal.signal();
                    }
                }
            }

            Runnable r = new EventRunner();
            Thread p = new Thread(r);
            p.start();
        }

        signal.waitFor();
        sleep(20000);

        //this should be the first occurrence of this alarm
        //there should be 1 alarm now
        int rowCount = m_db.countRows("select * from alarms");
        Integer counterColumn = m_db.getAlarmCount(reductionKey);
        MockUtil.println("rowcCount is: "+rowCount+", expected 1.");
        MockUtil.println("counterColumn is: "+counterColumn+", expected "+numberOfAlarmsToReduce);
        assertEquals(1, rowCount);
        assertEquals(numberOfAlarmsToReduce, counterColumn.intValue());

        Integer alarmId = m_db.getAlarmId(reductionKey);
        rowCount = m_db.countRows("select * from events where alarmid = "+alarmId);
        MockUtil.println(String.valueOf(rowCount) + " of events with alarmid: "+alarmId);
//      assertEquals(numberOfAlarmsToReduce, rowCount);

        rowCount = m_db.countRows("select * from events where alarmid is null");
        MockUtil.println(String.valueOf(rowCount) + " of events with null alarmid");
        assertEquals(0, rowCount);

    }

    /**
     * @param reductionKey
     * @param node
     */
    private void sendNodeDownEvent(String reductionKey, MockNode node) {
        Event e = MockEventUtil.createNodeDownEvent("Test", node);

        if (reductionKey != null) {
            AlarmData data = new AlarmData();
            data.setAlarmType(1);
            data.setReductionKey(reductionKey);
            e.setAlarmData(data);
        } else {
            e.setAlarmData(null);
        }

        Logmsg logmsg = new Logmsg();
        logmsg.setDest("logndisplay");
        logmsg.setContent("testing");
        e.setLogmsg(logmsg);
        
        m_eventdIpcMgr.sendNow(e);
    }

    /**
     * @param reductionKey
     */
    private void sendServiceDownEvent(String reductionKey, MockService svc) {
        Event e = MockEventUtil.createServiceUnresponsiveEvent("Test", svc, "Not responding");

        if (reductionKey != null) {
            AlarmData data = new AlarmData();
            data.setAlarmType(1);
            data.setReductionKey(reductionKey);
            e.setAlarmData(data);
        } else {
            e.setAlarmData(null);
        }

        Logmsg logmsg = new Logmsg();
        logmsg.setDest("logndisplay");
        logmsg.setContent("testing");
        e.setLogmsg(logmsg);
        
        m_eventdIpcMgr.sendNow(e);
    }


}
