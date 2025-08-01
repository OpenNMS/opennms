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
