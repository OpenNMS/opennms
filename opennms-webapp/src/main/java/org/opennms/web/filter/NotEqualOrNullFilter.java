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

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

/**
 * <p>Abstract NotEqualOrNullFilter class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public abstract class NotEqualOrNullFilter<T> extends OneArgFilter<T> {
    
    
    /**
     * <p>Constructor for NotEqualOrNullFilter.</p>
     *
     * @param filterType a {@link java.lang.String} object.
     * @param type a {@link org.opennms.web.filter.SQLType} object.
     * @param fieldName a {@link java.lang.String} object.
     * @param propertyName a {@link java.lang.String} object.
     * @param value a T object.
     * @param <T> a T object.
     */
    public NotEqualOrNullFilter(final String filterType, final SQLType<T> type, final String fieldName, final String propertyName, final T value) {
        super(filterType, type, fieldName, propertyName, value);
    }

    /**
     * <p>getCriterion</p>
     *
     * @return a {@link org.hibernate.criterion.Criterion} object.
     */
    @Override
    public Criterion getCriterion() {
        return Restrictions.or(Restrictions.ne(getPropertyName(), getValue()), Restrictions.isNull(getPropertyName()));
    }
    
    /** {@inheritDoc} */
    @Override
    public String getSQLTemplate() {
        return " (" + getSQLFieldName() + "<> %s OR " + getSQLFieldName() + " IS NULL) ";
    }

}
