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
        log.debug("Generating collection set for node {} from message: {}", agent.getNodeId(), telemetryMsg)

        def nodeLevelResource = new NodeLevelResource(agent.getNodeId())

        /*
         * Example of retrieving data using NX-API from a Nexus Switch.
         * The following code will process the data when the switch is configured like this:
         * 
         * telemetry
         *   destination-group 100
         *     ip address 192.168.205.253 port 50001 protocol UDP encoding GPB 
         *   sensor-group 200
         *     data-source NX-API
         *     path "show system resources" depth 0
         *   subscription 300
         *     dst-grp 100
         *     snsr-grp 200 sample-interval 300000
         */ 
        if (telemetryMsg.getEncodingPath().equals("show system resources")) {
            builder.withNumericAttribute(nodeLevelResource, "nxos-stats", "load_avg_1min",
                NxosGpbParserUtil.getValueAsDouble(telemetryMsg, "load_avg_1min"), AttributeType.GAUGE)
            builder.withNumericAttribute(nodeLevelResource, "nxos-stats", "memory_usage_used",
                NxosGpbParserUtil.getValueAsDouble(telemetryMsg, "memory_usage_used"), AttributeType.GAUGE)
        
            NxosGpbParserUtil.getRowsFromTable(telemetryMsg, "cpu_usage").each { row ->
                def cpuId = NxosGpbParserUtil.getValueFromRowAsString(row, "cpuid")
                def genericTypeResource = new DeferredGenericTypeResource(nodeLevelResource, "nxosCpu", cpuId)
                ["idle", "kernel", "user"].each { metric ->
                    builder.withNumericAttribute(genericTypeResource, "nxos-cpu-stats", metric,
                        NxosGpbParserUtil.getValueFromRowAsDouble(row, metric), AttributeType.GAUGE)
                }
            }
        }

        /*
         * Example of retrieving data using DME from a Nexus Switch (Hint: the Visore Tool can help in this regard)
         * The following code will process the data when the switch is configured like this:
         * 
         * telemetry
         *   destination-group 100
         *     ip address 192.168.205.253 port 50001 protocol UDP encoding GPB 
         *   sensor-group 210
         *     path sys/intf/phys-[eth1/1]/dbgIfHCIn depth 0
         *     path sys/intf/phys-[eth1/1]/dbgIfHCOut depth 0
         *     path sys/intf/phys-[eth1/2]/dbgIfHCIn depth 0
         *     path sys/intf/phys-[eth1/2]/dbgIfHCOut depth 0
         *   subscription 300
         *     dst-grp 100
         *     snsr-grp 210 sample-interval 300000
         */ 
        def m;
        if ((m = telemetryMsg.getEncodingPath() =~ /sys\/intf\/phys-\[(.+)\]\/dbgIfHC(In|Out)/)) {
            def intfId = m.group(1).replaceAll(/\//,"-")
            def statsType = m.group(2)
            def genericTypeResource = new DeferredGenericTypeResource(nodeLevelResource, "nxosIntf", intfId)
            ["ucastPkts", "multicastPkts", "broadcastPkts", "octets"].each { metric ->
                builder.withNumericAttribute(genericTypeResource, "nxos-intfHC$statsType", "$metric$statsType",
                    NxosGpbParserUtil.getValueAsDouble(telemetryMsg, metric), AttributeType.COUNTER)
            }
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
