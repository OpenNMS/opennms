/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
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

import java.net.InetAddress;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.opennms.core.criteria.restrictions.SqlRestriction.Type;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.TroubleTicketState;

/**
 * Convenience lists of {@link CriteriaBehavior} objects for different database
 * tables.
 */
public abstract class CriteriaBehaviors {

    public static final ThreadLocal<DateFormat> SEARCH_DATE_FORMAT = new ThreadLocal<DateFormat>() {
        @Override
        public DateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        }
    };

    public static Date parseDate(String string) {
        try {
            return SEARCH_DATE_FORMAT.get().parse(string);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Unparseable date: " + string, e);
        }
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

    public static final Map<String,CriteriaBehavior<?>> ALARM_BEHAVIORS = new HashMap<>();
    // TODO
    //public static final Map<String,CriteriaBehavior<?>> ALARM_DETAILS_BEHAVIORS = new HashMap<>();
    public static final Map<String,CriteriaBehavior<?>> ASSET_RECORD_BEHAVIORS = new HashMap<>();
    public static final Map<String,CriteriaBehavior<?>> DIST_POLLER_BEHAVIORS = new HashMap<>();
    public static final Map<String,CriteriaBehavior<?>> EVENT_BEHAVIORS = new HashMap<>();
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
        ALARM_BEHAVIORS.put("id", new CriteriaBehavior<Integer>(Integer::parseInt));
        ALARM_BEHAVIORS.put("alarmAckTime", new CriteriaBehavior<Date>(CriteriaBehaviors::parseDate));
        ALARM_BEHAVIORS.put("alarmType", new CriteriaBehavior<Integer>(Integer::parseInt));
        ALARM_BEHAVIORS.put("counter", new CriteriaBehavior<Integer>(Integer::parseInt));
        ALARM_BEHAVIORS.put("firstAutomationTime", new CriteriaBehavior<Date>(CriteriaBehaviors::parseDate));
        ALARM_BEHAVIORS.put("firstEventTime", new CriteriaBehavior<Date>(CriteriaBehaviors::parseDate));
        ALARM_BEHAVIORS.put("ifIndex", new CriteriaBehavior<Integer>(Integer::parseInt));
        ALARM_BEHAVIORS.put("ipAddr", new CriteriaBehavior<InetAddress>(InetAddressUtils::addr));
        ALARM_BEHAVIORS.put("lastAutomationTime", new CriteriaBehavior<Date>(CriteriaBehaviors::parseDate));
        ALARM_BEHAVIORS.put("lastEventTime", new CriteriaBehavior<Date>(CriteriaBehaviors::parseDate));
        ALARM_BEHAVIORS.put("severity", new CriteriaBehavior<OnmsSeverity>(OnmsSeverity::get));
        ALARM_BEHAVIORS.put("suppressedTime", new CriteriaBehavior<Date>(CriteriaBehaviors::parseDate));
        ALARM_BEHAVIORS.put("suppressedUntil", new CriteriaBehavior<Date>(CriteriaBehaviors::parseDate));
        ALARM_BEHAVIORS.put("troubleTicketState", new CriteriaBehavior<TroubleTicketState>(TroubleTicketState::valueOf));
        ALARM_BEHAVIORS.put("x733ProbableCause", new CriteriaBehavior<Integer>(Integer::parseInt));

        ASSET_RECORD_BEHAVIORS.put("id", new CriteriaBehavior<Integer>(Integer::parseInt));
        ASSET_RECORD_BEHAVIORS.put("lastModifiedDate", new CriteriaBehavior<Date>(CriteriaBehaviors::parseDate));
        //ASSET_RECORD_BEHAVIORS.put("geolocation", ???);

        DIST_POLLER_BEHAVIORS.put("lastUpdated", new CriteriaBehavior<Date>(CriteriaBehaviors::parseDate));

        EVENT_BEHAVIORS.put("eventAckTime", new CriteriaBehavior<Date>(CriteriaBehaviors::parseDate));
        EVENT_BEHAVIORS.put("eventCreateTime", new CriteriaBehavior<Date>(CriteriaBehaviors::parseDate));
        EVENT_BEHAVIORS.put("eventSeverity", new CriteriaBehavior<Integer>(Integer::parseInt));
        EVENT_BEHAVIORS.put("eventSuppressedCount", new CriteriaBehavior<Integer>(Integer::parseInt));
        EVENT_BEHAVIORS.put("eventTime", new CriteriaBehavior<Date>(CriteriaBehaviors::parseDate));
        EVENT_BEHAVIORS.put("eventTTicketState", new CriteriaBehavior<Integer>(Integer::parseInt));
        EVENT_BEHAVIORS.put("id", new CriteriaBehavior<Integer>(Integer::parseInt));
        EVENT_BEHAVIORS.put("ifIndex", new CriteriaBehavior<Integer>(Integer::parseInt));
        EVENT_BEHAVIORS.put("ipAddr", new CriteriaBehavior<InetAddress>(InetAddressUtils::addr));

        IP_INTERFACE_BEHAVIORS.put("id", new CriteriaBehavior<Integer>(Integer::parseInt));
        IP_INTERFACE_BEHAVIORS.put("ipLastCapsdPoll", new CriteriaBehavior<Date>(CriteriaBehaviors::parseDate));
        IP_INTERFACE_BEHAVIORS.put("ipAddress", new CriteriaBehavior<InetAddress>(InetAddressUtils::addr));

        MONITORED_SERVICE_BEHAVIORS.put("id", new CriteriaBehavior<Integer>(Integer::parseInt));
        MONITORED_SERVICE_BEHAVIORS.put("ifindex", new CriteriaBehavior<Integer>(Integer::parseInt));
        MONITORED_SERVICE_BEHAVIORS.put("lastFail", new CriteriaBehavior<Date>(CriteriaBehaviors::parseDate));
        MONITORED_SERVICE_BEHAVIORS.put("lastGood", new CriteriaBehavior<Date>(CriteriaBehaviors::parseDate));

        MONITORING_LOCATION_BEHAVIORS.put("latitude", new CriteriaBehavior<Float>(Float::parseFloat));
        MONITORING_LOCATION_BEHAVIORS.put("longitude", new CriteriaBehavior<Float>(Float::parseFloat));
        MONITORING_LOCATION_BEHAVIORS.put("priority", new CriteriaBehavior<Long>(Long::parseLong));
        //MONITORING_LOCATION_BEHAVIORS.put("tags", ???);

        NODE_BEHAVIORS.put("id", new CriteriaBehavior<Integer>(Integer::parseInt));
        NODE_BEHAVIORS.put("createTime", new CriteriaBehavior<Date>(CriteriaBehaviors::parseDate));
        NODE_BEHAVIORS.put("lastCapsdPoll", new CriteriaBehavior<Date>(CriteriaBehaviors::parseDate));

        // Add aliases with join conditions when joining in the many-to-many node-to-category relationship
        CriteriaBehavior<Integer> categoryId = new CriteriaBehavior<Integer>(Aliases.category.prop("id"), Integer::parseInt, (b,v,c,w) -> {
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
        categoryId.setSkipProperty(true);
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
        categoryName.setSkipProperty(true);
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
        categoryDescription.setSkipProperty(true);
        NODE_CATEGORY_BEHAVIORS.put("description", categoryDescription);

        NOTIFICATION_BEHAVIORS.put("notifyId", new CriteriaBehavior<Integer>(Integer::parseInt));
        NOTIFICATION_BEHAVIORS.put("pageTime", new CriteriaBehavior<Date>(CriteriaBehaviors::parseDate));
        NOTIFICATION_BEHAVIORS.put("respondTime", new CriteriaBehavior<Date>(CriteriaBehaviors::parseDate));

        OUTAGE_BEHAVIORS.put("id", new CriteriaBehavior<Integer>(Integer::parseInt));
        OUTAGE_BEHAVIORS.put("ifLostService", new CriteriaBehavior<Date>(CriteriaBehaviors::parseDate));
        OUTAGE_BEHAVIORS.put("ifRegainedService", new CriteriaBehavior<Date>(CriteriaBehaviors::parseDate));
        OUTAGE_BEHAVIORS.put("suppressTime", new CriteriaBehavior<Date>(CriteriaBehaviors::parseDate));

        SERVICE_TYPE_BEHAVIORS.put("id", new CriteriaBehavior<Integer>(Integer::parseInt));

        SNMP_INTERFACE_BEHAVIORS.put("id", new CriteriaBehavior<Integer>(Integer::parseInt));
        SNMP_INTERFACE_BEHAVIORS.put("ifAdminStatus", new CriteriaBehavior<Integer>(Integer::parseInt));
        SNMP_INTERFACE_BEHAVIORS.put("ifIndex", new CriteriaBehavior<Integer>(Integer::parseInt));
        SNMP_INTERFACE_BEHAVIORS.put("ifOperStatus", new CriteriaBehavior<Integer>(Integer::parseInt));
        SNMP_INTERFACE_BEHAVIORS.put("ifSpeed", new CriteriaBehavior<Long>(Long::parseLong));
        SNMP_INTERFACE_BEHAVIORS.put("lastCapsdPoll", new CriteriaBehavior<Date>(CriteriaBehaviors::parseDate));
        SNMP_INTERFACE_BEHAVIORS.put("lastSnmpPoll", new CriteriaBehavior<Date>(CriteriaBehaviors::parseDate));
        SNMP_INTERFACE_BEHAVIORS.put("netMask", new CriteriaBehavior<InetAddress>(InetAddressUtils::addr));
    }
}
