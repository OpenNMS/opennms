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

package org.opennms.netmgt.telemetry.protocols.sflow.adapter;

import static org.opennms.netmgt.telemetry.protocols.common.utils.BsonUtils.first;
import static org.opennms.netmgt.telemetry.protocols.common.utils.BsonUtils.get;
import static org.opennms.netmgt.telemetry.protocols.common.utils.BsonUtils.getArray;
import static org.opennms.netmgt.telemetry.protocols.common.utils.BsonUtils.getDocument;
import static org.opennms.netmgt.telemetry.protocols.common.utils.BsonUtils.getString;

import java.time.Instant;
import java.util.List;

import org.bson.BsonDocument;
import org.bson.BsonValue;
import org.bson.RawBsonDocument;
import org.opennms.netmgt.flows.api.Flow;
import org.opennms.netmgt.flows.processing.Pipeline;
import org.opennms.netmgt.telemetry.api.adapter.TelemetryMessageLogEntry;
import org.opennms.netmgt.telemetry.config.api.AdapterDefinition;
import org.opennms.netmgt.telemetry.protocols.flows.AbstractFlowAdapter;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.Lists;

public class SFlowAdapter extends AbstractFlowAdapter<BsonDocument> {

    public SFlowAdapter(final AdapterDefinition adapterConfig,
                        final MetricRegistry metricRegistry,
                        final Pipeline pipeline) {
        super(adapterConfig, metricRegistry, pipeline);
    }

    @Override
    protected BsonDocument parse(TelemetryMessageLogEntry message) {
        return new RawBsonDocument(message.getByteArray());
    }

    private static RuntimeException invalidDocument() {
        throw new RuntimeException("Invalid Document");
    }

    @Override
    public List<Flow> convert(final BsonDocument packet, final Instant receivedAt) {
        return convertDocument(packet, receivedAt);
    }

    public static List<Flow> convertDocument(final BsonDocument packet, final Instant receivedAt) {
        final List<Flow> result = Lists.newLinkedList();

        final SFlow.Header header = new SFlow.Header(packet);

        for (final BsonValue sample : getArray(packet, "data", "samples").orElseThrow(SFlowAdapter::invalidDocument)) {
            final BsonDocument sampleDocument = sample.asDocument();

            final String format = getString(sampleDocument, "format").orElseThrow(SFlowAdapter::invalidDocument);
            if ("0:1".equals(format) || "0:3".equals(format)) {
                // Handle only (expanded) flow samples

                if (first(get(sampleDocument, "data", "flows", "0:1", "ipv4"),
                          get(sampleDocument, "data", "flows", "0:1", "ipv6"),
                          get(sampleDocument, "data", "flows", "0:3"),
                          get(sampleDocument, "data", "flows", "0:4")).isPresent()) {
                    // Handle only flows containing IP related records
                    result.add(new SFlow(header, getDocument(sampleDocument, "data").orElseThrow(SFlowAdapter::invalidDocument), receivedAt));
                }
            }
        }

        return result;
    }
}
