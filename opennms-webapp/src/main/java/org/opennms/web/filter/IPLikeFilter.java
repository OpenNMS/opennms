/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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
import org.hibernate.type.StringType;


/**
 * Encapsulates all interface filtering functionality.
 */
public abstract class IPLikeFilter extends OneArgFilter<String> {

    /**
     * <p>Constructor for IPLikeFilter.</p>
     *
     * @param filterType a {@link java.lang.String} object.
     * @param fieldName a {@link java.lang.String} object.
     * @param propertyName a {@link java.lang.String} object.
     * @param ipLikePattern a {@link java.lang.String} object.
     */
    public IPLikeFilter(final String filterType, final String fieldName, final String propertyName, final String ipLikePattern) {
        super(filterType, SQLType.STRING, fieldName, propertyName, ipLikePattern);
    }

    /** {@inheritDoc} */
    @Override
    public String getSQLTemplate() {
        return " IPLIKE(" + getSQLFieldName() + ", %s) ";
    }

    /** {@inheritDoc} */
    @Override
    public Criterion getCriterion() {
        return Restrictions.sqlRestriction("iplike( {alias}."+getPropertyName()+", ?)", getValue(), StringType.INSTANCE);
    }
    
    

}
