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
// 2008 Jan 27: Test checks for event validity. - dj@opennms.org
// 2008 Jan 27: Move alarm-specific tests to JdbcAlarmWriterTest. - dj@opennms.org
// 2008 Jan 26: Change to use dependency injection for EventWriter and refactor
//              quite a bit. - dj@opennms.org
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
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.netmgt.eventd.processor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.concurrent.BarrierSignaler;
import org.opennms.netmgt.alarmd.AlarmPersisterImpl;
import org.opennms.netmgt.alarmd.Alarmd;
import org.opennms.netmgt.dao.AlarmDao;
import org.opennms.netmgt.dao.EventDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.db.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.db.OpenNMSConfigurationExecutionListener;
import org.opennms.netmgt.dao.db.TemporaryDatabaseAware;
import org.opennms.netmgt.dao.db.TemporaryDatabaseExecutionListener;
import org.opennms.netmgt.mock.MockDatabase;
import org.opennms.netmgt.mock.MockEventIpcManager;
import org.opennms.netmgt.mock.MockEventUtil;
import org.opennms.netmgt.mock.MockNetwork;
import org.opennms.netmgt.mock.MockNode;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.xml.event.AlarmData;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Logmsg;
import org.opennms.test.ThrowableAnticipator;
import org.opennms.test.mock.MockUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.util.StringUtils;

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
        "classpath:/META-INF/opennms/applicationContext-alarmd.xml",
        "classpath:/META-INF/opennms/applicationContext-setupIpLike-enabled.xml"
})
@JUnitTemporaryDatabase(tempDbClass=MockDatabase.class)
public class JdbcAlarmWriterTest implements TemporaryDatabaseAware<MockDatabase> {
    // private JdbcEventWriter m_jdbcEventWriter;
    private MockNetwork m_mockNetwork = new MockNetwork();

    private Alarmd m_alarmd;

    @Autowired
    private AlarmDao m_alarmDao;

    @Autowired
    private EventDao m_eventDao;

    @Autowired
    private NodeDao m_nodeDao;

    @Autowired
    private JdbcTemplate m_jdbcTemplate;

    @Autowired
    private MockEventIpcManager m_eventdIpcMgr;

    private MockDatabase m_database;

    public void setTemporaryDatabase(MockDatabase database) {
        m_database = database;
    }

    @Before
    public void setUp() throws Exception {
        m_mockNetwork.createStandardNetwork();

        m_eventdIpcMgr.setEventWriter(m_database);

        m_alarmd = new Alarmd();
        m_alarmd.setEventForwarder(m_eventdIpcMgr);
        m_alarmd.setEventSubscriptionService(m_eventdIpcMgr);
        AlarmPersisterImpl persister = new AlarmPersisterImpl();
        persister.setAlarmDao(m_alarmDao);
        persister.setEventDao(m_eventDao);
        m_alarmd.setPersister(persister);
        m_alarmd.afterPropertiesSet();
        // Doesn't do anything yet
        m_alarmd.start();
        
        // Insert some empty nodes to avoid foreign-key violations on subsequent events/alarms
        OnmsNode node = new OnmsNode();
        node.setId(1);
        m_nodeDao.save(node);
    }

    @After
    public void tearDown() throws Exception {
        m_alarmd.destroy();
    }

    @Test
    public void testPersistAlarm() throws Exception {
        MockNode node = m_mockNetwork.getNode(1);

        //there should be no alarms in the alarms table
        assertEquals(0, m_jdbcTemplate.queryForInt("select count(*) from alarms"));

        //this should be the first occurrence of this alarm
        //there should be 1 alarm now
        sendNodeDownEvent("%nodeid%", node);
        Thread.sleep(1000);
        assertEquals(1, m_jdbcTemplate.queryForInt("select count(*) from alarms"));

        //this should be the second occurrence and shouldn't create another row
        //there should still be only 1 alarm
        sendNodeDownEvent("%nodeid%", node);
        Thread.sleep(1000);
        assertEquals(1, m_jdbcTemplate.queryForInt("select count(*) from alarms"));

        //this should be a new alarm because of the new key
        //there should now be 2 alarms
        sendNodeDownEvent("DontReduceThis", node);
        Thread.sleep(1000);
        assertEquals(2, m_jdbcTemplate.queryForInt("select count(*) from alarms"));

        MockUtil.println("Going for the print of the counter column");
        m_jdbcTemplate.query("select reductionKey, sum(counter) from alarms group by reductionKey", new RowCallbackHandler() {
            public void processRow(ResultSet rs) throws SQLException {
                MockUtil.println("count for reductionKey: " + rs.getString(1) + " is: " + rs.getObject(2));
            }

        });
    }

    @Test
    @Ignore
    public void testPersistManyAlarmsAtOnce() throws InterruptedException {
        int numberOfAlarmsToReduce = 10;

        //there should be no alarms in the alarms table
        assertEquals(0, m_jdbcTemplate.queryForInt("select count(*) from alarms"));

        final String reductionKey = "countThese";
        final MockNode node = m_mockNetwork.getNode(1);

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
                    } catch (Throwable t) {
                        t.printStackTrace();
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

        //this should be the first occurrence of this alarm
        //there should be 1 alarm now
        int rowCount = m_jdbcTemplate.queryForInt("select count(*) from alarms");
        Integer counterColumn = m_jdbcTemplate.queryForInt("select counter from alarms where reductionKey = ?", new Object[] { reductionKey });
        MockUtil.println("rowcCount is: "+rowCount+", expected 1.");
        MockUtil.println("counterColumn is: "+counterColumn+", expected "+numberOfAlarmsToReduce);
        assertEquals(1, rowCount);
        if (numberOfAlarmsToReduce != counterColumn) {
            final List<Integer> reducedEvents = new ArrayList<Integer>();
            m_jdbcTemplate.query("select eventid from events where alarmID is not null", new RowCallbackHandler() {
                public void processRow(ResultSet rs) throws SQLException {
                    reducedEvents.add(rs.getInt(1));
                }
            });
            Collections.sort(reducedEvents);

            final List<Integer> nonReducedEvents = new ArrayList<Integer>();
            m_jdbcTemplate.query("select eventid from events where alarmID is null", new RowCallbackHandler() {
                public void processRow(ResultSet rs) throws SQLException {
                    nonReducedEvents.add(rs.getInt(1));
                }
            });
            Collections.sort(nonReducedEvents);

            fail("number of alarms to reduce (" + numberOfAlarmsToReduce + ") were not reduced into a single alarm (instead the counter column reads " + counterColumn + "); "
                    + "events that were reduced: " + StringUtils.collectionToCommaDelimitedString(reducedEvents) + "; events that were not reduced: "
                    + StringUtils.collectionToCommaDelimitedString(nonReducedEvents));
        }


        Integer alarmId = m_jdbcTemplate.queryForInt("select alarmId from alarms where reductionKey = ?", new Object[] { reductionKey });
        rowCount = m_jdbcTemplate.queryForInt("select count(*) from events where alarmid = ?", new Object[] { alarmId });
        MockUtil.println(String.valueOf(rowCount) + " of events with alarmid: "+alarmId);
        //      assertEquals(numberOfAlarmsToReduce, rowCount);

        rowCount = m_jdbcTemplate.queryForInt("select count(*) from events where alarmid is null");
        MockUtil.println(String.valueOf(rowCount) + " of events with null alarmid");
        assertEquals(0, rowCount);

    }

    @Test
    public void testNullEvent() throws Exception {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("event argument must not be null"));
        try {
            m_alarmd.getPersister().persist(null);
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }

    @Test
    public void testNoLogmsg() throws Exception {
        Event event = new Event();
        event.setAlarmData(new AlarmData());

        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("Incoming event has an illegal dbid (0), aborting"));
        try {
            m_alarmd.getPersister().persist(event);
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }

    @Test
    public void testNoAlarmData() throws Exception {
        Event event = new Event();
        event.setLogmsg(new Logmsg());

        m_alarmd.getPersister().persist(event);
    }

    @Test
    public void testNoDbid() throws Exception {
        Event event = new Event();
        event.setLogmsg(new Logmsg());
        event.setAlarmData(new AlarmData());

        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("Incoming event has an illegal dbid (0), aborting"));
        try {
            m_alarmd.getPersister().persist(event);
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }

    private void sendNodeDownEvent(String reductionKey, MockNode node) throws SQLException {
        Event event = MockEventUtil.createNodeDownEvent("Test", node);

        if (reductionKey != null) {
            AlarmData data = new AlarmData();
            data.setAlarmType(1);
            data.setReductionKey(reductionKey);
            event.setAlarmData(data);
        } else {
            event.setAlarmData(null);
        }

        Logmsg logmsg = new Logmsg();
        logmsg.setDest("logndisplay");
        logmsg.setContent("testing");
        event.setLogmsg(logmsg);

        m_eventdIpcMgr.sendNow(event);
    }
}
