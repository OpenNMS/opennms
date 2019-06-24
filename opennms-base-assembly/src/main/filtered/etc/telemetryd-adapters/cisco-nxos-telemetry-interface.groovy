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

import org.opennms.netmgt.collection.api.AttributeType
import org.opennms.netmgt.collection.support.builder.DeferredGenericTypeResource
import org.opennms.netmgt.collection.support.builder.NodeLevelResource
import org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis
import org.opennms.netmgt.telemetry.protocols.nxos.adapter.NxosGpbParserUtil

@Slf4j
class CollectionSetGenerator {
    static generate(agent, builder, telemetryMsg) {
        log.debug("Generating collection set for message: {}", telemetryMsg)
        // build the node-level resource
        NodeLevelResource nodeLevelResource = new NodeLevelResource(agent.getNodeId())
        builder.withNumericAttribute(nodeLevelResource, "stats", "load_avg_1min",
                NxosGpbParserUtil.getValueAsDouble(telemetryMsg, "load_avg_1min"), AttributeType.GAUGE)
        
        for(TelemetryBis.TelemetryField row : NxosGpbParserUtil.getRowsFromTable(telemetryMsg, "cpu_usage")) {
            String cpuId = NxosGpbParserUtil.getValueFromRowAsString(row, "cpuid");
            DeferredGenericTypeResource genericTypeResource = new DeferredGenericTypeResource(nodeLevelResource, "nxosCpu", cpuId);
            builder.withNumericAttribute(genericTypeResource, "stats", "kernel",
                    NxosGpbParserUtil.getValueFromRowAsDouble(row, "kernel"), AttributeType.GAUGE)
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
