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
package org.opennms.features.topology.plugins.topo.application;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.features.topology.api.topo.VertexRef;

public class ApplicationVertexTest {

    @Test
    public void testHierarchy() {
        LegacyApplicationVertex vertex = createVertex("100");
        vertex.addChildren(createVertex("service:1"));
        vertex.addChildren(createVertex("service:2"));

        Assert.assertEquals(true, vertex.isRoot());
        Assert.assertEquals(false, vertex.isLeaf());
        Assert.assertEquals(true, vertex.isPartOf("100"));
        Assert.assertEquals(false, vertex.isPartOf("101"));

        // Verify first level
        for (VertexRef eachRef : vertex.getChildren()) {
            Assert.assertEquals(LegacyApplicationTopologyProvider.TOPOLOGY_NAMESPACE, eachRef.getNamespace());
            LegacyApplicationVertex child = (LegacyApplicationVertex) eachRef;

            Assert.assertEquals(false, child.isRoot());
            Assert.assertEquals(true, child.isLeaf());
            Assert.assertEquals(vertex, child.getRoot());
            Assert.assertEquals(vertex, child.getParent());
            Assert.assertEquals(true, child.isPartOf("100"));
            Assert.assertEquals(false, child.isPartOf("101"));
        }
    }

    private static LegacyApplicationVertex createVertex(String id) {
        return new LegacyApplicationVertex(id, id);
    }
}
