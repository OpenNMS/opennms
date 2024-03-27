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
