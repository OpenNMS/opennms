/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

import org.hibernate.Hibernate;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.StringType;
import org.hibernate.type.Type;


/**
 * Encapsulates all interface filtering functionality.
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public abstract class IPLikeFilter extends OneArgFilter<String> {

    private static final Type STRING_TYPE = new StringType();

    /**
     * <p>Constructor for IPLikeFilter.</p>
     *
     * @param filterType a {@link java.lang.String} object.
     * @param fieldName a {@link java.lang.String} object.
     * @param propertyName a {@link java.lang.String} object.
     * @param ipLikePattern a {@link java.lang.String} object.
     */
    public IPLikeFilter(String filterType, String fieldName, String propertyName, String ipLikePattern) {
        super(filterType, SQLType.STRING, fieldName, propertyName, ipLikePattern);
    }

    /** {@inheritDoc} */
    @Override
    public String getSQLTemplate() {
        return " IPLIKE("+getSQLFieldName()+", %s) ";
    }

    /** {@inheritDoc} */
    @Override
    public Criterion getCriterion() {
        return Restrictions.sqlRestriction("iplike( {alias}."+getPropertyName()+", ?)", getValue(), Hibernate.STRING);
    }
    
    

}
