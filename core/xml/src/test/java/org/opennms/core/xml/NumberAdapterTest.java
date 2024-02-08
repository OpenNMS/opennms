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
package org.opennms.core.xml;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class NumberAdapterTest {

    @Test
    public void testNumberAdapter() throws Exception {
        NumberAdapter adapter = new NumberAdapter();
        assertEquals("4.65450971582E11", adapter.marshal(4.65450971582E11));
        assertEquals(4.65450971582E11, adapter.unmarshal("4.65450971582E11"));
        assertEquals("45.0", adapter.marshal(45));
        assertEquals(45.0, adapter.unmarshal("45"));
        assertEquals("2.147483647E9", adapter.marshal(Integer.MAX_VALUE));
        assertEquals("NaN", adapter.marshal(Double.NaN));
        assertEquals(Double.NaN, adapter.unmarshal("NaN"));
        assertEquals("Infinity", adapter.marshal(Double.POSITIVE_INFINITY));
        assertEquals(Double.POSITIVE_INFINITY, adapter.unmarshal("Infinity"));
        assertEquals("-Infinity", adapter.marshal(Double.NEGATIVE_INFINITY));
        assertEquals(Double.NEGATIVE_INFINITY, adapter.unmarshal("-Infinity"));
        assertEquals(Double.MAX_VALUE, adapter.unmarshal("1.7976931348623157E308D"));
        assertEquals("1.7976931348623157E308", adapter.marshal(Double.MAX_VALUE));
        assertEquals("9.223372036854776E18", adapter.marshal(Long.MAX_VALUE));
        assertEquals("-9.223372036854776E18", adapter.marshal(Long.MIN_VALUE));

    }
}
