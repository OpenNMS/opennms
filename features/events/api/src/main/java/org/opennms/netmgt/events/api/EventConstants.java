/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.events.api;

import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.opennms.core.utils.Base64;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.xml.event.Value;

/**
 * This class holds all OpenNMS events related constants - the UEIs, parm
 * names, the event time format etc.
 */
public abstract class EventConstants {
    /**
     * The date format string to parse a Date.toString() type string to a
     * database timestamp using the PostgreSQL to_timestamp() built-in function.
     */
    public static final String POSTGRES_DATE_FORMAT = "\'Dy Mon DD HH24:MI:SS Tz YYYY\'";

    /**
     * This value is prepended to the UEI to form a Camel JMS endpoint URI.
     */
    public static final String JMS_URI_PREFIX = "queuingservice:topic:OpenNMS.Eventd.BroadcastEvent?concurrentConsumers=1&selector=uei='";

    /**
     * This value is appended to the UEI to form a Camel JMS endpoint URI.
     */
    public static final String JMS_URI_SUFFIX = "'";

    //
    // The eventUEIs used by OpenNMS
    //

    /**
     * The status query control event.
     */
    public static final String STATUS_QUERY_CONTROL_EVENT_UEI = "uei.opennms.org/internal/control/status";
    public static final String STATUS_QUERY_CONTROL_JMS_URI = JMS_URI_PREFIX + STATUS_QUERY_CONTROL_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * The start event.
     */
    public static final String START_CONTROL_EVENT_UEI = "uei.opennms.org/internal/control/start";
    public static final String START_CONTROL_JMS_URI = JMS_URI_PREFIX + START_CONTROL_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * The pause event.
     */
    public static final String PAUSE_CONTROL_EVENT_UEI = "uei.opennms.org/internal/control/pause";
    public static final String PAUSE_CONTROL_JMS_URI = JMS_URI_PREFIX + PAUSE_CONTROL_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * The resume event.
     */
    public static final String RESUME_CONTROL_EVENT_UEI = "uei.opennms.org/internal/control/resume";
    public static final String RESUME_CONTROL_JMS_URI = JMS_URI_PREFIX + RESUME_CONTROL_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * The stop event.
     */
    public static final String STOP_CONTROL_EVENT_UEI = "uei.opennms.org/internal/control/stop";
    public static final String STOP_CONTROL_JMS_URI = JMS_URI_PREFIX + STOP_CONTROL_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * The 'start pending' response event.
     */
    public static final String CONTROL_START_PENDING_EVENT_UEI = "uei.opennms.org/internal/control/startPending";
    public static final String CONTROL_START_PENDING_JMS_URI = JMS_URI_PREFIX + CONTROL_START_PENDING_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * The 'starting' response event.
     */
    public static final String CONTROL_STARTING_EVENT_UEI = "uei.opennms.org/internal/control/starting";
    public static final String CONTROL_STARTING_JMS_URI = JMS_URI_PREFIX + CONTROL_STARTING_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * The 'pause pending' response event.
     */
    public static final String CONTROL_PAUSE_PENDING_EVENT_UEI = "uei.opennms.org/internal/control/pausePending";
    public static final String CONTROL_PAUSE_PENDING_JMS_URI = JMS_URI_PREFIX + CONTROL_PAUSE_PENDING_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * The 'paused' response event.
     */
    public static final String CONTROL_PAUSED_EVENT_UEI = "uei.opennms.org/internal/control/paused";
    public static final String CONTROL_PAUSED_JMS_URI = JMS_URI_PREFIX + CONTROL_PAUSED_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * The 'resume pending' response event.
     */
    public static final String CONTROL_RESUME_PENDING_EVENT_UEI = "uei.opennms.org/internal/control/resumePending";
    public static final String CONTROL_RESUME_PENDING_JMS_URI = JMS_URI_PREFIX + CONTROL_RESUME_PENDING_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * The 'running' response event.
     */
    public static final String CONTROL_RUNNING_EVENT_UEI = "uei.opennms.org/internal/control/running";
    public static final String CONTROL_RUNNING_JMS_URI = JMS_URI_PREFIX + CONTROL_RUNNING_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * The 'stop pending' response event.
     */
    public static final String CONTROL_STOP_PENDING_EVENT_UEI = "uei.opennms.org/internal/control/stopPending";
    public static final String CONTROL_STOP_PENDING_JMS_URI = JMS_URI_PREFIX + CONTROL_STOP_PENDING_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * The 'stopped' response event.
     */
    public static final String CONTROL_STOPPED_EVENT_UEI = "uei.opennms.org/internal/control/stopped";
    public static final String CONTROL_STOPPED_JMS_URI = JMS_URI_PREFIX + CONTROL_STOPPED_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * The control error reponse event.
     */
    public static final String CONTROL_ERROR_EVENT_UEI = "uei.opennms.org/internal/control/error";
    public static final String CONTROL_ERROR_JMS_URI = JMS_URI_PREFIX + CONTROL_ERROR_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * The new suspect event UEI.
     */
    public static final String NEW_SUSPECT_INTERFACE_EVENT_UEI = "uei.opennms.org/internal/discovery/newSuspect";
    public static final String NEW_SUSPECT_INTERFACE_JMS_URI = JMS_URI_PREFIX + NEW_SUSPECT_INTERFACE_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * The discovery pause event UEI.
     */
    public static final String DISC_PAUSE_EVENT_UEI = "uei.opennms.org/internal/capsd/discPause";
    public static final String DISC_PAUSE_JMS_URI = JMS_URI_PREFIX + DISC_PAUSE_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * The discovery resume event UEI.
     */
    public static final String DISC_RESUME_EVENT_UEI = "uei.opennms.org/internal/capsd/discResume";
    public static final String DISC_RESUME_JMS_URI = JMS_URI_PREFIX + DISC_RESUME_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * The discovery configuration changed event UEI.
     */
    public static final String DISCOVERYCONFIG_CHANGED_EVENT_UEI = "uei.opennms.org/internal/discoveryConfigChange";
    public static final String DISCOVERYCONFIG_CHANGED_JMS_URI = JMS_URI_PREFIX + DISCOVERYCONFIG_CHANGED_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * The update server event UEI.
     */
    public static final String UPDATE_SERVER_EVENT_UEI = "uei.opennms.org/internal/capsd/updateServer";
    public static final String UPDATE_SERVER_JMS_URI = JMS_URI_PREFIX + UPDATE_SERVER_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * The update service event UEI.
     */
    public static final String UPDATE_SERVICE_EVENT_UEI = "uei.opennms.org/internal/capsd/updateService";
    public static final String UPDATE_SERVICE_JMS_URI = JMS_URI_PREFIX + UPDATE_SERVICE_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * The add node event UEI.
     */
    public static final String ADD_NODE_EVENT_UEI = "uei.opennms.org/internal/capsd/addNode";
    public static final String ADD_NODE_JMS_URI = JMS_URI_PREFIX + ADD_NODE_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * The delete node event UEI.
     */
    public static final String DELETE_NODE_EVENT_UEI = "uei.opennms.org/internal/capsd/deleteNode";
    public static final String DELETE_NODE_JMS_URI = JMS_URI_PREFIX + DELETE_NODE_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * The add interface event UEI.
     */
    public static final String ADD_INTERFACE_EVENT_UEI = "uei.opennms.org/internal/capsd/addInterface";
    public static final String ADD_INTERFACE_JMS_URI = JMS_URI_PREFIX + ADD_INTERFACE_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * The delete interface event UEI.
     */
    public static final String DELETE_INTERFACE_EVENT_UEI = "uei.opennms.org/internal/capsd/deleteInterface";
    public static final String DELETE_INTERFACE_JMS_URI = JMS_URI_PREFIX + DELETE_INTERFACE_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * The change service event UEI.
     */
    public static final String CHANGE_SERVICE_EVENT_UEI = "uei.opennms.org/internal/capsd/changeService";
    public static final String CHANGE_SERVICE_JMS_URI = JMS_URI_PREFIX + CHANGE_SERVICE_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * The outage created event UEI.
     */
    public static final String OUTAGE_CREATED_EVENT_UEI = "uei.opennms.org/internal/poller/outageCreated";
    public static final String OUTAGE_CREATED_JMS_URI = JMS_URI_PREFIX + OUTAGE_CREATED_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * The outage Resolved event UEI.
     */
    public static final String OUTAGE_RESOLVED_EVENT_UEI = "uei.opennms.org/internal/poller/outageResolved";
    public static final String OUTAGE_RESOLVED_JMS_URI = JMS_URI_PREFIX + OUTAGE_RESOLVED_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * The restart polling node event UEI.
     */
    public static final String RESTART_POLLING_INTERFACE_EVENT_UEI = "uei.opennms.org/nodes/restartPollingInterface";
    public static final String RESTART_POLLING_INTERFACE_JMS_URI = JMS_URI_PREFIX + RESTART_POLLING_INTERFACE_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * The node added event UEI.
     */
    public static final String NODE_ADDED_EVENT_UEI = "uei.opennms.org/nodes/nodeAdded";
    public static final String NODE_ADDED_JMS_URI = JMS_URI_PREFIX + NODE_ADDED_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * The node updated event UEI (added for the ProvisioningAdapter integration).
     */
    public static final String NODE_UPDATED_EVENT_UEI = "uei.opennms.org/nodes/nodeUpdated";
    public static final String NODE_UPDATED_JMS_URI = JMS_URI_PREFIX + NODE_UPDATED_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * The node category membership changed UEI.
     */
    public static final String NODE_CATEGORY_MEMBERSHIP_CHANGED_EVENT_UEI = "uei.opennms.org/nodes/nodeCategoryMembershipChanged";
    public static final String NODE_CATEGORY_MEMBERSHIP_CHANGED_JMS_URI = JMS_URI_PREFIX + NODE_CATEGORY_MEMBERSHIP_CHANGED_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * The node gained interface event UEI.
     */
    public static final String NODE_GAINED_INTERFACE_EVENT_UEI = "uei.opennms.org/nodes/nodeGainedInterface";
    public static final String NODE_GAINED_INTERFACE_JMS_URI = JMS_URI_PREFIX + NODE_GAINED_INTERFACE_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * The node gained service event UEI.
     */
    public static final String NODE_GAINED_SERVICE_EVENT_UEI = "uei.opennms.org/nodes/nodeGainedService";
    public static final String NODE_GAINED_SERVICE_JMS_URI = JMS_URI_PREFIX + NODE_GAINED_SERVICE_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * The node lost service event UEI.
     */
    public static final String NODE_LOST_SERVICE_EVENT_UEI = "uei.opennms.org/nodes/nodeLostService";
    public static final String NODE_LOST_SERVICE_JMS_URI = JMS_URI_PREFIX + NODE_LOST_SERVICE_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * The service responsive event UEI.
     */
    public static final String SERVICE_RESPONSIVE_EVENT_UEI = "uei.opennms.org/nodes/serviceResponsive";
    public static final String SERVICE_RESPONSIVE_JMS_URI = JMS_URI_PREFIX + SERVICE_RESPONSIVE_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * The service unresponsive event UEI.
     */
    public static final String SERVICE_UNRESPONSIVE_EVENT_UEI = "uei.opennms.org/nodes/serviceUnresponsive";
    public static final String SERVICE_UNRESPONSIVE_JMS_URI = JMS_URI_PREFIX + SERVICE_UNRESPONSIVE_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * The service unmanaged event UEI.
     */
    public static final String SERVICE_UNMANAGED_EVENT_UEI = "uei.opennms.org/nodes/serviceUnmanaged";
    public static final String SERVICE_UNMANAGED_JMS_URI = JMS_URI_PREFIX + SERVICE_UNMANAGED_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * The interface down event UEI.
     */
    public static final String INTERFACE_DOWN_EVENT_UEI = "uei.opennms.org/nodes/interfaceDown";
    public static final String INTERFACE_DOWN_JMS_URI = JMS_URI_PREFIX + INTERFACE_DOWN_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * The SNMP interface operStatus down event UEI.
     */
    public static final String SNMP_INTERFACE_OPER_DOWN_EVENT_UEI = "uei.opennms.org/nodes/snmp/interfaceOperDown";
    public static final String SNMP_INTERFACE_OPER_DOWN_JMS_URI = JMS_URI_PREFIX + SNMP_INTERFACE_OPER_DOWN_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * The SNMP interface admin down event UEI.
     */
    public static final String SNMP_INTERFACE_ADMIN_DOWN_EVENT_UEI = "uei.opennms.org/nodes/snmp/interfaceAdminDown";
    public static final String SNMP_INTERFACE_ADMIN_DOWN_JMS_URI = JMS_URI_PREFIX + SNMP_INTERFACE_ADMIN_DOWN_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * The node down event UEI.
     */
    public static final String NODE_DOWN_EVENT_UEI = "uei.opennms.org/nodes/nodeDown";
    public static final String NODE_DOWN_JMS_URI = JMS_URI_PREFIX + NODE_DOWN_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * The path outage event UEI.
     */
    public static final String PATH_OUTAGE_EVENT_UEI = "uei.opennms.org/nodes/pathOutage";
    public static final String PATH_OUTAGE_JMS_URI = JMS_URI_PREFIX + PATH_OUTAGE_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * The node up event UEI.
     */
    public static final String NODE_UP_EVENT_UEI = "uei.opennms.org/nodes/nodeUp";
    public static final String NODE_UP_JMS_URI = JMS_URI_PREFIX + NODE_UP_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * The interface up event UEI.
     */
    public static final String INTERFACE_UP_EVENT_UEI = "uei.opennms.org/nodes/interfaceUp";
    public static final String INTERFACE_UP_JMS_URI = JMS_URI_PREFIX + INTERFACE_UP_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * The SNMP interface operStatus up event UEI.
     */
    public static final String SNMP_INTERFACE_OPER_UP_EVENT_UEI = "uei.opennms.org/nodes/snmp/interfaceOperUp";
    public static final String SNMP_INTERFACE_OPER_UP_JMS_URI = JMS_URI_PREFIX + SNMP_INTERFACE_OPER_UP_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * The SNMP interface admin up event UEI.
     */
    public static final String SNMP_INTERFACE_ADMIN_UP_EVENT_UEI = "uei.opennms.org/nodes/snmp/interfaceAdminUp";
    public static final String SNMP_INTERFACE_ADMIN_UP_JMS_URI = JMS_URI_PREFIX + SNMP_INTERFACE_ADMIN_UP_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * The node regained service event UEI.
     */
    public static final String NODE_REGAINED_SERVICE_EVENT_UEI = "uei.opennms.org/nodes/nodeRegainedService";
    public static final String NODE_REGAINED_SERVICE_JMS_URI = JMS_URI_PREFIX + NODE_REGAINED_SERVICE_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * The delete service event UEI.
     */
    public static final String DELETE_SERVICE_EVENT_UEI = "uei.opennms.org/nodes/deleteService";
    public static final String DELETE_SERVICE_JMS_URI = JMS_URI_PREFIX + DELETE_SERVICE_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * The service deleted event UEI.
     */
    public static final String SERVICE_DELETED_EVENT_UEI = "uei.opennms.org/nodes/serviceDeleted";
    public static final String SERVICE_DELETED_JMS_URI = JMS_URI_PREFIX + SERVICE_DELETED_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * The interface deleted event UEI.
     */
    public static final String INTERFACE_DELETED_EVENT_UEI = "uei.opennms.org/nodes/interfaceDeleted";
    public static final String INTERFACE_DELETED_JMS_URI = JMS_URI_PREFIX + INTERFACE_DELETED_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * The node deleted event UEI.
     */
    public static final String NODE_DELETED_EVENT_UEI = "uei.opennms.org/nodes/nodeDeleted";
    public static final String NODE_DELETED_JMS_URI = JMS_URI_PREFIX + NODE_DELETED_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * The low threshold exceeded event UEI.
     */
    public static final String LOW_THRESHOLD_EVENT_UEI = "uei.opennms.org/threshold/lowThresholdExceeded";
    public static final String LOW_THRESHOLD_JMS_URI = JMS_URI_PREFIX + LOW_THRESHOLD_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * The high threshold exceeded event UEI.
     */
    public static final String HIGH_THRESHOLD_EVENT_UEI = "uei.opennms.org/threshold/highThresholdExceeded";
    public static final String HIGH_THRESHOLD_JMS_URI = JMS_URI_PREFIX + HIGH_THRESHOLD_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * The high threshold rearm event UEI.
     */
    public static final String HIGH_THRESHOLD_REARM_EVENT_UEI = "uei.opennms.org/threshold/highThresholdRearmed";
    public static final String HIGH_THRESHOLD_REARM_JMS_URI = JMS_URI_PREFIX + HIGH_THRESHOLD_REARM_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * The low threshold rearm event UEI.
     */
    public static final String LOW_THRESHOLD_REARM_EVENT_UEI = "uei.opennms.org/threshold/lowThresholdRearmed";
    public static final String LOW_THRESHOLD_REARM_JMS_URI = JMS_URI_PREFIX + LOW_THRESHOLD_REARM_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * The relative change event UEI.
     */
    public static final String RELATIVE_CHANGE_THRESHOLD_EVENT_UEI = "uei.opennms.org/threshold/relativeChangeExceeded";
    public static final String RELATIVE_CHANGE_THRESHOLD_JMS_URI = JMS_URI_PREFIX + RELATIVE_CHANGE_THRESHOLD_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * The relative change event UEI.
     */
    public static final String ABSOLUTE_CHANGE_THRESHOLD_EVENT_UEI = "uei.opennms.org/threshold/absoluteChangeExceeded";
    public static final String ABSOLUTE_CHANGE_THRESHOLD_JMS_URI = JMS_URI_PREFIX + ABSOLUTE_CHANGE_THRESHOLD_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * ThresholdEvaluatorRearmingAbsoluteChange exceeded UEI.
     */
    public static final String REARMING_ABSOLUTE_CHANGE_EXCEEDED_EVENT_UEI = "uei.opennms.org/threshold/rearmingAbsoluteChangeExceeded";
    public static final String REARMING_ABSOLUTE_CHANGE_EXCEEDED_JMS_URI = JMS_URI_PREFIX + REARMING_ABSOLUTE_CHANGE_EXCEEDED_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * ThresholdEvaluatorRearmingAbsoluteChange exceeded UEI.
     */
    public static final String REARMING_ABSOLUTE_CHANGE_REARM_EVENT_UEI = "uei.opennms.org/threshold/rearmingAbsoluteChangeRearmed";
    public static final String REARMING_ABSOLUTE_CHANGE_REARM_JMS_URI = JMS_URI_PREFIX + REARMING_ABSOLUTE_CHANGE_REARM_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * The interface index changed event.
     */
    public static final String INTERFACE_INDEX_CHANGED_EVENT_UEI = "uei.opennms.org/nodes/interfaceIndexChanged";
    public static final String INTERFACE_INDEX_CHANGED_JMS_URI = JMS_URI_PREFIX + INTERFACE_INDEX_CHANGED_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * The interface supports SNMP event...generated during capability rescan
     * when an already managed interface gains SNMP support for the first time.
     */
    public static final String INTERFACE_SUPPORTS_SNMP_EVENT_UEI = "uei.opennms.org/internal/capsd/interfaceSupportsSNMP";
    public static final String INTERFACE_SUPPORTS_SNMP_JMS_URI = JMS_URI_PREFIX + INTERFACE_SUPPORTS_SNMP_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * A service scan has discovered a duplicate IP address.
     */
    public static final String DUPLICATE_IPINTERFACE_EVENT_UEI = "uei.opennms.org/internal/capsd/duplicateIPAddress";
    public static final String DUPLICATE_IPINTERFACE_JMS_URI = JMS_URI_PREFIX + DUPLICATE_IPINTERFACE_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * The interface reparented event.
     */
    public static final String INTERFACE_REPARENTED_EVENT_UEI = "uei.opennms.org/nodes/interfaceReparented";
    public static final String INTERFACE_REPARENTED_JMS_URI = JMS_URI_PREFIX + INTERFACE_REPARENTED_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * The node info changed event.
     */
    public static final String NODE_INFO_CHANGED_EVENT_UEI = "uei.opennms.org/nodes/nodeInfoChanged";
    public static final String NODE_INFO_CHANGED_JMS_URI = JMS_URI_PREFIX + NODE_INFO_CHANGED_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * The interface IP host name changed event.
     */
    public static final String INTERFACE_IP_HOSTNAME_CHANGED_EVENT_UEI = "uei.opennms.org/nodes/interfaceIPHostNameChanged";
    public static final String INTERFACE_IP_HOSTNAME_CHANGED_JMS_URI = JMS_URI_PREFIX + INTERFACE_IP_HOSTNAME_CHANGED_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * The node label changed event.
     */
    public static final String NODE_LABEL_CHANGED_EVENT_UEI = "uei.opennms.org/nodes/nodeLabelChanged";
    public static final String NODE_LABEL_CHANGED_JMS_URI = JMS_URI_PREFIX + NODE_LABEL_CHANGED_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * The node label source changed event.
     */
    public static final String NODE_LABEL_SOURCE_CHANGED_EVENT_UEI = "uei.opennms.org/nodes/nodeLabelSourceChanged";
    public static final String NODE_LABEL_SOURCE_CHANGED_JMS_URI = JMS_URI_PREFIX + NODE_LABEL_SOURCE_CHANGED_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * The node deleted event UEI.
     */
    public static final String DUP_NODE_DELETED_EVENT_UEI = "uei.opennms.org/nodes/duplicateNodeDeleted";
    public static final String DUP_NODE_DELETED_JMS_URI = JMS_URI_PREFIX + DUP_NODE_DELETED_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * The primary SNMP interface changed event.
     */
    public static final String PRIMARY_SNMP_INTERFACE_CHANGED_EVENT_UEI = "uei.opennms.org/nodes/primarySnmpInterfaceChanged";
    public static final String PRIMARY_SNMP_INTERFACE_CHANGED_JMS_URI = JMS_URI_PREFIX + PRIMARY_SNMP_INTERFACE_CHANGED_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * The reinitialize primary SNMP interface event.
     */
    public static final String REINITIALIZE_PRIMARY_SNMP_INTERFACE_EVENT_UEI = "uei.opennms.org/nodes/reinitializePrimarySnmpInterface";
    public static final String REINITIALIZE_PRIMARY_SNMP_INTERFACE_JMS_URI = JMS_URI_PREFIX + REINITIALIZE_PRIMARY_SNMP_INTERFACE_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * The configure SNMP event.
     */
    public static final String CONFIGURE_SNMP_EVENT_UEI = "uei.opennms.org/internal/configureSNMP";
    public static final String CONFIGURE_SNMP_JMS_URI = JMS_URI_PREFIX + CONFIGURE_SNMP_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * Collection failed.
     */
    public static final String DATA_COLLECTION_FAILED_EVENT_UEI = "uei.opennms.org/nodes/dataCollectionFailed";
    public static final String DATA_COLLECTION_FAILED_JMS_URI = JMS_URI_PREFIX + DATA_COLLECTION_FAILED_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * Collection succeeded.
     */
    public static final String DATA_COLLECTION_SUCCEEDED_EVENT_UEI = "uei.opennms.org/nodes/dataCollectionSucceeded";
    public static final String DATA_COLLECTION_SUCCEEDED_JMS_URI = JMS_URI_PREFIX + DATA_COLLECTION_SUCCEEDED_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * Thresholding failed.
     */
    public static final String THRESHOLDING_FAILED_EVENT_UEI = "uei.opennms.org/nodes/thresholdingFailed";
    public static final String THRESHOLDING_FAILED_JMS_URI = JMS_URI_PREFIX + THRESHOLDING_FAILED_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * Thresholding succeeded.
     */
    public static final String THRESHOLDING_SUCCEEDED_EVENT_UEI = "uei.opennms.org/nodes/thresholdingSucceeded";
    public static final String THRESHOLDING_SUCCEEDED_JMS_URI = JMS_URI_PREFIX + THRESHOLDING_SUCCEEDED_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * The force interface rescan event UEI
     */
    public static final String FORCE_RESCAN_EVENT_UEI = "uei.opennms.org/internal/capsd/forceRescan";
    public static final String FORCE_RESCAN_JMS_URI = JMS_URI_PREFIX + FORCE_RESCAN_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * The suspend polling service event UEI
     */
    public static final String SUSPEND_POLLING_SERVICE_EVENT_UEI = "uei.opennms.org/internal/poller/suspendPollingService";
    public static final String SUSPEND_POLLING_SERVICE_JMS_URI = JMS_URI_PREFIX + SUSPEND_POLLING_SERVICE_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * The resume polling service event UEI.
     */
    public static final String RESUME_POLLING_SERVICE_EVENT_UEI = "uei.opennms.org/internal/poller/resumePollingService";
    public static final String RESUME_POLLING_SERVICE_JMS_URI = JMS_URI_PREFIX + RESUME_POLLING_SERVICE_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * The SNMP conflicts with db UEI.
     */
    public static final String SNMP_CONFLICTS_WITH_DB_EVENT_UEI = "uei.opennms.org/internal/capsd/snmpConflictsWithDb";
    public static final String SNMP_CONFLICTS_WITH_DB_JMS_URI = JMS_URI_PREFIX + SNMP_CONFLICTS_WITH_DB_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * The rescan completed UEI.
     */
    public static final String RESCAN_COMPLETED_EVENT_UEI = "uei.opennms.org/internal/capsd/rescanCompleted";
    public static final String RESCAN_COMPLETED_JMS_URI = JMS_URI_PREFIX + RESCAN_COMPLETED_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * The suspect scan completed UEI.
     */
    public static final String SUSPECT_SCAN_COMPLETED_EVENT_UEI = "uei.opennms.org/internal/capsd/suspectScanCompleted";
    public static final String SUSPECT_SCAN_COMPLETED_JMS_URI = JMS_URI_PREFIX + SUSPECT_SCAN_COMPLETED_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * The RTC subscribe event.
     */
    public static final String RTC_SUBSCRIBE_EVENT_UEI = "uei.opennms.org/internal/rtc/subscribe";
    public static final String RTC_SUBSCRIBE_JMS_URI = JMS_URI_PREFIX + RTC_SUBSCRIBE_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * The RTC unsubscribe event.
     */
    public static final String RTC_UNSUBSCRIBE_EVENT_UEI = "uei.opennms.org/internal/rtc/unsubscribe";
    public static final String RTC_UNSUBSCRIBE_JMS_URI = JMS_URI_PREFIX + RTC_UNSUBSCRIBE_EVENT_UEI + JMS_URI_SUFFIX;

    
    /**
     * An event used by queued to indicate that data for certain rrds should be immediately flushed to the disk.
     */
    public static final String PROMOTE_QUEUE_DATA_UEI = "uei.opennms.org/internal/promoteQueueData";
    public static final String PROMOTE_QUEUE_DATA_JMS_URI = JMS_URI_PREFIX + PROMOTE_QUEUE_DATA_UEI + JMS_URI_SUFFIX;

    /**
     * A service poll returned an unknown status (due to a problem getting poll
     * information).
     */
    public static final String SERVICE_STATUS_UNKNOWN = "uei.opennms.org/internal/unknownServiceStatus";
    public static final String SERVICE_STATUS_UNKNOWN_JMS_URI = JMS_URI_PREFIX + SERVICE_STATUS_UNKNOWN + JMS_URI_SUFFIX;

    /**
     * Notification without users event.
     */
    public static final String NOTIFICATION_WITHOUT_USERS = "uei.opennms.org/internal/notificationWithoutUsers";
    public static final String NOTIFICATION_WITHOUT_USERS_JMS_URI = JMS_URI_PREFIX + NOTIFICATION_WITHOUT_USERS + JMS_URI_SUFFIX;

    /**
     * A vulnerability scan on a specific interface was initiated by the user
     * via the web UI.
     */
    public static final String SPECIFIC_VULN_SCAN_EVENT_UEI = "uei.opennms.org/vulnscand/specificVulnerabilityScan";
    public static final String SPECIFIC_VULN_SCAN_JMS_URI = JMS_URI_PREFIX + SPECIFIC_VULN_SCAN_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * Demand poll service event ui.
     */
    public static final String DEMAND_POLL_SERVICE_EVENT_UEI = "uei.opennms.org/internal/demandPollService";
    public static final String DEMAND_POLL_SERVICE_JMS_URI = JMS_URI_PREFIX + DEMAND_POLL_SERVICE_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * An event to signal that a user has changed asset information via the web
     * UI.
     */
    public static final String ASSET_INFO_CHANGED_EVENT_UEI = "uei.opennms.org/nodes/assetInfoChanged";
    public static final String ASSET_INFO_CHANGED_JMS_URI = JMS_URI_PREFIX + ASSET_INFO_CHANGED_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * The scheduled-outages configuration was changed by the user via the web UI (or manually, for that matter).
     */
    public static final String SCHEDOUTAGES_CHANGED_EVENT_UEI = "uei.opennms.org/internal/schedOutagesChanged";
    public static final String SCHEDOUTAGES_CHANGED_JMS_URI = JMS_URI_PREFIX + SCHEDOUTAGES_CHANGED_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * The threshold config was changed by the user via the web UI, or manually.
     */
    public static final String THRESHOLDCONFIG_CHANGED_EVENT_UEI = "uei.opennms.org/internal/thresholdConfigChange";
    public static final String THRESHOLDCONFIG_CHANGED_JMS_URI = JMS_URI_PREFIX + THRESHOLDCONFIG_CHANGED_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * The event config was changed by the user via the web UI, or manually, and should be reloaded.
     */
    public static final String EVENTSCONFIG_CHANGED_EVENT_UEI = "uei.opennms.org/internal/eventsConfigChange";
    public static final String EVENTSCONFIG_CHANGED_JMS_URI = JMS_URI_PREFIX + EVENTSCONFIG_CHANGED_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * The Snmp Poller config was changed by the user via the web UI, or manually, and should be reloaded.
     */
    public static final String SNMPPOLLERCONFIG_CHANGED_EVENT_UEI = "uei.opennms.org/internal/reloadSnmpPollerConfig";
    public static final String SNMPPOLLERCONFIG_CHANGED_JMS_URI = JMS_URI_PREFIX + SNMPPOLLERCONFIG_CHANGED_EVENT_UEI + JMS_URI_SUFFIX;

    /**
     * Reload Vacuumd configuration UEI.
     */
    public static final String RELOAD_VACUUMD_CONFIG_UEI = "uei.opennms.org/internal/reloadVacuumdConfig";
    public static final String RELOAD_VACUUMD_CONFIG_JMS_URI = JMS_URI_PREFIX + RELOAD_VACUUMD_CONFIG_UEI + JMS_URI_SUFFIX;

    /**
     * Reload Daemon configuration UEI.
     */
    public static final String RELOAD_DAEMON_CONFIG_UEI = "uei.opennms.org/internal/reloadDaemonConfig";
    public static final String RELOAD_DAEMON_CONFIG_JMS_URI = JMS_URI_PREFIX + RELOAD_DAEMON_CONFIG_UEI + JMS_URI_SUFFIX;
    /** Constant <code>RELOAD_DAEMON_CONFIG_FAILED_UEI="uei.opennms.org/internal/reloadDaemonCo"{trunked}</code> */
    public static final String RELOAD_DAEMON_CONFIG_FAILED_UEI = "uei.opennms.org/internal/reloadDaemonConfigFailed";
    public static final String RELOAD_DAEMON_CONFIG_FAILED_JMS_URI = JMS_URI_PREFIX + RELOAD_DAEMON_CONFIG_FAILED_UEI + JMS_URI_SUFFIX;
    /** Constant <code>RELOAD_DAEMON_CONFIG_SUCCESSFUL_UEI="uei.opennms.org/internal/reloadDaemonCo"{trunked}</code> */
    public static final String RELOAD_DAEMON_CONFIG_SUCCESSFUL_UEI = "uei.opennms.org/internal/reloadDaemonConfigSuccessful";
    public static final String RELOAD_DAEMON_CONFIG_SUCCESSFUL_JMS_URI = JMS_URI_PREFIX + RELOAD_DAEMON_CONFIG_SUCCESSFUL_UEI + JMS_URI_SUFFIX;
    /** Constant <code>PARM_DAEMON_NAME="daemonName"</code> */
    public static final String PARM_DAEMON_NAME = "daemonName";
    /** Constant <code>PARM_CONFIG_FILE_NAME="configFile"</code> */
    public static final String PARM_CONFIG_FILE_NAME = "configFile";

    /*
     * Reportd UEIs.
     */
    
    public static final String REPORTD_RUN_REPORT = "uei.opennms.org/reportd/runReport";
    public static final String REPORTD_RUN_REPORT_JMS_URI = JMS_URI_PREFIX + REPORTD_RUN_REPORT + JMS_URI_SUFFIX;
    /** Constant <code>PARM_REPORT_NAME="reportName"</code> */
    public static final String PARM_REPORT_NAME = "reportName";
    public static final String REPORT_RUN_FAILED_UEI = "uei.opennms.org/reportd/reportRunFailed";
    public static final String REPORT_RUN_FAILED_JMS_URI = JMS_URI_PREFIX + REPORT_RUN_FAILED_UEI + JMS_URI_SUFFIX;
    public static final String REPORT_DELIVERY_FAILED_UEI = "uei.opennms.org/reportd/reportDeliveryFailed";
    public static final String REPORT_DELIVERY_FAILED_JMS_URI = JMS_URI_PREFIX + REPORT_DELIVERY_FAILED_UEI + JMS_URI_SUFFIX;
    
    /** Constant <code>REMOTE_NODE_LOST_SERVICE_UEI="uei.opennms.org/remote/nodes/nodeLostSe"{trunked}</code> */
    public static final String REMOTE_NODE_LOST_SERVICE_UEI = "uei.opennms.org/remote/nodes/nodeLostService";
    public static final String REMOTE_NODE_LOST_SERVICE_JMS_URI = JMS_URI_PREFIX + REMOTE_NODE_LOST_SERVICE_UEI + JMS_URI_SUFFIX;
    /** Constant <code>REMOTE_NODE_REGAINED_SERVICE_UEI="uei.opennms.org/remote/nodes/nodeRegain"{trunked}</code> */
    public static final String REMOTE_NODE_REGAINED_SERVICE_UEI = "uei.opennms.org/remote/nodes/nodeRegainedService";
    public static final String REMOTE_NODE_REGAINED_SERVICE_JMS_URI = JMS_URI_PREFIX + REMOTE_NODE_REGAINED_SERVICE_UEI + JMS_URI_SUFFIX;
    /** Constant <code>LOCATION_MONITOR_REGISTERED_UEI="uei.opennms.org/remote/locationMonitorR"{trunked}</code> */
    public static final String LOCATION_MONITOR_REGISTERED_UEI="uei.opennms.org/remote/locationMonitorRegistered";
    public static final String LOCATION_MONITOR_REGISTERED_JMS_URI = JMS_URI_PREFIX + LOCATION_MONITOR_REGISTERED_UEI + JMS_URI_SUFFIX;
    /** Constant <code>LOCATION_MONITOR_STARTED_UEI="uei.opennms.org/remote/locationMonitorS"{trunked}</code> */
    public static final String LOCATION_MONITOR_STARTED_UEI="uei.opennms.org/remote/locationMonitorStarted";
    public static final String LOCATION_MONITOR_STARTED_JMS_URI = JMS_URI_PREFIX + LOCATION_MONITOR_STARTED_UEI + JMS_URI_SUFFIX;
    /** Constant <code>LOCATION_MONITOR_STOPPED_UEI="uei.opennms.org/remote/locationMonitorS"{trunked}</code> */
    public static final String LOCATION_MONITOR_STOPPED_UEI="uei.opennms.org/remote/locationMonitorStopped";
    public static final String LOCATION_MONITOR_STOPPED_JMS_URI = JMS_URI_PREFIX + LOCATION_MONITOR_STOPPED_UEI + JMS_URI_SUFFIX;
    /** Constant <code>LOCATION_MONITOR_PAUSED_UEI="uei.opennms.org/remote/locationMonitorP"{trunked}</code> */
    public static final String LOCATION_MONITOR_PAUSED_UEI="uei.opennms.org/remote/locationMonitorPaused";
    public static final String LOCATION_MONITOR_PAUSED_JMS_URI = JMS_URI_PREFIX + LOCATION_MONITOR_PAUSED_UEI + JMS_URI_SUFFIX;
    /** Constant <code>LOCATION_MONITOR_DISCONNECTED_UEI="uei.opennms.org/remote/locationMonitorD"{trunked}</code> */
    public static final String LOCATION_MONITOR_DISCONNECTED_UEI="uei.opennms.org/remote/locationMonitorDisconnected";
    public static final String LOCATION_MONITOR_DISCONNECTED_JMS_URI = JMS_URI_PREFIX + LOCATION_MONITOR_DISCONNECTED_UEI + JMS_URI_SUFFIX;
    /** Constant <code>LOCATION_MONITOR_RECONNECTED_UEI="uei.opennms.org/remote/locationMonitorR"{trunked}</code> */
    public static final String LOCATION_MONITOR_RECONNECTED_UEI="uei.opennms.org/remote/locationMonitorReconnected";
    public static final String LOCATION_MONITOR_RECONNECTED_JMS_URI = JMS_URI_PREFIX + LOCATION_MONITOR_RECONNECTED_UEI + JMS_URI_SUFFIX;
    /** Constant <code>LOCATION_MONITOR_CONFIG_CHANGE_DETECTED_UEI="uei.opennms.org/remote/configurationCha"{trunked}</code> */
    public static final String LOCATION_MONITOR_CONFIG_CHANGE_DETECTED_UEI="uei.opennms.org/remote/configurationChangeDetected";
    public static final String LOCATION_MONITOR_CONFIG_CHANGE_DETECTED_JMS_URI = JMS_URI_PREFIX + LOCATION_MONITOR_CONFIG_CHANGE_DETECTED_UEI + JMS_URI_SUFFIX;
    public static final String LOCATION_MONITOR_CONNECTION_ADDRESS_CHANGED_UEI="uei.opennms.org/remote/locationMonitorConnectionAddressChanged";
    public static final String LOCATION_MONITOR_CONNECTION_ADDRESS_CHANGED_JMS_URI = JMS_URI_PREFIX + LOCATION_MONITOR_CONNECTION_ADDRESS_CHANGED_UEI + JMS_URI_SUFFIX;

    public static final String REMOTE_SUCCESSFUL_SCAN_REPORT_UEI="uei.opennms.org/remote/successfulScanReport";
    public static final String REMOTE_SUCCESSFUL_SCAN_REPORT_JMS_URI = JMS_URI_PREFIX + REMOTE_SUCCESSFUL_SCAN_REPORT_UEI + JMS_URI_SUFFIX;
    public static final String REMOTE_UNSUCCESSFUL_SCAN_REPORT_UEI="uei.opennms.org/remote/unsuccessfulScanReport";
    public static final String REMOTE_UNSUCCESSFUL_SCAN_REPORT_JMS_URI = JMS_URI_PREFIX + REMOTE_UNSUCCESSFUL_SCAN_REPORT_UEI + JMS_URI_SUFFIX;

    /** Constant <code>RELOAD_IMPORT_UEI="uei.opennms.org/internal/importer/reloa"{trunked}</code> */
    public static final String RELOAD_IMPORT_UEI = "uei.opennms.org/internal/importer/reloadImport";
    public static final String RELOAD_IMPORT_JMS_URI = JMS_URI_PREFIX + RELOAD_IMPORT_UEI + JMS_URI_SUFFIX;
    /** Constant <code>IMPORT_STARTED_UEI="uei.opennms.org/internal/importer/impor"{trunked}</code> */
    public static final String IMPORT_STARTED_UEI = "uei.opennms.org/internal/importer/importStarted";
    public static final String IMPORT_STARTED_JMS_URI = JMS_URI_PREFIX + IMPORT_STARTED_UEI + JMS_URI_SUFFIX;
    /** Constant <code>IMPORT_SUCCESSFUL_UEI="uei.opennms.org/internal/importer/impor"{trunked}</code> */
    public static final String IMPORT_SUCCESSFUL_UEI = "uei.opennms.org/internal/importer/importSuccessful";
    public static final String IMPORT_SUCCESSFUL_JMS_URI = JMS_URI_PREFIX + IMPORT_SUCCESSFUL_UEI + JMS_URI_SUFFIX;
    /** Constant <code>IMPORT_FAILED_UEI="uei.opennms.org/internal/importer/impor"{trunked}</code> */
    public static final String IMPORT_FAILED_UEI = "uei.opennms.org/internal/importer/importFailed";
    public static final String IMPORT_FAILED_JMS_URI = JMS_URI_PREFIX + IMPORT_FAILED_UEI + JMS_URI_SUFFIX;
    /** Constant <code>PROVISIONING_ADAPTER_FAILED="uei.opennms.org/provisioner/provisionin"{trunked}</code> */
    public static final String PROVISIONING_ADAPTER_FAILED = "uei.opennms.org/provisioner/provisioningAdapterFailed";
    public static final String JMS_URI = JMS_URI_PREFIX + PROVISIONING_ADAPTER_FAILED + JMS_URI_SUFFIX;

    /** Constant <code>PROVISION_SCAN_COMPLETE_UEI="uei.opennms.org/internal/provisiond/nod"{trunked}</code> */
    public static final String PROVISION_SCAN_COMPLETE_UEI="uei.opennms.org/internal/provisiond/nodeScanCompleted";
    public static final String PROVISION_SCAN_COMPLETE_JMS_URI = JMS_URI_PREFIX + PROVISION_SCAN_COMPLETE_UEI + JMS_URI_SUFFIX;
    /** Constant <code>PROVISION_SCAN_ABORTED_UEI="uei.opennms.org/internal/provisiond/nod"{trunked}</code> */
    public static final String PROVISION_SCAN_ABORTED_UEI="uei.opennms.org/internal/provisiond/nodeScanAborted";
    public static final String PROVISION_SCAN_ABORTED_JMS_URI = JMS_URI_PREFIX + PROVISION_SCAN_ABORTED_UEI + JMS_URI_SUFFIX;
    
    /** Constant <code>PARM_FAILURE_MESSAGE="failureMessage"</code> */
    public static final String PARM_FAILURE_MESSAGE = "failureMessage";

    /** Constant <code>PARM_IMPORT_STATS="importStats"</code> */
    public static final String PARM_IMPORT_STATS = "importStats";

    /** Constant <code>PARM_IMPORT_RESOURCE="importResource"</code> */
    public static final String PARM_IMPORT_RESOURCE = "importResource";

    public static final String PARM_IMPORT_RESCAN_EXISTING = "importRescanExisting";
    
    /** Constant <code>PARM_ALARM_ID="alarmId"</code> */
    public static final String PARM_ALARM_ID = "alarmId";
    /** Constant <code>PARM_ALARM_UEI="alarmUei"</code> */
    public static final String PARM_ALARM_UEI = "alarmUei";
    /** Constant <code>PARM_TROUBLE_TICKET="troubleTicket"</code> */
    public static final String PARM_TROUBLE_TICKET = "troubleTicket";

    /** Constant <code>TROUBLETICKET_CREATE_UEI="uei.opennms.org/troubleTicket/create"</code> */
    public static final String TROUBLETICKET_CREATE_UEI = "uei.opennms.org/troubleTicket/create";
    public static final String TROUBLETICKET_CREATE_JMS_URI = JMS_URI_PREFIX + TROUBLETICKET_CREATE_UEI + JMS_URI_SUFFIX;
    /** Constant <code>TROUBLETICKET_UPDATE_UEI="uei.opennms.org/troubleTicket/update"</code> */
    public static final String TROUBLETICKET_UPDATE_UEI = "uei.opennms.org/troubleTicket/update";
    public static final String TROUBLETICKET_UPDATE_JMS_URI = JMS_URI_PREFIX + TROUBLETICKET_UPDATE_UEI + JMS_URI_SUFFIX;
    /** Constant <code>TROUBLETICKET_CLOSE_UEI="uei.opennms.org/troubleTicket/close"</code> */
    public static final String TROUBLETICKET_CLOSE_UEI = "uei.opennms.org/troubleTicket/close";
    public static final String TROUBLETICKET_CLOSE_JMS_URI = JMS_URI_PREFIX + TROUBLETICKET_CLOSE_UEI + JMS_URI_SUFFIX;
    /** Constant <code>TROUBLETICKET_CANCEL_UEI="uei.opennms.org/troubleTicket/cancel"</code> */
    public static final String TROUBLETICKET_CANCEL_UEI = "uei.opennms.org/troubleTicket/cancel";
    public static final String TROUBLETICKET_CANCEL_JMS_URI = JMS_URI_PREFIX + TROUBLETICKET_CANCEL_UEI + JMS_URI_SUFFIX;
    
    /** Constant <code>TL1_AUTONOMOUS_MESSAGE_UEI="uei.opennms.org/api/tl1d/message/autono"{trunked}</code> */
    public static final String TL1_AUTONOMOUS_MESSAGE_UEI = "uei.opennms.org/api/tl1d/message/autonomous";
    public static final String TL1_AUTONOMOUS_MESSAGE_JMS_URI = JMS_URI_PREFIX + TL1_AUTONOMOUS_MESSAGE_UEI + JMS_URI_SUFFIX;

    /** Constant <code>RANCID_DOWNLOAD_SUCCESS_UEI="uei.opennms.org/standard/rancid/traps/r"{trunked}</code> */
    public static final String RANCID_DOWNLOAD_SUCCESS_UEI="uei.opennms.org/standard/rancid/traps/rancidTrapDownloadSuccess";
    public static final String RANCID_DOWNLOAD_SUCCESS_JMS_URI = JMS_URI_PREFIX + RANCID_DOWNLOAD_SUCCESS_UEI + JMS_URI_SUFFIX;
    /** Constant <code>RANCID_DOWNLOAD_FAILURE_UEI="uei.opennms.org/standard/rancid/traps/r"{trunked}</code> */
    public static final String RANCID_DOWNLOAD_FAILURE_UEI="uei.opennms.org/standard/rancid/traps/rancidTrapDownloadFailure";
    public static final String RANCID_DOWNLOAD_FAILURE_JMS_URI = JMS_URI_PREFIX + RANCID_DOWNLOAD_FAILURE_UEI + JMS_URI_SUFFIX;
    /** Constant <code>RANCID_GROUP_PROCESSING_COMPLETED_UEI="uei.opennms.org/standard/rancid/traps/r"{trunked}</code> */
    public static final String RANCID_GROUP_PROCESSING_COMPLETED_UEI="uei.opennms.org/standard/rancid/traps/rancidTrapGroupProcessingCompleted";
    public static final String RANCID_GROUP_PROCESSING_COMPLETED_JMS_URI = JMS_URI_PREFIX + RANCID_GROUP_PROCESSING_COMPLETED_UEI + JMS_URI_SUFFIX;

    /** Constant <code>DATA_LINK_FAILED_EVENT_UEI="uei.opennms.org/internal/linkd/dataLink"{trunked}</code> */
    public static final String DATA_LINK_FAILED_EVENT_UEI = "uei.opennms.org/internal/linkd/dataLinkFailed";
    public static final String DATA_LINK_FAILED_JMS_URI = JMS_URI_PREFIX + DATA_LINK_FAILED_EVENT_UEI + JMS_URI_SUFFIX;
    /** Constant <code>DATA_LINK_RESTORED_EVENT_UEI="uei.opennms.org/internal/linkd/dataLink"{trunked}</code> */
    public static final String DATA_LINK_RESTORED_EVENT_UEI = "uei.opennms.org/internal/linkd/dataLinkRestored";
    public static final String DATA_LINK_RESTORED_JMS_URI = JMS_URI_PREFIX + DATA_LINK_RESTORED_EVENT_UEI + JMS_URI_SUFFIX;
    /** Constant <code>DATA_LINK_UNMANAGED_EVENT_UEI="uei.opennms.org/internal/linkd/dataLink"{trunked}</code> */
    public static final String DATA_LINK_UNMANAGED_EVENT_UEI = "uei.opennms.org/internal/linkd/dataLinkUnmanaged";
    public static final String DATA_LINK_UNMANAGED_JMS_URI = JMS_URI_PREFIX + DATA_LINK_UNMANAGED_EVENT_UEI + JMS_URI_SUFFIX;

    /** Constant <code>TOPOLOGY_LINK_DOWN_EVENT_UEI="uei.opennms.org/internal/topology/linkDown"{trunked}</code> */
    public static final String TOPOLOGY_LINK_DOWN_EVENT_UEI = "uei.opennms.org/internal/topology/linkDown";
    public static final String TOPOLOGY_LINK_DOWN_JMS_URI = JMS_URI_PREFIX + TOPOLOGY_LINK_DOWN_EVENT_UEI + JMS_URI_SUFFIX;
    /** Constant <code>TOPOLOGY_LINK_UP_EVENT_UEI="uei.opennms.org/internal/topology/linkUp"{trunked}</code> */
    public static final String TOPOLOGY_LINK_UP_EVENT_UEI = "uei.opennms.org/internal/topology/linkUp";
    public static final String TOPOLOGY_LINK_UP_JMS_URI = JMS_URI_PREFIX + TOPOLOGY_LINK_UP_EVENT_UEI + JMS_URI_SUFFIX;

    public static final String HARDWARE_INVENTORY_FAILED_UEI = "uei.opennms.org/internal/discovery/hardwareInventoryFailed";
    public static final String HARDWARE_INVENTORY_FAILED_JMS_URI = JMS_URI_PREFIX + HARDWARE_INVENTORY_FAILED_UEI + JMS_URI_SUFFIX;
    public static final String HARDWARE_INVENTORY_SUCCESSFUL_UEI = "uei.opennms.org/internal/discovery/hardwareInventorySuccessful";
    public static final String HARDWARE_INVENTORY_SUCCESSFUL_JMS_URI = JMS_URI_PREFIX + HARDWARE_INVENTORY_SUCCESSFUL_UEI + JMS_URI_SUFFIX;

    public static final String KSC_REPORT_UPDATED_UEI = "uei.opennms.org/internal/kscReportUpdated";
    public static final String KSC_REPORT_UPDATED_JMS_URI = JMS_URI_PREFIX + KSC_REPORT_UPDATED_UEI + JMS_URI_SUFFIX;
    public static final String PARAM_REPORT_TITLE = "reportTitle";
    public static final String PARAM_REPORT_GRAPH_COUNT = "graphCount";


    public static final String MONITORING_SYSTEM_ADDED_UEI = "uei.opennms.org/internal/monitoringSystemAdded";
    public static final String MONITORING_SYSTEM_LOCATION_CHANGED_UEI = "uei.opennms.org/internal/monitoringSystemLocationChanged";
    public static final String MONITORING_SYSTEM_DELETED_UEI = "uei.opennms.org/internal/monitoringSystemDeleted";
    public static final String PARAM_MONITORING_SYSTEM_TYPE = "monitoringSystemType";
    public static final String PARAM_MONITORING_SYSTEM_ID = "monitoringSystemId";
    public static final String PARAM_MONITORING_SYSTEM_LOCATION = "monitoringSystemLocation";
    public static final String PARAM_MONITORING_SYSTEM_PREV_LOCATION = "monitoringSystemPreviousLocation";
    //
    // end eventUEIs
    //

    //
    // Various event parms sent
    //
    /**
     * The criticalPathIp used in determining if a node down event is
     * due to a path outage.
     */
    public static final String PARM_CRITICAL_PATH_IP = "criticalPathIp";

    /**
     * The criticalPathServiceName used in determining if a node down event is
     * due to a path outage.
     */
    public static final String PARM_CRITICAL_PATH_SVC = "criticalPathServiceName";

    /**
     * This parameter is set to true if a critical path outage has resulted in the
     * suppression of a notification.
     */
    public static final String PARM_CRITICAL_PATH_NOTICE_SUPRESSED = "noticeSupressed";
    
    /**
     * This parameter is set to indicate the id of the demandPoll object to store the results
     * of a demandPoll in.
     */
    public static final String PARM_DEMAND_POLL_ID = "demandPollId";

    /**
     * The nodeSysName from the node table when sent as an event parm.
     */
    public static final String PARM_NODE_SYSNAME = "nodesysname";

    /**
     * The nodeSysDescription from the node table when sent as an event parm.
     */
    public static final String PARM_NODE_SYSDESCRIPTION = "nodesysdescription";

    /**
     * The nodeSysOid from the node table when sent as an event parm.
     */
    public static final String PARM_NODE_SYSOID = "nodesysoid";

    /**
     * The nodeSysLocation from the node table when sent as an event parm.
     */
    public static final String PARM_NODE_SYSLOCATION = "nodesyslocation";

    /**
     * The nodeSysContact from the node table when sent as an event parm.
     */
    public static final String PARM_NODE_SYSCONTACT = "nodesyscontact";

    /**
     * The ipHostName from the ipinterface table when sent as an event parm.
     */
    public static final String PARM_IP_HOSTNAME = "iphostname";

    /**
     * The original ipHostName from the ipinterface table when sent as an event
     * parm.
     */
    public static final String PARM_OLD_IP_HOSTNAME = "oldiphostname";

    /**
     * Name of the method of discovery when sent as an event parm.
     */
    public static final String PARM_METHOD = "method";

    /**
     * The interface sent as a parm of an event.
     */
    public static final String PARM_INTERFACE = "interface";

    /**
     * The action sent as a parm of an event.
     */
    public static final String PARM_ACTION = "action";

    /**
     * The DPName sent as a parm of an event.
     */
    public static final String PARM_DPNAME = "dpName";

    /**
     * The old nodeid sent as a parm of the 'interfaceReparented' event.
     */
    public static final String PARM_OLD_NODEID = "oldNodeID";

    /**
     * The new nodeid sent as a parm of the 'interfaceReparented' event.
     */
    public static final String PARM_NEW_NODEID = "newNodeID";

    /**
     * The old ifIndex value sent as a parm of the 'interfaceIndexChanged' event.
     */
    public static final String PARM_OLD_IFINDEX = "oldIfIndex";

    /**
     * The new ifIndex value sent as a parm of the 'interfaceIndexChanged' event.
     */
    public static final String PARM_NEW_IFINDEX = "newIfIndex";

    /**
     * The nodeLabel from the node table when sent as an event parm.
     */
    public static final String PARM_NODE_LABEL = "nodelabel";

    /**
     * The nodeLabelSource from the node table when sent as an event parm.
     */
    public static final String PARM_NODE_LABEL_SOURCE = "nodelabelsource";

    /**
     * The oldNodeLabel sent as a parm of an event.
     */
    public static final String PARM_OLD_NODE_LABEL = "oldNodeLabel";

    /**
     * The oldNodeLabelSource sent as a parm of an event.
     */
    public static final String PARM_OLD_NODE_LABEL_SOURCE = "oldNodeLabelSource";

    /**
     * The newNodeLabel sent as a parm of an event.
     */
    public static final String PARM_NEW_NODE_LABEL = "newNodeLabel";

    /**
     * The newNodeLabelSource sent as a parm of an event.
     */
    public static final String PARM_NEW_NODE_LABEL_SOURCE = "newNodeLabelSource";

    /**
     * The nodeNetbiosName field from the node table when sent as an event parm.
     */
    public static final String PARM_NODE_NETBIOS_NAME = "nodenetbiosname";

    /**
     * The nodeDomainName field from the node table when sent as an event parm.
     */
    public static final String PARM_NODE_DOMAIN_NAME = "nodedomainname";

    /**
     * The operatingSystem field from the node table when sent as an event parm.
     */
    public static final String PARM_NODE_OPERATING_SYSTEM = "nodeoperatingsystem";

    /**
     * The old value of the primarySnmpInterface field of the ipInterface table
     * when sent as an event parm.
     */
    public static final String PARM_OLD_PRIMARY_SNMP_ADDRESS = "oldPrimarySnmpAddress";

    /**
     * The new value of the primarySnmpInterface field of the ipInterface table
     * when sent as an event parm.
     */
    public static final String PARM_NEW_PRIMARY_SNMP_ADDRESS = "newPrimarySnmpAddress";

    /**
     * The parameter name for the "SNMP security name string" when sent as an event parameter.
     */
    public static final String PARM_SNMP_SECURITY_NAME = "securityName";
    
    /**
     * The parameter name for the "SNMP security level string" when sent as an event parameter.
     */
    public static final String PARM_SNMP_SECURITY_LEVEL = "securityLevel";
    
    /**
     * The parameter name for the "SNMP auth passphrase string" when sent as an event parameter.
     */
    public static final String PARM_SNMP_AUTH_PASSPHRASE = "authPassphrase";
    
    /**
     * The parameter name for the "SNMP auth protocol string" when sent as an event parameter.
     */
    public static final String PARM_SNMP_AUTH_PROTOCOL = "authProtocol";
    
    /**
     * The parameter name for the "SNMP engine id string" when sent as an event parameter.
     */
    public static final String PARM_SNMP_ENGINE_ID = "engineId";
    
    /**
     * The parameter name for the "SNMP context engine id string" when sent as an event parameter.
     */
    public static final String PARM_SNMP_CONTEXT_ENGINE_ID = "contextEngineId";
    
    /**
     * The parameter name for the "SNMP enterprise id string" when sent as an event parameter.
     */
    public static final String PARM_SNMP_ENTERPRISE_ID = "enterpriseId";
    
    /**
     * The parameter name for the "SNMP context name string" when sent as an event parameter.
     */
    public static final String PARM_SNMP_CONTEXT_NAME = "contextName"; 
    
    /**
     * The parameter name for the "SNMP privacy passphrase string" when sent as an event parameter.
     */
    public static final String PARM_SNMP_PRIVACY_PASSPHRASE = "privPassphrase";
    
    /**
     * The parameter name for the "SNMP privacy protocol string" when sent as an event parameter.
     */
    public static final String PARM_SNMP_PRIVACY_PROTOCOL = "privProtocol";
    
    /**
     * The parameter name for the "SNMP max repetitions string" when sent as an event parameter.
     */
    public static final String PARM_SNMP_MAX_REPETITIONS = "maxRepetitions";
    
    /**
     * The parameter name for the "SNMP max request size string" when sent as an event parameter.
     */
    public static final String PARM_SNMP_MAX_REQUEST_SIZE = "maxRequestSize";
    
    /**
     * The parameter name for the "SNMP max vars per pdu string" when sent as an event parameter.
     */
    public static final String PARM_SNMP_MAX_VARS_PER_PDU = "maxVarsPerPdu";
    
    /**
     * The parameter name for the "SNMP proxy host string" when sent as an event parameter.
     */
    public static final String PARM_SNMP_PROXY_HOST= "proxyHost";
    
    /**
     * The first IP address in a range of IP addresses when sent as an event
     * parm.
     */
    public static final String PARM_FIRST_IP_ADDRESS = "firstIPAddress";

    /**
     * The last IP address in a range of IP addresses when sent as an event
     * parm.
     */
    public static final String PARM_LAST_IP_ADDRESS = "lastIPAddress";

    /**
     * The SNMP read community string when sent as an event parm.
     * @deprecated use {@link #PARM_SNMP_READ_COMMUNITY_STRING} instead
     */
    @Deprecated
    public static final String PARM_COMMUNITY_STRING = "communityString";
    
    /**
     * The SNMP read community string when sent as an event parm.
     */
    public static final String PARM_SNMP_READ_COMMUNITY_STRING = "readCommunityString";
    
    /**
     * The SNMP write community string when sent as an event parm.
     */
    public static final String PARM_SNMP_WRITE_COMMUNITY_STRING = "writeCommunityString";

    /**
     * The SNMP write community string when sent as an event parm.
     */
    public static final String PARM_SNMP_LOCATION = "location";

    /**
     * Service monitor qualifier when sent as an event parm
     */
    public static final String PARM_QUALIFIER = "qualifier";

    /**
     * The URL to which information is to be sent, sent as a parm to the rtc
     * subscribe and unsubscribe events.
     */
    public static final String PARM_URL = "url";

    /**
     * The category for which information is to be sent, sent as a parm to the
     * RTC subscribe event
     */
    public static final String PARM_CAT_LABEL = "catlabel";

    /**
     * Used to indicate categories added on a {@link #NODE_CATEGORY_MEMBERSHIP_CHANGED_EVENT_UEI} event.
     */
    public static final String PARM_CATEGORIES_ADDED = "categoriesAdded";

    /**
     * Used to indicate categories deleted on a {@link #NODE_CATEGORY_MEMBERSHIP_CHANGED_EVENT_UEI} event.
     */
    public static final String PARM_CATEGORIES_DELETED = "categoriesDeleted";

    /**
     * The username when sent as a parameter(like for the RTC subscribe)
     */
    public static final String PARM_USER = "user";

    /**
     * The passwd when sent as a parameter(like for the RTC subscribe)
     */
    public static final String PARM_PASSWD = "passwd";

    /**
     * The status of a service as returned from a service monitor
     */
    public static final String PARM_SERVICE_STATUS = "serviceStatus";

    /**
     * The external transaction number of an event to process.
     */
    public static final String PARM_TRANSACTION_NO = "txno";

    /**
     * Used for retaining the reason from a monitor determines SERVICE_UNAVAILABLE
     */
    public static final String PARM_LOSTSERVICE_REASON = "eventReason";
    
    /**
     * Used for setting the value for  PARM_LOSTSERVICE_REASON when the lost
     * service is due to a critical path outage
     */
    public static final String PARM_VALUE_PATHOUTAGE = "pathOutage";
    
    /**
     * Parms used for passive status events sent to the PassiveServiceKeeper
     */
    public static final String PARM_PASSIVE_NODE_LABEL = "passiveNodeLabel";
    /** Constant <code>PARM_PASSIVE_IPADDR="passiveIpAddr"</code> */
    public static final String PARM_PASSIVE_IPADDR = "passiveIpAddr";
    /** Constant <code>PARM_PASSIVE_SERVICE_NAME="passiveServiceName"</code> */
    public static final String PARM_PASSIVE_SERVICE_NAME = "passiveServiceName";
    /** Constant <code>PARM_PASSIVE_SERVICE_STATUS="passiveStatus"</code> */
    public static final String PARM_PASSIVE_SERVICE_STATUS = "passiveStatus";
    /** Constant <code>PARM_PASSIVE_REASON_CODE="passiveReasonCode"</code> */
    public static final String PARM_PASSIVE_REASON_CODE = "passiveReasonCode";

    /**
     * Parm used to importer event
     */
    public static final String PARM_FOREIGN_SOURCE = "foreignSource";
    /** Constant <code>PARM_FOREIGN_ID="foreignId"</code> */
    public static final String PARM_FOREIGN_ID = "foreignId";
    /** Constant <code>PARM_RESCAN_EXISTING="rescanExisting"</code> */
    public static final String PARM_RESCAN_EXISTING = "rescanExisting";
    
    /**
     * Parms used for configureSnmp events
     */
    public static final String PARM_VERSION = "version";
    /** Constant <code>PARM_TIMEOUT="timeout"</code> */
    public static final String PARM_TIMEOUT = "timeout";
    /** Constant <code>PARM_RETRY_COUNT="retryCount"</code> */
    public static final String PARM_RETRY_COUNT = "retryCount";
    /** Constant <code>PARM_PORT="port"</code> */
    public static final String PARM_PORT = "port";

    /** Constant <code>PARM_LOCATION_MONITOR_ID="locationMonitorId"</code> */
    public static final String PARM_LOCATION_MONITOR_ID = "locationMonitorId";
    /** Constant <code>PARM_LOCATION="location"</code> */
    public static final String PARM_LOCATION = "location";
    
    /**
     * Parm use for promoteEnqueuedData event
     */
    public static final String PARM_FILES_TO_PROMOTE = "filesToPromote";
    
    /**
     * Parameter used in event SNMP poller definition
     */ 
    public static final String PARM_SNMP_INTERFACE_IFINDEX="snmpifindex";

    /** Constant <code>PARM_SNMP_INTERFACE_IP="ipaddr"</code> */
    public static final String PARM_SNMP_INTERFACE_IP="ipaddr"; 

    /** Constant <code>PARM_SNMP_INTERFACE_NAME="snmpifname"</code> */
    public static final String PARM_SNMP_INTERFACE_NAME="snmpifname";

    /** Constant <code>PARM_SNMP_INTERFACE_DESC="snmpifdescr"</code> */
    public static final String PARM_SNMP_INTERFACE_DESC="snmpifdescr";

    /** Constant <code>PARM_SNMP_INTERFACE_ALIAS="snmpifalias"</code> */
    public static final String PARM_SNMP_INTERFACE_ALIAS="snmpifalias";

    /** Constant <code>PARM_SNMP_INTERFACE_MASK="mask"</code> */
    public static final String PARM_SNMP_INTERFACE_MASK="mask";
    
    //
    // End event parms
    //

    /**
     * Enumerated value for the state(tticket and forward) when entry is active.
     */
    public static final int STATE_ON = 1;

    /**
     * Enumerated value for the state(tticket and forward) when entry is not
     * active.
     */
    static final int STATE_OFF = 0;

    /**
     * UEI used for requesting an acknowledgment of an OnmsAcknowledgeable.
     */
    public static final String ACKNOWLEDGE_EVENT_UEI = "uei.opennms.org/ackd/acknowledge";

    /**
     * UEI used for indicating an OnmsAcknowledgeable has been acknowledged.
     */
    public static final String EVENT_ACKNOWLEDGED_UEI = "uei.opennms.org/ackd/acknowledgment";

    /**
     * UEI used for indicating a change management event.
     */
    public static final String NODE_CONFIG_CHANGE_UEI = "uei.opennms.org/internal/translator/entityConfigChanged";

    /**
     * Used for indicating a reason message in an event or alarm.
     */
    public static final String PARM_REASON = "reason";
    
    /**
     * Used for indication the first endpoint to a map link.
     */
    public static final String PARM_ENDPOINT1 = "endPoint1";
    
    /**
     * Used for indication the second endpoint to a map link.
     */
    public static final String PARM_ENDPOINT2 = "endPoint2";

    //
    // for Alarmd
    //

    // Sent when an alarm is created
    public static final String ALARM_CREATED_UEI   = "uei.opennms.org/alarms/alarmCreated";
    // Sent when an alarm is escalated, either by a user action or an automation
    public static final String ALARM_ESCALATED_UEI   = "uei.opennms.org/alarms/alarmEscalated";
    // Sent when an alarm is cleared, either by a user action or an automation
    public static final String ALARM_CLEARED_UEI = "uei.opennms.org/alarms/alarmCleared";
    // Sent when an alarm is un-cleared, either by a user action or an automation
    public static final String ALARM_UNCLEARED_UEI = "uei.opennms.org/alarms/alarmUncleared";
    // Sent when an alarm is updated with a reduce event
    public static final String ALARM_UPDATED_WITH_REDUCED_EVENT_UEI = "uei.opennms.org/alarms/alarmUpdatedWithReducedEvent";

    //
    // for Bsmd
    //
    public static final String BUSINESS_SERVICE_OPERATIONAL_STATUS_CHANGED_UEI = "uei.opennms.org/bsm/serviceOperationalStatusChanged";
    public static final String BUSINESS_SERVICE_PROBLEM_UEI = "uei.opennms.org/bsm/serviceProblem";
    public static final String BUSINESS_SERVICE_PROBLEM_RESOLVED_UEI = "uei.opennms.org/bsm/serviceProblemResolved";

    public static final String PARM_BUSINESS_SERVICE_ID = "businessServiceId";
    public static final String PARM_BUSINESS_SERVICE_NAME = "businessServiceName";
    public static final String PARM_NEW_SEVERITY_ID = "newSeverityId";
    public static final String PARM_NEW_SEVERITY_LABEL = "newSeverityLabel";
    public static final String PARM_PREV_SEVERITY_ID = "prevSeverityId";
    public static final String PARM_PREV_SEVERITY_LABEL = "prevSeverityLabel";

    //
    // for NCS service
    //
    public static final String COMPONENT_ADDED_UEI   = "uei.opennms.org/internal/ncs/componentAdded";
    public static final String COMPONENT_DELETED_UEI = "uei.opennms.org/internal/ncs/componentDeleted";
    public static final String COMPONENT_UPDATED_UEI = "uei.opennms.org/internal/ncs/componentUpdated";

    //
    // For Trapd
    //

    /** Constant <code>TYPE_STRING="string"</code> */
    public static final String TYPE_STRING = "string";

    /** Constant <code>TYPE_INT="int"</code> */
    public static final String TYPE_INT = "int";

    /** Constant <code>TYPE_SNMP_OCTET_STRING="OctetString"</code> */
    public static final String TYPE_SNMP_OCTET_STRING = "OctetString";

    /** Constant <code>TYPE_SNMP_INT32="Int32"</code> */
    public static final String TYPE_SNMP_INT32 = "Int32";

    /** Constant <code>TYPE_SNMP_NULL="Null"</code> */
    public static final String TYPE_SNMP_NULL = "Null";

    /** Constant <code>TYPE_SNMP_OBJECT_IDENTIFIER="ObjectIdentifier"</code> */
    public static final String TYPE_SNMP_OBJECT_IDENTIFIER = "ObjectIdentifier";

    /** Constant <code>TYPE_SNMP_IPADDRESS="IpAddress"</code> */
    public static final String TYPE_SNMP_IPADDRESS = "IpAddress";

    /** Constant <code>TYPE_SNMP_TIMETICKS="TimeTicks"</code> */
    public static final String TYPE_SNMP_TIMETICKS = "TimeTicks";

    /** Constant <code>TYPE_SNMP_COUNTER32="Counter32"</code> */
    public static final String TYPE_SNMP_COUNTER32 = "Counter32";

    /** Constant <code>TYPE_SNMP_GAUGE32="Gauge32"</code> */
    public static final String TYPE_SNMP_GAUGE32 = "Gauge32";

    /** Constant <code>TYPE_SNMP_OPAQUE="Opaque"</code> */
    public static final String TYPE_SNMP_OPAQUE = "Opaque";

    /** Constant <code>TYPE_SNMP_SEQUENCE="Sequence"</code> */
    public static final String TYPE_SNMP_SEQUENCE = "Sequence";

    /** Constant <code>TYPE_SNMP_COUNTER64="Counter64"</code> */
    public static final String TYPE_SNMP_COUNTER64 = "Counter64";

    /** Constant <code>XML_ENCODING_TEXT="text"</code> */
    public static final String XML_ENCODING_TEXT = "text";

    /** Constant <code>XML_ENCODING_BASE64="base64"</code> */
    public static final String XML_ENCODING_BASE64 = "base64";

    /** Constant <code>XML_ENCODING_MAC_ADDRESS="macAddress"</code> */
    public static final String XML_ENCODING_MAC_ADDRESS = "macAddress";

    /** Constant <code>OID_SNMP_IFINDEX</code> */
    public static final SnmpObjId OID_SNMP_IFINDEX = SnmpObjId.get(".1.3.6.1.2.1.2.2.1.1");

    public static final ThreadLocal<DateFormat> FORMATTER_FULL = new ThreadLocal<DateFormat>() {
        @Override
        protected synchronized DateFormat initialValue() {
            int timeFormat = DateFormat.FULL;
            // The DateFormat.FULL format for France/Germany do not include the seconds digit
            // which is necessary to have sub-minute resolution in event times. For these
            // locales, we'll fall back to using DateFormat.LONG.
            //
            // @see org.opennms.netmgt.DateFormatLocaleTest
            //
            if (Locale.getDefault().getLanguage().equals(Locale.FRANCE.getLanguage())) {
                timeFormat = DateFormat.LONG;
            } else if (Locale.getDefault().getLanguage().equals(Locale.GERMANY.getLanguage())) {
                timeFormat = DateFormat.LONG;
            }
            final DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.FULL, timeFormat);
            formatter.setLenient(true);
            return formatter;
        }
    };

    public static final ThreadLocal<DateFormat> FORMATTER_LONG = new ThreadLocal<DateFormat>() {
        @Override
        protected synchronized DateFormat initialValue() {
            final DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.LONG);
            formatter.setLenient(true);
            return formatter;
        }
    };
    
    public static final ThreadLocal<DateFormat> FORMATTER_FULL_GMT = new ThreadLocal<DateFormat>() {
        @Override
        protected synchronized DateFormat initialValue() {
            int timeFormat = DateFormat.FULL;
            // The DateFormat.FULL format for France/Germany do not include the seconds digit
            // which is necessary to have sub-minute resolution in event times. For these
            // locales, we'll fall back to using DateFormat.LONG.
            //
            // @see org.opennms.netmgt.DateFormatLocaleTest
            //
            if (Locale.getDefault().getLanguage().equals(Locale.FRANCE.getLanguage())) {
                timeFormat = DateFormat.LONG;
            } else if (Locale.getDefault().getLanguage().equals(Locale.GERMANY.getLanguage())) {
                timeFormat = DateFormat.LONG;
            }
            final DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.FULL, timeFormat);
            formatter.setLenient(true);
            formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
            return formatter;
        }
    };

    public static final ThreadLocal<DateFormat> FORMATTER_LONG_GMT = new ThreadLocal<DateFormat>() {
        @Override
        protected synchronized DateFormat initialValue() {
            final DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.LONG);
            formatter.setLenient(true);
            formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
            return formatter;
        }
    };

    /**
     * This {@link DateFormat} is used to parse timestamps from XML events that are generated by
     * the send-event.pl script. It always formats timestamps in English so we hard-code the locale
     * as {@link Locale#ENGLISH} in this {@link DateFormat}.
     */
    public static final ThreadLocal<DateFormat> FORMATTER_CUSTOM = new ThreadLocal<DateFormat>() {
        @Override
        protected synchronized DateFormat initialValue() {
            final DateFormat formatter = new SimpleDateFormat("EEEEE, d MMMMM yyyy k:mm:ss 'o''clock' z", Locale.ENGLISH);
            formatter.setLenient(true);
            return formatter;
        }
    };

    public static final ThreadLocal<DateFormat> FORMATTER_DEFAULT = new ThreadLocal<DateFormat>() {
        @Override
        protected synchronized DateFormat initialValue() {
            final DateFormat formatter = DateFormat.getDateTimeInstance();
            formatter.setLenient(true);
            return formatter;
        }
    };

    /**
     * An utility method to parse a string into a 'Date' instance. Note that the
     * string should be in the locale-specific DateFormat.LONG style for both
     * the date and time, althought DateFormat.FULL will be accepted as well.
     *
     * @see java.text.DateFormat
     * @param timeString a {@link java.lang.String} object.
     * @return a {@link java.util.Date} object.
     * @throws java.text.ParseException if any.
     */
    public static final Date parseToDate(final String timeString) throws ParseException {
        if (timeString == null) {
            throw new ParseException("time was null!", -1);
        }
        try {
            return FORMATTER_LONG.get().parse(timeString);
        } catch (final ParseException parseException) {
            try {
                return FORMATTER_CUSTOM.get().parse(timeString);
            } catch (final ParseException pe) {
                return FORMATTER_FULL.get().parse(timeString);
            }
        }
    }

    /**
     * A utility method to format a 'Date' into a string in the locale-specific
     * LONG DateFormat style for both the date and time.
     *
     * @see java.text.DateFormat
     * @param date a {@link java.util.Date} object.
     * @return a {@link java.lang.String} object.
     */
    public static final String formatToString(final Date date) {
    	return FORMATTER_LONG_GMT.get().format(date);
    }

	/**
	 * Converts the value of a parm ('Value') of the instance to a string
	 *
	 * @param pvalue a {@link org.opennms.netmgt.xml.event.Value} object.
	 * @return a {@link java.lang.String} object.
	 */
	public static String getValueAsString(Value pvalue) {
		if (pvalue == null)
			return null;
		
		if (pvalue.getContent() == null)
			return null;

		String result = "";
		String encoding = pvalue.getEncoding();
		if (encoding.equals(EventConstants.XML_ENCODING_TEXT)) {
			result = pvalue.getContent();
		} else if (encoding.equals(EventConstants.XML_ENCODING_BASE64)) {
			byte[] bytes = Base64.decodeBase64(pvalue.getContent().toCharArray());
			result = "0x"+toHexString(bytes);
		} else if (encoding.equals(EventConstants.XML_ENCODING_MAC_ADDRESS)) {
			result = pvalue.getContent();
		} else {
			throw new IllegalStateException("Unknown encoding for parm value: " + encoding);
		}
		
		return result.trim();
	}
	
	public static String toHexString(byte[] data) {
		final StringBuffer b = new StringBuffer();
		for (int i = 0; i < data.length; ++i) {
			final int x = (int) data[i] & 0xff;
			if (x < 16) b.append("0");
			b.append(Integer.toString(x, 16).toLowerCase());
		}
		return b.toString();
	}

    //
    // For Trapd
    //

    /**
     * Converts the value of the instance to a string representation in the
     * correct encoding system.
     *
     * @param encoding a {@link java.lang.String} object.
     * @param value a {@link java.lang.Object} object.
     * @return a {@link java.lang.String} object.
     */
    public static String toString(String encoding, Object value) {
        if (encoding == null || value == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        String result = null;

        if (XML_ENCODING_TEXT.equals(encoding)) {
            if (value instanceof String)
                result = (String) value;
            else if (value instanceof Number)
                result = value.toString();
            else if (value instanceof SnmpValue)
                result = ((SnmpValue)value).toString();
        } else if (XML_ENCODING_BASE64.equals(encoding)) {
            if (value instanceof String)
                result = new String(Base64.encodeBase64(((String) value).getBytes()));
            else if (value instanceof Number) {
                
                byte[] ibuf = null;
                if (value instanceof BigInteger)
                    ibuf = ((BigInteger) value).toByteArray();
                else
                    ibuf = BigInteger.valueOf(((Number) value).longValue()).toByteArray();

                result = new String(Base64.encodeBase64(ibuf));
            }
            else if (value instanceof SnmpValue) {
                SnmpValue snmpValue = (SnmpValue)value;
                result = new String(Base64.encodeBase64(snmpValue.getBytes()));
            }
        } else if (XML_ENCODING_MAC_ADDRESS.equals(encoding)) {
            if (value instanceof SnmpValue) {
                SnmpValue snmpValue = (SnmpValue)value;
                StringBuffer macAddress = new StringBuffer();
                byte[] bytes = snmpValue.getBytes();
                for (int i = 0; i < bytes.length; i++) {
                    if (i > 0) macAddress.append(":");
                    macAddress.append(String.format("%02X", bytes[i]));
                }
                result = macAddress.toString();
            }
        }
        
        if (result == null)
            throw new IllegalArgumentException("unable to encode "+value+" of type "+value.getClass());

        return result;
    }

}
