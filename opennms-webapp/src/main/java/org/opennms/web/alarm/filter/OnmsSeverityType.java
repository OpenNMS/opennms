/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 2 of the License,
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

package org.opennms.web.alarm.filter;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.web.filter.SQLType;

/**
 * OnmsSeverityType
 *
 * @author brozow
 * @version $Id: $
 * @since 1.8.1
 */
public class OnmsSeverityType implements SQLType<OnmsSeverity> {

    /**
     * <p>bindParam</p>
     *
     * @param ps a {@link java.sql.PreparedStatement} object.
     * @param parameterIndex a int.
     * @param value a {@link org.opennms.netmgt.model.OnmsSeverity} object.
     * @throws java.sql.SQLException if any.
     */
    public void bindParam(PreparedStatement ps, int parameterIndex, OnmsSeverity value) throws SQLException {
        ps.setInt(parameterIndex, value.getId());
    }

    /**
     * <p>createArray</p>
     *
     * @param value1 a {@link org.opennms.netmgt.model.OnmsSeverity} object.
     * @param value2 a {@link org.opennms.netmgt.model.OnmsSeverity} object.
     * @return an array of {@link org.opennms.netmgt.model.OnmsSeverity} objects.
     */
    public OnmsSeverity[] createArray(OnmsSeverity value1, OnmsSeverity value2) {
        return new OnmsSeverity[] { value1, value2 };
    }

    /**
     * <p>formatValue</p>
     *
     * @param value a {@link org.opennms.netmgt.model.OnmsSeverity} object.
     * @return a {@link java.lang.String} object.
     */
    public String formatValue(OnmsSeverity value) {
        return String.valueOf(value.getId());
    }

    /**
     * <p>getValueAsString</p>
     *
     * @param value a {@link org.opennms.netmgt.model.OnmsSeverity} object.
     * @return a {@link java.lang.String} object.
     */
    public String getValueAsString(OnmsSeverity value) {
        return String.valueOf(value.getId());
    }

}
