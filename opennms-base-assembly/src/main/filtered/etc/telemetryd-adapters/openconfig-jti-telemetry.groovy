/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020-2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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
import org.opennms.core.utils.RrdLabelUtils
import org.opennms.features.openconfig.proto.jti.Telemetry
import org.opennms.netmgt.collection.api.AttributeType
import org.opennms.netmgt.collection.support.builder.InterfaceLevelResource
import org.opennms.netmgt.collection.support.builder.NodeLevelResource


@Slf4j
class CollectionSetGenerator {
    static generate(agent, builder, openConfigData) {
        log.debug("Generating collection set for message: {}", openConfigData)
        NodeLevelResource nodeLevelResource = new NodeLevelResource(agent.getNodeId())
        // Record the sequence number
        builder.withSequenceNumber(openConfigData.getSequenceNumber())
        // This is just an example on how to build interface level resources.
        Optional<Telemetry.KeyValue> ifName =
                openConfigData.getKvList().stream().filter({keyValue -> keyValue.getKey().contains("name")}).findFirst();
        if (ifName.isPresent()) {
            String interfaceLabel = RrdLabelUtils.computeLabelForRRD(ifName.get().getStrValue(), null, null)
            InterfaceLevelResource interfaceResource = new InterfaceLevelResource(nodeLevelResource, interfaceLabel)
            openConfigData.getKvList().stream().filter({keyValue -> keyValue.getKey().contains("in-octets")}).findFirst()
                    .ifPresent({kv ->
                builder.withNumericAttribute(interfaceResource, "mib2-interfaces", "ifInOctets", kv.getDoubleValue(), AttributeType.COUNTER)})

        }

    }
}

// The following variables are passed in as globals from the adapter:
// agent: the agent (or node) against which the metrics will be associated
// builder: a reference to a CollectionSetBuilder to which the resources/metrics should be added
// msg: the message from which to extract the metrics

Telemetry.OpenConfigData openConfigData = msg

// Generate the CollectionSet
CollectionSetGenerator.generate(agent, builder, openConfigData)
