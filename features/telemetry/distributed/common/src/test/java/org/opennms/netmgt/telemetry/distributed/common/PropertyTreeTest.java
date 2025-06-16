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
package org.opennms.netmgt.telemetry.distributed.common;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.Optional;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;

public class PropertyTreeTest {

    @Test
    public void test() {
        final PropertyTree props = PropertyTree.from(ImmutableMap.<String, String>builder()
                .put("a", "1")
                .put("b.a", "2")
                .put("b.b", "3")
                .put("b.b.a", "4")
                .put("b.b.b", "5")
                .put("b.c.a", "6")
                .put("b.c.b", "7")
                .put("x.y.z.a.b", "0")
                .put("x.y.z.a.b.c", "1")
                .put("x.y.z.a.b.d", "2")
                .build());

        assertEquals(Optional.of("1"), props.find("a").flatMap(PropertyTree.Node::getValue));

        assertEquals(Optional.empty(), props.find("b").flatMap(PropertyTree.Node::getValue));
        assertEquals(Optional.of("2"), props.find("b", "a").flatMap(PropertyTree.Node::getValue));
        assertEquals(Optional.of("3"), props.find("b", "b").flatMap(PropertyTree.Node::getValue));
        assertEquals(Optional.of("4"), props.find("b", "b", "a").flatMap(PropertyTree.Node::getValue));
        assertEquals(Optional.empty(), props.find("b", "b", "a", "a").flatMap(PropertyTree.Node::getValue));
        assertEquals(Optional.of("5"), props.find("b", "b", "b").flatMap(PropertyTree.Node::getValue));
        assertEquals(Optional.empty(), props.find("b", "b", "c").flatMap(PropertyTree.Node::getValue));
        assertEquals(Optional.empty(), props.find("b", "c").flatMap(PropertyTree.Node::getValue));
        assertEquals(Optional.of("6"), props.find("b", "c", "a").flatMap(PropertyTree.Node::getValue));
        assertEquals(Optional.of("7"), props.find("b", "c", "b").flatMap(PropertyTree.Node::getValue));
        assertEquals(Optional.empty(), props.find("b", "c", "c").flatMap(PropertyTree.Node::getValue));
        assertEquals(Optional.of("1"), props.find("x", "y", "z", "a", "b", "c").flatMap(PropertyTree.Node::getValue));

        assertEquals("1", props.getRequiredString("a"));
        assertEquals("7", props.getRequiredString("b", "c", "b"));

        assertEquals(Optional.of(4), props.getOptionalInteger("b", "b", "a"));
        assertEquals(Optional.empty(), props.getOptionalInteger("b", "b", "x"));
        assertEquals(Optional.empty(), props.getOptionalInteger("b", "b", "y", "z"));

        assertEquals(ImmutableMap.of("a", "6", "b", "7"), props.getMap("b", "c"));
        assertEquals(ImmutableMap.of("a.b", "0", "a.b.c", "1", "a.b.d", "2"), props.getFlatMap("x", "y", "z"));
        assertEquals(Collections.emptyMap(), props.getMap("b", "x"));
        assertEquals(Collections.emptyMap(), props.getMap("b", "y", "z"));

        assertEquals(3, props.getSubTrees("b").size());
    }

    /**
     * see NMS-13477
     */
    @Test
    public void testWhitespaces() {
        final PropertyTree props = PropertyTree.from(ImmutableMap.<String, String>builder()
                .put("a", "1 ")
                .put("b", " 2")
                .put("x.y.z.a.b", "1 ")
                .put("x.y.z.a.c", " 2")
                .build());

        assertEquals(Optional.empty(), props.find("c").flatMap(PropertyTree.Node::getValue));
        assertEquals(Optional.of("1"), props.find("a").flatMap(PropertyTree.Node::getValue));
        assertEquals(Optional.of("2"), props.find("b").flatMap(PropertyTree.Node::getValue));

        assertEquals("1", props.getRequiredString("a"));
        assertEquals("2", props.getRequiredString("b"));

        assertEquals(Optional.of(1), props.getOptionalInteger("a"));
        assertEquals(Optional.of(2), props.getOptionalInteger("b"));

        assertEquals(ImmutableMap.of("a.b", "1", "a.c", "2"), props.getFlatMap("x", "y", "z"));
    }
}
