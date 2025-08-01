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
package org.opennms.netmgt.timeseries.sampleread;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.math.BigDecimal;

public class LateAggregationParamsTest {

    @Test
    public void canCalculateLagParams() {

        // Supply sane values and make sure the same values are returned
        LateAggregationParams lag = LateAggregationParams.builder().step(300 * 1000L).interval(150 * 1000L).heartbeat(450 * 1000L).build();
        assertEquals(300 * 1000L, lag.step);
        assertEquals(150 * 1000L, lag.interval);
        assertTrue(lag.heartbeat >= lag.step);

        // Supply a step that is not a multiple of the interval, make sure this is corrected
        lag = LateAggregationParams.builder().step(310 * 1000L).interval(150 * 1000L).heartbeat(450 * 1000L).build();
        assertEquals(310000L, lag.step);
        assertEquals(155000L, lag.interval);
        assertTrue(lag.heartbeat >= lag.step);

        // Supply an interval that is much larger than the step
        lag = LateAggregationParams.builder().step(300 * 1000L).interval(1500 * 1000L).heartbeat(45000 * 1000L).build();
        assertEquals(300 * 1000L, lag.step);
        // Interval should be reduced
        assertEquals(150 * 1000L, lag.interval);
        assertTrue(lag.heartbeat >= lag.step);

        var stepSize = 300 * 1000L;
        lag = LateAggregationParams.builder().step(stepSize).build();
        assertEquals(stepSize, lag.step);
        // heartbeat should same as default when step is smaller than DEFAULT_HEARTBEAT_MS
        assertEquals(LateAggregationParams.DEFAULT_HEARTBEAT_MS, lag.heartbeat);
        assertEquals(stepSize / LateAggregationParams.INTERVAL_DIVIDER, lag.interval);
        assertTrue(lag.heartbeat >= lag.step);

        // make sure if step is bigger than DEFAULT_HEARTBEAT_MS
        stepSize = LateAggregationParams.DEFAULT_HEARTBEAT_MS * 10;
        lag = LateAggregationParams.builder().step(stepSize).build();
        assertEquals(stepSize, lag.step);
        assertEquals(LateAggregationParams.DEFAULT_HEARTBEAT_MULTIPLIER.multiply(new BigDecimal(stepSize)).longValue(), lag.heartbeat);
        assertEquals(stepSize / LateAggregationParams.INTERVAL_DIVIDER, lag.interval);
        assertTrue(lag.heartbeat >= lag.step);
    }


    @Test
    public void canLimitStepSize() {

        // Request a step size smaller than the lower bound
        LateAggregationParams lag = LateAggregationParams.builder().step(LateAggregationParams.MIN_STEP_MS - 1).build();
        assertEquals(LateAggregationParams.MIN_STEP_MS, lag.step);
    }
}
