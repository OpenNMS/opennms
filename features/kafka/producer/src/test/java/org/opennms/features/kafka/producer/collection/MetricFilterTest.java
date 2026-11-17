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
package org.opennms.features.kafka.producer.collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.util.Map;

import org.junit.Test;
import org.opennms.features.kafka.producer.model.CollectionSetProtos;
import org.springframework.expression.ParseException;

public class MetricFilterTest {

    private static CollectionSetProtos.NodeLevelResource node(int id, String label, String location) {
        final CollectionSetProtos.NodeLevelResource.Builder builder = CollectionSetProtos.NodeLevelResource.newBuilder()
                .setNodeId(id)
                .setForeignSource("fs")
                .setForeignId("fid-" + id);
        if (label != null) {
            builder.setNodeLabel(label);
        }
        if (location != null) {
            builder.setLocation(location);
        }
        return builder.build();
    }

    private static CollectionSetProtos.CollectionSetResource nodeResource(int id, String label) {
        return CollectionSetProtos.CollectionSetResource.newBuilder()
                .setNode(node(id, label, "Default"))
                .setResourceId("node[" + id + "].nodeSnmp[]")
                .setResourceTypeName("nodeSnmp")
                .addNumeric(CollectionSetProtos.NumericAttribute.newBuilder().setName("loadavg1").setValue(1.0))
                .build();
    }

    private static CollectionSetProtos.CollectionSetResource interfaceResource(int id, int ifIndex) {
        return CollectionSetProtos.CollectionSetResource.newBuilder()
                .setInterface(CollectionSetProtos.InterfaceLevelResource.newBuilder()
                        .setNode(node(id, "node-" + id, "Raleigh"))
                        .setInstance("eth" + ifIndex + "-aabbcc")
                        .setIfIndex(ifIndex))
                .setResourceId("node[" + id + "].interfaceSnmp[eth" + ifIndex + "-aabbcc]")
                .setResourceName("eth" + ifIndex + "-aabbcc")
                .setResourceTypeName("interfaceSnmp")
                .build();
    }

    private static CollectionSetProtos.CollectionSetResource genericResource(int id, String type, String instance) {
        return CollectionSetProtos.CollectionSetResource.newBuilder()
                .setGeneric(CollectionSetProtos.GenericTypeResource.newBuilder()
                        .setNode(node(id, "node-" + id, "Raleigh"))
                        .setType(type)
                        .setInstance(instance))
                .setResourceId("node[" + id + "]." + type + "[" + instance + "]")
                .setResourceName(instance)
                .setResourceTypeName(type)
                .build();
    }

    private static CollectionSetProtos.CollectionSetResource responseResource(String ip, String location) {
        return responseResource(ip, location, null);
    }

    private static CollectionSetProtos.CollectionSetResource responseResource(String ip, String location, CollectionSetProtos.NodeLevelResource node) {
        final CollectionSetProtos.ResponseTimeResource.Builder response = CollectionSetProtos.ResponseTimeResource.newBuilder()
                .setInstance(ip);
        if (location != null) {
            response.setLocation(location);
        }
        if (node != null) {
            response.setNode(node);
        }
        return CollectionSetProtos.CollectionSetResource.newBuilder().setResponse(response).build();
    }

    @Test
    public void parseReturnsNullForBlankExpression() {
        assertThat(MetricFilter.parse(null), nullValue());
        assertThat(MetricFilter.parse(""), nullValue());
        assertThat(MetricFilter.parse("nodeId == 1"), notNullValue());
    }

    @Test(expected = ParseException.class)
    public void parseRejectsInvalidExpression() {
        MetricFilter.parse("nodeId == ");
    }

    @Test
    public void forwardsAndDropsByNodeId() {
        final MetricFilter filter = MetricFilter.parse("nodeId == 5");
        assertThat(filter.evaluate(nodeResource(5, "five")).getOutcome(), equalTo(MetricFilter.Outcome.FORWARD));
        assertThat(filter.evaluate(nodeResource(6, "six")).getOutcome(), equalTo(MetricFilter.Outcome.DROP));
        assertThat(filter.evaluate(interfaceResource(5, 2)).getOutcome(), equalTo(MetricFilter.Outcome.FORWARD));
        assertThat(filter.evaluate(genericResource(5, "hrStorageIndex", "1")).getOutcome(), equalTo(MetricFilter.Outcome.FORWARD));
        // Response time resources match through the node they carry
        assertThat(filter.evaluate(responseResource("10.0.0.1", null, node(5, "five", "Default"))).getOutcome(), equalTo(MetricFilter.Outcome.FORWARD));
        assertThat(filter.evaluate(responseResource("10.0.0.1", null, node(6, "six", "Default"))).getOutcome(), equalTo(MetricFilter.Outcome.DROP));
        assertThat(filter.evaluate(responseResource("10.0.0.1", null)).getOutcome(), equalTo(MetricFilter.Outcome.DROP));
    }

    @Test
    public void exposesResourceTypeNamesLikeThePersister() {
        assertThat(MetricFilter.parse("resourceTypeName == 'nodeSnmp'").evaluate(nodeResource(1, "one")).getOutcome(), equalTo(MetricFilter.Outcome.FORWARD));
        assertThat(MetricFilter.parse("resourceTypeName == 'interfaceSnmp' and ifIndex == 3").evaluate(interfaceResource(1, 3)).getOutcome(), equalTo(MetricFilter.Outcome.FORWARD));
        assertThat(MetricFilter.parse("resourceTypeName == 'hrStorageIndex' and instance == '7'").evaluate(genericResource(1, "hrStorageIndex", "7")).getOutcome(), equalTo(MetricFilter.Outcome.FORWARD));
        assertThat(MetricFilter.parse("getResourceTypeName() == 'responseTime'").evaluate(responseResource("10.0.0.1", null)).getOutcome(), equalTo(MetricFilter.Outcome.FORWARD));
    }

    @Test
    public void evaluationErrorIsReportedAndForwarded() {
        // nodeLabel is null for a node without a label, so calling a method on it fails
        final MetricFilter filter = MetricFilter.parse("nodeLabel.startsWith('x')");
        final MetricFilter.Result result = filter.evaluate(nodeResource(1, null));
        assertThat(result.getOutcome(), equalTo(MetricFilter.Outcome.ERROR));
        assertThat(result.getCause(), notNullValue());
        assertThat(result.isForwarded(), is(true));
        assertThat(filter.shouldForward(nodeResource(1, null)), is(true));
    }

    @Test
    public void nullResultIsDropped() {
        // A non boolean expression that yields null converts to null, which is treated as false
        final MetricFilter filter = MetricFilter.parse("nodeLabel");
        assertThat(filter.evaluate(nodeResource(1, null)).getOutcome(), equalTo(MetricFilter.Outcome.DROP));
    }

    @Test
    public void applyKeepsMatchingResourcesAndTimestamp() {
        final CollectionSetProtos.CollectionSet collectionSet = CollectionSetProtos.CollectionSet.newBuilder()
                .setTimestamp(42L)
                .addResource(nodeResource(5, "five"))
                .addResource(interfaceResource(5, 1))
                .addResource(nodeResource(6, "six"))
                .build();

        final CollectionSetProtos.CollectionSet filtered = MetricFilter.parse("nodeId == 5").apply(collectionSet);
        assertThat(filtered.getTimestamp(), equalTo(42L));
        assertThat(filtered.getResourceCount(), equalTo(2));
        assertThat(filtered.getResource(0), equalTo(nodeResource(5, "five")));
        assertThat(filtered.getResource(1), equalTo(interfaceResource(5, 1)));

        final CollectionSetProtos.CollectionSet empty = MetricFilter.parse("nodeId == 7").apply(collectionSet);
        assertThat(empty.getTimestamp(), equalTo(42L));
        assertThat(empty.getResourceCount(), equalTo(0));
    }

    @Test
    public void locationFollowsHistoricalBehaviour() {
        final MetricFilter filter = MetricFilter.parse("location == 'Raleigh'");
        assertThat(filter.evaluate(interfaceResource(1, 1)).getOutcome(), equalTo(MetricFilter.Outcome.FORWARD));
        // A response time resource reports its own location, not the node's
        assertThat(filter.evaluate(responseResource("10.0.0.1", "Raleigh", node(1, "one", "Raleigh"))).getOutcome(), equalTo(MetricFilter.Outcome.FORWARD));
        // At the default location the response time resource has no location of its own, so the node's is not used either
        assertThat(new MetricFilter.ResourceFilterContext(responseResource("10.0.0.1", null, node(1, "one", "Default"))).getLocation(), nullValue());
        // A response time resource without node information reports no location
        assertThat(new MetricFilter.ResourceFilterContext(responseResource("10.0.0.1", "Raleigh")).getLocation(), nullValue());
        assertThat(filter.evaluate(responseResource("10.0.0.1", "Raleigh")).getOutcome(), equalTo(MetricFilter.Outcome.DROP));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void deprecatedPersisterContextStillWorks() {
        final KafkaPersister.ResourceFilterContext ctx = new KafkaPersister.ResourceFilterContext(interfaceResource(5, 3));
        assertThat(ctx.getNodeId(), equalTo(5L));
        assertThat(ctx.getIfIndex(), equalTo(3));
    }

    @Test
    public void contextExposesAllPropertiesInStableOrder() {
        final Map<String, Object> map = new MetricFilter.ResourceFilterContext(interfaceResource(5, 3)).toMap();
        assertThat(map.keySet(), contains("nodeId", "nodeLabel", "foreignSource", "foreignId", "location",
                "resourceTypeName", "resourceName", "resourceId", "ifIndex", "instance"));
        assertThat(map.get("nodeId"), equalTo(5L));
        assertThat(map.get("nodeLabel"), equalTo("node-5"));
        assertThat(map.get("foreignSource"), equalTo("fs"));
        assertThat(map.get("foreignId"), equalTo("fid-5"));
        assertThat(map.get("location"), equalTo("Raleigh"));
        assertThat(map.get("resourceTypeName"), equalTo("interfaceSnmp"));
        assertThat(map.get("resourceName"), equalTo("eth3-aabbcc"));
        assertThat(map.get("resourceId"), equalTo("node[5].interfaceSnmp[eth3-aabbcc]"));
        assertThat(map.get("ifIndex"), equalTo(3));
        assertThat(map.get("instance"), equalTo("eth3-aabbcc"));

        final Map<String, Object> nodeMap = new MetricFilter.ResourceFilterContext(nodeResource(5, "five")).toMap();
        assertThat(nodeMap.get("resourceName"), nullValue());
        assertThat(nodeMap.get("ifIndex"), nullValue());
        assertThat(nodeMap.get("instance"), nullValue());
    }
}
