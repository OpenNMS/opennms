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

package org.opennms.smoketest.bsm;

import static com.jayway.awaitility.Awaitility.await;
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
