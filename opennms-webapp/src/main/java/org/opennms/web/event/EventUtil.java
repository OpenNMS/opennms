/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.web.event;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.servlet.ServletContext;

import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.web.event.filter.AcknowledgedByFilter;
import org.opennms.web.event.filter.AfterDateFilter;
import org.opennms.web.event.filter.AlarmIDFilter;
import org.opennms.web.event.filter.BeforeDateFilter;
import org.opennms.web.event.filter.EventIdFilter;
import org.opennms.web.event.filter.EventTextFilter;
import org.opennms.web.event.filter.ExactUEIFilter;
import org.opennms.web.event.filter.IPAddrLikeFilter;
import org.opennms.web.event.filter.IfIndexFilter;
import org.opennms.web.event.filter.InterfaceFilter;
import org.opennms.web.event.filter.LocationFilter;
import org.opennms.web.event.filter.LogMessageMatchesAnyFilter;
import org.opennms.web.event.filter.NegativeAcknowledgedByFilter;
import org.opennms.web.event.filter.NegativeEventTextFilter;
import org.opennms.web.event.filter.NegativeExactUEIFilter;
import org.opennms.web.event.filter.NegativeIPAddrLikeFilter;
import org.opennms.web.event.filter.NegativeInterfaceFilter;
import org.opennms.web.event.filter.NegativeLocationFilter;
import org.opennms.web.event.filter.NegativeNodeFilter;
import org.opennms.web.event.filter.NegativeNodeLocationFilter;
import org.opennms.web.event.filter.NegativeNodeNameLikeFilter;
import org.opennms.web.event.filter.NegativePartialUEIFilter;
import org.opennms.web.event.filter.NegativeServiceFilter;
import org.opennms.web.event.filter.NegativeSeverityFilter;
import org.opennms.web.event.filter.NegativeSystemIdFilter;
import org.opennms.web.event.filter.NodeFilter;
import org.opennms.web.event.filter.NodeLocationFilter;
import org.opennms.web.event.filter.NodeNameLikeFilter;
import org.opennms.web.event.filter.PartialUEIFilter;
import org.opennms.web.event.filter.ServiceFilter;
import org.opennms.web.event.filter.ServiceOrFilter;
import org.opennms.web.event.filter.SeverityFilter;
import org.opennms.web.event.filter.SeverityOrFilter;
import org.opennms.web.event.filter.SystemIdFilter;
import org.opennms.web.filter.ConditionalFilter;
import org.opennms.web.filter.Filter;
import org.opennms.web.utils.filter.CheckboxFilterUtils;
import org.opennms.web.utils.filter.FilterTokenizeUtils;

/**
 * <p>Abstract EventUtil class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public abstract class EventUtil {

    /** Constant <code>NEGATION_PREFIX_SYMBOL="!"</code> */
    private static final String NEGATION_PREFIX_SYMBOL = "!";

    /** Constant <code>ARRAY_DELIMITER=","</code> */
    private static final String ARRAY_DELIMITER = ",";

    /** Constant <code>ANY_OPTION="Any"</code> */
    public static final String ANY_OPTION = "Any";

    /**
     * Checks if a string represents an integer.
     *
     * @param str The string to check.
     * @return True if the string is an integer, false otherwise.
     */
    public static boolean isInteger(String str) {
        return str != null && !str.isEmpty() && str.chars().allMatch(Character::isDigit);
    }

    /**
     * <p>getFilter</p>
     *
     * @param filterString a {@link java.lang.String} object.
     * @return a org$opennms$web$filter$Filter object.
     */
    public static Filter getFilter(String filterString, ServletContext servletContext) {
        if (filterString == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Filter filter = null;

        String[] tokenizedFilterString = FilterTokenizeUtils.tokenizeFilterString(filterString);
        String type = tokenizedFilterString[0];
        String value = tokenizedFilterString[1];

        if (type.equals(SeverityFilter.TYPE)) {
            String[] ids = value.split(ARRAY_DELIMITER);
            OnmsSeverity[] severities = new OnmsSeverity[ids.length];
            for (int index = 0; index < ids.length; index++) {
                severities[index] = OnmsSeverity.get(WebSecurityUtils.safeParseInt(ids[index]));
            }
            filter = new SeverityOrFilter(severities);
        } else if (type.equals(NodeFilter.TYPE)) {
            filter = new NodeFilter(WebSecurityUtils.safeParseInt(value), servletContext);
        } else if (type.equals(NodeNameLikeFilter.TYPE)) {
            if (value.startsWith(NEGATION_PREFIX_SYMBOL)) {
                filter = new NegativeNodeNameLikeFilter(value.substring(1));
            } else {
                filter = new NodeNameLikeFilter(value);
            }
        } else if (type.equals(InterfaceFilter.TYPE)) {
            filter = new InterfaceFilter(value);
        } else if (type.equals(ConditionalFilter.TYPE)) {
            String cleanedValue = value.substring(1, value.length() - 1); // Remove surrounding brackets
            String[] ids = cleanedValue.split(ARRAY_DELIMITER);
            boolean hasInteger = false;

            List<Integer> serviceIdsList = new ArrayList<>();
            List<OnmsSeverity> severitiesList = new ArrayList<>();

            for (String id : ids) {
                String trimmedId = id.trim();
                if (isInteger(trimmedId)) {
                    hasInteger = true;
                    serviceIdsList.add(WebSecurityUtils.safeParseInt(trimmedId));
                } else {
                    severitiesList.add(OnmsSeverity.get(trimmedId));
                }
            }
            if (hasInteger) {
                Integer[] serviceIds = serviceIdsList.toArray(new Integer[0]);
                filter = new ServiceOrFilter(serviceIds, servletContext);
            } else {
                OnmsSeverity[] severities = severitiesList.toArray(new OnmsSeverity[0]);
                filter = new SeverityOrFilter(severities);
            }
        } else if (type.equals(ServiceFilter.TYPE)) {
            String[] ids = value.split(ARRAY_DELIMITER);
            Integer[] serviceIds = new Integer[ids.length];
            for (int index = 0; index < ids.length; index++) {
                serviceIds[index] = WebSecurityUtils.safeParseInt(ids[index]);
            }
            filter = new ServiceOrFilter(serviceIds, servletContext);
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
            filter = new NegativeNodeFilter(WebSecurityUtils.safeParseInt(value), servletContext);
        } else if (type.equals(NegativeInterfaceFilter.TYPE)) {
            filter = new NegativeInterfaceFilter(value);
        } else if (type.equals(NegativeServiceFilter.TYPE)) {
            filter = new NegativeServiceFilter(WebSecurityUtils.safeParseInt(value), servletContext);
        } else if (type.equals(NegativePartialUEIFilter.TYPE)) {
            filter = new NegativePartialUEIFilter(value);
        } else if (type.equals(NegativeExactUEIFilter.TYPE)) {
            filter = new NegativeExactUEIFilter(value);
        } else if (type.equals(NegativeAcknowledgedByFilter.TYPE)) {
            filter = new NegativeAcknowledgedByFilter(value);
        } else if (type.equals(EventIdFilter.TYPE)) {
            filter = new EventIdFilter(WebSecurityUtils.safeParseInt(value));
        } else if (type.equals(IPAddrLikeFilter.TYPE)) {
            if (value.startsWith(NEGATION_PREFIX_SYMBOL)) {
                filter = new NegativeIPAddrLikeFilter(value.substring(1));
            } else {
                filter = new IPAddrLikeFilter(value);
            }
        } else if (type.equals(EventTextFilter.TYPE)) {
            if (value.startsWith(NEGATION_PREFIX_SYMBOL)) {
                filter = new NegativeEventTextFilter(value.substring(1));
            } else {
                filter = new EventTextFilter(value);
            }
        } else if (type.equals(LogMessageMatchesAnyFilter.TYPE)) {
            filter = new LogMessageMatchesAnyFilter(value);
        } else if (type.equals(BeforeDateFilter.TYPE)) {
            filter = new BeforeDateFilter(WebSecurityUtils.safeParseLong(value));
        } else if (type.equals(AfterDateFilter.TYPE)) {
            filter = new AfterDateFilter(WebSecurityUtils.safeParseLong(value));
        } else if (type.equals(AlarmIDFilter.TYPE)) {
            filter = new AlarmIDFilter(WebSecurityUtils.safeParseInt(value));
        } else if (type.equals(LocationFilter.TYPE)) {
            filter = new LocationFilter(WebSecurityUtils.sanitizeString(value));
        } else if (type.equals(SystemIdFilter.TYPE)) {
            if (!value.equalsIgnoreCase(ANY_OPTION)) {
                filter = new SystemIdFilter(WebSecurityUtils.sanitizeString(value));
            }
        } else if (type.equals(NegativeLocationFilter.TYPE)) {
            filter = new NegativeLocationFilter(WebSecurityUtils.sanitizeString(value));
        } else if (type.equals(NegativeSystemIdFilter.TYPE)) {
            filter = new NegativeSystemIdFilter(WebSecurityUtils.sanitizeString(value));
        } else if (type.equals(NodeLocationFilter.TYPE)) {
            if (!value.equalsIgnoreCase(ANY_OPTION)) {
                filter = new NodeLocationFilter(WebSecurityUtils.sanitizeString(value));
            }
        } else if (type.equals(NegativeNodeLocationFilter.TYPE)) {
            filter = new NegativeNodeLocationFilter(WebSecurityUtils.sanitizeString(value));
        }

        return filter;
    }

    /**
     * <p>getFilterString</p>
     *
     * @param filter a org$opennms$web$filter$Filter object.
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
     * @return a org$opennms$web$filter$Filter object.
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

	public static List<Filter> getFilterList(String[] filterStrings, ServletContext servletContext) {
        filterStrings = CheckboxFilterUtils.handleCheckboxDuplication(filterStrings);

		List<Filter> filterList = new ArrayList<>();
        if (filterStrings != null) {
            for (String filterString : filterStrings) {
                Filter filter = EventUtil.getFilter(filterString, servletContext);
                if (filter != null) {
                    filterList.add(filter);
                }
            }
        }
		return filterList;
	}
}
