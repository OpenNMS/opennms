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
import java.util.List;
import java.util.StringTokenizer;

import org.opennms.web.event.filter.AcknowledgedByFilter;
import org.opennms.web.event.filter.AfterDateFilter;
import org.opennms.web.event.filter.BeforeDateFilter;
import org.opennms.web.event.filter.ExactUEIFilter;
import org.opennms.web.event.filter.Filter;
import org.opennms.web.event.filter.IPLikeFilter;
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

public abstract class EventUtil extends Object {
    protected static final HashMap colors;

    protected static final HashMap labels;

    protected static final HashMap sortStyles;

    protected static final HashMap ackTypes;

    protected static final List severities;

    public static final String ANY_SERVICES_OPTION = "Any";

    public static final String ANY_SEVERITIES_OPTION = "Any";

    public static final String ANY_RELATIVE_TIMES_OPTION = "Any";

    static {
        severities = new java.util.ArrayList();
        severities.add(new Integer(Event.INDETERMINATE_SEVERITY));
        severities.add(new Integer(Event.CLEARED_SEVERITY));
        severities.add(new Integer(Event.NORMAL_SEVERITY));
        severities.add(new Integer(Event.WARNING_SEVERITY));
        severities.add(new Integer(Event.MINOR_SEVERITY));
        severities.add(new Integer(Event.MAJOR_SEVERITY));
        severities.add(new Integer(Event.CRITICAL_SEVERITY));

        colors = new java.util.HashMap();
        colors.put(new Integer(Event.INDETERMINATE_SEVERITY), "lightblue");
        colors.put(new Integer(Event.CLEARED_SEVERITY), "white");
        colors.put(new Integer(Event.NORMAL_SEVERITY), "green");
        colors.put(new Integer(Event.WARNING_SEVERITY), "cyan");
        colors.put(new Integer(Event.MINOR_SEVERITY), "yellow");
        colors.put(new Integer(Event.MAJOR_SEVERITY), "orange");
        colors.put(new Integer(Event.CRITICAL_SEVERITY), "red");

        labels = new java.util.HashMap();
        labels.put(new Integer(Event.INDETERMINATE_SEVERITY), "Indeterminate");
        labels.put(new Integer(Event.CLEARED_SEVERITY), "Cleared");
        labels.put(new Integer(Event.NORMAL_SEVERITY), "Normal");
        labels.put(new Integer(Event.WARNING_SEVERITY), "Warning");
        labels.put(new Integer(Event.MINOR_SEVERITY), "Minor");
        labels.put(new Integer(Event.MAJOR_SEVERITY), "Major");
        labels.put(new Integer(Event.CRITICAL_SEVERITY), "Critical");

        sortStyles = new java.util.HashMap();
        sortStyles.put("severity", EventFactory.SortStyle.SEVERITY);
        sortStyles.put("time", EventFactory.SortStyle.TIME);
        sortStyles.put("node", EventFactory.SortStyle.NODE);
        sortStyles.put("interface", EventFactory.SortStyle.INTERFACE);
        sortStyles.put("service", EventFactory.SortStyle.SERVICE);
        sortStyles.put("poller", EventFactory.SortStyle.POLLER);
        sortStyles.put("id", EventFactory.SortStyle.ID);
        sortStyles.put("rev_severity", EventFactory.SortStyle.REVERSE_SEVERITY);
        sortStyles.put("rev_time", EventFactory.SortStyle.REVERSE_TIME);
        sortStyles.put("rev_node", EventFactory.SortStyle.REVERSE_NODE);
        sortStyles.put("rev_interface", EventFactory.SortStyle.REVERSE_INTERFACE);
        sortStyles.put("rev_service", EventFactory.SortStyle.REVERSE_SERVICE);
        sortStyles.put("rev_poller", EventFactory.SortStyle.REVERSE_POLLER);
        sortStyles.put("rev_id", EventFactory.SortStyle.REVERSE_ID);

        sortStyles.put(EventFactory.SortStyle.SEVERITY, "severity");
        sortStyles.put(EventFactory.SortStyle.TIME, "time");
        sortStyles.put(EventFactory.SortStyle.NODE, "node");
        sortStyles.put(EventFactory.SortStyle.INTERFACE, "interface");
        sortStyles.put(EventFactory.SortStyle.SERVICE, "service");
        sortStyles.put(EventFactory.SortStyle.POLLER, "poller");
        sortStyles.put(EventFactory.SortStyle.ID, "id");
        sortStyles.put(EventFactory.SortStyle.REVERSE_SEVERITY, "rev_severity");
        sortStyles.put(EventFactory.SortStyle.REVERSE_TIME, "rev_time");
        sortStyles.put(EventFactory.SortStyle.REVERSE_NODE, "rev_node");
        sortStyles.put(EventFactory.SortStyle.REVERSE_INTERFACE, "rev_interface");
        sortStyles.put(EventFactory.SortStyle.REVERSE_SERVICE, "rev_service");
        sortStyles.put(EventFactory.SortStyle.REVERSE_POLLER, "rev_poller");
        sortStyles.put(EventFactory.SortStyle.REVERSE_ID, "rev_id");

        ackTypes = new java.util.HashMap();
        ackTypes.put("ack", EventFactory.AcknowledgeType.ACKNOWLEDGED);
        ackTypes.put("unack", EventFactory.AcknowledgeType.UNACKNOWLEDGED);
        ackTypes.put("both", EventFactory.AcknowledgeType.BOTH);
        ackTypes.put(EventFactory.AcknowledgeType.ACKNOWLEDGED, "ack");
        ackTypes.put(EventFactory.AcknowledgeType.UNACKNOWLEDGED, "unack");
        ackTypes.put(EventFactory.AcknowledgeType.BOTH, "both");
    }

    public static List getSeverityList() {
        return severities;
    }

    public static int getSeverityId(int index) {
        return ((Integer) severities.get(index)).intValue();
    }

    public static String getSeverityColor(int severity) {
        return ((String) colors.get(new Integer(severity)));
    }

    public static String getSeverityLabel(int severity) {
        return ((String) labels.get(new Integer(severity)));
    }

    public static EventFactory.SortStyle getSortStyle(String sortStyleString) {
        if (sortStyleString == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        return (EventFactory.SortStyle) sortStyles.get(sortStyleString.toLowerCase());
    }

    public static String getSortStyleString(EventFactory.SortStyle sortStyle) {
        if (sortStyle == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        return (String) sortStyles.get(sortStyle);
    }

    public static EventFactory.AcknowledgeType getAcknowledgeType(String ackTypeString) {
        if (ackTypeString == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        return (EventFactory.AcknowledgeType) ackTypes.get(ackTypeString.toLowerCase());
    }

    public static String getAcknowledgeTypeString(EventFactory.AcknowledgeType ackType) {
        if (ackType == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        return (String) ackTypes.get(ackType);
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
            filter = new SeverityFilter(Integer.parseInt(value));
        } else if (type.equals(NodeFilter.TYPE)) {
            filter = new NodeFilter(Integer.parseInt(value));
        } else if (type.equals(NodeNameLikeFilter.TYPE)) {
            filter = new NodeNameLikeFilter(value);
        } else if (type.equals(InterfaceFilter.TYPE)) {
            filter = new InterfaceFilter(value);
        } else if (type.equals(ServiceFilter.TYPE)) {
            filter = new ServiceFilter(Integer.parseInt(value));
        } else if (type.equals(PartialUEIFilter.TYPE)) {
            filter = new PartialUEIFilter(value);
        } else if (type.equals(ExactUEIFilter.TYPE)) {
            filter = new ExactUEIFilter(value);
        } else if (type.equals(AcknowledgedByFilter.TYPE)) {
            filter = new AcknowledgedByFilter(value);
        } else if (type.equals(NegativeSeverityFilter.TYPE)) {
            filter = new NegativeSeverityFilter(Integer.parseInt(value));
        } else if (type.equals(NegativeNodeFilter.TYPE)) {
            filter = new NegativeNodeFilter(Integer.parseInt(value));
        } else if (type.equals(NegativeInterfaceFilter.TYPE)) {
            filter = new NegativeInterfaceFilter(value);
        } else if (type.equals(NegativeServiceFilter.TYPE)) {
            filter = new NegativeServiceFilter(Integer.parseInt(value));
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
        } else if (type.equals(BeforeDateFilter.TYPE)) {
            filter = new BeforeDateFilter(Long.parseLong(value));
        } else if (type.equals(AfterDateFilter.TYPE)) {
            filter = new AfterDateFilter(Long.parseLong(value));
        }

        return (filter);
    }

    public static String getFilterString(Filter filter) {
        if (filter == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        return (filter.getDescription());
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
