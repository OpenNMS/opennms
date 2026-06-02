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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Coordinates HA startup sequencing.
 *
 * <p>Called by {@code Starter} before the main service Invoker runs. If no
 * {@code ha-configuration.xml} is present, or if HA is disabled, this class
 * is a complete no-op and the process starts normally.
 *
 * <p>For a configured-PRIMARY instance this class returns immediately after
 * writing the initial heartbeat and starting the background heartbeat thread.
 *
 * <p>For a configured-SECONDARY instance this class blocks the Starter thread
 * until either:
 * <ul>
 *   <li>The PRIMARY heartbeat is found to be stale beyond the failover
 *       threshold (promotion), or
 *   <li>{@link #shutdown()} is called (process is stopping).
 * </ul>
 *
 * <p>This class is a static singleton so that {@code Manager.stop()} can reach
 * it to trigger a clean shutdown while the Starter thread may be blocked.
 */
public class HaStartupCoordinator {

    private static final Logger LOG = LoggerFactory.getLogger(HaStartupCoordinator.class);

    private static final String CONFIG_FILE = "ha-configuration.xml";

    private static volatile HaStartupCoordinator INSTANCE;

    private final HaConfiguration config;
    private final DbConnectionFactory dbFactory;
    private final CountDownLatch startupGate = new CountDownLatch(1);
    private final AtomicBoolean stopRequested = new AtomicBoolean(false);
    private final AtomicReference<HaInstanceState> currentState = new AtomicReference<>(HaInstanceState.STANDBY);
    private ScheduledExecutorService scheduler;

    private HaStartupCoordinator(HaConfiguration config, DbConnectionFactory dbFactory) {
        this.config = config;
        this.dbFactory = dbFactory;
    }

    /**
     * Loads {@code ha-configuration.xml} from {@code $OPENNMS_HOME/etc/} and
     * initialises the static singleton. Returns {@code null} (and registers a
     * no-op singleton) if the file is absent or HA is disabled.
     */
    public static HaStartupCoordinator load() {
        String opennmsHome = System.getProperty("opennms.home", ".");
        File configFile = new File(opennmsHome, "etc/" + CONFIG_FILE);

        if (!configFile.exists()) {
            LOG.debug("HA config file not found at {}; HA is disabled", configFile.getAbsolutePath());
            INSTANCE = null;
            return null;
        }

        try {
            JAXBContext ctx = JAXBContext.newInstance(HaConfiguration.class);
            Unmarshaller u = ctx.createUnmarshaller();
            HaConfiguration cfg = (HaConfiguration) u.unmarshal(configFile);

            if (!cfg.isEnabled()) {
                LOG.info("HA config found but disabled; starting in standalone mode");
                INSTANCE = null;
                return null;
            }

            if (cfg.getInstanceId() == null || cfg.getRole() == null) {
                LOG.error("HA config is missing required fields (instance-id, role); HA is disabled");
                INSTANCE = null;
                return null;
            }

            DbConnectionFactory dbFactory = DbConnectionFactory.fromSystemProperties();
            INSTANCE = new HaStartupCoordinator(cfg, dbFactory);
            LOG.info("HA enabled: instance-id={}, role={}", cfg.getInstanceId(), cfg.getRole());
            return INSTANCE;

        } catch (Exception e) {
            LOG.error("Failed to load HA configuration from {}; HA is disabled", configFile.getAbsolutePath(), e);
            INSTANCE = null;
            return null;
        }
    }

    public static HaStartupCoordinator getInstance() {
        return INSTANCE;
    }

    /**
     * Blocks the calling thread until this instance is cleared to start all
     * OpenNMS services. No-op when HA is disabled ({@code INSTANCE} is null).
     *
     * @return {@code true} if services should start, {@code false} if the
     *         process should exit cleanly without starting services.
     */
    public static boolean awaitReadyToStart() {
        HaStartupCoordinator coord = INSTANCE;
        if (coord == null) {
            return true;
        }
        return coord.doAwaitReadyToStart();
    }

    /**
     * Signals the coordinator to stop. Safe to call from shutdown hooks or
     * from {@code Manager.stop()}.
     */
    public static void shutdown() {
        HaStartupCoordinator coord = INSTANCE;
        if (coord != null) {
            coord.doShutdown();
        }
    }

    // -------------------------------------------------------------------------
    // Instance methods
    // -------------------------------------------------------------------------

    boolean doAwaitReadyToStart() {
        try {
            writeInitialStatus();
        } catch (Exception e) {
            LOG.error("HA: failed to write initial status to DB; HA is disabled, proceeding with startup", e);
            return true;
        }

        // Two-thread pool: heartbeat/monitor on one thread, config sync on another
        scheduler = Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(r, "ha-coordinator");
            t.setDaemon(true);
            return t;
        });

        if (config.getRole() == HaRole.PRIMARY) {
            scheduler.scheduleAtFixedRate(this::writeHeartbeat,
                    0, config.getHeartbeatIntervalSeconds(), TimeUnit.SECONDS);
            LOG.info("HA: PRIMARY mode — heartbeat thread started, proceeding with service startup");
            return true;
        }

        // SECONDARY: optionally sync config from partner, then monitor PRIMARY heartbeat
        if (config.isSyncEnabled() && config.getPartnerRestUrl() != null) {
            HaConfigSyncer syncer = new HaConfigSyncer(config);
            LOG.info("HA: SECONDARY mode — config sync enabled from {}, interval {}s",
                    config.getPartnerRestUrl(), config.getSyncIntervalSeconds());
            scheduler.scheduleAtFixedRate(syncer::sync,
                    0, config.getSyncIntervalSeconds(), TimeUnit.SECONDS);
        } else {
            LOG.info("HA: SECONDARY mode — config sync disabled (partner-rest-url not set or sync-enabled=false)");
        }

        LOG.info("HA: SECONDARY mode — starting monitor loop, blocking service startup");
        scheduler.scheduleAtFixedRate(this::checkPrimaryHeartbeat,
                config.getHeartbeatIntervalSeconds(), config.getHeartbeatIntervalSeconds(), TimeUnit.SECONDS);

        try {
            startupGate.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        if (stopRequested.get()) {
            LOG.info("HA: shutdown requested while in SECONDARY standby; exiting without starting services");
            return false;
        }

        LOG.info("HA: SECONDARY promoted to ACTIVE — proceeding with service startup");
        // Switch heartbeat thread on for the promoted instance
        scheduler.scheduleAtFixedRate(this::writeHeartbeat,
                0, config.getHeartbeatIntervalSeconds(), TimeUnit.SECONDS);
        return true;
    }

    void doShutdown() {
        stopRequested.set(true);
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
        updateState(HaInstanceState.FAILED);
        startupGate.countDown(); // unblock Starter if waiting
        LOG.info("HA coordinator shut down");
    }

    private void writeInitialStatus() throws Exception {
        String hostname = resolveHostname();
        try (Connection conn = dbFactory.getConnection()) {
            String sql = "INSERT INTO ha_instance_status (instance_id, configured_role, current_state, last_heartbeat, hostname) " +
                         "VALUES (?, ?, ?, ?, ?) " +
                         "ON CONFLICT (instance_id) DO UPDATE SET " +
                         "configured_role = EXCLUDED.configured_role, " +
                         "current_state = EXCLUDED.current_state, " +
                         "last_heartbeat = EXCLUDED.last_heartbeat, " +
                         "hostname = EXCLUDED.hostname";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, config.getInstanceId());
                ps.setString(2, config.getRole().name());
                HaInstanceState initialState = config.getRole() == HaRole.PRIMARY ? HaInstanceState.ACTIVE : HaInstanceState.STANDBY;
                ps.setString(3, initialState.name());
                ps.setTimestamp(4, Timestamp.from(Instant.now()));
                ps.setString(5, hostname);
                ps.executeUpdate();
            }
        }
        currentState.set(config.getRole() == HaRole.PRIMARY ? HaInstanceState.ACTIVE : HaInstanceState.STANDBY);
        LOG.info("HA: wrote initial status: instance={}, role={}, state={}", config.getInstanceId(), config.getRole(), currentState.get());
    }

    private void writeHeartbeat() {
        if (stopRequested.get()) return;
        try (Connection conn = dbFactory.getConnection()) {
            String sql = "UPDATE ha_instance_status SET last_heartbeat = ?, current_state = ? WHERE instance_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setTimestamp(1, Timestamp.from(Instant.now()));
                ps.setString(2, currentState.get().name());
                ps.setString(3, config.getInstanceId());
                ps.executeUpdate();
            }
        } catch (Exception e) {
            LOG.warn("HA: failed to write heartbeat for instance {}", config.getInstanceId(), e);
        }
    }

    private void checkPrimaryHeartbeat() {
        if (stopRequested.get() || startupGate.getCount() == 0) return;
        if (config.getPartnerInstanceId() == null) {
            LOG.warn("HA: no partner-instance-id configured; cannot monitor PRIMARY heartbeat");
            return;
        }

        try {
            Instant primaryHeartbeat = readPartnerHeartbeat();
            if (primaryHeartbeat == null) {
                LOG.warn("HA: no heartbeat row found for partner {}; will retry", config.getPartnerInstanceId());
                return;
            }

            long ageSeconds = Instant.now().getEpochSecond() - primaryHeartbeat.getEpochSecond();
            if (ageSeconds <= config.getFailoverThresholdSeconds()) {
                LOG.debug("HA: PRIMARY heartbeat age {}s is within threshold {}s — staying STANDBY",
                        ageSeconds, config.getFailoverThresholdSeconds());
                return;
            }

            LOG.warn("HA: PRIMARY heartbeat is {}s old (threshold {}s) — verifying before promoting",
                    ageSeconds, config.getFailoverThresholdSeconds());

            // Anti-flap: wait one more interval and re-read
            Thread.sleep(TimeUnit.SECONDS.toMillis(config.getHeartbeatIntervalSeconds()));
            if (stopRequested.get()) return;

            primaryHeartbeat = readPartnerHeartbeat();
            if (primaryHeartbeat != null) {
                ageSeconds = Instant.now().getEpochSecond() - primaryHeartbeat.getEpochSecond();
            }
            if (primaryHeartbeat != null && ageSeconds <= config.getFailoverThresholdSeconds()) {
                LOG.info("HA: PRIMARY heartbeat recovered (age now {}s) — staying STANDBY", ageSeconds);
                return;
            }

            promote();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            LOG.error("HA: error during PRIMARY heartbeat check", e);
        }
    }

    private Instant readPartnerHeartbeat() throws Exception {
        try (Connection conn = dbFactory.getConnection()) {
            String sql = "SELECT last_heartbeat FROM ha_instance_status WHERE instance_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, config.getPartnerInstanceId());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getTimestamp(1).toInstant();
                    }
                }
            }
        }
        return null;
    }

    private void promote() {
        LOG.warn("HA: PRIMARY appears failed — SECONDARY {} is promoting to ACTIVE", config.getInstanceId());
        updateState(HaInstanceState.ACTIVE);
        currentState.set(HaInstanceState.ACTIVE);
        startupGate.countDown();
    }

    private void updateState(HaInstanceState state) {
        try (Connection conn = dbFactory.getConnection()) {
            String sql = "UPDATE ha_instance_status SET current_state = ?, last_heartbeat = ? WHERE instance_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, state.name());
                ps.setTimestamp(2, Timestamp.from(Instant.now()));
                ps.setString(3, config.getInstanceId());
                ps.executeUpdate();
            }
        } catch (Exception e) {
            LOG.error("HA: failed to update state to {} for instance {}", state, config.getInstanceId(), e);
        }
    }

    private static String resolveHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "unknown";
        }
    }

    public HaConfiguration getConfig() {
        return config;
    }

    public HaInstanceState getCurrentState() {
        return currentState.get();
    }
}
