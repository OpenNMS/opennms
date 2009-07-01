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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Ignore;
import org.opennms.core.concurrent.BarrierSignaler;
import org.opennms.netmgt.dao.db.PopulatedTemporaryDatabaseTestCase;
import org.opennms.netmgt.eventd.JdbcEventdServiceManager;
import org.opennms.netmgt.eventd.processor.JdbcAlarmWriter;
import org.opennms.netmgt.eventd.processor.JdbcEventWriter;
import org.opennms.netmgt.mock.MockEventUtil;
import org.opennms.netmgt.mock.MockNetwork;
import org.opennms.netmgt.mock.MockNode;
import org.opennms.netmgt.xml.event.AlarmData;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Logmsg;
import org.opennms.test.ThrowableAnticipator;
import org.opennms.test.mock.MockUtil;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.util.StringUtils;

@Deprecated
@Ignore
public class JdbcAlarmWriterTest extends PopulatedTemporaryDatabaseTestCase {
    private JdbcAlarmWriter m_jdbcAlarmWriter;
    private JdbcEventWriter m_jdbcEventWriter;
    private MockNetwork m_mockNetwork = new MockNetwork();

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        m_mockNetwork.createStandardNetwork();
        
        JdbcEventdServiceManager eventdServiceManager = new JdbcEventdServiceManager();
        eventdServiceManager.setDataSource(getDataSource());
        eventdServiceManager.afterPropertiesSet();
        
        m_jdbcEventWriter = new JdbcEventWriter();
        m_jdbcEventWriter.setEventdServiceManager(eventdServiceManager);
        m_jdbcEventWriter.setDataSource(getDataSource());
        m_jdbcEventWriter.setGetNextIdString("SELECT nextval('eventsNxtId')");
        m_jdbcEventWriter.afterPropertiesSet();
        
        m_jdbcAlarmWriter = new JdbcAlarmWriter();
        m_jdbcAlarmWriter.setEventdServiceManager(eventdServiceManager);
        m_jdbcAlarmWriter.setDataSource(getDataSource());
        m_jdbcAlarmWriter.setGetNextIdString("SELECT nextval('alarmsNxtId')");
        m_jdbcAlarmWriter.afterPropertiesSet();
    }

    /**
     * tests sequence of newly initialized db
     */
    public void testNextAlarmId() {
        int nextId = getJdbcTemplate().queryForInt(m_jdbcAlarmWriter.getGetNextIdString());
        
        // an empty db should produce '1' here
        assertEquals(1, nextId);
    }
    

    public void testPersistAlarm() throws Exception {
        MockNode node = m_mockNetwork.getNode(1);

        //there should be no alarms in the alarms table
        assertEquals(0, jdbcTemplate.queryForInt("select count(*) from alarms"));

        //this should be the first occurrence of this alarm
        //there should be 1 alarm now
        sendNodeDownEvent("%nodeid%", node);
        Thread.sleep(1000);
        assertEquals(1, jdbcTemplate.queryForInt("select count(*) from alarms"));

        //this should be the second occurrence and shouldn't create another row
        //there should still be only 1 alarm
        sendNodeDownEvent("%nodeid%", node);
        Thread.sleep(1000);
        assertEquals(1, jdbcTemplate.queryForInt("select count(*) from alarms"));

        //this should be a new alarm because of the new key
        //there should now be 2 alarms
        sendNodeDownEvent("DontReduceThis", node);
        Thread.sleep(1000);
        assertEquals(2, jdbcTemplate.queryForInt("select count(*) from alarms"));

        MockUtil.println("Going for the print of the counter column");
        getJdbcTemplate().getJdbcOperations().query("select reductionKey, sum(counter) from alarms group by reductionKey", new RowCallbackHandler() {
            public void processRow(ResultSet rs) throws SQLException {
                MockUtil.println("count for reductionKey: " + rs.getString(1) + " is: " + rs.getObject(2));
            }
            
        });
    }
    
    public void testPersistManyAlarmsAtOnce() throws InterruptedException {
        int numberOfAlarmsToReduce = 10;
        
        //there should be no alarms in the alarms table
        assertEquals(0, jdbcTemplate.queryForInt("select count(*) from alarms"));

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
        int rowCount = jdbcTemplate.queryForInt("select count(*) from alarms");
        Integer counterColumn = jdbcTemplate.queryForInt("select counter from alarms where reductionKey = ?", new Object[] { reductionKey });
        MockUtil.println("rowcCount is: "+rowCount+", expected 1.");
        MockUtil.println("counterColumn is: "+counterColumn+", expected "+numberOfAlarmsToReduce);
        assertEquals(1, rowCount);
        if (numberOfAlarmsToReduce != counterColumn) {
            final List<Integer> reducedEvents = new ArrayList<Integer>();
            jdbcTemplate.getJdbcOperations().query("select eventid from events where alarmID is not null", new RowCallbackHandler() {
                public void processRow(ResultSet rs) throws SQLException {
                    reducedEvents.add(rs.getInt(1));
                }
            });
            Collections.sort(reducedEvents);

            final List<Integer> nonReducedEvents = new ArrayList<Integer>();
            jdbcTemplate.getJdbcOperations().query("select eventid from events where alarmID is null", new RowCallbackHandler() {
                public void processRow(ResultSet rs) throws SQLException {
                    nonReducedEvents.add(rs.getInt(1));
                }
            });
            Collections.sort(nonReducedEvents);
            
            fail("number of alarms to reduce (" + numberOfAlarmsToReduce + ") were not reduced into a single alarm (only " + counterColumn + " were); "
                    + "events that were reduced: " + StringUtils.collectionToCommaDelimitedString(reducedEvents) + "; events that were not reduced: "
                    + StringUtils.collectionToCommaDelimitedString(nonReducedEvents));
        }
        

        Integer alarmId = jdbcTemplate.queryForInt("select alarmId from alarms where reductionKey = ?", new Object[] { reductionKey });
        rowCount = jdbcTemplate.queryForInt("select count(*) from events where alarmid = ?", new Object[] { alarmId });
        MockUtil.println(String.valueOf(rowCount) + " of events with alarmid: "+alarmId);
//      assertEquals(numberOfAlarmsToReduce, rowCount);

        rowCount = jdbcTemplate.queryForInt("select count(*) from events where alarmid is null");
        MockUtil.println(String.valueOf(rowCount) + " of events with null alarmid");
        assertEquals(0, rowCount);

    }

    public void testNullEvent() throws Exception {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("event argument must not be null"));
        try {
            m_jdbcAlarmWriter.process(null, null);
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }
    
    public void testNoLogmsg() throws Exception {
        Event event = new Event();
        event.setAlarmData(new AlarmData());
        
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("event does not have a logmsg"));
        try {
            m_jdbcAlarmWriter.process(null, event);
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }
    
    public void testNoAlarmData() throws Exception {
        Event event = new Event();
        event.setLogmsg(new Logmsg());
        
        m_jdbcAlarmWriter.process(null, event);
    }

    public void testNoDbid() throws Exception {
        Event event = new Event();
        event.setLogmsg(new Logmsg());
        event.setAlarmData(new AlarmData());
        
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("event does not have a dbid"));
        try {
            m_jdbcAlarmWriter.process(null, event);
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
        
//        if (e.getAlarmData() == null && econf.getAlarmData() != null) {
//            AlarmData alarmData = new AlarmData();
//            alarmData.setAlarmType(econf.getAlarmData().getAlarmType());
//            alarmData.setReductionKey(econf.getAlarmData().getReductionKey());
//            alarmData.setClearUei(econf.getAlarmData().getClearUei());
//            alarmData.setAutoClean(econf.getAlarmData().getAutoClean());
//            alarmData.setX733AlarmType(econf.getAlarmData().getX733AlarmType());
//            alarmData.setX733ProbableCause(econf.getAlarmData().getX733ProbableCause());
//            alarmData.setClearKey(econf.getAlarmData().getClearKey());
//            e.setAlarmData(alarmData);
//        }

        m_jdbcEventWriter.process(null, event);
        m_jdbcAlarmWriter.process(null, event);
    }
}
