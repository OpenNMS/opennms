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
package org.opennms.netmgt.provision.service;

import com.codahale.metrics.jmx.JmxReporter;
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
import java.util.AbstractMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
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
    private final ConcurrentHashMap<String, TimeTrackerOverallMonitor> overallMonitors = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ConcurrentLinkedDeque<String>> metricAssociation = new ConcurrentHashMap<>();
    public static final int MAX_METRIC_ASSOCIATION_SIZE = 30;
    private MetricRegistry metricRegistry = new MetricRegistry();
    private MetricRegistry metricRegistryOverall = new MetricRegistry();
    private JmxReporter jmxReporter;
    private JmxReporter jmxReporterOverall;

    /**
     * For spring use. Default is 3 days.
     */
    public MonitorHolder() {
        this(3L *24L * 3600L);
    }

    public MonitorHolder(long seconds) {
        jmxReporter = JmxReporter.forRegistry(metricRegistry).inDomain("org.opennms.netmgt.provision.status").build();
        jmxReporter.start();
        jmxReporterOverall = JmxReporter.forRegistry(metricRegistryOverall).inDomain("org.opennms.netmgt.provision.overall").build();
        jmxReporterOverall.start();
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
        String metricName = MetricRegistry.name(name, LocalDateTime.now().format(datetimeFormat), String.valueOf(job.hashCode()));
        updateAssociations(name, metricName);
        return monitors.get(metricName);
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
        String metricName = MetricRegistry.name(name, LocalDateTime.now().format(datetimeFormat));
        updateAssociations(name, metricName);
        return monitors.get(metricName);
    }

    /**
     * Keep track of overall metrics and single job metrics
     * Key should be the actual requisition (overall metric) and the metric name should be a concatenation that includes
     * requisition, timestamp and job
     * This help to link NodeScans overall metrics to the monitorKey which is associated to a single job
     * @param key overall metric name (requisition)
     * @param metricName single job metric name
     */
    public void updateAssociations(final String key, final String metricName) {

        if (!metricAssociation.containsKey(key)) {
            var names = new ConcurrentLinkedDeque<String>();
            names.addFirst(metricName);
            metricAssociation.put(key, names);
        } else if (metricAssociation.containsKey(key) && !metricAssociation.get(key).contains(metricName)) {
            metricAssociation.get(key).addFirst(metricName);
        }
        if (metricAssociation.get(key).size() > MAX_METRIC_ASSOCIATION_SIZE) {
            metricAssociation.get(key).removeLast();
        }

    }

    /**
     * It will return existing overall monitor or create new one
     *
     * @param name
     * @return
     */
    public TimeTrackerOverallMonitor createOverallMonitor(String name) {
        TimeTrackerOverallMonitor overallMonitor = null;
        if (overallMonitors.containsKey(name)) {
            overallMonitor = overallMonitors.get(name);
        } else {
            overallMonitor = new TimeTrackerOverallMonitor(name, metricRegistryOverall);
            overallMonitors.put(name, overallMonitor);
        }
        return overallMonitor;
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

    /**
     * Gets an overall metric associated to a single job metric
     * @param metricName
     * @return
     */
    public TimeTrackerOverallMonitor getOverallMonitorForMetric(String metricName) {
        for(var map: metricAssociation.entrySet()){
            if(map.getValue().contains(metricName)) {
                return overallMonitors.get(map.getKey());
            }
        }
        return null;
    }

    public Map<String, TimeTrackerOverallMonitor> getOverallMonitors() {
        return overallMonitors;
    }
    
    public AbstractMap<String, ConcurrentLinkedDeque<String>> getAssociatedMetrics(){
        return this.metricAssociation;
    }

    public void shutdown() {
        jmxReporter.close();
        jmxReporterOverall.close();
    }
}
