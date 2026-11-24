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
package org.opennms.web.rest.v2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.test.Level;
import org.opennms.core.test.LoggingEvent;
import org.opennms.core.test.MockLogAppender;
import org.opennms.netmgt.config.OnCallScheduleExpander.OnCallNow;
import org.opennms.netmgt.model.usermgmt.OnCallRole;
import org.opennms.netmgt.model.usermgmt.OnCallSchedule;
import org.opennms.netmgt.model.usermgmt.OnCallScheduleTime;
import org.opennms.netmgt.model.usermgmt.UserAccount;
import org.opennms.netmgt.model.usermgmt.UserGroup;

/** The WARN for a stored schedule that cannot be evaluated: once per role and value, then DEBUG. */
public class OnCallRolesRestServiceTest {

    private OnCallRolesRestService m_service;

    @Before
    public void setUp() {
        MockLogAppender.setupLogging(false, "DEBUG");
        m_service = new OnCallRolesRestService();
    }

    /** A role with one monthly schedule on {@code day}. */
    private static OnCallRole role(final String name, final String day) {
        final OnCallRole role = new OnCallRole(name, new UserGroup("Ops"), new UserAccount("alice"));
        final OnCallSchedule schedule = new OnCallSchedule(new UserAccount("bob"), OnCallSchedule.Type.monthly);
        schedule.getTimes().add(new OnCallScheduleTime(day, "08:00:00", "17:00:00"));
        role.getSchedules().add(schedule);
        return role;
    }

    private static List<String> logged(final Level level) {
        return Arrays.stream(MockLogAppender.getEvents())
                .filter(e -> OnCallRolesRestService.class.getName().equals(e.getLoggerName()) && e.getLevel() == level)
                .map(LoggingEvent::getMessage)
                .collect(Collectors.toList());
    }

    @Test
    public void testUnreadableMonthlyDayWarnsOncePerRoleAndValue() {
        final OnCallNow now = m_service.onCallNow(role("warn-once", "x"));
        assertTrue(now.users().isEmpty());
        m_service.onCallNow(role("warn-once", "x"));
        m_service.onCallNow(role("warn-once", "x"));
        assertEquals(1, logged(Level.WARN).size());
        assertTrue(logged(Level.WARN).get(0), logged(Level.WARN).get(0).contains("warn-once"));
        assertTrue(logged(Level.WARN).get(0), logged(Level.WARN).get(0).contains("\"x\""));
        assertEquals(2, logged(Level.DEBUG).size());

        // another value on the same role, and the same value on another role, each warn once more
        m_service.onCallNow(role("warn-once", "y"));
        m_service.onCallNow(role("warn-once-too", "x"));
        m_service.onCallNow(role("warn-once-too", "x"));
        assertEquals(3, logged(Level.WARN).size());
        assertEquals(3, logged(Level.DEBUG).size());
    }

    @Test
    public void testTheWarnedSetIsBounded() {
        m_service.onCallNow(role("bounded-first", "x"));
        for (int i = 0; i < OnCallRolesRestService.MAX_WARNED_SCHEDULES; i++) {
            m_service.onCallNow(role("bounded-" + i, "x"));
        }
        assertEquals(OnCallRolesRestService.MAX_WARNED_SCHEDULES + 1, logged(Level.WARN).size());
        // the first key was dropped to make room, so it warns again
        m_service.onCallNow(role("bounded-first", "x"));
        assertEquals(OnCallRolesRestService.MAX_WARNED_SCHEDULES + 2, logged(Level.WARN).size());
    }

    @Test
    public void testEachServiceRemembersItsOwnWarnings() {
        m_service.onCallNow(role("per-instance", "x"));
        new OnCallRolesRestService().onCallNow(role("per-instance", "x"));
        assertEquals(2, logged(Level.WARN).size());
    }

    @Test
    public void testAReadableScheduleLogsNothing() {
        m_service.onCallNow(role("readable", "15"));
        assertTrue(logged(Level.WARN).isEmpty());
        assertTrue(logged(Level.DEBUG).isEmpty());
    }
}
