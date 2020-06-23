/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
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
 * <p>Abstract InFilter class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public abstract class InFilter<T> extends MultiArgFilter<T> {
    
    /**
     * <p>Constructor for InFilter.</p>
     *
     * @param filterType a {@link java.lang.String} object.
     * @param type a {@link org.opennms.web.filter.SQLType} object.
     * @param fieldName a {@link java.lang.String} object.
     * @param propertyName a {@link java.lang.String} object.
     * @param values an array of T objects.
     * @param <T> a T object.
     */
    public InFilter(String filterType, SQLType<T> type, String fieldName, String propertyName, T[] values){
        super(filterType, type, fieldName, propertyName, values);
    }
    
    /** {@inheritDoc} */
    @Override
    public Criterion getCriterion() {
        return Restrictions.in(getPropertyName(), getValuesAsList());
    }
    
    /** {@inheritDoc} */
    @Override
    public String getSQLTemplate() {
        final StringBuilder buf = new StringBuilder(" ");
        buf.append(getSQLFieldName());
        buf.append(" IN (");
        T[] values = getValues();
        
        for(int i = 0; i < values.length; i++) {
            if (i != 0) {
                buf.append(", ");
            }
            buf.append("%s");
        }
        buf.append(") ");
        return buf.toString();
    }

}
