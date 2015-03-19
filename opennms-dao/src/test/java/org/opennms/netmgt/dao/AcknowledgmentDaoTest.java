/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.Date;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.api.AcknowledgmentDao;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.dao.api.EventDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.AckAction;
import org.opennms.netmgt.model.AckType;
import org.opennms.netmgt.model.OnmsAcknowledgment;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests for Acknowledgment DAO
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 *
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/applicationContext-setupIpLike-enabled.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(dirtiesContext=false)
public class AcknowledgmentDaoTest implements InitializingBean {
	@Autowired
    private AcknowledgmentDao m_acknowledgmentDao;
	
	@Autowired
	private DistPollerDao m_distPollerDao;

	@Autowired
	private AlarmDao m_alarmDao;

	@Autowired
	private NodeDao m_nodeDao;

	@Autowired
	private EventDao m_eventDao;

	@Autowired
	private DatabasePopulator m_databasePopulator;
	
    private static boolean m_populated = false;
    

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @BeforeTransaction
    public void setUp() {
        try {
            if (!m_populated) {
                m_databasePopulator.populateDatabase();
            }
        } catch (Throwable e) {
            e.printStackTrace(System.err);
        } finally {
            m_populated = true;
        }
    }

	@Test
	@Transactional
    public void testSaveUnspecified() {
        OnmsAcknowledgment ack = new OnmsAcknowledgment();
        ack.setAckTime(new Date());
        ack.setAckType(AckType.UNSPECIFIED);
        ack.setAckAction(AckAction.UNSPECIFIED);
        ack.setAckUser("not-admin");
        getAcknowledgmentDao().save(ack);
        getAcknowledgmentDao().flush();
        Integer id = new Integer(ack.getId());
        ack = null;
        
        OnmsAcknowledgment ack2 = getAcknowledgmentDao().get(id);
        assertNotNull(ack2);
        assertEquals(id, ack2.getId());
        assertFalse("admin".equals(ack2.getAckUser()));
        assertEquals("not-admin", ack2.getAckUser());
        
    }

    private AcknowledgmentDao getAcknowledgmentDao() {
    	return m_acknowledgmentDao;
	}

	@Test
    @Transactional
    public void testSaveWithAlarm() {
        OnmsEvent event = new OnmsEvent();
        event.setEventLog("Y");
        event.setEventDisplay("Y");
        event.setEventCreateTime(new Date());
        event.setDistPoller(m_distPollerDao.load("localhost"));
        event.setEventTime(new Date());
        event.setEventSeverity(new Integer(7));
        event.setEventUei("uei://org/opennms/test/EventDaoTest");
        event.setEventSource("test");
        m_eventDao.save(event);
        
        OnmsNode node = m_nodeDao.findAll().iterator().next();

        OnmsAlarm alarm = new OnmsAlarm();
        
        alarm.setNode(node);
        alarm.setUei(event.getEventUei());
        alarm.setSeverityId(event.getEventSeverity());
        alarm.setFirstEventTime(event.getEventTime());
        alarm.setLastEvent(event);
        alarm.setCounter(new Integer(1));
        alarm.setDistPoller(m_distPollerDao.load("localhost"));
        alarm.setAlarmAckTime(new Date());
        alarm.setAlarmAckUser("not-admin");
        
        m_alarmDao.save(alarm);
        m_alarmDao.flush();
        
        OnmsAcknowledgment ack = new OnmsAcknowledgment(alarm);
        getAcknowledgmentDao().save(ack);
        Integer ackId = new Integer(ack.getId());
        ack = null;
        
        OnmsAcknowledgment ack2 = getAcknowledgmentDao().get(ackId);
        OnmsAlarm alarm2 = m_alarmDao.get(ack2.getRefId());
        
        assertEquals(ack2.getAckUser(), alarm2.getAlarmAckUser());
        assertEquals(ack2.getAckTime(), alarm2.getAlarmAckTime());
    }
}
