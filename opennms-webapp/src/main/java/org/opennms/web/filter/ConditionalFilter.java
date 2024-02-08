/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
        
        final StringBuilder buf = new StringBuilder(TYPE);
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
        
        final StringBuilder buf = new StringBuilder("( ");
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
        
        final StringBuilder buf = new StringBuilder("( ");
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
        
        final StringBuilder buf = new StringBuilder("( ");
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
