/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
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
    public final String getDescription() {
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
    public abstract Criterion getCriterion();

    /** {@inheritDoc} */
    public abstract int bindParam(PreparedStatement ps, int parameterIndex) throws SQLException;

    /**
     * <p>getParamSql</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public abstract String getParamSql();

    /**
     * <p>getSql</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public abstract String getSql();

    /**
     * <p>getTextDescription</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public abstract String getTextDescription();

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String toString() {
        return new ToStringBuilder(this)
            .append("description", getDescription())
            .append("text description", getTextDescription())
            .append("SQL field name", getSQLFieldName())
            .append("property name", getPropertyName())
            .toString();
    }
}
