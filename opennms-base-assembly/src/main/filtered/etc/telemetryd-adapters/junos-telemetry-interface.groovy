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
import org.opennms.core.utils.RrdLabelUtils
import org.opennms.netmgt.collection.api.AttributeType
import org.opennms.netmgt.telemetry.protocols.jti.adapter.proto.Port
import org.opennms.netmgt.telemetry.protocols.jti.adapter.proto.TelemetryTop
import org.opennms.netmgt.collection.support.builder.InterfaceLevelResource
import org.opennms.netmgt.collection.support.builder.NodeLevelResource

@Slf4j
class CollectionSetGenerator {
    static generate(agent, builder, jtiMsg) {
        log.debug("Generating collection set for message: {}", jtiMsg)
        // First, build the top node-level resources, as other resource types
        // will depend on this one
        NodeLevelResource nodeLevelResource = new NodeLevelResource(agent.getNodeId())

        // Retrieve the interface statistic extension
        TelemetryTop.EnterpriseSensors entSensors = jtiMsg.getEnterprise()
        TelemetryTop.JuniperNetworksSensors jnprSensors = entSensors.getExtension(TelemetryTop.juniperNetworks);
        Port.GPort port = jnprSensors.getExtension(Port.jnprInterfaceExt);

        for (Port.InterfaceInfos interfaceInfos : port.getInterfaceStatsList()) {
            // Use the given ifName for the label (we don't have the ifDescr of the physAddr in this context)
            String interfaceLabel = RrdLabelUtils.computeLabelForRRD(interfaceInfos.getIfName(), null, null);

            // Build an interface-level resource for every interface
            InterfaceLevelResource interfaceResource = new InterfaceLevelResource(nodeLevelResource, interfaceLabel);

            // Store the ifInOctets and ifOutOctets in a familar fashion, allowing the existing graph definitions to be used
            builder.withNumericAttribute(interfaceResource, "mib2-interfaces", "ifInOctets", interfaceInfos.getIngressStats().getIfOctets(), AttributeType.COUNTER);
            builder.withNumericAttribute(interfaceResource, "mib2-interfaces", "ifOutOctets", interfaceInfos.getEgressStats().getIfOctets(), AttributeType.COUNTER);
        }
    }
}

// The following variables are passed in as globals from the adapter:
// agent: the agent (or node) against which the metrics will be associated
// builder: a reference to a CollectionSetBuilder to which the resources/metrics should be added
// msg: the message from which to extract the metrics

// In our case, the msg will a JTI msg
TelemetryTop.TelemetryStream jtiMsg = msg

// Generate the CollectionSet
CollectionSetGenerator.generate(agent, builder, jtiMsg)
