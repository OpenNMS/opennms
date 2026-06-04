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
    // Config reload
    // -------------------------------------------------------------------------

    @Test
    public void configReloadAppliesSyncEnabledToggle() throws Exception {
        HaConfiguration original = primaryConfig();
        original.setSyncEnabled(true);
        original.setPartnerRestUrl("http://partner:8980/opennms");
        HaStartupCoordinator coord = createCoordinator(original, mockDbFactory);

        HaConfiguration updated = copyOf(original);
        updated.setSyncEnabled(false);

        coord.applyConfigReload(updated);

        assertFalse("sync-enabled should have flipped to false", coord.getConfig().isSyncEnabled());
    }

    @Test
    public void configReloadAppliesHeartbeatIntervalChange() throws Exception {
        HaConfiguration original = primaryConfig();
        original.setHeartbeatIntervalSeconds(10);
        original.setFailoverThresholdSeconds(30);
        HaStartupCoordinator coord = createCoordinator(original, mockDbFactory);

        HaConfiguration updated = copyOf(original);
        updated.setHeartbeatIntervalSeconds(15);

        coord.applyConfigReload(updated);

        assertEquals(15, coord.getConfig().getHeartbeatIntervalSeconds());
    }

    @Test
    public void configReloadClampsOutOfRangeValues() throws Exception {
        HaConfiguration original = primaryConfig();
        original.setHeartbeatIntervalSeconds(10);
        original.setFailoverThresholdSeconds(30);
        HaStartupCoordinator coord = createCoordinator(original, mockDbFactory);

        HaConfiguration updated = copyOf(original);
        updated.setHeartbeatIntervalSeconds(1);     // below minimum 5
        updated.setFailoverThresholdSeconds(2);    // below minimum 20

        coord.applyConfigReload(updated);

        assertEquals(HaStartupCoordinator.MIN_HEARTBEAT_INTERVAL_SECONDS, coord.getConfig().getHeartbeatIntervalSeconds());
        assertEquals(HaStartupCoordinator.MIN_FAILOVER_THRESHOLD_SECONDS, coord.getConfig().getFailoverThresholdSeconds());
    }

    @Test
    public void configReloadRejectsRoleChange() throws Exception {
        HaConfiguration original = primaryConfig();
        HaStartupCoordinator coord = createCoordinator(original, mockDbFactory);

        HaConfiguration updated = copyOf(original);
        updated.setRole(HaRole.SECONDARY); // forbidden at runtime

        coord.applyConfigReload(updated);

        assertEquals("role change must be ignored", HaRole.PRIMARY, coord.getConfig().getRole());
    }

    @Test
    public void configReloadRejectsInstanceIdChange() throws Exception {
        HaConfiguration original = primaryConfig();
        HaStartupCoordinator coord = createCoordinator(original, mockDbFactory);

        HaConfiguration updated = copyOf(original);
        updated.setInstanceId("opennms-renamed");

        coord.applyConfigReload(updated);

        assertEquals("instance-id change must be ignored", "opennms-primary", coord.getConfig().getInstanceId());
    }

    @Test
    public void configReloadRejectsEnabledToggle() throws Exception {
        HaConfiguration original = primaryConfig();
        HaStartupCoordinator coord = createCoordinator(original, mockDbFactory);

        HaConfiguration updated = copyOf(original);
        updated.setEnabled(false);

        coord.applyConfigReload(updated);

        assertTrue("enabled toggle must be ignored at runtime", coord.getConfig().isEnabled());
    }

    @Test
    public void configReloadNoOpWhenUnchanged() throws Exception {
        HaConfiguration original = primaryConfig();
        // Use already-valid values so the reload's clamp doesn't introduce a "change"
        original.setHeartbeatIntervalSeconds(10);
        original.setFailoverThresholdSeconds(30);
        HaStartupCoordinator coord = createCoordinator(original, mockDbFactory);
        HaConfiguration before = coord.getConfig();

        coord.applyConfigReload(copyOf(original));

        assertEquals(before.getHeartbeatIntervalSeconds(), coord.getConfig().getHeartbeatIntervalSeconds());
        assertEquals(before.getFailoverThresholdSeconds(), coord.getConfig().getFailoverThresholdSeconds());
        assertEquals(before.isSyncEnabled(), coord.getConfig().isSyncEnabled());
    }

    // -------------------------------------------------------------------------
    // Split-brain detection
    // -------------------------------------------------------------------------

    @Test
    public void splitBrainYieldsWhenOurHeartbeatIsOlder() throws Exception {
        HaConfiguration cfg = primaryConfig();
        HaStartupCoordinator coord = createCoordinator(cfg, mockDbFactory);
        setStaticInstance(coord);
        // Manually place coordinator in ACTIVE state
        setCurrentState(coord, HaInstanceState.ACTIVE);

        // Query returns both instances ACTIVE; our age is larger (disconnected longer) → we yield
        when(mockRs.next()).thenReturn(true, true, false);
        when(mockRs.getString(1))
                .thenReturn("opennms-primary")   // row 1: us
                .thenReturn("opennms-secondary"); // row 2: partner
        when(mockRs.getString(2))
                .thenReturn(HaInstanceState.ACTIVE.name())
                .thenReturn(HaInstanceState.ACTIVE.name());
        when(mockRs.getLong(3))
                .thenReturn(120L) // our age: 120s (disconnected longer)
                .thenReturn(5L);  // partner age: 5s (continuously active)

        coord.checkForSplitBrain();

        assertEquals("should have stepped down to STANDBY", HaInstanceState.STANDBY, coord.getCurrentState());
    }

    @Test
    public void splitBrainContinuesWhenOurHeartbeatIsFresher() throws Exception {
        HaConfiguration cfg = primaryConfig();
        HaStartupCoordinator coord = createCoordinator(cfg, mockDbFactory);
        setStaticInstance(coord);
        setCurrentState(coord, HaInstanceState.ACTIVE);

        // Our age is smaller (continuously active) — we should continue
        when(mockRs.next()).thenReturn(true, true, false);
        when(mockRs.getString(1))
                .thenReturn("opennms-primary")
                .thenReturn("opennms-secondary");
        when(mockRs.getString(2))
                .thenReturn(HaInstanceState.ACTIVE.name())
                .thenReturn(HaInstanceState.ACTIVE.name());
        when(mockRs.getLong(3))
                .thenReturn(5L)    // our age: 5s (continuously active)
                .thenReturn(120L); // partner age: 120s (was disconnected)

        coord.checkForSplitBrain();

        assertEquals("should stay ACTIVE when our heartbeat is fresher", HaInstanceState.ACTIVE, coord.getCurrentState());
    }

    @Test
    public void splitBrainTiebreakerLowerInstanceIdYields() throws Exception {
        // "opennms-primary" < "opennms-secondary" lexicographically → primary yields
        HaConfiguration cfg = primaryConfig();
        HaStartupCoordinator coord = createCoordinator(cfg, mockDbFactory);
        setStaticInstance(coord);
        setCurrentState(coord, HaInstanceState.ACTIVE);

        // Equal ages — tiebreaker: "opennms-primary" < "opennms-secondary" → primary yields
        when(mockRs.next()).thenReturn(true, true, false);
        when(mockRs.getString(1))
                .thenReturn("opennms-primary")
                .thenReturn("opennms-secondary");
        when(mockRs.getString(2))
                .thenReturn(HaInstanceState.ACTIVE.name())
                .thenReturn(HaInstanceState.ACTIVE.name());
        when(mockRs.getLong(3))
                .thenReturn(10L)  // our age
                .thenReturn(10L); // partner age (equal → tiebreaker)

        coord.checkForSplitBrain();

        assertEquals("lower instance-id should yield on tie", HaInstanceState.STANDBY, coord.getCurrentState());
    }

    @Test
    public void splitBrainNoActionWhenPartnerIsStandby() throws Exception {
        HaConfiguration cfg = primaryConfig();
        HaStartupCoordinator coord = createCoordinator(cfg, mockDbFactory);
        setStaticInstance(coord);
        setCurrentState(coord, HaInstanceState.ACTIVE);

        // Partner is STANDBY — normal operation, no split-brain
        when(mockRs.next()).thenReturn(true, true, false);
        when(mockRs.getString(1))
                .thenReturn("opennms-primary")
                .thenReturn("opennms-secondary");
        when(mockRs.getString(2))
                .thenReturn(HaInstanceState.ACTIVE.name())
                .thenReturn(HaInstanceState.STANDBY.name());
        when(mockRs.getLong(3))
                .thenReturn(5L)
                .thenReturn(5L);

        coord.checkForSplitBrain();

        assertEquals("should stay ACTIVE when partner is STANDBY", HaInstanceState.ACTIVE, coord.getCurrentState());
    }

    // -------------------------------------------------------------------------
    // Config clamping
    // -------------------------------------------------------------------------

    @Test
    public void clampConfigLeavesValidValuesUnchanged() {
        HaConfiguration cfg = new HaConfiguration();
        cfg.setHeartbeatIntervalSeconds(HaStartupCoordinator.MIN_HEARTBEAT_INTERVAL_SECONDS);
        cfg.setFailoverThresholdSeconds(HaStartupCoordinator.MIN_FAILOVER_THRESHOLD_SECONDS);

        HaStartupCoordinator.clampConfig(cfg);

        assertEquals(HaStartupCoordinator.MIN_HEARTBEAT_INTERVAL_SECONDS, cfg.getHeartbeatIntervalSeconds());
        assertEquals(HaStartupCoordinator.MIN_FAILOVER_THRESHOLD_SECONDS, cfg.getFailoverThresholdSeconds());
    }

    @Test
    public void clampConfigClampsHeartbeatIntervalBelowMinimum() {
        HaConfiguration cfg = new HaConfiguration();
        cfg.setHeartbeatIntervalSeconds(1);
        cfg.setFailoverThresholdSeconds(HaStartupCoordinator.MIN_FAILOVER_THRESHOLD_SECONDS);

        HaStartupCoordinator.clampConfig(cfg);

        assertEquals(HaStartupCoordinator.MIN_HEARTBEAT_INTERVAL_SECONDS, cfg.getHeartbeatIntervalSeconds());
        assertEquals(HaStartupCoordinator.MIN_FAILOVER_THRESHOLD_SECONDS, cfg.getFailoverThresholdSeconds());
    }

    @Test
    public void clampConfigClampsFailoverThresholdBelowMinimum() {
        HaConfiguration cfg = new HaConfiguration();
        cfg.setHeartbeatIntervalSeconds(HaStartupCoordinator.MIN_HEARTBEAT_INTERVAL_SECONDS);
        cfg.setFailoverThresholdSeconds(10);

        HaStartupCoordinator.clampConfig(cfg);

        assertEquals(HaStartupCoordinator.MIN_HEARTBEAT_INTERVAL_SECONDS, cfg.getHeartbeatIntervalSeconds());
        assertEquals(HaStartupCoordinator.MIN_FAILOVER_THRESHOLD_SECONDS, cfg.getFailoverThresholdSeconds());
    }

    @Test
    public void clampConfigClampsZeroValues() {
        HaConfiguration cfg = new HaConfiguration();
        cfg.setHeartbeatIntervalSeconds(0);
        cfg.setFailoverThresholdSeconds(0);

        HaStartupCoordinator.clampConfig(cfg);

        assertEquals(HaStartupCoordinator.MIN_HEARTBEAT_INTERVAL_SECONDS, cfg.getHeartbeatIntervalSeconds());
        assertEquals(HaStartupCoordinator.MIN_FAILOVER_THRESHOLD_SECONDS, cfg.getFailoverThresholdSeconds());
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
    // PRIMARY mode: SECONDARY is currently ACTIVE (post-failover, degraded start)
    // -------------------------------------------------------------------------

    @Test
    public void primaryEntersDegradedWhenSecondaryIsActive() throws Exception {
        HaConfiguration cfg = primaryConfig();
        cfg.setHeartbeatIntervalSeconds(1);
        cfg.setFailoverThresholdSeconds(5);

        // isPartnerActive(): SECONDARY row exists, state=ACTIVE, fresh heartbeat (1s old)
        // monitorForFailback() first call: SECONDARY has stepped down to STANDBY
        when(mockRs.next()).thenReturn(true);
        when(mockRs.getString(1))
                .thenReturn(HaInstanceState.ACTIVE.name())    // isPartnerActive
                .thenReturn(HaInstanceState.STANDBY.name());  // monitorForFailback
        when(mockRs.getLong(2)).thenReturn(1L);

        HaStartupCoordinator coord = createCoordinator(cfg, mockDbFactory);
        setStaticInstance(coord);

        boolean proceed = coord.doAwaitReadyToStart();

        assertTrue("PRIMARY should proceed after SECONDARY steps down", proceed);
        assertEquals(HaInstanceState.ACTIVE, coord.getCurrentState());
    }

    @Test
    public void primaryEmergesiFromDegradedWhenSecondaryHeartbeatStale() throws Exception {
        HaConfiguration cfg = primaryConfig();
        cfg.setHeartbeatIntervalSeconds(1);
        cfg.setFailoverThresholdSeconds(5);

        // isPartnerActive(): fresh (1s); monitorForFailback(): ACTIVE but stale (60s > threshold 5s)
        when(mockRs.next()).thenReturn(true);
        when(mockRs.getString(1)).thenReturn(HaInstanceState.ACTIVE.name()); // ACTIVE throughout
        when(mockRs.getLong(2))
                .thenReturn(1L)   // isPartnerActive: fresh
                .thenReturn(60L); // monitorForFailback: stale

        HaStartupCoordinator coord = createCoordinator(cfg, mockDbFactory);
        setStaticInstance(coord);

        boolean proceed = coord.doAwaitReadyToStart();

        assertTrue("PRIMARY should proceed when SECONDARY heartbeat goes stale", proceed);
        assertEquals(HaInstanceState.ACTIVE, coord.getCurrentState());
    }

    @Test
    public void primaryShutdownWhileDegraded() throws Exception {
        HaConfiguration cfg = primaryConfig();
        cfg.setHeartbeatIntervalSeconds(1);
        cfg.setFailoverThresholdSeconds(5);

        // SECONDARY stays ACTIVE throughout with fresh heartbeat — PRIMARY remains DEGRADED until shutdown
        when(mockRs.next()).thenReturn(true);
        when(mockRs.getString(1)).thenReturn(HaInstanceState.ACTIVE.name());
        when(mockRs.getLong(2)).thenReturn(1L);

        HaStartupCoordinator coord = createCoordinator(cfg, mockDbFactory);
        setStaticInstance(coord);

        scheduleShutdown(coord, 200);

        boolean proceed = coord.doAwaitReadyToStart();

        assertFalse("PRIMARY should not start when shutdown while DEGRADED", proceed);
    }

    // -------------------------------------------------------------------------
    // SECONDARY mode: healthy PRIMARY
    // -------------------------------------------------------------------------

    @Test
    public void secondaryStaysInStandbyWhenPrimaryIsHealthy() throws Exception {
        HaConfiguration cfg = secondaryConfig();
        // Fresh heartbeat: 5s old, within failover threshold of 5s
        when(mockRs.next()).thenReturn(true);
        when(mockRs.getString(1)).thenReturn(HaInstanceState.ACTIVE.name());
        when(mockRs.getLong(2)).thenReturn(5L);

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

        // Both reads (initial check + anti-flap re-read) return ACTIVE with stale heartbeat (60s > threshold 5s)
        when(mockRs.next()).thenReturn(true);
        when(mockRs.getString(1)).thenReturn(HaInstanceState.ACTIVE.name());
        when(mockRs.getLong(2)).thenReturn(60L);

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

        // First read: stale (60s > threshold 5s); second read (anti-flap): fresh (0s) — stays STANDBY
        when(mockRs.next()).thenReturn(true);
        when(mockRs.getString(1)).thenReturn(HaInstanceState.ACTIVE.name());
        when(mockRs.getLong(2))
                .thenReturn(60L) // first read: stale
                .thenReturn(0L); // second read: just written

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
        // PRIMARY appears healthy (0s age, well within threshold)
        when(mockRs.next()).thenReturn(true);
        when(mockRs.getString(1)).thenReturn(HaInstanceState.ACTIVE.name());
        when(mockRs.getLong(2)).thenReturn(0L);

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

    private static HaConfiguration copyOf(HaConfiguration src) {
        HaConfiguration c = new HaConfiguration();
        c.setEnabled(src.isEnabled());
        c.setInstanceId(src.getInstanceId());
        c.setRole(src.getRole());
        c.setPartnerInstanceId(src.getPartnerInstanceId());
        c.setHeartbeatIntervalSeconds(src.getHeartbeatIntervalSeconds());
        c.setFailoverThresholdSeconds(src.getFailoverThresholdSeconds());
        c.setSyncEnabled(src.isSyncEnabled());
        c.setSyncIntervalSeconds(src.getSyncIntervalSeconds());
        c.setPartnerRestUrl(src.getPartnerRestUrl());
        c.setSyncUsername(src.getSyncUsername());
        c.setSyncPassword(src.getSyncPassword());
        return c;
    }

    private static void setCurrentState(HaStartupCoordinator coord, HaInstanceState state) throws Exception {
        Field field = HaStartupCoordinator.class.getDeclaredField("currentState");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        AtomicReference<HaInstanceState> ref = (AtomicReference<HaInstanceState>) field.get(coord);
        ref.set(state);
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
