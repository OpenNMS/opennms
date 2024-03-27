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
package org.opennms.netmgt.alarmd.drools;

import static org.awaitility.Awaitility.await;
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
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
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
