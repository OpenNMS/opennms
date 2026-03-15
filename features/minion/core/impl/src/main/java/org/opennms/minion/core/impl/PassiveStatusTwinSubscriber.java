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
package org.opennms.minion.core.impl;

import java.io.Closeable;
import java.io.IOException;

import org.opennms.core.ipc.twin.api.TwinSubscriber;
import org.opennms.netmgt.poller.passive.PassiveStatusConfig;
import org.opennms.netmgt.poller.passive.PassiveStatusHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Receives passive service status from Pollerd via Twin API and populates
 * the shared {@link PassiveStatusHolder} so that
 * {@link org.opennms.netmgt.poller.monitors.PassiveServiceMonitor} can
 * execute on Minion.
 */
public class PassiveStatusTwinSubscriber {

    private static final Logger LOG = LoggerFactory.getLogger(PassiveStatusTwinSubscriber.class);

    private TwinSubscriber twinSubscriber;
    private Closeable twinSubscription;

    public void bind(TwinSubscriber twinSubscriber) {
        this.twinSubscriber = twinSubscriber;
        subscribe();
    }

    public void unbind(TwinSubscriber twinSubscriber) {
        this.twinSubscriber = null;
        closeSubscription();
    }

    private void subscribe() {
        if (twinSubscriber == null) {
            LOG.warn("TwinSubscriber is null, cannot subscribe to passive status");
            return;
        }
        twinSubscription = twinSubscriber.subscribe(
                PassiveStatusConfig.TWIN_KEY,
                PassiveStatusConfig.class,
                this::applyPassiveStatus
        );
        LOG.info("Subscribed to passive status Twin updates");
    }

    private void applyPassiveStatus(PassiveStatusConfig config) {
        if (config == null) {
            LOG.debug("Received null passive status config, ignoring");
            return;
        }
        LOG.info("Received passive status update with {} entries", config.getEntries().size());
        PassiveStatusHolder.updateFromConfig(config);
    }

    public void close() {
        closeSubscription();
    }

    private void closeSubscription() {
        if (twinSubscription != null) {
            try {
                twinSubscription.close();
            } catch (IOException e) {
                LOG.warn("Error closing passive status Twin subscription", e);
            }
            twinSubscription = null;
        }
    }
}
