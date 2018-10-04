/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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
