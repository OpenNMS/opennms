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
package org.opennms.netmgt.graph.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import org.hamcrest.Matchers;
import org.junit.Test;

public class NodeRefTest {

    @Test
    public void verifyEquals() {
        assertEquals(NodeRef.from("test:test"), NodeRef.from("test:test"));
        assertEquals(NodeRef.from(5), NodeRef.from(5));
        assertEquals(NodeRef.from("test", "test"), NodeRef.from("test", "test"));
        assertEquals(NodeRef.from(5, "test", "test"), NodeRef.from(5, "test", "test"));
        assertNotEquals(NodeRef.from("test:test"), NodeRef.from(5));
    }

    @Test
    public void verifyCreate() {
        final NodeRef ref1 = NodeRef.from("test:node1");
        assertThat(ref1.getForeignSource(), Matchers.is("test"));
        assertThat(ref1.getForeignId(), Matchers.is("node1"));
        assertNull(ref1.getNodeId());

        final NodeRef ref2 = NodeRef.from("5");
        assertNull(ref2.getForeignSource());
        assertNull(ref2.getForeignId());
        assertThat(ref2.getNodeId(), Matchers.is(5));
    }

    @Test
    public void verifyVariants() {
        final NodeRef ref1 = NodeRef.from("test:test");
        final NodeRef ref2 = NodeRef.from(5);
        final NodeRef ref3 = NodeRef.from(5, "test", "test");

        assertThat(ref1.getVariants(), Matchers.hasItem(ref1));
        assertThat(ref2.getVariants(), Matchers.hasItem(ref2));
        assertThat(ref3.getVariants(), Matchers.hasItems(ref1, ref2));
    }

}