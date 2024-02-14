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

import org.opennms.core.sysprops.SystemProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

public class LateAggregationParams {

    public static final long MIN_STEP_MS = SystemProperties.getLong("org.opennms.timeseries.query.minimum_step", 5L * 60L * 1000L);
    public static final int INTERVAL_DIVIDER = SystemProperties.getInteger("org.opennms.timeseries.query.interval_divider", 2);
    public static final long DEFAULT_HEARTBEAT_MS = SystemProperties.getLong("org.opennms.timeseries.query.heartbeat", 450L * 1000L);
    // https://docs.opennms.com/horizon/31/deployment/time-series-storage/timeseries/configuration.html#additional-configuration-options default is 1.5
    public static final BigDecimal DEFAULT_HEARTBEAT_MULTIPLIER = SystemProperties.getBigDecimal("org.opennms.timeseries.query.heartbeat.multiplier", new BigDecimal("1.5"));

    private static final Logger LOG = LoggerFactory.getLogger(LateAggregationParams.class);

    final long step;
    final long interval;
    final long heartbeat;

    private LateAggregationParams(long step, long interval, long heartbeat) {
        this.step = step;
        this.interval = interval;
        this.heartbeat = heartbeat;
    }

    public long getStep() {
        return step;
    }

    public long getInterval() {
        return interval;
    }

    public long getHeartbeat() {
        return heartbeat;
    }

    public static LateAggregationParamsBuilder builder() {
        return new LateAggregationParamsBuilder();
    }

    public static class LateAggregationParamsBuilder {

        private Long step;
        private Long interval;
        private Long heartbeat;

        public LateAggregationParamsBuilder step(Long step) {
            this.step = step;
            return this;
        }

        public LateAggregationParamsBuilder interval(Long interval) {
            this.interval = interval;
            return this;
        }

        public LateAggregationParamsBuilder heartbeat(Long heartbeat) {
            this.heartbeat = heartbeat;
            return this;
        }

        /**
         * Calculates the parameters to use for late aggregation.
         * <p>
         * Since we're in the process of transitioning from an RRD-world, most queries won't
         * contain a specified interval or heartbeat. For this reason, we need to derive sensible
         * values that will allow users to visualize the data on the graphs without too many NaNs.
         * <p>
         * The given step size will be variable based on the time range and the pixel width of the
         * graph, so we need to derive the interval and heartbeat accordingly.
         * <p>
         * Let S = step, I = interval and H = heartbeat, the constraints are as follows:
         * 0 < S
         * 0 < I
         * 0 < H
         * S = aI      for some integer a >= 2
         * H = bI      for some integer b >= 2
         * <p>
         * While achieving these constraints, we also want to optimize for:
         * min(|S - S*|)
         * where S* is the user supplied step and S is the effective step.
         */
        public LateAggregationParams build() {

            // Limit the step with a lower bound in order to prevent extremely large queries
            long effectiveStep = Math.max(MIN_STEP_MS, step);
            if (effectiveStep != step) {
                LOG.warn("Requested step size {} is too small. Using {}.", step, effectiveStep);
            }

            // If the interval is specified, and already a multiple of the step then use it as is
            long effectiveInterval = 0;
            if (interval != null && interval < effectiveStep && (effectiveStep % interval) == 0) {
                effectiveInterval = interval;
            } else {
                // Otherwise, make sure the step is evenly divisible by the INTERVAL_DIVIDER
                if (effectiveStep % INTERVAL_DIVIDER != 0) {
                    effectiveStep += effectiveStep % INTERVAL_DIVIDER;
                }
                effectiveInterval = effectiveStep / INTERVAL_DIVIDER;
            }

            // Use the given heartbeat if specified, fall back to the default
            long effectiveHeartbeat = heartbeat != null ? heartbeat : DEFAULT_HEARTBEAT_MS;
            // make sure heartbeat will never smaller than step
            if (effectiveHeartbeat < effectiveStep) {
                effectiveHeartbeat = DEFAULT_HEARTBEAT_MULTIPLIER.multiply(new BigDecimal(effectiveStep)).longValue();
            }
            if (effectiveInterval < effectiveHeartbeat) {
                if (effectiveHeartbeat % effectiveInterval != 0) {
                    effectiveHeartbeat += effectiveInterval - (effectiveHeartbeat % effectiveInterval);
                } else {
                    // Existing heartbeat is valid
                }
            } else {
                effectiveHeartbeat = effectiveInterval + 1;
                effectiveHeartbeat += effectiveHeartbeat % effectiveInterval;
            }

            return new LateAggregationParams(effectiveStep, effectiveInterval, effectiveHeartbeat);
        }
    }
}
