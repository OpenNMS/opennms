/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2023 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2023 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.persist;

import static org.junit.Assert.assertEquals;

import org.joda.time.Duration;
import org.junit.Test;

public class StringIntervalAdapterTest {
    final StringIntervalAdapter stringIntervalAdapter = new StringIntervalAdapter();
    final static long ONE_DAY_MS = 1000 * 3600 * 24;
    final static long ONE_SECOND_MS = 1000;

    @Test
    public void marshalTest() {
        assertEquals("0", stringIntervalAdapter.marshal(Duration.ZERO));
        assertEquals("-1s", stringIntervalAdapter.marshal(Duration.ZERO.minus(ONE_SECOND_MS)));
        assertEquals("1d", stringIntervalAdapter.marshal(Duration.ZERO.plus(ONE_DAY_MS)));
    }

    @Test
    public void unmarshalTest() {
        assertEquals(Duration.ZERO, stringIntervalAdapter.unmarshal("0"));
        assertEquals(Duration.ZERO, stringIntervalAdapter.unmarshal(" 0 "));
        assertEquals(Duration.ZERO.minus(ONE_SECOND_MS), stringIntervalAdapter.unmarshal("-1s"));
        assertEquals(Duration.ZERO.minus(ONE_SECOND_MS), stringIntervalAdapter.unmarshal(" -1s "));
        assertEquals(Duration.ZERO.plus(ONE_DAY_MS), stringIntervalAdapter.unmarshal("1d"));
        assertEquals(Duration.ZERO.plus(ONE_DAY_MS), stringIntervalAdapter.unmarshal(" 1d "));
        // see NMS-15768
        assertEquals(Duration.ZERO.minus(ONE_SECOND_MS), stringIntervalAdapter.unmarshal("-1"));
        assertEquals(Duration.ZERO.minus(ONE_SECOND_MS), stringIntervalAdapter.unmarshal(" -1 "));
    }
}
