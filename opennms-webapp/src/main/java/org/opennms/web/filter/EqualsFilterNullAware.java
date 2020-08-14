/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

/** Allows null as value for equals operations. */
public abstract class EqualsFilterNullAware extends OneArgFilter<String> {

    public EqualsFilterNullAware(final String filterType, final SQLType<String> type, final String fieldName, final String propertyName, final String value){
        super(filterType, type, fieldName, propertyName, toValueAllowNull(value));
    }

    private static String toValueAllowNull(String value) {
        return "null".equals(value) ? null : value;
    }

    @Override
    public Criterion getCriterion() {
        if(getValue() == null) {
            return Restrictions.isNull(getPropertyName());
        }
        return Restrictions.eq(getPropertyName(), getValue());
    }

    @Override
    public String getSQLTemplate() {
        if(getValue() == null) {
            return " " + getSQLFieldName() + " IS NULL ";
        }
        return " " + getSQLFieldName() + " =  %s ";
    }

}
