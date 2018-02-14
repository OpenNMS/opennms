/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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
package org.opennms.netmgt.alarmd.northbounder.drools;

import java.io.File;
import java.net.InetAddress;
import java.util.Date;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.alarmd.api.NorthboundAlarm;
import org.opennms.netmgt.alarmd.northbounder.drools.DroolsNorthbounder;
import org.opennms.netmgt.alarmd.northbounder.drools.DroolsNorthbounderConfigDao;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.EventDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.mock.EventAnticipator;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoringSystem;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.PrimaryType;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

/**
 * The test Class for Vacuumd with Drools.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(dirtiesContext=false)
public class VacuumdIT {

    /** The Constant DELAY. */
    private static final int DELAY = 5000;

    /** The node DAO. */
    @Autowired
    NodeDao m_nodeDao;

    /** The alarm DAO. */
    @Autowired
    AlarmDao m_alarmDao;

    /** The event DAO. */
    @Autowired
    EventDao m_eventDao;

    /** The event proxy. */
    @Autowired
    @Qualifier("eventProxy")
    private EventProxy m_eventProxy;

    /** The application context. */
    @Autowired
    private ApplicationContext m_appContext;

    /** The ancitipator. */
    private EventAnticipator m_ancitipator;

    /** The Drools NBI. */
    private DroolsNorthbounder nbi;

    /** The monitoring location. */
    OnmsMonitoringLocation m_location;

    /** The distributed poller. */
    OnmsDistPoller m_distPoller;

    /** The down alarm. */
    NorthboundAlarm m_down;

    /** The up alarm. */
    NorthboundAlarm m_up;

    /**
     * Sets up the test.
     *
     * @throws Exception the exception
     */
    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging();

        m_ancitipator = ((MockEventIpcManager) m_eventProxy).getEventAnticipator();
        m_location = new OnmsMonitoringLocation("Default", "Default");
        m_distPoller = new OnmsDistPoller("00000000-0000-0000-0000-000000000000");
        m_distPoller.setLabel("localhost");
        m_distPoller.setLocation("Default");
        m_distPoller.setType(OnmsMonitoringSystem.TYPE_OPENNMS);
        OnmsNode node = createNode();
        m_down = new NorthboundAlarm(createDownAlarm(node, new Date()));
        System.err.println(JaxbUtils.marshal(m_down));
        m_up = new NorthboundAlarm(createUpAlarm(node, new Date(m_down.getLastOccurrence().getTime() + DELAY)));
        System.err.println(JaxbUtils.marshal(m_up));

        // Setup the Drools northbounder configuration DAO
        DroolsNorthbounderConfigDao configDao = new DroolsNorthbounderConfigDao();
        configDao.setConfigResource(new FileSystemResource(new File("src/test/resources/etc/vacuumd/drools-northbounder-configuration.xml")));
        configDao.afterPropertiesSet();
        Assert.assertNotNull(configDao.getConfig().getEngine("Vacuumd"));        

        // Setup the northbounder
        nbi = new DroolsNorthbounder(m_appContext, configDao, m_eventProxy, "Vacuumd");
        nbi.afterPropertiesSet();
    }

    /**
     * Shutdown the test.
     *
     * @throws Exception the exception
     */
    @After
    public void shutdown() throws Exception {
        MockLogAppender.assertNoWarningsOrGreater();
    }

    /**
     * Test northbounder.
     *
     * @throws Exception the exception
     */
    @Test
    public void testNorthbounder() throws Exception {
        Event alarmCleared = new Event();
        alarmCleared.setUei("uei.opennms.org/alarms/alarmCleared");
        m_ancitipator.anticipateEvent(alarmCleared);

        Event alarmDeleted = new Event();
        alarmDeleted.setUei("uei.opennms.org/alarms/alarmDeleted");
        m_ancitipator.anticipateEvent(alarmDeleted);
        m_ancitipator.anticipateEvent(alarmDeleted);

        Assert.assertTrue(nbi.accepts(m_down));
        nbi.forwardAlarms(Lists.newArrayList(m_down));
        Thread.sleep(DELAY);

        Assert.assertTrue(nbi.accepts(m_up));
        nbi.forwardAlarms(Lists.newArrayList(m_up));
        Thread.sleep(DELAY);

        nbi.stop();

        m_ancitipator.verifyAnticipated();
    }

    /**
     * Creates the node.
     *
     * @return the node
     * @throws Exception the exception
     */
    @Transactional
    public OnmsNode createNode() throws Exception {
        OnmsNode node = new OnmsNode();
        node.setLocation(m_location);
        node.setForeignSource("Servers-MacOS");
        node.setForeignId("1");
        node.setLabel("my-test-server");
        OnmsSnmpInterface snmpInterface = new OnmsSnmpInterface(node, 1);
        snmpInterface.setIfAlias("Connection to OpenNMS Wifi");
        snmpInterface.setIfDescr("en1");
        snmpInterface.setIfName("en1/0");
        snmpInterface.setPhysAddr("00:00:00:00:00:01");
        InetAddress address = InetAddress.getByName("10.0.1.1");
        OnmsIpInterface onmsIf = new OnmsIpInterface(address, node);
        onmsIf.setSnmpInterface(snmpInterface);
        onmsIf.setIfIndex(1);
        onmsIf.setIpHostName("my-test-server");
        onmsIf.setIsSnmpPrimary(PrimaryType.PRIMARY);
        node.getIpInterfaces().add(onmsIf);
        m_nodeDao.save(node);
        m_nodeDao.flush();
        return node;
    }

    /**
     * Creates the down alarm.
     *
     * @param node the node
     * @param eventTime the event time
     * @return the alarm
     */
    @Transactional
    public OnmsAlarm createDownAlarm(OnmsNode node, Date eventTime) {
        final String uei = "uei.opennms.org/nodes/nodeDown";
        OnmsEvent event = new OnmsEvent();
        event.setEventSource("JUnit");
        event.setDistPoller(m_distPoller);
        event.setNode(node);
        event.setEventUei(uei);
        event.setEventCreateTime(eventTime);
        event.setEventTime(eventTime);
        event.setEventDisplay("Y");
        event.setEventLog("Y");
        event.setEventLogMsg("Node is down");
        event.setEventSeverity(OnmsSeverity.MAJOR.getId());
        event.setSeverityLabel(OnmsSeverity.MAJOR.getLabel());
        m_eventDao.save(event);
        m_eventDao.flush();

        OnmsAlarm alarm = new OnmsAlarm();
        alarm.setDistPoller(m_distPoller);
        alarm.setCounter(1);
        alarm.setLastEvent(event);
        alarm.setLastEventTime(event.getEventTime());        
        alarm.setNode(node);
        alarm.setUei(uei);
        alarm.setAlarmType(1);
        alarm.setSeverity(OnmsSeverity.MAJOR);
        alarm.setReductionKey(uei + "::" + node.getId());
        m_alarmDao.save(alarm);
        m_alarmDao.flush();
        return alarm;
    }

    /**
     * Creates the up alarm.
     *
     * @param node the node
     * @param eventTime the event time
     * @return the alarm
     */
    @Transactional
    public OnmsAlarm createUpAlarm(OnmsNode node, Date eventTime) {
        final String uei = "uei.opennms.org/nodes/nodeUp";
        OnmsEvent event = new OnmsEvent();
        event.setEventSource("JUnit");
        event.setDistPoller(m_distPoller);
        event.setNode(node);
        event.setEventUei(uei);
        event.setEventCreateTime(eventTime);
        event.setEventTime(eventTime);
        event.setEventDisplay("Y");
        event.setEventLog("Y");
        event.setEventLogMsg("Node is Up");
        event.setEventSeverity(OnmsSeverity.NORMAL.getId());
        event.setSeverityLabel(OnmsSeverity.NORMAL.getLabel());
        m_eventDao.save(event);
        m_eventDao.flush();

        OnmsAlarm alarm = new OnmsAlarm();
        alarm.setDistPoller(m_distPoller);
        alarm.setCounter(1);
        alarm.setLastEvent(event);
        alarm.setLastEventTime(event.getEventTime());
        alarm.setNode(node);
        alarm.setUei(uei);
        alarm.setAlarmType(2);
        alarm.setSeverity(OnmsSeverity.NORMAL);
        alarm.setReductionKey(uei + "::" + node.getId());
        alarm.setClearKey("uei.opennms.org/nodes/nodeDown::" + node.getId());
        m_alarmDao.save(alarm);
        m_alarmDao.flush();
        return alarm;
    }

}
