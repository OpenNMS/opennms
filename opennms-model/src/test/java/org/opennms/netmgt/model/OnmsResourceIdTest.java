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
