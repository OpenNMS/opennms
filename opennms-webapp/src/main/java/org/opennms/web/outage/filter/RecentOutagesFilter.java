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

package org.opennms.web.outage.filter;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.opennms.web.filter.OneArgFilter;
import org.opennms.web.filter.SQLType;

/**
 * <p>RecentOutagesFilter class.</p>
 *
 * @author brozow
 * @version $Id: $
 * @since 1.8.1
 */
public class RecentOutagesFilter extends OneArgFilter<Date> {
    /** Constant <code>TYPE="recent"</code> */
    public static final String TYPE = "recent";
    
    /**
     * <p>Constructor for RecentOutagesFilter.</p>
     */
    public RecentOutagesFilter() {
        this(yesterday());
    }
    
    /**
     * <p>Constructor for RecentOutagesFilter.</p>
     *
     * @param since a {@link java.util.Date} object.
     */
    public RecentOutagesFilter(Date since) {
        super(TYPE, SQLType.DATE, "OUTAGES.IFREGAINEDSERVICE", "ifRegainedService", since);
    }
    
    /** {@inheritDoc} */
    @Override
    public String getSQLTemplate() {
        return " (" + getSQLFieldName() + " > %s OR " + getSQLFieldName() + " IS NULL) ";
    }

    /** {@inheritDoc} */
    @Override
    public Criterion getCriterion() {
        return Restrictions.or(Restrictions.gt(getPropertyName(), getValue()), Restrictions.isNull(getPropertyName()));
    }

    /** {@inheritDoc} */
    @Override
    public String getTextDescription() {
        return "outage since " + getValueAsString(getValue());
    }
    
    /**
     * <p>yesterday</p>
     *
     * @return a {@link java.util.Date} object.
     */
    public static Date yesterday() {
        Calendar cal = new GregorianCalendar();
        cal.add( Calendar.DATE, -1 );
        return cal.getTime();
    }

}
