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
