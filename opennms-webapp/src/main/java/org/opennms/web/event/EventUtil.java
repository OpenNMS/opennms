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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.opennms.web.WebSecurityUtils;
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
import org.opennms.web.event.filter.AlarmIDFilter;

/**
 * <p>Abstract EventUtil class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.6.12
 */
public abstract class EventUtil extends Object {
    /** Constant <code>colors</code> */
    protected static final Map<Integer, String> colors;

    /** Constant <code>classes</code> */
    protected static final Map<Integer, String> classes;

    /** Constant <code>labels</code> */
    protected static final Map<Integer, String> labels;

    /** Constant <code>sortStyles</code> */
    protected static final Map<EventFactory.SortStyle, String> sortStyles;
    
    /** Constant <code>sortStylesString</code> */
    protected static final Map<String, EventFactory.SortStyle> sortStylesString;

    /** Constant <code>ackTypes</code> */
    protected static final Map<EventFactory.AcknowledgeType, String> ackTypes;
    
    /** Constant <code>ackTypesString</code> */
    protected static final Map<String, EventFactory.AcknowledgeType> ackTypesString;

    /** Constant <code>severities</code> */
    protected static final List<Integer> severities;

    /** Constant <code>ANY_SERVICES_OPTION="Any"</code> */
    public static final String ANY_SERVICES_OPTION = "Any";

    /** Constant <code>ANY_SEVERITIES_OPTION="Any"</code> */
    public static final String ANY_SEVERITIES_OPTION = "Any";

    /** Constant <code>ANY_RELATIVE_TIMES_OPTION="Any"</code> */
    public static final String ANY_RELATIVE_TIMES_OPTION = "Any";

    static {
        severities = new ArrayList<Integer>();
        severities.add(new Integer(Event.INDETERMINATE_SEVERITY));
        severities.add(new Integer(Event.CLEARED_SEVERITY));
        severities.add(new Integer(Event.NORMAL_SEVERITY));
        severities.add(new Integer(Event.WARNING_SEVERITY));
        severities.add(new Integer(Event.MINOR_SEVERITY));
        severities.add(new Integer(Event.MAJOR_SEVERITY));
        severities.add(new Integer(Event.CRITICAL_SEVERITY));

        classes = new HashMap<Integer, String>();
        classes.put(new Integer(Event.INDETERMINATE_SEVERITY), "sev_indeterminate");
        classes.put(new Integer(Event.CLEARED_SEVERITY), "sev_cleared");
        classes.put(new Integer(Event.NORMAL_SEVERITY), "sev_normal");
        classes.put(new Integer(Event.WARNING_SEVERITY), "sev_warning");
        classes.put(new Integer(Event.MINOR_SEVERITY), "sev_minor");
        classes.put(new Integer(Event.MAJOR_SEVERITY), "sev_major");
        classes.put(new Integer(Event.CRITICAL_SEVERITY), "sev_critical");

        colors = new HashMap<Integer, String>();
        colors.put(new Integer(Event.INDETERMINATE_SEVERITY), "lightblue");
        colors.put(new Integer(Event.CLEARED_SEVERITY), "white");
        colors.put(new Integer(Event.NORMAL_SEVERITY), "green");
        colors.put(new Integer(Event.WARNING_SEVERITY), "cyan");
        colors.put(new Integer(Event.MINOR_SEVERITY), "yellow");
        colors.put(new Integer(Event.MAJOR_SEVERITY), "orange");
        colors.put(new Integer(Event.CRITICAL_SEVERITY), "red");

        labels = new HashMap<Integer, String>();
        labels.put(new Integer(Event.INDETERMINATE_SEVERITY), "Indeterminate");
        labels.put(new Integer(Event.CLEARED_SEVERITY), "Cleared");
        labels.put(new Integer(Event.NORMAL_SEVERITY), "Normal");
        labels.put(new Integer(Event.WARNING_SEVERITY), "Warning");
        labels.put(new Integer(Event.MINOR_SEVERITY), "Minor");
        labels.put(new Integer(Event.MAJOR_SEVERITY), "Major");
        labels.put(new Integer(Event.CRITICAL_SEVERITY), "Critical");

        sortStylesString = new HashMap<String, EventFactory.SortStyle>();
        sortStylesString.put("severity", EventFactory.SortStyle.SEVERITY);
        sortStylesString.put("time", EventFactory.SortStyle.TIME);
        sortStylesString.put("node", EventFactory.SortStyle.NODE);
        sortStylesString.put("interface", EventFactory.SortStyle.INTERFACE);
        sortStylesString.put("service", EventFactory.SortStyle.SERVICE);
        sortStylesString.put("poller", EventFactory.SortStyle.POLLER);
        sortStylesString.put("id", EventFactory.SortStyle.ID);
        sortStylesString.put("rev_severity", EventFactory.SortStyle.REVERSE_SEVERITY);
        sortStylesString.put("rev_time", EventFactory.SortStyle.REVERSE_TIME);
        sortStylesString.put("rev_node", EventFactory.SortStyle.REVERSE_NODE);
        sortStylesString.put("rev_interface", EventFactory.SortStyle.REVERSE_INTERFACE);
        sortStylesString.put("rev_service", EventFactory.SortStyle.REVERSE_SERVICE);
        sortStylesString.put("rev_poller", EventFactory.SortStyle.REVERSE_POLLER);
        sortStylesString.put("rev_id", EventFactory.SortStyle.REVERSE_ID);

        sortStyles = new HashMap<EventFactory.SortStyle, String>();
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

        ackTypesString = new HashMap<String, EventFactory.AcknowledgeType>();
        ackTypesString.put("ack", EventFactory.AcknowledgeType.ACKNOWLEDGED);
        ackTypesString.put("unack", EventFactory.AcknowledgeType.UNACKNOWLEDGED);
        ackTypesString.put("both", EventFactory.AcknowledgeType.BOTH);

        ackTypes = new HashMap<EventFactory.AcknowledgeType, String>();
        ackTypes.put(EventFactory.AcknowledgeType.ACKNOWLEDGED, "ack");
        ackTypes.put(EventFactory.AcknowledgeType.UNACKNOWLEDGED, "unack");
        ackTypes.put(EventFactory.AcknowledgeType.BOTH, "both");
    }

    /**
     * <p>getSeverityList</p>
     *
     * @return a {@link java.util.List} object.
     */
    public static List getSeverityList() {
        return severities;
    }

    /**
     * <p>getSeverityId</p>
     *
     * @param index a int.
     * @return a int.
     */
    public static int getSeverityId(int index) {
        return severities.get(index).intValue();
    }

    /**
     * <p>getSeverityColor</p>
     *
     * @param severity a int.
     * @return a {@link java.lang.String} object.
     */
    public static String getSeverityColor(int severity) {
        return colors.get(new Integer(severity));
    }

    /**
     * <p>getSeverityClass</p>
     *
     * @param severity a int.
     * @return a {@link java.lang.String} object.
     */
    public static String getSeverityClass(int severity) {
        return classes.get(new Integer(severity));
    }

    /**
     * <p>getSeverityLabel</p>
     *
     * @param severity a int.
     * @return a {@link java.lang.String} object.
     */
    public static String getSeverityLabel(int severity) {
        return labels.get(new Integer(severity));
    }

    /**
     * <p>getSortStyle</p>
     *
     * @param sortStyleString a {@link java.lang.String} object.
     * @return a {@link org.opennms.web.event.EventFactory.SortStyle} object.
     */
    public static EventFactory.SortStyle getSortStyle(String sortStyleString) {
        if (sortStyleString == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        return sortStylesString.get(sortStyleString.toLowerCase());
    }

    /**
     * <p>getSortStyleString</p>
     *
     * @param sortStyle a {@link org.opennms.web.event.EventFactory.SortStyle} object.
     * @return a {@link java.lang.String} object.
     */
    public static String getSortStyleString(EventFactory.SortStyle sortStyle) {
        if (sortStyle == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        return sortStyles.get(sortStyle);
    }

    /**
     * <p>getAcknowledgeType</p>
     *
     * @param ackTypeString a {@link java.lang.String} object.
     * @return a {@link org.opennms.web.event.EventFactory.AcknowledgeType} object.
     */
    public static EventFactory.AcknowledgeType getAcknowledgeType(String ackTypeString) {
        if (ackTypeString == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        return ackTypesString.get(ackTypeString.toLowerCase());
    }

    /**
     * <p>getAcknowledgeTypeString</p>
     *
     * @param ackType a {@link org.opennms.web.event.EventFactory.AcknowledgeType} object.
     * @return a {@link java.lang.String} object.
     */
    public static String getAcknowledgeTypeString(EventFactory.AcknowledgeType ackType) {
        if (ackType == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        return ackTypes.get(ackType);
    }

    /**
     * <p>getFilter</p>
     *
     * @param filterString a {@link java.lang.String} object.
     * @return a {@link org.opennms.web.event.filter.Filter} object.
     */
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
        } else if (type.equals(IPLikeFilter.TYPE)) {
            filter = new IPLikeFilter(value);
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

    /**
     * <p>getFilterString</p>
     *
     * @param filter a {@link org.opennms.web.event.filter.Filter} object.
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
     * @return a {@link org.opennms.web.event.filter.Filter} object.
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

        filter = new AfterDateFilter(now.getTime());

        return filter;
    }
}
