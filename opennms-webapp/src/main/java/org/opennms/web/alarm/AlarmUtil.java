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
package org.opennms.web.alarm;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.servlet.ServletContext;

import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.opennms.core.utils.InetAddressUtils;
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
import org.opennms.web.filter.ConditionalFilter;
import org.opennms.web.filter.Filter;
import org.opennms.web.utils.filter.CheckboxFilterUtils;
import org.opennms.web.utils.filter.FilterTokenizeUtils;

import static org.opennms.web.utils.filter.CheckboxFilterUtils.ARRAY_DELIMITER;

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

    /** Constant <code>NEGATION_PREFIX_SYMBOL="!"</code> */
    public static final String NEGATION_PREFIX_SYMBOL = "!";

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
     * @param allFilters a {@link java.lang.String}[] object holding all filter Strings
     * @param filterString a {@link java.lang.String} object.
     * @return a {@link org.opennms.web.filter.Filter} object.
     */
    public static Filter getFilter(String[] allFilters, String filterString, ServletContext servletContext) {
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
            filter = new InterfaceFilter(InetAddressUtils.addr(value));
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
            if (CheckboxFilterUtils.isCheckboxToggled(nestedFilterString)) {
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
                return FilterTokenizeUtils.tokenizeFilterString(thisFilter);
            }
        }
        return null;
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
        filterStrings = CheckboxFilterUtils.handleCheckboxDuplication(filterStrings);

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
}
