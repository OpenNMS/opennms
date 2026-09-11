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
package org.opennms.netmgt.availability;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.opennms.core.concurrent.LogPreservingThreadFactory;
import org.opennms.core.sysprops.SystemProperties;
import org.opennms.netmgt.dao.api.CategoryAvailabilityCalculator;
import org.opennms.netmgt.dao.api.CategoryAvailabilitySnapshotDao;
import org.opennms.netmgt.model.availability.CategoryAvailability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Keeps one availability snapshot per category up to date.
 *
 * Every category is refreshed on its own schedule. After each refresh the
 * next one is scheduled at a multiple of how long the refresh took, clamped
 * between a floor and a ceiling. Small installations therefore refresh close
 * to the floor and large ones back off on their own. Refreshes share a pool
 * of one thread by default, so they run one after another and a slow
 * category delays the ones queued behind it; raise the thread count to let
 * categories refresh concurrently at the cost of concurrent database load.
 *
 * Tunables, read from system properties (opennms.properties):
 * <ul>
 *   <li>{@code org.opennms.availability.window.minutes}: rolling window, default 1440 (24 hours)</li>
 *   <li>{@code org.opennms.availability.refresh.minSeconds}: shortest delay between refreshes of a category, default 60</li>
 *   <li>{@code org.opennms.availability.refresh.maxSeconds}: longest delay, default 900</li>
 *   <li>{@code org.opennms.availability.refresh.pacingFactor}: next delay = last duration times this, default 5</li>
 *   <li>{@code org.opennms.availability.refresh.threads}: categories refreshed concurrently, default 1</li>
 * </ul>
 */
public class AvailabilitySnapshotScheduler {
    private static final Logger LOG = LoggerFactory.getLogger(AvailabilitySnapshotScheduler.class);

    public static final String WINDOW_MINUTES_PROPERTY = "org.opennms.availability.window.minutes";
    public static final String MIN_SECONDS_PROPERTY = "org.opennms.availability.refresh.minSeconds";
    public static final String MAX_SECONDS_PROPERTY = "org.opennms.availability.refresh.maxSeconds";
    public static final String PACING_FACTOR_PROPERTY = "org.opennms.availability.refresh.pacingFactor";
    public static final String THREADS_PROPERTY = "org.opennms.availability.refresh.threads";

    private final CategoryAvailabilityCalculator m_calculator;
    private final CategoryAvailabilitySnapshotDao m_snapshotDao;
    private final CategoryDefinitionProvider m_definitions;

    private long m_windowMillis = TimeUnit.MINUTES.toMillis(SystemProperties.getLong(WINDOW_MINUTES_PROPERTY, 1440));
    private long m_minIntervalMillis = TimeUnit.SECONDS.toMillis(SystemProperties.getLong(MIN_SECONDS_PROPERTY, 60));
    private long m_maxIntervalMillis = TimeUnit.SECONDS.toMillis(SystemProperties.getLong(MAX_SECONDS_PROPERTY, 900));
    private double m_pacingFactor = parseDouble(System.getProperty(PACING_FACTOR_PROPERTY), 5.0);
    private int m_threads = SystemProperties.getInteger(THREADS_PROPERTY, 1);

    private CategoryAvailabilityThresholdNotifier m_thresholdNotifier;

    private ScheduledExecutorService m_executor;
    private volatile boolean m_running;

    public AvailabilitySnapshotScheduler(final CategoryAvailabilityCalculator calculator, final CategoryAvailabilitySnapshotDao snapshotDao, final CategoryDefinitionProvider definitions) {
        m_calculator = Objects.requireNonNull(calculator, "calculator");
        m_snapshotDao = Objects.requireNonNull(snapshotDao, "snapshotDao");
        m_definitions = Objects.requireNonNull(definitions, "definitions");
    }

    public synchronized void start() {
        if (m_running) {
            return;
        }
        if (m_minIntervalMillis <= 0 || m_maxIntervalMillis < m_minIntervalMillis || m_windowMillis <= 0 || m_pacingFactor <= 0 || m_threads <= 0) {
            throw new IllegalStateException("Invalid availability refresh settings: window=" + m_windowMillis + "ms min=" + m_minIntervalMillis
                    + "ms max=" + m_maxIntervalMillis + "ms factor=" + m_pacingFactor + " threads=" + m_threads);
        }
        final List<CategoryDefinition> definitions = m_definitions.getDefinitions();
        LOG.info("Starting availability snapshot scheduler for {} categories: window {} min, refresh between {} s and {} s, pacing factor {}, {} thread(s)",
                definitions.size(), m_windowMillis / 60000L, m_minIntervalMillis / 1000L, m_maxIntervalMillis / 1000L, m_pacingFactor, m_threads);

        m_executor = Executors.newScheduledThreadPool(m_threads, new LogPreservingThreadFactory("AvailabilitySnapshot", m_threads));
        m_running = true;

        try {
            m_snapshotDao.retainOnly(definitions.stream().map(CategoryDefinition::getLabel).collect(Collectors.toList()));
        } catch (final RuntimeException e) {
            LOG.warn("Failed to prune snapshots of removed categories", e);
        }
        for (final CategoryDefinition definition : definitions) {
            schedule(definition, 0);
        }
    }

    public synchronized void stop() {
        m_running = false;
        if (m_executor != null) {
            m_executor.shutdownNow();
            m_executor = null;
        }
        LOG.info("Availability snapshot scheduler stopped");
    }

    public boolean isRunning() {
        return m_running;
    }

    private void schedule(final CategoryDefinition definition, final long delayMillis) {
        final ScheduledExecutorService executor = m_executor;
        if (!m_running || executor == null) {
            return;
        }
        executor.schedule(() -> refresh(definition), delayMillis, TimeUnit.MILLISECONDS);
    }

    /** Compute and store one category, then schedule its next refresh. */
    void refresh(final CategoryDefinition definition) {
        if (!m_running) {
            return;
        }
        final long began = System.nanoTime();
        long nextDelay;
        try {
            final Date end = new Date();
            final Date start = new Date(end.getTime() - m_windowMillis);
            final CategoryAvailability previous = m_thresholdNotifier == null ? null : m_snapshotDao.findSummary(definition.getLabel()).orElse(null);
            final CategoryAvailability availability = m_calculator.calculate(definition.getLabel(), definition.getRule(), definition.getServices(), start, end);
            m_snapshotDao.save(availability);
            if (m_thresholdNotifier != null) {
                m_thresholdNotifier.evaluate(definition, previous, availability);
            }
            final long duration = (System.nanoTime() - began) / 1_000_000L;
            nextDelay = nextDelayMillis(duration, m_minIntervalMillis, m_maxIntervalMillis, m_pacingFactor);
            LOG.debug("Refreshed availability for '{}' ({} nodes, {} services, {}%) in {} ms; next refresh in {} s",
                    definition.getLabel(), availability.getNodeCount(), availability.getServiceCount(), availability.getAvailability(), duration, nextDelay / 1000L);
        } catch (final Throwable t) {
            nextDelay = m_maxIntervalMillis;
            LOG.error("Failed to refresh availability for category '{}'; retrying in {} s", definition.getLabel(), nextDelay / 1000L, t);
        }
        schedule(definition, nextDelay);
    }

    /** Next delay: the last duration scaled by the pacing factor, clamped to [min, max]. */
    static long nextDelayMillis(final long durationMillis, final long minMillis, final long maxMillis, final double factor) {
        final double paced = durationMillis * factor;
        return (long) Math.max(minMillis, Math.min(maxMillis, paced));
    }

    private static double parseDouble(final String value, final double defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (final NumberFormatException e) {
            LOG.warn("Ignoring invalid value '{}' for {}", value, PACING_FACTOR_PROPERTY);
            return defaultValue;
        }
    }

    /** Optional: emits events when a category crosses its warning threshold. */
    public void setThresholdNotifier(final CategoryAvailabilityThresholdNotifier thresholdNotifier) {
        m_thresholdNotifier = thresholdNotifier;
    }

    public void setWindowMillis(final long windowMillis) {
        m_windowMillis = windowMillis;
    }

    public void setMinIntervalMillis(final long minIntervalMillis) {
        m_minIntervalMillis = minIntervalMillis;
    }

    public void setMaxIntervalMillis(final long maxIntervalMillis) {
        m_maxIntervalMillis = maxIntervalMillis;
    }

    public void setPacingFactor(final double pacingFactor) {
        m_pacingFactor = pacingFactor;
    }

    public void setThreads(final int threads) {
        m_threads = threads;
    }
}
