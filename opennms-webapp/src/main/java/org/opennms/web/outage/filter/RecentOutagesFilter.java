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
