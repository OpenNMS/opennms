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
package org.opennms.netmgt.passive;

import java.io.IOException;

import org.opennms.core.ipc.twin.api.TwinPublisher;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.poller.passive.PassiveStatusConfig;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.opennms.netmgt.events.api.EventListener;
import org.opennms.netmgt.events.api.model.IEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Publishes passive service status to Minion instances via the Twin API.
 *
 * <p>Listens for the same {@code passiveServiceStatus} events as {@link PassiveStatusKeeper},
 * and after each event publishes the current status map so that
 * {@link org.opennms.netmgt.poller.monitors.PassiveServiceMonitor} can execute on Minion.</p>
 *
 * <p>Follows the proven {@code SnmpV3UserTwinPublisher} pattern.</p>
 */
public class PassiveStatusTwinPublisher implements EventListener {

    private static final Logger LOG = LoggerFactory.getLogger(PassiveStatusTwinPublisher.class);

    private static final String PASSIVE_STATUS_UEI = "uei.opennms.org/services/passiveServiceStatus";

    private final TwinPublisher twinPublisher;
    private final PassiveStatusKeeper passiveStatusKeeper;
    private final EventIpcManager eventIpcManager;
    private TwinPublisher.Session<PassiveStatusConfig> twinSession;

    public PassiveStatusTwinPublisher(TwinPublisher twinPublisher,
                                      PassiveStatusKeeper passiveStatusKeeper,
                                      EventIpcManager eventIpcManager) {
        this.twinPublisher = twinPublisher;
        this.passiveStatusKeeper = passiveStatusKeeper;
        this.eventIpcManager = eventIpcManager;
    }

    public void init() {
        try {
            twinSession = twinPublisher.register(PassiveStatusConfig.TWIN_KEY, PassiveStatusConfig.class);
            eventIpcManager.addEventListener(this, PASSIVE_STATUS_UEI);
            publishCurrentStatus();
            LOG.info("Passive status Twin publisher initialized");
        } catch (Exception e) {
            // Twin publisher may not be available if the Kafka Twin feature isn't installed.
            // PassiveServiceMonitor still works on Pollerd via local PassiveStatusHolder.
            LOG.warn("Twin publisher not available — passive status sync to Minion disabled: {}", e.getMessage());
        }
    }

    public void publishCurrentStatus() {
        if (twinSession == null) {
            LOG.warn("Twin session not initialized, cannot publish passive status");
            return;
        }
        try {
            PassiveStatusConfig config = PassiveStatusConfig.fromStatusMap(passiveStatusKeeper.getStatusTable());
            twinSession.publish(config);
            LOG.debug("Published passive status config with {} entries", config.getEntries().size());
        } catch (IOException e) {
            LOG.error("Failed to publish passive status via Twin", e);
        }
    }

    @Override
    public String getName() {
        return "PassiveStatusTwinPublisher";
    }

    @Override
    public void onEvent(IEvent e) {
        if (PASSIVE_STATUS_UEI.equals(e.getUei())) {
            publishCurrentStatus();
        }
    }

    public void close() {
        eventIpcManager.removeEventListener(this);
        if (twinSession != null) {
            try {
                twinSession.close();
            } catch (IOException e) {
                LOG.warn("Error closing passive status Twin session", e);
            }
        }
    }
}
