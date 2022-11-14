/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
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
import org.opennms.netmgt.collection.api.AttributeType
import org.opennms.netmgt.telemetry.protocols.graphite.adapter.GraphiteMetric
import org.opennms.netmgt.collection.support.builder.InterfaceLevelResource
import org.opennms.netmgt.collection.support.builder.NodeLevelResource

/**
 * Simple graphite handler. No schema support, you'll have to parse/handle the message paths yourself.
 *
 * You can send test data easily by doing something like:
 * <code>echo "eth0.my-metric 100 `date +%s`" | nc -u -w1 localhost 2003</code>
 *
 * Alternate version, using IPv4 and setting 'localhost' as the source:
 * <code>echo "eth0.my-metric 100 `date +%s`" | nc -u -w1 -4 -s localhost localhost 2003</code>
 *
 * Note that log messages using 'log' will show up in etc/karaf.log. Depending on your Karaf log configuration
 * settings, you may need to use 'log.error()' for them to show up.
 *
 * Scripts are cached but should automatically reload when updated and saved, however this may not occur if there
 * are compilation errors. If you want to force reload, you can run the following karaf command:
 *
 * <code>opennms:reload-daemon telemetryd</code>
 */
@Slf4j
class CollectionSetGenerator {
    static generate(agent, builder, graphiteMsg) {
        log.debug("Generating collection set for message: {}", graphiteMsg)
        // First, build the top node-level resources, as other resource types
        // will depend on this one
        NodeLevelResource nodeLevelResource = new NodeLevelResource(agent.getNodeId())

        if (graphiteMsg.path.startsWith("eth")) {
            // finding the mac address for the interface is left as an exercise to the reader ;)
            String ifaceLabel = RrdLabelUtils.computeLabelForRRD(graphiteMsg.path.split("\\.")[0], null, null);
            InterfaceLevelResource interfaceResource = new InterfaceLevelResource(nodeLevelResource, ifaceLabel);
            builder.withGauge(interfaceResource, "some-group", graphiteMsg.path.split("\\.")[1], graphiteMsg.longValue());
        } else {
            log.warn("I don't know how to handle this message from graphite. :(  {}", graphiteMsg);
        }
    }
}

// The following variables are passed in as globals from the adapter:
// agent: the agent (or node) against which the metrics will be associated
// builder: a reference to a CollectionSetBuilder to which the resources/metrics should be added
// msg: the message from which to extract the metrics

// In our case, the msg will a GraphiteMetric object
GraphiteMetric graphiteMsg = msg

// Generate the CollectionSet
CollectionSetGenerator.generate(agent, builder, graphiteMsg)
