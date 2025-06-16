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

        // Record the sequence number
        builder.withSequenceNumber(jtiMsg.getSequenceNumber())

        for (Port.InterfaceInfos interfaceInfos : port.getInterfaceStatsList()) {
            // Use the given ifName for the label (we don't have the ifDescr of the physAddr in this context)
            String interfaceLabel = RrdLabelUtils.computeLabelForRRD(interfaceInfos.getIfName(), null, null);

            // Build an interface-level resource for every interface
            InterfaceLevelResource interfaceResource = new InterfaceLevelResource(nodeLevelResource, interfaceLabel);

            // Store the ifInOctets and ifOutOctets in a familar fashion, allowing the existing graph definitions to be used
            builder.withNumericAttribute(interfaceResource, "mib2-interfaces", "ifInOctets", interfaceInfos.getIngressStats().getIfOctets(), AttributeType.COUNTER);
            builder.withNumericAttribute(interfaceResource, "mib2-interfaces", "ifOutOctets", interfaceInfos.getEgressStats().getIfOctets(), AttributeType.COUNTER);

            // Store if1SecPkts
            builder.withNumericAttribute(interfaceResource, "mib2-interfaces", "ifIn1SecPkts", interfaceInfos.getIngressStats().getIf1SecPkts(), AttributeType.GAUGE);
            builder.withNumericAttribute(interfaceResource, "mib2-interfaces", "ifOut1SecPkts", interfaceInfos.getEgressStats().getIf1SecPkts(), AttributeType.GAUGE);
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
