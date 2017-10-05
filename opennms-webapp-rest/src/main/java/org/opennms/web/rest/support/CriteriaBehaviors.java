/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.web.rest.support;

import static org.opennms.web.rest.support.CriteriaValueConverters.DATE_CONVERTER;
import static org.opennms.web.rest.support.CriteriaValueConverters.FLOAT_CONVERTER;
import static org.opennms.web.rest.support.CriteriaValueConverters.INET_ADDRESS_CONVERTER;
import static org.opennms.web.rest.support.CriteriaValueConverters.INT_CONVERTER;
import static org.opennms.web.rest.support.CriteriaValueConverters.LONG_CONVERTER;

import java.net.InetAddress;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.cxf.jaxrs.ext.search.ConditionType;
import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.criteria.restrictions.Restrictions;
import org.opennms.core.criteria.restrictions.SqlRestriction.Type;
import org.opennms.netmgt.model.OnmsEventParameter;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.TroubleTicketState;
import org.opennms.web.api.ISO8601DateEditor;

/**
 * Convenience lists of {@link CriteriaBehavior} objects for different database
 * tables.
 */
public abstract class CriteriaBehaviors {
    public static Date parseDate(final String string) {
        return ISO8601DateEditor.stringToDate(string);
    }

    /**
     * Prepend a join alias to the property ID for each {@link CriteriaBehavior}.
     * 
     * @param alias
     * @param behaviors
     * @return
     */
    public static final Map<String,CriteriaBehavior<?>> withAliasPrefix(Aliases alias, Map<String,CriteriaBehavior<?>> behaviors) {
        Map<String,CriteriaBehavior<?>> retval = new HashMap<>();
        for (Map.Entry<String,CriteriaBehavior<?>> entry : behaviors.entrySet()) {
            retval.put(alias.prop(entry.getKey()), entry.getValue());
        }
        return retval;
    }

    /**
     * Prepend a join alias to the property ID for each {@link CriteriaBehavior}.
     * 
     * @param alias
     * @param behaviors
     * @return
     */
    public static final Map<String,CriteriaBehavior<?>> withAliasPrefix(String alias, Map<String,CriteriaBehavior<?>> behaviors) {
        Map<String,CriteriaBehavior<?>> retval = new HashMap<>();
        for (Map.Entry<String,CriteriaBehavior<?>> entry : behaviors.entrySet()) {
            retval.put(alias + "." + entry.getKey(), entry.getValue());
        }
        return retval;
    }

    /**
     * <p>This criteria behavior uses SQL subselect restrictions when doing wildcard filtering or 
     * negative filtering (since these queries will most likely return multiple rows that match
     * the criteria) but it uses an alias with a join condition for specific filters. The join
     * conditions will stack so that multiple specific filters will narrow the results, allowing
     * you to query for specific combinations of:</p>
     * <ul>
     * <li>eventParameter.name</li>
     * <li>eventParameter.value</li>
     * <li>eventParameter.type</li>
     * </ul>
     * <p>Querying for a specific name-value pair is the primary use case for event parameter filtering.</p>
     */
    private static final class EventParameterBehavior extends StringCriteriaBehavior {

        /**
         * @param eventParameterPath The Hibernate property path for the eventParameter relationship
         * @param eventIdColumn The column name in the database for the event ID column in the root entity table
         * @param eventParameterProperty The name of the property in the {@link OnmsEventParameter} object that
         *        this behavior is being applied to
         */
        public EventParameterBehavior(String eventParameterPath, String eventIdColumn, String eventParameterProperty) {
            super(Aliases.eventParameter.prop(eventParameterProperty), (b,v,c,w) -> {
                switch (c) {
                case EQUALS:
                    if (w) {
                        b.sql(String.format("{alias}.%s in (select event_parameters.eventid from event_parameters where event_parameters.%s %s ?)", eventIdColumn, eventParameterProperty, w ? "like" : "="), v, Type.STRING);
                    } else {
                        // Add an eventParameter alias that only matches the specified value
                        b.alias(
                            eventParameterPath,
                            Aliases.eventParameter.toString(),
                            JoinType.LEFT_JOIN,
                            Restrictions.or(
                                Restrictions.eq(Aliases.eventParameter.prop(eventParameterProperty), v),
                                Restrictions.isNull(Aliases.eventParameter.prop(eventParameterProperty))
                            )
                        );
                    }
                    break;
                case NOT_EQUALS:
                    b.sql(String.format("{alias}.%s not in (select event_parameters.eventid from event_parameters where event_parameters.%s %s ?)", eventIdColumn, eventParameterProperty, w ? "like" : "="), v, Type.STRING);
                    break;
                default:
                    throw new IllegalArgumentException("Illegal condition type when filtering event_parameters." + eventParameterProperty + ": " + c.toString());
                }
            });
        }

        @Override
        public boolean shouldSkipProperty(ConditionType condition, boolean wildcard) {
            switch(condition) {
            case EQUALS:
                if (wildcard) {
                    // If we're using a SQL subselect restriction, then skip the normal
                    // property filtering
                    return true;
                } else {
                    // If we're using an alias with join condition, then process the
                    // property filtering as usual
                    return false;
                }
            case NOT_EQUALS:
                // All negative filters use SQL subselect restrictions so skip the normal
                // property filtering
                return true;
            default:
                return super.shouldSkipProperty(condition, wildcard);
            }
        }
    }

    public static final Map<String,CriteriaBehavior<?>> ALARM_BEHAVIORS = new HashMap<>();
    public static final Map<String,CriteriaBehavior<?>> ALARM_LASTEVENT_PARAMETER_BEHAVIORS = new HashMap<>();
    // TODO
    //public static final Map<String,CriteriaBehavior<?>> ALARM_DETAILS_BEHAVIORS = new HashMap<>();
    public static final Map<String,CriteriaBehavior<?>> ASSET_RECORD_BEHAVIORS = new HashMap<>();
    public static final Map<String,CriteriaBehavior<?>> DIST_POLLER_BEHAVIORS = new HashMap<>();
    public static final Map<String,CriteriaBehavior<?>> EVENT_BEHAVIORS = new HashMap<>();
    public static final Map<String,CriteriaBehavior<?>> EVENT_PARAMETER_BEHAVIORS = new HashMap<>();
    public static final Map<String,CriteriaBehavior<?>> IP_INTERFACE_BEHAVIORS = new HashMap<>();
    // TODO
    public static final Map<String,CriteriaBehavior<?>> MEMO_BEHAVIORS = new HashMap<>();
    // TODO
    public static final Map<String,CriteriaBehavior<?>> MONITORED_SERVICE_BEHAVIORS = new HashMap<>();
    public static final Map<String,CriteriaBehavior<?>> MONITORING_LOCATION_BEHAVIORS = new HashMap<>();
    public static final Map<String,CriteriaBehavior<?>> NODE_BEHAVIORS = new HashMap<>();
    public static final Map<String,CriteriaBehavior<?>> NODE_CATEGORY_BEHAVIORS = new HashMap<>();
    public static final Map<String,CriteriaBehavior<?>> NOTIFICATION_BEHAVIORS = new HashMap<>();
    public static final Map<String,CriteriaBehavior<?>> OUTAGE_BEHAVIORS = new HashMap<>();
    // TODO
    public static final Map<String,CriteriaBehavior<?>> REDUCTION_KEY_MEMO_BEHAVIORS = new HashMap<>();
    public static final Map<String,CriteriaBehavior<?>> SERVICE_TYPE_BEHAVIORS = new HashMap<>();
    public static final Map<String,CriteriaBehavior<?>> SNMP_INTERFACE_BEHAVIORS = new HashMap<>();

    static {
        ALARM_BEHAVIORS.put("id", new CriteriaBehavior<Integer>(INT_CONVERTER));
        ALARM_BEHAVIORS.put("alarmAckTime", new CriteriaBehavior<Date>(DATE_CONVERTER));
        ALARM_BEHAVIORS.put("alarmType", new CriteriaBehavior<Integer>(INT_CONVERTER));
        ALARM_BEHAVIORS.put("counter", new CriteriaBehavior<Integer>(INT_CONVERTER));
        ALARM_BEHAVIORS.put("firstAutomationTime", new CriteriaBehavior<Date>(DATE_CONVERTER));
        ALARM_BEHAVIORS.put("firstEventTime", new CriteriaBehavior<Date>(DATE_CONVERTER));
        ALARM_BEHAVIORS.put("ifIndex", new CriteriaBehavior<Integer>(INT_CONVERTER));
        ALARM_BEHAVIORS.put("ipAddr", new CriteriaBehavior<InetAddress>(INET_ADDRESS_CONVERTER));
        ALARM_BEHAVIORS.put("lastAutomationTime", new CriteriaBehavior<Date>(DATE_CONVERTER));
        ALARM_BEHAVIORS.put("lastEventTime", new CriteriaBehavior<Date>(DATE_CONVERTER));
        ALARM_BEHAVIORS.put("severity", new CriteriaBehavior<OnmsSeverity>(OnmsSeverity::get));
        ALARM_BEHAVIORS.put("suppressedTime", new CriteriaBehavior<Date>(DATE_CONVERTER));
        ALARM_BEHAVIORS.put("suppressedUntil", new CriteriaBehavior<Date>(DATE_CONVERTER));
        ALARM_BEHAVIORS.put("troubleTicketState", new CriteriaBehavior<TroubleTicketState>(TroubleTicketState::valueOf));
        ALARM_BEHAVIORS.put("x733ProbableCause", new CriteriaBehavior<Integer>(INT_CONVERTER));

        ALARM_LASTEVENT_PARAMETER_BEHAVIORS.put("name", new EventParameterBehavior("lastEvent.eventParameters", "lasteventid", "name"));
        ALARM_LASTEVENT_PARAMETER_BEHAVIORS.put("value", new EventParameterBehavior("lastEvent.eventParameters", "lasteventid", "value"));
        ALARM_LASTEVENT_PARAMETER_BEHAVIORS.put("type", new EventParameterBehavior("lastEvent.eventParameters", "lasteventid", "type"));

        ASSET_RECORD_BEHAVIORS.put("id", new CriteriaBehavior<Integer>(INT_CONVERTER));
        ASSET_RECORD_BEHAVIORS.put("lastModifiedDate", new CriteriaBehavior<Date>(DATE_CONVERTER));
        //ASSET_RECORD_BEHAVIORS.put("geolocation", ???);

        DIST_POLLER_BEHAVIORS.put("lastUpdated", new CriteriaBehavior<Date>(DATE_CONVERTER));

        EVENT_BEHAVIORS.put("eventAckTime", new CriteriaBehavior<Date>(DATE_CONVERTER));
        EVENT_BEHAVIORS.put("eventCreateTime", new CriteriaBehavior<Date>(DATE_CONVERTER));
        EVENT_BEHAVIORS.put("eventSeverity", new CriteriaBehavior<Integer>(INT_CONVERTER));
        EVENT_BEHAVIORS.put("eventSuppressedCount", new CriteriaBehavior<Integer>(INT_CONVERTER));
        EVENT_BEHAVIORS.put("eventTime", new CriteriaBehavior<Date>(DATE_CONVERTER));
        EVENT_BEHAVIORS.put("eventTTicketState", new CriteriaBehavior<Integer>(INT_CONVERTER));
        EVENT_BEHAVIORS.put("id", new CriteriaBehavior<Integer>(INT_CONVERTER));
        EVENT_BEHAVIORS.put("ifIndex", new CriteriaBehavior<Integer>(INT_CONVERTER));
        EVENT_BEHAVIORS.put("ipAddr", new CriteriaBehavior<InetAddress>(INET_ADDRESS_CONVERTER));

        EVENT_PARAMETER_BEHAVIORS.put("name", new EventParameterBehavior(Aliases.event.prop("eventParameters"), "eventid", "name"));
        EVENT_PARAMETER_BEHAVIORS.put("value", new EventParameterBehavior(Aliases.event.prop("eventParameters"), "eventid", "value"));
        EVENT_PARAMETER_BEHAVIORS.put("type", new EventParameterBehavior(Aliases.event.prop("eventParameters"), "eventid", "type"));

        IP_INTERFACE_BEHAVIORS.put("id", new CriteriaBehavior<Integer>(INT_CONVERTER));
        IP_INTERFACE_BEHAVIORS.put("ipLastCapsdPoll", new CriteriaBehavior<Date>(DATE_CONVERTER));
        IP_INTERFACE_BEHAVIORS.put("ipAddress", new CriteriaBehavior<InetAddress>(INET_ADDRESS_CONVERTER));
        IP_INTERFACE_BEHAVIORS.put("netMask", new CriteriaBehavior<InetAddress>(INET_ADDRESS_CONVERTER));

        MONITORED_SERVICE_BEHAVIORS.put("id", new CriteriaBehavior<Integer>(INT_CONVERTER));
        MONITORED_SERVICE_BEHAVIORS.put("lastFail", new CriteriaBehavior<Date>(DATE_CONVERTER));
        MONITORED_SERVICE_BEHAVIORS.put("lastGood", new CriteriaBehavior<Date>(DATE_CONVERTER));

        MONITORING_LOCATION_BEHAVIORS.put("latitude", new CriteriaBehavior<Float>(FLOAT_CONVERTER));
        MONITORING_LOCATION_BEHAVIORS.put("longitude", new CriteriaBehavior<Float>(FLOAT_CONVERTER));
        MONITORING_LOCATION_BEHAVIORS.put("priority", new CriteriaBehavior<Long>(LONG_CONVERTER));
        //MONITORING_LOCATION_BEHAVIORS.put("tags", ???);

        NODE_BEHAVIORS.put("id", new CriteriaBehavior<Integer>(INT_CONVERTER));
        NODE_BEHAVIORS.put("createTime", new CriteriaBehavior<Date>(DATE_CONVERTER));
        NODE_BEHAVIORS.put("lastCapsdPoll", new CriteriaBehavior<Date>(DATE_CONVERTER));

        // Add aliases with join conditions when joining in the many-to-many node-to-category relationship
        CriteriaBehavior<Integer> categoryId = new CriteriaBehavior<Integer>(Aliases.category.prop("id"), INT_CONVERTER, (b,v,c,w) -> {
            // Add a categories alias that only matches the specified value
            // TODO: This should work but Hibernate is generating invalid SQL for this criteria
            //b.alias(Aliases.node.prop("categories"), Aliases.category.toString(), JoinType.LEFT_JOIN, Restrictions.or(Restrictions.eq(Aliases.category.prop("id"), v), Restrictions.isNull(Aliases.category.prop("id"))));

            switch (c) {
            case EQUALS:
                b.sql("{alias}.nodeid in (select category_node.nodeid from category_node where category_node.categoryid = ?)", v, Type.INTEGER);
                break;
            case NOT_EQUALS:
                b.sql("{alias}.nodeid not in (select category_node.nodeid from category_node where category_node.categoryid = ?)", v, Type.INTEGER);
                break;
            default:
                throw new IllegalArgumentException("Illegal condition type when filtering category.id: " + c.toString());
            }
        });
        // Skip normal processing of the property since we're doing all of the filtering 
        // in the beforeVisit() method
        categoryId.setSkipPropertyByDefault(true);
        NODE_CATEGORY_BEHAVIORS.put("id", categoryId);

        CriteriaBehavior<String> categoryName = new StringCriteriaBehavior(Aliases.category.prop("name"), (b,v,c,w) -> {
            // Add a categories alias that only matches the specified value
            // TODO: This should work but Hibernate is generating invalid SQL for this criteria
            //b.alias(Aliases.node.prop("categories"), Aliases.category.toString(), JoinType.LEFT_JOIN, Restrictions.or(Restrictions.eq(Aliases.category.prop("name"), v), Restrictions.isNull(Aliases.category.prop("name")))); 

            switch (c) {
            case EQUALS:
                b.sql(String.format("{alias}.nodeid in (select category_node.nodeid from category_node, categories where category_node.categoryid = categories.categoryid and categories.categoryname %s ?)", w ? "like" : "="), v, Type.STRING);
                break;
            case NOT_EQUALS:
                b.sql(String.format("{alias}.nodeid not in (select category_node.nodeid from category_node, categories where category_node.categoryid = categories.categoryid and categories.categoryname %s ?)", w ? "like" : "="), v, Type.STRING);
                break;
            default:
                throw new IllegalArgumentException("Illegal condition type when filtering category.name: " + c.toString());
            }
        });
        // Skip normal processing of the property since we're doing all of the filtering 
        // in the beforeVisit() method
        categoryName.setSkipPropertyByDefault(true);
        NODE_CATEGORY_BEHAVIORS.put("name", categoryName);

        CriteriaBehavior<String> categoryDescription = new StringCriteriaBehavior(Aliases.category.prop("description"), (b,v,c,w) -> {
            // Add a categories alias that only matches the specified value
            // TODO: This should work but Hibernate is generating invalid SQL for this criteria
            //b.alias(Aliases.node.prop("categories"), Aliases.category.toString(), JoinType.LEFT_JOIN, Restrictions.or(Restrictions.eq(Aliases.category.prop("description"), v), Restrictions.isNull(Aliases.category.prop("description")))); 

            switch (c) {
            case EQUALS:
                b.sql(String.format("{alias}.nodeid in (select category_node.nodeid from category_node, categories where category_node.categoryid = categories.categoryid and categories.categorydescription %s ?)", w ? "like" : "="), v, Type.STRING);
                break;
            case NOT_EQUALS:
                b.sql(String.format("{alias}.nodeid not in (select category_node.nodeid from category_node, categories where category_node.categoryid = categories.categoryid and categories.categorydescription %s ?)", w ? "like" : "="), v, Type.STRING);
                break;
            default:
                throw new IllegalArgumentException("Illegal condition type when filtering category.description: " + c.toString());
            }
        });
        // Skip normal processing of the property since we're doing all of the filtering 
        // in the beforeVisit() method
        categoryDescription.setSkipPropertyByDefault(true);
        NODE_CATEGORY_BEHAVIORS.put("description", categoryDescription);

        NOTIFICATION_BEHAVIORS.put("notifyId", new CriteriaBehavior<Integer>(INT_CONVERTER));
        NOTIFICATION_BEHAVIORS.put("pageTime", new CriteriaBehavior<Date>(DATE_CONVERTER));
        NOTIFICATION_BEHAVIORS.put("respondTime", new CriteriaBehavior<Date>(DATE_CONVERTER));

        OUTAGE_BEHAVIORS.put("id", new CriteriaBehavior<Integer>(INT_CONVERTER));
        OUTAGE_BEHAVIORS.put("ifLostService", new CriteriaBehavior<Date>(DATE_CONVERTER));
        OUTAGE_BEHAVIORS.put("ifRegainedService", new CriteriaBehavior<Date>(DATE_CONVERTER));
        OUTAGE_BEHAVIORS.put("suppressTime", new CriteriaBehavior<Date>(DATE_CONVERTER));

        SERVICE_TYPE_BEHAVIORS.put("id", new CriteriaBehavior<Integer>(INT_CONVERTER));

        SNMP_INTERFACE_BEHAVIORS.put("id", new CriteriaBehavior<Integer>(INT_CONVERTER));
        SNMP_INTERFACE_BEHAVIORS.put("ifAdminStatus", new CriteriaBehavior<Integer>(INT_CONVERTER));
        SNMP_INTERFACE_BEHAVIORS.put("ifIndex", new CriteriaBehavior<Integer>(INT_CONVERTER));
        SNMP_INTERFACE_BEHAVIORS.put("ifOperStatus", new CriteriaBehavior<Integer>(INT_CONVERTER));
        SNMP_INTERFACE_BEHAVIORS.put("ifSpeed", new CriteriaBehavior<Long>(LONG_CONVERTER));
        SNMP_INTERFACE_BEHAVIORS.put("lastCapsdPoll", new CriteriaBehavior<Date>(DATE_CONVERTER));
        SNMP_INTERFACE_BEHAVIORS.put("lastSnmpPoll", new CriteriaBehavior<Date>(DATE_CONVERTER));
    }
}
