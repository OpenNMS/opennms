/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.ipc;

import org.opennms.core.ipc.sink.api.AggregationPolicy;
import org.opennms.core.ipc.sink.api.AsyncPolicy;
import org.opennms.core.ipc.sink.xml.AbstractXmlSinkModule;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.telemetry.listeners.api.TelemetryMessage;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;

public class TelemetrySinkModule extends AbstractXmlSinkModule<TelemetryMessage, TelemetryMessageLogDTO> {
    private static final String MODULE_ID_PREFIX = "Telemetry.";

    private static final int DEFAULT_NUM_THREADS = Runtime.getRuntime().availableProcessors() * 2;
    private static final int DEFAULT_BATCH_SIZE = 1000;
    private static final int DEFAULT_BATCH_INTERVAL_MS = 500;
    private static final int DEFAULT_QUEUE_SIZE = 10000;

    @Autowired
    private DistPollerDao distPollerDao;

    private final ProtocolDefinition protocol;

    private final String moduleId;

    public TelemetrySinkModule(ProtocolDefinition protocol) {
        super(TelemetryMessageLogDTO.class);
        this.protocol = Objects.requireNonNull(protocol);
        this.moduleId = MODULE_ID_PREFIX + protocol.getName();
    }

    @Override
    public String getId() {
        return moduleId;
    }

    @Override
    public int getNumConsumerThreads() {
        return protocol.getNumThreads().orElse(DEFAULT_NUM_THREADS);
    }

    @Override
    public AggregationPolicy<TelemetryMessage, TelemetryMessageLogDTO> getAggregationPolicy() {
        final String systemId = distPollerDao.whoami().getId();
        final String systemLocation = distPollerDao.whoami().getLocation();
        return new AggregationPolicy<TelemetryMessage, TelemetryMessageLogDTO>() {
            @Override
            public int getCompletionSize() {
                return protocol.getBatchSize().orElse(DEFAULT_BATCH_SIZE);
            }

            @Override
            public int getCompletionIntervalMs() {
                return protocol.getBatchIntervalMs().orElse(DEFAULT_BATCH_INTERVAL_MS);
            }

            @Override
            public Object key(TelemetryMessage telemetryMessage) {
                return telemetryMessage.getSource();
            }

            @Override
            public TelemetryMessageLogDTO aggregate(TelemetryMessageLogDTO oldLog, TelemetryMessage message) {
                if (oldLog == null) {
                    oldLog = new TelemetryMessageLogDTO(systemLocation, systemId, message.getSource());
                }
                final TelemetryMessageDTO messageDTO = new TelemetryMessageDTO(message.getBuffer());
                oldLog.getMessages().add(messageDTO);
                return oldLog;
            }
        };
    }

    @Override
    public AsyncPolicy getAsyncPolicy() {
        return new AsyncPolicy() {
            @Override
            public int getQueueSize() {
                return protocol.getQueueSize().orElse(DEFAULT_QUEUE_SIZE);
            }

            @Override
            public int getNumThreads() {
                return protocol.getNumThreads().orElse(DEFAULT_NUM_THREADS);
            }

            @Override
            public boolean isBlockWhenFull() {
                // Always block when full
                return true;
            }
        };
    }

    public DistPollerDao getDistPollerDao() {
        return distPollerDao;
    }

    public void setDistPollerDao(DistPollerDao distPollerDao) {
        this.distPollerDao = distPollerDao;
    }
}
