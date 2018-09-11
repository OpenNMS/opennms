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

import java.util.List;

import org.bson.BsonDocument;
import org.bson.BsonValue;
import org.opennms.netmgt.flows.api.Converter;
import org.opennms.netmgt.flows.api.Flow;

import com.google.common.collect.Lists;

public class SFlowConverter implements Converter<BsonDocument> {

    @Override
    public List<Flow> convert(final BsonDocument packet) {
        final List<Flow> result = Lists.newLinkedList();

        final SFlow.Header header = new SFlow.Header(packet);

        for (final BsonValue sample : packet.getDocument("data").getArray("samples")) {
            final BsonDocument sampleDocument = sample.asDocument();

            if ("0:1".equals(sampleDocument.get("format").asString().getValue()) ||
                "0:3".equals(sampleDocument.get("format").asString().getValue())) {
                // Handle only (expanded) flow samples
                result.add(new SFlow(header, sampleDocument.get("data").asDocument()));
            }
        }

        return result;
    }
}
