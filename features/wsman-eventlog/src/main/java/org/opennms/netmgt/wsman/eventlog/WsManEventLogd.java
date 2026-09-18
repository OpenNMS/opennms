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
package org.opennms.netmgt.wsman.eventlog;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.opennms.netmgt.config.wsman.eventlog.Log;
import org.opennms.netmgt.config.wsman.eventlog.Package;
import org.opennms.netmgt.config.wsman.eventlog.WsmanEventlogConfiguration;
import org.opennms.netmgt.daemon.DaemonTools;
import org.opennms.netmgt.daemon.SpringServiceDaemon;
import org.opennms.netmgt.dao.WSManConfigDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.events.api.annotations.EventHandler;
import org.opennms.netmgt.events.api.annotations.EventListener;
import org.opennms.netmgt.events.api.model.IEvent;
import org.opennms.netmgt.wsman.eventlog.rpc.LocationAwareWsManEventLogClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 * Polls Windows event logs over WS-Man and turns each record into an OpenNMS event.
 * One timer task per package and log; the node polls run on a bounded pool.
 */
@EventListener(name = WsManEventLogd.NAME, logPrefix = WsManEventLogd.LOG_PREFIX)
public class WsManEventLogd implements SpringServiceDaemon {

    private static final Logger LOG = LoggerFactory.getLogger(WsManEventLogd.class);

    public static final String NAME = "WsManEventLogd";
    public static final String LOG_PREFIX = "wsmaneventlogd";

    @Autowired
    private WsManEventLogConfigDao configDao;

    @Autowired
    private WSManConfigDao wsManConfigDao;

    @Autowired
    private LocationAwareWsManEventLogClient client;

    @Autowired
    private EventLogTargetResolver targetResolver;

    @Autowired
    private EventLogCursorStore cursorStore;

    @Autowired
    private EventLogStatusStore statusStore;

    @Autowired
    private EventForwarder eventForwarder;

    private final WsManEventLogdMetrics metrics = new WsManEventLogdMetrics();

    private ScheduledExecutorService scheduler;
    private ExecutorService workers;
    private final List<ScheduledFuture<?>> tasks = new ArrayList<>();
    private final Map<String, CachedTargets> targetsByPackage = new ConcurrentHashMap<>();
    private volatile EventLogPoller poller;
    private volatile Duration targetRefresh = Duration.ofMinutes(5);

    public WsManEventLogd() {
    }

    public WsManEventLogd(WsManEventLogConfigDao configDao, WSManConfigDao wsManConfigDao, LocationAwareWsManEventLogClient client,
            EventLogTargetResolver targetResolver, EventLogCursorStore cursorStore, EventLogStatusStore statusStore, EventForwarder eventForwarder) {
        this.configDao = configDao;
        this.wsManConfigDao = wsManConfigDao;
        this.client = client;
        this.targetResolver = targetResolver;
        this.cursorStore = cursorStore;
        this.statusStore = statusStore;
        this.eventForwarder = eventForwarder;
    }

    @Override
    public void afterPropertiesSet() {
        Objects.requireNonNull(configDao, "configDao");
        Objects.requireNonNull(wsManConfigDao, "wsManConfigDao");
        Objects.requireNonNull(client, "client");
        Objects.requireNonNull(targetResolver, "targetResolver");
        Objects.requireNonNull(cursorStore, "cursorStore");
        Objects.requireNonNull(eventForwarder, "eventForwarder");
    }

    @Override
    public synchronized void start() {
        final WsmanEventlogConfiguration config = configDao.getConfig();
        targetRefresh = Durations.parse(config.getTargetRefreshInterval());
        poller = new EventLogPoller(wsManConfigDao, client, cursorStore, new EventLogEventMapper(NAME), eventForwarder, metrics, statusStore, config.getRetries());
        scheduler = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setNameFormat("WsManEventLogd-Scheduler").build());
        workers = Executors.newFixedThreadPool(config.getThreads(), new ThreadFactoryBuilder().setNameFormat("WsManEventLogd-Poll-%d").build());
        metrics.register();
        int scheduled = 0;
        for (Package pkg : config.getPackages()) {
            for (Log log : pkg.getLogs()) {
                if (!log.isEnabled()) {
                    continue;
                }
                final long interval = Math.max(1_000L, log.getInterval());
                tasks.add(scheduler.scheduleAtFixedRate(() -> tick(pkg, log), 0L, interval, TimeUnit.MILLISECONDS));
                scheduled++;
            }
        }
        LOG.info("Started with {} package(s) and {} scheduled log(s)", config.getPackages().size(), scheduled);
    }

    @Override
    public synchronized void destroy() {
        tasks.forEach(t -> t.cancel(false));
        tasks.clear();
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
        if (workers != null) {
            workers.shutdownNow();
            workers = null;
        }
        targetsByPackage.clear();
        metrics.unregister();
        LOG.info("Stopped");
    }

    void tick(Package pkg, Log log) {
        try {
            final List<EventLogTarget> targets = targetsFor(pkg);
            final ExecutorService pool = workers;
            final EventLogPoller current = poller;
            if (pool == null || current == null) {
                return;
            }
            for (EventLogTarget target : targets) {
                pool.execute(() -> {
                    try {
                        current.poll(pkg, log, target);
                    } catch (RuntimeException e) {
                        LOG.error("Unexpected failure polling {} on {}", log.getName(), target, e);
                    }
                });
            }
        } catch (RuntimeException e) {
            LOG.error("Could not resolve the targets of package {}: {}", pkg.getName(), e.getMessage(), e);
        }
    }

    List<EventLogTarget> targetsFor(Package pkg) {
        final CachedTargets cached = targetsByPackage.get(pkg.getName());
        if (cached != null && System.currentTimeMillis() - cached.at < targetRefresh.toMillis()) {
            return cached.targets;
        }
        final List<EventLogTarget> targets = targetResolver.resolve(pkg.getFilter());
        targetsByPackage.put(pkg.getName(), new CachedTargets(targets));
        metrics.setTargets(targetsByPackage.values().stream().mapToInt(c -> c.targets.size()).sum());
        return targets;
    }

    @EventHandler(uei = EventConstants.RELOAD_DAEMON_CONFIG_UEI)
    public void handleReloadDaemonConfig(IEvent event) {
        DaemonTools.handleReloadEvent(event, NAME, ev -> {
            configDao.reload();
            destroy();
            start();
        });
    }

    WsManEventLogdMetrics getMetrics() {
        return metrics;
    }

    private static final class CachedTargets {
        final long at = System.currentTimeMillis();
        final List<EventLogTarget> targets;

        CachedTargets(List<EventLogTarget> targets) {
            this.targets = targets;
        }
    }
}
