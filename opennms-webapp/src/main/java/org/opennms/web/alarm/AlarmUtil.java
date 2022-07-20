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

package org.opennms.web.alarm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.servlet.ServletContext;

import org.apache.commons.lang.ArrayUtils;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.StringUtils;
import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.web.alarm.filter.AcknowledgedByFilter;
import org.opennms.web.alarm.filter.AfterFirstEventTimeFilter;
import org.opennms.web.alarm.filter.AfterLastEventTimeFilter;
import org.opennms.web.alarm.filter.AlarmCriteria;
import org.opennms.web.alarm.filter.AlarmCriteria.AlarmCriteriaVisitor;
import org.opennms.web.alarm.filter.AlarmTextFilter;
import org.opennms.web.alarm.filter.BeforeFirstEventTimeFilter;
import org.opennms.web.alarm.filter.BeforeLastEventTimeFilter;
import org.opennms.web.alarm.filter.CategoryFilter;
import org.opennms.web.alarm.filter.EventParmLikeFilter;
import org.opennms.web.alarm.filter.ExactUEIFilter;
import org.opennms.web.alarm.filter.IPAddrLikeFilter;
import org.opennms.web.alarm.filter.InterfaceFilter;
import org.opennms.web.alarm.filter.LocationFilter;
import org.opennms.web.alarm.filter.LogMessageMatchesAnyFilter;
import org.opennms.web.alarm.filter.NegativeAcknowledgedByFilter;
import org.opennms.web.alarm.filter.NegativeAlarmTextFilter;
import org.opennms.web.alarm.filter.NegativeCategoryFilter;
import org.opennms.web.alarm.filter.NegativeEventParmLikeFilter;
import org.opennms.web.alarm.filter.NegativeExactUEIFilter;
import org.opennms.web.alarm.filter.NegativeIPAddrLikeFilter;
import org.opennms.web.alarm.filter.NegativeInterfaceFilter;
import org.opennms.web.alarm.filter.NegativeLocationFilter;
import org.opennms.web.alarm.filter.NegativeNodeFilter;
import org.opennms.web.alarm.filter.NegativeNodeLocationFilter;
import org.opennms.web.alarm.filter.NegativeNodeNameLikeFilter;
import org.opennms.web.alarm.filter.NegativePartialUEIFilter;
import org.opennms.web.alarm.filter.NegativeServiceFilter;
import org.opennms.web.alarm.filter.NegativeSeverityFilter;
import org.opennms.web.alarm.filter.NodeFilter;
import org.opennms.web.alarm.filter.NodeLocationFilter;
import org.opennms.web.alarm.filter.NodeNameLikeFilter;
import org.opennms.web.alarm.filter.PartialUEIFilter;
import org.opennms.web.alarm.filter.ServiceFilter;
import org.opennms.web.alarm.filter.ServiceOrFilter;
import org.opennms.web.alarm.filter.SeverityFilter;
import org.opennms.web.alarm.filter.SeverityOrFilter;
import org.opennms.web.alarm.filter.SituationFilter;
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

    /** Constant <code>POSITIVE_CHECKBOX_VALUE="ON"</code> */
    public static final String POSITIVE_CHECKBOX_VALUE = "on";

    /** Constant <code>NEGATION_PREFIX_SYMBOL="!"</code> */
    public static final String NEGATION_PREFIX_SYMBOL = "!";

    /** Constant <code>ARRAY_DELIMITER=","</code> */
    public static final String ARRAY_DELIMITER = ",";

    /**
     * Constant <code>MULTI_CHECKBOX_PATTERN="[a-zA-Z]+-\d+=1"</code>
     * Matches filter strings such as the format "severity-4=1" or "service-1=1" or similar
     */
    private static final Pattern MULTI_CHECKBOX_PATTERN = Pattern.compile("[a-zA-Z]+-\\d+=1");

    public static OnmsCriteria getOnmsCriteria(final AlarmCriteria alarmCriteria) {
        final OnmsCriteria criteria = new OnmsCriteria(OnmsAlarm.class);
        criteria.createAlias("node", "node", OnmsCriteria.LEFT_JOIN);
        criteria.createAlias("distPoller", "distPoller", OnmsCriteria.LEFT_JOIN);
        criteria.createAlias("lastEvent", "lastEvent", OnmsCriteria.LEFT_JOIN);
        criteria.createAlias("serviceType", "serviceType", OnmsCriteria.LEFT_JOIN);

        alarmCriteria.visit(new AlarmCriteriaVisitor<RuntimeException>() {

            @Override
            public void visitAckType(AcknowledgeType ackType) throws RuntimeException {
                if (ackType == AcknowledgeType.ACKNOWLEDGED) {
                    criteria.add(Restrictions.isNotNull("alarmAckUser"));
                } else if (ackType == AcknowledgeType.UNACKNOWLEDGED) {
                    criteria.add(Restrictions.isNull("alarmAckUser"));
                }
            }

            @Override
            public void visitFilter(Filter filter) throws RuntimeException {
                criteria.add(filter.getCriterion());
            }

            @Override
            public void visitLimit(int limit, int offset) throws RuntimeException {
                criteria.setMaxResults(limit);
                criteria.setFirstResult(offset);
            }

            @Override
            public void visitSortStyle(SortStyle sortStyle) throws RuntimeException {
                switch (sortStyle) {
                    case COUNT:
                        criteria.addOrder(Order.desc("counter"));
                        break;
                    case FIRSTEVENTTIME:
                        criteria.addOrder(Order.desc("firstEventTime"));
                        break;
                    case ID:
                        criteria.addOrder(Order.desc("id"));
                        break;
                    case INTERFACE:
                        criteria.addOrder(Order.desc("ipAddr"));
                        break;
                    case LASTEVENTTIME:
                        criteria.addOrder(Order.desc("lastEventTime"));
                        break;
                    case NODE:
                        criteria.addOrder(Order.desc("node.label"));
                        break;
                    case POLLER:
                        criteria.addOrder(Order.desc("distPoller"));
                        break;
                    case SERVICE:
                        criteria.addOrder(Order.desc("serviceType.name"));
                        break;
                    case SEVERITY:
                        criteria.addOrder(Order.desc("severity"));
                        break;
                    case ACKUSER:
                        criteria.addOrder(Order.asc("alarmAckUser"));
                        break;
                    case SITUATION:
                        criteria.addOrder(Order.desc("situation"));
                        break;
                    case REVERSE_COUNT:
                        criteria.addOrder(Order.asc("counter"));
                        break;
                    case REVERSE_FIRSTEVENTTIME:
                        criteria.addOrder(Order.asc("firstEventTime"));
                        break;
                    case REVERSE_ID:
                        criteria.addOrder(Order.asc("id"));
                        break;
                    case REVERSE_INTERFACE:
                        criteria.addOrder(Order.asc("ipAddr"));
                        break;
                    case REVERSE_LASTEVENTTIME:
                        criteria.addOrder(Order.asc("lastEventTime"));
                        break;
                    case REVERSE_NODE:
                        criteria.addOrder(Order.asc("node.label"));
                        break;
                    case REVERSE_POLLER:
                        criteria.addOrder(Order.asc("distPoller"));
                        break;
                    case REVERSE_SERVICE:
                        criteria.addOrder(Order.asc("serviceType.name"));
                        break;
                    case REVERSE_SEVERITY:
                        criteria.addOrder(Order.asc("severity"));
                        break;
                    case REVERSE_ACKUSER:
                        criteria.addOrder(Order.desc("alarmAckUser"));
                        break;
                    case REVERSE_SITUATION:
                        criteria.addOrder(Order.asc("situation"));
                        break;
                    default:
                        break;
                }
            }
        });

        return criteria;
    }

    /**
     * <p>getFilter</p>
     *
     * @param allFilters a {@link java.lang.String}[] object holding all filter Strings
     * @param filterString a {@link java.lang.String} object.
     * @return a {@link org.opennms.web.filter.Filter} object.
     */
    public static Filter getFilter(String[] allFilters, String filterString, ServletContext servletContext) {
        if (filterString == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Filter filter = null;

        String[] tokenizedFilterString = tokenizeFilterString(filterString);
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
            filter = new InterfaceFilter(InetAddressUtils.addr(value));
        } else if (type.equals(ServiceFilter.TYPE)) {
            String[] ids = value.split(ARRAY_DELIMITER);
            Integer[] serviceIds = new Integer[ids.length];
            for (int index = 0; index < ids.length; index++) {
                serviceIds[index] = WebSecurityUtils.safeParseInt(ids[index]);
            }
            filter = new ServiceOrFilter(serviceIds, servletContext);
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
            filter = new NegativeServiceFilter(WebSecurityUtils.safeParseInt(value), servletContext);
        } else if (type.equals(NegativePartialUEIFilter.TYPE)) {
            filter = new NegativePartialUEIFilter(value);
        } else if (type.equals(NegativeExactUEIFilter.TYPE)) {
            filter = new NegativeExactUEIFilter(value);
        } else if (type.equals(NegativeAcknowledgedByFilter.TYPE)) {
            filter = new NegativeAcknowledgedByFilter(value);
        } else if (type.equals(IPAddrLikeFilter.TYPE)) {
            if (value.startsWith(NEGATION_PREFIX_SYMBOL)) {
                filter = new NegativeIPAddrLikeFilter(value.substring(1));
            } else {
                filter = new IPAddrLikeFilter(value);
            }
        } else if (type.equals(AlarmTextFilter.TYPE)) {
            if (value.startsWith(NEGATION_PREFIX_SYMBOL)) {
                filter = new NegativeAlarmTextFilter(value.substring(1));
            } else {
                filter = new AlarmTextFilter(value);
            }
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
        } else if (type.equals(LocationFilter.TYPE)) {
            filter = new LocationFilter(value);
        } else if (type.equals(NegativeLocationFilter.TYPE)) {
            filter = new NegativeLocationFilter(value);
        } else if (type.equals(NodeLocationFilter.TYPE)) {
            filter = new NodeLocationFilter(value);
        } else if (type.equals(NegativeNodeLocationFilter.TYPE)) {
            filter = new NegativeNodeLocationFilter(value);
        } else if (type.equals(SituationFilter.TYPE)) {
            filter = new SituationFilter(Boolean.valueOf(value));
        } else if (type.equals(CategoryFilter.TYPE)) {
            String[] nestedFilterString = findFilterString(allFilters, NegativeCategoryFilter.NESTED_TYPE);
            if (isCheckboxToggled(nestedFilterString)) {
                filter = new NegativeCategoryFilter(value);
            } else {
                filter = new CategoryFilter(value);
            }
        }

        return filter;
    }

    /**
     * <p>findFilterString</p>
     *
     * @param allFilters a {@link java.lang.String}[] object representing all filters.
     * @param filterString a {@link java.lang.String} object.
     * @return a {@link java.lang.String}[] object representing the type and value tokenized.
     */
    private static String[] findFilterString(String[] allFilters, String filterString) {
        if (allFilters == null) {
            return null;
        }
        for (String thisFilter : allFilters) {
            if (thisFilter.startsWith(filterString)) {
                return tokenizeFilterString(thisFilter);
            }
        }
        return null;
    }

    /**
     * <p>tokenizeFilterString</p>
     *
     * @param filterString a {@link java.lang.String} object.
     * @return a {@link java.lang.String}[] object representing the type and value tokenized.
     */
    private static String[] tokenizeFilterString(String filterString) {
        String[] tempTokens = filterString.split("=");
        try {
            String type = tempTokens[0];
            String[] values = (String[]) ArrayUtils.remove(tempTokens, 0);
            String value = org.apache.commons.lang.StringUtils.join(values, "=");
            return new String[]{type, value};
        } catch (NoSuchElementException e) {
            throw new IllegalArgumentException("Could not tokenize filter string: " + filterString);
        }
    }

    /**
     * <p>isCheckboxToggled</p>
     *
     * @param tokenizedFilterString a {@link java.lang.String}[] object representing the type and value tokenized.
     * @return a {@link java.lang.Boolean}[] representing if the option is toggled.
     */
    private static boolean isCheckboxToggled(String[] tokenizedFilterString) {
        return tokenizedFilterString != null &&
                StringUtils.equalsTrimmed(tokenizedFilterString[1], POSITIVE_CHECKBOX_VALUE);
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

    public static List<Filter> getFilterList(String[] filterStrings, ServletContext servletContext) {
        filterStrings = handleCheckboxDuplication(filterStrings);

        List<Filter> filterList = new ArrayList<>();
        if (filterStrings != null) {
            for (String filterString : filterStrings) {
                Filter filter = AlarmUtil.getFilter(filterStrings, filterString, servletContext);
                if (filter != null) {
                    filterList.add(filter);
                }
            }
        }
        return filterList;
    }

    /**
     * <p>handleCheckboxDuplication</p>
     *
     * @param filterStrings array of filter strings
     * @return an array of filter strings which have checkbox deduplication handled. With multi-select there could be
     * multiple IDs selected so we try to consolidate it into a single filter string
     */
    private static String[] handleCheckboxDuplication(String[] filterStrings) {
        if (filterStrings == null) {
            return filterStrings;
        }
        Map<String, List<String>> selectedCheckboxes = dedupCheckboxSelections(filterStrings);

        if (selectedCheckboxes.isEmpty()) {
            return filterStrings;
        }
        return replaceCheckboxValues(filterStrings, selectedCheckboxes);
    }

    /**
     * <p>dedupCheckboxSelections</p>
     *
     * @param filterStrings array of filter strings
     * @return a Map of Filter type to List of IDs associated with the filter.
     */
    private static Map<String, List<String>> dedupCheckboxSelections(String[] filterStrings) {
        Map<String, List<String>> selectedCheckboxes = new HashMap<>();

        for (String filterString : filterStrings) {

            if (MULTI_CHECKBOX_PATTERN.matcher(filterString).matches()) {

                String[] filterStringHyphenSplit = filterString.split("-");
                String type = filterStringHyphenSplit[0];
                String remainder = filterStringHyphenSplit[1];
                String idStr = remainder.split("=")[0];

                if (selectedCheckboxes.containsKey(type)) {
                    selectedCheckboxes.get(type).add(idStr);
                } else {
                    List<String> checkedIds = new ArrayList<>();
                    checkedIds.add(idStr);
                    selectedCheckboxes.put(type, checkedIds);
                }
            }
        }
        return selectedCheckboxes;
    }

    /**
     * <p>replaceCheckboxValues</p>
     *
     * @param filterStrings array of filter strings
     * @param selectedCheckboxes map of multiselect filter to IDs
     * @return the new filter string array with consolidated multi-select checkbox values
     */
    private static String[] replaceCheckboxValues(String[] filterStrings, Map<String, List<String>> selectedCheckboxes) {
        List<String> collectedFilterList = Arrays.stream(filterStrings).filter(thisFilterString -> {
            for (String selectedKey : selectedCheckboxes.keySet()) {
                if (thisFilterString.startsWith(selectedKey + "-")) {
                    return false;
                }
            }
            return true;
        }).collect(Collectors.toList());

        for (String selectedKey : selectedCheckboxes.keySet()) {
            List<String> selectedIds = selectedCheckboxes.get(selectedKey);
            String joinedIds = String.join(ARRAY_DELIMITER, selectedIds);
            String checkboxFormattedFilter = String.format("%s=%s", selectedKey, joinedIds);

            collectedFilterList.add(checkboxFormattedFilter);
        }

        return collectedFilterList.toArray(new String[0]);
    }
}
