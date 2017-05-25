/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.minion.heartbeat.producer;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.opennms.core.ipc.sink.api.MessageDispatcherFactory;
import org.opennms.core.ipc.sink.api.SyncDispatcher;
import org.opennms.minion.core.api.MinionIdentity;
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
