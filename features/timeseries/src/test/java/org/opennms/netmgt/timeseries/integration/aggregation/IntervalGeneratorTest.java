/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.timeseries.integration.aggregation;


import static org.junit.Assert.assertEquals;
import static org.opennms.newts.api.Timestamp.fromEpochSeconds;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.opennms.newts.aggregate.IntervalGenerator;
import org.opennms.newts.api.Duration;
import org.opennms.newts.api.Timestamp;

import com.google.common.collect.Lists;

/** Copied from Newts project. */
public class IntervalGeneratorTest {

    private static final Duration DEFAULT_INTERVAL = Duration.seconds(300);

    @Test
    public void test() {

        List<Timestamp> timestamps = Lists.newArrayList(getTimestamps(150, 3500));

        assertEquals(13, timestamps.size());
        assertEquals(new Timestamp(0, TimeUnit.SECONDS), timestamps.get(0));
        assertEquals(new Timestamp(3300, TimeUnit.SECONDS), timestamps.get(11));
        assertEquals(new Timestamp(3600, TimeUnit.SECONDS), timestamps.get(12));

        timestamps = Lists.newArrayList(getTimestamps(0, 3600));

        assertEquals(13, timestamps.size());
        assertEquals(new Timestamp(0, TimeUnit.SECONDS), timestamps.get(0));
        assertEquals(new Timestamp(3600, TimeUnit.SECONDS), timestamps.get(12));

    }

    @Test
    public void testReversed() {

        List<Timestamp> timestamps = Lists.newArrayList(getTimestamps(150, 3500, true));

        assertEquals(13, timestamps.size());
        assertEquals(new Timestamp(0, TimeUnit.SECONDS), timestamps.get(12));
        assertEquals(new Timestamp(3300, TimeUnit.SECONDS), timestamps.get(1));
        assertEquals(new Timestamp(3600, TimeUnit.SECONDS), timestamps.get(0));

        timestamps = Lists.newArrayList(getTimestamps(0, 3600, true));

        assertEquals(13, timestamps.size());
        assertEquals(new Timestamp(0, TimeUnit.SECONDS), timestamps.get(12));
        assertEquals(new Timestamp(3600, TimeUnit.SECONDS), timestamps.get(0));

    }

    private Iterable<Timestamp> getTimestamps(long startSecs, long endSecs) {
        return getTimestamps(startSecs, endSecs, DEFAULT_INTERVAL, false);
    }

    private Iterable<Timestamp> getTimestamps(long startSecs, long endSecs, boolean reversed) {
        return getTimestamps(startSecs, endSecs, DEFAULT_INTERVAL, reversed);
    }

    private Iterable<Timestamp> getTimestamps(long startSecs, long endSecs, Duration duration, boolean reversed) {
        return new IntervalGenerator(
                fromEpochSeconds(startSecs).stepFloor(duration),
                fromEpochSeconds(endSecs).stepCeiling(duration),
                duration,
                reversed);
    }

}
