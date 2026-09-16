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
 *
 * <p>Everything above describes {@link HaMode#COORDINATOR} (the default). In
 * {@link HaMode#HEARTBEAT_ONLY} the entire state machine is suppressed — an
 * external HA agent supervises the pair — and this class only publishes
 * liveness via {@link HaHeartbeatWriter}; startup is never gated.
 */
public class HaStartupCoordinator {

    private static final Logger LOG = LoggerFactory.getLogger(HaStartupCoordinator.class);

    private static final String CONFIG_FILE = "ha-configuration.xml";

    static final int MIN_HEARTBEAT_INTERVAL_SECONDS = 5;
    static final int MIN_FAILOVER_THRESHOLD_SECONDS = 20;
    static final int MIN_SYNC_INTERVAL_SECONDS = 5;

    /** Pace between retries while establishing HA state at startup (fail closed). */
    static final int STARTUP_RETRY_SECONDS = 10;

    /** How often to re-read {@code ha-configuration.xml} from disk while running. */
    static final int CONFIG_RELOAD_INTERVAL_SECONDS = 60;

    private static volatile HaStartupCoordinator INSTANCE;

    /** Mutable: replaced by {@link #applyConfigReload(HaConfiguration)} when the on-disk file changes. */
    private volatile HaConfiguration config;
    private final DbConnectionFactory dbFactory;
    private final HaHeartbeatWriter heartbeatWriter;
    private final CountDownLatch startupGate = new CountDownLatch(1);
    private final AtomicBoolean stopRequested = new AtomicBoolean(false);
    private final AtomicBoolean heartbeatOnlyRegistered = new AtomicBoolean(false);
    private final AtomicBoolean steppingDown = new AtomicBoolean(false);
    private final AtomicBoolean terminalStatePublished = new AtomicBoolean(false);
    private final AtomicBoolean servicesAuthorized = new AtomicBoolean(false);
    /** Local observation window only — never compared across nodes. */
    private volatile long partnerRowMissingSinceNanos;
    private final AtomicReference<HaInstanceState> currentState = new AtomicReference<>(HaInstanceState.STANDBY);
    private ScheduledExecutorService scheduler;

    // Tracked schedule handles so they can be cancelled and re-scheduled on config reload.
    private volatile ScheduledFuture<?> heartbeatFuture;
    private volatile ScheduledFuture<?> standbyMonitorFuture;
    private volatile ScheduledFuture<?> failbackMonitorFuture;
    private volatile ScheduledFuture<?> syncFuture;
    private volatile ScheduledFuture<?> reloadFuture;

    /** Serializes sync passes: a rescheduled sync must not run concurrently
     * with a still-finishing pass from the previous configuration. */
    private final Object syncMutex = new Object();

    private HaStartupCoordinator(HaConfiguration config, DbConnectionFactory dbFactory) {
        this.config = config;
        this.dbFactory = dbFactory;
        this.heartbeatWriter = new HaHeartbeatWriter(dbFactory, () -> this.config.getInstanceId());
    }

    /**
     * Loads {@code ha-configuration.xml} from {@code $OPENNMS_HOME/etc/} and
     * initialises the static singleton. Returns {@code null} (HA disabled)
     * only when the file is absent or explicitly sets {@code enabled=false}.
     * Any other failure — unreadable/malformed config, missing required
     * fields, initialization errors — throws so the caller aborts startup:
     * a node whose HA intent cannot be established must not run ungated.
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
            INSTANCE = null;
            throw new IllegalStateException("Failed to load HA configuration from etc/" + CONFIG_FILE, e);
        }

        if (!cfg.isEnabled()) {
            LOG.info("HA config found but disabled; starting in standalone mode");
            INSTANCE = null;
            return null;
        }

        if (cfg.getInstanceId() == null || cfg.getInstanceId().isBlank() || cfg.getRole() == null) {
            INSTANCE = null;
            throw new IllegalStateException(
                    "HA config is missing required fields (instance-id, role) in etc/" + CONFIG_FILE);
        }

        String partnerError = partnerConfigError(cfg);
        if (partnerError != null) {
            INSTANCE = null;
            throw new IllegalStateException(partnerError + " in etc/" + CONFIG_FILE);
        }

        clampConfig(cfg);

        try {
            DbConnectionFactory dbFactory = DbConnectionFactory.fromDatasourcesXml();
            INSTANCE = new HaStartupCoordinator(cfg, dbFactory);
            LOG.info("HA enabled: instance-id={}, role={}", cfg.getInstanceId(), cfg.getRole());
            return INSTANCE;
        } catch (Exception e) {
            INSTANCE = null;
            throw new IllegalStateException("Failed to initialize the HA coordinator", e);
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

    /**
     * Coordinator mode monitors, arbitrates against, and fails over to the
     * partner's row — a missing partner-instance-id silently disables all of
     * it, and a self-referential one makes split-brain detection read this
     * node's own row. Heartbeat-only mode needs no partner.
     *
     * @return an error message, or {@code null} when the config is valid
     */
    static String partnerConfigError(HaConfiguration cfg) {
        if (cfg.getMode() != HaMode.COORDINATOR) {
            return null;
        }
        String partner = cfg.getPartnerInstanceId();
        if (partner == null || partner.isBlank()) {
            return "coordinator mode requires a partner-instance-id";
        }
        if (partner.equals(cfg.getInstanceId())) {
            return "partner-instance-id must differ from instance-id";
        }
        return null;
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
        // The threshold must comfortably exceed the heartbeat interval, or the
        // SECONDARY can observe a near-threshold age on both of its checks and
        // promote while the PRIMARY is healthy.
        int minThreshold = 2 * cfg.getHeartbeatIntervalSeconds();
        if (cfg.getFailoverThresholdSeconds() < minThreshold) {
            LOG.warn("HA config: failover-threshold-seconds={} is below 2x heartbeat-interval-seconds ({}); clamping to {}",
                    cfg.getFailoverThresholdSeconds(), cfg.getHeartbeatIntervalSeconds(), minThreshold);
            cfg.setFailoverThresholdSeconds(minThreshold);
        }
        if (cfg.getSyncIntervalSeconds() < MIN_SYNC_INTERVAL_SECONDS) {
            LOG.warn("HA config: sync-interval-seconds={} is below minimum {}; clamping to {}",
                    cfg.getSyncIntervalSeconds(), MIN_SYNC_INTERVAL_SECONDS, MIN_SYNC_INTERVAL_SECONDS);
            cfg.setSyncIntervalSeconds(MIN_SYNC_INTERVAL_SECONDS);
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
        if (newCfg.getMode() != current.getMode()) {
            throw new IllegalArgumentException(
                    "'mode' cannot be changed at runtime (requires restart)");
        }
        if (!Objects.equals(newCfg.getPartnerRestUrl(), current.getPartnerRestUrl())) {
            throw new IllegalArgumentException(
                    "'partner-rest-url' cannot be changed at runtime (requires restart)");
        }
        String partnerError = partnerConfigError(newCfg);
        if (partnerError != null) {
            throw new IllegalArgumentException(partnerError);
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
        if (newCfg.getMode() != oldCfg.getMode()) {
            LOG.error("HA: config reload rejected — 'mode' changed ({} → {}); requires restart",
                    oldCfg.getMode(), newCfg.getMode());
            return;
        }
        if (!Objects.equals(newCfg.getPartnerRestUrl(), oldCfg.getPartnerRestUrl())) {
            LOG.error("HA: config reload rejected — 'partner-rest-url' changed ({} → {}); requires restart",
                    oldCfg.getPartnerRestUrl(), newCfg.getPartnerRestUrl());
            return;
        }
        String partnerError = partnerConfigError(newCfg);
        if (partnerError != null) {
            LOG.error("HA: config reload rejected — {}", partnerError);
            return;
        }
        if (!Objects.equals(newCfg.getPartnerInstanceId(), oldCfg.getPartnerInstanceId())) {
            // A new partner gets the full threshold before its absence counts.
            partnerRowMissingSinceNanos = 0;
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
                || oldCfg.isSyncEnabled() != newCfg.isSyncEnabled()) {
            LOG.info("HA: sync settings changed (enabled: {} → {}, interval: {}s → {}s); re-evaluating sync schedule",
                    oldCfg.isSyncEnabled(), newCfg.isSyncEnabled(),
                    oldCfg.getSyncIntervalSeconds(), newCfg.getSyncIntervalSeconds());
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
            && Objects.equals(a.getSyncPassword(),      b.getSyncPassword())
            && Objects.equals(a.getSyncRoots(),         b.getSyncRoots())
            && Objects.equals(a.getSyncExcludes(),      b.getSyncExcludes());
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
        if (config.getMode() == HaMode.HEARTBEAT_ONLY) {
            return startHeartbeatOnly();
        }

        // Fail closed: an enabled coordinator-mode node must never start services
        // without its coordination state established. Retries pace on the startup
        // gate so a shutdown request wakes them immediately.
        while (true) {
            if (stopRequested.get()) {
                LOG.info("HA: shutdown requested before HA state was established; exiting without starting services");
                return false;
            }
            try {
                HaStatusSchema.ensureSchema(dbFactory);
                break;
            } catch (Exception e) {
                LOG.error("HA: failed to ensure ha_instance_status schema; retrying in {}s (startup stays gated)",
                        STARTUP_RETRY_SECONDS, e);
                if (!waitBeforeRetry()) {
                    LOG.info("HA: shutdown requested before HA state was established; exiting without starting services");
                    return false;
                }
            }
        }

        // Establish the initial state before this node advertises anything: a
        // PRIMARY may claim ACTIVE only after proving the partner is not serving —
        // when the partner is ACTIVE, the very first row write is DEGRADED, so a
        // restarting PRIMARY never briefly shows ACTIVE next to a serving
        // SECONDARY (which would trip the partner's split-brain arbitration). A
        // failed partner check or status write keeps startup gated and retries;
        // it never authorizes startup.
        boolean partnerActive = false;
        while (true) {
            if (stopRequested.get()) {
                LOG.info("HA: shutdown requested before HA state was established; exiting without starting services");
                return false;
            }
            try {
                partnerActive = config.getRole() == HaRole.PRIMARY && isPartnerActive();
                HaInstanceState initialState = partnerActive ? HaInstanceState.DEGRADED
                        : config.getRole() == HaRole.PRIMARY ? HaInstanceState.ACTIVE : HaInstanceState.STANDBY;
                writeInitialStatus(initialState);
                break;
            } catch (Exception e) {
                LOG.error("HA: could not establish initial HA state; retrying in {}s (startup stays gated)",
                        STARTUP_RETRY_SECONDS, e);
                if (!waitBeforeRetry()) {
                    LOG.info("HA: shutdown requested before HA state was established; exiting without starting services");
                    return false;
                }
            }
        }

        // heartbeat, partner monitor, and config sync must not share threads
        scheduler = Executors.newScheduledThreadPool(3, r -> {
            Thread t = new Thread(r, "ha-coordinator");
            t.setDaemon(true);
            return t;
        });

        // Periodic config reload — runs throughout the coordinator's lifetime so on-disk
        // edits to ha-configuration.xml take effect without restart.
        reloadFuture = scheduler.scheduleAtFixedRate(this::reloadConfig,
                CONFIG_RELOAD_INTERVAL_SECONDS, CONFIG_RELOAD_INTERVAL_SECONDS, TimeUnit.SECONDS);

        if (config.getRole() == HaRole.PRIMARY) {
            if (partnerActive) {
                LOG.warn("HA: PRIMARY mode — partner {} is currently ACTIVE; entering DEGRADED state until failback",
                        config.getPartnerInstanceId());

                startSyncIfApplicable();

                // Heartbeat from DEGRADED so the active partner can tell we're alive.
                // The heartbeat carries liveness only; DEGRADED/ACTIVE state is
                // written on the transition edges by updateState().
                heartbeatFuture = scheduler.scheduleAtFixedRate(this::writeHeartbeat,
                        config.getHeartbeatIntervalSeconds(), config.getHeartbeatIntervalSeconds(), TimeUnit.SECONDS);

                failbackMonitorFuture = scheduler.scheduleAtFixedRate(this::monitorForFailback,
                        config.getHeartbeatIntervalSeconds(), config.getHeartbeatIntervalSeconds(), TimeUnit.SECONDS);

                awaitGate();

                if (stopRequested.get()) {
                    LOG.info("HA: shutdown requested while PRIMARY in DEGRADED state; exiting without starting services");
                    return false;
                }

                LOG.info("HA: PRIMARY emerging from DEGRADED state — proceeding with service startup");
                // currentState already set to ACTIVE by promoteFromDegraded();
                // heartbeat task already running.
                servicesAuthorized.set(true);
                return true;
            }

            // Initial status write above already claimed ACTIVE and stamped active_since.
            heartbeatFuture = scheduler.scheduleAtFixedRate(this::writeHeartbeat,
                    0, config.getHeartbeatIntervalSeconds(), TimeUnit.SECONDS);
            LOG.info("HA: PRIMARY mode — heartbeat thread started, proceeding with service startup");
            servicesAuthorized.set(true);
            return true;
        }

        // SECONDARY: optionally sync config from partner, then monitor PRIMARY heartbeat.
        startSyncIfApplicable();

        // Heartbeat from STANDBY so the active partner can tell we're alive.
        // After promotion the same task continues; the ACTIVE state itself is
        // written once, at the promotion edge, by updateState().
        heartbeatFuture = scheduler.scheduleAtFixedRate(this::writeHeartbeat,
                config.getHeartbeatIntervalSeconds(), config.getHeartbeatIntervalSeconds(), TimeUnit.SECONDS);

        LOG.info("HA: SECONDARY mode — starting monitor loop, blocking service startup");
        standbyMonitorFuture = scheduler.scheduleAtFixedRate(this::checkPrimaryHeartbeat,
                config.getHeartbeatIntervalSeconds(), config.getHeartbeatIntervalSeconds(), TimeUnit.SECONDS);

        awaitGate();

        if (stopRequested.get()) {
            LOG.info("HA: shutdown requested while in SECONDARY standby; exiting without starting services");
            return false;
        }

        LOG.info("HA: SECONDARY promoted to ACTIVE — proceeding with service startup");
        // heartbeat task already running; promote() has already written ACTIVE.
        servicesAuthorized.set(true);
        return true;
    }

    /**
     * Blocks until the startup gate opens. The gate opens only on a persisted
     * promotion or on shutdown — an interrupt does neither, so waiting resumes
     * (fail closed) instead of falling through to an unauthorized startup.
     */
    private void awaitGate() {
        boolean interrupted = false;
        while (startupGate.getCount() > 0) {
            try {
                startupGate.await();
            } catch (InterruptedException e) {
                interrupted = true;
                LOG.warn("HA: startup gate wait interrupted; continuing to wait (the gate opens only on promotion or shutdown)");
            }
        }
        if (interrupted && stopRequested.get()) {
            Thread.currentThread().interrupt(); // exiting anyway; preserve the status
        }
    }

    /**
     * Paces a fail-closed startup retry. Waits on the startup gate rather than
     * sleeping so a shutdown request wakes the Starter thread immediately.
     *
     * @return {@code true} to retry, {@code false} if shutdown was requested
     */
    private boolean waitBeforeRetry() {
        try {
            startupGate.await(STARTUP_RETRY_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
        return !stopRequested.get();
    }

    /**
     * heartbeat-only mode: an external HA agent owns supervision — it writes
     * {@code current_state}/{@code active_since}/{@code agent_last_seen},
     * decides promotion and fencing, and starts/stops this service. OpenNMS's
     * only HA job here is publishing liveness, so this never gates startup:
     * no standby monitoring, no promotion, no DEGRADED/failback handling, no
     * split-brain check, and no config sync run from this JVM.
     */
    private boolean startHeartbeatOnly() {
        try {
            HaStatusSchema.ensureSchema(dbFactory);
            upsertSelfNonClobbering();
            heartbeatOnlyRegistered.set(true);
        } catch (Exception e) {
            // Never gate startup in this mode — the external agent supervises.
            // The heartbeat task below keeps retrying the registration.
            LOG.error("HA: failed to register instance row; heartbeat task will keep retrying", e);
        }

        scheduler = Executors.newScheduledThreadPool(1, r -> {
            Thread t = new Thread(r, "ha-heartbeat");
            t.setDaemon(true);
            return t;
        });

        reloadFuture = scheduler.scheduleAtFixedRate(this::reloadConfig,
                CONFIG_RELOAD_INTERVAL_SECONDS, CONFIG_RELOAD_INTERVAL_SECONDS, TimeUnit.SECONDS);
        heartbeatFuture = scheduler.scheduleAtFixedRate(this::writeHeartbeat,
                0, config.getHeartbeatIntervalSeconds(), TimeUnit.SECONDS);

        LOG.info("HA heartbeat-only mode — external agent supervises; proceeding with service startup");
        servicesAuthorized.set(true);
        return true;
    }

    /**
     * Registers this node's row without ever touching the supervisor-owned
     * columns: on conflict only {@code configured_role}, {@code hostname} and
     * {@code last_heartbeat} are refreshed — {@code current_state} and
     * {@code active_since} belong to the external agent in heartbeat-only mode.
     */
    private void upsertSelfNonClobbering() throws Exception {
        String hostname = resolveHostname();
        try (Connection conn = dbFactory.getConnection()) {
            String sql = "INSERT INTO ha_instance_status (instance_id, configured_role, current_state, last_heartbeat, hostname) " +
                         "VALUES (?, ?, 'STANDBY', NOW(), ?) " +
                         "ON CONFLICT (instance_id) DO UPDATE SET " +
                         "configured_role = EXCLUDED.configured_role, " +
                         "last_heartbeat = NOW(), " +
                         "hostname = EXCLUDED.hostname";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, config.getInstanceId());
                ps.setString(2, config.getRole().name());
                ps.setString(3, hostname);
                ps.executeUpdate();
            }
        }
        LOG.info("HA: registered instance row (heartbeat-only): instance={}, role={}",
                config.getInstanceId(), config.getRole());
    }

    /**
     * Starts the config sync task if sync is enabled, the partner URL is set, and
     * this instance is not currently ACTIVE. Idempotent: if a sync task is already
     * scheduled, it is cancelled and replaced.
     */
    private void startSyncIfApplicable() {
        if (config.getMode() == HaMode.HEARTBEAT_ONLY) {
            LOG.debug("HA: heartbeat-only mode — config sync is owned by the external agent; not scheduling");
            return;
        }

        cancelIfActive(syncFuture);
        syncFuture = null;

        HaConfiguration cfg = config;
        if (cfg.isSyncEnabled() && cfg.getPartnerRestUrl() != null
                && cfg.getPartnerRestUrl().startsWith("http://")) {
            LOG.warn("HA: partner-rest-url {} is not https — sync credentials and credential "
                    + "stores will cross the network in cleartext", cfg.getPartnerRestUrl());
        }
        if (!cfg.isSyncEnabled() || cfg.getPartnerRestUrl() == null) {
            LOG.info("HA: config sync inactive (sync-enabled={}, partner-rest-url={})",
                    cfg.isSyncEnabled(), cfg.getPartnerRestUrl());
            return;
        }
        if (currentState.get() == HaInstanceState.ACTIVE) {
            LOG.debug("HA: this instance is ACTIVE; not starting config sync");
            return;
        }
        HaConfigSyncer syncer = new HaConfigSyncer(this::getConfig, this::getCurrentState, syncStatusRecorder());
        LOG.info("HA: config sync started — partner {}, interval {}s",
                cfg.getPartnerRestUrl(), cfg.getSyncIntervalSeconds());
        syncFuture = scheduler.scheduleAtFixedRate(() -> {
            synchronized (syncMutex) {
                syncer.sync();
            }
        }, 0, cfg.getSyncIntervalSeconds(), TimeUnit.SECONDS);
    }

    /** Publishes each sync cycle's outcome into this node's own row, so a
     * broken sync is visible from the partner instead of only in this node's
     * log — a gated standby serves no REST and raises no events. */
    private HaConfigSyncer.StatusRecorder syncStatusRecorder() {
        return new HaConfigSyncer.StatusRecorder() {
            @Override public void syncSucceeded() {
                writeSyncStatus("last_sync_attempt = NOW(), last_sync_success = NOW(), last_sync_error = NULL", null);
            }
            @Override public void syncFailed(String reason) {
                writeSyncStatus("last_sync_attempt = NOW(), last_sync_error = ?", reason);
            }
            @Override public void bootConfigChanged() {
                writeSyncStatus("boot_config_changed_at = NOW()", null);
            }
        };
    }

    private void writeSyncStatus(String assignments, String errorParam) {
        try (Connection conn = dbFactory.getConnection()) {
            String sql = "UPDATE ha_instance_status SET " + assignments + " WHERE instance_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                int idx = 1;
                if (errorParam != null) {
                    ps.setString(idx++, errorParam);
                }
                ps.setString(idx, config.getInstanceId());
                ps.executeUpdate();
            }
        } catch (Exception e) {
            LOG.debug("HA: could not record sync status", e);
        }
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
        // A node that never started services has nothing draining; otherwise the
        // scheduler stays up so the heartbeat publishes liveness until
        // markServicesStopped() publishes the terminal state post-drain.
        if (!servicesAuthorized.get() && scheduler != null) {
            scheduler.shutdownNow();
        }
        startupGate.countDown(); // unblock Starter if waiting
        LOG.info("HA coordinator shut down");
    }

    /**
     * Begins stepping this ACTIVE instance down to STANDBY: stops the heartbeat
     * scheduler (the staleness clock starts now) and marks the step-down, but
     * deliberately does NOT publish STANDBY yet — the partner promotes the
     * moment it sees a non-ACTIVE state, and that must not happen while this
     * node's services are still draining. {@link #markServicesStopped()} writes
     * the STANDBY row once the drain completes. Does NOT stop OpenNMS services —
     * the caller triggers the service shutdown after this returns.
     */
    public void initiateFailover() {
        if (config.getMode() == HaMode.HEARTBEAT_ONLY) {
            throw new IllegalStateException(
                    "HA is in heartbeat-only mode; failover is controlled by the external HA agent");
        }
        LOG.warn("HA failover: {} ({}) stepping down ACTIVE → STANDBY (published after services stop)",
                config.getInstanceId(), config.getRole());
        steppingDown.set(true);
        currentState.set(HaInstanceState.STANDBY);
        stopRequested.set(true);
        // The scheduler keeps running: the heartbeat publishes liveness through
        // the drain so the partner promotes on the terminal-state write, never
        // on staleness beside a still-draining node.
    }

    /**
     * Called by {@code Manager.stop()} (reflectively) after the service Invoker
     * has finished: only now is a promotable state published — STANDBY for a
     * step-down, FAILED for a plain stop of an ACTIVE/DEGRADED node — so the
     * partner can never promote while this node is still draining. A drain that
     * outlives the failover threshold is covered by heartbeat staleness instead
     * (the heartbeat stopped when the shutdown began).
     */
    public static void markServicesStopped() {
        HaStartupCoordinator coord = INSTANCE;
        if (coord != null) {
            coord.doMarkServicesStopped();
        }
    }

    void doMarkServicesStopped() {
        // In heartbeat-only mode current_state belongs to the external agent.
        // Invoked from both doSystemExit (before the exit timer is armed) and
        // the end of Manager.stop (paths without a scheduled exit) — publish once.
        if (config.getMode() == HaMode.COORDINATOR
                && terminalStatePublished.compareAndSet(false, true)) {
            if (steppingDown.get()) {
                updateState(HaInstanceState.STANDBY);
            } else if (currentState.get() != HaInstanceState.STANDBY) {
                // A gated standby never advertised anything that needs retracting.
                updateState(HaInstanceState.FAILED);
            }
        }
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
    }

    private void writeInitialStatus(HaInstanceState initialState) throws Exception {
        String hostname = resolveHostname();
        try (Connection conn = dbFactory.getConnection()) {
            // A PRIMARY that proved the partner inactive takes ACTIVE now, so stamp
            // active_since with the DB clock; any other initial state carries no
            // ownership (NULL). See checkForSplitBrain.
            String activeSinceExpr = initialState == HaInstanceState.ACTIVE ? "NOW()" : "NULL";
            String sql = "INSERT INTO ha_instance_status (instance_id, configured_role, current_state, last_heartbeat, hostname, active_since) " +
                         "VALUES (?, ?, ?, NOW(), ?, " + activeSinceExpr + ") " +
                         "ON CONFLICT (instance_id) DO UPDATE SET " +
                         "configured_role = EXCLUDED.configured_role, " +
                         "current_state = EXCLUDED.current_state, " +
                         "last_heartbeat = NOW(), " +
                         "hostname = EXCLUDED.hostname, " +
                         "boot_config_changed_at = NULL, " +
                         "active_since = EXCLUDED.active_since";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, config.getInstanceId());
                ps.setString(2, config.getRole().name());
                ps.setString(3, initialState.name());
                ps.setString(4, hostname);
                ps.executeUpdate();
            }
        }
        currentState.set(initialState);
        LOG.info("HA: wrote initial status: instance={}, role={}, state={}", config.getInstanceId(), config.getRole(), initialState);
    }

    /**
     * The single heartbeat task for both modes. In heartbeat-only it also
     * completes a failed startup registration — the heartbeat UPDATE matches
     * zero rows until the row exists.
     */
    private void writeHeartbeat() {
        if (terminalStatePublished.get()) return;
        if (config.getMode() == HaMode.COORDINATOR) {
            if (checkForSplitBrain()) return; // yielded; halt is imminent
        } else if (!heartbeatOnlyRegistered.get()) {
            try {
                HaStatusSchema.ensureSchema(dbFactory);
                upsertSelfNonClobbering();
                heartbeatOnlyRegistered.set(true);
            } catch (Exception e) {
                LOG.warn("HA: instance registration failed; will retry next heartbeat cycle: {}", e.toString());
                return;
            }
        }
        // Liveness only: the heartbeat never carries current_state. State is
        // written exclusively on edge transitions (updateState), so a stale
        // in-JVM view can never overwrite a demotion written by the partner's
        // operator or by an external agent.
        heartbeatWriter.write();
    }

    /**
     * Detects split-brain: both this instance and its partner believe they are ACTIVE.
     *
     * <p>This can occur when this instance lost database connectivity, its heartbeat went
     * stale, and the partner promoted itself. When connectivity is restored this instance
     * resumes writing heartbeats — at which point both rows show ACTIVE.
     *
     * <p>Resolution is decided on {@code active_since} — the timestamp at which each node
     * took ownership of the ACTIVE role — <em>not</em> on heartbeat age. A split-brain only
     * arises when one node was ACTIVE, lost the DB, and the partner promoted itself later
     * because the first looked dead; the partner (with the <em>later</em> {@code active_since})
     * is the rightful owner, so the node that became ACTIVE <em>earlier</em> yields. This is
     * the stable signal: {@code active_since} is set only on the transition into ACTIVE and
     * never moves on a heartbeat, so — unlike {@code last_heartbeat}, which resets to ~0 the
     * instant a disconnected node reconnects — both nodes read the same two absolute
     * timestamps and always reach complementary decisions. If the timestamps are equal (or
     * either is missing), the instance with the lexicographically lower {@code instance_id}
     * yields as a deterministic tiebreaker.
     *
     * <p>Called at the start of every heartbeat write cycle; exits immediately when no
     * split-brain condition exists.
     */
    /** @return {@code true} if this instance yielded (termination is imminent). */
    boolean checkForSplitBrain() {
        final String partnerId = config.getPartnerInstanceId();
        if (partnerId == null) return false;

        double ourActiveSince = Double.NaN;
        double partnerActiveSince = Double.NaN;
        boolean ourStateActive = false;
        boolean partnerStateActive = false;
        long partnerHeartbeatAge = 0;

        try (Connection conn = dbFactory.getConnection()) {
            // active_since is read as an absolute epoch (seconds) from the DB clock — a stable
            // value that does not move between the two nodes' independent check cycles.
            String sql = "SELECT instance_id, current_state, " +
                         "EXTRACT(EPOCH FROM active_since) AS active_since_epoch, " +
                         "EXTRACT(EPOCH FROM (NOW() - last_heartbeat)) AS heartbeat_age " +
                         "FROM ha_instance_status WHERE instance_id IN (?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, config.getInstanceId());
                ps.setString(2, partnerId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String id           = rs.getString(1);
                        String state        = rs.getString(2);
                        double activeSince   = rs.getDouble(3);
                        boolean activeSinceNull = rs.wasNull();

                        if (config.getInstanceId().equals(id)) {
                            ourStateActive  = HaInstanceState.ACTIVE.name().equals(state);
                            ourActiveSince  = activeSinceNull ? Double.NaN : activeSince;
                        } else if (partnerId.equals(id)) {
                            partnerStateActive  = HaInstanceState.ACTIVE.name().equals(state);
                            partnerActiveSince  = activeSinceNull ? Double.NaN : activeSince;
                            partnerHeartbeatAge = rs.getLong(4);
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOG.warn("HA: split-brain check failed; skipping this cycle", e);
            return false;
        }

        if (!ourStateActive || !partnerStateActive) {
            return false;
        }

        // Arbitrate only against a live rival. An ACTIVE row with a heartbeat
        // stale beyond the threshold belongs to a partner that stopped (or is
        // draining) while holding the role — yielding to it would leave zero
        // live nodes. If it is actually alive and reconnects, its heartbeat
        // freshens and arbitration resumes on the next cycle.
        if (partnerHeartbeatAge > config.getFailoverThresholdSeconds()) {
            LOG.warn("HA: partner '{}' row reads ACTIVE but its heartbeat is {}s old (threshold {}s) — " +
                     "the partner appears to have stopped while ACTIVE; continuing as the sole active instance",
                    partnerId, partnerHeartbeatAge, config.getFailoverThresholdSeconds());
            return false;
        }

        // Both instances are ACTIVE — split-brain detected. The instance that became ACTIVE
        // earlier (smaller active_since) is the stale one and yields. A missing active_since
        // is treated as "earliest possible" so it yields against a node with a known
        // ownership time. Identical/both-missing values fall back to the instance-id tiebreaker.
        final boolean weYield;
        if (!Double.isNaN(ourActiveSince) && !Double.isNaN(partnerActiveSince)
                && ourActiveSince != partnerActiveSince) {
            weYield = ourActiveSince < partnerActiveSince;
        } else if (Double.isNaN(ourActiveSince) && !Double.isNaN(partnerActiveSince)) {
            weYield = true;  // we have no ownership timestamp; partner does → we yield
        } else if (!Double.isNaN(ourActiveSince) && Double.isNaN(partnerActiveSince)) {
            weYield = false; // partner has no ownership timestamp; we do → partner yields
        } else {
            // Equal timestamps, or neither recorded → deterministic tiebreaker on instance-id.
            weYield = config.getInstanceId().compareTo(partnerId) < 0;
        }

        if (weYield) {
            LOG.error("HA SPLIT-BRAIN DETECTED: both '{}' and '{}' are ACTIVE. " +
                      "This instance took the ACTIVE role earlier (our active-since epoch: {} vs partner: {}); " +
                      "yielding by terminating the JVM immediately.",
                      config.getInstanceId(), partnerId,
                      ourActiveSince, partnerActiveSince);
            // Flip our row to STANDBY immediately as an explicit signal to the partner
            // (best-effort; connectivity is present since the split-brain read above just
            // succeeded). Unlike a normal step-down there is no drain to overlap with —
            // the halt below is instantaneous — so the deferred-write rule does not apply.
            currentState.set(HaInstanceState.STANDBY);
            updateState(HaInstanceState.STANDBY);
            stopRequested.set(true);
            if (scheduler != null) {
                scheduler.shutdown();
            }
            // Hard-terminate rather than a graceful Manager.stop(): an orderly shutdown lets
            // pending/queued tasks from other daemons drain, which would mean a second ACTIVE
            // instance writing to the database during the split-brain window. Halting kills the
            // JVM immediately with no shutdown hooks, so no further writes can occur.
            terminateJvm(70);
            return true;
        } else {
            LOG.error("HA SPLIT-BRAIN DETECTED: both '{}' and '{}' are ACTIVE. " +
                      "This instance took the ACTIVE role later (our active-since epoch: {} vs partner: {}); continuing. " +
                      "Partner became ACTIVE earlier and should self-terminate on its next heartbeat write.",
                      config.getInstanceId(), partnerId,
                      ourActiveSince, partnerActiveSince);
            return false;
        }
    }

    /**
     * Forcibly terminates the JVM, the in-process equivalent of {@code SIGKILL}: the VM
     * stops immediately without running shutdown hooks or finalizers, so no other daemon's
     * pending or queued work can drain. This is deliberately more abrupt than a graceful
     * {@code Manager.stop()} — during a split-brain yield we must guarantee this instance
     * issues no further database writes while the partner is also ACTIVE.
     *
     * <p>Overridable so tests can verify the yield decision without killing the test JVM.
     *
     * @param code process exit status (non-zero signals abnormal termination)
     */
    protected void terminateJvm(int code) {
        Runtime.getRuntime().halt(code);
    }

    private void checkPrimaryHeartbeat() {
        if (stopRequested.get() || startupGate.getCount() == 0) return;
        // One verification concerns one partner: both reads below use this
        // snapshot, and a partner change mid-verification restarts it.
        final String partnerId = config.getPartnerInstanceId();
        if (partnerId == null) {
            LOG.warn("HA: no partner-instance-id configured; cannot monitor PRIMARY heartbeat");
            return;
        }

        try (Connection conn = dbFactory.getConnection()) {
            String sql = "SELECT current_state, EXTRACT(EPOCH FROM (NOW() - last_heartbeat)) AS age_seconds " +
                         "FROM ha_instance_status WHERE instance_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, partnerId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        if (partnerRowMissingSinceNanos == 0) {
                            partnerRowMissingSinceNanos = System.nanoTime();
                            LOG.warn("HA: no row found for partner {}; will promote if it stays missing beyond {}s",
                                    partnerId, config.getFailoverThresholdSeconds());
                            return;
                        }
                        long missingSeconds = TimeUnit.NANOSECONDS.toSeconds(
                                System.nanoTime() - partnerRowMissingSinceNanos);
                        if (missingSeconds <= config.getFailoverThresholdSeconds()) {
                            return;
                        }
                        LOG.warn("HA: partner {} row missing for {}s (threshold {}s) — verifying before promoting",
                                partnerId, missingSeconds, config.getFailoverThresholdSeconds());
                    } else {
                        partnerRowMissingSinceNanos = 0;

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
        if (!partnerId.equals(config.getPartnerInstanceId())) {
            LOG.info("HA: partner changed to {} during verification; restarting the check against the new partner",
                    config.getPartnerInstanceId());
            return;
        }

        try (Connection conn = dbFactory.getConnection()) {
            String sql = "SELECT current_state, EXTRACT(EPOCH FROM (NOW() - last_heartbeat)) AS age_seconds " +
                         "FROM ha_instance_status WHERE instance_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, partnerId);
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
     * and a heartbeat fresher than the failover threshold. Called at PRIMARY startup.
     * Throws when the check cannot be performed — callers must fail closed rather
     * than treat an unanswered question as "not active".
     */
    private boolean isPartnerActive() throws Exception {
        final String partnerId = config.getPartnerInstanceId();
        if (partnerId == null) return false;
        try (Connection conn = dbFactory.getConnection()) {
            String sql = "SELECT current_state, EXTRACT(EPOCH FROM (NOW() - last_heartbeat)) AS age_seconds " +
                         "FROM ha_instance_status WHERE instance_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, partnerId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String stateName = rs.getString(1);
                        long ageSeconds = rs.getLong(2);
                        return HaInstanceState.ACTIVE.name().equals(stateName)
                                && ageSeconds <= config.getFailoverThresholdSeconds();
                    }
                }
            }
        }
        return false;
    }

    /**
     * Scheduled while PRIMARY is in DEGRADED state. Releases the startup gate once
     * the SECONDARY's state is no longer ACTIVE or its heartbeat has gone stale.
     */
    private void monitorForFailback() {
        if (stopRequested.get() || startupGate.getCount() == 0) return;
        final String partnerId = config.getPartnerInstanceId();
        if (partnerId == null) {
            LOG.warn("HA: no partner-instance-id configured; cannot monitor for failback — promoting PRIMARY");
            promoteFromDegraded();
            return;
        }
        try (Connection conn = dbFactory.getConnection()) {
            String sql = "SELECT current_state, EXTRACT(EPOCH FROM (NOW() - last_heartbeat)) AS age_seconds " +
                         "FROM ha_instance_status WHERE instance_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, partnerId);
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
        if (!updateState(HaInstanceState.ACTIVE)) {
            LOG.error("HA: could not persist ACTIVE state; promotion aborted — will retry on the next monitor cycle");
            return;
        }
        currentState.set(HaInstanceState.ACTIVE);
        cancelIfActive(failbackMonitorFuture);
        cancelIfActive(syncFuture);
        startupGate.countDown();
    }


    private void promote() {
        LOG.warn("HA: PRIMARY appears failed — SECONDARY {} is promoting to ACTIVE", config.getInstanceId());
        if (!updateState(HaInstanceState.ACTIVE)) {
            LOG.error("HA: could not persist ACTIVE state; promotion aborted — will retry on the next monitor cycle");
            return;
        }
        currentState.set(HaInstanceState.ACTIVE);
        cancelIfActive(standbyMonitorFuture);
        cancelIfActive(syncFuture);
        startupGate.countDown();
    }

    /** @return {@code true} only if the state was persisted (exactly one row updated). */
    private boolean updateState(HaInstanceState state) {
        // active_since records when this instance took ownership of the ACTIVE role and is
        // the signal used to resolve split-brain (see checkForSplitBrain). On a transition
        // INTO ACTIVE we stamp it with the DB clock, but only on the actual edge — the
        // CASE guard leaves an existing active_since untouched if we are already ACTIVE, so
        // repeated ACTIVE writes never bump it. Any non-ACTIVE state clears it to NULL.
        final String activeSinceClause = state == HaInstanceState.ACTIVE
                ? "active_since = CASE WHEN current_state <> 'ACTIVE' THEN NOW() ELSE active_since END"
                : "active_since = NULL";
        try (Connection conn = dbFactory.getConnection()) {
            String sql = "UPDATE ha_instance_status SET current_state = ?, last_heartbeat = NOW(), "
                    + activeSinceClause + " WHERE instance_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, state.name());
                ps.setString(2, config.getInstanceId());
                int rows = ps.executeUpdate();
                if (rows != 1) {
                    LOG.error("HA: state update to {} matched {} rows for instance {} (expected 1)",
                            state, rows, config.getInstanceId());
                    return false;
                }
                return true;
            }
        } catch (Exception e) {
            LOG.error("HA: failed to update state to {} for instance {}", state, config.getInstanceId(), e);
            return false;
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

    public DbConnectionFactory getDbFactory() {
        return dbFactory;
    }

    public HaInstanceState getCurrentState() {
        if (config.getMode() == HaMode.HEARTBEAT_ONLY) {
            // The external agent owns current_state; the DB row is the truth.
            return readStateFromDb();
        }
        return currentState.get();
    }

    private HaInstanceState readStateFromDb() {
        try (Connection conn = dbFactory.getConnection()) {
            String sql = "SELECT current_state FROM ha_instance_status WHERE instance_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, config.getInstanceId());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return HaInstanceState.valueOf(rs.getString(1));
                    }
                }
            }
        } catch (Exception e) {
            LOG.debug("HA: could not read current_state from DB", e);
        }
        return HaInstanceState.STANDBY;
    }
}
