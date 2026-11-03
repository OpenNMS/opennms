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
package org.opennms.netmgt.graph.provider.legacy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;
import org.opennms.features.topology.api.topo.AbstractEdge;
import org.opennms.features.topology.api.topo.DefaultVertexRef;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.LinkDetailsAware;
import org.opennms.netmgt.graph.api.generic.GenericEdge;

/** What survives the legacy-to-generic edge conversion. */
public class LegacyEdgeTest {

    private static final String NAMESPACE = "nodes";

    private static class PlainEdge extends AbstractEdge implements Edge {
        PlainEdge() {
            super(NAMESPACE, "edge-1", vertex("source"), vertex("target"));
        }
    }

    private static class LinkEdge extends PlainEdge implements LinkDetailsAware {
        private final Integer sourceIfIndex;
        private final Integer targetIfIndex;

        LinkEdge(final Integer sourceIfIndex, final Integer targetIfIndex) {
            this.sourceIfIndex = sourceIfIndex;
            this.targetIfIndex = targetIfIndex;
        }

        @Override
        public Integer getSourceIfIndex() {
            return sourceIfIndex;
        }

        @Override
        public Integer getTargetIfIndex() {
            return targetIfIndex;
        }

        @Override
        public String getDiscoveryProtocol() {
            return "LLDP";
        }
    }

    private static DefaultVertexRef vertex(final String id) {
        return new DefaultVertexRef(NAMESPACE, id, id);
    }

    private static GenericEdge convert(final Edge edge) {
        return new LegacyEdge(edge).asGenericEdge();
    }

    /** Pins the wire type: strings, not numbers. */
    @Test
    public void carriesInterfaceIdentityAndProtocolAsStrings() {
        final GenericEdge converted = convert(new LinkEdge(2, 47));

        assertEquals("2", converted.getProperty(LegacyEdge.Properties.SOURCE_IFINDEX));
        assertEquals("47", converted.getProperty(LegacyEdge.Properties.TARGET_IFINDEX));
        assertEquals("LLDP", converted.getProperty(LegacyEdge.Properties.DISCOVERY_PROTOCOL));
    }

    /** Each ifIndex belongs to the endpoint its ref names. */
    @Test
    public void pairsEachIfIndexWithItsOwnEndpoint() {
        final GenericEdge converted = convert(new LinkEdge(2, 47));

        assertEquals("source", converted.getSource().getId());
        assertEquals("2", converted.getProperty(LegacyEdge.Properties.SOURCE_IFINDEX));
        assertEquals("target", converted.getTarget().getId());
        assertEquals("47", converted.getProperty(LegacyEdge.Properties.TARGET_IFINDEX));
    }

    @Test
    public void keepsTheEndpointsItAlwaysKept() {
        final GenericEdge converted = convert(new LinkEdge(2, 47));

        assertEquals("edge-1", converted.getId());
        assertEquals(NAMESPACE, converted.getNamespace());
    }

    /** One-sided discovery: the absent end is missing, not empty. */
    @Test
    public void omitsAnEndThatNeverResolved() {
        final GenericEdge converted = convert(new LinkEdge(2, null));

        assertEquals("2", converted.getProperty(LegacyEdge.Properties.SOURCE_IFINDEX));
        assertFalse(converted.getProperties().containsKey(LegacyEdge.Properties.TARGET_IFINDEX));
    }

    @Test
    public void addsNothingToAnEdgeThatKnowsNoLink() {
        final GenericEdge converted = convert(new PlainEdge());

        assertFalse(converted.getProperties().containsKey(LegacyEdge.Properties.SOURCE_IFINDEX));
        assertFalse(converted.getProperties().containsKey(LegacyEdge.Properties.TARGET_IFINDEX));
        assertFalse(converted.getProperties().containsKey(LegacyEdge.Properties.DISCOVERY_PROTOCOL));
    }
}
