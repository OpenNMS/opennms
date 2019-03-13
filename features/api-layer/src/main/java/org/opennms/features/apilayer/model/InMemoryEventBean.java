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

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.opennms.features.apilayer.utils.ModelMappers;
import org.opennms.integration.api.v1.model.EventParameter;
import org.opennms.integration.api.v1.model.InMemoryEvent;
import org.opennms.integration.api.v1.model.Severity;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.xml.event.Event;

import com.google.common.collect.ImmutableList;

public class InMemoryEventBean implements InMemoryEvent {

    private final Event event;
    private final Severity severity;
    private final List<EventParameter> parameters;

    public InMemoryEventBean(Event event) {
        this.event = Objects.requireNonNull(event);
        this.severity = ModelMappers.toSeverity(OnmsSeverity.get(event.getSeverity()));
        this.parameters = ImmutableList.copyOf(event.getParmCollection().stream()
                .filter(Objects::nonNull) // Skip null parameters
                .map(EventParameterBean::new)
                .collect(Collectors.toList()));
    }

    @Override
    public String getUei() {
        return event.getUei();
    }

    @Override
    public String getSource() {
        return event.getSource();
    }

    @Override
    public Severity getSeverity() {
        return severity;
    }

    @Override
    public List<EventParameter> getParameters() {
        return parameters;
    }

    @Override
    public Optional<String> getParameterValue(String name) {
        return parameters.stream()
                .filter(p -> Objects.equals(name, p.getName()))
                .map(EventParameter::getValue)
                .findFirst();
    }

    @Override
    public List<EventParameter> getParametersByName(String name) {
        return parameters.stream()
                .filter(p -> Objects.equals(name, p.getName()))
                .collect(Collectors.toList());
    }
}
