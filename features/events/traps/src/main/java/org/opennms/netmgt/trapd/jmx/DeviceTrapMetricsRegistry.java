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
package org.opennms.netmgt.trapd.jmx;

import java.lang.management.ManagementFactory;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registry for per-device trap metrics exposed as MBeans with structured ObjectNames.
 * Each device gets an MBean registered under:
 * {@code org.opennms.netmgt.trapd.device:location=<loc>,ip=<ip>,type=<mbeanType>}
 *
 * When disabled (default), {@link #getOrCreate} returns {@code null} for zero overhead.
 *
 * @param <T> the metrics holder type (must implement an MBean interface)
 */
public class DeviceTrapMetricsRegistry<T> {

    private static final Logger LOG = LoggerFactory.getLogger(DeviceTrapMetricsRegistry.class);
    private static final String JMX_DOMAIN = "org.opennms.netmgt.trapd.device";

    private final boolean enabled;
    private final String mbeanType;
    private final DeviceMetricsFactory<T> factory;
    private final MBeanServer mbeanServer;
    private final ConcurrentHashMap<DeviceKey, T> deviceMetrics = new ConcurrentHashMap<>();

    private static final class DeviceKey {
        final String location;
        final String ipAddress;

        DeviceKey(String location, String ipAddress) {
            this.location = location;
            this.ipAddress = ipAddress;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof DeviceKey)) return false;
            DeviceKey that = (DeviceKey) o;
            return Objects.equals(location, that.location) && Objects.equals(ipAddress, that.ipAddress);
        }

        @Override
        public int hashCode() {
            return Objects.hash(location, ipAddress);
        }
    }

    @FunctionalInterface
    public interface DeviceMetricsFactory<T> {
        T create(String location, String ipAddress);
    }

    /**
     * @param enabled   if false, {@link #getOrCreate} always returns null
     * @param factory   creates a new metrics instance given (location, ipAddress)
     * @param mbeanType type property for the JMX ObjectName (e.g. "listener" or "consumer")
     */
    public DeviceTrapMetricsRegistry(boolean enabled, DeviceMetricsFactory<T> factory, String mbeanType) {
        this.enabled = enabled;
        this.factory = factory;
        this.mbeanType = mbeanType;
        if (enabled) {
            this.mbeanServer = ManagementFactory.getPlatformMBeanServer();
            LOG.info("Per-device trap metrics enabled (type='{}') under JMX domain '{}'", mbeanType, JMX_DOMAIN);
        } else {
            this.mbeanServer = null;
        }
    }

    /**
     * Returns the per-device metrics for the given device, creating and registering
     * an MBean if necessary. Returns {@code null} when the registry is disabled.
     */
    public T getOrCreate(String location, String ipAddress) {
        if (!enabled) {
            return null;
        }
        DeviceKey key = new DeviceKey(location, ipAddress);
        return deviceMetrics.computeIfAbsent(key, k -> {
            T metrics = factory.create(location, ipAddress);
            registerMBean(metrics, location, ipAddress);
            return metrics;
        });
    }

    private void registerMBean(T metrics, String location, String ipAddress) {
        try {
            ObjectName objectName = new ObjectName(
                    JMX_DOMAIN + ":location=" + ObjectName.quote(location)
                            + ",ip=" + ObjectName.quote(ipAddress)
                            + ",type=" + mbeanType);
            mbeanServer.registerMBean(metrics, objectName);
            LOG.debug("Registered per-device MBean: {}", objectName);
        } catch (Exception e) {
            LOG.warn("Failed to register per-device MBean for location='{}', ip='{}'", location, ipAddress, e);
        }
    }

    /**
     * Unregisters all per-device MBeans and clears the registry.
     */
    public void shutdown() {
        for (DeviceKey key : deviceMetrics.keySet()) {
            unregisterMBean(key.location, key.ipAddress);
        }
        deviceMetrics.clear();
        LOG.info("Per-device trap metrics registry shut down");
    }

    private void unregisterMBean(String location, String ipAddress) {
        try {
            ObjectName objectName = new ObjectName(
                    JMX_DOMAIN + ":location=" + ObjectName.quote(location)
                            + ",ip=" + ObjectName.quote(ipAddress)
                            + ",type=" + mbeanType);
            if (mbeanServer != null && mbeanServer.isRegistered(objectName)) {
                mbeanServer.unregisterMBean(objectName);
                LOG.debug("Unregistered per-device MBean: {}", objectName);
            }
        } catch (Exception e) {
            LOG.warn("Failed to unregister per-device MBean for location='{}', ip='{}'", location, ipAddress, e);
        }
    }

    public boolean isEnabled() {
        return enabled;
    }
}
