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

import java.util.Objects;
import java.util.stream.Collectors;

import org.opennms.features.kafka.producer.model.OpennmsModelProtos;
import org.opennms.netmgt.model.OnmsAlarm;

/**
 * Contains a {@link OpennmsModelProtos.Alarm} to encapsulate comparison logic to the {@link OnmsAlarm source type} that
 * the proto object was originally mapped from. This prevents re-mapping the object to the source type to compare
 * against another alarm of the source type.
 * <p>
 * Note that this class only references the contained alarm and does not copy it. Therefore mutations to the original
 * alarm will effect the results from this class.
 */
public class AlarmEqualityChecker {
    /**
     * The {@link OpennmsModelProtos.Alarm contained alarm}.
     */
    private final OpennmsModelProtos.Alarm protoAlarm;

    /**
     * The mapper to use to map fields that cannot be easily compared directly.
     */
    private final ProtobufMapper protobufMapper;

    private AlarmEqualityChecker(OpennmsModelProtos.Alarm protoAlarm, ProtobufMapper protobufMapper) {
        this.protoAlarm = protoAlarm;
        this.protobufMapper = protobufMapper;
    }

    /**
     * Static factory method that creates an instance containing the given alarm.
     *
     * @param protoAlarm     the alarm to contain
     * @param protobufMapper the mapper to use
     * @return a new instance containing the given alarm
     */
    public static AlarmEqualityChecker withProtoAlarm(OpennmsModelProtos.Alarm protoAlarm,
                                                      ProtobufMapper protobufMapper) {
        return new AlarmEqualityChecker(Objects.requireNonNull(protoAlarm), Objects.requireNonNull(protobufMapper));
    }

    /**
     * Compares the fields in {@link #protoAlarm the contained alarm} with the fields in
     * {@link OnmsAlarm the given alarm} for any incremental differences in logical equality.
     * <p>
     * Incremental differences are differences in one of the following attributes:
     * <list>
     * <li>count</li>
     * <li>lastEvent</li>
     * <li>lastEventTime</li>
     * </list>
     *
     * @param onmsAlarm the alarm to compare with
     * @return whether or not the given alarm has incremental differences
     */
    private boolean hasIncrementalDifferences(OnmsAlarm onmsAlarm) {
        return !equalsOrZero((int) protoAlarm.getCount(), onmsAlarm.getCounter()) ||
                !equalsOrZero(protoAlarm.getLastEventTime(), onmsAlarm.getLastEventTime() == null ? null :
                        onmsAlarm.getLastEventTime().getTime()) ||
                ((protoAlarm.getLastEvent() == null || onmsAlarm.getLastEvent() == null ||
                        !equalsOrZero((int) protoAlarm.getLastEvent().getId(), onmsAlarm.getLastEvent().getId())) &&
                        (protoAlarm.getLastEvent() != OpennmsModelProtos.Event.getDefaultInstance() ||
                                onmsAlarm.getLastEvent() != null));
    }

    /**
     * Compares the fields in {@link #protoAlarm the contained alarm} except for those checked by
     * {@link #hasIncrementalDifferences} with the fields in {@link OnmsAlarm the given alarm} for any differences in
     * logical equality.
     * <p>
     * All fields not compared by {@link #hasIncrementalDifferences} must be compared here.
     * <p>
     * Note that the comparison between related alarms is done on the Ids only.
     *
     * @param onmsAlarm the alarm to compare with
     * @return whether or not the given alarm has non-incremental differences
     */
    public boolean hasNonIncrementalDifferences(OnmsAlarm onmsAlarm) {
        // TODO: order for efficient short-circuit
        return !(equalsOrZero((int) protoAlarm.getId(), onmsAlarm.getId()) &&
                equalsOrEmpty(protoAlarm.getUei(), onmsAlarm.getUei()) &&
                Objects.equals(protoAlarm.getNodeCriteria(), onmsAlarm.getNode() == null ?
                        OpennmsModelProtos.NodeCriteria.getDefaultInstance() :
                        protobufMapper.toNodeCriteria(onmsAlarm.getNode()).build()) &&
                equalsOrEmpty(protoAlarm.getIpAddress(), onmsAlarm.getIpAddr() == null ? null :
                        onmsAlarm.getIpAddr().toString()) &&
                equalsOrEmpty(protoAlarm.getServiceName(), onmsAlarm.getServiceType() == null ? null :
                        onmsAlarm.getServiceType().getName()) &&
                equalsOrEmpty(protoAlarm.getReductionKey(), onmsAlarm.getReductionKey()) &&
                Objects.equals(protoAlarm.getType(), protobufMapper.toType(onmsAlarm)) &&
                Objects.equals(protoAlarm.getSeverity(), onmsAlarm.getSeverity() == null ? null :
                        protobufMapper.toSeverity(onmsAlarm.getSeverity())) &&
                equalsOrZero(protoAlarm.getFirstEventTime(), onmsAlarm.getFirstEventTime() == null ? null :
                        onmsAlarm.getFirstEventTime().getTime()) &&
                equalsOrEmpty(protoAlarm.getDescription(), onmsAlarm.getDescription()) &&
                equalsOrEmpty(protoAlarm.getLogMessage(), onmsAlarm.getLogMsg()) &&
                equalsOrEmpty(protoAlarm.getAckUser(), onmsAlarm.getAckUser()) &&
                equalsOrZero(protoAlarm.getAckTime(), onmsAlarm.getAckTime() == null ? null :
                        onmsAlarm.getAckTime().getTime()) &&
                equalsOrZero(protoAlarm.getIfIndex(), onmsAlarm.getIfIndex()) &&
                equalsOrEmpty(protoAlarm.getOperatorInstructions(), onmsAlarm.getOperInstruct()) &&
                equalsOrEmpty(protoAlarm.getClearKey(), onmsAlarm.getClearKey()) &&
                equalsOrEmpty(protoAlarm.getManagedObjectInstance(), onmsAlarm.getManagedObjectInstance()) &&
                equalsOrEmpty(protoAlarm.getManagedObjectType(), onmsAlarm.getManagedObjectType()) &&
                // Note: I'm assuming comparing the set of related alarm Ids is sufficient here as differences in the
                // related alarms themselves shouldn't be interesting for the consumers of this method
                Objects.equals(protoAlarm.getRelatedAlarmList()
                        .stream()
                        .map(ra -> (int) ra.getId())
                        .collect(Collectors.toSet()), onmsAlarm.getRelatedAlarmIds()));
    }

    /**
     * Compares the fields in {@link #protoAlarm the contained alarm} with {@link OnmsAlarm the given alarm} for any
     * differences in logical equality.
     *
     * @param onmsAlarm the alarm to compare with
     * @return whether or not the given alarm has any differences
     */
    public boolean equalTo(OnmsAlarm onmsAlarm) {
        return !hasIncrementalDifferences(onmsAlarm) && !hasNonIncrementalDifferences(onmsAlarm);
    }

    // The methods below accommodate comparisons between primitive fields in the proto objects and reference fields in
    // the Onms* objects
    //
    // Because of the difference in types we can't tell the difference between a value being unset or explicitly set to
    // 0 on the proto objects

    private static boolean equalsOrEmpty(String a, String b) {
        if (b == null) {
            return Objects.equals(a, "");
        }

        return b.equals(a);
    }

    private static boolean equalsOrZero(long a, Long b) {
        if (b == null) {
            return a == 0;
        }

        return b.equals(a);
    }

    private static boolean equalsOrZero(int a, Integer b) {
        return equalsOrZero((long) a, b == null ? null : b.longValue());
    }
}
