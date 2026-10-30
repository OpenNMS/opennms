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
package org.opennms.netmgt.vmmgr;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.opennms.netmgt.events.api.EventConstants.OPENNMS_STARTED_UEI;
import static org.opennms.netmgt.events.api.EventConstants.OPENNMS_STOPPING_UEI;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.Test;
import org.opennms.netmgt.xml.event.Event;

public class LifecycleEventsTest {

    @Test
    public void previousRunThatStoppedIsClean() throws SQLException {
        assertEquals(LifecycleEvents.CLEAN, LifecycleEvents.previousShutdown(lastEvent(OPENNMS_STOPPING_UEI)));
    }

    @Test
    public void previousRunThatOnlyStartedIsUnclean() throws SQLException {
        assertEquals(LifecycleEvents.UNCLEAN, LifecycleEvents.previousShutdown(lastEvent(OPENNMS_STARTED_UEI)));
    }

    @Test
    public void noLifecycleEventIsUnknown() throws SQLException {
        assertEquals(LifecycleEvents.UNKNOWN, LifecycleEvents.previousShutdown(lastEvent(null)));
    }

    @Test
    public void databaseErrorIsUnknown() throws SQLException {
        final DataSource dataSource = mock(DataSource.class);
        when(dataSource.getConnection()).thenThrow(new SQLException("connection refused"));
        assertEquals(LifecycleEvents.UNKNOWN, LifecycleEvents.previousShutdown(dataSource));
    }

    @Test
    public void startedEventCarriesVersionStartupTimeAndPreviousShutdown() {
        final Event event = LifecycleEvents.startedEvent("37.0.0", 61_400, LifecycleEvents.UNCLEAN);
        assertEquals(OPENNMS_STARTED_UEI, event.getUei());
        assertEquals(LifecycleEvents.SOURCE, event.getSource());
        assertEquals("37.0.0", parm(event, LifecycleEvents.PARM_VERSION));
        assertEquals("61", parm(event, LifecycleEvents.PARM_STARTUP_SECONDS));
        assertEquals(LifecycleEvents.UNCLEAN, parm(event, LifecycleEvents.PARM_PREVIOUS_SHUTDOWN));
    }

    @Test
    public void stoppingEventCarriesVersionUptimeAndReason() {
        final Event event = LifecycleEvents.stoppingEvent("37.0.0", 3_600_000, LifecycleEvents.REASON_SIGNAL);
        assertEquals(OPENNMS_STOPPING_UEI, event.getUei());
        assertEquals("3600", parm(event, LifecycleEvents.PARM_UPTIME_SECONDS));
        assertEquals(LifecycleEvents.REASON_SIGNAL, parm(event, LifecycleEvents.PARM_REASON));
    }

    private static String parm(final Event event, final String name) {
        return event.getParm(name).getValue().getContent();
    }

    private static DataSource lastEvent(final String uei) throws SQLException {
        final ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(uei != null);
        when(resultSet.getString(1)).thenReturn(uei);
        final PreparedStatement statement = mock(PreparedStatement.class);
        when(statement.executeQuery()).thenReturn(resultSet);
        final Connection connection = mock(Connection.class);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        final DataSource dataSource = mock(DataSource.class);
        when(dataSource.getConnection()).thenReturn(connection);
        return dataSource;
    }
}
