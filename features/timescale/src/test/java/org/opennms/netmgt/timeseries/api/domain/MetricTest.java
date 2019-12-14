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

package org.opennms.netmgt.timeseries.api.domain;

import static org.junit.Assert.*;

import org.junit.Test;

public class MetricTest {

    @Test
    public void shouldValidate() {
        // needs mandatory tags
        assertThrows(IllegalArgumentException.class, () -> Metric.builder().build());
        assertThrows(IllegalArgumentException.class, () -> Metric.builder().tag(Metric.MandatoryTag.unit.name(), "ms").build());
        assertThrows(IllegalArgumentException.class, () -> Metric.builder().tag(Metric.MandatoryTag.mtype.name(), Metric.Mtype.counter.name()).build());
        Metric.builder().tag(Metric.MandatoryTag.unit.name(), "ms").tag(Metric.MandatoryTag.mtype.name(), Metric.Mtype.counter.name()).build();
        assertThrows(IllegalArgumentException.class, () -> Metric.builder().tag(Metric.MandatoryTag.mtype.name(), "unknown").build());
    }

    // TODO: Patrick: I think we have that functionality somewhere already: consolidate
    public static <T extends Throwable> void assertThrows(Class<T> expectedType, Runnable runnable) {
        try {
            runnable.run();
        } catch (Throwable t) {
            if (expectedType.isInstance(t)) {
                return; // thrown Exception is of expected type
            }
            fail("Expected Exception " + expectedType.getName());
        }
    }
}
