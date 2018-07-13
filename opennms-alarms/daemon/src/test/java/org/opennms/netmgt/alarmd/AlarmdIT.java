/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

import static com.jayway.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

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
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.mock.MockEventUtil;
import org.opennms.netmgt.mock.MockNetwork;
import org.opennms.netmgt.mock.MockNode;
import org.opennms.netmgt.model.OnmsAlarm;
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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;


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
@JUnitTemporaryDatabase(dirtiesContext=false,tempDbClass=MockDatabase.class,reuseDatabase=false)
public class AlarmdIT implements TemporaryDatabaseAware<MockDatabase>, InitializingBean {

    public class MockNorthbounder implements Northbounder {

        private boolean m_startCalled = false;
        private List<NorthboundAlarm> m_alarms = new ArrayList<>();

        @Override
        public void start() throws NorthbounderException {
            m_startCalled = true;
        }

        @Override
        public boolean isReady() {
            return true;
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

        @Override
        public void reloadConfig() {
        }

    }

    private MockNetwork m_mockNetwork = new MockNetwork();

    @Autowired
    private Alarmd m_alarmd;

    @Autowired
    private AlarmDao m_alarmDao;

    @Autowired
    private MonitoringLocationDao m_locationDao;

    @Autowired
    private NodeDao m_nodeDao;

    @Autowired
    private MockEventIpcManager m_eventMgr;

    @Autowired
    private ServiceRegistry m_registry;

    private MockDatabase m_database;

    private MockNorthbounder m_northbounder;

    @Override
    public void setTemporaryDatabase(final MockDatabase database) {
        m_database = database;
    }

    @Override
    public void afterPropertiesSet() {
        BeanUtils.assertAutowiring(this);
    }

    @Before
    public void setUp() {
        m_mockNetwork.createStandardNetwork();

        m_eventMgr.setEventWriter(m_database);

        // Insert some empty nodes to avoid foreign-key violations on subsequent events/alarms
        final OnmsNode node = new OnmsNode(m_locationDao.getDefaultLocation(), "node1");
        node.setId(1);
        m_nodeDao.save(node);
        
        m_northbounder = new MockNorthbounder();
        m_registry.register(m_northbounder, Northbounder.class);

        m_alarmd.start();
    }

    @After
    public void tearDown() {
        m_alarmd.destroy();
    }

    @Test
    @Transactional
    public void testPersistAlarm() throws Exception {
        final MockNode node = m_mockNetwork.getNode(1);

        //there should be no alarms in the alarms table
        assertEmptyAlarmTable();

        //this should be the first occurrence of this alarm
        //there should be 1 alarm now
        sendNodeDownEvent("%nodeid%", node);
        await().atMost(1, SECONDS).until(allAnticipatedEventsWereReceived());
        assertEquals(1, m_alarmDao.findAll().size());

        //this should be the second occurrence and shouldn't create another row
        //there should still be only 1 alarm
        sendNodeDownEvent("%nodeid%", node);
        await().atMost(1, SECONDS).until(allAnticipatedEventsWereReceived());
        assertEquals(1, m_alarmDao.findAll().size());

        //this should be a new alarm because of the new key
        //there should now be 2 alarms
        sendNodeDownEvent("DontReduceThis", node);
        await().atMost(1, SECONDS).until(allAnticipatedEventsWereReceived());
        assertEquals(2, m_alarmDao.findAll().size());

        MockUtil.println("Going for the print of the counter column");
        for (OnmsAlarm alarm : m_alarmDao.findAll()) {
            MockUtil.println("count for reductionKey: " + alarm.getReductionKey() + " is: " + alarm.getCounter());
        }
    }
    

    @Test
    public void testPersistManyAlarmsAtOnce() throws InterruptedException {
        int numberOfAlarmsToReduce = 10;

        //there should be no alarms in the alarms table
        assertEmptyAlarmTable();

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
        int alarmCount = m_alarmDao.findAll().size();
        assertEquals("there should be 1 alarm", 1, alarmCount);
        OnmsAlarm alarm = m_alarmDao.findByReductionKey(reductionKey);
        Integer counter = alarm.getCounter();
        MockUtil.println("rowcCount is: "+alarmCount+", expected 1.");
        MockUtil.println("counterColumn is: "+counter+", expected "+numberOfAlarmsToReduce);
        if (numberOfAlarmsToReduce != counter) {
            fail("number of alarms to reduce (" + numberOfAlarmsToReduce + ") were not reduced into a single alarm (instead the counter column reads " + counter + "); ");
        }
    }

    @Test
    @Transactional
    public void testPersistSituations() throws Exception {
        final MockNode node = m_mockNetwork.getNode(1);

        //there should be no alarms in the alarms table
        assertEmptyAlarmTable();

        //there should be no alarms in the alarm_situations table
        assertEmptyAlarmSituationTable();

        //create 3 alarms to roll up into situation
        sendNodeDownEvent("Alarm1", node);
        await().atMost(1, SECONDS).until(allAnticipatedEventsWereReceived());
        assertEquals(1, m_alarmDao.findAll().size());

        sendNodeDownEvent("Alarm2", node);
        await().atMost(1, SECONDS).until(allAnticipatedEventsWereReceived());
        assertEquals(2, m_alarmDao.findAll().size());

        sendNodeDownEvent("Alarm3", node);
        await().atMost(1, SECONDS).until(allAnticipatedEventsWereReceived());
        assertEquals(3, m_alarmDao.findAll().size());

        //create situation rolling up the first 2 alarms
        List<String> reductionKeys = new ArrayList<>(Arrays.asList("Alarm1", "Alarm2"));
        sendSituationEvent("Situation1", node, reductionKeys);
        await().atMost(1, SECONDS).until(allAnticipatedEventsWereReceived());
        OnmsAlarm situation = m_alarmDao.findByReductionKey("Situation1");
        assertEquals(2, situation.getRelatedAlarms().size());

        //send situation in with 3rd alarm, should result in 1 situation with 3 alarms
        List<String> newReductionKeys = new ArrayList<>(Arrays.asList("Alarm3"));
        sendSituationEvent("Situation1", node, newReductionKeys);
        await().atMost(1, SECONDS).until(allAnticipatedEventsWereReceived());
        situation = m_alarmDao.findByReductionKey("Situation1");
        assertEquals(3, situation.getRelatedAlarms().size());
    }
    
    @Test
    @Transactional
    public void testNullEvent() throws Exception {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new NullPointerException("Cannot create alarm from null event."));
        try {
            m_alarmd.getPersister().persist(null);
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }

    @Test
    @Transactional
    public void testNorthbounder() throws Exception {
        assertTrue(m_northbounder.isInitialized());
        assertThat(m_northbounder.getAlarms(), hasSize(0));

        final EventBuilder bldr = new EventBuilder("testNoLogmsg", "AlarmdTest");
        bldr.setAlarmData(new AlarmData());
        bldr.setLogMessage("This is a test.");

        final Event event = bldr.getEvent();
        event.setDbid(17);

        MockNode node = m_mockNetwork.getNode(1);
        sendNodeDownEvent("%nodeid%", node);

        final List<NorthboundAlarm> alarms = m_northbounder.getAlarms();
        assertThat(alarms, hasSize(greaterThan(0)));
    }
    

    @Test
    @Transactional
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
    @Transactional
    public void testNoAlarmData() throws Exception {
        EventBuilder bldr = new EventBuilder("testNoAlarmData", "AlarmdTest");
        bldr.setLogMessage(null);

        m_alarmd.getPersister().persist(bldr.getEvent());
    }

    @Test
    @Transactional
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
    @Transactional
    public void changeFields() throws InterruptedException, SQLException {
        assertEmptyAlarmTable();

        String reductionKey = "testUpdateField";
        MockNode node1 = m_mockNetwork.getNode(1);

        //Verify we have the default alarm
        sendNodeDownEvent(reductionKey, node1);
        OnmsAlarm alarm = m_alarmDao.findByReductionKey(reductionKey);
        assertEquals(OnmsSeverity.MAJOR, alarm.getSeverity());
        
        //Store the original logmsg from the original alarm (we are about to test changing it with subsequent alarm reduction)
        String defaultLogMsg = alarm.getLogMsg();
        assertTrue("The logmsg column should not be null", defaultLogMsg != null);

        //Duplicate the alarm but change the severity and verify the change
        sendNodeDownEventWithUpdateFieldSeverity(reductionKey, node1, OnmsSeverity.CRITICAL);
        assertEquals("Severity should now be Critical", OnmsSeverity.CRITICAL, m_alarmDao.findByReductionKey(reductionKey).getSeverity());
        
        //Duplicate the alarm but don't force the change of severity
        sendNodeDownEvent(reductionKey, node1);
        assertEquals("Severity should still be Critical", OnmsSeverity.CRITICAL, m_alarmDao.findByReductionKey(reductionKey).getSeverity());

        //Duplicate the alarm and change the logmsg
        sendNodeDownEventChangeLogMsg(reductionKey, node1, "new logMsg");
        String newLogMsg = m_alarmDao.findByReductionKey(reductionKey).getLogMsg();
        assertEquals("new logMsg", newLogMsg);
        assertTrue(!newLogMsg.equals(defaultLogMsg));
        
        //Duplicate the alarm but force logmsg to not change (logmsg field is updated by default)
        sendNodeDownEventDontChangeLogMsg(reductionKey, node1, "newer logMsg");
        newLogMsg = m_alarmDao.findByReductionKey(reductionKey).getLogMsg();
        assertTrue("The logMsg should not have changed.", !"newer logMsg".equals(newLogMsg));
        assertEquals("The logMsg should still be equal to the previous update.", "new logMsg", newLogMsg);

        
        //Duplicate the alarm with the default configuration and verify the logmsg has changed (as is the default behavior
        //for this field)
        sendNodeDownEvent(reductionKey, node1);
        newLogMsg = m_alarmDao.findByReductionKey(reductionKey).getLogMsg();
        assertTrue("The logMsg should have changed.", !"new logMsg".equals(newLogMsg));
        assertEquals("The logMsg should new be the default logMsg.", newLogMsg, defaultLogMsg);
        
        // Acknowledge the alarm via update-field for "acktime" / "ackuser"
        Map<String,String> eventParams = new LinkedHashMap<>();
        sendNodeDownEventWithUpdateFieldsAckUserAndTime(reductionKey, node1, "swivelchair", "", eventParams);
        String newAckUser = m_alarmDao.findByReductionKey(reductionKey).getAckUser();
        long newAckTime = m_alarmDao.findByReductionKey(reductionKey).getAckTime().getTime();
        assertEquals("New alarmackuser must be as in event parameter", "swivelchair", newAckUser);
        assertTrue("New alarmacktime must be non-null", newAckTime != 0);
        assertTrue("New alarmacktime must be within 5s of current system time", System.currentTimeMillis() - newAckTime < 5000);
        
        // Change the alarm's ackuser / acktime via update-field -- acktime heuristically assumed to be in whole seconds
        eventParams.put("extSourcedAckUser", "somebodyelse");
        eventParams.put("extSourcedAckTime", "1000000");
        sendNodeDownEventWithUpdateFieldsAckUserAndTime(reductionKey, node1, "%parm[#1]%", "%parm[extSourcedAckTime]%", eventParams);
        newAckUser = m_alarmDao.findByReductionKey(reductionKey).getAckUser();
        newAckTime = m_alarmDao.findByReductionKey(reductionKey).getAckTime().getTime();
        assertEquals("somebodyelse", newAckUser);
        assertEquals(1000000000L, newAckTime);
        
        // Change the alarm's ackuser / acktime via update-field -- acktime heuristically assumed to be in milliseconds
        eventParams.put("extSourcedAckUser", "somethirdactor");
        eventParams.put("extSourcedAckTime", "1526040190000");
        sendNodeDownEventWithUpdateFieldsAckUserAndTime(reductionKey, node1, "%parm[#1]%", "%parm[extSourcedAckTime]%", eventParams);
        newAckUser = m_alarmDao.findByReductionKey(reductionKey).getAckUser();
        newAckTime = m_alarmDao.findByReductionKey(reductionKey).getAckTime().getTime();
        assertEquals("somethirdactor", newAckUser);
        assertEquals(1526040190000L, newAckTime);
        
        // Change the alarm's ackuser / acktime via update-field -- acktime heuristically assumed to be an SNMPv2-TC::DateAndTime including time zone
        eventParams.put("extSourcedAckUser", "someotheractortz");
        eventParams.put("extSourcedAckTime", "0x07e2050b0d2a3a052d0000");
        sendNodeDownEventWithUpdateFieldsAckUserAndTime(reductionKey, node1, "%parm[#1]%", "%parm[extSourcedAckTime]%", eventParams);
        newAckUser = m_alarmDao.findByReductionKey(reductionKey).getAckUser();
        newAckTime = m_alarmDao.findByReductionKey(reductionKey).getAckTime().getTime();
        assertEquals("someotheractortz", newAckUser);
        assertEquals(1526046178500L, newAckTime);
        
        // Change the alarm's ackuser / acktime via update-field -- acktime heuristically assumed to be an SNMPv2-TC::DateAndTime excluding time zone
        eventParams.put("extSourcedAckUser", "someotheractornotz");
        eventParams.put("extSourcedAckTime", "0x07e2050b0d2a3a09");
        sendNodeDownEventWithUpdateFieldsAckUserAndTime(reductionKey, node1, "%parm[#1]%", "%parm[extSourcedAckTime]%", eventParams);
        newAckUser = m_alarmDao.findByReductionKey(reductionKey).getAckUser();
        newAckTime = m_alarmDao.findByReductionKey(reductionKey).getAckTime().getTime();
        assertEquals("someotheractornotz", newAckUser);
        assertEquals(1526046178900L, newAckTime);
        
        // De-acknowledge the alarm via update-field. Verify this nulls both acktime and ackuser.
        eventParams.clear();
        eventParams.put("extSourcedAckUser", "somethirdactor");
        eventParams.put("extSourcedAckTime", "null");
        sendNodeDownEventWithUpdateFieldsAckUserAndTime(reductionKey, node1, "%parm[extSourcedAckUser]%", "%parm[#2]%", eventParams);
        assertNull(m_alarmDao.findByReductionKey(reductionKey).getAckUser());
        assertNull(m_alarmDao.findByReductionKey(reductionKey).getAckTime());
    }

    //Supporting method for test
    private void sendNodeDownEventDontChangeLogMsg(String reductionKey, MockNode node, String logMsg) {
        
        EventBuilder event = MockEventUtil.createNodeDownEventBuilder("Test", node);

        if (reductionKey != null) {
            AlarmData data = new AlarmData();
            data.setAlarmType(1);
            data.setReductionKey(reductionKey);
            
            List<UpdateField> fields = new ArrayList<>();
            
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

        m_eventMgr.sendNow(event.getEvent());
    }
    
    private void sendNodeDownEventChangeLogMsg(String reductionKey, MockNode node, String logMsg) {
        
        EventBuilder event = MockEventUtil.createNodeDownEventBuilder("Test", node);

        if (reductionKey != null) {
            AlarmData data = new AlarmData();
            data.setAlarmType(1);
            data.setReductionKey(reductionKey);
            
            List<UpdateField> fields = new ArrayList<>();
            
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

        m_eventMgr.sendNow(event.getEvent());
    }

    private void sendNodeDownEventWithUpdateFieldSeverity(String reductionKey, MockNode node, OnmsSeverity severity) {
        EventBuilder event = MockEventUtil.createNodeDownEventBuilder("Test", node);

        if (reductionKey != null) {
            AlarmData data = new AlarmData();
            data.setAlarmType(1);
            data.setReductionKey(reductionKey);
            
            List<UpdateField> fields = new ArrayList<>();
            
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

        m_eventMgr.sendNow(event.getEvent());
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

        m_eventMgr.sendNow(event.getEvent());
    }

    private void sendSituationEvent(String reductionKey, MockNode node, List<String> alarmReductionKeys) throws SQLException {
        EventBuilder event = MockEventUtil.createNodeEventBuilder("Test", "uei.opennms.org/nodes/situation", node);
        AlarmData data = new AlarmData();
        data.setAlarmType(1);
        data.setReductionKey(reductionKey);
        event.setSeverity(OnmsSeverity.MAJOR.getLabel());
        event.setAlarmData(data);
        event.setLogDest("logndisplay");
        event.setLogMessage("testing");
        for (String alarm : alarmReductionKeys) {
            // TOOD revisit when event_parameters table supports multiple params with the same name (NMS-10214)
            // For now, suffix the parameter name with the value.
            event.addParam("related-reductionKey" + alarm, alarm);
        }
        m_eventMgr.sendNow(event.getEvent());
    }

    private void assertEmptyAlarmTable() {
        List<OnmsAlarm> alarms = m_alarmDao.findAll();
        assertEquals("Found one or more alarms: " + alarms, 0, alarms.size());
    }
    
    private void assertEmptyAlarmSituationTable() {
        List<String> alarmDescriptions = m_alarmDao.findAll().stream()
                .map(a -> a.getRelatedAlarms())
                .flatMap(Collection::stream)
                .map(a -> String.format("Alarm[id=%s, reductionKey=%s, severity=%s]", a.getId(), a.getReductionKey(), a.getSeverity()))
                .collect(Collectors.toList());
        assertEquals("Found one or more alarms linked to Situations: " + alarmDescriptions, 0, alarmDescriptions.size());
    }

    private void sendNodeDownEventWithUpdateFieldsAckUserAndTime(String reductionKey, MockNode node, String ackUserExpr, String ackTimeExpr, Map<String,String> params) throws SQLException {
        EventBuilder event = MockEventUtil.createNodeDownEventBuilder("Test", node);

        if (reductionKey != null) {
            AlarmData data = new AlarmData();
            data.setAlarmType(1);
            data.setReductionKey(reductionKey);
            
            List<UpdateField> fields = new ArrayList<>();
            
            UpdateField field = new UpdateField();
            if (ackUserExpr != null) {
                field.setFieldName("AckUser");
                field.setUpdateOnReduction(Boolean.TRUE);
                field.setValueExpression(ackUserExpr);
                fields.add(field);
            }
            
            field = new UpdateField();
            if (ackTimeExpr != null) {
                field.setFieldName("AckTime");
                field.setUpdateOnReduction(Boolean.TRUE);
                field.setValueExpression(ackTimeExpr);
                fields.add(field);
            }
            
            data.setUpdateField(fields);
            
            event.setAlarmData(data);
        } else {
            event.setAlarmData(null);
        }

        event.setLogDest("logndisplay");
        event.setLogMessage("testing");
        
        for (String paramName : params.keySet()) {
            event.addParam(paramName, params.get(paramName), "OctetString", null);
        }

        m_eventMgr.sendNow(event.getEvent());
    }

    public Callable<Boolean> allAnticipatedEventsWereReceived() {
        return new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return m_eventMgr.getEventAnticipator().getAnticipatedEvents().isEmpty();
            }
        };
    }
}
