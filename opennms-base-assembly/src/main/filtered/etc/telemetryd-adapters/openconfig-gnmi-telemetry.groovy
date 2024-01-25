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
import org.opennms.features.openconfig.proto.gnmi.Gnmi
import org.opennms.netmgt.collection.api.AttributeType
import org.opennms.netmgt.collection.support.builder.InterfaceLevelResource
import org.opennms.netmgt.collection.support.builder.NodeLevelResource

@Slf4j
class CollectionSetGenerator {
    static generate(agent, builder, response) {
        log.debug("Generating collection set for message: {}", response)
        NodeLevelResource nodeLevelResource = new NodeLevelResource(agent.getNodeId())
        // Sample code for parsing and building resources, real data may vary
        Gnmi.Notification notification = response.getUpdate()
        List<Gnmi.Update> updateList = notification.getUpdateList()
        String interfaceLabel = null;
        // Use more traditional java way since java/groovy syntax differs a lot with streams
        for (Gnmi.PathElem pathElem : notification.getPrefix().getElemList()) {
            if (pathElem.getName().equals("interface")) {
                interfaceLabel = pathElem.getKeyMap().get("name");
                break;
            }
        }
        if (interfaceLabel == null) {
            interfaceLabel = agent.getHostAddress();
        }
        InterfaceLevelResource interfaceResource = new InterfaceLevelResource(nodeLevelResource, interfaceLabel);
        for (Gnmi.Update update : updateList) {

            StringBuilder pathName = new StringBuilder();
            for (Gnmi.PathElem pathElem : update.getPath().getElemList()) {
                pathName.append(pathElem.getName()).append("/");
            }
            pathName.setLength(pathName.length() - 1); // Remove trailing slash

            if (update.getVal().getValueCase().equals(Gnmi.TypedValue.ValueCase.UINT_VAL)) {
                long value = update.getVal().getUintVal();
                builder.withNumericAttribute(interfaceResource, "gnmi-interfaces", pathName.toString(),
                        value, AttributeType.COUNTER);
            }
        }
    }
}

// The following variables are passed in as globals from the adapter:
// agent: the agent (or node) against which the metrics will be associated
// builder: a reference to a CollectionSetBuilder to which the resources/metrics should be added
// msg: the message from which to extract the metrics

Gnmi.SubscribeResponse response = msg

// Generate the CollectionSet
CollectionSetGenerator.generate(agent, builder, response)
