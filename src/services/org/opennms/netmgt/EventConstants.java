//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 Blast Internet Services, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of Blast Internet Services, Inc.
//
// Modifications:
//
// 2003 Nov 11: Merged changes from Rackspace project
// 2003 Sep 09: Added code to support duplicate IP address handling.
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
//      http://www.blast.com/
//

package org.opennms.netmgt;

import java.util.Date;
import java.text.DateFormat;
import java.text.ParseException;

/**
 * This class holds all OpenNMS events related constants -
 * the UEI's, parm names, the event time format etc.
 *
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 *
 */
public class EventConstants
{
	/**
	 * <P>The date format string to parse a Date.toString() type string
	 * to a database timestamp using the postgres to_timestamp() built-in function</P>
	 */
	public static final String POSTGRES_DATE_FORMAT = "\'Dy Mon DD HH24:MI:SS Tz YYYY\'";

	/**
	 * The string property set on JMS messages to indicate the encoding to be used
	 */
	public static final String JMS_MSG_PROP_CHAR_ENCODING = "char_encoding";
	
	/**
	 * The value for the string property set on JMS messages to indicate the encoding to be used
	 */
	public static final String JMS_MSG_PROP_CHAR_ENCODING_VALUE = "US-ASCII";
	
	/**
	 * The string property set on JMS messages to indicate the sender service
	 */
	public static final String JMS_MSG_PROP_SENDER = "sender";
	
	/**
	 * The string property set on JMS messages broadcast from eventd
	 * - to use UEI(s) as a filter
	 */
	public static final String JMS_MSG_PROP_UEI_SELECTOR = "ueiSelector";

	//
	// the eventUEIs used by OpenNMS
	//

	/**
	 * The status query control event
	 */
	public final static String STATUS_QUERY_CONTROL_EVENT_UEI = "uei.opennms.org/internal/control/status";

	/**
	 * The start event
	 */
	public final static String START_CONTROL_EVENT_UEI = "uei.opennms.org/internal/control/start";
	
	/**
	 * The pause event
	 */
	public final static String PAUSE_CONTROL_EVENT_UEI = "uei.opennms.org/internal/control/pause";
	
	/**
	 * The resume event
	 */
	public final static String RESUME_CONTROL_EVENT_UEI = "uei.opennms.org/internal/control/resume";
	
	/**
	 * The stop event
	 */
	public final static String STOP_CONTROL_EVENT_UEI = "uei.opennms.org/internal/control/stop";
	
	/**
	 * The 'start pending' response event
	 */
	public final static String CONTROL_START_PENDING_EVENT_UEI = "uei.opennms.org/internal/control/startPending";

	/**
	 * The 'starting' response event
	 */
	public final static String CONTROL_STARTING_EVENT_UEI = "uei.opennms.org/internal/control/starting";

	/**
	 * The 'pause pending' response event
	 */
	public final static String CONTROL_PAUSE_PENDING_EVENT_UEI = "uei.opennms.org/internal/control/pausePending";
	
	/**
	 * The 'paused' response event
	 */
	public final static String CONTROL_PAUSED_EVENT_UEI = "uei.opennms.org/internal/control/paused";
	
	/**
	 * The 'resume pending' response event
	 */
	public final static String CONTROL_RESUME_PENDING_EVENT_UEI = "uei.opennms.org/internal/control/resumePending";

	/**
	 * The 'running' response event
	 */
	public final static String CONTROL_RUNNING_EVENT_UEI = "uei.opennms.org/internal/control/running";

	/**
	 * The 'stop pending' response event
	 */
	public final static String CONTROL_STOP_PENDING_EVENT_UEI = "uei.opennms.org/internal/control/stopPending";
	
	/**
	 * The 'stopped' response event
	 */
	public final static String CONTROL_STOPPED_EVENT_UEI = "uei.opennms.org/internal/control/stopped";
	
	/**
	 * The control error reponse event
	 */
	public final static String CONTROL_ERROR_EVENT_UEI = "uei.opennms.org/internal/control/error";
	
	/**
	 * The new suspect event UEI
	 */
	public final static String	NEW_SUSPECT_INTERFACE_EVENT_UEI = "uei.opennms.org/internal/discovery/newSuspect";
	
	/**
	 * The discovery pause event UEI
	 */
	public final static String DISC_PAUSE_EVENT_UEI = "uei.opennms.org/internal/capsd/discPause";

        /**
	 * The discovery resume event UEI
	 */
	public final static String DISC_RESUME_EVENT_UEI = "uei.opennms.org/internal/capsd/discResume";

        /**
	 * The update server event UEI
	 */
	public final static String	UPDATE_SERVER_EVENT_UEI = "uei.opennms.org/internal/capsd/updateServer";
	
        /**
	 * The update service event UEI
	 */
	public final static String	UPDATE_SERVICE_EVENT_UEI = "uei.opennms.org/internal/capsd/updateService";
        
        /**
	 * The add node event UEI
	 */
	public final static String	ADD_NODE_EVENT_UEI = "uei.opennms.org/internal/capsd/addNode";
	
        /**
	 * The delete node event UEI
	 */
	public final static String	DELETE_NODE_EVENT_UEI = "uei.opennms.org/internal/capsd/deleteNode";
        
        /**
	 * The add interface event UEI
	 */
	public final static String	ADD_INTERFACE_EVENT_UEI = "uei.opennms.org/internal/capsd/addInterface";
        
        /**
	 * The add interface event UEI
	 */
	public final static String	DELETE_INTERFACE_EVENT_UEI = "uei.opennms.org/internal/capsd/deleteInterface";
	
        /**
	 * The change service event UEI
	 */
	public final static String	CHANGE_SERVICE_EVENT_UEI = "uei.opennms.org/internal/capsd/changeService";
	
        /**
	 * The restart polling node event UEI
	 */
	public final static String	RESTART_POLLING_INTERFACE_EVENT_UEI = "uei.opennms.org/nodes/restartPollingInterface";
	
        /**
	 * The node added event UEI
	 */
	public final static String	NODE_ADDED_EVENT_UEI = "uei.opennms.org/nodes/nodeAdded";
	
	/**
	 * The node gained interface event UEI
	 */
	public final static String	NODE_GAINED_INTERFACE_EVENT_UEI = "uei.opennms.org/nodes/nodeGainedInterface";
	
	/**
	 * The node gained service event UEI
	 */
	public final static String	NODE_GAINED_SERVICE_EVENT_UEI = "uei.opennms.org/nodes/nodeGainedService";

	/**
	 * The node lost service event UEI
	 */
	public final static String	NODE_LOST_SERVICE_EVENT_UEI = "uei.opennms.org/nodes/nodeLostService";
	
	/**
	 * The service responsive event UEI
	 */
	public final static String	SERVICE_RESPONSIVE_EVENT_UEI = "uei.opennms.org/nodes/serviceResponsive";
	
	/**
	 * The service unresponsive event UEI
	 */
	public final static String	SERVICE_UNRESPONSIVE_EVENT_UEI = "uei.opennms.org/nodes/serviceUnresponsive";

	/**
	 * The interface down event UEI
	 */
	public final static String	INTERFACE_DOWN_EVENT_UEI = "uei.opennms.org/nodes/interfaceDown";

	/**
	 * The node down event UEI
	 */
	public final static String	NODE_DOWN_EVENT_UEI = "uei.opennms.org/nodes/nodeDown";

	/**
	 * The node up event UEI
	 */
	public final static String	NODE_UP_EVENT_UEI = "uei.opennms.org/nodes/nodeUp";

	/**
	 * The interface up event UEI
	 */
	public final static String	INTERFACE_UP_EVENT_UEI = "uei.opennms.org/nodes/interfaceUp";

	/**
	 * The node regained service event UEI
	 */
	public final static String	NODE_REGAINED_SERVICE_EVENT_UEI = "uei.opennms.org/nodes/nodeRegainedService";

	/**
	 * The delete service event UEI
	 */
	public final static String DELETE_SERVICE_EVENT_UEI = "uei.opennms.org/nodes/deleteService";
	
	/**
	 * The service deleted event UEI
	 */
	public final static String SERVICE_DELETED_EVENT_UEI = "uei.opennms.org/nodes/serviceDeleted";
	
	/**
	 * The interface deleted event UEI
	 */
	public final static String INTERFACE_DELETED_EVENT_UEI = "uei.opennms.org/nodes/interfaceDeleted";

	/**
	 * The node deleted event UEI
	 */
	public final static String NODE_DELETED_EVENT_UEI = "uei.opennms.org/nodes/nodeDeleted";

	/**
	 * The low threshold exceeded event UEI
	 */
	public final static String LOW_THRESHOLD_EVENT_UEI = "uei.opennms.org/threshold/lowThresholdExceeded";
 
	/**
	 * The high threshold exceeded event UEI
	 */
	public final static String HIGH_THRESHOLD_EVENT_UEI = "uei.opennms.org/threshold/highThresholdExceeded";   

	/**
	 * The high threshold exceeded event UEI
	 */
	public final static String HIGH_THRESHOLD_REARM_EVENT_UEI = "uei.opennms.org/threshold/highThresholdRearmed";   

	/**
	 * The high threshold exceeded event UEI
	 */
	public final static String LOW_THRESHOLD_REARM_EVENT_UEI = "uei.opennms.org/threshold/lowThresholdRearmed";   

	/**
	 * The interface index changed event
	 */
	public final static String INTERFACE_INDEX_CHANGED_EVENT_UEI = "uei.opennms.org/nodes/interfaceIndexChanged";
	
	/**	
	 * The interface supports SNMP event...generated during capability rescan when an already managed
	 * interface gains SNMP support for the first time
	*/
	public final static String INTERFACE_SUPPORTS_SNMP_EVENT_UEI = "uei.opennms.org/internal/capsd/interfaceSupportsSNMP";
	
	/**	
	 * A service scan has discovered a duplicate IP address.
	*/
	public final static String DUPLICATE_IPINTERFACE_EVENT_UEI = "uei.opennms.org/internal/capsd/duplicateIPAddress";
	
	/**
	 * The interface reparented event
	 */
	public final static String INTERFACE_REPARENTED_EVENT_UEI = "uei.opennms.org/nodes/interfaceReparented";
	
	/**
	 * The node info changed event
	 */
	public final static String NODE_INFO_CHANGED_EVENT_UEI = "uei.opennms.org/nodes/nodeInfoChanged";
	
	/**
	 * The interface IP host name changed event
	 */
	public final static String INTERFACE_IP_HOSTNAME_CHANGED_EVENT_UEI = "uei.opennms.org/nodes/interfaceIPHostNameChanged";

	/**
	 * The node label changed event
	 */
	public final static String NODE_LABEL_CHANGED_EVENT_UEI = "uei.opennms.org/nodes/nodeLabelChanged";

	/**
	 * The node label source changed event
	*/
	public final static String NODE_LABEL_SOURCE_CHANGED_EVENT_UEI = "uei.opennms.org/nodes/nodeLabelSourceChanged";

	/**
	 * The node deleted event UEI
	 */
	public final static String DUP_NODE_DELETED_EVENT_UEI = "uei.opennms.org/nodes/duplicateNodeDeleted";
	
	/**
	* The primary SNMP interface changed event.
	*/
	public final static String PRIMARY_SNMP_INTERFACE_CHANGED_EVENT_UEI = "uei.opennms.org/nodes/primarySnmpInterfaceChanged";
	
	/**
	 * The reinitialize primary SNMP interface event.
	 */
	public final static String REINITIALIZE_PRIMARY_SNMP_INTERFACE_EVENT_UEI = "uei.opennms.org/nodes/reinitializePrimarySnmpInterface";
	
	/** 
	 * Collection failed
	 */
	public final static String DATA_COLLECTION_FAILED_EVENT_UEI = "uei.opennms.org/nodes/dataCollectionFailed";
	
	/** 
	 * Collection succeeded
	 */
	public final static String DATA_COLLECTION_SUCCEEDED_EVENT_UEI = "uei.opennms.org/nodes/dataCollectionSucceeded";

	/** 
	 * Thresholding failed
	 */
	public final static String THRESHOLDING_FAILED_EVENT_UEI = "uei.opennms.org/nodes/thresholdingFailed";
	
	/** 
	 * Thresholding succeeded
	 */
	public final static String THRESHOLDING_SUCCEEDED_EVENT_UEI = "uei.opennms.org/nodes/thresholdingSucceeded";

	/** 
	 * The force interface rescan event UEI
	 */
	public final static String FORCE_RESCAN_EVENT_UEI = "uei.opennms.org/internal/capsd/forceRescan";
	
	/**
	 * The RTC subscribe event
	 */
	public final static String RTC_SUBSCRIBE_EVENT_UEI = "uei.opennms.org/internal/rtc/subscribe";

	/**
	 * The RTC unsubscribe event
	 */
	public final static String RTC_UNSUBSCRIBE_EVENT_UEI = "uei.opennms.org/internal/rtc/unsubscribe";
	
	/**
	 * A service poll returned an unknown status (due to a problem getting poll information)
	 */
	public final static String SERVICE_STATUS_UNKNOWN = "uei.opennms.org/internal/unknownServiceStatus";

	/**
	 *
	 */
	public final static String NOTIFICATION_WITHOUT_USERS = "uei.opennms.org/internal/notificationWithoutUsers";

	/**
	 * A vulnerability scan on a specific interface was initiated by the user via the web UI
	 */
	public final static String SPECIFIC_VULN_SCAN_EVENT_UEI = "uei.opennms.org/vulnscand/specificVulnerabilityScan";

	//
	// end eventUEIs
	//

	//
	// Various event parms sent
	//

	/**
	 * The nodeSysName from the node table when sent as an event parm
	 */
	public final static String PARM_NODE_SYSNAME = "nodesysname";

	/**
	 * The nodeSysDescription from the node table when sent as an event parm
	 */
	public final static String PARM_NODE_SYSDESCRIPTION = "nodesysdescription";

	/**
	 * The nodeSysOid from the node table when sent as an event parm
	 */
	public final static String PARM_NODE_SYSOID = "nodesysoid";
	
	/**
	 * The nodeSysLocation from the node table when sent as an event parm
	 */
	public final static String PARM_NODE_SYSLOCATION = "nodesyslocation";
	
	/**
	 * The nodeSysContact from the node table when sent as an event parm
	 */
	public final static String PARM_NODE_SYSCONTACT = "nodesyscontact";
	
	/**
	 * The ipHostName from the ipinterface table when sent as an event parm
	 */
	public final static String PARM_IP_HOSTNAME = "iphostname";

	/**
	 * The original ipHostName from the ipinterface table when sent as an event parm
	 */
	public final static String PARM_OLD_IP_HOSTNAME = "oldiphostname";

	
	/**
	 * Name of the method of discovery when sent as an event parm
	 */
	public final static String PARM_METHOD = "method";

	/**
	 * The interface sent as a parm of an event
	 */
	public final static String PARM_INTERFACE = "interface";
	
        /**
	 * The action sent as a parm of an event
	 */
	public final static String PARM_ACTION = "action";
        
        /**
	 * The DPName sent as a parm of an event
	 */
	public final static String PARM_DPNAME = "dpName";
	
	/**
	 * The old nodeid sent as a parm of the 'interfaceReparented' event
	 */
	public final static String PARM_OLD_NODEID = "oldNodeID";
	
	/**
	 * The new nodeid sent as a parm of the 'interfaceReparented' event
	 */
	public final static String PARM_NEW_NODEID = "newNodeID";

	/**
	 * The old ifIndex value sent as a parm of the 'interfaceIndexChanged' event
	 */
	public final static String PARM_OLD_IFINDEX = "oldIfIndex";
	
	/**
	 * The new ifIndex value sent as a parm of the 'interfaceIndexChanged' event 
	 */
	public final static String PARM_NEW_IFINDEX = "newIfIndex";
	
	/** 
	 * The nodeLabel from the node table when sent as an event parm
	 */
	public final static String PARM_NODE_LABEL = "nodelabel";
	
	/**
	 * The nodeLabelSource from the node table when sent as an event parm
	 */
	public final static String PARM_NODE_LABEL_SOURCE = "nodelabelsource";
	
	/** 
	 * The oldNodeLabel sent as a parm of an event
	 */
	public final static String PARM_OLD_NODE_LABEL = "oldNodeLabel";
	
	/** 
	 * The oldNodeLabelSource sent as a parm of an event
	 */
	public final static String PARM_OLD_NODE_LABEL_SOURCE = "oldNodeLabelSource";
	
	/** 
	 * The newNodeLabel sent as a parm of an event
	 */
	public final static String PARM_NEW_NODE_LABEL = "newNodeLabel";
	
	/** 
	 * The newNodeLabelSource sent as a parm of an event
	 */
	public final static String PARM_NEW_NODE_LABEL_SOURCE = "newNodeLabelSource";
	
	/**
	 * The nodeNetbiosName field from the node table when sent as an event parm
	 */
	public final static String PARM_NODE_NETBIOS_NAME = "nodenetbiosname";
	
	/**
	 * The nodeDomainName field from the node table when sent as an event parm
	 */
	public final static String PARM_NODE_DOMAIN_NAME= "nodedomainname";
	
	/**
	 * The operatingSystem field from the node table when sent as an event parm
	 */
	public final static String PARM_NODE_OPERATING_SYSTEM = "nodeoperatingsystem";
	
	/**
	 * The old value of the primarySnmpInterface field of the ipInterface table when sent as an
	 * event parm.
	 */
	public final static String PARM_OLD_PRIMARY_SNMP_ADDRESS = "oldPrimarySnmpAddress";
	
	/**
	 * The new value of the primarySnmpInterface field of the ipInterface table when sent as an
	 * event parm.
	 */
	public final static String PARM_NEW_PRIMARY_SNMP_ADDRESS = "newPrimarySnmpAddress";
	
	/**
	 * Service monitor qualifier when sent as an event parm
	 */
	public final static String PARM_QUALIFIER = "qualifier";
	
	/**
	 * The URL to which information is to be sent, sent as a parm
	 * to the rtc subscribe and unsubscribe events
	 */
	public final static String PARM_URL = "url";

	/**
	 * The category for which information is to be sent, sent as a parm
	 * to the rtc subscribe event
	 */
	public final static String PARM_CAT_LABEL = "catlabel";

	/**
	 * The username when sent as a parameter(like for the rtc subscribe)
	 */
	public final static String PARM_USER = "user";

	/**
	 * The passwd when sent as a parameter(like for the rtc subscribe)
	 */
	public final static String PARM_PASSWD = "passwd";
	
	/**
	 * The status of a service as returned from a service monitor
	 */
	public final static String PARM_SERVICE_STATUS = "serviceStatus";

        /**
         * The external transaction number of an event to process.
         */
        public final static String PARM_TRANSACTION_NO = "txno";

	//
	// End event parms
	//

	/**
	 * <P>An utility method to parse a string into a 'Date' instance.
	 * Note that the string should be in the locale specific DateFormat.FULL
	 * style for both the date and time</P>
	 *
	 * @see java.text.DateFormat
	 */
	public static final Date parseToDate(String timeString)
		throws ParseException
	{
		return DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL).parse(timeString);
	}

	/**
	 * <P>An utility method to format a 'Date' into a string in the
	 * local specific DateFormat.FULL style for both the date and time</P>
	 *
	 * @see java.text.DateFormat
	 */
	public static final String formatToString(Date date)
	{
		return DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL).format(date);
	}

	/**
	 * <P>An utility method to format a 'Date' into a string in the
	 * local specific DateFormat.DEFAULT style for both the date and time.
	 * This is used by the webui and a change here should get all time display
	 * in the webui changed</P>
	 *
	 * @see java.text.DateFormat
	 * @deprecated This is no longer used by the UI.  All WebUI-specific code should
	 * under the org.opennms.web packages.
	 */
	public static final String formatToUIString(Date date)
	{
		return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM).format(date);
	}
}

