/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.service;

import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import org.opennms.netmgt.provision.service.operations.ProvisionMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * It is the holder class for all provisiond performance monitors. It also creates a JmxReporter.
 */
public class MonitorHolder {
    private static final Logger LOG = LoggerFactory.getLogger(MonitorHolder.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    private final DateTimeFormatter datetimeFormat = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private LoadingCache<String, TimeTrackingMonitor> monitors;
    private MetricRegistry metricRegistry = new MetricRegistry();
    private JmxReporter jmxReporter;

    /**
     * For spring use. Default is 3 days.
     */
    public MonitorHolder() {
        this(3L *24L * 3600L);
    }

    public MonitorHolder(long seconds) {
        jmxReporter = JmxReporter.forRegistry(metricRegistry).inDomain("org.opennms.netmgt.provision.status").build();
        jmxReporter.start();
        this.createCacheWithExpireTime(seconds);
    }

    /**
     * It will create a new local cache with specific access expire hours. If existing cache is not empty, it will also copy exising data to new cache.
     * @param seconds
     */
    public void createCacheWithExpireTime(long seconds) {
        LOG.info("Create cache with expire time {} seconds.", seconds);
        var oldMonitors = monitors;
        monitors = CacheBuilder.newBuilder()
                .expireAfterAccess(seconds, TimeUnit.SECONDS)
                .removalListener((RemovalListener<String, ProvisionMonitor>) removalNotification -> {
                    metricRegistry.removeMatching((s, metric) -> s.indexOf(removalNotification.getKey()) == 0);
                    try {
                        LOG.info("{} Summary job name: {}\nOutput:\n{}", LocalDateTime.now().format(DateTimeFormatter.ISO_TIME),  removalNotification.getKey(), mapper.writeValueAsString(removalNotification.getValue()));
                    } catch (JsonProcessingException e) {
                        LOG.warn("Fail to write summary for {} error: {}.", removalNotification.getKey(), e.getMessage());
                    }
                }).build(new CacheLoader<>() {
                    @Override
                    public TimeTrackingMonitor load(String key) {
                        return new TimeTrackingMonitor(key, metricRegistry);
                    }
                });
        if (oldMonitors != null) {
            monitors.putAll(oldMonitors.asMap());
        }
    }

    /**
     * It will return existing monitor or create new one
     *
     * @param name (For key. It will append with start time)
     * @param job
     * @return TimeTrackingMonitor
     * @throws ExecutionException
     */
    public TimeTrackingMonitor createMonitor(String name, ImportJob job) throws ExecutionException {
        monitors.cleanUp();
        return monitors.get(MetricRegistry.name(name, LocalDateTime.now().format(datetimeFormat), String.valueOf(job.hashCode())));
    }

    /**
     * It will return existing monitor or create new one
     *
     * @param name (For key. It will append with start time)
     * @return TimeTrackingMonitor
     * @throws ExecutionException
     */
    public TimeTrackingMonitor createMonitor(String name) throws ExecutionException {
        monitors.cleanUp();
        return monitors.get(MetricRegistry.name(name, LocalDateTime.now().format(datetimeFormat)));
    }

    /**
     * It will only return existing monitor
     *
     * @param key
     * @return monitor (nullable)
     * @throws ExecutionException
     */
    public ProvisionMonitor getMonitor(String key) {
        monitors.cleanUp();
        if (key == null) {
            return null;
        }
        return monitors.getIfPresent(key);
    }

    public Map<String, TimeTrackingMonitor> getMonitors() {
        monitors.cleanUp();
        return monitors.asMap();
    }

    public void shutdown() {
        jmxReporter.close();
    }
}
