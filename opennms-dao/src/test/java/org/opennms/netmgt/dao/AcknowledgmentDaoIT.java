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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Sets;

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
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(dirtiesContext=false)
public class AcknowledgmentDaoIT implements InitializingBean {
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
        if (!m_populated) {
            m_databasePopulator.populateDatabase();
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
        event.setDistPoller(m_distPollerDao.whoami());
        event.setEventTime(new Date());
        event.setEventSeverity(OnmsSeverity.CRITICAL.getId());
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
        alarm.setDistPoller(m_distPollerDao.whoami());
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

    @Test
    public void clearingSituationClearsRelatedAlarms() {

        OnmsAlarm alarm1 = new OnmsAlarm();
        alarm1.setId(1);
        alarm1.setAlarmType(1);
        alarm1.setUei("uei://org/opennms/test/EventDaoTest");
        alarm1.setSeverity(OnmsSeverity.WARNING);
        alarm1.setReductionKey("n1:oops1");
        alarm1.setLastEventTime(new Date(1000));
        alarm1.setCounter(new Integer(1));
        alarm1.setDistPoller(m_distPollerDao.whoami());
        m_alarmDao.save(alarm1);
        m_alarmDao.flush();

        OnmsAlarm alarm2 = new OnmsAlarm();
        alarm2.setId(2);
        alarm2.setAlarmType(1);
        alarm2.setUei("uei://org/opennms/test/EventDaoTest");
        alarm2.setSeverity(OnmsSeverity.WARNING);
        alarm2.setReductionKey("n1:oops2");
        alarm2.setLastEventTime(new Date(1000));
        alarm2.setCounter(new Integer(1));
        alarm2.setDistPoller(m_distPollerDao.whoami());
        m_alarmDao.save(alarm2);
        m_alarmDao.flush();

        OnmsAlarm situation = new OnmsAlarm();
        situation.setId(3);
        situation.setAlarmType(1);
        situation.setUei("uei://org/opennms/test/EventDaoTest");
        situation.setSeverity(OnmsSeverity.CRITICAL);
        situation.setReductionKey("n1:situation");
        situation.setLastEventTime(new Date(2000));
        situation.setRelatedAlarms(Sets.newHashSet(alarm1, alarm2));
        situation.setCounter(new Integer(1));
        situation.setDistPoller(m_distPollerDao.whoami());
        m_alarmDao.save(situation);
        m_alarmDao.flush();

        OnmsAcknowledgment clear = new OnmsAcknowledgment(situation);
        clear.setAckAction(AckAction.CLEAR);
        getAcknowledgmentDao().processAck(clear);

        // The situation should be cleared
        OnmsAlarm retrievedSituation = m_alarmDao.get(situation.getId());
        assertThat(retrievedSituation.getSeverity(), equalTo(OnmsSeverity.CLEARED));
        // Both related alarms should be cleared
        OnmsAlarm retrieved1 = m_alarmDao.get(situation.getId());
        assertThat(retrieved1.getSeverity(), equalTo(OnmsSeverity.CLEARED));
        OnmsAlarm retrieved2 = m_alarmDao.get(situation.getId());
        assertThat(retrieved2.getSeverity(), equalTo(OnmsSeverity.CLEARED));

    }

    @Test
    @Transactional
    public void testFindLatestAcks() {
        assertThat(m_acknowledgmentDao.findLatestAcks(), hasSize(0));

        int numAcks = 100;
        Map<Integer, Integer> dbIdsByRefId = new HashMap<>();
        dbIdsByRefId.put(1, generateAcks(1, numAcks));
        dbIdsByRefId.put(2, generateAcks(2, numAcks));
        dbIdsByRefId.put(3, generateAcks(3, numAcks));
        assertThat(m_acknowledgmentDao.findAll(), hasSize(greaterThan(3)));

        List<OnmsAcknowledgment> acks = m_acknowledgmentDao.findLatestAcks();        
        assertThat(acks, hasSize(3));
        // Check that we got an ack back for each of the refIds we expect
        assertThat(acks.stream()
                .map(OnmsAcknowledgment::getRefId)
                .collect(Collectors.toList()), containsInAnyOrder(1, 2, 3));

        // Check that the ackTimes are the latest ones
        for (OnmsAcknowledgment ack : acks) {
            assertThat(ack.getAckTime(), equalTo(new Date(numAcks)));
        }
        
        // Check that the Ids are the latest ones given that we had ties for ackTime
        for (OnmsAcknowledgment ack : acks) {
            assertThat(ack.getId(), equalTo(dbIdsByRefId.get(ack.getRefId())));
        }
        
        // Check that we get consistent results with finding the acks the hard way with separate queries
        List<OnmsAcknowledgment> acksTheHardWay = findAcksTheHardWay(Arrays.asList(1, 2, 3));
        assertThat(acks, equalTo(acksTheHardWay));
    }
    
    @Test
    @Transactional
    public void testFindLatestAckForRefId() {
        int numAcks = 100;
        Integer latestIdFor1 = generateAcks(1, 100);
        generateAcks(2, 100);
        assertThat(m_acknowledgmentDao.findAll(), hasSize(greaterThan(3)));

        //noinspection OptionalGetWithoutIsPresent
        OnmsAcknowledgment ack = m_acknowledgmentDao.findLatestAckForRefId(1).get();
        // Check to make sure the ack has the latest time and the right Id since we purposely created a tie
        assertThat(ack.getAckTime(), equalTo(new Date(numAcks)));
        assertThat(ack.getId(), equalTo(latestIdFor1));
    }

    /**
     * Generate acks where some of the acks share the same ackTime.
     */
    private Integer generateAcks(Integer refId, int numAcks) {        
        OnmsAcknowledgment sameTimeAck = new OnmsAcknowledgment();
        sameTimeAck.setRefId(refId);
        sameTimeAck.setAckTime(new Date(numAcks));
        m_acknowledgmentDao.save(sameTimeAck);
        
        IntStream.range(3, numAcks + 1).forEach(i -> {
            OnmsAcknowledgment ack = new OnmsAcknowledgment();
            ack.setRefId(refId);
            ack.setAckTime(new Date(i));
            m_acknowledgmentDao.save(ack);
        });

        sameTimeAck = new OnmsAcknowledgment();
        sameTimeAck.setRefId(refId);
        sameTimeAck.setAckTime(new Date(numAcks));
        int id = m_acknowledgmentDao.save(sameTimeAck);
        

        m_acknowledgmentDao.flush();
        
        return id;
    }

    /**
     * Find acks one by one with separate queries.
     */
    private List<OnmsAcknowledgment> findAcksTheHardWay(List<Integer> refIds){
        List<OnmsAcknowledgment> foundAcks = new ArrayList<>();

        for (Integer refId : refIds) {
            //noinspection OptionalGetWithoutIsPresent
            foundAcks.add(m_acknowledgmentDao.findLatestAckForRefId(refId).get());
        }

        return foundAcks;
    }
}
