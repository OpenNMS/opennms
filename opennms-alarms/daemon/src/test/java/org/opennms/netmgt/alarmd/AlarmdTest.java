/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.alarmd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.soa.ServiceRegistry;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.MockDatabase;
import org.opennms.core.test.db.TemporaryDatabaseAware;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.alarmd.api.NorthboundAlarm;
import org.opennms.netmgt.alarmd.api.Northbounder;
import org.opennms.netmgt.alarmd.api.NorthbounderException;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.mock.MockEventUtil;
import org.opennms.netmgt.mock.MockNetwork;
import org.opennms.netmgt.mock.MockNode;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.AlarmData;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.UpdateField;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.test.ThrowableAnticipator;
import org.opennms.test.mock.MockUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.StringUtils;


@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-alarmd.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(dirtiesContext=false,tempDbClass=MockDatabase.class)
public class AlarmdTest implements TemporaryDatabaseAware<MockDatabase>, InitializingBean {

    public class MockNorthbounder implements Northbounder {

        private boolean m_startCalled = false;
        private List<NorthboundAlarm> m_alarms = new ArrayList<NorthboundAlarm>();

        @Override
        public void start() throws NorthbounderException {
            m_startCalled = true;
        }

        @Override
        public void onAlarm(final NorthboundAlarm alarm) throws NorthbounderException {
            m_alarms.add(alarm);
        }

        @Override
        public void stop() throws NorthbounderException {
        }

        public boolean isInitialized() {
            return m_startCalled;
        }
        
        public List<NorthboundAlarm> getAlarms() {
            return m_alarms;
        }

		@Override
		public String getName() {
			return "MockNorthbounder";
		}
    }

    private MockNetwork m_mockNetwork = new MockNetwork();

    @Autowired
    private Alarmd m_alarmd;

    @Autowired
    private NodeDao m_nodeDao;

    @Autowired
    private JdbcTemplate m_jdbcTemplate;

    @Autowired
    private MockEventIpcManager m_eventdIpcMgr;

    @Autowired
    private ServiceRegistry m_registry;

    private MockDatabase m_database;

    private MockNorthbounder m_northbounder;

    @Override
    public void setTemporaryDatabase(final MockDatabase database) {
        m_database = database;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Before
    public void setUp() throws Exception {
        m_mockNetwork.createStandardNetwork();

        m_eventdIpcMgr.setEventWriter(m_database);

        // Insert some empty nodes to avoid foreign-key violations on subsequent events/alarms
        final OnmsNode node = new OnmsNode();
        node.setId(1);
        node.setLabel("node1");
        m_nodeDao.save(node);
        
        m_northbounder = new MockNorthbounder();
        m_registry.register(m_northbounder, Northbounder.class);
    }

    @After
    public void tearDown() throws Exception {
        m_alarmd.destroy();
    }

    @Test
    @JUnitTemporaryDatabase(tempDbClass=MockDatabase.class) // Relies on specific IDs so we need a fresh database
    public void testPersistAlarm() throws Exception {
        final MockNode node = m_mockNetwork.getNode(1);

        //there should be no alarms in the alarms table
        assertEquals(0, m_jdbcTemplate.queryForObject("select count(*) from alarms", Integer.class).intValue());

        //this should be the first occurrence of this alarm
        //there should be 1 alarm now
        sendNodeDownEvent("%nodeid%", node);
        Thread.sleep(1000);
        assertEquals(1, m_jdbcTemplate.queryForObject("select count(*) from alarms", Integer.class).intValue());

        //this should be the second occurrence and shouldn't create another row
        //there should still be only 1 alarm
        sendNodeDownEvent("%nodeid%", node);
        Thread.sleep(1000);
        assertEquals(1, m_jdbcTemplate.queryForObject("select count(*) from alarms", Integer.class).intValue());

        //this should be a new alarm because of the new key
        //there should now be 2 alarms
        sendNodeDownEvent("DontReduceThis", node);
        Thread.sleep(1000);
        assertEquals(2, m_jdbcTemplate.queryForObject("select count(*) from alarms", Integer.class).intValue());

        MockUtil.println("Going for the print of the counter column");
        m_jdbcTemplate.query("select reductionKey, sum(counter) from alarms group by reductionKey", new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet rs) throws SQLException {
                MockUtil.println("count for reductionKey: " + rs.getString(1) + " is: " + rs.getObject(2));
            }

        });
    }
    

    @Test
    @JUnitTemporaryDatabase(tempDbClass=MockDatabase.class)
    public void testPersistManyAlarmsAtOnce() throws InterruptedException {
        int numberOfAlarmsToReduce = 10;

        //there should be no alarms in the alarms table
        assertEquals(0, m_jdbcTemplate.queryForObject("select count(*) from alarms", Integer.class).intValue());

        final String reductionKey = "countThese";
        final MockNode node = m_mockNetwork.getNode(1);

        final long millis = System.currentTimeMillis()+2500;

        final CountDownLatch signal = new CountDownLatch(numberOfAlarmsToReduce);

        for (int i=1; i<= numberOfAlarmsToReduce; i++) {
            MockUtil.println("Creating Runnable: "+i+" of "+numberOfAlarmsToReduce+" events to reduce.");

            class EventRunner implements Runnable {
                @Override
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
                        signal.countDown();
                    }
                }
            }

            Runnable r = new EventRunner();
            Thread p = new Thread(r);
            p.start();
        }

        signal.await();

        //this should be the first occurrence of this alarm
        //there should be 1 alarm now
        int rowCount = m_jdbcTemplate.queryForObject("select count(*) from alarms", Integer.class).intValue();
        Integer counterColumn = m_jdbcTemplate.queryForObject("select counter from alarms where reductionKey = ?", new Object[] { reductionKey }, Integer.class).intValue();
        MockUtil.println("rowcCount is: "+rowCount+", expected 1.");
        MockUtil.println("counterColumn is: "+counterColumn+", expected "+numberOfAlarmsToReduce);
        assertEquals(1, rowCount);
        if (numberOfAlarmsToReduce != counterColumn) {
            final List<Integer> reducedEvents = new ArrayList<Integer>();
            m_jdbcTemplate.query("select eventid from events where alarmID is not null", new RowCallbackHandler() {
                @Override
                public void processRow(ResultSet rs) throws SQLException {
                    reducedEvents.add(rs.getInt(1));
                }
            });
            Collections.sort(reducedEvents);

            final List<Integer> nonReducedEvents = new ArrayList<Integer>();
            m_jdbcTemplate.query("select eventid from events where alarmID is null", new RowCallbackHandler() {
                @Override
                public void processRow(ResultSet rs) throws SQLException {
                    nonReducedEvents.add(rs.getInt(1));
                }
            });
            Collections.sort(nonReducedEvents);

            fail("number of alarms to reduce (" + numberOfAlarmsToReduce + ") were not reduced into a single alarm (instead the counter column reads " + counterColumn + "); "
                    + "events that were reduced: " + StringUtils.collectionToCommaDelimitedString(reducedEvents) + "; events that were not reduced: "
                    + StringUtils.collectionToCommaDelimitedString(nonReducedEvents));
        }


        Integer alarmId = m_jdbcTemplate.queryForObject("select alarmId from alarms where reductionKey = ?", new Object[] { reductionKey }, Integer.class).intValue();
        rowCount = m_jdbcTemplate.queryForObject("select count(*) from events where alarmid = ?", new Object[] { alarmId }, Integer.class).intValue();
        MockUtil.println(String.valueOf(rowCount) + " of events with alarmid: "+alarmId);
        //      assertEquals(numberOfAlarmsToReduce, rowCount);

        rowCount = m_jdbcTemplate.queryForObject("select count(*) from events where alarmid is null", Integer.class).intValue();
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
    @JUnitTemporaryDatabase(tempDbClass=MockDatabase.class) // Relies on specific IDs so we need a fresh database
    public void testNorthbounder() throws Exception {
        assertTrue(m_northbounder.isInitialized());
        assertTrue(m_northbounder.getAlarms().isEmpty());

        final EventBuilder bldr = new EventBuilder("testNoLogmsg", "AlarmdTest");
        bldr.setAlarmData(new AlarmData());
        bldr.setLogMessage("This is a test.");

        final Event event = bldr.getEvent();
        event.setDbid(17);

        MockNode node = m_mockNetwork.getNode(1);
        sendNodeDownEvent("%nodeid%", node);

        final List<NorthboundAlarm> alarms = m_northbounder.getAlarms();
        assertTrue(alarms.size() > 0);
    }
    

    @Test
    public void testNoLogmsg() throws Exception {
        EventBuilder bldr = new EventBuilder("testNoLogmsg", "AlarmdTest");
        bldr.setAlarmData(new AlarmData());

        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("Incoming event has an illegal dbid (0), aborting"));
        try {
            m_alarmd.getPersister().persist(bldr.getEvent());
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }

    @Test
    public void testNoAlarmData() throws Exception {
        EventBuilder bldr = new EventBuilder("testNoAlarmData", "AlarmdTest");
        bldr.setLogMessage(null);

        m_alarmd.getPersister().persist(bldr.getEvent());
    }

    @Test
    public void testNoDbid() throws Exception {
        EventBuilder bldr = new EventBuilder("testNoDbid", "AlarmdTest");
        bldr.setLogMessage(null);
        bldr.setAlarmData(new AlarmData());

        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("Incoming event has an illegal dbid (0), aborting"));
        try {
            m_alarmd.getPersister().persist(bldr.getEvent());
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }
    
    @Test
    @JUnitTemporaryDatabase(tempDbClass=MockDatabase.class)
    public void changeFields() throws InterruptedException, SQLException {
        assertEquals(0, m_jdbcTemplate.queryForObject("select count(*) from alarms", Integer.class).intValue());
        
        String reductionKey = "testUpdateField";
        
        int alarmCount = m_jdbcTemplate.queryForObject("select count(*) from alarms", Integer.class).intValue();
        
        assertEquals(0, alarmCount);
        
        MockNode node1 = m_mockNetwork.getNode(1);
        
        //Verify we have the default alarm
        sendNodeDownEvent(reductionKey, node1);
        int severity = m_jdbcTemplate.queryForObject("select severity from alarms a where a.reductionKey = ?", new Object[] { reductionKey }, Integer.class).intValue();
        assertEquals(OnmsSeverity.MAJOR, OnmsSeverity.get(severity));
        
        //Store the original logmsg from the original alarm (we are about to test changing it with subsequent alarm reduction)
        String defaultLogMsg = m_jdbcTemplate.query("select logmsg from alarms", new ResultSetExtractor<String>() {

            @Override
            public String extractData(ResultSet results) throws SQLException, DataAccessException {
                results.next();
                int row = results.getRow();
                boolean isLast = results.isLast();
                boolean isFirst = results.isFirst();
                
                if (row != 1 && !isLast && !isFirst) {
                    throw new SQLException("Row count is not = 1.  There should only be one row returned from the query: \n"+ results.getStatement());
                }
                
                return results.getString(1);
            }
            
        });
        
        assertTrue("The logmsg column should not be null", defaultLogMsg != null);

        //Duplicate the alarm but change the severity and verify the change
        sendNodeDownEventWithUpdateFieldSeverity(reductionKey, node1, OnmsSeverity.CRITICAL);
        severity = m_jdbcTemplate.queryForObject("select severity from alarms", Integer.class).intValue();
        assertEquals("Severity should now be Critical", OnmsSeverity.CRITICAL, OnmsSeverity.get(severity));
        
        //Duplicate the alarm but don't force the change of severity
        sendNodeDownEvent(reductionKey, node1);
        severity = m_jdbcTemplate.queryForObject("select severity from alarms", Integer.class).intValue();
        assertEquals("Severity should still be Critical", OnmsSeverity.CRITICAL, OnmsSeverity.get(severity));

        //Duplicate the alarm and change the logmsg
        sendNodeDownEventChangeLogMsg(reductionKey, node1, "new logMsg");
        String newLogMsg = m_jdbcTemplate.query("select logmsg from alarms", new ResultSetExtractor<String>() {
            @Override
            public String extractData(ResultSet results) throws SQLException, DataAccessException {
                results.next();
                return results.getString(1);
            }
        });
        assertEquals("new logMsg", newLogMsg);
        assertTrue(!newLogMsg.equals(defaultLogMsg));
        
        //Duplicate the alarm but force logmsg to not change (lggmsg field is updated by default)
        sendNodeDownEventDontChangeLogMsg(reductionKey, node1, "newer logMsg");
        newLogMsg = m_jdbcTemplate.query("select logmsg from alarms", new ResultSetExtractor<String>() {
            @Override
            public String extractData(ResultSet results) throws SQLException, DataAccessException {
                results.next();
                return results.getString(1);
            }
        });
        assertTrue("The logMsg should not have changed.", !"newer logMsg".equals(newLogMsg));
        assertEquals("The logMsg should still be equal to the previous update.", "new logMsg", newLogMsg);

        
        //Duplicate the alarm with the default configuration and verify the logmsg has changed (as is the default behavior
        //for this field)
        sendNodeDownEvent(reductionKey, node1);
        newLogMsg = m_jdbcTemplate.query("select logmsg from alarms", new ResultSetExtractor<String>() {
            @Override
            public String extractData(ResultSet results) throws SQLException, DataAccessException {
                results.next();
                return results.getString(1);
            }
        });
        assertTrue("The logMsg should have changed.", !"new logMsg".equals(newLogMsg));
        assertEquals("The logMsg should new be the default logMsg.", newLogMsg, defaultLogMsg);
        
    }

    //Supporting method for test
    private void sendNodeDownEventDontChangeLogMsg(String reductionKey, MockNode node, String logMsg) {
        
        EventBuilder event = MockEventUtil.createNodeDownEventBuilder("Test", node);

        if (reductionKey != null) {
            AlarmData data = new AlarmData();
            data.setAlarmType(1);
            data.setReductionKey(reductionKey);
            
            List<UpdateField> fields = new ArrayList<UpdateField>();
            
            UpdateField field = new UpdateField();
            field.setFieldName("logMsg");
            field.setUpdateOnReduction(Boolean.FALSE);
            fields.add(field);
            
            data.setUpdateField(fields);
            
            event.setAlarmData(data);
        } else {
            event.setAlarmData(null);
        }

        event.setLogDest("logndisplay");
        event.setLogMessage(logMsg);

        m_eventdIpcMgr.sendNow(event.getEvent());
    }
    
    private void sendNodeDownEventChangeLogMsg(String reductionKey, MockNode node, String logMsg) {
        
        EventBuilder event = MockEventUtil.createNodeDownEventBuilder("Test", node);

        if (reductionKey != null) {
            AlarmData data = new AlarmData();
            data.setAlarmType(1);
            data.setReductionKey(reductionKey);
            
            List<UpdateField> fields = new ArrayList<UpdateField>();
            
            UpdateField field = new UpdateField();
            field.setFieldName("logMsg");
            field.setUpdateOnReduction(Boolean.TRUE);
            fields.add(field);
            
            data.setUpdateField(fields);
            
            event.setAlarmData(data);
        } else {
            event.setAlarmData(null);
        }

        event.setLogDest("logndisplay");
        event.setLogMessage(logMsg);

        m_eventdIpcMgr.sendNow(event.getEvent());
    }

    private void sendNodeDownEventWithUpdateFieldSeverity(String reductionKey, MockNode node, OnmsSeverity severity) throws SQLException {
        EventBuilder event = MockEventUtil.createNodeDownEventBuilder("Test", node);

        if (reductionKey != null) {
            AlarmData data = new AlarmData();
            data.setAlarmType(1);
            data.setReductionKey(reductionKey);
            
            List<UpdateField> fields = new ArrayList<UpdateField>();
            
            UpdateField field = new UpdateField();
            field.setFieldName("Severity");
            field.setUpdateOnReduction(Boolean.TRUE);
            fields.add(field);
            
            data.setUpdateField(fields);
            
            event.setAlarmData(data);
        } else {
            event.setAlarmData(null);
        }

        event.setLogDest("logndisplay");
        event.setLogMessage("testing");
        
        event.setSeverity(severity.getLabel());

        m_eventdIpcMgr.sendNow(event.getEvent());
    }

    private void sendNodeDownEvent(String reductionKey, MockNode node) throws SQLException {
        EventBuilder event = MockEventUtil.createNodeDownEventBuilder("Test", node);

        if (reductionKey != null) {
            AlarmData data = new AlarmData();
            data.setAlarmType(1);
            data.setReductionKey(reductionKey);            
            event.setAlarmData(data);
        } else {
            event.setAlarmData(null);
        }

        event.setLogDest("logndisplay");
        event.setLogMessage("testing");

        m_eventdIpcMgr.sendNow(event.getEvent());
    }
}
