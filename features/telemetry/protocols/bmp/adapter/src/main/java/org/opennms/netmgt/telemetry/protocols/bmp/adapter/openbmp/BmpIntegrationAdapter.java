/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import org.opennms.netmgt.telemetry.api.adapter.TelemetryMessageLog;
import org.opennms.netmgt.telemetry.api.adapter.TelemetryMessageLogEntry;
import org.opennms.netmgt.telemetry.config.api.AdapterDefinition;
import org.opennms.netmgt.telemetry.protocols.bmp.adapter.BmpAdapterCommon;
import org.opennms.netmgt.telemetry.protocols.collection.AbstractCollectionAdapter;
import org.opennms.netmgt.telemetry.protocols.collection.CollectionSetWithAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;

public class BmpIntegrationAdapter extends AbstractCollectionAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(BmpIntegrationAdapter.class);

    private final AtomicLong sequence = new AtomicLong();

    private final BmpMessageHandler messageHandler;

    public BmpIntegrationAdapter(final AdapterDefinition adapterConfig,
                                 final MetricRegistry metricRegistry,
                                 final BmpMessageHandler messageHandler) {
        super(adapterConfig, metricRegistry);
        this.messageHandler = Objects.requireNonNull(messageHandler);
    }
    
    @Override
    public Stream<CollectionSetWithAgent> handleCollectionMessage(TelemetryMessageLogEntry messageLogEntry, TelemetryMessageLog messageLog) {
        BmpAdapterCommon.handleBmpMessage(messageLogEntry, messageLog, messageHandler, sequence);
        return Stream.empty();
    }


    @Override
    public void destroy() {
        this.messageHandler.close();
        super.destroy();
    }


}
