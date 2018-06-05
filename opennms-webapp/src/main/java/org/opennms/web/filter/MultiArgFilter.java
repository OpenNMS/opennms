/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.filter;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

/**
 * TwoArgFilter
 *
 * @author brozow
 * @version $Id: $
 * @since 1.8.1
 */
public abstract class MultiArgFilter<T> extends BaseFilter<T> {

    private T[] m_values;
    
    /**
     * <p>Constructor for MultiArgFilter.</p>
     *
     * @param filterType a {@link java.lang.String} object.
     * @param sqlType a {@link org.opennms.web.filter.SQLType} object.
     * @param fieldName a {@link java.lang.String} object.
     * @param propertyName a {@link java.lang.String} object.
     * @param values an array of T objects.
     * @param <T> a T object.
     */
    public MultiArgFilter(String filterType, SQLType<T> sqlType, String fieldName, String propertyName, T[] values) {
        super(filterType, sqlType, fieldName, propertyName);
        m_values = values;
    }
    
    /**
     * <p>getValues</p>
     *
     * @return an array of T objects.
     */
    public T[] getValues() {
        return m_values;
    }
    
    /**
     * <p>getValuesAsList</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<T> getValuesAsList() {
        return Arrays.asList(m_values);
    }
    
    /**
     * <p>getSQLTemplate</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public abstract String getSQLTemplate();

    /** {@inheritDoc} */
    @Override
    public final int bindParam(PreparedStatement ps, int parameterIndex) throws SQLException {
        for(int i = 0; i < m_values.length; i++) {
            bindValue(ps, parameterIndex+i, m_values[i]);
        }
        return m_values.length;
    }

    /** {@inheritDoc} */
    @Override
    public final String getValueString() {
        final StringBuilder buf = new StringBuilder();
        for(int i = 0; i < m_values.length; i++) {
            if (i != 0) {
                buf.append(',');
            }
            buf.append(getValueAsString(m_values[i]));
        }
        return buf.toString();
    }
    
    /** {@inheritDoc} */
    @Override
    public final String getParamSql() {
        Object[] qmarks = new String[m_values.length];

        Arrays.fill(qmarks, "?");

        return String.format(getSQLTemplate(), qmarks);
    }

    /** {@inheritDoc} */
    @Override
    public final String getSql() {
        Object[] formattedVals = new String[m_values.length];
        
        for(int i = 0; i < m_values.length; i++) {
            formattedVals[i] = formatValue(m_values[i]);
        }
        return String.format(getSQLTemplate(), formattedVals);
    }

    


}
