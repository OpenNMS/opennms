/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2007-2008 The OpenNMS Group, Inc.  All rights reserved.
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

package org.opennms.netmgt.model;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.StringType;

/**
 * Provide OpenNMS-specific Hibernate Restrictions.
 *  
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class OnmsRestrictions {
    private static final StringType STRING_TYPE = new StringType();
    
    /**
     * Performs an iplike match on the ipAddr column of the current table.
     * 
     * @param value iplike match
     * @return SQL restriction for this iplike match
     */
    public static Criterion ipLike(String value) {
        return Restrictions.sqlRestriction("iplike({alias}.ipAddr, ?)", value, STRING_TYPE);
    }
}
