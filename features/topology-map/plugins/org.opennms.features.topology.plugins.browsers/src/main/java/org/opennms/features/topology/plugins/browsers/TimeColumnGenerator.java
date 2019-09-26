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

package org.opennms.features.topology.plugins.browsers;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;

import org.opennms.features.timeformat.api.TimeformatService;
import org.opennms.vaadin.user.UserTimeZoneExtractor;

import com.vaadin.v7.data.Property;
import com.vaadin.v7.ui.Table;

public class TimeColumnGenerator  implements Table.ColumnGenerator {

    private TimeformatService timeformatService;

    public TimeColumnGenerator(TimeformatService timeformatService) {
        this.timeformatService = timeformatService;
    }

    @Override
    public Object generateCell(Table source, Object itemId, Object columnId) {
        final ZoneId userTimeZoneId =  UserTimeZoneExtractor.extractUserTimeZoneIdOrNull(source.getUI());
        final Property property = source.getContainerProperty(itemId, columnId);
        if (property == null || property.getValue() == null) {
            return null;
        }
        String formattedValue;
        if(property.getType().equals(Instant.class)){
            formattedValue = timeformatService.format((Instant) property.getValue(), userTimeZoneId);
        } else if(property.getType().equals(Date.class)){
            formattedValue = timeformatService.format((Date) property.getValue(), userTimeZoneId);
        } else {
            formattedValue = property.toString();
        }
        return formattedValue;
    }
}
