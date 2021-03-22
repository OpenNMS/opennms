/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.alarmd.drools;

import static com.jayway.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.alarmd.AlarmLifecycleListenerManager;
import org.opennms.netmgt.alarmd.api.AlarmLifecycleListener;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.TroubleTicketState;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.support.TransactionTemplate;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-alarmd.xml",
        "classpath:/applicationContext-test-troubleTicketer.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class DefaultAlarmTicketerServiceIT implements AlarmLifecycleListener {

    @Autowired
    private AlarmDao alarmDao;

    @Autowired
    private DistPollerDao distPollerDao;

    @Autowired
    private AlarmTicketerService alarmTicketerService;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private AlarmLifecycleListenerManager alarmLifecycleListenerManager;

    private AtomicBoolean ticketStateUpdated = new AtomicBoolean(false);

    private TroubleTicketState troubleTicketState = null;

    @Test
    public void canUpdateLastAutomationTime() {


        alarmLifecycleListenerManager.onListenerRegistered(this, Collections.emptyMap());
        // Create some alarm
        OnmsAlarm a1 = transactionTemplate.execute(status -> {
            OnmsAlarm alarm = new OnmsAlarm();
            alarm.setDistPoller(distPollerDao.whoami());
            alarm.setCounter(1);
            alarm.setUei("linkDown");
            alarm.setLastAutomationTime(new Date(0));
            alarmDao.saveOrUpdate(alarm);
            return alarm;
        });

        // Use the ticketer service to trigger a create ticket event for the alarm, which should
        // also update the last automation time
        Date now = new Date();
        alarmTicketerService.createTicket(a1, now);
        // Verifies that ticket state changes gets notified.
        await().atMost(10, TimeUnit.SECONDS).untilTrue(ticketStateUpdated);
        // clear again.
        ticketStateUpdated.set(false);

        // Use a separate transaction to verify that the last automation time wa updated
        transactionTemplate.execute(status -> {
            final OnmsAlarm alarmInTrans = alarmDao.get(a1.getId());
            assertThat(alarmInTrans.getLastAutomationTime().getTime(), equalTo(now.getTime()));
            return null;
        });
        ticketStateUpdated.set(false);

    }


    @Test
    public void testThatUpdateAndCloseTicketsTriggersAlarmNotification() {

        alarmLifecycleListenerManager.onListenerRegistered(this, Collections.emptyMap());
        // Create some alarm
        OnmsAlarm a1 = transactionTemplate.execute(status -> {
            OnmsAlarm alarm = new OnmsAlarm();
            alarm.setDistPoller(distPollerDao.whoami());
            alarm.setCounter(1);
            alarm.setUei("linkDown");
            alarm.setLastAutomationTime(new Date(0));
            alarmDao.saveOrUpdate(alarm);
            return alarm;
        });
        Date now = new Date();
        // Set different ticket Id that should be in cancelled state.
        a1.setTTicketId("testId2");
        alarmTicketerService.updateTicket(a1, now);
        // Verifies that ticket state changes gets notified.
        await().atMost(10, TimeUnit.SECONDS).untilTrue(ticketStateUpdated);
        assertEquals(TroubleTicketState.CANCELLED, troubleTicketState);
        // clear again
        ticketStateUpdated.set(false);

        // Close the ticket and verify that ticket state gets notified.
        a1.setSeverity(OnmsSeverity.CLEARED);
        transactionTemplate.execute(status -> {
            alarmDao.saveOrUpdate(a1);
            return a1;
        });
        alarmTicketerService.closeTicket(a1, now);
        await().atMost(10, TimeUnit.SECONDS).untilTrue(ticketStateUpdated);
        assertEquals(TroubleTicketState.CLOSED, troubleTicketState);
        // clear again
        ticketStateUpdated.set(false);

    }

    @Override
    public void handleAlarmSnapshot(List<OnmsAlarm> alarms) {

    }

    @Override
    public void preHandleAlarmSnapshot() {
        // pass
    }

    @Override
    public void postHandleAlarmSnapshot() {
        // pass
    }

    @Override
    public void handleNewOrUpdatedAlarm(OnmsAlarm alarm) {
        if (alarm.getTTicketState() != null) {
            ticketStateUpdated.set(true);
            troubleTicketState = alarm.getTTicketState();
        }
    }

    @Override
    public void handleDeletedAlarm(int alarmId, String reductionKey) {

    }
}
