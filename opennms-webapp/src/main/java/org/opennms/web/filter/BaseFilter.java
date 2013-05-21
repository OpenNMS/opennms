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

import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.criterion.Criterion;

/**
 * BaseFilter
 *
 * @author brozow
 * @version $Id: $
 * @since 1.8.1
 */
public abstract class BaseFilter<T> implements Filter {
    
    protected String m_filterName;
    protected SQLType<T> m_sqlType;
    private String m_fieldName;
    private String m_propertyName;
    
    /**
     * <p>Constructor for BaseFilter.</p>
     *
     * @param filterType a {@link java.lang.String} object.
     * @param sqlType a {@link org.opennms.web.filter.SQLType} object.
     * @param fieldName a {@link java.lang.String} object.
     * @param propertyName a {@link java.lang.String} object.
     * @param <T> a T object.
     */
    public BaseFilter(String filterType, SQLType<T> sqlType, String fieldName, String propertyName) {
        m_filterName = filterType;
        m_sqlType = sqlType;
        m_fieldName = fieldName;
        m_propertyName = propertyName;
    }


    /**
     * <p>getSQLFieldName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSQLFieldName() {
        return m_fieldName;
    }
    
    /**
     * <p>getPropertyName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getPropertyName() {
        return m_propertyName;
    }

    /**
     * <p>getDescription</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getDescription() {
        return m_filterName+"="+getValueString();
    }
    
    /**
     * <p>bindValue</p>
     *
     * @param ps a {@link java.sql.PreparedStatement} object.
     * @param parameterIndex a int.
     * @param value a T object.
     * @throws java.sql.SQLException if any.
     */
    final public void bindValue(PreparedStatement ps, int parameterIndex, T value) throws SQLException {
        m_sqlType.bindParam(ps, parameterIndex, value);
    }
    
    /**
     * <p>formatValue</p>
     *
     * @param value a T object.
     * @return a {@link java.lang.String} object.
     */
    public String formatValue(T value) {
        return m_sqlType.formatValue(value);
    }
    
    /**
     * <p>getValueAsString</p>
     *
     * @param value a T object.
     * @return a {@link java.lang.String} object.
     */
    final public String getValueAsString(T value) {
        return m_sqlType.getValueAsString(value);
    }
    
    /**
     * <p>getValueString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public abstract String getValueString();
    
    /**
     * <p>getCriterion</p>
     *
     * @return a {@link org.hibernate.criterion.Criterion} object.
     */
    @Override
    public abstract Criterion getCriterion();

    /** {@inheritDoc} */
    @Override
    public abstract int bindParam(PreparedStatement ps, int parameterIndex) throws SQLException;

    /**
     * <p>getParamSql</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public abstract String getParamSql();

    /**
     * <p>getSql</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public abstract String getSql();

    /**
     * <p>getTextDescription</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public abstract String getTextDescription();

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("description", getDescription())
            .append("text description", getTextDescription())
            .append("SQL field name", getSQLFieldName())
            .append("property name", getPropertyName())
            .toString();
    }
}
