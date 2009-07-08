/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/


package org.opennms.web.filter;

import org.hibernate.Hibernate;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.StringType;
import org.hibernate.type.Type;


/** Encapsulates all interface filtering functionality. */
public abstract class IPLikeFilter extends OneArgFilter<String> {

    private static final Type STRING_TYPE = new StringType();

    public IPLikeFilter(String filterType, String fieldName, String propertyName, String ipLikePattern) {
        super(filterType, SQLType.STRING, fieldName, propertyName, ipLikePattern);
    }

    @Override
    public String getSQLTemplate() {
        return " IPLIKE("+getSQLFieldName()+", %s) ";
    }

    @Override
    public Criterion getCriterion() {
        return Restrictions.sqlRestriction("iplike( {alias}."+getPropertyName()+", ?)", getValue(), Hibernate.STRING);
    }
    
    

}
