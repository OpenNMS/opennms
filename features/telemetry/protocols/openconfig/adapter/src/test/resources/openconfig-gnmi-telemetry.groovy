/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2025 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2025 The OpenNMS Group, Inc.
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
