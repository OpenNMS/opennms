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

import java.sql.SQLException;

import org.opennms.web.element.NetworkElementFactory;
import org.opennms.web.filter.NotEqualOrNullFilter;
import org.opennms.web.filter.SQLType;

/** Encapsulates all service filtering functionality. */
public class NegativeServiceFilter extends NotEqualOrNullFilter<Integer> {
    public static final String TYPE = "servicenot";

    public NegativeServiceFilter(int serviceId) {
        super(TYPE, SQLType.INT, "SERVICEID", "serviceType.id", serviceId);
    }

    public String getTextDescription() {
        String serviceName = Integer.toString(getValue());
        try {
            serviceName = NetworkElementFactory.getServiceNameFromId(getValue());
        } catch (SQLException e) {
        }

        return ("service is not " + serviceName);
    }

    public String toString() {
        return ("<AlarmFactory.NegativeServiceFilter: " + this.getDescription() + ">");
    }

    public int getServiceId() {
        return getValue();
    }

    public boolean equals(Object obj) {
        return (this.toString().equals(obj.toString()));
    }
}
