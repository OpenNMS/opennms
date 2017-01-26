/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2015 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.bsm.daemon;

import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.bsm.persistence.api.BusinessService;
import org.opennms.netmgt.bsm.persistence.api.BusinessServiceDao;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.dao.mock.EventAnticipator;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath*:/META-INF/opennms/component-service.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/applicationContext-eventUtil.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-setupIpLike-enabled.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/applicationContext-bsmd.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(reuseDatabase = false)
@Transactional
public class BsmdIT {

    @Autowired
    private DistPollerDao m_distPollerDao;

    @Autowired
    private AlarmDao m_alarmDao;

    @Autowired
    private DatabasePopulator m_databasePopulator;

    @Autowired
    private BusinessServiceDao m_businessServiceDao;

    @Autowired
    private MockEventIpcManager m_eventMgr;

    @Autowired
    private Bsmd m_bsmd;

    private EventAnticipator m_anticipator;

    @Before
    public void setUp() throws Exception {
        BeanUtils.assertAutowiring(this);

        // We don't have a full blown event configuration, so don't validate these during the integration tests
        m_bsmd.setVerifyReductionKeys(false);
        // Replace the default eventIpcManager with our mock
        m_bsmd.setEventIpcManager(m_eventMgr);

        m_databasePopulator.populateDatabase();

        m_anticipator = new EventAnticipator();
        m_eventMgr.setEventAnticipator(m_anticipator);
    }

    @After
    public void tearDown() throws Exception {
        m_bsmd.destroy();
    }

    /**
     * Verifies that the daemon generates events when the operational status
     * of a Business Service is changed.
     *
     * @throws Exception
     */
    @Test
    public void canSendEventsOnOperationalStatusChanged() throws Exception {
        // Create a business service
        BusinessService simpleBs = createSimpleBusinessService();

        // Start the daemon
        m_bsmd.start();

        // Setup expectations
        EventBuilder ebldr = new EventBuilder(EventConstants.BUSINESS_SERVICE_OPERATIONAL_STATUS_CHANGED_UEI, "test");
        ebldr.addParam(EventConstants.PARM_BUSINESS_SERVICE_ID, simpleBs.getId());
        m_anticipator.anticipateEvent(ebldr.getEvent());

        // Create the alarm
        OnmsAlarm alarm = new OnmsAlarm();
        alarm.setUei(EventConstants.NODE_LOST_SERVICE_EVENT_UEI);
        alarm.setSeverity(OnmsSeverity.CRITICAL);
        alarm.setAlarmType(1);
        alarm.setCounter(1);
        alarm.setDistPoller(m_distPollerDao.whoami());
        alarm.setReductionKey(String.format("%s::1:192.168.1.1:ICMP", EventConstants.NODE_LOST_SERVICE_EVENT_UEI));
        m_alarmDao.save(alarm);

        // Send alarm created event
        ebldr = new EventBuilder(EventConstants.ALARM_CREATED_UEI, "test");
        ebldr.addParam(EventConstants.PARM_ALARM_ID, alarm.getId());
        m_bsmd.handleAlarmLifecycleEvents(ebldr.getEvent());

        // Verify expectations
        Collection<Event> stillWaitingFor = m_anticipator.waitForAnticipated(5000);
        assertTrue("Expected events not forthcoming " + stillWaitingFor, stillWaitingFor.isEmpty());
    }

    /**
     * Verifies that a reload of the Bsmd works as expected.
     */
    @Test
    public void verifyReloadBsmd() throws Exception {
        BusinessService businessService1 = createBusinessService("service1");
        m_bsmd.start();
        Assert.assertEquals(OnmsSeverity.NORMAL, m_bsmd.getBusinessServiceStateMachine().getOperationalStatus(businessService1));

        // verify reload of business services works when event is send
        BusinessService businessService2 = createBusinessService("service2");
        Assert.assertNull(m_bsmd.getBusinessServiceStateMachine().getOperationalStatus(businessService2));
        EventBuilder ebldr = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_UEI, "test");
        ebldr.addParam(EventConstants.PARM_DAEMON_NAME, "bsmd");
        m_eventMgr.sendNow(ebldr.getEvent());
        Assert.assertEquals(OnmsSeverity.NORMAL, m_bsmd.getBusinessServiceStateMachine().getOperationalStatus(businessService2));
    }

    private BusinessService createBusinessService(String name) {
        BusinessService bs = new BusinessService();
        bs.setName(name);

        // Grab the first monitored service from node 1
        OnmsMonitoredService ipService = m_databasePopulator.getNode1()
                .getIpInterfaces().iterator().next()
                .getMonitoredServices().iterator().next();
        bs.addIpService(ipService);

        // Persist
        m_businessServiceDao.save(bs);
        m_businessServiceDao.flush();

        return bs;
    }

    private BusinessService createSimpleBusinessService() {
        return createBusinessService("MyBusinessService");
    }
}
