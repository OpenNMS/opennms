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

import java.lang.management.ManagementFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WsManEventLogdMetrics implements WsManEventLogdMetricsMBean {

    private static final Logger LOG = LoggerFactory.getLogger(WsManEventLogdMetrics.class);

    public static final String OBJECT_NAME = "OpenNMS:Name=WsManEventLogd,Type=Metrics";

    private final AtomicLong recordsRead = new AtomicLong();
    private final AtomicLong eventsPublished = new AtomicLong();
    private final AtomicLong pollsCompleted = new AtomicLong();
    private final AtomicLong pollsFailed = new AtomicLong();
    private final AtomicLong pollsTruncated = new AtomicLong();
    private final AtomicInteger targets = new AtomicInteger();

    private ObjectName registeredName;

    public void register() {
        try {
            final MBeanServer server = ManagementFactory.getPlatformMBeanServer();
            final ObjectName name = new ObjectName(OBJECT_NAME);
            if (server.isRegistered(name)) {
                server.unregisterMBean(name);
            }
            server.registerMBean(this, name);
            registeredName = name;
        } catch (Exception e) {
            LOG.warn("Could not register {}: {}", OBJECT_NAME, e.getMessage());
        }
    }

    public void unregister() {
        if (registeredName == null) {
            return;
        }
        try {
            ManagementFactory.getPlatformMBeanServer().unregisterMBean(registeredName);
        } catch (Exception e) {
            LOG.debug("Could not unregister {}: {}", OBJECT_NAME, e.getMessage());
        } finally {
            registeredName = null;
        }
    }

    public void recordsRead(int n) {
        recordsRead.addAndGet(n);
    }

    public void eventsPublished(int n) {
        eventsPublished.addAndGet(n);
    }

    public void pollCompleted() {
        pollsCompleted.incrementAndGet();
    }

    public void pollFailed() {
        pollsFailed.incrementAndGet();
    }

    public void pollTruncated() {
        pollsTruncated.incrementAndGet();
    }

    public void setTargets(int n) {
        targets.set(n);
    }

    @Override
    public long getRecordsRead() {
        return recordsRead.get();
    }

    @Override
    public long getEventsPublished() {
        return eventsPublished.get();
    }

    @Override
    public long getPollsCompleted() {
        return pollsCompleted.get();
    }

    @Override
    public long getPollsFailed() {
        return pollsFailed.get();
    }

    @Override
    public long getPollsTruncated() {
        return pollsTruncated.get();
    }

    @Override
    public int getTargets() {
        return targets.get();
    }
}
