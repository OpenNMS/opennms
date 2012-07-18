/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.ticketd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.api.integration.ticketing.Plugin;
import org.opennms.api.integration.ticketing.Ticket;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.utils.BeanUtils;
import org.opennms.netmgt.dao.AlarmDao;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.db.JUnitConfigurationEnvironment;
import org.opennms.netmgt.dao.db.JUnitTemporaryDatabase;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.TroubleTicketState;
import org.opennms.test.DaoTestConfigBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

/**
 * NOTE: This class relies on the system property "opennms.ticketer.plugin" being set to
 * {@link TestTicketerPlugin}. Currently, this is done inside {@link DaoTestConfigBean}
 * which is invoked by the OpenNMSConfigurationExecutionListener.
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
		"classpath:/META-INF/opennms/applicationContext-soa.xml",
		"classpath:/META-INF/opennms/applicationContext-dao.xml",
		"classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/applicationContext-setupIpLike-enabled.xml",
		"classpath:/META-INF/opennms/mockEventIpcManager.xml",
		"classpath:/META-INF/opennms/applicationContext-troubleTicketer.xml",
		"classpath:/org/opennms/netmgt/ticketd/applicationContext-configOverride.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment(systemProperties={
        // Set opennms.ticketer.plugin to a value for unit testing
        "opennms.ticketer.plugin=org.opennms.netmgt.ticketd.DefaultTicketerServiceLayerIntegrationTest.TestTicketerPlugin"
})
@JUnitTemporaryDatabase
public class DefaultTicketerServiceLayerIntegrationTest implements InitializingBean {
	@Autowired
	private TicketerServiceLayer m_ticketerServiceLayer;

	@Autowired
	private TestTicketerPlugin m_ticketerPlugin;
	
	@Autowired
	private AlarmDao m_alarmDao;

	@Autowired
	private DatabasePopulator m_databasePopulator;

	@Override
	public void afterPropertiesSet() throws Exception {
	    BeanUtils.assertAutowiring(this);
	}

	@Before
	public void setUp() {
		m_databasePopulator.populateDatabase();
	}

	@Test
	@Transactional
	public void testWire() {
		assertNotNull(m_ticketerServiceLayer);
		assertNotNull(m_ticketerPlugin);

		OnmsAlarm alarm = m_alarmDao.findAll().get(0);
		assertNull(alarm.getTTicketState());
		assertNull(alarm.getTTicketId());

		final int alarmId = alarm.getId();

		m_ticketerServiceLayer.createTicketForAlarm(alarmId);

		m_alarmDao.flush();

		alarm = m_alarmDao.get(alarmId);
		assertEquals(TroubleTicketState.OPEN, alarm.getTTicketState());
		assertNotNull(alarm.getTTicketId());
		assertEquals("testId", alarm.getTTicketId());

		m_ticketerServiceLayer.updateTicketForAlarm(alarm.getId(), alarm.getTTicketId());

		m_alarmDao.flush();

		alarm = m_alarmDao.get(alarmId);
		assertEquals(TroubleTicketState.OPEN, alarm.getTTicketState());

		m_ticketerServiceLayer.closeTicketForAlarm(alarmId,
				alarm.getTTicketId());

		m_alarmDao.flush();

		alarm = m_alarmDao.get(alarmId);
		assertEquals(TroubleTicketState.CLOSED, alarm.getTTicketState());

	}

	public static class TestTicketerPlugin implements Plugin {

		public Ticket get(String ticketId) {
			Ticket ticket = new Ticket();
			ticket.setId(ticketId);
			return ticket;
		}

		public void saveOrUpdate(Ticket ticket) {
			ticket.setId("testId");
		}

	}

}
