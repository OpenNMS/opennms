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

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

/**
 * <p>Abstract BetweenFilter class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public abstract class BetweenFilter<T> extends MultiArgFilter<T> {
    
    /**
     * <p>Constructor for BetweenFilter.</p>
     *
     * @param filterType a {@link java.lang.String} object.
     * @param type a {@link org.opennms.web.filter.SQLType} object.
     * @param fieldName a {@link java.lang.String} object.
     * @param propertyName a {@link java.lang.String} object.
     * @param first a T object.
     * @param last a T object.
     * @param <T> a T object.
     */
    public BetweenFilter(String filterType, SQLType<T> type, String fieldName, String propertyName, T first, T last) {
        super(filterType, type, fieldName, propertyName, type.createArray(first, last));
    }

    /**
     * <p>getFirst</p>
     *
     * @return a T object.
     */
    public T getFirst() { return getValues()[0]; }
    /**
     * <p>getLast</p>
     *
     * @return a T object.
     */
    public T getLast() { return getValues()[1]; }
    
    /** {@inheritDoc} */
    @Override
    public Criterion getCriterion() {
        return Restrictions.between(getPropertyName(), getFirst(), getLast());
    }
    
    /** {@inheritDoc} */
    @Override
    public String getSQLTemplate() {
        return " "+getSQLFieldName() + " BETWEEN %s AND %s ";
    }

    
}
