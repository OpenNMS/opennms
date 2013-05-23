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
 * <p>IntegerSqlType class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class IntegerSqlType implements SQLType<Integer> {

    /**
     * <p>formatValue</p>
     *
     * @param value a {@link java.lang.Integer} object.
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String formatValue(Integer value) {
        return value.toString();
    }
    
    /**
     * <p>getValueAsString</p>
     *
     * @param value a {@link java.lang.Integer} object.
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getValueAsString(Integer value) {
        return String.valueOf(value);
    }

    /**
     * <p>bindParam</p>
     *
     * @param ps a {@link java.sql.PreparedStatement} object.
     * @param parameterIndex a int.
     * @param value a {@link java.lang.Integer} object.
     * @throws java.sql.SQLException if any.
     */
    @Override
    public void bindParam(PreparedStatement ps, int parameterIndex, Integer value) throws SQLException {
        ps.setInt(parameterIndex, value);
    }

    /**
     * <p>createArray</p>
     *
     * @param value1 a {@link java.lang.Integer} object.
     * @param value2 a {@link java.lang.Integer} object.
     * @return an array of {@link java.lang.Integer} objects.
     */
    @Override
    public Integer[] createArray(Integer value1, Integer value2) {
        return new Integer[] { value1, value2 };
    }

}
