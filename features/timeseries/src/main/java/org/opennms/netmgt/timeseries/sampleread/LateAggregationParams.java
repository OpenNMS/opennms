/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2020 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.timeseries.sampleread;

import org.opennms.core.sysprops.SystemProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LateAggregationParams {

    public static final long MIN_STEP_MS = SystemProperties.getLong("org.opennms.timeseries.query.minimum_step", 5L * 60L * 1000L);
    public static final int INTERVAL_DIVIDER = SystemProperties.getInteger("org.opennms.timeseries.query.interval_divider", 2);
    public static final long DEFAULT_HEARTBEAT_MS = SystemProperties.getLong("org.opennms.timeseries.query.heartbeat", 450L * 1000L);
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
