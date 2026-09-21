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
package org.opennms.features.kafka.producer.shell;

import java.util.Map;
import java.util.SortedMap;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.features.kafka.producer.collection.MetricTopicRouter;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.model.OnmsNode;

import com.codahale.metrics.Counter;

/**
 * Previews the Kafka topic that collected metrics for a given node, interface or service would be
 * routed to. The counterpart of {@code opennms:metadata-test} for metric routing.
 */
@Command(scope = "opennms", name = "kafka-metric-routing-test",
        description = "Show the Kafka topic that metrics for the given node, interface or service are routed to.")
@Service
@SuppressWarnings("java:S106")
public class MetricRoutingTest implements Action {

    @Reference
    private MetricTopicRouter metricTopicRouter;

    @Reference
    private NodeDao nodeDao;

    @Reference
    private SessionUtils sessionUtils;

    @Option(name = "-n", aliases = "--node", description = "Node ID or FS:FID", required = true, multiValued = false)
    private String nodeRef;

    @Option(name = "-i", aliases = "--interface-address", description = "Address of the IP interface the collecting service is assigned to", required = false, multiValued = false)
    private String interfaceAddress;

    @Option(name = "-s", aliases = "--service-name", description = "Service name", required = false, multiValued = false)
    private String serviceName;

    @Option(name = "-c", aliases = "--clear-cache", description = "Drop all cached routing decisions before resolving", required = false, multiValued = false)
    private boolean clearCache;

    @Override
    public Object execute() {
        if (clearCache) {
            metricTopicRouter.invalidateCache();
            System.out.printf("Cleared the metric routing cache.%n");
        }

        System.out.printf("Metric routing: %s%n", metricTopicRouter.isEnabled() ? "enabled" : "disabled");
        System.out.printf("Meta-data key : %s%n", metricTopicRouter.describeContextKey());
        System.out.printf("Default topic : %s%n", metricTopicRouter.getDefaultTopic());

        // The metadata collections are lazy, so hold a session open for the whole resolution -
        // the same reason opennms:metadata-test wraps its execute().
        sessionUtils.withReadOnlyTransaction(() -> {
            final OnmsNode node = nodeDao.get(nodeRef);
            if (node == null) {
                System.out.printf("Cannot find node with ID/FS:FID=%s.%n", nodeRef);
                return null;
            }

            final MetricTopicRouter.RoutingKey key = new MetricTopicRouter.RoutingKey(
                    node.getId(), interfaceAddress, serviceName);
            final MetricTopicRouter.Resolution resolution = metricTopicRouter.resolve(key);

            System.out.printf("---%n");
            System.out.printf("Node          : %d%n", node.getId());
            if (interfaceAddress != null) {
                System.out.printf("Interface     : %s%n", interfaceAddress);
            }
            if (serviceName != null) {
                System.out.printf("Service       : %s%n", serviceName);
            }
            System.out.printf("---%n");
            System.out.printf("Meta-data value: %s%n", resolution.value == null ? "<not set>" : "'" + resolution.value + "'");
            System.out.printf("Resolved from  : %s%n", resolution.scopeName == null ? "<nothing>" : resolution.scopeName);
            System.out.printf("Outcome        : %s%n", describe(resolution.status));
            System.out.printf("Topic          : %s%n", resolution.topic);
            return null;
        });

        printCounters();
        return null;
    }

    private static String describe(final MetricTopicRouter.Status status) {
        switch (status) {
            case DISABLED:
                return "DISABLED - metric routing is turned off, everything goes to the default topic";
            case ROUTED:
                return "ROUTED - the meta-data value is used as the topic name";
            case UNRESOLVED_NODE:
                return "UNRESOLVED_NODE - no node could be resolved, using the default topic";
            case UNMAPPED:
                return "UNMAPPED - the meta-data key is not set (or blank), using the default topic";
            case SANITIZE_REJECTED:
                return "SANITIZE_REJECTED - the value is not a legal Kafka topic name, using the default topic";
            default:
                return status.toString();
        }
    }

    private void printCounters() {
        final SortedMap<String, Counter> counters = metricTopicRouter.getMetricRegistry().getCounters();
        if (counters.isEmpty()) {
            return;
        }
        System.out.printf("---%nCounters:%n");
        for (final Map.Entry<String, Counter> entry : counters.entrySet()) {
            System.out.printf("  %-50s %d%n", entry.getKey(), entry.getValue().getCount());
        }
    }
}
