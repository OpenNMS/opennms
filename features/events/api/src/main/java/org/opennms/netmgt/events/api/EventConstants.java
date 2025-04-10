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
package org.opennms.netmgt.events.api;

import java.math.BigInteger;
import java.text.ParseException;
import java.util.Date;

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

    //
    // The eventUEIs used by OpenNMS
    //

    /**
     * The situation event UEI.
     */
    public static final String SITUATION_EVENT_UEI = "uei.opennms.org/alarms/situation";

    /**
     * The new suspect event UEI.
     */
    public static final String NEW_SUSPECT_INTERFACE_EVENT_UEI = "uei.opennms.org/internal/discovery/newSuspect";

    /**
     * The discovery pause event UEI.
     */
    public static final String DISC_PAUSE_EVENT_UEI = "uei.opennms.org/internal/capsd/discPause";

    /**
     * The discovery resume event UEI.
     */
    public static final String DISC_RESUME_EVENT_UEI = "uei.opennms.org/internal/capsd/discResume";

    /**
     * The discovery configuration changed event UEI.
     */
    public static final String DISCOVERYCONFIG_CHANGED_EVENT_UEI = "uei.opennms.org/internal/discoveryConfigChange";

    /**
     * The add node event UEI.
     */
    public static final String ADD_NODE_EVENT_UEI = "uei.opennms.org/internal/capsd/addNode";

    /**
     * The delete node event UEI.
     */
    public static final String DELETE_NODE_EVENT_UEI = "uei.opennms.org/internal/capsd/deleteNode";

    /**
     * The delete interface event UEI.
     */
    public static final String DELETE_INTERFACE_EVENT_UEI = "uei.opennms.org/internal/capsd/deleteInterface";

    /**
     * The outage created event UEI.
     */
    public static final String OUTAGE_CREATED_EVENT_UEI = "uei.opennms.org/internal/poller/outageCreated";

    /**
     * The outage Resolved event UEI.
     */
    public static final String OUTAGE_RESOLVED_EVENT_UEI = "uei.opennms.org/internal/poller/outageResolved";

    /**
     * The node added event UEI.
     */
    public static final String NODE_ADDED_EVENT_UEI = "uei.opennms.org/nodes/nodeAdded";
    
    /**
     * The node updated event UEI (added for the ProvisioningAdapter integration).
     */
    public static final String NODE_UPDATED_EVENT_UEI = "uei.opennms.org/nodes/nodeUpdated";


    /**
     * The node location changed event UEI.
     */
    public static final String NODE_LOCATION_CHANGED_EVENT_UEI = "uei.opennms.org/nodes/nodeLocationChanged";
    
	/**
	 * The node category membership changed UEI.
	 */
    public static final String NODE_CATEGORY_MEMBERSHIP_CHANGED_EVENT_UEI = "uei.opennms.org/nodes/nodeCategoryMembershipChanged";

    /**
     * The node gained interface event UEI.
     */
    public static final String NODE_GAINED_INTERFACE_EVENT_UEI = "uei.opennms.org/nodes/nodeGainedInterface";

    /**
     * The node gained service event UEI.
     */
    public static final String NODE_GAINED_SERVICE_EVENT_UEI = "uei.opennms.org/nodes/nodeGainedService";

    /**
     * The node lost service event UEI.
     */
    public static final String NODE_LOST_SERVICE_EVENT_UEI = "uei.opennms.org/nodes/nodeLostService";

    /**
     * The service responsive event UEI.
     */
    public static final String SERVICE_RESPONSIVE_EVENT_UEI = "uei.opennms.org/nodes/serviceResponsive";

    /**
     * The service unresponsive event UEI.
     */
    public static final String SERVICE_UNRESPONSIVE_EVENT_UEI = "uei.opennms.org/nodes/serviceUnresponsive";

    /**
     * The service unmanaged event UEI.
     */
    public static final String SERVICE_UNMANAGED_EVENT_UEI = "uei.opennms.org/nodes/serviceUnmanaged";

    /**
     * The interface down event UEI.
     */
    public static final String INTERFACE_DOWN_EVENT_UEI = "uei.opennms.org/nodes/interfaceDown";

    /**
     * The SNMP interface operStatus down event UEI.
     */
    public static final String SNMP_INTERFACE_OPER_DOWN_EVENT_UEI = "uei.opennms.org/nodes/snmp/interfaceOperDown";

    /**
     * The SNMP interface admin down event UEI.
     */
    public static final String SNMP_INTERFACE_ADMIN_DOWN_EVENT_UEI = "uei.opennms.org/nodes/snmp/interfaceAdminDown";

    /**
     * The SNMP interface operStatus testing event UEI.
     */
    public static final String SNMP_INTERFACE_OPER_TESTING_EVENT_UEI = "uei.opennms.org/nodes/snmp/interfaceOperTesting";

    /**
     * The SNMP interface operStatus unknown event UEI.
     */
    public static final String SNMP_INTERFACE_OPER_UNKNOWN_EVENT_UEI = "uei.opennms.org/nodes/snmp/interfaceOperUnknown";

    /**
     * The SNMP interface operStatus dormant event UEI.
     */
    public static final String SNMP_INTERFACE_OPER_DORMANT_EVENT_UEI = "uei.opennms.org/nodes/snmp/interfaceOperDormant";

    /**
     * The SNMP interface operStatus notPresent event UEI.
     */
    public static final String SNMP_INTERFACE_OPER_NOT_PRESENT_EVENT_UEI = "uei.opennms.org/nodes/snmp/interfaceOperNotPresent";

    /**
     * The SNMP interface operStatus lowerLayerDown event UEI.
     */
    public static final String SNMP_INTERFACE_OPER_LOWER_LAYER_DOWN_EVENT_UEI = "uei.opennms.org/nodes/snmp/interfaceOperLowerLayerDown";

    /**
     * The node down event UEI.
     */
    public static final String NODE_DOWN_EVENT_UEI = "uei.opennms.org/nodes/nodeDown";

    /**
     * The path outage event UEI.
     */
    public static final String PATH_OUTAGE_EVENT_UEI = "uei.opennms.org/nodes/pathOutage";

    /**
     * The node up event UEI.
     */
    public static final String NODE_UP_EVENT_UEI = "uei.opennms.org/nodes/nodeUp";

    /**
     * The interface up event UEI.
     */
    public static final String INTERFACE_UP_EVENT_UEI = "uei.opennms.org/nodes/interfaceUp";

    /**
     * The SNMP interface operStatus up event UEI.
     */
    public static final String SNMP_INTERFACE_OPER_UP_EVENT_UEI = "uei.opennms.org/nodes/snmp/interfaceOperUp";

    /**
     * The SNMP interface admin up event UEI.
     */
    public static final String SNMP_INTERFACE_ADMIN_UP_EVENT_UEI = "uei.opennms.org/nodes/snmp/interfaceAdminUp";

    /**
     * The node regained service event UEI.
     */
    public static final String NODE_REGAINED_SERVICE_EVENT_UEI = "uei.opennms.org/nodes/nodeRegainedService";

    /**
     * The delete service event UEI.
     */
    public static final String DELETE_SERVICE_EVENT_UEI = "uei.opennms.org/nodes/deleteService";

    /**
     * Whether to ignore unmanaged interfaces when cascading service delete.
     */
    public static final String PARM_IGNORE_UNMANAGED = "ignoreUnmanaged";

    /**
     * The service deleted event UEI.
     */
    public static final String SERVICE_DELETED_EVENT_UEI = "uei.opennms.org/nodes/serviceDeleted";

    /**
     * The interface deleted event UEI.
     */
    public static final String INTERFACE_DELETED_EVENT_UEI = "uei.opennms.org/nodes/interfaceDeleted";

    /**
     * The node deleted event UEI.
     */
    public static final String NODE_DELETED_EVENT_UEI = "uei.opennms.org/nodes/nodeDeleted";

    /**
     * The low threshold exceeded event UEI.
     */
    public static final String LOW_THRESHOLD_EVENT_UEI = "uei.opennms.org/threshold/lowThresholdExceeded";

    /**
     * The high threshold exceeded event UEI.
     */
    public static final String HIGH_THRESHOLD_EVENT_UEI = "uei.opennms.org/threshold/highThresholdExceeded";

    /**
     * The high threshold rearm event UEI.
     */
    public static final String HIGH_THRESHOLD_REARM_EVENT_UEI = "uei.opennms.org/threshold/highThresholdRearmed";

    /**
     * The low threshold rearm event UEI.
     */
    public static final String LOW_THRESHOLD_REARM_EVENT_UEI = "uei.opennms.org/threshold/lowThresholdRearmed";

    /**
     * The relative change event UEI.
     */
    public static final String RELATIVE_CHANGE_THRESHOLD_EVENT_UEI = "uei.opennms.org/threshold/relativeChangeExceeded";

    /**
     * The relative change event UEI.
     */
    public static final String ABSOLUTE_CHANGE_THRESHOLD_EVENT_UEI = "uei.opennms.org/threshold/absoluteChangeExceeded";

    /**
     * ThresholdEvaluatorRearmingAbsoluteChange exceeded UEI.
     */
    public static final String REARMING_ABSOLUTE_CHANGE_EXCEEDED_EVENT_UEI = "uei.opennms.org/threshold/rearmingAbsoluteChangeExceeded";
    
    /**
     * ThresholdEvaluatorRearmingAbsoluteChange exceeded UEI.
     */
    public static final String REARMING_ABSOLUTE_CHANGE_REARM_EVENT_UEI = "uei.opennms.org/threshold/rearmingAbsoluteChangeRearmed";

    /**
     * The interface reparented event.
     */
    public static final String INTERFACE_REPARENTED_EVENT_UEI = "uei.opennms.org/nodes/interfaceReparented";

    /**
     * The node info changed event.
     */
    public static final String NODE_INFO_CHANGED_EVENT_UEI = "uei.opennms.org/nodes/nodeInfoChanged";

    /**
     * The node label changed event.
     */
    public static final String NODE_LABEL_CHANGED_EVENT_UEI = "uei.opennms.org/nodes/nodeLabelChanged";

    /**
     * The node deleted event UEI.
     */
    public static final String DUP_NODE_DELETED_EVENT_UEI = "uei.opennms.org/nodes/duplicateNodeDeleted";

    /**
     * The primary SNMP interface changed event.
     */
    public static final String PRIMARY_SNMP_INTERFACE_CHANGED_EVENT_UEI = "uei.opennms.org/nodes/primarySnmpInterfaceChanged";

    /**
     * The reinitialize primary SNMP interface event.
     */
    public static final String REINITIALIZE_PRIMARY_SNMP_INTERFACE_EVENT_UEI = "uei.opennms.org/nodes/reinitializePrimarySnmpInterface";

    /**
     * The configure SNMP event.
     */
    public static final String CONFIGURE_SNMP_EVENT_UEI = "uei.opennms.org/internal/configureSNMP";

    /**
     * Collection failed.
     */
    public static final String DATA_COLLECTION_FAILED_EVENT_UEI = "uei.opennms.org/nodes/dataCollectionFailed";

    /**
     * Collection succeeded.
     */
    public static final String DATA_COLLECTION_SUCCEEDED_EVENT_UEI = "uei.opennms.org/nodes/dataCollectionSucceeded";

    /**
     * The force interface rescan event UEI
     */
    public static final String FORCE_RESCAN_EVENT_UEI = "uei.opennms.org/internal/capsd/forceRescan";

    /**
     * The suspend polling service event UEI
     */
    public static final String SUSPEND_POLLING_SERVICE_EVENT_UEI = "uei.opennms.org/internal/poller/suspendPollingService";

    /**
     * The resume polling service event UEI.
     */

    public static final String RESUME_POLLING_SERVICE_EVENT_UEI = "uei.opennms.org/internal/poller/resumePollingService";

    /**
     * The rescan completed UEI.
     */

    public static final String RESCAN_COMPLETED_EVENT_UEI = "uei.opennms.org/internal/capsd/rescanCompleted";

    /**
     * The RTC subscribe event.
     */
    public static final String RTC_SUBSCRIBE_EVENT_UEI = "uei.opennms.org/internal/rtc/subscribe";

    /**
     * The RTC unsubscribe event.
     */
    public static final String RTC_UNSUBSCRIBE_EVENT_UEI = "uei.opennms.org/internal/rtc/unsubscribe";

    
    /**
     * An event used by queued to indicate that data for certain rrds should be immediately flushed to the disk.
     */
    public static final String PROMOTE_QUEUE_DATA_UEI = "uei.opennms.org/internal/promoteQueueData";

    /**
     * Notification without users event.
     */
    public static final String NOTIFICATION_WITHOUT_USERS = "uei.opennms.org/internal/notificationWithoutUsers";

    /**
     * An event to signal that a user has changed asset information via the web
     * UI.
     */
    public static final String ASSET_INFO_CHANGED_EVENT_UEI = "uei.opennms.org/nodes/assetInfoChanged";

	/**
        * The scheduled-outages configuration was changed by the user via the web UI (or manually, for that matter).
        */
    public static final String SCHEDOUTAGES_CHANGED_EVENT_UEI = "uei.opennms.org/internal/schedOutagesChanged";
    
    /**
     * The threshold config was changed by the user via the web UI, or manually.
     */
    public static final String THRESHOLDCONFIG_CHANGED_EVENT_UEI = "uei.opennms.org/internal/thresholdConfigChange";
       
    /**
     * The event config was changed by the user via the web UI, or manually, and should be reloaded.
     */
    public static final String EVENTSCONFIG_CHANGED_EVENT_UEI = "uei.opennms.org/internal/eventsConfigChange";
    
    /**
     * The Snmp Poller config was changed by the user via the web UI, or manually, and should be reloaded.
     */
    public static final String SNMPPOLLERCONFIG_CHANGED_EVENT_UEI = "uei.opennms.org/internal/reloadSnmpPollerConfig";
    
    /**
     * Reload Vacuumd configuration UEI.
     */
    public static final String RELOAD_VACUUMD_CONFIG_UEI = "uei.opennms.org/internal/reloadVacuumdConfig";

    /**
     * Reload topology UEI.
     */
    public static final String RELOAD_TOPOLOGY_UEI = "uei.opennms.org/internal/reloadTopology";

    /**
     * Reload Drools Correlation Engine.
     */
    public static final String DROOLS_ENGINE_ENCOUNTERED_EXCEPTION = "uei.opennms.org/internal/droolsEngineException";

    /**
     * Reload Daemon configuration UEI.
     */
    public static final String RELOAD_DAEMON_CONFIG_UEI = "uei.opennms.org/internal/reloadDaemonConfig";
    /** Constant <code>RELOAD_DAEMON_CONFIG_FAILED_UEI="uei.opennms.org/internal/reloadDaemonCo"{trunked}</code> */
    public static final String RELOAD_DAEMON_CONFIG_FAILED_UEI = "uei.opennms.org/internal/reloadDaemonConfigFailed";
    /** Constant <code>RELOAD_DAEMON_CONFIG_SUCCESSFUL_UEI="uei.opennms.org/internal/reloadDaemonCo"{trunked}</code> */
    public static final String RELOAD_DAEMON_CONFIG_SUCCESSFUL_UEI = "uei.opennms.org/internal/reloadDaemonConfigSuccessful";
    /** Constant <code>PARM_DAEMON_NAME="daemonName"</code> */
    public static final String PARM_DAEMON_NAME = "daemonName";
    /** Constant <code>PARM_CONFIG_FILE_NAME="configFile"</code> */
    public static final String PARM_CONFIG_FILE_NAME = "configFile";

    /*
     * Reportd UEIs.
     */
    
    public static final String REPORTD_RUN_REPORT = "uei.opennms.org/reportd/runReport";
    /** Constant <code>PARM_REPORT_NAME="reportName"</code> */
    public static final String PARM_REPORT_NAME = "reportName";
    public static final String REPORT_RUN_FAILED_UEI = "uei.opennms.org/reportd/reportRunFailed";
    public static final String REPORT_DELIVERY_FAILED_UEI = "uei.opennms.org/reportd/reportDeliveryFailed";
    
    public static final String PERSPECTIVE_NODE_LOST_SERVICE_UEI = "uei.opennms.org/perspective/nodes/nodeLostService";
    public static final String PERSPECTIVE_NODE_REGAINED_SERVICE_UEI = "uei.opennms.org/perspective/nodes/nodeRegainedService";
    /** Constant <code>LOCATION_MONITOR_REGISTERED_UEI="uei.opennms.org/remote/locationMonitorR"{trunked}</code> */
    public static final String LOCATION_MONITOR_REGISTERED_UEI="uei.opennms.org/remote/locationMonitorRegistered";
    /** Constant <code>LOCATION_MONITOR_STARTED_UEI="uei.opennms.org/remote/locationMonitorS"{trunked}</code> */
    public static final String LOCATION_MONITOR_STARTED_UEI="uei.opennms.org/remote/locationMonitorStarted";
    /** Constant <code>LOCATION_MONITOR_STOPPED_UEI="uei.opennms.org/remote/locationMonitorS"{trunked}</code> */
    public static final String LOCATION_MONITOR_STOPPED_UEI="uei.opennms.org/remote/locationMonitorStopped";
    /** Constant <code>LOCATION_MONITOR_PAUSED_UEI="uei.opennms.org/remote/locationMonitorP"{trunked}</code> */
    public static final String LOCATION_MONITOR_PAUSED_UEI="uei.opennms.org/remote/locationMonitorPaused";
    /** Constant <code>LOCATION_MONITOR_DISCONNECTED_UEI="uei.opennms.org/remote/locationMonitorD"{trunked}</code> */
    public static final String LOCATION_MONITOR_DISCONNECTED_UEI="uei.opennms.org/remote/locationMonitorDisconnected";
    /** Constant <code>LOCATION_MONITOR_RECONNECTED_UEI="uei.opennms.org/remote/locationMonitorR"{trunked}</code> */
    public static final String LOCATION_MONITOR_RECONNECTED_UEI="uei.opennms.org/remote/locationMonitorReconnected";
    public static final String LOCATION_MONITOR_CONNECTION_ADDRESS_CHANGED_UEI="uei.opennms.org/remote/locationMonitorConnectionAddressChanged";

    public static final String REMOTE_SUCCESSFUL_SCAN_REPORT_UEI="uei.opennms.org/remote/successfulScanReport";
    public static final String REMOTE_UNSUCCESSFUL_SCAN_REPORT_UEI="uei.opennms.org/remote/unsuccessfulScanReport";

    /** Constant <code>RELOAD_IMPORT_UEI="uei.opennms.org/internal/importer/reloa"{trunked}</code> */
    public static final String RELOAD_IMPORT_UEI = "uei.opennms.org/internal/importer/reloadImport";
    /** Constant <code>IMPORT_STARTED_UEI="uei.opennms.org/internal/importer/impor"{trunked}</code> */
    public static final String IMPORT_STARTED_UEI = "uei.opennms.org/internal/importer/importStarted";
    /** Constant <code>IMPORT_SUCCESSFUL_UEI="uei.opennms.org/internal/importer/impor"{trunked}</code> */
    public static final String IMPORT_SUCCESSFUL_UEI = "uei.opennms.org/internal/importer/importSuccessful";
    /** Constant <code>IMPORT_FAILED_UEI="uei.opennms.org/internal/importer/impor"{trunked}</code> */
    public static final String IMPORT_FAILED_UEI = "uei.opennms.org/internal/importer/importFailed";
    /** Constant <code>PROVISIONING_ADAPTER_FAILED="uei.opennms.org/provisioner/provisionin"{trunked}</code> */
    public static final String PROVISIONING_ADAPTER_FAILED = "uei.opennms.org/provisioner/provisioningAdapterFailed";

    /** Constant <code>PROVISION_SCAN_COMPLETE_UEI="uei.opennms.org/internal/provisiond/nod"{trunked}</code> */
    public static final String PROVISION_SCAN_COMPLETE_UEI="uei.opennms.org/internal/provisiond/nodeScanCompleted";
    /** Constant <code>PROVISION_SCAN_ABORTED_UEI="uei.opennms.org/internal/provisiond/nod"{trunked}</code> */
    public static final String PROVISION_SCAN_ABORTED_UEI="uei.opennms.org/internal/provisiond/nodeScanAborted";

    public static final String PROVISION_SCHEDULED_NODE_SCAN_STARTED ="uei.opennms.org/internal/provisiond/scheduledNodeScanStarted";
    
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
    /** Constant <code>PARM_ALARM_REDUCTION_KEY="alarmReductionKey"</code> */
    public static final String PARM_ALARM_REDUCTION_KEY = "alarmReductionKey";
    /** Constant <code>PARM_TROUBLE_TICKET="troubleTicket"</code> */
    public static final String PARM_TROUBLE_TICKET = "troubleTicket";

    /** Constant <code>TROUBLETICKET_CREATE_UEI="uei.opennms.org/troubleTicket/create"</code> */
    public static final String TROUBLETICKET_CREATE_UEI = "uei.opennms.org/troubleTicket/create";
    /** Constant <code>TROUBLETICKET_UPDATE_UEI="uei.opennms.org/troubleTicket/update"</code> */
    public static final String TROUBLETICKET_UPDATE_UEI = "uei.opennms.org/troubleTicket/update";
    /** Constant <code>TROUBLETICKET_CLOSE_UEI="uei.opennms.org/troubleTicket/close"</code> */
    public static final String TROUBLETICKET_CLOSE_UEI = "uei.opennms.org/troubleTicket/close";
    /** Constant <code>TROUBLETICKET_CANCEL_UEI="uei.opennms.org/troubleTicket/cancel"</code> */
    public static final String TROUBLETICKET_CANCEL_UEI = "uei.opennms.org/troubleTicket/cancel";
    
    /** Constant <code>TL1_AUTONOMOUS_MESSAGE_UEI="uei.opennms.org/api/tl1d/message/autono"{trunked}</code> */
    public static final String TL1_AUTONOMOUS_MESSAGE_UEI = "uei.opennms.org/api/tl1d/message/autonomous";

    /** Constant <code>TOPOLOGY_LINK_DOWN_EVENT_UEI="uei.opennms.org/internal/topology/linkDown"{trunked}</code> */
    public static final String TOPOLOGY_LINK_DOWN_EVENT_UEI = "uei.opennms.org/internal/topology/linkDown";
    /** Constant <code>TOPOLOGY_LINK_UP_EVENT_UEI="uei.opennms.org/internal/topology/linkUp"{trunked}</code> */
    public static final String TOPOLOGY_LINK_UP_EVENT_UEI = "uei.opennms.org/internal/topology/linkUp";

    public static final String HARDWARE_INVENTORY_FAILED_UEI = "uei.opennms.org/internal/discovery/hardwareInventoryFailed";
    public static final String HARDWARE_INVENTORY_SUCCESSFUL_UEI = "uei.opennms.org/internal/discovery/hardwareInventorySuccessful";

    public static final String KSC_REPORT_UPDATED_UEI = "uei.opennms.org/internal/kscReportUpdated";
    public static final String PARAM_REPORT_TITLE = "reportTitle";
    public static final String PARAM_REPORT_GRAPH_COUNT = "graphCount";

    public static final String MONITORING_SYSTEM_ADDED_UEI = "uei.opennms.org/internal/monitoringSystemAdded";
    public static final String MONITORING_SYSTEM_LOCATION_CHANGED_UEI = "uei.opennms.org/internal/monitoringSystemLocationChanged";
    public static final String MONITORING_SYSTEM_DELETED_UEI = "uei.opennms.org/internal/monitoringSystemDeleted";
    public static final String PARAM_MONITORING_SYSTEM_TYPE = "monitoringSystemType";
    public static final String PARAM_MONITORING_SYSTEM_ID = "monitoringSystemId";
    public static final String PARAM_MONITORING_SYSTEM_LOCATION = "monitoringSystemLocation";
    public static final String PARAM_MONITORING_SYSTEM_PREV_LOCATION = "monitoringSystemPreviousLocation";

    public static final String PARAM_TOPOLOGY_NAMESPACE = "namespace";
    public static final String DEVICE_CONFIG_BACKUP_STARTED_UEI = "uei.opennms.org/deviceconfig/configBackupStarted";
    public static final String DEVICE_CONFIG_BACKUP_FAILED_UEI = "uei.opennms.org/deviceconfig/configBackupFailed";
    public static final String DEVICE_CONFIG_BACKUP_SUCCEEDED_UEI = "uei.opennms.org/deviceconfig/configBackupSucceeded";
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

    public static final String PARM_IPINTERFACE_ID = "ipInterfaceID";

    /**
     * The nodeLabel from the node table when sent as an event parm.
     */
    public static final String PARM_NODE_LABEL = "nodelabel";

    /**
     * The prev location for the node when sent as an event parm.
     */
    public static final String PARM_NODE_PREV_LOCATION = "nodePrevLocation";

    /**
     * The current location for the node table when sent as an event parm.
     */
    public static final String PARM_NODE_CURRENT_LOCATION = "nodeCurrentLocation";

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
     * The ttl when sent as an event parm.
     */
    public static final String PARM_TTL = "ttl";

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
    /** Constant <code>PARM_RESCAN_EXISTING="monitorKey"</code> */
    public static final String PARM_MONITOR_KEY = "monitorKey";

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
     * login success UEI
     */
    public static final String AUTHENTICATION_SUCCESS_UEI = "uei.opennms.org/internal/authentication/successfulLogin";

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
    // for BMP
    //
    public static final String BMP_PEER_DOWN = "uei.opennms.org/bmp/peerDown";
    public static final String BMP_PEER_UP = "uei.opennms.org/bmp/peerUp";

    //
    // for Bsmd
    //
    public static final String BUSINESS_SERVICE_OPERATIONAL_STATUS_CHANGED_UEI = "uei.opennms.org/bsm/serviceOperationalStatusChanged";
    public static final String BUSINESS_SERVICE_PROBLEM_UEI = "uei.opennms.org/bsm/serviceProblem";
    public static final String BUSINESS_SERVICE_PROBLEM_RESOLVED_UEI = "uei.opennms.org/bsm/serviceProblemResolved";
    public static final String BUSINESS_SERVICE_DELETED_EVENT_UEI = "uei.opennms.org/internal/serviceDeleted";
    public static final String APPLICATION_CREATED_EVENT_UEI = "uei.opennms.org/internal/applicationCreated";
    public static final String APPLICATION_CHANGED_EVENT_UEI = "uei.opennms.org/internal/applicationChanged";
    public static final String APPLICATION_DELETED_EVENT_UEI = "uei.opennms.org/internal/applicationDeleted";
    public static final String BUSINESS_SERVICE_GRAPH_INVALIDATED = "uei.opennms.org/bsm/graphInvalidated";

    public static final String PARM_BUSINESS_SERVICE_ID = "businessServiceId";
    public static final String PARM_BUSINESS_SERVICE_NAME = "businessServiceName";
    public static final String PARM_NEW_SEVERITY_ID = "newSeverityId";
    public static final String PARM_NEW_SEVERITY_LABEL = "newSeverityLabel";
    public static final String PARM_PREV_SEVERITY_ID = "prevSeverityId";
    public static final String PARM_PREV_SEVERITY_LABEL = "prevSeverityLabel";
    public static final String PARM_APPLICATION_ID = "applicationId";
    public static final String PARM_APPLICATION_NAME = "applicationName";

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

    /**
     * A utility method to parse a string into a 'Date' instance.
     *
     * @deprecated use {@link #getEventDatetimeFormatter()} instead
     * @param timeString a {@link java.lang.String} object
     * @return a {@link java.util.Date} object
     * @throws java.text.ParseException if any
     */
    public static final Date parseToDate(final String timeString) throws ParseException {
        return getEventDatetimeFormatter().parse(timeString);
    }

    /**
     * A utility method to format a 'Date' into a string.
     *
     * @deprecated use {@link #getEventDatetimeFormatter()} instead
     * @param date a {@link java.util.Date} object
     * @return a {@link java.lang.String} object
     */
    public static final String formatToString(final Date date) {
        return getEventDatetimeFormatter().format(date);
    }

    /**
     * Get the appropriate instance of an event date/time formatter based on whether
     * the system property org.opennms.events.legacyFormatter is true or false.
     *
     * @return a formatter instance
     */
    public static EventDatetimeFormatter getEventDatetimeFormatter() {
        if (Boolean.getBoolean("org.opennms.events.legacyFormatter")) {
            return new LegacyDatetimeFormatter();
        } else {
            return new ISODatetimeFormatter();
        }
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
		final StringBuilder b = new StringBuilder();
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
                final StringBuilder macAddress = new StringBuilder();
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
