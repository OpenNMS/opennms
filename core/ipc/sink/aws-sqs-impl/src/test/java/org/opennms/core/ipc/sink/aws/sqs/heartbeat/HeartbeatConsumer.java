/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.core.ipc.sink.aws.sqs.heartbeat;

import org.opennms.core.ipc.sink.api.MessageConsumer;
import org.opennms.core.ipc.sink.api.SinkModule;

import com.codahale.metrics.Meter;

/**
 * The Class HeartbeatConsumer.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class HeartbeatConsumer implements MessageConsumer<Heartbeat,Heartbeat> {

    /** The heartbeat module. */
    private final HeartbeatModule heartbeatModule;

    /** The received meter. */
    private final Meter receivedMeter;

    /**
     * Instantiates a new heartbeat consumer.
     *
     * @param heartbeatModule the heartbeat module
     * @param receivedMeter the received meter
     */
    public HeartbeatConsumer(HeartbeatModule heartbeatModule, Meter receivedMeter) {
        this.heartbeatModule = heartbeatModule;
        this.receivedMeter = receivedMeter;
    }

    /* (non-Javadoc)
     * @see org.opennms.core.ipc.sink.api.MessageConsumer#getModule()
     */
    @Override
    public SinkModule<Heartbeat,Heartbeat> getModule() {
        return heartbeatModule;
    }

    /* (non-Javadoc)
     * @see org.opennms.core.ipc.sink.api.MessageConsumer#handleMessage(org.opennms.core.ipc.sink.api.Message)
     */
    @Override
    public void handleMessage(Heartbeat message) {
        receivedMeter.mark();
    }

}
