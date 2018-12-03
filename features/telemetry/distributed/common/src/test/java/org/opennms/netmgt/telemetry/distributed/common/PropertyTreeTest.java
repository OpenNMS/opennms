/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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
                .put("x.y.z.a.b.c", "0")
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
        assertEquals(Optional.of("0"), props.find("x", "y", "z", "a", "b", "c").flatMap(PropertyTree.Node::getValue));

        assertEquals("1", props.getRequiredString("a"));
        assertEquals("7", props.getRequiredString("b", "c", "b"));

        assertEquals(Optional.of(4), props.getOptionalInteger("b", "b", "a"));
        assertEquals(Optional.empty(), props.getOptionalInteger("b", "b", "x"));
        assertEquals(Optional.empty(), props.getOptionalInteger("b", "b", "y", "z"));

        assertEquals(ImmutableMap.of("a", "6", "b", "7"), props.getMap("b", "c"));
        assertEquals(Collections.emptyMap(), props.getMap("b", "x"));
        assertEquals(Collections.emptyMap(), props.getMap("b", "y", "z"));

        assertEquals(3, props.getSubTrees("b").size());
    }

}
