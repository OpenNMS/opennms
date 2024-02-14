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
package org.opennms.smoketest.bsm;

import static org.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;

import org.junit.ClassRule;
import org.junit.Test;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.hibernate.AlarmDaoHibernate;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.opennms.smoketest.utils.DaoUtils;
import org.opennms.smoketest.utils.RestClient;

public class BsmEventsIT {
    @ClassRule
    public static final OpenNMSStack stack = OpenNMSStack.MINIMAL;

    public Event getServiceProblemEvent(final int businessServiceId, final String severity) {
        final EventBuilder eventBuilder = new EventBuilder("uei.opennms.org/bsm/serviceProblem", "test")
                .setParam("businessServiceId", businessServiceId).setSeverity(severity);
        return eventBuilder.getEvent();
    }

    public Event getServiceDeletedEvent(final int businessServiceId) {
        final EventBuilder eventBuilder = new EventBuilder("uei.opennms.org/internal/serviceDeleted", "test")
                .setParam("businessServiceId", businessServiceId);
        return eventBuilder.getEvent();
    }

    @Test
    public void testSeverityChange() throws Exception {
        final RestClient restClient = stack.opennms().getRestClient();
        restClient.sendEvent(getServiceProblemEvent(42, "Warning"));

        final AlarmDao alarmDao = stack.postgres().getDaoFactory().getDao(AlarmDaoHibernate.class);

        final OnmsAlarm onmsAlarm1 = await().atMost(2, MINUTES).pollInterval(10, SECONDS)
                .until(DaoUtils.findMatchingCallable(alarmDao, new CriteriaBuilder(OnmsAlarm.class)
                        .eq("uei", "uei.opennms.org/bsm/serviceProblem")
                        .toCriteria()), notNullValue());

        assertEquals(1L, (long) onmsAlarm1.getCounter());
        assertEquals(OnmsSeverity.WARNING, onmsAlarm1.getSeverity());

        restClient.sendEvent(getServiceProblemEvent(42, "Minor"));

        final OnmsAlarm onmsAlarm2 = await().atMost(2, MINUTES).pollInterval(10, SECONDS)
                .until(DaoUtils.findMatchingCallable(alarmDao, new CriteriaBuilder(OnmsAlarm.class)
                        .eq("uei", "uei.opennms.org/bsm/serviceProblem")
                        .toCriteria()), notNullValue());

        assertEquals(2L, (long) onmsAlarm2.getCounter());
        assertEquals(OnmsSeverity.MINOR, onmsAlarm2.getSeverity());
    }

    @Test
    public void testAlarmCleared() throws Exception {
        final RestClient restClient = stack.opennms().getRestClient();
        restClient.sendEvent(getServiceProblemEvent(42, "Major"));

        final AlarmDao alarmDao = stack.postgres().getDaoFactory().getDao(AlarmDaoHibernate.class);

        final OnmsAlarm onmsAlarm1 = await().atMost(2, MINUTES).pollInterval(10, SECONDS)
                .until(DaoUtils.findMatchingCallable(alarmDao, new CriteriaBuilder(OnmsAlarm.class)
                        .eq("uei", "uei.opennms.org/bsm/serviceProblem")
                        .eq("severity", OnmsSeverity.MAJOR)
                        .toCriteria()), notNullValue());

        assertEquals(OnmsSeverity.MAJOR, onmsAlarm1.getSeverity());

        restClient.sendEvent(getServiceDeletedEvent(42));

        final OnmsAlarm onmsAlarm2 = await().atMost(2, MINUTES).pollInterval(10, SECONDS)
                .until(DaoUtils.findMatchingCallable(alarmDao, new CriteriaBuilder(OnmsAlarm.class)
                        .eq("uei", "uei.opennms.org/bsm/serviceProblem")
                        .eq("id", onmsAlarm1.getId())
                        .toCriteria()), notNullValue());

        assertEquals(onmsAlarm1.getId(), onmsAlarm2.getId());
        assertEquals(OnmsSeverity.CLEARED, onmsAlarm2.getSeverity());
    }
}
