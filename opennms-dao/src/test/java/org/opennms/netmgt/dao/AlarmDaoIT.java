/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.dao;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.criteria.Alias;
import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.criteria.Order;
import org.opennms.core.criteria.restrictions.EqRestriction;
import org.opennms.core.criteria.restrictions.SqlRestriction.Type;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.dao.api.EventDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.alarm.AlarmSummary;
import org.opennms.netmgt.model.alarm.SituationSummary;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.test.ThrowableAnticipator;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Sets;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
		"classpath:/META-INF/opennms/applicationContext-soa.xml",
		"classpath:/META-INF/opennms/applicationContext-dao.xml",
		"classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
		"classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
		"classpath*:/META-INF/opennms/component-dao.xml",
		"classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
		"classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(dirtiesContext=false)
public class AlarmDaoIT implements InitializingBean {

	@Autowired
	private DistPollerDao m_distPollerDao;

	@Autowired
	private EventDao m_eventDao;

	@Autowired
	private NodeDao m_nodeDao;

	@Autowired
	private AlarmDao m_alarmDao;

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
	public void testActions() {
		OnmsEvent event = new OnmsEvent();
		event.setEventLog("Y");
		event.setEventDisplay("Y");
		event.setEventCreateTime(new Date());
		event.setDistPoller(m_distPollerDao.whoami());
		event.setEventTime(new Date());
		event.setEventSeverity(OnmsSeverity.MAJOR.getId());
		event.setEventUei("uei://org/opennms/test/EventDaoTest");
		event.setEventSource("test");
		m_eventDao.save(event);

		OnmsNode node = m_nodeDao.findAll().iterator().next();

		OnmsAlarm alarm = new OnmsAlarm();

		alarm.setNode(node);
		alarm.setUei(event.getEventUei());
		alarm.setSeverity(OnmsSeverity.get(event.getEventSeverity()));
		alarm.setSeverityId(event.getEventSeverity());
		alarm.setFirstEventTime(event.getEventTime());
		alarm.setLastEvent(event);
		alarm.setCounter(1);
		alarm.setDistPoller(m_distPollerDao.whoami());

		m_alarmDao.save(alarm);

		OnmsAlarm newAlarm = m_alarmDao.load(alarm.getId());
		assertEquals("uei://org/opennms/test/EventDaoTest", newAlarm.getUei());
		assertEquals(alarm.getLastEvent().getId(), newAlarm.getLastEvent().getId());

		assertEquals(OnmsSeverity.MAJOR, newAlarm.getSeverity());

		newAlarm.escalate("admin");
		assertEquals(OnmsSeverity.CRITICAL, newAlarm.getSeverity());

		newAlarm.clear("admin");
		assertEquals(OnmsSeverity.CLEARED, newAlarm.getSeverity());

		newAlarm.unacknowledge("admin");
		assertNull(newAlarm.getAckUser());
		assertNull(newAlarm.getAlarmAckTime());

	}

	private OnmsAlarm createAlarm(OnmsEvent event) {

		OnmsNode node = m_nodeDao.findAll().iterator().next();

		OnmsAlarm alarm = new OnmsAlarm();
		alarm.setNode(node);
		alarm.setUei(event.getEventUei());
		alarm.setSeverityId(event.getEventSeverity());
		alarm.setFirstEventTime(event.getEventTime());
		alarm.setLastEvent(event);
		alarm.setCounter(1);
		alarm.setDistPoller(m_distPollerDao.whoami());

		return  alarm;
	}

	@Test
	@Transactional
	public void testGetNumAlarmsLastHours() {

		OnmsEvent event = new OnmsEvent();
		event.setEventLog("Y");
		event.setEventDisplay("Y");
		event.setEventCreateTime(new Date());
		event.setDistPoller(m_distPollerDao.whoami());
		event.setEventTime(new Date());
		event.setEventSeverity(OnmsSeverity.MAJOR.getId());
		event.setEventUei("uei://org/opennms/test/EventDaoTest");
		event.setEventSource("test");
		m_eventDao.save(event);

		//there exists one populated alarm in setup with time: 2015-07-14 13:45:48
		assertEquals(1, m_alarmDao.findAll().size());

		//verify zero count for last 0 hours
		long alarmCount = m_alarmDao.getNumAlarmsLastHours(0);
		assertEquals(0, alarmCount);

		//saving a new alarm
		OnmsAlarm alarm = createAlarm(event);
		m_alarmDao.save(alarm);

		//saving another alarm
		OnmsAlarm alarm1 = createAlarm(event);
		m_alarmDao.save(alarm1);
		m_alarmDao.flush();

		//verify all count should be 3 after saving two new alarms
		assertEquals(3, m_alarmDao.findAll().size());

		//verify zero count for -1 hours,
		alarmCount = m_alarmDao.getNumAlarmsLastHours(-1);
		assertEquals(0, alarmCount);
		//verify count should be 0 for last 0 hours as per condition
		alarmCount = m_alarmDao.getNumAlarmsLastHours(0);
		assertEquals(0, alarmCount);

		//param hours value one hour
		//verify there should be 2 count for last one hour after saving new alarms
		alarmCount = m_alarmDao.getNumAlarmsLastHours(1);
		assertEquals(2, alarmCount);

		//updating alarm time 61 minutes earlier
		alarm.setFirstEventTime(Date.from(Instant.now().minus(Duration.ofMinutes(61))));
		m_alarmDao.save(alarm);
		m_alarmDao.flush();

		//verify all count should still be 3 after updating alarm event time
		assertEquals(3, m_alarmDao.findAll().size());

		//verify there should be 1 count after updating alarm time, alarm1 event time is same
		alarmCount = m_alarmDao.getNumAlarmsLastHours(1);
		assertEquals(1, alarmCount);

		//verify there should be 2 count for last 10 hours also
		alarmCount = m_alarmDao.getNumAlarmsLastHours(10);
		assertEquals(2, alarmCount);

		//saving another alarm
		OnmsAlarm alarm2 = createAlarm(event);
		alarm2.setFirstEventTime(Date.from(Instant.now().minus(Duration.ofHours(11))));
		m_alarmDao.save(alarm2);

		//verify all count should be 4 after adding new with alarm2 event time 11 hours earlier
		assertEquals(4, m_alarmDao.findAll().size());
		//verify count should be 2 for last 10 hours, as alarm2 event time lies in last 11th hour
		alarmCount = m_alarmDao.getNumAlarmsLastHours(10);
		assertEquals(2, alarmCount);

		//verify count should be 3 for last 11 hours, including 2 alarms for last 10 hours
		alarmCount = m_alarmDao.getNumAlarmsLastHours(11);
		assertEquals(3, alarmCount);
	}

	@Test
	@Transactional
	public void testSave() {
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
		alarm.setCounter(1);
		alarm.setDistPoller(m_distPollerDao.whoami());

		m_alarmDao.save(alarm);
		// It works we're so smart! hehe

		OnmsAlarm newAlarm = m_alarmDao.load(alarm.getId());
		assertEquals("uei://org/opennms/test/EventDaoTest", newAlarm.getUei());
		assertEquals(alarm.getLastEvent().getId(), newAlarm.getLastEvent().getId());

		Collection<OnmsAlarm> alarms;
		Criteria criteria = new Criteria(OnmsAlarm.class);
		criteria.addRestriction(new EqRestriction("node.id", node.getId()));
		alarms = m_alarmDao.findMatching(criteria);
		assertEquals(1, alarms.size());
		newAlarm = alarms.iterator().next();
		assertEquals("uei://org/opennms/test/EventDaoTest", newAlarm.getUei());
		assertEquals(alarm.getLastEvent().getId(), newAlarm.getLastEvent().getId());
	}

	@Test
	@Transactional
	public void testAlarmSummary() {
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
		alarm.setCounter(1);
		alarm.setDistPoller(m_distPollerDao.whoami());

		m_alarmDao.save(alarm);

		List<AlarmSummary> summary = m_alarmDao.getNodeAlarmSummaries();
		Assert.assertNotNull(summary);
		Assert.assertEquals(1, summary.size());
		AlarmSummary sum = summary.get(0);
		Assert.assertEquals(node.getLabel(), sum.getNodeLabel());
		Assert.assertEquals(alarm.getSeverity().getId(), sum.getMaxSeverity().getId());
		Assert.assertNotSame("N/A", sum.getFuzzyTimeDown());
	}

	@Test
	@Transactional
	public void testSituationSummary() {
		final OnmsEvent event = new OnmsEvent();
		event.setEventLog("Y");
		event.setEventDisplay("Y");
		event.setEventCreateTime(new Date());
		event.setDistPoller(m_distPollerDao.whoami());
		event.setEventTime(new Date());
		event.setEventSeverity(OnmsSeverity.CRITICAL.getId());
		event.setEventUei("uei://org/opennms/test/EventDaoTest");
		event.setEventSource("test");
		m_eventDao.save(event);

		final OnmsNode node = m_nodeDao.findAll().iterator().next();

		final OnmsAlarm alarm1 = new OnmsAlarm();
		alarm1.setNode(node);
		alarm1.setUei(event.getEventUei());
		alarm1.setSeverityId(event.getEventSeverity());
		alarm1.setFirstEventTime(event.getEventTime());
		alarm1.setLastEvent(event);
		alarm1.setCounter(1);
		alarm1.setDistPoller(m_distPollerDao.whoami());
		m_alarmDao.save(alarm1);

		final OnmsAlarm alarm2 = new OnmsAlarm();
		alarm2.setNode(node);
		alarm2.setUei(event.getEventUei());
		alarm2.setSeverityId(event.getEventSeverity());
		alarm2.setFirstEventTime(event.getEventTime());
		alarm2.setLastEvent(event);
		alarm2.setCounter(1);
		alarm2.setDistPoller(m_distPollerDao.whoami());
		alarm2.setRelatedAlarms(Sets.newHashSet(alarm1));
		m_alarmDao.save(alarm2);

		m_alarmDao.findAll();

		final List<SituationSummary> summaries = m_alarmDao.getSituationSummaries();
		Assert.assertNotNull(summaries);
		Assert.assertEquals(1, summaries.size());
		Assert.assertEquals(OnmsSeverity.CRITICAL, summaries.get(0).getSituationSeverity());
		Assert.assertEquals(1, summaries.get(0).getRelatedAlarms());
	}

	@Test
	@Transactional
	public void testSituationSeverities() {
		final OnmsEvent event = new OnmsEvent();
		event.setEventLog("Y");
		event.setEventDisplay("Y");
		event.setEventCreateTime(new Date());
		event.setDistPoller(m_distPollerDao.whoami());
		event.setEventTime(new Date());
		event.setEventSeverity(OnmsSeverity.CRITICAL.getId());
		event.setEventUei("uei://org/opennms/test/EventDaoTest");
		event.setEventSource("test");
		m_eventDao.save(event);

		final OnmsNode node = m_nodeDao.findAll().iterator().next();

		final OnmsAlarm alarm1 = new OnmsAlarm();
		alarm1.setNode(node);
		alarm1.setUei(event.getEventUei());
		alarm1.setSeverityId(event.getEventSeverity());
		alarm1.setFirstEventTime(event.getEventTime());
		alarm1.setLastEvent(event);
		alarm1.setCounter(1);
		alarm1.setDistPoller(m_distPollerDao.whoami());
		m_alarmDao.save(alarm1);

		for(OnmsSeverity onmsSeverity : OnmsSeverity.values()) {
			final OnmsAlarm situation = new OnmsAlarm();
			situation.setNode(node);
			situation.setUei(event.getEventUei());
			situation.setSeverityId(onmsSeverity.getId());
			situation.setFirstEventTime(event.getEventTime());
			situation.setLastEvent(event);
			situation.setCounter(1);
			situation.setDistPoller(m_distPollerDao.whoami());
			situation.setRelatedAlarms(Sets.newHashSet(alarm1));
			m_alarmDao.save(situation);
		}

		m_alarmDao.findAll();

		final List<SituationSummary> summaries = m_alarmDao.getSituationSummaries();
		Assert.assertNotNull(summaries);
		Assert.assertEquals(4, summaries.size());
		Assert.assertEquals(1, summaries.stream().filter(s -> s.getSituationSeverity().equals(OnmsSeverity.CRITICAL)).count());
		Assert.assertEquals(1, summaries.stream().filter(s -> s.getSituationSeverity().equals(OnmsSeverity.MAJOR)).count());
		Assert.assertEquals(1, summaries.stream().filter(s -> s.getSituationSeverity().equals(OnmsSeverity.MINOR)).count());
		Assert.assertEquals(1, summaries.stream().filter(s -> s.getSituationSeverity().equals(OnmsSeverity.WARNING)).count());
		Assert.assertEquals(0, summaries.stream().filter(s -> s.getSituationSeverity().equals(OnmsSeverity.NORMAL)).count());
		Assert.assertEquals(0, summaries.stream().filter(s -> s.getSituationSeverity().equals(OnmsSeverity.CLEARED)).count());
		Assert.assertEquals(0, summaries.stream().filter(s -> s.getSituationSeverity().equals(OnmsSeverity.INDETERMINATE)).count());
		Assert.assertEquals(1, summaries.get(0).getRelatedAlarms());

		// verify the total number of situations
		assertThat(m_alarmDao.getNumSituations(), equalTo(7L));
	}

	@Test
	@Transactional
	public void testAlarmSummary_WithEmptyNodeIdsArray() {
		List<AlarmSummary> summary = m_alarmDao.getNodeAlarmSummaries();
		Assert.assertNotNull(summary); // the result does not really matter, as long as we get a result
		summary = null;
		summary = m_alarmDao.getNodeAlarmSummaries();
		Assert.assertNotNull(summary);
	}

	@Test
	@Transactional
	public void testAlarmSummary_AlarmWithNoEvent() {
		OnmsNode node = m_nodeDao.findAll().iterator().next();

		OnmsAlarm alarm = new OnmsAlarm();
		alarm.setNode(node);
		alarm.setUei("uei://org/opennms/test/badAlarmTest");
		alarm.setSeverityId(new Integer(7));
		alarm.setCounter(1);
		alarm.setDistPoller(m_distPollerDao.whoami());

		m_alarmDao.save(alarm);

		List<AlarmSummary> summary = m_alarmDao.getNodeAlarmSummaries();
		Assert.assertNotNull(summary);
		Assert.assertEquals(1, summary.size());
		AlarmSummary sum = summary.get(0);
		Assert.assertEquals(node.getLabel(), sum.getNodeLabel());
		Assert.assertEquals(alarm.getSeverity().getId(), sum.getMaxSeverity().getId());
		Assert.assertEquals("N/A", sum.getFuzzyTimeDown());
	}

	@Test
	@Transactional
	public void testSortOnNodeLabel() {
		Criteria criteria = new Criteria(OnmsAlarm.class);
		criteria.setAliases(Arrays.asList(new Alias[] {
				new Alias("node", "node", JoinType.LEFT_JOIN)
		}));
		criteria.setOrders(Arrays.asList(new Order[] {
				Order.asc("node.label")
		}));
		m_alarmDao.findMatching(criteria);
	}

	/**
	 * @see https://issues.opennms.org/browse/NMS-9480
	 */
	@Test
	@Transactional
	public void testParameterizedSql() {
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
		alarm.setCounter(1);
		alarm.setDistPoller(m_distPollerDao.whoami());

		m_alarmDao.save(alarm);

		CriteriaBuilder cb = new CriteriaBuilder(OnmsAlarm.class);
		cb.sql("{alias}.alarmid in (?)", alarm.getId(), Type.INTEGER);
		List<OnmsAlarm> alarms = m_alarmDao.findMatching(cb.toCriteria());
		assertEquals(alarm.getId(), alarms.get(0).getId());
		assertEquals(event.getEventTime(), alarms.get(0).getFirstEventTime());
		assertEquals(event.getEventUei(), alarms.get(0).getUei());

		cb = new CriteriaBuilder(OnmsAlarm.class);
		cb.sql("{alias}.firsteventtime = ?", event.getEventTime(), Type.TIMESTAMP);
		m_alarmDao.findMatching(cb.toCriteria());
		assertEquals(alarm.getId(), alarms.get(0).getId());
		assertEquals(event.getEventTime(), alarms.get(0).getFirstEventTime());
		assertEquals(event.getEventUei(), alarms.get(0).getUei());

		cb = new CriteriaBuilder(OnmsAlarm.class);
		cb.sql("{alias}.eventuei = ?", event.getEventUei(), Type.STRING);
		m_alarmDao.findMatching(cb.toCriteria());
		assertEquals(alarm.getId(), alarms.get(0).getId());
		assertEquals(event.getEventTime(), alarms.get(0).getFirstEventTime());
		assertEquals(event.getEventUei(), alarms.get(0).getUei());

		cb = new CriteriaBuilder(OnmsAlarm.class);
		cb.sql("{alias}.alarmid = ? and {alias}.eventuei like ?", new Object[] { alarm.getId(), "%uei.opennms.org%" }, new Type[] { Type.INTEGER, Type.STRING });
		m_alarmDao.findMatching(cb.toCriteria());
		assertEquals(alarm.getId(), alarms.get(0).getId());
		assertEquals(event.getEventTime(), alarms.get(0).getFirstEventTime());
		assertEquals(event.getEventUei(), alarms.get(0).getUei());
	}

	@Test
	@Transactional
	public void testAlarmDetails() {
		// Create some alarm without any details
		OnmsNode node = m_nodeDao.findAll().iterator().next();
		OnmsAlarm alarm = new OnmsAlarm();
		alarm.setNode(node);
		alarm.setUei(EventConstants.NODE_DOWN_EVENT_UEI);
		alarm.setSeverityId(OnmsSeverity.CLEARED.getId());
		alarm.setCounter(1);
		alarm.setDistPoller(m_distPollerDao.whoami());
		m_alarmDao.save(alarm);

		// Retrieve it from the database and verify that there are no details
		OnmsAlarm alarmFromDb = m_alarmDao.get(alarm.getId());
		if (alarmFromDb.getDetails() == null) {
			alarmFromDb.setDetails(new HashMap<>());
		}
		assertThat(alarmFromDb.getDetails().entrySet(), empty());

		// Now add some properties
		alarmFromDb.getDetails().put("k1", "v1");
		alarmFromDb.getDetails().put("k2", "v2");
		alarmFromDb.getDetails().put("k3", null);
		m_alarmDao.update(alarmFromDb);
		m_alarmDao.flush();

		// Retrieve it back from the database and verify the properties
		alarmFromDb = m_alarmDao.get(alarm.getId());
		assertThat(alarmFromDb.getDetails().entrySet(), hasSize(3));
		assertThat(alarmFromDb.getDetails().get("k1"), equalTo("v1"));
		assertThat(alarmFromDb.getDetails().get("k2"), equalTo("v2"));
		assertThat(alarmFromDb.getDetails().get("k3"), nullValue());
	}
}
