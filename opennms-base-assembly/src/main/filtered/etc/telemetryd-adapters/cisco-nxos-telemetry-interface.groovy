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

import groovy.util.logging.Slf4j

import java.util.List

import org.opennms.netmgt.collection.api.AttributeType
import org.opennms.netmgt.collection.support.builder.NodeLevelResource
import org.opennms.netmgt.telemetry.adapters.nxos.proto.TelemetryBis
import org.opennms.netmgt.telemetry.adapters.nxos.proto.TelemetryBis.TelemetryField

@Slf4j
class CollectionSetGenerator {
    static generate(agent, builder, telemetryMsg) {
        log.debug("Generating collection set for message: {}", telemetryMsg)
        // build the node-level resource
        NodeLevelResource nodeLevelResource = new NodeLevelResource(agent.getNodeId())
        Double load_avg_1min = null;
        String load_avg_str= null;
        Long collectionId = null;
        if (!telemetryMsg.getDataGpbkvList().isEmpty()) {
            if (!telemetryMsg.getDataGpbkvList().get(0).getFieldsList().isEmpty()
                    && telemetryMsg.getDataGpbkvList().get(0).getFieldsList().size() >= 2) {
                if (!telemetryMsg.getDataGpbkvList().get(0).getFieldsList().get(1).getFieldsList().isEmpty()) {
                    if (!telemetryMsg.getDataGpbkvList().get(0).getFieldsList().get(1).getFieldsList().get(0).getFieldsList()
                            .isEmpty()) {
                        if (telemetryMsg.getDataGpbkvList().get(0).getFieldsList().get(1).getFieldsList().get(0).getFieldsList()
                                .get(0).getName().equals("load_avg_1min")) {
                            load_avg_str = telemetryMsg.getDataGpbkvList().get(0).getFieldsList().get(1).getFieldsList().get(0)
                                    .getFieldsList().get(0).getStringValue();
                            load_avg_1min = Double.valueOf(load_avg_str);
                        }
                    }
                }
            }
        }
        if (load_avg_1min == null) {
            collectionId = telemetryMsg.getCollectionId();
            builder.withStringAttribute(nodeLevelResource, "stats", "collectionId", collectionId.toString());
        } else {
            builder.withNumericAttribute(nodeLevelResource, "stats", "load_avg_1min", load_avg_1min, AttributeType.GAUGE);
        }
    }
}

// The following variables are passed in as globals from the adapter:
// agent: the agent (or node) against which the metrics will be associated
// builder: a reference to a CollectionSetBuilder to which the resources/metrics should be added
// telemetryMsg: the message from which to extract the metrics

TelemetryBis.Telemetry telemetryMsg = msg

// Generate the CollectionSet
CollectionSetGenerator.generate(agent, builder, telemetryMsg)
