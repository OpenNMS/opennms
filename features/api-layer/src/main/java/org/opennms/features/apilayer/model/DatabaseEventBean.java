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
import java.util.stream.Collectors;

import org.opennms.integration.api.v1.model.DatabaseEvent;
import org.opennms.integration.api.v1.model.EventParameter;
import org.opennms.netmgt.model.OnmsEvent;

import com.google.common.collect.ImmutableList;

public class DatabaseEventBean implements DatabaseEvent {

    private final OnmsEvent event;

    private final List<EventParameter> parameters;

    public DatabaseEventBean(OnmsEvent event) {
        this.event = Objects.requireNonNull(event);
        this.parameters = ImmutableList.copyOf(event.getEventParameters().stream()
                .map(EventParameterBean::new)
                .collect(Collectors.toList()));
    }

    @Override
    public String getUei() {
        return event.getEventUei();
    }

    @Override
    public Integer getId() {
        return event.getId();
    }

    @Override
    public List<EventParameter> getParameters() {
        return parameters;
    }

    @Override
    public List<EventParameter> getParametersByName(String name) {
        return parameters.stream()
                .filter(p -> Objects.equals(name, p.getName()))
                .collect(Collectors.toList());
    }
}
