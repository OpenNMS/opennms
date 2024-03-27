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
