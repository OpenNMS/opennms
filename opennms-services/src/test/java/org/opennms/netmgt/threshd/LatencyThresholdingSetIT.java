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

package org.opennms.netmgt.threshd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.opennms.core.utils.InetAddressUtils.addr;

import java.io.File;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.MockDatabase;
import org.opennms.core.test.db.TemporaryDatabaseAware;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.PollOutagesConfigFactory;
import org.opennms.netmgt.config.ThreshdConfigFactory;
import org.opennms.netmgt.config.ThresholdingConfigFactory;
import org.opennms.netmgt.dao.api.IfLabel;
import org.opennms.netmgt.dao.hibernate.IfLabelDaoImpl;
import org.opennms.netmgt.dao.mock.EventAnticipator;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.dao.support.FilesystemResourceStorageDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.filter.FilterDaoFactory;
import org.opennms.netmgt.filter.api.FilterDao;
import org.opennms.netmgt.mock.MockNetwork;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.test.FileAnticipator;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;

/**
 * These tests were removed from {@link ThresholdingVisitorTest} because they
 * require Spring context initialization in order to have a working copy of
 * {@link IfLabelDaoImpl} which is used while storing latency information. 
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 *
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(tempDbClass=MockDatabase.class)
public class LatencyThresholdingSetIT implements TemporaryDatabaseAware<MockDatabase> {

    private static final Logger LOG = LoggerFactory.getLogger(LatencyThresholdingSetIT.class);

    private FileAnticipator m_fileAnticipator;
    private List<Event> m_anticipatedEvents = new ArrayList<>();
    private MockDatabase m_db;

    @Autowired
    private MockEventIpcManager m_eventIpcManager;

    @Autowired
    private JdbcTemplate m_jdbcTemplate;

    @Autowired
    private ApplicationContext m_context;

    private FilesystemResourceStorageDao m_resourceStorageDao;

    private static final Comparator<Parm> PARM_COMPARATOR = new Comparator<Parm>() {
        @Override
        public int compare(Parm o1, Parm o2) {
            if (o1 == null && o2 == null) return 0;
            if (o1 == null && o2 != null) return 1;
            if (o1 != null && o2 == null) return -1;

            int retVal = o1.getParmName().compareTo(o2.getParmName());
            if (retVal == 0) {
                String c1 = o1.getValue().getContent();
                String c2 = o2.getValue().getContent();
                if (c1 == null && c2 == null) return 0;
                if (c1 == null && c2 != null) return 1;
                if (c1 != null && c2 == null) return -1;

                retVal = c1.compareTo(c2);
            }
            return retVal;
        }
    };

    private static final Comparator<Event> EVENT_COMPARATOR = new Comparator<Event>() {

        private int compareStrings(String s1, String s2) {
            if (s1 == null && s2 == null) return 0;
            if (s1 == null && s2 != null) return 1;
            if (s1 != null && s2 == null) return -1;
            return (s1.compareTo(s2));
        }

        @Override
        public int compare(Event e1, Event e2) {
            if (e1 == null && e2 == null) return 0;
            if (e1 == null && e2 != null) return 1;
            if (e1 != null && e2 == null) return -1;

            int retVal = compareStrings(e1.getUei(), e2.getUei());
            if (retVal == 0) {
                retVal = InetAddressUtils.toInteger(e1.getInterfaceAddress()).compareTo(InetAddressUtils.toInteger(e2.getInterfaceAddress()));
            }
            if (retVal == 0) {
                retVal = compareStrings(e1.getService(), e2.getService());
            }
            if (retVal == 0) {
                List<Parm> anticipatedParms = e1.getParmCollection();
                List<Parm> receivedParms = e2.getParmCollection();
                Collections.sort(anticipatedParms, PARM_COMPARATOR);
                Collections.sort(receivedParms, PARM_COMPARATOR);
                if (anticipatedParms.size() != receivedParms.size()) {
                    retVal = Integer.valueOf(anticipatedParms.size()).compareTo(Integer.valueOf(receivedParms.size()));
                }
                if (retVal == 0) {
                    for (int i = 0; i < anticipatedParms.size(); i++) {
                        Parm anticipated = anticipatedParms.get(i);
                        Parm received = receivedParms.get(i);

                        retVal = compareStrings(anticipated.getParmName(), received.getParmName());
                        if (retVal == 0) {
                            retVal = compareStrings(anticipated.getValue().getContent(), received.getValue().getContent());
                        }
                        if (retVal != 0) {
                            break;
                        }
                    }
                }
            }

            return retVal;
        }
    };

    @Override
    public void setTemporaryDatabase(MockDatabase database) {
        m_db = database;
    }

    @Before
    public void setUp() throws Exception {
        BeanUtils.setStaticApplicationContext(m_context);

        // Resets Counters Cache Data
        CollectionResourceWrapper.s_cache.clear();

        MockLogAppender.setupLogging();

        m_fileAnticipator = new FileAnticipator();

        m_resourceStorageDao = new FilesystemResourceStorageDao();
        m_resourceStorageDao.setRrdDirectory(m_fileAnticipator.getTempDir());

        // Use a mock FilterDao that always returns 127.0.0.1 in the active IP list
        FilterDao filterDao = EasyMock.createMock(FilterDao.class);
        EasyMock.expect(filterDao.getActiveIPAddressList((String)EasyMock.anyObject())).andReturn(Collections.singletonList(addr("127.0.0.1"))).anyTimes();
        filterDao.flushActiveIpAddressListCache();
        EasyMock.expectLastCall().anyTimes();
        FilterDaoFactory.setInstance(filterDao);
        EasyMock.replay(filterDao);

        DateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
        final StringBuilder sb = new StringBuilder("<?xml version=\"1.0\"?>");
        sb.append("<outages>");
        sb.append("<outage name=\"junit outage\" type=\"specific\">");
        sb.append("<time begins=\"");
        sb.append(formatter.format(new Date(System.currentTimeMillis() - 3600000)));
        sb.append("\" ends=\"");
        sb.append(formatter.format(new Date(System.currentTimeMillis() + 3600000)));
        sb.append("\"/>");
        sb.append("<interface address=\"match-any\"/>");
        sb.append("</outage>");
        sb.append("</outages>");
        File file = new File("target/poll-outages.xml");
        FileWriter writer = new FileWriter(file);
        writer.write(sb.toString());
        writer.close();
        PollOutagesConfigFactory.setInstance(new PollOutagesConfigFactory(new FileSystemResource(file)));
        PollOutagesConfigFactory.getInstance().afterPropertiesSet();
        initFactories("/threshd-configuration.xml","/test-thresholds.xml");
        m_anticipatedEvents = new ArrayList<>();
    };
    
    private void initFactories(String threshd, String thresholds) throws Exception {
        LOG.info("Initialize Threshold Factories");
        ThresholdingConfigFactory.setInstance(new ThresholdingConfigFactory(getClass().getResourceAsStream(thresholds)));
        ThreshdConfigFactory.setInstance(new ThreshdConfigFactory(getClass().getResourceAsStream(threshd),"127.0.0.1", false));
    }

    @After
    public void tearDown() throws Exception {
        m_fileAnticipator.deleteExpected();
        m_fileAnticipator.tearDown();
        m_anticipatedEvents.clear();
        m_eventIpcManager.reset();
    }

    /*
     * Testing custom ThresholdingSet implementation for in-line Latency thresholds processing (Bug 3448)
     */
    @Test
    @JUnitTemporaryDatabase(tempDbClass=MockDatabase.class)
    public void testBug3488() throws Exception {
        String ipAddress = "127.0.0.1";
        setupSnmpInterfaceDatabase(m_db, ipAddress, null);
        LatencyThresholdingSet thresholdingSet = new LatencyThresholdingSet(1, ipAddress, "HTTP", null, getRepository(), m_resourceStorageDao);
        assertTrue(thresholdingSet.hasThresholds()); // Global Test
        Map<String, Double> attributes = new HashMap<String, Double>();
        attributes.put("http", 200.0);
        assertTrue(thresholdingSet.hasThresholds(attributes)); // Datasource Test

        List<Event> triggerEvents = new ArrayList<>();
        for (int i=0; i<5; i++)
            triggerEvents.addAll(thresholdingSet.applyThresholds("http", attributes));
        assertTrue(triggerEvents.size() == 1);

        addEvent(EventConstants.HIGH_THRESHOLD_EVENT_UEI, "127.0.0.1", "HTTP", 5, 100.0, 50.0, 200.0, IfLabel.NO_IFLABEL, "127.0.0.1[http]", "http", IfLabel.NO_IFLABEL, null, m_eventIpcManager.getEventAnticipator(), m_anticipatedEvents);
        ThresholdingEventProxy proxy = new ThresholdingEventProxy();
        proxy.add(triggerEvents);
        proxy.sendAllEvents();
        verifyEvents(0);
    }

    /*
     * This test uses this files from src/test/resources:
     * - threshd-configuration-bug3575.xml
     * - test-thresholds-bug3575.xml
     */
    @Test
    @JUnitTemporaryDatabase(tempDbClass=MockDatabase.class)
    public void testBug3575() throws Exception {
        initFactories("/threshd-configuration-bug3575.xml","/test-thresholds-bug3575.xml");
        String ipAddress = "127.0.0.1";
        String ifName = "eth0";
        setupSnmpInterfaceDatabase(m_db, ipAddress, ifName);
        LatencyThresholdingSet thresholdingSet = new LatencyThresholdingSet(1, ipAddress, "StrafePing", null, getRepository(), m_resourceStorageDao);
        assertTrue(thresholdingSet.hasThresholds());
        Map<String, Double> attributes = new HashMap<String, Double>();
        for (double i=1; i<21; i++) {
            attributes.put("ping" + i, 2 * i);
        }
        attributes.put("loss", 60.0);
        attributes.put("median", 100.0);
        attributes.put(PollStatus.PROPERTY_RESPONSE_TIME, 100.0);
        assertTrue(thresholdingSet.hasThresholds(attributes));
        List<Event> triggerEvents = thresholdingSet.applyThresholds("StrafePing", attributes);
        assertTrue(triggerEvents.size() == 1);
        addEvent(EventConstants.HIGH_THRESHOLD_EVENT_UEI, "127.0.0.1", "StrafePing", 1, 50.0, 25.0, 60.0, ifName, "127.0.0.1[StrafePing]", "loss", "eth0", null, m_eventIpcManager.getEventAnticipator(), m_anticipatedEvents);
        ThresholdingEventProxy proxy = new ThresholdingEventProxy();
        proxy.add(triggerEvents);
        proxy.sendAllEvents();
        verifyEvents(0);
    }

    /*
     * Testing custom ThresholdingSet implementation for in-line Latency thresholds processing for Pollerd.
     * 
     * This test validate that Bug 1582 has been fixed.
     * ifLabel and ifIndex are set correctly based on Bug 2711
     */
    @Test
    @JUnitTemporaryDatabase(tempDbClass=MockDatabase.class)
    public void testLatencyThresholdingSet() throws Exception {
        Integer ifIndex = 1;
        String ifName = "lo0";
        setupSnmpInterfaceDatabase(m_db, "127.0.0.1", ifName);

        LatencyThresholdingSet thresholdingSet = new LatencyThresholdingSet(1, "127.0.0.1", "HTTP", null, getRepository(), m_resourceStorageDao);
        assertTrue(thresholdingSet.hasThresholds()); // Global Test
        Map<String, Double> attributes = new HashMap<String, Double>();
        attributes.put("http", 90.0);
        assertTrue(thresholdingSet.hasThresholds(attributes)); // Datasource Test
        List<Event> triggerEvents = thresholdingSet.applyThresholds("http", attributes);
        assertTrue(triggerEvents.size() == 0);

        // Test Trigger
        attributes.put("http", 200.0);
        for (int i = 1; i < 5; i++) {
            LOG.debug("testLatencyThresholdingSet: run number {}", i);
            if (thresholdingSet.hasThresholds(attributes)) {
                triggerEvents = thresholdingSet.applyThresholds("http", attributes);
                assertTrue(triggerEvents.size() == 0);
            }
        }
        if (thresholdingSet.hasThresholds(attributes)) {
            LOG.debug("testLatencyThresholdingSet: run number 5");
            triggerEvents = thresholdingSet.applyThresholds("http", attributes);
            assertTrue(triggerEvents.size() == 1);
        }
        
        // Test Rearm
        List<Event> rearmEvents = null;
        if (thresholdingSet.hasThresholds(attributes)) {
            attributes.put("http", 40.0);
            rearmEvents = thresholdingSet.applyThresholds("http", attributes);
            assertTrue(rearmEvents.size() == 1);
        }

        // Validate Events
        addEvent(EventConstants.HIGH_THRESHOLD_EVENT_UEI, "127.0.0.1", "HTTP", 5, 100.0, 50.0, 200.0, ifName, "127.0.0.1[http]", "http", ifName, ifIndex.toString(), m_eventIpcManager.getEventAnticipator(), m_anticipatedEvents);
        addEvent(EventConstants.HIGH_THRESHOLD_REARM_EVENT_UEI, "127.0.0.1", "HTTP", 5, 100.0, 50.0, 40.0, ifName, "127.0.0.1[http]", "http", ifName, ifIndex.toString(), m_eventIpcManager.getEventAnticipator(), m_anticipatedEvents);
        ThresholdingEventProxy proxy = new ThresholdingEventProxy();
        proxy.add(triggerEvents);
        proxy.add(rearmEvents);
        proxy.sendAllEvents();
        verifyEvents(0);
    }

    /*
     * Testing counter reset.
     * When a threshold condition increases the violation count, and before reach the trigger, the value of the variable is on rearm
     * condition, the counter should be reinitialized and should start over again.
     * 
     * This test validate that Bug 1582 has been fixed.
     */
    @Test    
    @JUnitTemporaryDatabase(tempDbClass=MockDatabase.class)
    public void testCounterReset() throws Exception {
        String ifName = "lo0";
        setupSnmpInterfaceDatabase(m_db, "127.0.0.1", ifName);

        LatencyThresholdingSet thresholdingSet = new LatencyThresholdingSet(1, "127.0.0.1", "HTTP", null, getRepository(), m_resourceStorageDao);
        assertTrue(thresholdingSet.hasThresholds()); // Global Test
        Map<String, Double> attributes = new HashMap<String, Double>();
        attributes.put("http", 90.0);
        assertTrue(thresholdingSet.hasThresholds(attributes)); // Datasource Test
        List<Event> triggerEvents = thresholdingSet.applyThresholds("http", attributes);
        assertTrue(triggerEvents.size() == 0);

        // Testing trigger the threshold 3 times
        attributes.put("http", 200.0);
        for (int i = 1; i <= 3; i++) {
            LOG.debug("testLatencyThresholdingSet: ------------------------------------ trigger number {}", i);
            if (thresholdingSet.hasThresholds(attributes)) {
                triggerEvents = thresholdingSet.applyThresholds("http", attributes);
                assertTrue(triggerEvents.size() == 0);
            }
        }
        assertTrue(triggerEvents.size() == 0);
        
        // This should reset the counter
        attributes.put("http", 40.0);
        LOG.debug("testLatencyThresholdingSet: ------------------------------------ reseting counter");
        triggerEvents = thresholdingSet.applyThresholds("http", attributes);

        // Increase the counter again two times, no threshold should be generated
        attributes.put("http", 300.0);
        for (int i = 4; i <= 5; i++) {
            LOG.debug("testLatencyThresholdingSet: ------------------------------------ trigger number {}", i);
            if (thresholdingSet.hasThresholds(attributes)) {
                triggerEvents = thresholdingSet.applyThresholds("http", attributes);
                assertTrue(triggerEvents.size() == 0);
            }
        }
        
        // Increase 3 more times and now, the threshold event should be triggered.
        for (int i = 6; i <= 8; i++) {
            LOG.debug("testLatencyThresholdingSet: ------------------------------------ trigger number {}", i);
            if (thresholdingSet.hasThresholds(attributes)) {
                triggerEvents = thresholdingSet.applyThresholds("http", attributes);
                if (i < 8)
                    assertTrue(triggerEvents.size() == 0);
            }
        }
        
        assertTrue(triggerEvents.size() == 1);
    }

    private RrdRepository getRepository() {
        RrdRepository repo = new RrdRepository();
        repo.setRrdBaseDir(m_fileAnticipator.getTempDir());
        return repo;
    }

    private static void addEvent(String uei, String ipaddr, String service, Integer trigger, Double threshold, Double rearm, Double value, String label, String instance, String ds, String ifLabel, String ifIndex, EventAnticipator anticipator, List<Event> anticipatedEvents) {
        
        EventBuilder bldr = new EventBuilder(uei, "ThresholdingVisitorTest");
        bldr.setNodeid(1);
        bldr.setInterface(addr(ipaddr));
        bldr.setService(service);

        bldr.addParam("label", label);

        if (ifLabel != null) {
            bldr.addParam("ifLabel", ifLabel);
        }
        
        if (ifIndex != null) {
            bldr.addParam("ifIndex", ifIndex);
        }

        bldr.addParam("ds", ds);
        
        if (value != null) {
            String pattern = System.getProperty("org.opennms.threshd.value.decimalformat", "###.##"); // See Bug 3427
            DecimalFormat valueFormatter = new DecimalFormat(pattern);
            bldr.addParam("value", value.isNaN() ? AbstractThresholdEvaluatorState.FORMATED_NAN : valueFormatter.format(value));
        }

        bldr.addParam("instance", instance);

        bldr.addParam("trigger", trigger);

        if (threshold != null) {
            bldr.addParam("threshold", threshold);
        }

        if (rearm != null) {
            bldr.addParam("rearm", rearm);
        }

        Event event = bldr.getEvent();
        anticipator.anticipateEvent(event, true);
        anticipatedEvents.add(event);
    }

    private void verifyEvents(int remainEvents) {
        if (remainEvents == 0) {
            List<Event> receivedList = new ArrayList<>(m_eventIpcManager.getEventAnticipator().getAnticipatedEventsReceived());
            
            Collections.sort(receivedList, EVENT_COMPARATOR);
            Collections.sort(m_anticipatedEvents, EVENT_COMPARATOR);
            LOG.info("verifyEvents: Anticipated={}, Received= {}", receivedList.size(), m_anticipatedEvents.size());
            if (m_anticipatedEvents.size() != receivedList.size()) {
                for (Event e : m_anticipatedEvents) {
                    System.err.println("expected event " + e.getUei() + ": " + e.getDescr());
                }
                System.err.println("anticipated = " + m_anticipatedEvents + "\nreceived = " + receivedList);
                fail("Anticipated event count (" + m_anticipatedEvents.size() + ") is different from received event count (" + receivedList.size() + ").");
            }
            for (int i = 0; i < m_anticipatedEvents.size(); i++) {
                LOG.info("verifyEvents: processing event {}", (i+1));
                compareEvents(m_anticipatedEvents.get(i), receivedList.get(i));
            }
        }
        m_eventIpcManager.getEventAnticipator().verifyAnticipated(0, 0, 0, remainEvents, 0);
    }
    
    private static void compareEvents(Event anticipated, Event received) {
        assertEquals("UEIs must match", anticipated.getUei(), received.getUei());
        assertEquals("NodeIDs must match", anticipated.getNodeid(), received.getNodeid());
        assertEquals("interfaces must match", anticipated.getInterface(), received.getInterface());
        assertEquals("services must match", anticipated.getService(), received.getService());
        compareParms(anticipated.getParmCollection(), received.getParmCollection());
    }

    private static void compareParms(List<Parm> anticipatedParms, List<Parm> receivedParms) {
        Collections.sort(anticipatedParms, PARM_COMPARATOR);
        Collections.sort(receivedParms, PARM_COMPARATOR);
        for (Parm source : anticipatedParms) {
            Parm found = null;
            for (Parm p : receivedParms) {
                if (p.getParmName().equals(source.getParmName()))
                    found = p;
            }
            assertNotNull("parameter " + source.getParmName() + " must be found on the received event", found);
            if (source.getValue().getContent() == null) source.getValue().setContent("null");
            assertEquals("content must match for parameter " + source.getParmName(), source.getValue().getContent(), found.getValue().getContent());
        }
    }

    private void setupSnmpInterfaceDatabase(MockDatabase db, String ipAddress, String ifName) throws Exception {
        MockNetwork network = new MockNetwork();
        network.setCriticalService("ICMP");
        network.addNode(1, "testNode");
        network.addInterface(ipAddress);
        network.setIfIndex(1);
        if (ifName != null) {
            network.setIfAlias(ifName);
        }
        network.addService("ICMP");
        network.addService("SNMP");
        network.addService("HTTP");
        network.addPathOutage(1, InetAddressUtils.addr("192.168.1.1"), "ICMP");
        db.populate(network);

        assertEquals(new Integer(1), m_jdbcTemplate.queryForObject("select count(*) from node where nodeid = '1' and nodelabel = 'testNode'", Integer.class));
        assertEquals(new Integer(1), m_jdbcTemplate.queryForObject("select count(*) from ipinterface where nodeid = '1' and ipaddr = '" + ipAddress + "'", Integer.class));
        if (ifName == null) {
            assertEquals(new Integer(1), m_jdbcTemplate.queryForObject("select count(*) from snmpInterface where id = '1' and nodeid = '1' and snmpifIndex = '1'", Integer.class));
        } else {
            assertEquals(new Integer(1), m_jdbcTemplate.queryForObject("select count(*) from snmpInterface where id = '1' and nodeid = '1' and snmpifIndex = '1' and snmpifalias = '" + ifName + "' and snmpifdescr = '" + ifName + "'", Integer.class));
        }
    }
}
