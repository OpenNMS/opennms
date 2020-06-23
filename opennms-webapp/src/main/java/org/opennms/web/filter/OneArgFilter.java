/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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

/**
 * OneArgFilter
 *
 * @author brozow
 * @version $Id: $
 * @since 1.8.1
 */
public abstract class OneArgFilter<T> extends BaseFilter<T> {
    
    private T m_value;
    
    /**
     * <p>Constructor for OneArgFilter.</p>
     *
     * @param filterType a {@link java.lang.String} object.
     * @param sqlType a {@link org.opennms.web.filter.SQLType} object.
     * @param fieldName a {@link java.lang.String} object.
     * @param propertyName a {@link java.lang.String} object.
     * @param value a T object.
     * @param <T> a T object.
     */
    public OneArgFilter(String filterType, SQLType<T> sqlType, String fieldName, String propertyName, T value) {
        super(filterType, sqlType, fieldName, propertyName);
        m_value = value;
    }
    
    /**
     * <p>getValue</p>
     *
     * @return a T object.
     */
    public final T getValue() { return m_value; };

    /**
     * <p>getSQLTemplate</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public abstract String getSQLTemplate();
    
    /**
     * <p>getBoundValue</p>
     *
     * @param value a T object.
     * @return a T object.
     */
    public T getBoundValue(T value) {
        return value;
    }

    /** {@inheritDoc} */
    @Override
    public final int bindParam(PreparedStatement ps, int parameterIndex) throws SQLException {
        bindValue(ps, parameterIndex, getBoundValue(m_value));
        return 1;
    }
    
    /** {@inheritDoc} */
    @Override
    public final String getValueString() {
        return getValueAsString(m_value);
    }
    

    /** {@inheritDoc} */
    @Override
    public final String getParamSql() {
        return String.format(getSQLTemplate(), "?");
    }

    /** {@inheritDoc} */
    @Override
    public final String getSql() {
        return String.format(getSQLTemplate(), formatValue(m_value));
    }
    
    @Override
    public String getTextDescription() {
        return getDescription();
    }

}
