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
package org.opennms.features.kafka.producer;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.opennms.features.kafka.producer.model.OpennmsModelProtos;

/**
 * Checks equality between two alarms based on a defined set of excluded fields.
 * <p>
 * Equality methods in this class modify their parameters as a side effect of comparison (by clearing certain fields).
 * This is done to avoid needlessly cloning parameters by default. If modification is undesirable the parameters must
 * be cloned before being passed to this class and discarded afterwards.
 */
public class AlarmEqualityChecker {
    /**
     * The function to use to apply exclusions.
     */
    private final Function<OpennmsModelProtos.Alarm.Builder, OpennmsModelProtos.Alarm.Builder> applyExclusions;

    /**
     * Private constructor.
     *
     * @param applyExclusions exclusion function
     */
    private AlarmEqualityChecker(Function<OpennmsModelProtos.Alarm.Builder,
            OpennmsModelProtos.Alarm.Builder> applyExclusions) {
        this.applyExclusions = Objects.requireNonNull(applyExclusions);
    }

    /**
     * Static factory method.
     *
     * @param applyExclusions exclusion function
     * @return the instance with the given exclusion function
     */
    public static AlarmEqualityChecker with(Function<OpennmsModelProtos.Alarm.Builder,
            OpennmsModelProtos.Alarm.Builder> applyExclusions) {
        return new AlarmEqualityChecker(applyExclusions);
    }

    /**
     * Checks two given alarms for equality excluding a defined set of fields during the equality check.
     *
     * @param a alarm a
     * @param b alarm b
     * @return true if equal, false otherwise
     */
    public boolean equalsExcludingOnBoth(OpennmsModelProtos.Alarm.Builder a, OpennmsModelProtos.Alarm.Builder b) {
        return applyExclusions.apply(Objects.requireNonNull(a)).build()
                .equals(applyExclusions.apply(Objects.requireNonNull(b)).build());
    }

    /**
     * Checks two given alarms for equality excluding a defined set of fields on alarm a during the equality check.
     *
     * @param a alarm a which will have exclusions applied
     * @param b alarm b
     * @return true if equal, false otherwise
     */
    public boolean equalsExcludingOnFirst(OpennmsModelProtos.Alarm.Builder a, OpennmsModelProtos.Alarm b) {
        return applyExclusions.apply(Objects.requireNonNull(a)).build()
                .equals(Objects.requireNonNull(b));
    }

    /**
     * Static class to namespace a predefined set of exclusions.
     */
    public static class Exclusions {
        /**
         * The default exclusions.
         *
         * @param alarmBuilder the alarm builder to apply exclusions to
         * @return the alarm builder with exclusions applied
         */
        public static OpennmsModelProtos.Alarm.Builder defaultExclusions(
                OpennmsModelProtos.Alarm.Builder alarmBuilder) {
            // Recursively apply these exclusions to any related alarms
            if (!alarmBuilder.getRelatedAlarmList().isEmpty()) {
                List<OpennmsModelProtos.Alarm> relatedAlarmsWithExclusions = alarmBuilder.getRelatedAlarmList().stream()
                        // Convert the related alarm into a builder
                        .map(OpennmsModelProtos.Alarm::newBuilder)
                        // Apply the exclusions
                        .map(Exclusions::defaultExclusions)
                        // Convert the alarm back
                        .map(OpennmsModelProtos.Alarm.Builder::build)
                        .collect(Collectors.toList());
                // Doesn't look like we can just replace the list at this point so we need to clear it and repopulate
                // it iteratively
                // Clear all the untouched related alarms
                alarmBuilder.clearRelatedAlarm();
                // Replace them with the excluded related alarms
                relatedAlarmsWithExclusions.forEach(alarmBuilder::addRelatedAlarm);
            }

            return alarmBuilder
                    .clearCount()
                    .clearLastEvent()
                    .clearLastEventTime();
        }
    }
}
