/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.dao.api.EventDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsEventParameter;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class EventDaoIT implements InitializingBean {
    @Autowired
    private DistPollerDao m_distPollerDao;
    
    @Autowired
    private EventDao m_eventDao;

    @Autowired
    private NodeDao m_nodeDao;

    @Autowired
    private DatabasePopulator m_databasePopulator;

    private static boolean m_populated = false;

    @BeforeTransaction
    public void setUp() {
        if (!m_populated) {
            m_databasePopulator.populateDatabase();
            m_populated = true;
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

	@Test
	@Transactional
    public void testSave() {
        OnmsEvent event = new OnmsEvent();
        event.setDistPoller(m_distPollerDao.whoami());
        event.setEventCreateTime(new Date());
        event.setEventDescr("event dao test");
        event.setEventHost("localhost");
        event.setEventLog("Y");
        event.setEventDisplay("Y");
        event.setEventLogGroup("event dao test log group");
        event.setEventLogMsg("event dao test log msg");
        event.setEventSeverity(OnmsSeverity.CRITICAL.getId());
        event.setEventSource("EventDaoTest");
        event.setEventTime(new Date());
        event.setEventUei("uei://org/opennms/test/EventDaoTest");
        OnmsNode node = (OnmsNode) m_nodeDao.findAll().iterator().next();
        OnmsIpInterface iface = (OnmsIpInterface)node.getIpInterfaces().iterator().next();
        OnmsMonitoredService service = (OnmsMonitoredService)iface.getMonitoredServices().iterator().next();
        event.setNode(node);
	    event.setServiceType(service.getServiceType());
        OnmsAlarm alarm = new OnmsAlarm();
	    event.setAlarm(alarm);
        event.setIpAddr(iface.getIpAddress());
        event.setEventParameters(Lists.newArrayList(
                new OnmsEventParameter(event, "label", "node", "string"),
                new OnmsEventParameter(event, "ds", "(memAvailReal + memCached) / memTotalReal * 100.0", "string"),
                new OnmsEventParameter(event, "description", "(memAvailReal + memCached) / memTotalReal * 100.0", "string"),
                new OnmsEventParameter(event, "value", "4.7", "string"),
                new OnmsEventParameter(event, "instance", "node", "string"),
                new OnmsEventParameter(event, "instanceLabel", "node", "string"),
                new OnmsEventParameter(event, "resourceId", "node[70].nodeSnmp[]", "string"),
                new OnmsEventParameter(event, "threshold", "5.0", "string"),
                new OnmsEventParameter(event, "trigger", "2", "string"),
                new OnmsEventParameter(event, "rearm", "10.0", "string")));
        m_eventDao.save(event);
       
        OnmsEvent newEvent = m_eventDao.load(event.getId());
        assertEquals("uei://org/opennms/test/EventDaoTest", newEvent.getEventUei());
        assertNotNull(newEvent.getServiceType());
        assertEquals(service.getNodeId(), newEvent.getNode().getId());
        assertEquals(event.getIpAddr(), newEvent.getIpAddr());
        
        System.err.println(JaxbUtils.marshal(event));
    }

    @Test
    @Transactional
    public void testGetEventsAfterDate() {
        List<String> ueiList = new ArrayList<>();
        ueiList.add("uei/1"); // dummy
        ueiList.add("uei/2"); // dummy
        m_eventDao.getEventsAfterDate(ueiList, new Date()); // we just want to ensure that no exception is thrown :)
    }
}
