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
package org.opennms.features.topology.api.support;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.features.topology.api.support.hops.DefaultVertexHopCriteria;
import org.opennms.features.topology.api.topo.AbstractVertex;
import org.opennms.features.topology.api.topo.BackendGraph;
import org.opennms.features.topology.api.topo.simple.SimpleGraphBuilder;

import com.google.common.collect.Lists;

public class FocusStrategyTest {

    @Test
    public void testFocusStrategies() {
        final BackendGraph graph = new SimpleGraphBuilder("namespace1")
                .vertex("1")
                .vertex("2")
                .vertex("3")
                .get();
        Assert.assertEquals(Lists.newArrayList(), FocusStrategy.EMPTY.getFocusCriteria(graph));
        Assert.assertEquals(Lists.newArrayList(
                hopCriteria("namespace1", "1"),
                hopCriteria("namespace1", "2"),
                hopCriteria("namespace1", "3")), FocusStrategy.ALL.getFocusCriteria(graph));
        Assert.assertEquals(Lists.newArrayList(
                hopCriteria("namespace1", "1")), FocusStrategy.FIRST.getFocusCriteria(graph));
        Assert.assertEquals(Lists.newArrayList(
                hopCriteria("namespace1", "2")), FocusStrategy.SPECIFIC.getFocusCriteria(graph, "2"));
    }

    private DefaultVertexHopCriteria hopCriteria(String namespace, String id) {
        return new DefaultVertexHopCriteria(new AbstractVertex(namespace, id));
    }

}