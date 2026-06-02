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
package org.opennms.netmgt.ha;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class HaStartupCoordinatorTest {

    private DbConnectionFactory mockDbFactory;
    private Connection mockConn;
    private PreparedStatement mockPs;
    private ResultSet mockRs;

    @Before
    public void setUp() throws Exception {
        mockDbFactory = mock(DbConnectionFactory.class);
        mockConn = mock(Connection.class);
        mockPs = mock(PreparedStatement.class);
        mockRs = mock(ResultSet.class);

        when(mockDbFactory.getConnection()).thenReturn(mockConn);
        when(mockConn.prepareStatement(anyString())).thenReturn(mockPs);
        when(mockPs.executeQuery()).thenReturn(mockRs);
        when(mockPs.executeUpdate()).thenReturn(1);

        // Clear static instance between tests
        setStaticInstance(null);
    }

    @After
    public void tearDown() throws Exception {
        setStaticInstance(null);
    }

    // -------------------------------------------------------------------------
    // PRIMARY mode
    // -------------------------------------------------------------------------

    @Test
    public void primaryReturnsImmediately() throws Exception {
        HaConfiguration cfg = primaryConfig();
        HaStartupCoordinator coord = createCoordinator(cfg, mockDbFactory);
        setStaticInstance(coord);

        // No partner heartbeat row needed for PRIMARY
        boolean proceed = coord.doAwaitReadyToStart();

        assertTrue("PRIMARY should allow services to start", proceed);
        assertEquals(HaInstanceState.ACTIVE, coord.getCurrentState());
    }

    @Test
    public void primaryWritesHeartbeat() throws Exception {
        HaConfiguration cfg = primaryConfig();
        HaStartupCoordinator coord = createCoordinator(cfg, mockDbFactory);
        setStaticInstance(coord);

        coord.doAwaitReadyToStart();

        // Verify initial DB write happened
        verify(mockConn, atLeastOnce()).prepareStatement(contains("INSERT INTO ha_instance_status"));
        verify(mockPs, atLeastOnce()).executeUpdate();
    }

    // -------------------------------------------------------------------------
    // SECONDARY mode: healthy PRIMARY
    // -------------------------------------------------------------------------

    @Test
    public void secondaryStaysInStandbyWhenPrimaryIsHealthy() throws Exception {
        HaConfiguration cfg = secondaryConfig();
        // Fresh heartbeat: 5 seconds ago, well within 30s threshold
        when(mockRs.next()).thenReturn(true);
        when(mockRs.getTimestamp(1)).thenReturn(Timestamp.from(Instant.now().minusSeconds(5)));

        HaStartupCoordinator coord = createCoordinator(cfg, mockDbFactory);
        setStaticInstance(coord);

        // Trigger shutdown after 200ms to unblock the await
        scheduleShutdown(coord, 200);

        long start = System.currentTimeMillis();
        boolean proceed = coord.doAwaitReadyToStart();
        long elapsed = System.currentTimeMillis() - start;

        assertFalse("SECONDARY should NOT proceed when PRIMARY is healthy", proceed);
        assertTrue("Should have waited before shutdown signal", elapsed >= 100);
    }

    // -------------------------------------------------------------------------
    // SECONDARY mode: stale PRIMARY → promotion
    // -------------------------------------------------------------------------

    @Test
    public void secondaryPromotesWhenPrimaryIsStaleTwice() throws Exception {
        HaConfiguration cfg = secondaryConfig();
        cfg.setHeartbeatIntervalSeconds(1);
        cfg.setFailoverThresholdSeconds(5);

        // Both reads (initial check + anti-flap re-read) return stale heartbeat
        when(mockRs.next()).thenReturn(true);
        when(mockRs.getTimestamp(1)).thenReturn(Timestamp.from(Instant.now().minusSeconds(60)));

        HaStartupCoordinator coord = createCoordinator(cfg, mockDbFactory);
        setStaticInstance(coord);

        boolean proceed = coord.doAwaitReadyToStart();

        assertTrue("SECONDARY should proceed after promotion", proceed);
        assertEquals(HaInstanceState.ACTIVE, coord.getCurrentState());
    }

    @Test
    public void secondaryDoesNotPromoteWhenPrimaryRecoversBetweenChecks() throws Exception {
        HaConfiguration cfg = secondaryConfig();
        cfg.setHeartbeatIntervalSeconds(1);
        cfg.setFailoverThresholdSeconds(5);

        // First read: stale; second read (anti-flap): fresh
        when(mockRs.next()).thenReturn(true);
        when(mockRs.getTimestamp(1))
                .thenReturn(Timestamp.from(Instant.now().minusSeconds(60))) // first read: stale
                .thenReturn(Timestamp.from(Instant.now()));                  // second read: fresh

        HaStartupCoordinator coord = createCoordinator(cfg, mockDbFactory);
        setStaticInstance(coord);

        // Shutdown after 3 seconds to prevent infinite wait
        scheduleShutdown(coord, 3000);

        boolean proceed = coord.doAwaitReadyToStart();

        assertFalse("SECONDARY should NOT promote when PRIMARY recovers between checks", proceed);
        assertEquals(HaInstanceState.STANDBY, coord.getCurrentState());
    }

    // -------------------------------------------------------------------------
    // Shutdown while waiting
    // -------------------------------------------------------------------------

    @Test
    public void shutdownWhileSecondaryWaiting() throws Exception {
        HaConfiguration cfg = secondaryConfig();
        // PRIMARY appears healthy
        when(mockRs.next()).thenReturn(true);
        when(mockRs.getTimestamp(1)).thenReturn(Timestamp.from(Instant.now()));

        HaStartupCoordinator coord = createCoordinator(cfg, mockDbFactory);
        setStaticInstance(coord);

        scheduleShutdown(coord, 100);

        boolean proceed = coord.doAwaitReadyToStart();

        assertFalse("Should return false on shutdown signal", proceed);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static HaConfiguration primaryConfig() {
        HaConfiguration cfg = new HaConfiguration();
        cfg.setEnabled(true);
        cfg.setInstanceId("opennms-primary");
        cfg.setRole(HaRole.PRIMARY);
        cfg.setPartnerInstanceId("opennms-secondary");
        cfg.setHeartbeatIntervalSeconds(1);
        cfg.setFailoverThresholdSeconds(5);
        return cfg;
    }

    private static HaConfiguration secondaryConfig() {
        HaConfiguration cfg = new HaConfiguration();
        cfg.setEnabled(true);
        cfg.setInstanceId("opennms-secondary");
        cfg.setRole(HaRole.SECONDARY);
        cfg.setPartnerInstanceId("opennms-primary");
        cfg.setHeartbeatIntervalSeconds(1);
        cfg.setFailoverThresholdSeconds(5);
        return cfg;
    }

    private static HaStartupCoordinator createCoordinator(HaConfiguration cfg, DbConnectionFactory dbFactory)
            throws Exception {
        Constructor<HaStartupCoordinator> ctor =
                HaStartupCoordinator.class.getDeclaredConstructor(HaConfiguration.class, DbConnectionFactory.class);
        ctor.setAccessible(true);
        return ctor.newInstance(cfg, dbFactory);
    }

    private static void setStaticInstance(HaStartupCoordinator instance) throws Exception {
        Field field = HaStartupCoordinator.class.getDeclaredField("INSTANCE");
        field.setAccessible(true);
        field.set(null, instance);
    }

    private static void scheduleShutdown(HaStartupCoordinator coord, long delayMs) {
        Thread t = new Thread(() -> {
            try {
                Thread.sleep(delayMs);
                coord.doShutdown();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        t.setDaemon(true);
        t.start();
    }
}
