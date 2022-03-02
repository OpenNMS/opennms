/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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