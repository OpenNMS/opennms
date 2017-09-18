/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.model;

import org.junit.Test;

import junit.framework.TestCase;

public class OnmsResourceIdTest extends TestCase {

    @Test
    public void testToString() {
        final ResourceId id1 = ResourceId.get("node", "imported[ipam%europe+asia]:foo.bar\\x");
        assertEquals("node[imported\\[ipam%europe+asia\\]:foo.bar\\\\x]",
                     id1.toString());

        final ResourceId id2 = ResourceId.get(id1, "http", "http://[fe80::%lo]/test.me");
        assertEquals("node[imported\\[ipam%europe+asia\\]:foo.bar\\\\x].http[http://\\[fe80::%lo\\]/test.me]",
                     id2.toString());
    }

    @Test
    public void testNull() {
        assertNull(ResourceId.fromString(null));
    }

    @Test
    public void testFromString() {
        final ResourceId id1 = ResourceId.fromString("node[imported\\[ipam%europe+asia\\]:foo.bar\\\\x]");
        assertNull(id1.parent);
        assertEquals("node", id1.type);
        assertEquals("imported[ipam%europe+asia]:foo.bar\\x", id1.name);

        final ResourceId id2 = ResourceId.fromString("node[imported\\[ipam%europe+asia\\]:foo.bar\\\\x].http[http://\\[fe80::%lo\\]/test.me]");
        assertNotNull(id2.parent);
        assertEquals("http", id2.type);
        assertEquals("http://[fe80::%lo]/test.me", id2.name);
        assertNull(id2.parent.parent);
        assertEquals("node", id2.parent.type);
        assertEquals("imported[ipam%europe+asia]:foo.bar\\x", id2.parent.name);
    }

}
