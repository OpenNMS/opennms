/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2005-2006, 2008-2009 The OpenNMS Group, Inc.  All rights reserved.
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


package org.opennms.web.alarm.filter;

import java.util.Date;

import org.opennms.web.filter.LessThanFilter;
import org.opennms.web.filter.SQLType;

public class BeforeFirstEventTimeFilter extends LessThanFilter<Date> {
    public static final String TYPE = "beforefirsteventtime";

    public BeforeFirstEventTimeFilter(Date date) {
        super(TYPE, SQLType.DATE, "FIRSTEVENTTIME", "firstEventTime", date);
    }

    public BeforeFirstEventTimeFilter(long epochTime) {
        this(new Date(epochTime));
    }


    public String getTextDescription() {
        return ("time of first event before \"" + getValue() + "\"");
    }

    public String toString() {
        return ("<BeforeFirstEventTimeFilter: " + this.getDescription() + ">");
    }

    public Date getDate() {
        return getValue();
    }

    public boolean equals(Object obj) {
        return (this.toString().equals(obj.toString()));
    }
}
