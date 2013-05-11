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
import org.opennms.netmgt.model.OnmsCriteria;


/**
 * <p>Abstract ConditionalFilter class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public abstract class ConditionalFilter implements Filter {
    /** Constant <code>TYPE="conditionalFilter"</code> */
    public static final String TYPE = "conditionalFilter";
    
    private String m_conditionType;
    private Filter[] m_filters;
    
    /**
     * <p>Constructor for ConditionalFilter.</p>
     *
     * @param conditionType a {@link java.lang.String} object.
     * @param filters a {@link org.opennms.web.filter.Filter} object.
     */
    public ConditionalFilter(String conditionType, Filter... filters){
        if (filters.length == 0) {
            throw new IllegalArgumentException("You must pass at least one filter");
        }
        m_conditionType = conditionType;
        m_filters = filters;
    }
    
    /**
     * <p>getFilters</p>
     *
     * @return an array of {@link org.opennms.web.filter.Filter} objects.
     */
    public Filter[] getFilters() {
        return m_filters;
    }
    
    /** {@inheritDoc} */
    @Override
    public int bindParam(PreparedStatement ps, int parameterIndex) throws SQLException {
        int parametersBound = 0;
        for (Filter mFilter : m_filters) {
            parametersBound += mFilter.bindParam(ps, parameterIndex + parametersBound);
        }
        return parametersBound;
    }

    /**
     * <p>getDescription</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getDescription() {
        if (m_filters.length == 1) {
            return m_filters[0].getDescription();
        }
        
        StringBuilder buf = new StringBuilder(TYPE);
        buf.append("=");
        buf.append(m_conditionType);
        for(Filter filter : m_filters) {
            buf.append('(');
            buf.append(filter.getDescription());
            buf.append(')');
        }
        return buf.toString();
    }

    /**
     * <p>getParamSql</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getParamSql() {
        if (m_filters.length == 1) {
            return m_filters[0].getParamSql();
        }
        
        StringBuilder buf = new StringBuilder("( ");
        for(int i = 0; i < m_filters.length; i++){
            if (i != 0) {
                buf.append(m_conditionType);
                buf.append(" ");
            }
            buf.append(m_filters[i].getParamSql());
        }
        buf.append(") ");
        return buf.toString();
    }

    /**
     * <p>getSql</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getSql() {
        if (m_filters.length == 1) {
            return m_filters[0].getSql();
        }
        
        StringBuilder buf = new StringBuilder("( ");
        for(int i = 0; i < m_filters.length; i++){
            if (i != 0) {
                buf.append(m_conditionType);
                buf.append(" ");
            }
            buf.append(m_filters[i].getSql());
        }
        buf.append(") ");
        return buf.toString();
    }

    /**
     * <p>getTextDescription</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getTextDescription() {
        if (m_filters.length == 1) {
            return m_filters[0].getTextDescription();
        }
        
        StringBuilder buf = new StringBuilder("( ");
        for(int i = 0; i < m_filters.length; i++){
            if (i != 0) {
                buf.append(m_conditionType);
                buf.append(" ");
            }
            buf.append(m_filters[i].getTextDescription());
        }
        buf.append(")");
        return buf.toString();
    }


    /**
     * <p>applyCriteria</p>
     *
     * @param criteria a {@link org.opennms.netmgt.model.OnmsCriteria} object.
     */
    public void applyCriteria(OnmsCriteria criteria) {
        criteria.add(getCriterion());
    }
    
    /**
     * <p>getCriterion</p>
     *
     * @return a {@link org.hibernate.criterion.Criterion} object.
     */
    @Override
    abstract public Criterion getCriterion();

    
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
            .toString();
    }

}
