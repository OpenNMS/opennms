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
package org.opennms.features.events.sink.dispatcher;

import org.opennms.netmgt.config.api.EventdConfig;

/**
 *  This is used to initialize sink module on Minion.
 *  Doesn't support listening events on TCP/UDP.
 */
public class EventdConfigBean implements EventdConfig {

    private int numThreads;
    private int queueSize;
    private int batchSize;
    private int batchIntervalMs;

    @Override
    public String getTCPIpAddress() {
        return null;
    }

    @Override
    public int getTCPPort() {
        return 0;
    }

    @Override
    public String getUDPIpAddress() {
        return null;
    }

    @Override
    public int getUDPPort() {
        return 0;
    }

    @Override
    public int getReceivers() {
        return 0;
    }

    @Override
    public int getQueueLength() {
        return 0;
    }

    @Override
    public String getSocketSoTimeoutRequired() {
        return null;
    }

    @Override
    public int getSocketSoTimeoutPeriod() {
        return 0;
    }

    @Override
    public boolean hasSocketSoTimeoutPeriod() {
        return false;
    }

    @Override
    public String getGetNextEventID() {
        return null;
    }

    @Override
    public int getNumThreads() {
        if (numThreads <= 0) {
            return Runtime.getRuntime().availableProcessors() * 2;
        }
        return numThreads;
    }

    public void setNumThreads(int numThreads) {
        this.numThreads = numThreads;
    }

    @Override
    public int getQueueSize() {
        return queueSize;
    }

    public void setQueueSize(int queueSize) {
        this.queueSize = queueSize;
    }

    @Override
    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    @Override
    public int getBatchIntervalMs() {
        return batchIntervalMs;
    }

    public void setBatchIntervalMs(int batchIntervalMs) {
        this.batchIntervalMs = batchIntervalMs;
    }
}
