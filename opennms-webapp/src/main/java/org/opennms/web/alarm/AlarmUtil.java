/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

package org.opennms.web.alarm;

import java.util.Calendar;
import java.util.NoSuchElementException;

import javax.servlet.ServletContext;

import org.apache.commons.lang.ArrayUtils;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.web.alarm.filter.AcknowledgedByFilter;
import org.opennms.web.alarm.filter.AfterFirstEventTimeFilter;
import org.opennms.web.alarm.filter.AfterLastEventTimeFilter;
import org.opennms.web.alarm.filter.BeforeFirstEventTimeFilter;
import org.opennms.web.alarm.filter.BeforeLastEventTimeFilter;
import org.opennms.web.alarm.filter.EventParmLikeFilter;
import org.opennms.web.alarm.filter.ExactUEIFilter;
import org.opennms.web.alarm.filter.IPAddrLikeFilter;
import org.opennms.web.alarm.filter.InterfaceFilter;
import org.opennms.web.alarm.filter.LogMessageMatchesAnyFilter;
import org.opennms.web.alarm.filter.LogMessageSubstringFilter;
import org.opennms.web.alarm.filter.NegativeAcknowledgedByFilter;
import org.opennms.web.alarm.filter.NegativeEventParmLikeFilter;
import org.opennms.web.alarm.filter.NegativeExactUEIFilter;
import org.opennms.web.alarm.filter.NegativeInterfaceFilter;
import org.opennms.web.alarm.filter.NegativeNodeFilter;
import org.opennms.web.alarm.filter.NegativePartialUEIFilter;
import org.opennms.web.alarm.filter.NegativeServiceFilter;
import org.opennms.web.alarm.filter.NegativeSeverityFilter;
import org.opennms.web.alarm.filter.NodeFilter;
import org.opennms.web.alarm.filter.NodeNameLikeFilter;
import org.opennms.web.alarm.filter.PartialUEIFilter;
import org.opennms.web.alarm.filter.ServiceFilter;
import org.opennms.web.alarm.filter.SeverityFilter;
import org.opennms.web.filter.Filter;

/**
 * <p>Abstract AlarmUtil class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public abstract class AlarmUtil extends Object {
    /** Constant <code>ANY_SERVICES_OPTION="Any"</code> */
    public static final String ANY_SERVICES_OPTION = "Any";

    /** Constant <code>ANY_SEVERITIES_OPTION="Any"</code> */
    public static final String ANY_SEVERITIES_OPTION = "Any";

    /** Constant <code>ANY_RELATIVE_TIMES_OPTION="Any"</code> */
    public static final String ANY_RELATIVE_TIMES_OPTION = "Any";

    /**
     * <p>getFilter</p>
     *
     * @param filterString a {@link java.lang.String} object.
     * @return a {@link org.opennms.web.filter.Filter} object.
     */
    public static Filter getFilter(String filterString, ServletContext servletContext) {
        if (filterString == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Filter filter = null;

        String[] tempTokens = filterString.split("=");
        String type;
        String value;
        try {
            type = tempTokens[0];
            String[] values = (String[]) ArrayUtils.remove(tempTokens, 0);
            value = org.apache.commons.lang.StringUtils.join(values, "=");
        } catch (NoSuchElementException e) {
            throw new IllegalArgumentException("Could not tokenize filter string: " + filterString);
        }

        if (type.equals(SeverityFilter.TYPE)) {
            filter = new SeverityFilter(OnmsSeverity.get(WebSecurityUtils.safeParseInt(value)));
        } else if (type.equals(NodeFilter.TYPE)) {
            filter = new NodeFilter(WebSecurityUtils.safeParseInt(value), servletContext);
        } else if (type.equals(NodeNameLikeFilter.TYPE)) {
            filter = new NodeNameLikeFilter(value);
        } else if (type.equals(InterfaceFilter.TYPE)) {
            filter = new InterfaceFilter(InetAddressUtils.addr(value));
        } else if (type.equals(ServiceFilter.TYPE)) {
            filter = new ServiceFilter(WebSecurityUtils.safeParseInt(value));
        } else if (type.equals(PartialUEIFilter.TYPE)) {
            filter = new PartialUEIFilter(value);
        } else if (type.equals(ExactUEIFilter.TYPE)) {
            filter = new ExactUEIFilter(value);
        } else if (type.equals(AcknowledgedByFilter.TYPE)) {
            filter = new AcknowledgedByFilter(value);
        } else if (type.equals(NegativeSeverityFilter.TYPE)) {
            filter = new NegativeSeverityFilter(OnmsSeverity.get(WebSecurityUtils.safeParseInt(value)));
        } else if (type.equals(NegativeNodeFilter.TYPE)) {
            filter = new NegativeNodeFilter(WebSecurityUtils.safeParseInt(value), servletContext);
        } else if (type.equals(NegativeInterfaceFilter.TYPE)) {
            filter = new NegativeInterfaceFilter(InetAddressUtils.addr(value));
        } else if (type.equals(NegativeServiceFilter.TYPE)) {
            filter = new NegativeServiceFilter(WebSecurityUtils.safeParseInt(value));
        } else if (type.equals(NegativePartialUEIFilter.TYPE)) {
            filter = new NegativePartialUEIFilter(value);
        } else if (type.equals(NegativeExactUEIFilter.TYPE)) {
            filter = new NegativeExactUEIFilter(value);
        } else if (type.equals(NegativeAcknowledgedByFilter.TYPE)) {
            filter = new NegativeAcknowledgedByFilter(value);
        } else if (type.equals(IPAddrLikeFilter.TYPE)) {
            filter = new IPAddrLikeFilter(value);
        } else if (type.equals(LogMessageSubstringFilter.TYPE)) {
            filter = new LogMessageSubstringFilter(value);
        } else if (type.equals(LogMessageMatchesAnyFilter.TYPE)) {
            filter = new LogMessageMatchesAnyFilter(value);
        } else if (type.equals(BeforeLastEventTimeFilter.TYPE)) {
            filter = new BeforeLastEventTimeFilter(WebSecurityUtils.safeParseLong(value));
        } else if (type.equals(BeforeFirstEventTimeFilter.TYPE)) {
            filter = new BeforeFirstEventTimeFilter(WebSecurityUtils.safeParseLong(value));
        } else if (type.equals(AfterLastEventTimeFilter.TYPE)) {
            filter = new AfterLastEventTimeFilter(WebSecurityUtils.safeParseLong(value));
        } else if (type.equals(AfterFirstEventTimeFilter.TYPE)) {
            filter = new AfterFirstEventTimeFilter(WebSecurityUtils.safeParseLong(value));
        } else if (type.equals(EventParmLikeFilter.TYPE)) {
            filter = new EventParmLikeFilter(value);
        } else if(type.equals(NegativeEventParmLikeFilter.TYPE)) {
            filter = new NegativeEventParmLikeFilter(value);
        }

        return filter;
    }

    /**
     * <p>getFilterString</p>
     *
     * @param filter a {@link org.opennms.web.filter.Filter} object.
     * @return a {@link java.lang.String} object.
     */
    public static String getFilterString(Filter filter) {
        if (filter == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        return filter.getDescription();
    }

    /** Constant <code>LAST_HOUR_RELATIVE_TIME=1</code> */
    public static final int LAST_HOUR_RELATIVE_TIME = 1;

    /** Constant <code>LAST_FOUR_HOURS_RELATIVE_TIME=2</code> */
    public static final int LAST_FOUR_HOURS_RELATIVE_TIME = 2;

    /** Constant <code>LAST_EIGHT_HOURS_RELATIVE_TIME=3</code> */
    public static final int LAST_EIGHT_HOURS_RELATIVE_TIME = 3;

    /** Constant <code>LAST_TWELVE_HOURS_RELATIVE_TIME=4</code> */
    public static final int LAST_TWELVE_HOURS_RELATIVE_TIME = 4;

    /** Constant <code>LAST_DAY_RELATIVE_TIME=5</code> */
    public static final int LAST_DAY_RELATIVE_TIME = 5;

    /** Constant <code>LAST_WEEK_RELATIVE_TIME=6</code> */
    public static final int LAST_WEEK_RELATIVE_TIME = 6;

    /** Constant <code>LAST_MONTH_RELATIVE_TIME=7</code> */
    public static final int LAST_MONTH_RELATIVE_TIME = 7;

    /**
     * <p>getRelativeTimeFilter</p>
     *
     * @param relativeTime a int.
     * @return a {@link org.opennms.web.filter.Filter} object.
     */
    public static Filter getRelativeTimeFilter(int relativeTime) {
        Filter filter = null;
        Calendar now = Calendar.getInstance();

        switch (relativeTime) {
        case LAST_HOUR_RELATIVE_TIME:
            now.add(Calendar.HOUR, -1);
            break;

        case LAST_FOUR_HOURS_RELATIVE_TIME:
            now.add(Calendar.HOUR, -4);
            break;

        case LAST_EIGHT_HOURS_RELATIVE_TIME:
            now.add(Calendar.HOUR, -8);
            break;

        case LAST_TWELVE_HOURS_RELATIVE_TIME:
            now.add(Calendar.HOUR, -12);
            break;

        case LAST_DAY_RELATIVE_TIME:
            now.add(Calendar.HOUR, -24);
            break;

        case LAST_WEEK_RELATIVE_TIME:
            now.add(Calendar.HOUR, -24 * 7);
            break;

        case LAST_MONTH_RELATIVE_TIME:
            now.add(Calendar.MONTH, -1);
            break;

        default:
            throw new IllegalArgumentException("Unknown relative time constant: " + relativeTime);
        }

        filter = new AfterLastEventTimeFilter(now.getTime());

        return filter;
    }
}
