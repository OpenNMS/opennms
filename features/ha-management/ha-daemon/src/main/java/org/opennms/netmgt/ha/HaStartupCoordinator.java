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
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
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

    static final int MIN_HEARTBEAT_INTERVAL_SECONDS = 5;
    static final int MIN_FAILOVER_THRESHOLD_SECONDS = 20;

    /** How often to re-read {@code ha-configuration.xml} from disk while running. */
    static final int CONFIG_RELOAD_INTERVAL_SECONDS = 60;

    private static volatile HaStartupCoordinator INSTANCE;

    /** Mutable: replaced by {@link #applyConfigReload(HaConfiguration)} when the on-disk file changes. */
    private volatile HaConfiguration config;
    private final DbConnectionFactory dbFactory;
    private final CountDownLatch startupGate = new CountDownLatch(1);
    private final AtomicBoolean stopRequested = new AtomicBoolean(false);
    private final AtomicReference<HaInstanceState> currentState = new AtomicReference<>(HaInstanceState.STANDBY);
    private ScheduledExecutorService scheduler;

    // Tracked schedule handles so they can be cancelled and re-scheduled on config reload.
    private volatile ScheduledFuture<?> heartbeatFuture;
    private volatile ScheduledFuture<?> standbyMonitorFuture;
    private volatile ScheduledFuture<?> failbackMonitorFuture;
    private volatile ScheduledFuture<?> syncFuture;
    private volatile ScheduledFuture<?> reloadFuture;

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
        HaConfiguration cfg;
        try {
            cfg = readConfigFromDisk();
        } catch (FileNotFoundException e) {
            LOG.debug("HA config file not found; HA is disabled");
            INSTANCE = null;
            return null;
        } catch (Exception e) {
            LOG.error("Failed to load HA configuration; HA is disabled", e);
            INSTANCE = null;
            return null;
        }

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

        clampConfig(cfg);

        try {
            DbConnectionFactory dbFactory = DbConnectionFactory.fromDatasourcesXml();
            INSTANCE = new HaStartupCoordinator(cfg, dbFactory);
            LOG.info("HA enabled: instance-id={}, role={}", cfg.getInstanceId(), cfg.getRole());
            return INSTANCE;
        } catch (Exception e) {
            LOG.error("Failed to initialize HA coordinator; HA is disabled", e);
            INSTANCE = null;
            return null;
        }
    }

    /**
     * Reads and unmarshals {@code $OPENNMS_HOME/etc/ha-configuration.xml}.
     * Throws {@link FileNotFoundException} if the file is absent.
     */
    private static HaConfiguration readConfigFromDisk() throws Exception {
        String opennmsHome = System.getProperty("opennms.home", ".");
        File configFile = new File(opennmsHome, "etc/" + CONFIG_FILE);
        if (!configFile.exists()) {
            throw new FileNotFoundException(configFile.getAbsolutePath());
        }
        JAXBContext ctx = JAXBContext.newInstance(HaConfiguration.class);
        Unmarshaller u = ctx.createUnmarshaller();
        return (HaConfiguration) u.unmarshal(configFile);
    }

    static void clampConfig(HaConfiguration cfg) {
        if (cfg.getHeartbeatIntervalSeconds() < MIN_HEARTBEAT_INTERVAL_SECONDS) {
            LOG.warn("HA config: heartbeat-interval-seconds={} is below minimum {}; clamping to {}",
                    cfg.getHeartbeatIntervalSeconds(), MIN_HEARTBEAT_INTERVAL_SECONDS, MIN_HEARTBEAT_INTERVAL_SECONDS);
            cfg.setHeartbeatIntervalSeconds(MIN_HEARTBEAT_INTERVAL_SECONDS);
        }
        if (cfg.getFailoverThresholdSeconds() < MIN_FAILOVER_THRESHOLD_SECONDS) {
            LOG.warn("HA config: failover-threshold-seconds={} is below minimum {}; clamping to {}",
                    cfg.getFailoverThresholdSeconds(), MIN_FAILOVER_THRESHOLD_SECONDS, MIN_FAILOVER_THRESHOLD_SECONDS);
            cfg.setFailoverThresholdSeconds(MIN_FAILOVER_THRESHOLD_SECONDS);
        }
    }

    /**
     * Re-reads {@code ha-configuration.xml} from disk and applies any changes.
     * Called periodically by the scheduler, and on demand via REST or the
     * Karaf shell, so on-disk edits take effect without restart. Parse/read
     * errors are logged and the existing config is retained.
     */
    public synchronized void reloadConfig() {
        if (stopRequested.get()) return;

        HaConfiguration newCfg;
        try {
            newCfg = readConfigFromDisk();
        } catch (FileNotFoundException e) {
            LOG.warn("HA: config file no longer present at reload; keeping existing config");
            return;
        } catch (Exception e) {
            LOG.warn("HA: failed to re-read config; keeping existing config", e);
            return;
        }
        applyConfigReload(newCfg);
    }

    /**
     * Replaces {@code etc/ha-configuration.xml} on disk with {@code newCfg}
     * and immediately re-applies it to the running coordinator.
     *
     * <p>Validates that the immutable identity fields ({@code enabled},
     * {@code instance-id}, {@code role}) match the running configuration before
     * writing — these cannot be changed at runtime. Out-of-range values are
     * clamped to the same minimums applied by {@link #clampConfig(HaConfiguration)}.
     *
     * <p>The write is atomic (write-then-move) so a partial/corrupt file is
     * never visible to a concurrent reader. After the write succeeds, an
     * immediate {@link #reloadConfig()} is invoked so callers don't have to.
     *
     * @throws IllegalArgumentException if any immutable field differs from the
     *         running configuration
     * @throws IOException if marshalling or the file write fails
     */
    public synchronized void writeConfig(HaConfiguration newCfg) throws IOException {
        HaConfiguration current = this.config;

        if (newCfg.isEnabled() != current.isEnabled()) {
            throw new IllegalArgumentException(
                    "'enabled' cannot be changed at runtime (requires restart)");
        }
        if (!Objects.equals(newCfg.getInstanceId(), current.getInstanceId())) {
            throw new IllegalArgumentException(
                    "'instance-id' cannot be changed at runtime (requires restart)");
        }
        if (newCfg.getRole() != current.getRole()) {
            throw new IllegalArgumentException(
                    "'role' cannot be changed at runtime (requires restart)");
        }

        clampConfig(newCfg);

        String opennmsHome = System.getProperty("opennms.home", ".");
        Path configFile = Paths.get(opennmsHome, "etc", CONFIG_FILE);
        Path tempFile   = Paths.get(opennmsHome, "etc", CONFIG_FILE + ".tmp");

        try {
            JAXBContext ctx = JAXBContext.newInstance(HaConfiguration.class);
            Marshaller m = ctx.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            m.marshal(newCfg, tempFile.toFile());
        } catch (JAXBException e) {
            Files.deleteIfExists(tempFile);
            throw new IOException("Failed to marshal HA configuration", e);
        }

        try {
            Files.move(tempFile, configFile,
                    StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            // Atomic move not supported (e.g. on some Windows filesystems); fall back
            Files.move(tempFile, configFile, StandardCopyOption.REPLACE_EXISTING);
        }

        LOG.info("HA: wrote updated configuration to {}", configFile);

        // Apply the file we just wrote so the change takes effect immediately.
        reloadConfig();
    }

    /**
     * Diffs {@code newCfg} against the live config and applies whichever changes are
     * safe at runtime. Changes to immutable identity fields ({@code enabled},
     * {@code instance-id}, {@code role}) are rejected with an ERROR and ignored.
     */
    void applyConfigReload(HaConfiguration newCfg) {
        HaConfiguration oldCfg = this.config;

        // Reject immutable-field changes — they require a process restart to take effect safely.
        if (newCfg.isEnabled() != oldCfg.isEnabled()) {
            LOG.error("HA: config reload rejected — 'enabled' changed ({} → {}); requires restart",
                    oldCfg.isEnabled(), newCfg.isEnabled());
            return;
        }
        if (!Objects.equals(newCfg.getInstanceId(), oldCfg.getInstanceId())) {
            LOG.error("HA: config reload rejected — 'instance-id' changed ({} → {}); requires restart",
                    oldCfg.getInstanceId(), newCfg.getInstanceId());
            return;
        }
        if (newCfg.getRole() != oldCfg.getRole()) {
            LOG.error("HA: config reload rejected — 'role' changed ({} → {}); requires restart",
                    oldCfg.getRole(), newCfg.getRole());
            return;
        }

        clampConfig(newCfg);

        if (configEquivalent(oldCfg, newCfg)) {
            return;
        }

        LOG.info("HA: config reload detected changes — applying");
        this.config = newCfg;

        // Heartbeat interval drives writeHeartbeat, checkPrimaryHeartbeat, and monitorForFailback.
        if (oldCfg.getHeartbeatIntervalSeconds() != newCfg.getHeartbeatIntervalSeconds()) {
            LOG.info("HA: heartbeat-interval changed {}s → {}s; rescheduling tasks",
                    oldCfg.getHeartbeatIntervalSeconds(), newCfg.getHeartbeatIntervalSeconds());
            rescheduleHeartbeatTasks(newCfg.getHeartbeatIntervalSeconds());
        }

        // Sync changes: interval, enable flag, or partner URL → cancel and re-evaluate.
        if (oldCfg.getSyncIntervalSeconds() != newCfg.getSyncIntervalSeconds()
                || oldCfg.isSyncEnabled() != newCfg.isSyncEnabled()
                || !Objects.equals(oldCfg.getPartnerRestUrl(), newCfg.getPartnerRestUrl())) {
            LOG.info("HA: sync settings changed (enabled: {} → {}, interval: {}s → {}s, partner-url: {} → {}); re-evaluating sync schedule",
                    oldCfg.isSyncEnabled(), newCfg.isSyncEnabled(),
                    oldCfg.getSyncIntervalSeconds(), newCfg.getSyncIntervalSeconds(),
                    oldCfg.getPartnerRestUrl(), newCfg.getPartnerRestUrl());
            startSyncIfApplicable();
        }

        // failover-threshold and sync credentials are read on every cycle — no action needed here.
    }

    private void rescheduleHeartbeatTasks(int newIntervalSeconds) {
        if (cancelIfActive(heartbeatFuture)) {
            heartbeatFuture = scheduler.scheduleAtFixedRate(this::writeHeartbeat,
                    newIntervalSeconds, newIntervalSeconds, TimeUnit.SECONDS);
        }
        if (cancelIfActive(standbyMonitorFuture)) {
            standbyMonitorFuture = scheduler.scheduleAtFixedRate(this::checkPrimaryHeartbeat,
                    newIntervalSeconds, newIntervalSeconds, TimeUnit.SECONDS);
        }
        if (cancelIfActive(failbackMonitorFuture)) {
            failbackMonitorFuture = scheduler.scheduleAtFixedRate(this::monitorForFailback,
                    newIntervalSeconds, newIntervalSeconds, TimeUnit.SECONDS);
        }
    }

    /**
     * True if {@code a} and {@code b} have the same values in every field this
     * coordinator cares about at runtime.
     */
    private static boolean configEquivalent(HaConfiguration a, HaConfiguration b) {
        return a.getHeartbeatIntervalSeconds() == b.getHeartbeatIntervalSeconds()
            && a.getFailoverThresholdSeconds()  == b.getFailoverThresholdSeconds()
            && a.isSyncEnabled()                == b.isSyncEnabled()
            && a.getSyncIntervalSeconds()       == b.getSyncIntervalSeconds()
            && Objects.equals(a.getPartnerInstanceId(), b.getPartnerInstanceId())
            && Objects.equals(a.getPartnerRestUrl(),    b.getPartnerRestUrl())
            && Objects.equals(a.getSyncUsername(),      b.getSyncUsername())
            && Objects.equals(a.getSyncPassword(),      b.getSyncPassword());
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

        // Periodic config reload — runs throughout the coordinator's lifetime so on-disk
        // edits to ha-configuration.xml take effect without restart.
        reloadFuture = scheduler.scheduleAtFixedRate(this::reloadConfig,
                CONFIG_RELOAD_INTERVAL_SECONDS, CONFIG_RELOAD_INTERVAL_SECONDS, TimeUnit.SECONDS);

        if (config.getRole() == HaRole.PRIMARY) {
            if (isPartnerActive()) {
                LOG.warn("HA: PRIMARY mode — partner {} is currently ACTIVE; entering DEGRADED state until failback",
                        config.getPartnerInstanceId());
                updateState(HaInstanceState.DEGRADED);
                currentState.set(HaInstanceState.DEGRADED);

                startSyncIfApplicable();

                // Heartbeat from DEGRADED so the active partner can tell we're alive.
                // writeHeartbeat publishes whichever state currentState holds at write time,
                // so it correctly tracks the transition DEGRADED → ACTIVE after promotion.
                heartbeatFuture = scheduler.scheduleAtFixedRate(this::writeHeartbeat,
                        config.getHeartbeatIntervalSeconds(), config.getHeartbeatIntervalSeconds(), TimeUnit.SECONDS);

                failbackMonitorFuture = scheduler.scheduleAtFixedRate(this::monitorForFailback,
                        config.getHeartbeatIntervalSeconds(), config.getHeartbeatIntervalSeconds(), TimeUnit.SECONDS);

                try {
                    startupGate.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                if (stopRequested.get()) {
                    LOG.info("HA: shutdown requested while PRIMARY in DEGRADED state; exiting without starting services");
                    return false;
                }

                LOG.info("HA: PRIMARY emerging from DEGRADED state — proceeding with service startup");
                // currentState already set to ACTIVE by promoteFromDegraded();
                // heartbeat task already running.
                return true;
            }

            updateState(HaInstanceState.ACTIVE);
            currentState.set(HaInstanceState.ACTIVE);
            heartbeatFuture = scheduler.scheduleAtFixedRate(this::writeHeartbeat,
                    0, config.getHeartbeatIntervalSeconds(), TimeUnit.SECONDS);
            LOG.info("HA: PRIMARY mode — heartbeat thread started, proceeding with service startup");
            return true;
        }

        // SECONDARY: optionally sync config from partner, then monitor PRIMARY heartbeat.
        startSyncIfApplicable();

        // Heartbeat from STANDBY so the active partner can tell we're alive.
        // After promotion the same task continues, publishing currentState=ACTIVE.
        heartbeatFuture = scheduler.scheduleAtFixedRate(this::writeHeartbeat,
                config.getHeartbeatIntervalSeconds(), config.getHeartbeatIntervalSeconds(), TimeUnit.SECONDS);

        LOG.info("HA: SECONDARY mode — starting monitor loop, blocking service startup");
        standbyMonitorFuture = scheduler.scheduleAtFixedRate(this::checkPrimaryHeartbeat,
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
        // heartbeat task already running; it will publish the new ACTIVE state on the next cycle.
        return true;
    }

    /**
     * Starts the config sync task if sync is enabled, the partner URL is set, and
     * this instance is not currently ACTIVE. Idempotent: if a sync task is already
     * scheduled, it is cancelled and replaced.
     */
    private void startSyncIfApplicable() {
        cancelIfActive(syncFuture);
        syncFuture = null;

        HaConfiguration cfg = config;
        if (!cfg.isSyncEnabled() || cfg.getPartnerRestUrl() == null) {
            LOG.info("HA: config sync inactive (sync-enabled={}, partner-rest-url={})",
                    cfg.isSyncEnabled(), cfg.getPartnerRestUrl());
            return;
        }
        if (currentState.get() == HaInstanceState.ACTIVE) {
            LOG.debug("HA: this instance is ACTIVE; not starting config sync");
            return;
        }
        HaConfigSyncer syncer = new HaConfigSyncer(this::getConfig, this::getCurrentState);
        LOG.info("HA: config sync started — partner {}, interval {}s",
                cfg.getPartnerRestUrl(), cfg.getSyncIntervalSeconds());
        syncFuture = scheduler.scheduleAtFixedRate(syncer::sync,
                0, cfg.getSyncIntervalSeconds(), TimeUnit.SECONDS);
    }

    private static boolean cancelIfActive(ScheduledFuture<?> f) {
        if (f != null && !f.isDone() && !f.isCancelled()) {
            f.cancel(false); // graceful: let in-flight task complete
            return true;
        }
        return false;
    }

    void doShutdown() {
        stopRequested.set(true);
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
        // Don't overwrite STANDBY with FAILED — failback already set the correct state.
        if (currentState.get() != HaInstanceState.STANDBY) {
            updateState(HaInstanceState.FAILED);
        }
        startupGate.countDown(); // unblock Starter if waiting
        LOG.info("HA coordinator shut down");
    }

    /**
     * Transitions this ACTIVE instance to STANDBY so the partner can take over.
     * Updates the DB immediately so the partner's monitor loop sees the change,
     * then stops the heartbeat scheduler so the heartbeat also goes stale as a
     * belt-and-suspenders signal. Does NOT stop OpenNMS services — the caller is
     * responsible for triggering the service shutdown after this returns.
     */
    public void initiateFailover() {
        LOG.warn("HA failover: {} ({}) stepping down ACTIVE → STANDBY",
                config.getInstanceId(), config.getRole());
        currentState.set(HaInstanceState.STANDBY);
        updateState(HaInstanceState.STANDBY);
        stopRequested.set(true);
        if (scheduler != null) {
            scheduler.shutdown(); // graceful: let any in-flight heartbeat write finish
        }
    }

    private void writeInitialStatus() throws Exception {
        String hostname = resolveHostname();
        try (Connection conn = dbFactory.getConnection()) {
            String sql = "INSERT INTO ha_instance_status (instance_id, configured_role, current_state, last_heartbeat, hostname) " +
                         "VALUES (?, ?, ?, NOW(), ?) " +
                         "ON CONFLICT (instance_id) DO UPDATE SET " +
                         "configured_role = EXCLUDED.configured_role, " +
                         "current_state = EXCLUDED.current_state, " +
                         "last_heartbeat = NOW(), " +
                         "hostname = EXCLUDED.hostname";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, config.getInstanceId());
                ps.setString(2, config.getRole().name());
                HaInstanceState initialState = config.getRole() == HaRole.PRIMARY ? HaInstanceState.ACTIVE : HaInstanceState.STANDBY;
                ps.setString(3, initialState.name());
                ps.setString(4, hostname);
                ps.executeUpdate();
            }
        }
        currentState.set(config.getRole() == HaRole.PRIMARY ? HaInstanceState.ACTIVE : HaInstanceState.STANDBY);
        LOG.info("HA: wrote initial status: instance={}, role={}, state={}", config.getInstanceId(), config.getRole(), currentState.get());
    }

    private void writeHeartbeat() {
        if (stopRequested.get()) return;
        checkForSplitBrain();
        if (stopRequested.get()) return;
        try (Connection conn = dbFactory.getConnection()) {
            String sql = "UPDATE ha_instance_status SET last_heartbeat = NOW(), current_state = ? WHERE instance_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, currentState.get().name());
                ps.setString(2, config.getInstanceId());
                ps.executeUpdate();
            }
        } catch (Exception e) {
            LOG.warn("HA: failed to write heartbeat for instance {}", config.getInstanceId(), e);
        }
    }

    /**
     * Detects split-brain: both this instance and its partner believe they are ACTIVE.
     *
     * <p>This can occur when this instance lost database connectivity, its heartbeat went
     * stale, and the partner promoted itself. When connectivity is restored this instance
     * resumes writing heartbeats — at which point both rows show ACTIVE.
     *
     * <p>Resolution: the instance whose {@code last_heartbeat} is older (the one that was
     * disconnected) yields by calling {@link #initiateFailover()} and stopping services.
     * If timestamps are equal, the instance with the lexicographically lower
     * {@code instance_id} yields as a deterministic tiebreaker.
     *
     * <p>Called at the start of every heartbeat write cycle; exits immediately when no
     * split-brain condition exists.
     */
    void checkForSplitBrain() {
        if (config.getPartnerInstanceId() == null) return;

        long ourAgeSeconds = -1;
        long partnerAgeSeconds = -1;
        boolean ourStateActive = false;
        boolean partnerStateActive = false;

        try (Connection conn = dbFactory.getConnection()) {
            // age_seconds is computed entirely on the DB server
            String sql = "SELECT instance_id, current_state, " +
                         "EXTRACT(EPOCH FROM (NOW() - last_heartbeat)) AS age_seconds " +
                         "FROM ha_instance_status WHERE instance_id IN (?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, config.getInstanceId());
                ps.setString(2, config.getPartnerInstanceId());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String id    = rs.getString(1);
                        String state = rs.getString(2);
                        long   age   = rs.getLong(3);

                        if (config.getInstanceId().equals(id)) {
                            ourStateActive  = HaInstanceState.ACTIVE.name().equals(state);
                            ourAgeSeconds   = age;
                        } else if (config.getPartnerInstanceId().equals(id)) {
                            partnerStateActive  = HaInstanceState.ACTIVE.name().equals(state);
                            partnerAgeSeconds   = age;
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOG.warn("HA: split-brain check failed; skipping this cycle", e);
            return;
        }

        if (!ourStateActive || !partnerStateActive
                || ourAgeSeconds < 0 || partnerAgeSeconds < 0) {
            return;
        }

        // Both instances are ACTIVE — split-brain detected.
        // The instance with the larger age (disconnected longer, per DB clock) yields.
        // Equal ages: deterministic tiebreaker on instance-id.
        boolean weYield = ourAgeSeconds > partnerAgeSeconds
                || (ourAgeSeconds == partnerAgeSeconds
                    && config.getInstanceId().compareTo(config.getPartnerInstanceId()) < 0);

        if (weYield) {
            LOG.error("HA SPLIT-BRAIN DETECTED: both '{}' and '{}' are ACTIVE. " +
                      "This instance has been disconnected longer (our heartbeat age: {}s vs partner: {}s); yielding.",
                      config.getInstanceId(), config.getPartnerInstanceId(),
                      ourAgeSeconds, partnerAgeSeconds);
            initiateFailover();
            stopServicesViaMBean();
        } else {
            LOG.error("HA SPLIT-BRAIN DETECTED: both '{}' and '{}' are ACTIVE. " +
                      "This instance has the fresher heartbeat (our age: {}s vs partner: {}s); continuing. " +
                      "Partner should self-terminate on its next heartbeat write.",
                      config.getInstanceId(), config.getPartnerInstanceId(),
                      ourAgeSeconds, partnerAgeSeconds);
        }
    }

    private void stopServicesViaMBean() {
        Thread t = new Thread(() -> {
            try {
                Thread.sleep(500);
                List<MBeanServer> servers = MBeanServerFactory.findMBeanServer(null);
                if (!servers.isEmpty()) {
                    servers.get(0).invoke(
                            ObjectName.getInstance("OpenNMS:Name=Manager"), "stop",
                            new Object[0], new String[0]);
                } else {
                    LOG.error("HA split-brain: no MBeanServer found; services may not stop cleanly");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                LOG.error("HA split-brain: error stopping services after split-brain resolution", e);
            }
        }, "ha-split-brain-stop");
        t.setDaemon(false);
        t.start();
    }

    private void checkPrimaryHeartbeat() {
        if (stopRequested.get() || startupGate.getCount() == 0) return;
        if (config.getPartnerInstanceId() == null) {
            LOG.warn("HA: no partner-instance-id configured; cannot monitor PRIMARY heartbeat");
            return;
        }

        try (Connection conn = dbFactory.getConnection()) {
            String sql = "SELECT current_state, EXTRACT(EPOCH FROM (NOW() - last_heartbeat)) AS age_seconds " +
                         "FROM ha_instance_status WHERE instance_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, config.getPartnerInstanceId());
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        LOG.warn("HA: no row found for partner {}; will retry", config.getPartnerInstanceId());
                        return;
                    }

                    String stateName = rs.getString(1);

                    // Voluntary step-down: PRIMARY explicitly set its state to non-ACTIVE.
                    // Promote immediately without the anti-flap wait.
                    if (!HaInstanceState.ACTIVE.name().equals(stateName)) {
                        LOG.info("HA: PRIMARY state is {} — promoting SECONDARY immediately", stateName);
                        promote();
                        return;
                    }

                    long ageSeconds = rs.getLong(2);

                    if (ageSeconds <= config.getFailoverThresholdSeconds()) {
                        LOG.debug("HA: PRIMARY heartbeat age {}s within threshold {}s — staying STANDBY",
                                ageSeconds, config.getFailoverThresholdSeconds());
                        return;
                    }

                    LOG.warn("HA: PRIMARY heartbeat is {}s old (threshold {}s) — verifying before promoting",
                            ageSeconds, config.getFailoverThresholdSeconds());
                }
            }
        } catch (Exception e) {
            LOG.error("HA: error during PRIMARY heartbeat check", e);
            return;
        }

        // Anti-flap: wait one more interval and re-read before committing to promotion
        try {
            Thread.sleep(TimeUnit.SECONDS.toMillis(config.getHeartbeatIntervalSeconds()));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }
        if (stopRequested.get()) return;

        try (Connection conn = dbFactory.getConnection()) {
            String sql = "SELECT current_state, EXTRACT(EPOCH FROM (NOW() - last_heartbeat)) AS age_seconds " +
                         "FROM ha_instance_status WHERE instance_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, config.getPartnerInstanceId());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String stateName = rs.getString(1);
                        long ageSeconds = rs.getLong(2);
                        if (HaInstanceState.ACTIVE.name().equals(stateName)
                                && ageSeconds <= config.getFailoverThresholdSeconds()) {
                            LOG.info("HA: PRIMARY heartbeat recovered (age now {}s) — staying STANDBY", ageSeconds);
                            return;
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("HA: error during PRIMARY heartbeat re-check", e);
            return;
        }

        promote();
    }

    /**
     * Returns true if the configured partner currently has {@code current_state=ACTIVE}
     * and a heartbeat fresher than the failover threshold. Called once at PRIMARY startup.
     */
    private boolean isPartnerActive() {
        if (config.getPartnerInstanceId() == null) return false;
        try (Connection conn = dbFactory.getConnection()) {
            String sql = "SELECT current_state, EXTRACT(EPOCH FROM (NOW() - last_heartbeat)) AS age_seconds " +
                         "FROM ha_instance_status WHERE instance_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, config.getPartnerInstanceId());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String stateName = rs.getString(1);
                        long ageSeconds = rs.getLong(2);
                        return HaInstanceState.ACTIVE.name().equals(stateName)
                                && ageSeconds <= config.getFailoverThresholdSeconds();
                    }
                }
            }
        } catch (Exception e) {
            LOG.warn("HA: could not check partner active state at startup; assuming not active", e);
        }
        return false;
    }

    /**
     * Scheduled while PRIMARY is in DEGRADED state. Releases the startup gate once
     * the SECONDARY's state is no longer ACTIVE or its heartbeat has gone stale.
     */
    private void monitorForFailback() {
        if (stopRequested.get() || startupGate.getCount() == 0) return;
        if (config.getPartnerInstanceId() == null) {
            LOG.warn("HA: no partner-instance-id configured; cannot monitor for failback — promoting PRIMARY");
            promoteFromDegraded();
            return;
        }
        try (Connection conn = dbFactory.getConnection()) {
            String sql = "SELECT current_state, EXTRACT(EPOCH FROM (NOW() - last_heartbeat)) AS age_seconds " +
                         "FROM ha_instance_status WHERE instance_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, config.getPartnerInstanceId());
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        LOG.info("HA: no DB row found for SECONDARY — PRIMARY reclaiming ACTIVE role");
                        promoteFromDegraded();
                        return;
                    }
                    String stateName = rs.getString(1);
                    long ageSeconds = rs.getLong(2);
                    boolean secondaryStillActive = HaInstanceState.ACTIVE.name().equals(stateName)
                            && ageSeconds <= config.getFailoverThresholdSeconds();
                    if (!secondaryStillActive) {
                        LOG.info("HA: SECONDARY no longer active (state={}, heartbeatAge={}s) — PRIMARY reclaiming ACTIVE role",
                                stateName, ageSeconds);
                        promoteFromDegraded();
                    } else {
                        LOG.debug("HA: SECONDARY still ACTIVE (heartbeatAge={}s) — PRIMARY remaining in DEGRADED state", ageSeconds);
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("HA: error during failback monitoring", e);
        }
    }

    private void promoteFromDegraded() {
        LOG.warn("HA: PRIMARY {} reclaiming ACTIVE role", config.getInstanceId());
        updateState(HaInstanceState.ACTIVE);
        currentState.set(HaInstanceState.ACTIVE);
        startupGate.countDown();
    }


    private void promote() {
        LOG.warn("HA: PRIMARY appears failed — SECONDARY {} is promoting to ACTIVE", config.getInstanceId());
        updateState(HaInstanceState.ACTIVE);
        currentState.set(HaInstanceState.ACTIVE);
        startupGate.countDown();
    }

    private void updateState(HaInstanceState state) {
        try (Connection conn = dbFactory.getConnection()) {
            String sql = "UPDATE ha_instance_status SET current_state = ?, last_heartbeat = NOW() WHERE instance_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, state.name());
                ps.setString(2, config.getInstanceId());
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
