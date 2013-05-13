/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.filter;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;

import org.opennms.netmgt.EventConstants;

/**
 * <p>DateSqlType class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class DateSqlType implements SQLType<Date> {

    /**
     * <p>bindParam</p>
     *
     * @param ps a {@link java.sql.PreparedStatement} object.
     * @param parameterIndex a int.
     * @param value a {@link java.util.Date} object.
     * @throws java.sql.SQLException if any.
     */
    @Override
    public void bindParam(PreparedStatement ps, int parameterIndex, Date value) throws SQLException {
        ps.setTimestamp(parameterIndex, new java.sql.Timestamp(value.getTime()));
    }
    
    /**
     * <p>getValueAsString</p>
     *
     * @param value a {@link java.util.Date} object.
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getValueAsString(Date value) {
        return value == null ? "Null" : String.valueOf(value.getTime());
    }

    /**
     * <p>formatValue</p>
     *
     * @param value a {@link java.util.Date} object.
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String formatValue(Date value) {
        return "to_timestamp(\'" + value.toString() + "\', " + EventConstants.POSTGRES_DATE_FORMAT +")";
    }

    /**
     * <p>createArray</p>
     *
     * @param value1 a {@link java.util.Date} object.
     * @param value2 a {@link java.util.Date} object.
     * @return an array of {@link java.util.Date} objects.
     */
    @Override
    public Date[] createArray(Date value1, Date value2) {
        return new Date[] { value1, value2 };
    }

}
