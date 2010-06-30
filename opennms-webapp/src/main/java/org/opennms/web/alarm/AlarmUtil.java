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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.web.WebSecurityUtils;
import org.opennms.web.alarm.filter.AcknowledgedByFilter;
import org.opennms.web.alarm.filter.AfterLastEventTimeFilter;
import org.opennms.web.alarm.filter.BeforeLastEventTimeFilter;
import org.opennms.web.alarm.filter.AfterFirstEventTimeFilter;
import org.opennms.web.alarm.filter.BeforeFirstEventTimeFilter;
import org.opennms.web.alarm.filter.ExactUEIFilter;
import org.opennms.web.alarm.filter.Filter;
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

/**
 * <p>Abstract AlarmUtil class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.6.12
 */
public abstract class AlarmUtil extends Object {
    /** Constant <code>colors</code> */
    protected static final Map<Integer, String> colors;

    /** Constant <code>labels</code> */
    protected static final Map<Integer, String> labels;

    /** Constant <code>sortStyles</code> */
    protected static final Map<AlarmFactory.SortStyle, String> sortStyles;
    
    /** Constant <code>sortStylesString</code> */
    protected static final Map<String, AlarmFactory.SortStyle> sortStylesString;

    /** Constant <code>ackTypes</code> */
    protected static final Map<AlarmFactory.AcknowledgeType, String> ackTypes;

    /** Constant <code>ackTypesString</code> */
    protected static final Map<String, AlarmFactory.AcknowledgeType> ackTypesString;

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
        severities.add(new Integer(OnmsAlarm.INDETERMINATE_SEVERITY));
        severities.add(new Integer(OnmsAlarm.CLEARED_SEVERITY));
        severities.add(new Integer(OnmsAlarm.NORMAL_SEVERITY));
        severities.add(new Integer(OnmsAlarm.WARNING_SEVERITY));
        severities.add(new Integer(OnmsAlarm.MINOR_SEVERITY));
        severities.add(new Integer(OnmsAlarm.MAJOR_SEVERITY));
        severities.add(new Integer(OnmsAlarm.CRITICAL_SEVERITY));

        colors = new HashMap<Integer, String>();
        colors.put(new Integer(OnmsAlarm.INDETERMINATE_SEVERITY), "lightblue");
        colors.put(new Integer(OnmsAlarm.CLEARED_SEVERITY), "white");
        colors.put(new Integer(OnmsAlarm.NORMAL_SEVERITY), "green");
        colors.put(new Integer(OnmsAlarm.WARNING_SEVERITY), "cyan");
        colors.put(new Integer(OnmsAlarm.MINOR_SEVERITY), "yellow");
        colors.put(new Integer(OnmsAlarm.MAJOR_SEVERITY), "orange");
        colors.put(new Integer(OnmsAlarm.CRITICAL_SEVERITY), "red");

        labels = new HashMap<Integer, String>();
        labels.put(new Integer(OnmsAlarm.INDETERMINATE_SEVERITY), "Indeterminate");
        labels.put(new Integer(OnmsAlarm.CLEARED_SEVERITY), "Cleared");
        labels.put(new Integer(OnmsAlarm.NORMAL_SEVERITY), "Normal");
        labels.put(new Integer(OnmsAlarm.WARNING_SEVERITY), "Warning");
        labels.put(new Integer(OnmsAlarm.MINOR_SEVERITY), "Minor");
        labels.put(new Integer(OnmsAlarm.MAJOR_SEVERITY), "Major");
        labels.put(new Integer(OnmsAlarm.CRITICAL_SEVERITY), "Critical");

        sortStylesString = new HashMap<String, AlarmFactory.SortStyle>();
        sortStylesString.put("severity", AlarmFactory.SortStyle.SEVERITY);
        sortStylesString.put("lasteventtime", AlarmFactory.SortStyle.LASTEVENTTIME);
        sortStylesString.put("firsteventtime", AlarmFactory.SortStyle.FIRSTEVENTTIME);
        sortStylesString.put("node", AlarmFactory.SortStyle.NODE);
        sortStylesString.put("interface", AlarmFactory.SortStyle.INTERFACE);
        sortStylesString.put("service", AlarmFactory.SortStyle.SERVICE);
        sortStylesString.put("poller", AlarmFactory.SortStyle.POLLER);
        sortStylesString.put("id", AlarmFactory.SortStyle.ID);
        sortStylesString.put("count", AlarmFactory.SortStyle.COUNT);
        sortStylesString.put("rev_severity", AlarmFactory.SortStyle.REVERSE_SEVERITY);
        sortStylesString.put("rev_lasteventtime", AlarmFactory.SortStyle.REVERSE_LASTEVENTTIME);
        sortStylesString.put("rev_firsteventtime", AlarmFactory.SortStyle.REVERSE_FIRSTEVENTTIME);
        sortStylesString.put("rev_node", AlarmFactory.SortStyle.REVERSE_NODE);
        sortStylesString.put("rev_interface", AlarmFactory.SortStyle.REVERSE_INTERFACE);
        sortStylesString.put("rev_service", AlarmFactory.SortStyle.REVERSE_SERVICE);
        sortStylesString.put("rev_poller", AlarmFactory.SortStyle.REVERSE_POLLER);
        sortStylesString.put("rev_id", AlarmFactory.SortStyle.REVERSE_ID);
        sortStylesString.put("rev_count", AlarmFactory.SortStyle.REVERSE_COUNT);

        sortStyles = new HashMap<AlarmFactory.SortStyle, String>();
        sortStyles.put(AlarmFactory.SortStyle.SEVERITY, "severity");
        sortStyles.put(AlarmFactory.SortStyle.LASTEVENTTIME, "lasteventtime");
        sortStyles.put(AlarmFactory.SortStyle.FIRSTEVENTTIME, "firsteventtime");
        sortStyles.put(AlarmFactory.SortStyle.NODE, "node");
        sortStyles.put(AlarmFactory.SortStyle.INTERFACE, "interface");
        sortStyles.put(AlarmFactory.SortStyle.SERVICE, "service");
        sortStyles.put(AlarmFactory.SortStyle.POLLER, "poller");
        sortStyles.put(AlarmFactory.SortStyle.ID, "id");
        sortStyles.put(AlarmFactory.SortStyle.COUNT, "count");
        sortStyles.put(AlarmFactory.SortStyle.REVERSE_SEVERITY, "rev_severity");
        sortStyles.put(AlarmFactory.SortStyle.REVERSE_LASTEVENTTIME, "rev_lasteventtime");
        sortStyles.put(AlarmFactory.SortStyle.REVERSE_FIRSTEVENTTIME, "rev_firsteventtime");
        sortStyles.put(AlarmFactory.SortStyle.REVERSE_NODE, "rev_node");
        sortStyles.put(AlarmFactory.SortStyle.REVERSE_INTERFACE, "rev_interface");
        sortStyles.put(AlarmFactory.SortStyle.REVERSE_SERVICE, "rev_service");
        sortStyles.put(AlarmFactory.SortStyle.REVERSE_POLLER, "rev_poller");
        sortStyles.put(AlarmFactory.SortStyle.REVERSE_ID, "rev_id");
        sortStyles.put(AlarmFactory.SortStyle.REVERSE_COUNT, "rev_count");

        ackTypesString = new HashMap<String, AlarmFactory.AcknowledgeType>();
        ackTypesString.put("ack", AlarmFactory.AcknowledgeType.ACKNOWLEDGED);
        ackTypesString.put("unack", AlarmFactory.AcknowledgeType.UNACKNOWLEDGED);
        ackTypesString.put("both", AlarmFactory.AcknowledgeType.BOTH);

        ackTypes = new HashMap<AlarmFactory.AcknowledgeType, String>();
        ackTypes.put(AlarmFactory.AcknowledgeType.ACKNOWLEDGED, "ack");
        ackTypes.put(AlarmFactory.AcknowledgeType.UNACKNOWLEDGED, "unack");
        ackTypes.put(AlarmFactory.AcknowledgeType.BOTH, "both");
    }

    /**
     * <p>getSeverityList</p>
     *
     * @return a {@link java.util.List} object.
     */
    public static List<Integer> getSeverityList() {
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
     * @return a {@link org.opennms.web.alarm.AlarmFactory.SortStyle} object.
     */
    public static AlarmFactory.SortStyle getSortStyle(String sortStyleString) {
        if (sortStyleString == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        return sortStylesString.get(sortStyleString.toLowerCase());
    }

    /**
     * <p>getSortStyleString</p>
     *
     * @param sortStyle a {@link org.opennms.web.alarm.AlarmFactory.SortStyle} object.
     * @return a {@link java.lang.String} object.
     */
    public static String getSortStyleString(AlarmFactory.SortStyle sortStyle) {
        if (sortStyle == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        return sortStyles.get(sortStyle);
    }

    /**
     * <p>getAcknowledgeType</p>
     *
     * @param ackTypeString a {@link java.lang.String} object.
     * @return a {@link org.opennms.web.alarm.AlarmFactory.AcknowledgeType} object.
     */
    public static AlarmFactory.AcknowledgeType getAcknowledgeType(String ackTypeString) {
        if (ackTypeString == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        return ackTypesString.get(ackTypeString.toLowerCase());
    }

    /**
     * <p>getAcknowledgeTypeString</p>
     *
     * @param ackType a {@link org.opennms.web.alarm.AlarmFactory.AcknowledgeType} object.
     * @return a {@link java.lang.String} object.
     */
    public static String getAcknowledgeTypeString(AlarmFactory.AcknowledgeType ackType) {
        if (ackType == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        return ackTypes.get(ackType);
    }

    /**
     * <p>getFilter</p>
     *
     * @param filterString a {@link java.lang.String} object.
     * @return a {@link org.opennms.web.alarm.filter.Filter} object.
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

    /**
     * <p>getFilterString</p>
     *
     * @param filter a {@link org.opennms.web.alarm.filter.Filter} object.
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
     * @return a {@link org.opennms.web.alarm.filter.Filter} object.
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
