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
package org.opennms.enlinkd.generator.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class MacAddressGeneratorTest {

    @Test
    public void shouldProduceStreamOfMacAddresses() {
        MacAddressGenerator gen = new MacAddressGenerator();
        assertEquals("000000000001", gen.next());
        assertEquals("000000000002", gen.next());
        assertEquals("000000000003", gen.next());
        assertEquals("000000000004", gen.next());
        assertEquals("000000000005", gen.next());
        assertEquals("000000000006", gen.next());
        assertEquals("000000000007", gen.next());
        assertEquals("000000000008", gen.next());
        assertEquals("000000000009", gen.next());
        assertEquals("00000000000a", gen.next());
        assertEquals("00000000000b", gen.next());
        assertEquals("00000000000c", gen.next());
        assertEquals("00000000000d", gen.next());
        assertEquals("00000000000e", gen.next());
        assertEquals("00000000000f", gen.next());

        assertEquals("000000000010", gen.next());
        assertEquals("000000000011", gen.next());
        assertEquals("000000000012", gen.next());
        assertEquals("000000000013", gen.next());
        assertEquals("000000000014", gen.next());
        assertEquals("000000000015", gen.next());
        assertEquals("000000000016", gen.next());
        assertEquals("000000000017", gen.next());
        assertEquals("000000000018", gen.next());
        assertEquals("000000000019", gen.next());
        assertEquals("00000000001a", gen.next());
        assertEquals("00000000001b", gen.next());
        assertEquals("00000000001c", gen.next());
        assertEquals("00000000001d", gen.next());
        assertEquals("00000000001e", gen.next());
        assertEquals("00000000001f", gen.next());

    }
}
