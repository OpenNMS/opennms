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

package org.opennms.netmgt.rrd.tcp;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;

import org.junit.Test;

import com.google.common.io.Resources;

public class PerformanceDataProtobufTest {

    /**
     * Validate that we can decode a known payload.
     *
     * Used to help ensure we don't break binary compatibility unknowingly.
     */
    @Test
    public void canDecodePayload() throws IOException {
        PerformanceDataProtos.PerformanceDataReadings.Builder builder = PerformanceDataProtos.PerformanceDataReadings.newBuilder();
        builder.addMessage(PerformanceDataProtos.PerformanceDataReading.newBuilder()
                .setPath("path")
                .setOwner("me")
                .setTimestamp(1)
                .addAllDblValue(Arrays.asList(1d,2d,3d))
                .addAllStrValue(Arrays.asList("a", "b", "c"))
        );
        PerformanceDataProtos.PerformanceDataReadings expectedReadings = builder.build();

        PerformanceDataProtos.PerformanceDataReadings actualReadings;
        URL url = Resources.getResource("rrd-perf.dat");
        try (InputStream is = Resources.asByteSource(url).openStream()) {
            actualReadings = PerformanceDataProtos.PerformanceDataReadings.parseFrom(is);
        }

        assertEquals(expectedReadings, actualReadings);
    }
}
