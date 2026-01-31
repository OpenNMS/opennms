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

import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import org.opennms.core.utils.InetAddressUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrapdInstrumentation {

    private static final Logger LOG = LoggerFactory.getLogger(TrapdInstrumentation.class);
    private static final String DEVICE_METRICS_FLAG = "org.opennms.netmgt.trapd.enableDeviceMetrics";

    private final DeviceTrapMetricsRegistry<DeviceConsumerTrapMetrics> deviceRegistry;

    private final AtomicLong trapsReceived = new AtomicLong();
    private final AtomicLong v1TrapsReceived = new AtomicLong();
    private final AtomicLong v2cTrapsReceived = new AtomicLong();
    private final AtomicLong v3TrapsReceived = new AtomicLong();
    private final AtomicLong vUnknownTrapsReceived = new AtomicLong();
    private final AtomicLong trapsDiscarded = new AtomicLong();
    private final AtomicLong trapsErrored = new AtomicLong();
    private final AtomicLong rawTrapsReceived = new AtomicLong();
    private int maxQueueSize = 0;
    private int batchSize = 0;
    private Supplier<Integer> queueSizeSupplier = () -> 0;

    public TrapdInstrumentation() {
        boolean enabled = Boolean.getBoolean(DEVICE_METRICS_FLAG);
        this.deviceRegistry = new DeviceTrapMetricsRegistry<>(enabled, DeviceConsumerTrapMetrics::new, "consumer");
    }

    public void incTrapsReceivedCount(String version) {
        trapsReceived.incrementAndGet();
        if ("v1".equals(version)) {
            v1TrapsReceived.incrementAndGet();
        } else if ("v2c".equals(version) || "v2".equals(version)) {
            v2cTrapsReceived.incrementAndGet();
        } else if ("v3".equals(version)) {
            v3TrapsReceived.incrementAndGet();
        } else {
            vUnknownTrapsReceived.incrementAndGet();
            LOG.warn("Received a trap with an unknown SNMP protocol version '{}'.", version);
        }
    }

    public void incTrapsReceivedCount(String version, String location, InetAddress trapAddress) {
        incTrapsReceivedCount(version);
        DeviceConsumerTrapMetrics device = getDeviceMetrics(location, trapAddress);
        if (device != null) {
            device.incTrapsReceived();
        }
    }

    public void incDiscardCount() {
        trapsDiscarded.incrementAndGet();
    }

    public void incDiscardCount(String location, InetAddress trapAddress) {
        incDiscardCount();
        DeviceConsumerTrapMetrics device = getDeviceMetrics(location, trapAddress);
        if (device != null) {
            device.incTrapsDiscarded();
        }
    }

    public void incErrorCount() {
        trapsErrored.incrementAndGet();
    }

    public void incErrorCount(String location, InetAddress trapAddress) {
        incErrorCount();
        DeviceConsumerTrapMetrics device = getDeviceMetrics(location, trapAddress);
        if (device != null) {
            device.incTrapsErrored();
        }
    }

    public long getV1TrapsReceived() {
        return v1TrapsReceived.get();
    }

    public long getV2cTrapsReceived() {
        return v2cTrapsReceived.get();
    }

    public long getV3TrapsReceived() {
        return v3TrapsReceived.get();
    }

    public long getVUnknownTrapsReceived() {
        return vUnknownTrapsReceived.get();
    }

    public long getTrapsDiscarded() {
        return trapsDiscarded.get();
    }

    public long getTrapsErrored() {
        return trapsErrored.get();
    }

    public long getTrapsReceived() {
        return trapsReceived.get();
    }

    public void incRawTrapsReceivedCount() {
        rawTrapsReceived.incrementAndGet();
    }

    public long getRawTrapsReceived() {
        return rawTrapsReceived.get();
    }

    public void setMaxQueueSize(int size) {
        this.maxQueueSize = size;
    }

    public int getMaxQueueSize() {
        return maxQueueSize;
    }

    public void setBatchSize(int size) {
        this.batchSize = size;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setQueueSizeSupplier(Supplier<Integer> supplier) {
        this.queueSizeSupplier = supplier != null ? supplier : () -> 0;
    }

    public int getCurrentQueueSize() {
        return queueSizeSupplier.get();
    }

    public void shutdown() {
        deviceRegistry.shutdown();
    }

    private DeviceConsumerTrapMetrics getDeviceMetrics(String location, InetAddress trapAddress) {
        if (location == null || trapAddress == null) {
            return null;
        }
        return deviceRegistry.getOrCreate(location, InetAddressUtils.str(trapAddress));
    }
}
