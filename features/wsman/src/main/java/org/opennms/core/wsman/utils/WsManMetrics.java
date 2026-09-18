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
package org.opennms.core.wsman.utils;

import java.lang.management.ManagementFactory;
import java.util.concurrent.atomic.AtomicLong;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Counters for everything the WS-Man integration does in this JVM: requests sent by
 * any client, monitor polls, collections. One instance per JVM, registered as
 * {@code OpenNMS:Name=WsMan} so the JMX collection can graph it.
 */
public final class WsManMetrics implements WsManMetricsMBean {

    private static final Logger LOG = LoggerFactory.getLogger(WsManMetrics.class);

    public static final String OBJECT_NAME = "OpenNMS:Name=WsMan";

    public static final WsManMetrics INSTANCE = new WsManMetrics();

    private final AtomicLong requests = new AtomicLong();
    private final AtomicLong requestFailures = new AtomicLong();
    private final AtomicLong commands = new AtomicLong();
    private final AtomicLong monitorPollsUp = new AtomicLong();
    private final AtomicLong monitorPollsDown = new AtomicLong();
    private final AtomicLong collectionsCompleted = new AtomicLong();
    private final AtomicLong collectionsFailed = new AtomicLong();
    private final AtomicLong attributesCollected = new AtomicLong();

    private WsManMetrics() {
        try {
            final MBeanServer server = ManagementFactory.getPlatformMBeanServer();
            final ObjectName name = new ObjectName(OBJECT_NAME);
            if (!server.isRegistered(name)) {
                server.registerMBean(this, name);
            }
        } catch (Exception e) {
            LOG.warn("Could not register {}: {}", OBJECT_NAME, e.getMessage());
        }
    }

    public void request() {
        requests.incrementAndGet();
    }

    public void requestFailed() {
        requestFailures.incrementAndGet();
    }

    public void command() {
        commands.incrementAndGet();
    }

    public void monitorPoll(boolean up) {
        (up ? monitorPollsUp : monitorPollsDown).incrementAndGet();
    }

    public void collectionCompleted() {
        collectionsCompleted.incrementAndGet();
    }

    public void collectionFailed() {
        collectionsFailed.incrementAndGet();
    }

    public void attributeCollected() {
        attributesCollected.incrementAndGet();
    }

    @Override
    public long getRequests() {
        return requests.get();
    }

    @Override
    public long getRequestFailures() {
        return requestFailures.get();
    }

    @Override
    public long getCommands() {
        return commands.get();
    }

    @Override
    public long getMonitorPollsUp() {
        return monitorPollsUp.get();
    }

    @Override
    public long getMonitorPollsDown() {
        return monitorPollsDown.get();
    }

    @Override
    public long getCollectionsCompleted() {
        return collectionsCompleted.get();
    }

    @Override
    public long getCollectionsFailed() {
        return collectionsFailed.get();
    }

    @Override
    public long getAttributesCollected() {
        return attributesCollected.get();
    }
}
