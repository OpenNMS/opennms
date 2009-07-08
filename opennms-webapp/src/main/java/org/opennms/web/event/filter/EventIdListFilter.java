/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.web.event.filter;

import java.util.Collection;

import org.opennms.web.filter.InFilter;
import org.opennms.web.filter.SQLType;

public class EventIdListFilter extends InFilter<Integer> {
    public static final String TYPE = "eventIdList";
    
    private static Integer[] box(int[] values) {
        if (values == null) {
            return null;
        }
        
        Integer[] boxed = new Integer[values.length];
        for(int i = 0; i < values.length; i++) {
            boxed[i] = values[i];
        }
        
        return boxed;
    }
    
    public EventIdListFilter(int[] eventIds) {
        super(TYPE, SQLType.INT, "EVENTID", "id", box(eventIds));
    }
    
    public EventIdListFilter(Collection<Integer> eventIds) {
        super(TYPE, SQLType.INT, "EVENTID", "id", eventIds.toArray(new Integer[0]));
    }

    public String getTextDescription() {
        StringBuilder buf = new StringBuilder("eventId in ");
        buf.append("(");
        buf.append(getValueString());
        buf.append(")");
        return buf.toString();
    }
    
}
