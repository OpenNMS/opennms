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
package org.opennms.minion.heartbeat.producer;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.opennms.core.ipc.sink.api.MessageDispatcherFactory;
import org.opennms.core.ipc.sink.api.SyncDispatcher;
import org.opennms.distributed.core.api.MinionIdentity;
import org.opennms.features.apilayer.common.VersionBean;
import org.opennms.features.apilayer.minion.RuntimeInfoImpl;
import org.opennms.minion.heartbeat.common.HeartbeatModule;
import org.opennms.minion.heartbeat.common.MinionIdentityDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HeartbeatProducer {

    private static final Logger LOG = LoggerFactory.getLogger(HeartbeatProducer.class);

    private static final int PERIOD_MS = 30 * 1000;

    final Timer timer;

    public HeartbeatProducer(MinionIdentity identity, MessageDispatcherFactory messageDispatcherFactory) {
        final MinionIdentityDTO identityDTO = new MinionIdentityDTO(identity);
        // retrieve minion version
        final RuntimeInfoImpl minionInfo = new RuntimeInfoImpl(identity);
        final VersionBean minionVersion = (VersionBean) minionInfo.getVersion();
        final SyncDispatcher<MinionIdentityDTO> dispatcher = messageDispatcherFactory.createSyncDispatcher(new HeartbeatModule());
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    LOG.info("Sending heartbeat to Minion with id: {} at location: {}",
                            identity.getId(), identity.getLocation());
                    // We reuse the same DTO instead of creating a new object
                    // on every trigger, so we need to update the timestamp each time
                    identityDTO.setVersion(minionVersion.toString());
                    identityDTO.setTimestamp(new Date());
                    dispatcher.send(identityDTO);
                } catch (Throwable t) {
                    LOG.error("An error occured while sending the heartbeat. Will try again in {} ms", PERIOD_MS, t);
                }
            }
        }, 0, PERIOD_MS);
    }

    /**
     * Used to cancel the timer when the Blueprint is destroyed.
     */
    public void cancel() {
        timer.cancel();
    }
}
