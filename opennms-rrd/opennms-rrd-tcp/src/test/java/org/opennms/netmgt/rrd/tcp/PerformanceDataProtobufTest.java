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
