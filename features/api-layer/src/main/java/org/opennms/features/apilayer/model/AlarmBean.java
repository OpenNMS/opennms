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

package org.opennms.features.apilayer.model;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.opennms.features.apilayer.utils.ModelMappers;
import org.opennms.integration.api.v1.config.events.AlarmType;
import org.opennms.integration.api.v1.model.Alarm;
import org.opennms.integration.api.v1.model.DatabaseEvent;
import org.opennms.integration.api.v1.model.Node;
import org.opennms.integration.api.v1.model.Severity;
import org.opennms.netmgt.model.OnmsAlarm;

import com.google.common.collect.ImmutableMap;

public class AlarmBean implements Alarm {

    private final OnmsAlarm alarm;

    private final NodeBean node;

    private final Map<String, String> attributes;

    private final Severity severity;

    private final List<Alarm> relatedAlarms;

    private final DatabaseEvent lastEvent;

    public AlarmBean(OnmsAlarm alarm) {
        this.alarm = Objects.requireNonNull(alarm);
        this.attributes = alarm.getDetails() != null ? ImmutableMap.copyOf(alarm.getDetails()) : Collections.emptyMap();
        this.node = alarm.getNode() != null ? new NodeBean(alarm.getNode()) : null;
        this.severity = ModelMappers.toSeverity(alarm.getSeverity());
        this.relatedAlarms = alarm.getRelatedAlarms().stream().map(AlarmBean::new).collect(Collectors.toList());
        this.lastEvent = ModelMappers.toEvent(alarm.getLastEvent());
    }

    @Override
    public String getReductionKey() {
        return alarm.getReductionKey();
    }

    @Override
    public Integer getId() {
        return alarm.getId();
    }

    @Override
    public Node getNode() {
        return node;
    }

    @Override
    public AlarmType getType() {
        return null;
    }

    @Override
    public String getManagedObjectInstance() {
        return alarm.getManagedObjectInstance();
    }

    @Override
    public String getManagedObjectType() {
        return alarm.getManagedObjectType();
    }

    @Override
    public Map<String, String> getAttributes() {
        return attributes;
    }

    @Override
    public Severity getSeverity() {
        return severity;
    }

    @Override
    public boolean isSituation() {
        return relatedAlarms.size() > 0;
    }

    @Override
    public List<Alarm> getRelatedAlarms() {
        return relatedAlarms;
    }

    @Override
    public String getLogMessage() {
        return alarm.getLogMsg();
    }

    @Override
    public String getDescription() {
        return alarm.getDescription();
    }

    @Override
    public Date getLastEventTime() {
        return alarm.getLastEventTime();
    }

    @Override
    public Date getFirstEventTime() {
        return alarm.getFirstEventTime();
    }

    @Override
    public DatabaseEvent getLastEvent() {
        return lastEvent;
    }

}
