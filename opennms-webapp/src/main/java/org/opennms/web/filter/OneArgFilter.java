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
