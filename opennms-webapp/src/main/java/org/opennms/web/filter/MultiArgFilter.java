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
