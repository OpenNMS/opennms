//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2008 Oct 04: Severity -> OnmsSeverity name change and some method name changes. - dj@opennms.org
// 2008 Sep 27: Move code related to new enum classes into those classes
//              and use new constructor signatures for severity filters. - dj@opennms.org
// 2007 Jul 24: Java 5 generics. - dj@opennms.org
// 2005 Apr 18: This file created from EventUtil.java
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//

package org.opennms.web.alarm;

import java.util.Calendar;
import java.util.StringTokenizer;

import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.web.WebSecurityUtils;
import org.opennms.web.alarm.filter.AcknowledgedByFilter;
import org.opennms.web.alarm.filter.AfterFirstEventTimeFilter;
import org.opennms.web.alarm.filter.AfterLastEventTimeFilter;
import org.opennms.web.alarm.filter.BeforeFirstEventTimeFilter;
import org.opennms.web.alarm.filter.BeforeLastEventTimeFilter;
import org.opennms.web.alarm.filter.ExactUEIFilter;
import org.opennms.web.alarm.filter.IPLikeFilter;
import org.opennms.web.alarm.filter.InterfaceFilter;
import org.opennms.web.alarm.filter.LogMessageMatchesAnyFilter;
import org.opennms.web.alarm.filter.LogMessageSubstringFilter;
import org.opennms.web.alarm.filter.NegativeAcknowledgedByFilter;
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

public abstract class AlarmUtil extends Object {
    public static final String ANY_SERVICES_OPTION = "Any";

    public static final String ANY_SEVERITIES_OPTION = "Any";

    public static final String ANY_RELATIVE_TIMES_OPTION = "Any";

    public static Filter getFilter(String filterString) {
        if (filterString == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Filter filter = null;

        StringTokenizer tokens = new StringTokenizer(filterString, "=");
        String type = tokens.nextToken();
        String value = tokens.nextToken();

        if (type.equals(SeverityFilter.TYPE)) {
            filter = new SeverityFilter(OnmsSeverity.get(WebSecurityUtils.safeParseInt(value)));
        } else if (type.equals(NodeFilter.TYPE)) {
            filter = new NodeFilter(WebSecurityUtils.safeParseInt(value));
        } else if (type.equals(NodeNameLikeFilter.TYPE)) {
            filter = new NodeNameLikeFilter(value);
        } else if (type.equals(InterfaceFilter.TYPE)) {
            filter = new InterfaceFilter(value);
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
            filter = new NegativeNodeFilter(WebSecurityUtils.safeParseInt(value));
        } else if (type.equals(NegativeInterfaceFilter.TYPE)) {
            filter = new NegativeInterfaceFilter(value);
        } else if (type.equals(NegativeServiceFilter.TYPE)) {
            filter = new NegativeServiceFilter(WebSecurityUtils.safeParseInt(value));
        } else if (type.equals(NegativePartialUEIFilter.TYPE)) {
            filter = new NegativePartialUEIFilter(value);
        } else if (type.equals(NegativeExactUEIFilter.TYPE)) {
            filter = new NegativeExactUEIFilter(value);
        } else if (type.equals(NegativeAcknowledgedByFilter.TYPE)) {
            filter = new NegativeAcknowledgedByFilter(value);
        } else if (type.equals(IPLikeFilter.TYPE)) {
            filter = new IPLikeFilter(value);
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
        }

        return filter;
    }

    public static String getFilterString(Filter filter) {
        if (filter == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        return filter.getDescription();
    }

    public static final int LAST_HOUR_RELATIVE_TIME = 1;

    public static final int LAST_FOUR_HOURS_RELATIVE_TIME = 2;

    public static final int LAST_EIGHT_HOURS_RELATIVE_TIME = 3;

    public static final int LAST_TWELVE_HOURS_RELATIVE_TIME = 4;

    public static final int LAST_DAY_RELATIVE_TIME = 5;

    public static final int LAST_WEEK_RELATIVE_TIME = 6;

    public static final int LAST_MONTH_RELATIVE_TIME = 7;

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
