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

package org.opennms.netmgt.telemetry.protocols.netflow.adapter.ipfix;

import org.bson.BsonDocument;
import org.bson.RawBsonDocument;
import org.opennms.netmgt.flows.api.FlowRepository;
import org.opennms.netmgt.telemetry.api.adapter.TelemetryMessageLogEntry;
import org.opennms.netmgt.telemetry.protocols.flows.AbstractFlowAdapter;

import com.codahale.metrics.MetricRegistry;

public class IpfixAdapter extends AbstractFlowAdapter<BsonDocument> {

    public IpfixAdapter(final MetricRegistry metricRegistry,
                        final FlowRepository flowRepository) {
        super(metricRegistry, flowRepository, new IpfixConverter());
    }

    @Override
    protected BsonDocument parse(TelemetryMessageLogEntry message) {
        return new RawBsonDocument(message.getByteArray());
    }
}
