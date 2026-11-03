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
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.features.kafka.producer.model.CollectionSetProtos;
import org.opennms.netmgt.model.OnmsAttribute;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.OnmsResourceType;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.model.ResourceTypeUtils;
import org.opennms.netmgt.model.RrdGraphAttribute;
import org.opennms.netmgt.model.StringPropertyAttribute;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;

public class ResourceTreeMapperTest {

    /** Minimal resource type: only the name matters to the mapper. */
    public static final class StubResourceType implements OnmsResourceType {
        private final String name;

        public StubResourceType(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getLabel() {
            return name;
        }

        @Override
        public boolean isResourceTypeOnParent(OnmsResource parent) {
            return false;
        }

        @Override
        public List<OnmsResource> getResourcesForParent(OnmsResource parent) {
            return Collections.emptyList();
        }

        @Override
        public OnmsResource getChildByName(OnmsResource parent, String name) {
            return null;
        }

        @Override
        public String getLinkForResource(OnmsResource resource) {
            return null;
        }
    }

    public static OnmsResource resource(OnmsResource parent, String type, String name, OnmsAttribute... attributes) {
        final Set<OnmsAttribute> attrs = new HashSet<>();
        Collections.addAll(attrs, attributes);
        final OnmsResource resource = new OnmsResource(name, name, new StubResourceType(type), attrs, ResourcePath.get("snmp", "1", name));
        resource.setParent(parent);
        return resource;
    }

    public static OnmsNode node(int id, String location) {
        final OnmsNode node = new OnmsNode();
        node.setId(id);
        node.setLabel("node-" + id);
        node.setForeignSource("fs");
        node.setForeignId("fid-" + id);
        node.setLocation(new OnmsMonitoringLocation(location, location));
        return node;
    }

    private final ResourceTreeMapper mapper = new ResourceTreeMapper();

    private OnmsNode node;
    private OnmsResource nodeTop;

    @Before
    public void setUp() {
        node = node(5, "Raleigh");
        // The DAO names the top-level node resource "fs:fid" whenever both are set, regardless of storeByForeignSource
        nodeTop = resource(null, "node", "fs:fid-5");
        nodeTop.setEntity(node);
    }

    @After
    public void tearDown() {
        System.clearProperty("org.opennms.rrd.storeByForeignSource");
    }

    @Test
    public void mapsNodeLevelResource() {
        final OnmsResource resource = resource(nodeTop, "nodeSnmp", "",
                new RrdGraphAttribute("loadavg5", "snmp/5", "loadavg5.rrd"),
                new RrdGraphAttribute("loadavg1", "snmp/5", "loadavg1.rrd"),
                new StringPropertyAttribute("sysName", "host"));

        final CollectionSetProtos.CollectionSetResource proto = mapper.toProto(resource);

        assertThat(proto.hasNode(), is(true));
        assertThat(proto.getNode().getNodeId(), equalTo(5L));
        assertThat(proto.getNode().getNodeLabel(), equalTo("node-5"));
        assertThat(proto.getNode().getForeignSource(), equalTo("fs"));
        assertThat(proto.getNode().getForeignId(), equalTo("fid-5"));
        assertThat(proto.getNode().getLocation(), equalTo("Raleigh"));
        assertThat(proto.getResourceId(), equalTo("node[5].nodeSnmp[]"));
        assertThat(proto.getResourceTypeName(), equalTo("nodeSnmp"));
        assertThat(proto.getResourceName(), equalTo(""));
        assertThat(proto.getNumericList().stream().map(CollectionSetProtos.NumericAttribute::getName).collect(Collectors.toList()),
                contains("loadavg1", "loadavg5"));
        // the tree holds no values, so none are made up
        assertThat(proto.getNumeric(0).getValue(), equalTo(0.0));
        assertThat(proto.getNumeric(0).hasMetricValue(), is(false));
        assertThat(proto.getStringCount(), equalTo(1));
        assertThat(proto.getString(0).getName(), equalTo("sysName"));
        assertThat(proto.getString(0).getValue(), equalTo("host"));
    }

    @Test
    public void mapsInterfaceLevelResourceWithIfIndexFromEntity() {
        final OnmsResource resource = resource(nodeTop, "interfaceSnmp", "eth0-aabbcc");
        resource.setEntity(new OnmsSnmpInterface(node, 3));

        final CollectionSetProtos.CollectionSetResource proto = mapper.toProto(resource, node);

        assertThat(proto.hasInterface(), is(true));
        assertThat(proto.getInterface().getNode().getNodeId(), equalTo(5L));
        assertThat(proto.getInterface().getInstance(), equalTo("eth0-aabbcc"));
        assertThat(proto.getInterface().getIfIndex(), equalTo(3));
        assertThat(proto.getResourceId(), equalTo("node[5].interfaceSnmp[eth0-aabbcc]"));
        assertThat(proto.getResourceName(), equalTo("eth0-aabbcc"));
        assertThat(proto.getResourceTypeName(), equalTo("interfaceSnmp"));

        final MetricFilter.ResourceFilterContext ctx = new MetricFilter.ResourceFilterContext(proto);
        assertThat(ctx.getIfIndex(), equalTo(3));
        assertThat(ctx.getInstance(), equalTo("eth0-aabbcc"));
    }

    @Test
    public void mapsDirectoriesWithoutSnmpInterfaceAsNodeLevel() {
        // Non-SNMP collectors store node-level data in a sub-directory the tree lists as interfaceSnmp;
        // the producer forwards that data as a node-level resource
        final OnmsResource jvm = resource(nodeTop, "interfaceSnmp", "opennms-jvm",
                new RrdGraphAttribute("HeapUsed", "snmp/5/opennms-jvm", "HeapUsed.rrd"));
        assertThat(mapper.isNodeLevelDirectory(jvm), is(true));
        assertThat(mapper.skipReason(jvm, node), nullValue());
        final CollectionSetProtos.CollectionSetResource proto = mapper.toProto(jvm, node);
        assertThat(proto.hasNode(), is(true));
        assertThat(proto.getNode().getNodeId(), equalTo(5L));
        assertThat(proto.getResourceId(), equalTo("node[5].nodeSnmp[]"));
        assertThat(proto.getResourceTypeName(), equalTo("nodeSnmp"));
        assertThat(proto.getNumeric(0).getName(), equalTo("HeapUsed"));

        final OnmsResource noIfIndex = resource(nodeTop, "interfaceSnmp", "eth2-ddeeff");
        noIfIndex.setEntity(new OnmsSnmpInterface(node, (Integer) null));
        assertThat(mapper.isNodeLevelDirectory(noIfIndex), is(true));
        assertThat(mapper.toProto(noIfIndex, node).hasNode(), is(true));

        final OnmsResource eth0 = resource(nodeTop, "interfaceSnmp", "eth0-aabbcc");
        eth0.setEntity(new OnmsSnmpInterface(node, 3));
        assertThat(mapper.isNodeLevelDirectory(eth0), is(false));
        assertThat(mapper.isNodeLevelDirectory(resource(nodeTop, "nodeSnmp", "")), is(false));
    }

    @Test
    public void skipsPerspectiveResponseTimeResources() {
        assertThat(mapper.toProto(resource(nodeTop, "perspectiveResponseTime", "Minion"), node), nullValue());
        assertThat(mapper.skipReason(resource(nodeTop, "perspectiveResponseTime", "Minion"), node), containsString("never forwarded"));
    }

    @Test
    public void skipsServiceStatusResources() {
        // Pollerd persists status with an empty parent path, which the CollectionSetMapper drops
        assertThat(mapper.toProto(resource(nodeTop, "status", "10.0.0.1"), node), nullValue());
        assertThat(mapper.skipReason(resource(nodeTop, "status", "10.0.0.1"), node), containsString("never forwarded"));
        assertThat(mapper.skipReason(resource(nodeTop, "responseTime", "10.0.0.1"), node), nullValue());
        assertThat(mapper.skipReason(resource(nodeTop, "nodeSnmp", ""), null), containsString("not associated with a node"));
    }

    @Test
    public void mapsGenericResource() {
        final CollectionSetProtos.CollectionSetResource proto = mapper.toProto(resource(nodeTop, "hrStorageIndex", "7"));

        assertThat(proto.hasGeneric(), is(true));
        assertThat(proto.getGeneric().getType(), equalTo("hrStorageIndex"));
        assertThat(proto.getGeneric().getInstance(), equalTo("7"));
        assertThat(proto.getGeneric().getNode().getNodeId(), equalTo(5L));
        assertThat(proto.getResourceId(), equalTo("node[5].hrStorageIndex[7]"));
        assertThat(proto.getResourceName(), equalTo("7"));
        assertThat(proto.getResourceTypeName(), equalTo("hrStorageIndex"));
        assertThat(new MetricFilter.ResourceFilterContext(proto).getResourceTypeName(), equalTo("hrStorageIndex"));
    }

    @Test
    public void mapsResponseTimeResourceLikeTheCollectionSetMapper() {
        final OnmsResource resource = resource(nodeTop, "responseTime", "10.0.0.1");

        final CollectionSetProtos.CollectionSetResource proto = mapper.toProto(resource, node);
        assertThat(proto.hasResponse(), is(true));
        assertThat(proto.getResponse().getInstance(), equalTo("10.0.0.1"));
        assertThat(proto.getResponse().getLocation(), equalTo("Raleigh"));
        // Node information comes from the node_id tag at collection time
        assertThat(proto.getResponse().hasNode(), is(true));
        assertThat(proto.getResponse().getNode().getNodeId(), equalTo(5L));
        assertThat(proto.getResponse().getNode().getLocation(), equalTo("Raleigh"));
        // but the resource id fields are never populated for response time resources
        assertThat(proto.getResourceId(), equalTo(""));
        assertThat(new MetricFilter.ResourceFilterContext(proto).getResourceTypeName(), equalTo("responseTime"));
        assertThat(new MetricFilter.ResourceFilterContext(proto).getNodeId(), equalTo(5L));
        assertThat(new MetricFilter.ResourceFilterContext(proto).getLocation(), equalTo("Raleigh"));

        // The default location is not written, and the context then reports no location at all
        final CollectionSetProtos.CollectionSetResource defaultLocation = mapper.toProto(resource, node(6, "Default"));
        assertThat(defaultLocation.getResponse().getLocation(), equalTo(""));
        assertThat(defaultLocation.getResponse().getNode().getNodeId(), equalTo(6L));
        assertThat(new MetricFilter.ResourceFilterContext(defaultLocation).getLocation(), nullValue());

        // Other locations are sanitized like the storage path is
        final CollectionSetProtos.CollectionSetResource spaced = mapper.toProto(resource, node(7, "New York"));
        assertThat(spaced.getResponse().getLocation(), equalTo(ResourcePath.sanitize("New York")));
    }

    @Test
    public void usesStoreByForeignSourceResourceIdsOnlyWhenEnabled() {
        final OnmsResource iface = resource(nodeTop, "interfaceSnmp", "eth0-aabbcc");
        iface.setEntity(new OnmsSnmpInterface(node, 3));

        // The tree id is node[fs:fid-5].interfaceSnmp[...], which the producer never emits
        assertThat(iface.getId().toString(), equalTo("node[fs:fid-5].interfaceSnmp[eth0-aabbcc]"));
        assertThat(mapper.toProto(iface).getResourceId(), equalTo("node[5].interfaceSnmp[eth0-aabbcc]"));

        System.setProperty("org.opennms.rrd.storeByForeignSource", "true");
        assertThat(ResourceTypeUtils.isStoreByForeignSource(), is(true));
        assertThat(mapper.toProto(iface).getResourceId(), equalTo("nodeSource[fs:fid-5].interfaceSnmp[eth0-aabbcc]"));
        assertThat(mapper.toProto(resource(nodeTop, "nodeSnmp", "")).getResourceId(), equalTo("nodeSource[fs:fid-5].nodeSnmp[]"));

        // Nodes without foreign source fall back to the node id even when storing by foreign source
        final OnmsNode unprovisioned = node(8, "Default");
        unprovisioned.setForeignSource(null);
        unprovisioned.setForeignId(null);
        assertThat(mapper.toProto(resource(nodeTop, "hrStorageIndex", "1"), unprovisioned).getResourceId(), equalTo("node[8].hrStorageIndex[1]"));
    }

    @Test
    public void resourcesWithoutNodeAreNotMapped() {
        final OnmsResource domain = resource(null, "domain", "example");
        assertThat(mapper.toProto(resource(domain, "interfaceSnmp", "eth0")), nullValue());
        assertThat(mapper.toProto(resource(domain, "responseTime", "10.0.0.1")), nullValue());
        assertThat(ResourceTreeMapper.findNode(resource(domain, "interfaceSnmp", "eth0")), nullValue());
        assertThat(ResourceTreeMapper.findNode(resource(nodeTop, "nodeSnmp", "")), equalTo(node));
    }
}
