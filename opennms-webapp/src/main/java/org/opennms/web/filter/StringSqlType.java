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

/**
 * <p>StringSqlType class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class StringSqlType implements SQLType<String> {

    /**
     * <p>bindParam</p>
     *
     * @param ps a {@link java.sql.PreparedStatement} object.
     * @param parameterIndex a int.
     * @param value a {@link java.lang.String} object.
     * @throws java.sql.SQLException if any.
     */
    @Override
    public void bindParam(PreparedStatement ps, int parameterIndex, String value) throws SQLException {
        ps.setString(parameterIndex, value);
    }
    
    /**
     * <p>getValueAsString</p>
     *
     * @param value a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getValueAsString(String value) {
        return value;
    }

    /**
     * <p>formatValue</p>
     *
     * @param value a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String formatValue(String value) {
        return "'" + value + "'";
    }

    /**
     * <p>createArray</p>
     *
     * @param value1 a {@link java.lang.String} object.
     * @param value2 a {@link java.lang.String} object.
     * @return an array of {@link java.lang.String} objects.
     */
    @Override
    public String[] createArray(String value1, String value2) {
        return new String[] { value1, value2 };
    }

}
