/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
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
