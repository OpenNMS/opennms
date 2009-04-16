//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2007 Jul 24: Java 5 generics. - dj@opennms.org
// 2005 Sep 30: Added getSeverityClass and supporting code for
//              CSS conversion. -- DJ Gregor
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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

package org.opennms.web.event;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.web.WebSecurityUtils;
import org.opennms.web.event.filter.AcknowledgedByFilter;
import org.opennms.web.event.filter.AfterDateFilter;
import org.opennms.web.event.filter.AlarmIDFilter;
import org.opennms.web.event.filter.BeforeDateFilter;
import org.opennms.web.event.filter.ExactUEIFilter;
import org.opennms.web.event.filter.IPAddrLikeFilter;
import org.opennms.web.event.filter.IfIndexFilter;
import org.opennms.web.event.filter.InterfaceFilter;
import org.opennms.web.event.filter.LogMessageMatchesAnyFilter;
import org.opennms.web.event.filter.LogMessageSubstringFilter;
import org.opennms.web.event.filter.NegativeAcknowledgedByFilter;
import org.opennms.web.event.filter.NegativeExactUEIFilter;
import org.opennms.web.event.filter.NegativeInterfaceFilter;
import org.opennms.web.event.filter.NegativeNodeFilter;
import org.opennms.web.event.filter.NegativePartialUEIFilter;
import org.opennms.web.event.filter.NegativeServiceFilter;
import org.opennms.web.event.filter.NegativeSeverityFilter;
import org.opennms.web.event.filter.NodeFilter;
import org.opennms.web.event.filter.NodeNameLikeFilter;
import org.opennms.web.event.filter.PartialUEIFilter;
import org.opennms.web.event.filter.ServiceFilter;
import org.opennms.web.event.filter.SeverityFilter;
import org.opennms.web.filter.Filter;

public abstract class EventUtil extends Object {
    protected static final Map<OnmsSeverity, String> classes;

    protected static final Map<SortStyle, String> sortStyles;
    
    protected static final Map<String, SortStyle> sortStylesString;

    protected static final Map<AcknowledgeType, String> ackTypes;
    
    protected static final Map<String, AcknowledgeType> ackTypesString;

    public static final String ANY_SERVICES_OPTION = "Any";

    public static final String ANY_SEVERITIES_OPTION = "Any";

    public static final String ANY_RELATIVE_TIMES_OPTION = "Any";

    static {
        classes = new HashMap<OnmsSeverity, String>();
        classes.put(OnmsSeverity.INDETERMINATE, "sev_indeterminate");
        classes.put(OnmsSeverity.CLEARED, "sev_cleared");
        classes.put(OnmsSeverity.NORMAL, "sev_normal");
        classes.put(OnmsSeverity.WARNING, "sev_warning");
        classes.put(OnmsSeverity.MINOR, "sev_minor");
        classes.put(OnmsSeverity.MAJOR, "sev_major");
        classes.put(OnmsSeverity.CRITICAL, "sev_critical");

        sortStylesString = new HashMap<String, SortStyle>();
        sortStylesString.put("severity", SortStyle.SEVERITY);
        sortStylesString.put("time", SortStyle.TIME);
        sortStylesString.put("node", SortStyle.NODE);
        sortStylesString.put("interface", SortStyle.INTERFACE);
        sortStylesString.put("service", SortStyle.SERVICE);
        sortStylesString.put("poller", SortStyle.POLLER);
        sortStylesString.put("id", SortStyle.ID);
        sortStylesString.put("rev_severity", SortStyle.REVERSE_SEVERITY);
        sortStylesString.put("rev_time", SortStyle.REVERSE_TIME);
        sortStylesString.put("rev_node", SortStyle.REVERSE_NODE);
        sortStylesString.put("rev_interface", SortStyle.REVERSE_INTERFACE);
        sortStylesString.put("rev_service", SortStyle.REVERSE_SERVICE);
        sortStylesString.put("rev_poller", SortStyle.REVERSE_POLLER);
        sortStylesString.put("rev_id", SortStyle.REVERSE_ID);

        sortStyles = new HashMap<SortStyle, String>();
        sortStyles.put(SortStyle.SEVERITY, "severity");
        sortStyles.put(SortStyle.TIME, "time");
        sortStyles.put(SortStyle.NODE, "node");
        sortStyles.put(SortStyle.INTERFACE, "interface");
        sortStyles.put(SortStyle.SERVICE, "service");
        sortStyles.put(SortStyle.POLLER, "poller");
        sortStyles.put(SortStyle.ID, "id");
        sortStyles.put(SortStyle.REVERSE_SEVERITY, "rev_severity");
        sortStyles.put(SortStyle.REVERSE_TIME, "rev_time");
        sortStyles.put(SortStyle.REVERSE_NODE, "rev_node");
        sortStyles.put(SortStyle.REVERSE_INTERFACE, "rev_interface");
        sortStyles.put(SortStyle.REVERSE_SERVICE, "rev_service");
        sortStyles.put(SortStyle.REVERSE_POLLER, "rev_poller");
        sortStyles.put(SortStyle.REVERSE_ID, "rev_id");

        ackTypesString = new HashMap<String, AcknowledgeType>();
        ackTypesString.put("ack", AcknowledgeType.ACKNOWLEDGED);
        ackTypesString.put("unack", AcknowledgeType.UNACKNOWLEDGED);
        ackTypesString.put("both", AcknowledgeType.BOTH);

        ackTypes = new HashMap<AcknowledgeType, String>();
        ackTypes.put(AcknowledgeType.ACKNOWLEDGED, "ack");
        ackTypes.put(AcknowledgeType.UNACKNOWLEDGED, "unack");
        ackTypes.put(AcknowledgeType.BOTH, "both");
    }

    public static String getSeverityColor(int severity) {
        return OnmsSeverity.get(severity).getColor();
    }

    public static String getSeverityClass(int severity) {
        return classes.get(new Integer(severity));
    }

    public static SortStyle getSortStyle(String sortStyleString) {
        if (sortStyleString == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        return sortStylesString.get(sortStyleString.toLowerCase());
    }

    public static String getSortStyleString(SortStyle sortStyle) {
        if (sortStyle == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        return sortStyles.get(sortStyle);
    }

    public static AcknowledgeType getAcknowledgeType(String ackTypeString) {
        if (ackTypeString == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        return ackTypesString.get(ackTypeString.toLowerCase());
    }

    public static String getAcknowledgeTypeString(AcknowledgeType ackType) {
        if (ackType == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        return ackTypes.get(ackType);
    }

    public static Filter getFilter(String filterString) {
        if (filterString == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Filter filter = null;

        StringTokenizer tokens = new StringTokenizer(filterString, "=");
        String type = tokens.nextToken();
        String value = tokens.nextToken();

        if (type.equals(SeverityFilter.TYPE)) {
            filter = new SeverityFilter(WebSecurityUtils.safeParseInt(value));
        } else if (type.equals(NodeFilter.TYPE)) {
            filter = new NodeFilter(WebSecurityUtils.safeParseInt(value));
        } else if (type.equals(NodeNameLikeFilter.TYPE)) {
            filter = new NodeNameLikeFilter(value);
        } else if (type.equals(InterfaceFilter.TYPE)) {
            filter = new InterfaceFilter(value);
        } else if (type.equals(ServiceFilter.TYPE)) {
            filter = new ServiceFilter(WebSecurityUtils.safeParseInt(value));
        } else if (type.equals(IfIndexFilter.TYPE)) {
            filter = new IfIndexFilter(WebSecurityUtils.safeParseInt(value));
        } else if (type.equals(PartialUEIFilter.TYPE)) {
            filter = new PartialUEIFilter(value);
        } else if (type.equals(ExactUEIFilter.TYPE)) {
            filter = new ExactUEIFilter(value);
        } else if (type.equals(AcknowledgedByFilter.TYPE)) {
            filter = new AcknowledgedByFilter(value);
        } else if (type.equals(NegativeSeverityFilter.TYPE)) {
            filter = new NegativeSeverityFilter(WebSecurityUtils.safeParseInt(value));
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
        } else if (type.equals(IPAddrLikeFilter.TYPE)) {
            filter = new IPAddrLikeFilter(value);
        } else if (type.equals(LogMessageSubstringFilter.TYPE)) {
            filter = new LogMessageSubstringFilter(value);
        } else if (type.equals(LogMessageMatchesAnyFilter.TYPE)) {
            filter = new LogMessageMatchesAnyFilter(value);
        } else if (type.equals(BeforeDateFilter.TYPE)) {
            filter = new BeforeDateFilter(WebSecurityUtils.safeParseLong(value));
        } else if (type.equals(AfterDateFilter.TYPE)) {
            filter = new AfterDateFilter(WebSecurityUtils.safeParseLong(value));
        } else if (type.equals(AlarmIDFilter.TYPE)) {
            filter = new AlarmIDFilter(WebSecurityUtils.safeParseInt(value));
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

        filter = new AfterDateFilter(now.getTime());

        return filter;
    }
}
