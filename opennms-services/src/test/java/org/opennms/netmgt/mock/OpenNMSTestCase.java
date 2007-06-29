//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2007 Jun 10: Fix sequence for alarms and add getJdbcTemplate(). - dj@opennms.org
// 2006 Aug 22: Move anticipator verify code into runTest(). - dj@opennms.org
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
//   OpenNMS Licensing       <license@opennms.org>
//   http://www.opennms.org/
//   http://www.opennms.com/
//
// Tab Size = 8

package org.opennms.netmgt.mock;

import java.io.Reader;
import java.io.StringReader;

import junit.framework.TestCase;

import org.opennms.netmgt.config.DataSourceFactory;
import org.opennms.netmgt.config.EventdConfigManager;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.eventd.EventConfigurationManager;
import org.opennms.netmgt.eventd.EventIpcManager;
import org.opennms.netmgt.eventd.EventIpcManagerDefaultImpl;
import org.opennms.netmgt.eventd.EventIpcManagerFactory;
import org.opennms.netmgt.eventd.Eventd;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.utils.EventProxy;
import org.opennms.netmgt.utils.EventProxyException;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Log;
import org.opennms.test.mock.MockLogAppender;
import org.opennms.test.mock.MockUtil;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

public class OpenNMSTestCase extends TestCase {

    /*
     * changed ports from default because tests will fail if opennms 
     * is running.
     */
    protected static String MOCK_EVENT_CONFIG = "<EventdConfiguration\n" + 
            "   TCPPort=\"5837\"\n" + 
            "   UDPPort=\"5837\"\n" + 
            "   receivers=\"5\"\n" + 
//            "   getNextEventID=\"SELECT max(eventId)+1 from events\"\n" + 
            "   getNextEventID=\"select nextVal('eventsNxtId')\"\n" +
//            "   getNextAlarmID=\"SELECT max(alarmId)+1 from alarms\"\n" + 
            "   getNextAlarmID=\"select nextVal('alarmsNxtId')\"\n" +
            "   socketSoTimeoutRequired=\"yes\"\n" + 
            "   socketSoTimeoutPeriod=\"3000\">\n" + 
            "</EventdConfiguration>";
    
    protected static String MOCK_EVENT_CONF = "<?xml version=\"1.0\"?>\n" + 
            "<events xmlns=\"http://xmlns.opennms.org/xsd/eventconf\">\n" + 
            "   <global>\n" + 
            "       <security>\n" + 
            "           <doNotOverride>logmsg</doNotOverride>\n" + 
            "           <doNotOverride>operaction</doNotOverride>\n" + 
            "           <doNotOverride>autoaction</doNotOverride>\n" + 
            "           <doNotOverride>tticket</doNotOverride>\n" + 
            "           <doNotOverride>script</doNotOverride>\n" + 
            "       </security>\n" + 
            "   </global>\n" + 
            "        <event>\n" + 
            "                <uei>MATCH-ANY-UEI</uei>\n" + 
            "                <event-label>OpenNMS defined event: MATCH-ANY-UEI</event-label>\n" + 
            "                <descr>\n" + 
            "                        &lt;p&gt;This UEI will never be generated, but exists\n" + 
            "                        so that notifications can match any UEI for a\n" + 
            "                        particular filter rule. Useful to see all events for\n" + 
            "                        a particular node via notifications.\n" + 
            "                        &lt;/p&gt;\n" + 
            "                </descr>\n" + 
            "                <logmsg dest=\'logonly\'>\n" + 
            "                        MATCH-ANY-UEI event.\n" + 
            "                </logmsg>\n" + 
            "                <severity>Indeterminate</severity>\n" + 
            "        </event>\n" + 
            "   <event>\n" + 
            "       <uei>uei.opennms.org/internal/capsd/discPause</uei>\n" + 
            "       <event-label>OpenNMS-defined internal event: capsd discPause</event-label>\n" + 
            "       <descr>\n" + 
            "           &lt;p&gt;The services scanning engine has asked discovery to\n" + 
            "           pause due to a backlog of interfaces yet to be scanned.\n" + 
            "           &lt;/p&gt;\n" + 
            "       </descr>\n" + 
            "       <logmsg dest=\'logonly\'>\n" + 
            "           Capsd has asked Discovery to pause momentarily.\n" + 
            "       </logmsg>\n" + 
            "       <severity>Indeterminate</severity>\n" + 
            "   </event>\n" + 
            "   <event>\n" + 
            "       <uei>uei.opennms.org/internal/capsd/discResume</uei>\n" + 
            "       <event-label>OpenNMS-defined internal event: capsd discResume</event-label>\n" + 
            "       <descr>\n" + 
            "           &lt;p&gt;Capsd is approving discovery to resume adding nodes\n" + 
            "           to the Capsd queue.&lt;/p&gt;\n" + 
            "       </descr>\n" + 
            "       <logmsg dest=\'logonly\'>\n" + 
            "           Capsd is ready for Discovery to resume scheduling nodes.\n" + 
            "       </logmsg>\n" + 
            "       <severity>Indeterminate</severity>\n" + 
            "   </event>\n" + 
            "   <event>\n" + 
            "       <uei>uei.opennms.org/internal/capsd/snmpConflictsWithDb</uei>\n" + 
            "       <event-label>OpenNMS-defined capsd event: snmpConflictsWithDb</event-label>\n" + 
            "       <descr>\n" + 
            "           &lt;p&gt;During a rescan the ip interfaces as determined by snmp\n" + 
            "           conflict with the ip interfaces listed in the database for this node.\n" + 
            "           &lt;/p&gt;\n" + 
            "       </descr>\n" + 
            "       <logmsg dest=\'logndisplay\'>\n" + 
            "           IP interfaces as determined by SNMP disagree with the database.\n" + 
            "       </logmsg>\n" + 
            "       <severity>Warning</severity>\n" + 
            "   </event>\n" + 
            "   <event>\n" + 
            "       <uei>uei.opennms.org/internal/capsd/forceRescan</uei>\n" + 
            "       <event-label>OpenNMS-defined internal event: capsd forceRescan</event-label>\n" + 
            "       <descr>\n" + 
            "           &lt;p&gt;A services scan has been forced.&lt;/p&gt;\n" + 
            "           &lt;p&gt;The administrator has forced a services scan on\n" + 
            "           this node to update the list of supported\n" + 
            "           services.&lt;/p&gt;\n" + 
            "       </descr>\n" + 
            "       <logmsg dest=\'logndisplay\'>\n" + 
            "           &lt;p&gt;A services scan has been forced on this\n" + 
            "           node.&lt;/p&gt;\n" + 
            "       </logmsg>\n" + 
            "       <severity>Major</severity>\n" + 
            "   </event>\n" + 
            "   <event>\n" + 
            "       <uei>uei.opennms.org/internal/capsd/interfaceSupportsSNMP</uei>\n" + 
            "       <event-label>OpenNMS-defined internal event: capsd interfaceSupportsSNMP</event-label>\n" + 
            "       <descr>\n" + 
            "           &lt;p&gt;A services scan has verified that this interface\n" + 
            "           supports the SNMP protocol.&lt;/p&gt;\n" + 
            "       </descr>\n" + 
            "       <logmsg dest=\'logonly\'>\n" + 
            "           &lt;p&gt;A services scan has verified that this interface\n" + 
            "           supports the SNMP protocol.&lt;/p&gt;\n" + 
            "       </logmsg>\n" + 
            "       <severity>Normal</severity>\n" + 
            "   </event>\n" + 
            "   <event>\n" + 
            "       <uei>uei.opennms.org/internal/capsd/duplicateIPAddress</uei>\n" + 
            "       <event-label>OpenNMS-defined internal event: capsd duplicateIPAddress</event-label>\n" + 
            "       <descr>\n" + 
            "           &lt;p&gt;A services scan has discovered an IP address on \n" + 
            "               more than one device: IP Address: %interface%. The address\n" + 
            "           has been added to the database under all nodes that have it.\n" + 
            "           If this address is in error, please take steps to correct it.\n" + 
            "           &lt;/p&gt;\n" + 
            "       </descr>\n" + 
            "       <logmsg dest=\'logndisplay\'>\n" + 
            "           &lt;p&gt;A services scan has discovered a duplicate IP address:\n" + 
            "           %interface%.&lt;/p&gt;\n" + 
            "       </logmsg>\n" + 
            "       <severity>Minor</severity>\n" + 
            "   </event>\n" + 
            "   <event>\n" + 
            "       <uei>uei.opennms.org/internal/capsd/updateServer</uei>\n" + 
            "       <event-label>OpenNMS-defined internal event: capsd updateServer</event-label>\n" + 
            "       <descr>\n" + 
            "           &lt;p&gt;This event is an external command to add an\n" + 
            "                        interface/server mapping to the database. The required \n" + 
            "                        paramater is the IP address of the interface: %interface%, \n" + 
            "                        the NMS host server name: %host%, and the optional parameter \n" + 
            "                        of a node label: %nodelabel%. This event will cause an \n" + 
            "                        addInterface event. &lt;/p&gt;\n" + 
            "       </descr>\n" + 
            "       <logmsg dest=\'logndisplay\'>\n" + 
            "           &lt;p&gt;A request has been made to add an interface/server\n" + 
            "                        mapping as well as the interface to the database. The interface:\n" + 
            "           %interface%, the NMS host: %host%,  and node label: %nodelabel%\n" + 
            "                        are specified. &lt;/p&gt;\n" + 
            "       </logmsg>\n" + 
            "       <severity>Normal</severity>\n" + 
            "   </event>\n" + 
            "   <event>\n" + 
            "       <uei>uei.opennms.org/internal/capsd/updateService</uei>\n" + 
            "       <event-label>OpenNMS-defined internal event: capsd updateService</event-label>\n" + 
            "       <descr>\n" + 
            "           &lt;p&gt;This event is an external command to add or remove \n" + 
            "                        an interface/service mapping into the database. The required \n" + 
            "                        paramater is the IP address of the interface: %interface%, \n" + 
            "                        the service name: %service%, and the optional parameter \n" + 
            "                        action: %action%. This event will cause a changeService\n" + 
            "                        event with the specified action. &lt;/p&gt;\n" + 
            "       </descr>\n" + 
            "       <logmsg dest=\'logndisplay\'>\n" + 
            "           &lt;p&gt;A request has been made to add or remove an \n" + 
            "                        interface/service mapping, as well as add or remove a service:\n" + 
            "                        %service% from the specified interface: %interface%. &lt;/p&gt;\n" + 
            "       </logmsg>\n" + 
            "       <severity>Normal</severity>\n" + 
            "   </event>\n" + 
            "   <event>\n" + 
            "       <uei>uei.opennms.org/internal/capsd/addNode</uei>\n" + 
            "       <event-label>OpenNMS-defined internal event: capsd addNode</event-label>\n" + 
            "       <descr>\n" + 
            "           &lt;p&gt;This event is an external command to add a node\n" + 
            "           to the database. The required paramater is the IP\n" + 
            "           address for the main interface: %interface%, and\n" + 
            "           the optional parameter of a node label: %nodelabel%.&lt;/p&gt;\n" + 
            "       </descr>\n" + 
            "       <logmsg dest=\'logndisplay\'>\n" + 
            "           &lt;p&gt;A request has been made to add a node with interface:\n" + 
            "           %interface% and node label: %nodelabel%.&lt;/p&gt;\n" + 
            "       </logmsg>\n" + 
            "       <severity>Normal</severity>\n" + 
            "   </event>\n" + 
            "   <event>\n" + 
            "       <uei>uei.opennms.org/internal/capsd/deleteNode</uei>\n" + 
            "       <event-label>OpenNMS-defined internal event: capsd deleteNode</event-label>\n" + 
            "       <descr>\n" + 
            "           &lt;p&gt;This event is an external command to delete a node\n" + 
            "           from the database. The required paramater is the IP\n" + 
            "           address for one interface: %interface%.&lt;/p&gt;\n" + 
            "       </descr>\n" + 
            "       <logmsg dest=\'logndisplay\'>\n" + 
            "           &lt;p&gt;A request has been made to delete a node with interface:\n" + 
            "           %interface%.&lt;/p&gt;\n" + 
            "       </logmsg>\n" + 
            "       <severity>Normal</severity>\n" + 
            "   </event>\n" + 
            "   <event>\n" + 
            "       <uei>uei.opennms.org/internal/capsd/addInterface</uei>\n" + 
            "       <event-label>OpenNMS-defined internal event: capsd addInterface</event-label>\n" + 
            "       <descr>\n" + 
            "           &lt;p&gt;This event is an external command to add an interface\n" + 
            "           to the database. The required paramater is the IP\n" + 
            "           address for the main interface: %interface%.&lt;/p&gt;\n" + 
            "       </descr>\n" + 
            "       <logmsg dest=\'logndisplay\'>\n" + 
            "           &lt;p&gt;A request has been made to add an interface \n" + 
            "           %interface%.&lt;/p&gt;\n" + 
            "       </logmsg>\n" + 
            "       <severity>Normal</severity>\n" + 
            "   </event>\n" + 
            "   <event>\n" + 
            "       <uei>uei.opennms.org/internal/capsd/deleteInterface</uei>\n" + 
            "       <event-label>OpenNMS-defined internal event: capsd deleteInterface</event-label>\n" + 
            "       <descr>\n" + 
            "           &lt;p&gt;This event is an external command to delete an interface\n" + 
            "           from the database. The required paramater is the IP\n" + 
            "           address for the interface: %interface%.&lt;/p&gt;\n" + 
            "       </descr>\n" + 
            "       <logmsg dest=\'logndisplay\'>\n" + 
            "           &lt;p&gt;A request has been made to delete an interface:\n" + 
            "           %interface%.&lt;/p&gt;\n" + 
            "       </logmsg>\n" + 
            "       <severity>Normal</severity>\n" + 
            "   </event>\n" + 
            "   <event>\n" + 
            "       <uei>uei.opennms.org/internal/capsd/changeService</uei>\n" + 
            "       <event-label>OpenNMS-defined internal event: capsd changeService</event-label>\n" + 
            "       <descr>\n" + 
            "           &lt;p&gt;This event will add or remove a service from an interface.\n" + 
            "           The paramters include the interface, %interface%, the service,\n" + 
            "           %service%, and any required qualifiers, %parm[#2]%. The action\n" + 
            "           taken will be: %parm[#1]%.&lt;/p&gt;\n" + 
            "       </descr>\n" + 
            "       <logmsg dest=\'logndisplay\'>\n" + 
            "           &lt;p&gt;A request has been made to %parm[#1]% the %service% service\n" + 
            "           on interface: %interface%.&lt;/p&gt;\n" + 
            "       </logmsg>\n" + 
            "       <severity>Normal</severity>\n" + 
            "   </event>\n" + 
            "        <event>\n" + 
            "                <uei>uei.opennms.org/nodes/restartPollingInterface</uei>\n" + 
            "                <event-label>OpeNMS-defined node event: restartPollingInterface</event-label>\n" + 
            "                <descr> \n" + 
            "                        &lt;p&gt;This event is an external command to restart polling \n" + 
            "                        all active services on a specified interface. The requeried \n" + 
            "                        parameter is the IP address of the interface. (%parm [ipaddr]%). \n" + 
            "                        &lt;/p&gt; \n" + 
            "                </descr>\n" + 
            "                <logmsg dest=\'logndisplay\'>\n" + 
            "                        Reschedule polling to services on a specified interface(%parm[ipaddr]%).                \n" + 
            "                </logmsg>\n" + 
            "                <severity>Normal</severity>\n" + 
            "        </event>\n" + 
            "   <event>\n" + 
            "       <uei>uei.opennms.org/internal/capsd/xmlrpcNotification</uei>\n" + 
            "       <event-label>OpenNMS-defined internal event: capsd xmlrpcNotification</event-label>\n" + 
            "       <descr>\n" + 
            "           &lt;p&gt;This event will be produced by capsd to carry the process status\n" + 
            "                        information, so that the xmlrpcd could catch it, and reports to the external\n" + 
            "                        XMLRPC server. The paramters include transaction number, %parm [txNo]%, \n" + 
            "                        source event uei, %parm [srcUei]%, message, %parm [message]%, and process\n" + 
            "                        status, %parm [status]%.&lt;/p&gt;\n" + 
            "       </descr>\n" + 
            "       <logmsg dest=\'logndisplay\'>\n" + 
            "           &lt;p&gt;A request has been made to xmlrpcd to report status for \n" + 
            "           %parm [srcUei]%.&lt;/p&gt;\n" + 
            "       </logmsg>\n" + 
            "       <severity>Normal</severity>\n" + 
            "   </event>\n" + 
            "   <event>\n" + 
            "       <uei>uei.opennms.org/internal/discovery/newSuspect</uei>\n" + 
            "       <event-label>OpenNMS-defined internal event: discovery newSuspect</event-label>\n" + 
            "       <descr>\n" + 
            "           &lt;p&gt;Interface %interface% has been discovered and is\n" + 
            "           being queued for a services scan.&lt;/p&gt;\n" + 
            "       </descr>\n" + 
            "       <logmsg dest=\'logndisplay\'>\n" + 
            "           A new interface (%interface%) has been discovered and is\n" + 
            "           being queued for a services scan.\n" + 
            "       </logmsg>\n" + 
            "       <severity>Warning</severity>\n" + 
            "   </event>\n" + 
            "   <event>\n" + 
            "       <uei>uei.opennms.org/internal/interfaceManaged</uei>\n" + 
            "       <event-label>OpenNMS-defined internal event: interfaceManaged</event-label>\n" + 
            "       <descr>\n" + 
            "           &lt;p&gt;The interface %interface% is being\n" + 
            "           remanaged.&lt;/p&gt; &lt;p&gt;This interface will now\n" + 
            "           participate in service polling.&lt;/p&gt;\n" + 
            "       </descr>\n" + 
            "       <logmsg dest=\'logndisplay\'>\n" + 
            "           The interface %interface% is being remanaged.\n" + 
            "       </logmsg>\n" + 
            "       <severity>Warning</severity>\n" + 
            "   </event>\n" + 
            "   <event>\n" + 
            "       <uei>uei.opennms.org/internal/interfaceUnmanaged</uei>\n" + 
            "       <event-label>OpenNMS-defined internal event: interfaceUnmanaged</event-label>\n" + 
            "       <descr>\n" + 
            "           &lt;p&gt;The interface %interface% is being forcibly\n" + 
            "           unmanaged.&lt;/p&gt; &lt;p&gt;This interface and all\n" + 
            "           associated services will &lt;b&gt;NOT&lt;/b&gt; be polled\n" + 
            "           until the interface is remanaged.&lt;/p&gt;\n" + 
            "       </descr>\n" + 
            "       <logmsg dest=\'logndisplay\'>\n" + 
            "           The interface %interface% is being forcibly unmanaged.\n" + 
            "       </logmsg>\n" + 
            "       <severity>Major</severity>\n" + 
            "   </event>\n" + 
            "   <event>\n" + 
            "       <uei>uei.opennms.org/internal/notificationWithoutUsers</uei>\n" + 
            "       <event-label>OpenNMS-defined internal event: notificationWithoutUsers</event-label>\n" + 
            "       <descr>\n" + 
            "           &lt;p&gt;A destination path in a notification has not been\n" + 
            "           assigned any users.&lt;/p&gt;\n" + 
            "       </descr>\n" + 
            "       <logmsg dest=\'logndisplay\'>\n" + 
            "           A destination path in a notification has not been assigned\n" + 
            "           any users.\n" + 
            "       </logmsg>\n" + 
            "       <severity>Normal</severity>\n" + 
            "   </event>\n" + 
            "   <event>\n" + 
            "       <uei>uei.opennms.org/internal/notificationsTurnedOff</uei>\n" + 
            "       <event-label>OpenNMS-defined internal event: notificationsTurnedOff</event-label>\n" + 
            "       <descr>\n" + 
            "           &lt;p&gt;Notifications have been disabled.&lt;/p&gt;\n" + 
            "           &lt;p&gt;The administrator has disabled notifications on\n" + 
            "           OpenNMS. No pages or emails will be sent until notifications\n" + 
            "           are reenabled.&lt;/p&gt;\n" + 
            "       </descr>\n" + 
            "       <logmsg dest=\'logndisplay\'>\n" + 
            "           &lt;p&gt;Notifications have been disabled.&lt;/p&gt;\n" + 
            "       </logmsg>\n" + 
            "       <severity>Major</severity>\n" + 
            "   </event>\n" + 
            "   <event>\n" + 
            "       <uei>uei.opennms.org/internal/notificationsTurnedOn</uei>\n" + 
            "       <event-label>OpenNMS-defined internal event: notificationsTurnedOn</event-label>\n" + 
            "       <descr>\n" + 
            "           &lt;p&gt;Notifications have been enabled.&lt;/p&gt;\n" + 
            "           &lt;p&gt;The administrator has enabled notifications on\n" + 
            "           OpenNMS. Pages and/or emails will be sent based upon receipt\n" + 
            "           of important events.&lt;/p&gt;\n" + 
            "       </descr>\n" + 
            "       <logmsg dest=\'logndisplay\'>\n" + 
            "           &lt;p&gt;Notifications have been enabled.&lt;/p&gt;\n" + 
            "       </logmsg>\n" + 
            "       <severity>Major</severity>\n" + 
            "   </event>\n" + 
            "   <event>\n" + 
            "       <uei>uei.opennms.org/internal/reloadEventConfig</uei>\n" + 
            "       <event-label>OpenNMS-defined internal event: reloadEventConfig</event-label>\n" + 
            "       <descr>\n" + 
            "           &lt;p&gt;The event configuration files have changed. The\n" + 
            "           event services will now restart to detect the\n" + 
            "           changes.&lt;/p&gt;\n" + 
            "       </descr>\n" + 
            "       <logmsg dest=\'logonly\'>\n" + 
            "           &lt;p&gt;The event configuration files have\n" + 
            "           changed.&lt;/p&gt;\n" + 
            "       </logmsg>\n" + 
            "       <severity>Major</severity>\n" + 
            "   </event>\n" + 
            "   <event>\n" + 
            "       <uei>uei.opennms.org/internal/reloadPollerConfig</uei>\n" + 
            "       <event-label>OpenNMS-defined internal event: reloadPollerConfig</event-label>\n" + 
            "       <descr>\n" + 
            "           &lt;p&gt;The administrator has changed the poller\n" + 
            "           configuration files. The pollers and related services will\n" + 
            "           now restart to detect the changes.&lt;/p&gt;\n" + 
            "       </descr>\n" + 
            "       <logmsg dest=\'logndisplay\'>\n" + 
            "           &lt;p&gt;The poller configuration files have\n" + 
            "           changed.&lt;/p&gt;\n" + 
            "       </logmsg>\n" + 
            "       <severity>Major</severity>\n" + 
            "   </event>\n" + 
            "   <event>\n" + 
            "       <uei>uei.opennms.org/internal/reloadScriptConfig</uei>\n" + 
            "       <event-label>OpenNMS-defined internal event: reloadScriptConfig</event-label>\n" + 
            "       <descr>\n" + 
            "           &lt;p&gt;The administrator has changed the ScriptD\n" + 
            "           configuration. ScriptD will load the new configuration.&lt;/p&gt;\n" + 
            "       </descr>\n" + 
            "       <logmsg dest=\'logndisplay\'>\n" + 
            "           &lt;p&gt;The ScriptD configuration files have changed.&lt;/p&gt;\n" + 
            "       </logmsg>\n" + 
            "       <severity>Major</severity>\n" + 
            "   </event>\n" + 
            "   <event>\n" + 
            "       <uei>uei.opennms.org/internal/restartSCM</uei>\n" + 
            "       <event-label>OpenNMS-defined internal event: restartSCM</event-label>\n" + 
            "       <descr>\n" + 
            "           &lt;p&gt;SCM has been asked to restart.&lt;/p&gt;\n" + 
            "       </descr>\n" + 
            "       <logmsg dest=\'logndisplay\'>\n" + 
            "           SCM has been asked to restart.\n" + 
            "       </logmsg>\n" + 
            "       <severity>Normal</severity>\n" + 
            "   </event>\n" + 
            "   <event>\n" + 
            "       <uei>uei.opennms.org/internal/rtc/subscribe</uei>\n" + 
            "       <event-label>OpenNMS-defined internal event: rtc subscribe</event-label>\n" + 
            "       <descr>\n" + 
            "           &lt;p&gt;This event is generated to RTC by any process that\n" + 
            "           wishes to receive POSTs of RTC data.&lt;/p&gt;\n" + 
            "       </descr>\n" + 
            "       <logmsg dest=\'logonly\'>\n" + 
            "           A subscription to RTC for the %parm[viewname]% for\n" + 
            "           %parm[url]% has been generated.\n" + 
            "       </logmsg>\n" + 
            "       <severity>Indeterminate</severity>\n" + 
            "   </event>\n" + 
            "   <event>\n" + 
            "       <uei>uei.opennms.org/internal/rtc/unsubscribe</uei>\n" + 
            "       <event-label>OpenNMS-defined internal event: rtc unsubscribe</event-label>\n" + 
            "       <descr>\n" + 
            "           &lt;p&gt;This event is generated to RTC by any subscribed\n" + 
            "           process that wishes to discontinue receipt of POSTs of RTC\n" + 
            "           data.&lt;/p&gt;\n" + 
            "       </descr>\n" + 
            "       <logmsg dest=\'logonly\'>\n" + 
            "           Unsubscribe request received from %parm[url]%.\n" + 
            "       </logmsg>\n" + 
            "       <severity>Indeterminate</severity>\n" + 
            "   </event>\n" + 
            "   <event>\n" + 
            "       <uei>uei.opennms.org/internal/serviceManaged</uei>\n" + 
            "       <event-label>OpenNMS-defined internal event: serviceManaged</event-label>\n" + 
            "       <descr>\n" + 
            "           &lt;p&gt;The service %service% on interface %interface% is\n" + 
            "           being remanaged.&lt;/p&gt;\n" + 
            "       </descr>\n" + 
            "       <logmsg dest=\'logndisplay\'>\n" + 
            "           The service %service% on interface %interface% is being\n" + 
            "           remanaged.\n" + 
            "       </logmsg>\n" + 
            "       <severity>Warning</severity>\n" + 
            "   </event>\n" + 
            "   <event>\n" + 
            "       <uei>uei.opennms.org/internal/serviceUnmanaged</uei>\n" + 
            "       <event-label>OpenNMS-defined internal event: serviceUnmanaged</event-label>\n" + 
            "       <descr>\n" + 
            "           &lt;p&gt;The service %service% on interface %interface% is\n" + 
            "           being forcibly unmanaged.&lt;/p&gt;\n" + 
            "       </descr>\n" + 
            "       <logmsg dest=\'logndisplay\'>\n" + 
            "           The service %service% on interface %interface% is being\n" + 
            "           forcibly unmanaged.\n" + 
            "       </logmsg>\n" + 
            "       <severity>Major</severity>\n" + 
            "   </event>\n" + 
            "   <event>\n" + 
            "       <uei>uei.opennms.org/internal/unknownServiceStatus</uei>\n" + 
            "       <event-label>OpenNMS-defined internal event: unknownServiceStatus</event-label>\n" + 
            "       <descr>\n" + 
            "           &lt;p&gt;The Scheduler has received an unrecognized service\n" + 
            "           status from a scheduler.&lt;/p&gt;\n" + 
            "       </descr>\n" + 
            "       <logmsg dest=\'logonly\'>\n" + 
            "           The Scheduler has received an unrecognized service status\n" + 
            "           from a scheduler.\n" + 
            "       </logmsg>\n" + 
            "       <severity>Indeterminate</severity>\n" + 
            "   </event>\n" + 
            "   <event>\n" + 
            "       <uei>uei.opennms.org/nodes/dataCollectionFailed</uei>\n" + 
            "       <event-label>OpenNMS-defined node event: dataCollectionFailed</event-label>\n" + 
            "       <descr>\n" + 
            "           &lt;p&gt;%service% data collection on interface %interface%\n" + 
            "           failed.&lt;/p&gt;\n" + 
            "       </descr>\n" + 
            "       <logmsg dest=\'logndisplay\'>\n" + 
            "           %service% data collection on interface %interface% failed.\n" + 
            "       </logmsg>\n" + 
            "       <severity>Warning</severity>\n" + 
            "   </event>\n" + 
            "   <event>\n" + 
            "       <uei>uei.opennms.org/nodes/dataCollectionSucceeded</uei>\n" + 
            "       <event-label>OpenNMS-defined node event: dataCollectionSucceeded</event-label>\n" + 
            "       <descr>\n" + 
            "           &lt;p&gt;%service% data collection on interface %interface%\n" + 
            "           previously failed and has been restored.&lt;/p&gt;\n" + 
            "       </descr>\n" + 
            "       <logmsg dest=\'logndisplay\'>\n" + 
            "           %service% data collection on interface %interface% prevously\n" + 
            "           failed and has been restored.\n" + 
            "       </logmsg>\n" + 
            "       <severity>Cleared</severity>\n" + 
            "   </event>\n" + 
            "   <event>\n" + 
            "       <uei>uei.opennms.org/nodes/deleteService</uei>\n" + 
            "       <event-label>OpenNMS-defined node event: deleteService</event-label>\n" + 
            "       <descr>\n" + 
            "           &lt;p&gt;Due to excessive downtime, the %service% service on\n" + 
            "           interface %interface% has been scheduled for\n" + 
            "           deletion.&lt;/p&gt; &lt;p&gt;When a service has been down\n" + 
            "           for one week, it is determined to have been removed and will\n" + 
            "           be deleted. If the service is later rediscovered, it will be\n" + 
            "           re-added and associated with the appropriate\n" + 
            "           interface.&lt;/p&gt; &lt;p&gt;If this is the only service\n" + 
            "           associated with an interface, the interface will be\n" + 
            "           scheduled for deletion as well, with the generation of the\n" + 
            "           deleteInterface event.&lt;/p&gt;\n" + 
            "       </descr>\n" + 
            "       <logmsg dest=\'logndisplay\'>\n" + 
            "           The %service% service on interface %interface% has been\n" + 
            "           scheduled for deletion.\n" + 
            "       </logmsg>\n" + 
            "       <severity>Minor</severity>\n" + 
            "   </event>\n" + 
            "   <event>\n" + 
            "       <uei>uei.opennms.org/nodes/duplicateNodeDeleted</uei>\n" + 
            "       <event-label>OpenNMS-defined node event: duplicateNodeDeleted</event-label>\n" + 
            "       <descr>\n" + 
            "           &lt;p&gt;Node #&lt;a\n" + 
            "           href=\"element/node.jsp?node=%nodeid%\"&gt;%nodeid%&lt;/a&gt;\n" + 
            "           was determined to be a duplicate node and is being flagged\n" + 
            "           for deletion.&lt;/p&gt;\n" + 
            "       </descr>\n" + 
            "       <logmsg dest=\'logndisplay\'>\n" + 
            "           &lt;p&gt;Node #&lt;a\n" + 
            "           href=\"element/node.jsp?node=%nodeid%\"&gt;%nodeid%&lt;/a&gt;\n" + 
            "           was determined to be a duplicate node and is being flagged\n" + 
            "           for deletion.&lt;/p&gt;\n" + 
            "       </logmsg>\n" + 
            "       <severity>Minor</severity>\n" + 
            "   </event>\n" + 
            "   <event>\n" + 
            "       <uei>uei.opennms.org/nodes/interfaceDeleted</uei>\n" + 
            "       <event-label>OpenNMS-defined node event: interfaceDeleted</event-label>\n" + 
            "       <descr>\n" + 
            "           &lt;p&gt;Interface %interface% deleted from node #&lt;a\n" + 
            "           href=\"element/node.jsp?node=%nodeid%\"&gt;\n" + 
            "           %nodeid%&lt;/a&gt;&lt;/p&gt; &lt;p&gt;This event is\n" + 
            "           generated following an extended outage for a service, in\n" + 
            "           which that service is the only service associated with an\n" + 
            "           interface. If the service is later rediscovered, a new\n" + 
            "           interface will be added and the service will be associated\n" + 
            "           with that new interface.&lt;/p&gt;\n" + 
            "       </descr>\n" + 
            "       <logmsg dest=\'logndisplay\'>\n" + 
            "           Interface %interface% deleted from node #&lt;a\n" + 
            "           href=\"element/node.jsp?node=%nodeid%\"&gt;%nodeid%&lt;/a&gt;.\n" + 
            "       </logmsg>\n" + 
            "       <severity>Major</severity>\n" + 
            "   </event>\n" + 
            "   <event>\n" + 
            "       <uei>uei.opennms.org/nodes/interfaceDown</uei>\n" + 
            "       <event-label>OpenNMS-defined node event: interfaceDown</event-label>\n" + 
            "       <descr>\n" + 
            "           &lt;p&gt;All services are down on interface %interface%\n" + 
            "           &lt;/p&gt; &lt;p&gt;This event is generated when node outage\n" + 
            "           processing determines that the critical service or all\n" + 
            "           services on the interface are now down &lt;/p&gt; &lt;p&gt;\n" + 
            "           New outage records have been created and service level\n" + 
            "           availability calculations will be impacted until this outage\n" + 
            "           is resolved.&lt;/p&gt;\n" + 
            "       </descr>\n" + 
            "       <logmsg dest=\'logndisplay\'>\n" + 
            "           Interface %interface% is down.\n" + 
            "       </logmsg>\n" + 
            "       <severity>Major</severity>\n" + 
            "   </event>\n" + 
            "   <event>\n" + 
            "       <uei>uei.opennms.org/nodes/interfaceIPHostNameChanged</uei>\n" + 
            "       <event-label>OpenNMS-defined node event: interfaceIPHostNameChanged</event-label>\n" + 
            "       <descr>\n" + 
            "           &lt;p&gt;The hostname for this node changed.&lt;/p&gt;\n" + 
            "       </descr>\n" + 
            "       <logmsg dest=\'logndisplay\'>\n" + 
            "           The hostname for this node changed.\n" + 
            "       </logmsg>\n" + 
            "       <severity>Minor</severity>\n" + 
            "   </event>\n" + 
            "   <event>\n" + 
            "       <uei>uei.opennms.org/nodes/interfaceIndexChanged</uei>\n" + 
            "       <event-label>OpenNMS-defined node event: interfaceIndexChanged</event-label>\n" + 
            "       <descr>\n" + 
            "           &lt;p&gt;SNMP Interface Index %parm[oldIfIndex]% has changed\n" + 
            "           to %parm[newIfIndex]% on %interface%&lt;/p&gt; &lt;p&gt;The\n" + 
            "           ifIndex, or unique numeric identifier of an SNMP device\'s\n" + 
            "           interfaces, can be reordered by the SNMP agent. Usually this\n" + 
            "           happens if interfaces are added or removed, or by a change\n" + 
            "           in administrative or operational status.&lt;/p&gt;\n" + 
            "           &lt;p&gt;This is typically not a reason for concern, but you\n" + 
            "           should be aware that the active configuration on this SNMP\n" + 
            "           device has changed, and the hardware configuration may have\n" + 
            "           been impacted as well.&lt;/p&gt;\n" + 
            "       </descr>\n" + 
            "       <logmsg dest=\'logndisplay\'>\n" + 
            "           &lt;p&gt;SNMP Interface Index %parm[oldIfIndex]% has changed\n" + 
            "           to %parm[newIfIndex]% on %interface%&lt;/p&gt;\n" + 
            "       </logmsg>\n" + 
            "       <severity>Minor</severity>\n" + 
            "   </event>\n" + 
            "   <event>\n" + 
            "       <uei>uei.opennms.org/nodes/interfaceReparented</uei>\n" + 
            "       <event-label>OpenNMS-defined node event: interfaceReparented</event-label>\n" + 
            "       <descr>\n" + 
            "           &lt;p&gt;Interface %interface% has been reparented under\n" + 
            "           node %parm[newNodeID]% from node\n" + 
            "           %parm[oldNodeID]%.&lt;/p&gt; &lt;p&gt;Usually this happens\n" + 
            "           after a services scan discovers that a node with multiple\n" + 
            "           interfaces is now running an SNMP agent and is therefore\n" + 
            "           able to reparent the node\'s interfaces under a single node\n" + 
            "           identifier.&lt;/p&gt; &lt;p&gt;This is typically not a\n" + 
            "           reason for concern, but you should be aware that the node\n" + 
            "           association of this interface has changed.&lt;/p&gt;\n" + 
            "       </descr>\n" + 
            "       <logmsg dest=\'logndisplay\'>\n" + 
            "           %interface% has been reparented under node %parm[newNodeID]%\n" + 
            "           from node %parm[oldNodeID]%.\n" + 
            "       </logmsg>\n" + 
            "       <severity>Warning</severity>\n" + 
            "   </event>\n" + 
            "   <event>\n" + 
            "       <uei>uei.opennms.org/nodes/interfaceUp</uei>\n" + 
            "       <event-label>OpenNMS-defined node event: interfaceUp</event-label>\n" + 
            "       <descr>\n" + 
            "           &lt;p&gt;The interface %interface% which was previously down\n" + 
            "           is now up.&lt;/p&gt; &lt;p&gt;This event is generated when\n" + 
            "           node outage processing determines that the critical service\n" + 
            "           or all services on the interface are restored. &lt;/p&gt;\n" + 
            "           &lt;p&gt;This event will cause any active outages associated\n" + 
            "           with this interface to be cleared.&lt;/p&gt;\n" + 
            "       </descr>\n" + 
            "       <logmsg dest=\'logndisplay\'>\n" + 
            "           Interface %interface% is up.\n" + 
            "       </logmsg>\n" + 
            "       <severity>Cleared</severity>\n" + 
            "   </event>\n" + 
            "   <event>\n" + 
            "       <uei>uei.opennms.org/nodes/nodeAdded</uei>\n" + 
            "       <event-label>OpenNMS-defined node event: nodeAdded</event-label>\n" + 
            "       <descr>\n" + 
            "           &lt;p&gt;A new node (%parm[nodelabel]%) was discovered by\n" + 
            "           OpenNMS.&lt;/p&gt;\n" + 
            "       </descr>\n" + 
            "       <logmsg dest=\'logndisplay\'>\n" + 
            "           A new node (%parm[nodelabel]%) was discovered by OpenNMS.\n" + 
            "       </logmsg>\n" + 
            "       <severity>Warning</severity>\n" + 
            "   </event>\n" + 
            "   <event>\n" + 
            "       <uei>uei.opennms.org/nodes/nodeDeleted</uei>\n" + 
            "       <event-label>OpenNMS-defined node event: nodeDeleted</event-label>\n" + 
            "       <descr>\n" + 
            "           &lt;p&gt;Node %nodeid% was deleted.&lt;/p&gt; \n" + 
            "           &lt;p&gt;OpenNMS will delete any node\n" + 
            "           that is down for seven (7) consecutive days, or via operator action. \n" + 
            "           Nodes are considered \"down\" if there are no interfaces that have\n" + 
            "           pollable services associated with them.&lt;/p&gt;\n" + 
            "           &lt;p&gt;If a node becomes active again\n" + 
            "           &lt;i&gt;after&lt;/i&gt; it has been deleted, it will be\n" + 
            "           rediscovered in the next, daily discovery cycle and will be\n" + 
            "           re-added to OpenNMS\'s database as a new node. It will be\n" + 
            "           disassociated with any historic outage\n" + 
            "           information.&lt;/p&gt;\n" + 
            "       </descr>\n" + 
            "       <logmsg dest=\'logndisplay\'>\n" + 
            "           Node &lt;a\n" + 
            "           href=\"element/node.jsp?node=%nodeid%\"&gt;%nodeid%&lt;/a&gt;\n" + 
            "           was deleted.\n" + 
            "       </logmsg>\n" + 
            "       <severity>Minor</severity>\n" + 
            "   </event>\n" + 
            "   <event>\n" + 
            "       <uei>uei.opennms.org/nodes/nodeDown</uei>\n" + 
            "       <event-label>OpenNMS-defined node event: nodeDown</event-label>\n" + 
            "       <descr>\n" + 
            "           &lt;p&gt;All interfaces on node %parm[nodelabel]% are\n" + 
            "           down.&lt;/p&gt; &lt;p&gt;This event is generated when node\n" + 
            "           outage processing determines that all interfaces on the node\n" + 
            "           are down.&lt;/p&gt; &lt;p&gt;New outage records have been\n" + 
            "           created and service level availability calculations will be\n" + 
            "           impacted until this outage is resolved.&lt;/p&gt;\n" + 
            "       </descr>\n" + 
            "       <logmsg dest=\'logndisplay\'>\n" + 
            "           Node %parm[nodelabel]% is down.\n" + 
            "       </logmsg>\n" + 
            "       <severity>Major</severity>\n" +
            "        <alarm-data reduction-key=\"%uei%:%dpname%:%nodeid%\" alarm-type=\"1\" />\n"+
            "   </event>\n" + 
            "   <event>\n" + 
            "       <uei>uei.opennms.org/nodes/nodeGainedInterface</uei>\n" + 
            "       <event-label>OpenNMS-defined node event: nodeGainedInterface</event-label>\n" + 
            "       <descr>\n" + 
            "           &lt;p&gt;Interface %interface% has been associated with Node\n" + 
            "           #&lt;a\n" + 
            "           href=\"element/node.jsp?node=%nodeid%\"&gt;%nodeid%&lt;/a&gt;.&lt;/p&gt;\n" + 
            "       </descr>\n" + 
            "       <logmsg dest=\'logndisplay\'>\n" + 
            "           Interface %interface% has been associated with Node #&lt;a\n" + 
            "           href=\"element/node.jsp?node=%nodeid%\"&gt;%nodeid%&lt;/a&gt;.\n" + 
            "       </logmsg>\n" + 
            "       <severity>Warning</severity>\n" + 
            "   </event>\n" + 
            "   <event>\n" + 
            "       <uei>uei.opennms.org/nodes/nodeGainedService</uei>\n" + 
            "       <event-label>OpenNMS-defined node event: nodeGainedService</event-label>\n" + 
            "       <descr>\n" + 
            "           &lt;p&gt;A service scan has identified the %service% service\n" + 
            "           on interface %interface%.&lt;/p&gt; &lt;p&gt;If this\n" + 
            "           interface (%interface%) is within the list of ranges and\n" + 
            "           specific addresses to be managed by OpenNMS, this service\n" + 
            "           will be scheduled for regular availability checks.&lt;/p&gt;\n" + 
            "       </descr>\n" + 
            "       <logmsg dest=\'logndisplay\'>\n" + 
            "           The %service% service has been discovered on interface\n" + 
            "           %interface%.\n" + 
            "       </logmsg>\n" + 
            "       <severity>Warning</severity>\n" + 
            "   </event>\n" + 
            "   <event>\n" + 
            "       <uei>uei.opennms.org/internal/poller/suspendPollingService</uei>\n" + 
            "       <event-label>OpenNMS-defined poller event: suspendPollingService</event-label>\n" + 
            "       <descr>\n" + 
            "           &lt;p&gt;A forced rescan has identified the %service% service\n" + 
            "           on interface %interface% as no longer part of any poller package,\n" + 
            "           or the service has been unmanaged.\n" + 
            "           &lt;/p&gt; Polling will be discontinued.&lt;/p&gt;\n" + 
            "       </descr>\n" + 
            "       <logmsg dest=\'logndisplay\'>\n" + 
            "           Polling will be discontinued for %service% service on interface\n" + 
            "           %interface%.\n" + 
            "       </logmsg>\n" + 
            "       <severity>Warning</severity>\n" + 
            "   </event>\n" + 
            "   <event>\n" + 
            "       <uei>uei.opennms.org/internal/poller/resumePollingService</uei>\n" + 
            "       <event-label>OpenNMS-defined poller event: resumePollingService</event-label>\n" + 
            "       <descr>\n" + 
            "           &lt;p&gt;A forced rescan has identified the %service% service\n" + 
            "           on interface %interface% as covered by a poller package, and\n" + 
            "           managed.\n" + 
            "           &lt;/p&gt; Polling will begin in accordance with the package and\n" + 
            "           any applicable outage calendar.&lt;/p&gt;\n" + 
            "       </descr>\n" + 
            "       <logmsg dest=\'logndisplay\'>\n" + 
            "           Polling will begin/resume for %service% service on interface\n" + 
            "           %interface%.\n" + 
            "       </logmsg>\n" + 
            "       <severity>Warning</severity>\n" + 
            "   </event>\n" + 
            "   <event>\n" + 
            "       <uei>uei.opennms.org/nodes/nodeInfoChanged</uei>\n" + 
            "       <event-label>OpenNMS-defined node event: nodeInfoChanged</event-label>\n" + 
            "       <descr>\n" + 
            "           &lt;p&gt;Node information has changed for node\n" + 
            "           #%nodeid%.&lt;/p&gt;\n" + 
            "       </descr>\n" + 
            "       <logmsg dest=\'logndisplay\'>\n" + 
            "           &lt;p&gt;Node information has changed for &lt;a\n" + 
            "           href=\"element/node.jsp?node=%nodeid%\"&gt;%nodeid%&lt;/a&gt;.&lt;/p&gt;\n" + 
            "       </logmsg>\n" + 
            "       <severity>Warning</severity>\n" + 
            "   </event>\n" + 
            "   <event>\n" + 
            "       <uei>uei.opennms.org/nodes/nodeLabelChanged</uei>\n" + 
            "       <event-label>OpenNMS-defined node event: nodeLabelChanged</event-label>\n" + 
            "       <descr>\n" + 
            "           &lt;p&gt;Node #&lt;a\n" + 
            "           href=\"element/node.jsp?node=%nodeid%\"&gt;%nodeid%&lt;/a&gt;\'s\n" + 
            "           label was changed from \"%parm[oldNodeLabel]%\" to\n" + 
            "           \"%parm[newNodeLabel]%\".&lt;/p&gt;\n" + 
            "       </descr>\n" + 
            "       <logmsg dest=\'logndisplay\'>\n" + 
            "           Node #&lt;a\n" + 
            "           href=\"element/node.jsp?node=%nodeid%\"&gt;%nodeid%&lt;/a&gt;\'s\n" + 
            "           label was changed from \"%parm[oldNodeLabel]%\" to\n" + 
            "           \"%parm[newNodeLabel]%\".\n" + 
            "       </logmsg>\n" + 
            "       <severity>Normal</severity>\n" + 
            "   </event>\n" + 
            "   <event>\n" + 
            "       <uei>uei.opennms.org/nodes/nodeLostService</uei>\n" + 
            "       <event-label>OpenNMS-defined node event: nodeLostService</event-label>\n" + 
            "       <descr>\n" + 
            "           &lt;p&gt;A %service% outage was identified on interface\n" + 
            "           %interface%.&lt;/p&gt; &lt;p&gt;A new Outage record has been\n" + 
            "           created and service level availability calculations will be\n" + 
            "           impacted until this outage is resolved.&lt;/p&gt;\n" + 
            "       </descr>\n" + 
            "       <logmsg dest=\'logndisplay\'>\n" + 
            "           %service% outage identified on interface %interface%.\n" + 
            "       </logmsg>\n" + 
            "       <severity>Major</severity>\n" + 
            "   </event>\n" + 
            "   <event>\n" + 
            "       <uei>uei.opennms.org/nodes/nodeRegainedService</uei>\n" + 
            "       <event-label>OpenNMS-defined node event: nodeRegainedService</event-label>\n" + 
            "       <descr>\n" + 
            "           &lt;p&gt;The %service% service on interface %interface% was\n" + 
            "           previously down and has been restored.&lt;/p&gt;\n" + 
            "           &lt;p&gt;This event is generated when a service which had\n" + 
            "           previously failed polling attempts is again responding to\n" + 
            "           polls by OpenNMS. &lt;/p&gt; &lt;p&gt;This event will cause\n" + 
            "           any active outages associated with this service/interface\n" + 
            "           combination to be cleared.&lt;/p&gt;\n" + 
            "       </descr>\n" + 
            "       <logmsg dest=\'logndisplay\'>\n" + 
            "           The %service% outage on interface %interface% has been\n" + 
            "           cleared. Service is restored.\n" + 
            "       </logmsg>\n" + 
            "       <severity>Cleared</severity>\n" + 
            "   </event>\n" + 
            "   <event>\n" + 
            "       <uei>uei.opennms.org/nodes/nodeUp</uei>\n" + 
            "       <event-label>OpenNMS-defined node event: nodeUp</event-label>\n" + 
            "       <descr>\n" + 
            "           &lt;p&gt;Node %parm[nodelabel]% which was previously down is\n" + 
            "           now up.&lt;/p&gt; &lt;p&gt;This event is generated when node\n" + 
            "           outage processing determines that all interfaces on the node\n" + 
            "           are up.&lt;/p&gt; &lt;p&gt;This event will cause any active\n" + 
            "           outages associated with this node to be cleared.&lt;/p&gt;\n" + 
            "       </descr>\n" + 
            "       <logmsg dest=\'logndisplay\'>\n" + 
            "           Node %parm[nodelabel]% is up.\n" + 
            "       </logmsg>\n" + 
            "       <severity>Cleared</severity>\n" + 
            "       <alarm-data reduction-key=\"%uei%:%dpname%:%nodeid%\" alarm-type=\"2\" clear-uei=\"uei.opennms.org/nodes/nodeDown\" />\n"+
            "   </event>\n" + 
            "   <event>\n" + 
            "       <uei>uei.opennms.org/nodes/primarySnmpInterfaceChanged</uei>\n" + 
            "       <event-label>OpenNMS-defined node event: primarySnmpInterfaceChanged</event-label>\n" + 
            "       <descr>\n" + 
            "           &lt;p&gt;This event indicates that the interface selected\n" + 
            "           for SNMP data collection for this node has changed. This is\n" + 
            "           usually due to a network or address reconfiguration\n" + 
            "           impacting this device.&lt;/p&gt;\n" + 
            "       </descr>\n" + 
            "       <logmsg dest=\'logndisplay\'>\n" + 
            "           Primary SNMP interface for node &lt;a\n" + 
            "           href=\"element/node.jsp?node=%nodeid%\"&gt;%nodeid%&lt;/a&gt;\n" + 
            "           has changed from %parm[oldPrimarySnmpAddress]% to\n" + 
            "           %parm[newPrimarySnmpAddress]%.\n" + 
            "       </logmsg>\n" + 
            "       <severity>Warning</severity>\n" + 
            "   </event>\n" + 
            "   <event>\n" + 
            "       <uei>uei.opennms.org/nodes/reinitializePrimarySnmpInterface</uei>\n" + 
            "       <event-label>OpenNMS-defined node event: reinitializePrimarySnmpInterface</event-label>\n" + 
            "       <descr>\n" + 
            "           &lt;p&gt;A change in configuration on this node has been\n" + 
            "           detected and the SNMP data collection mechanism is being\n" + 
            "           triggered to refresh its required profile of the remote\n" + 
            "           node.&lt;/p&gt;\n" + 
            "       </descr>\n" + 
            "       <logmsg dest=\'logndisplay\'>\n" + 
            "           SNMP information on %interface% is being refreshed for data\n" + 
            "           collection purposes.\n" + 
            "       </logmsg>\n" + 
            "       <severity>Warning</severity>\n" + 
            "   </event>\n" + 
            "   <event>\n" + 
            "                <uei>uei.opennms.org/nodes/serviceResponsive</uei>\n" + 
            "       <event-label>OpenNMS-defined node event: serviceResponsive</event-label>\n" + 
            "       <descr>\n" + 
            "           &lt;p&gt;The %service% service which was previously unresponsive\n" + 
            "           is now responding normally on interface %interface%.&lt;/p&gt; \n" + 
            "       </descr>\n" + 
            "       <logmsg dest=\'logndisplay\'>\n" + 
            "           %service% is responding normally on interface %interface%.\n" + 
            "       </logmsg>\n" + 
            "       <severity>Cleared</severity>\n" + 
            "        </event>\n" + 
            "   <event>\n" + 
            "       <uei>uei.opennms.org/nodes/serviceDeleted</uei>\n" + 
            "       <event-label>OpenNMS-defined node event: serviceDeleted</event-label>\n" + 
            "       <descr>\n" + 
            "           &lt;p&gt;Service %service% was deleted from interface\n" + 
            "           %interface%, associated with Node ID# %nodeid%.&lt;/p&gt;\n" + 
            "           &lt;p&gt;When a service is deleted from an interface, it is\n" + 
            "           due to extended downtime of that service of over seven (7)\n" + 
            "           days.&lt;/p&gt; &lt;p&gt;If a previously deleted service\n" + 
            "           becomes active again on an interface, it will be re-added to\n" + 
            "           the OpenNMS database as a new occurrence of that service and\n" + 
            "           will be disassociated with any historic outages.&lt;/p&gt;\n" + 
            "       </descr>\n" + 
            "       <logmsg dest=\'logndisplay\'>\n" + 
            "           The %service% service was deleted from interface\n" + 
            "           %interface%.\n" + 
            "       </logmsg>\n" + 
            "       <severity>Major</severity>\n" + 
            "   </event>\n" + 
            "   <event>\n" + 
            "                <uei>uei.opennms.org/nodes/serviceUnresponsive</uei>\n" + 
            "       <event-label>OpenNMS-defined node event: serviceUnresponsive</event-label>\n" + 
            "       <descr>\n" + 
            "           &lt;p&gt;The %service% service is up but was unresponsive \n" + 
            "           during the last poll on interface %interface%.&lt;/p&gt; \n" + 
            "       </descr>\n" + 
            "       <logmsg dest=\'logndisplay\'>\n" + 
            "           %service% is up but unresponsive on interface %interface%.\n" + 
            "       </logmsg>\n" + 
            "       <severity>Major</severity>\n" + 
            "        </event>\n" + 
            "   <event>\n" + 
            "       <uei>uei.opennms.org/threshold/highThresholdExceeded</uei>\n" + 
            "       <event-label>OpenNMS-defined threshold event: highThresholdExceeded</event-label>\n" + 
            "       <descr>\n" + 
            "           &lt;p&gt;High threshold exceeded for %service% datasource\n" + 
            "                        %parm[ds]% on interface %interface%, parms: %parm[all]%&lt;/p&gt;\n" + 
            "                        &lt;p&gt;By default, OpenNMS watches some key parameters\n" + 
            "                        on devices in your network and will alert you with\n" + 
            "                        an event if certain conditions arise. For example, if\n" + 
            "                        the CPU utilization on your Cisco router maintains an\n" + 
            "                        inordinately high percentage of utilization for an extended\n" + 
            "                        period, an event will be generated. These thresholds are\n" + 
            "                        determined and configured based on vendor recommendations,\n" + 
            "                        tempered with real-world experience in working\n" + 
            "                        deployments.&lt;/p&gt; &lt;p&gt;This specific event\n" + 
            "                        indicates that a high threshold was exceeded.&lt;/p&gt;\n" + 
            "       </descr>\n" + 
            "       <logmsg dest=\'logndisplay\'>\n" + 
            "           High threshold exceeded for %service% datasource %parm[ds]% on interface\n" + 
            "           %interface%, parms: %parm[all]%\n" + 
            "       </logmsg>\n" + 
            "       <severity>Warning</severity>\n" + 
            "   </event>\n" + 
            "   <event>\n" + 
            "       <uei>uei.opennms.org/threshold/lowThresholdExceeded</uei>\n" + 
            "       <event-label>OpenNMS-defined threshold event: lowThresholdExceeded</event-label>\n" + 
            "       <descr>\n" + 
            "           &lt;p&gt;Low threshold exceeded for %service% datasource\n" + 
            "                        %parm[ds]% on interface %interface%, parms: %parm[all]%.&lt;/p&gt;\n" + 
            "                        &lt;p&gt;By default, OpenNMS watches some key parameters\n" + 
            "                        on devices in your network and will alert you with\n" + 
            "                        an event if certain conditions arise. For example, if\n" + 
            "                        the CPU utilization on your Cisco router maintains an\n" + 
            "                        inordinately high percentage of utilization for an extended\n" + 
            "                        period, an event will be generated. These thresholds are\n" + 
            "                        determined and configured based on working experience with\n" + 
            "                        real deployments, not vendor recommendation alone.&lt;/p&gt;\n" + 
            "                        &lt;p&gt;This specific event indicates that a low threshold\n" + 
            "                        was exceeded.&lt;/p&gt;\n" + 
            "       </descr>\n" + 
            "       <logmsg dest=\'logndisplay\'>\n" + 
            "           Low threshold exceeded for %service% datasource %parm[ds]% on interface\n" + 
            "           %interface%, parms: %parm[all]%\n" + 
            "       </logmsg>\n" + 
            "       <severity>Warning</severity>\n" + 
            "   </event>\n" + 
            "        <event>\n" + 
            "                <uei>uei.opennms.org/threshold/highThresholdRearmed</uei>\n" + 
            "                <event-label>OpenNMS-defined threshold event: highThresholdRearmed</event-label>\n" + 
            "                <descr>\n" + 
            "                        &lt;p&gt;High threshold has been rearmed for %service% datasource\n" + 
            "                        %parm[ds]% on interface %interface%, parms: %parm[all]%&lt;/p&gt;\n" + 
            "                        &lt;p&gt;By default, OpenNMS watches some key parameters\n" + 
            "                        on devices in your network and will alert you with\n" + 
            "                        an event if certain conditions arise. For example, if\n" + 
            "                        the CPU utilization on your Cisco router maintains an\n" + 
            "                        inordinately high percentage of utilization for an extended\n" + 
            "                        period, an event will be generated. These thresholds are\n" + 
            "                        determined and configured based on vendor recommendations,\n" + 
            "                        tempered with real-world experience in working\n" + 
            "                        deployments.&lt;/p&gt; &lt;p&gt;This specific event\n" + 
            "                        indicates that a high threshold was exceeded but then dropped \n" + 
            "           below the rearm threshold..&lt;/p&gt;\n" + 
            "                </descr>\n" + 
            "                <logmsg dest=\'logndisplay\'>\n" + 
            "                        High threshold rearmed for %service% datasource %parm[ds]% on interface\n" + 
            "                        %interface%, parms: %parm[all]%\n" + 
            "                </logmsg>\n" + 
            "                <severity>Cleared</severity>\n" + 
            "        </event>\n" + 
            "        <event>\n" + 
            "                <uei>uei.opennms.org/threshold/lowThresholdRearmed</uei>\n" + 
            "                <event-label>OpenNMS-defined threshold event: lowThresholdRearmed</event-label>\n" + 
            "                <descr>\n" + 
            "                        &lt;p&gt;Low threshold has been rearmed for %service% datasource\n" + 
            "                        %parm[ds]% on interface %interface%, parms: %parm[all]%.&lt;/p&gt;\n" + 
            "                        &lt;p&gt;By default, OpenNMS watches some key parameters\n" + 
            "                        on devices in your network and will alert you with\n" + 
            "                        an event if certain conditions arise. For example, if\n" + 
            "                        the CPU utilization on your Cisco router maintains an\n" + 
            "                        inordinately high percentage of utilization for an extended\n" + 
            "                        period, an event will be generated. These thresholds are\n" + 
            "                        determined and configured based on working experience with\n" + 
            "                        real deployments, not vendor recommendation alone.&lt;/p&gt;\n" + 
            "                        &lt;p&gt;This specific event indicates that a low threshold\n" + 
            "                        was exceeded but then dropped below the rearm threshold.&lt;/p&gt;\n" + 
            "                </descr>\n" + 
            "                <logmsg dest=\'logndisplay\'>\n" + 
            "                        Low threshold rearmed for %service% datasource %parm[ds]% on interface\n" + 
            "                        %interface%, parms: %parm[all]%\n" + 
            "                </logmsg>\n" + 
            "                <severity>Cleared</severity>\n" + 
            "        </event>\n" + 
            "   <event>\n" + 
            "       <uei>uei.opennms.org/nodes/thresholdingFailed</uei>\n" + 
            "       <event-label>OpenNMS-defined threshold event: thresholdingFailed</event-label>\n" + 
            "       <descr>\n" + 
            "           &lt;p&gt;%service% thresholding on interface %interface%\n" + 
            "           failed.&lt;/p&gt;\n" + 
            "       </descr>\n" + 
            "       <logmsg dest=\'logndisplay\'>\n" + 
            "           %service% thresholding on interface %interface% failed.\n" + 
            "       </logmsg>\n" + 
            "       <severity>Warning</severity>\n" + 
            "   </event>\n" + 
            "   <event>\n" + 
            "       <uei>uei.opennms.org/nodes/thresholdingSucceeded</uei>\n" + 
            "       <event-label>OpenNMS-defined threshold event: thresholdingSucceeded</event-label>\n" + 
            "       <descr>\n" + 
            "           &lt;p&gt;%service% thresholding on interface %interface%\n" + 
            "           previously failed and has been restored.&lt;/p&gt;\n" + 
            "       </descr>\n" + 
            "       <logmsg dest=\'logndisplay\'>\n" + 
            "           %service% thresholding on interface %interface% prevously\n" + 
            "           failed and has been restored.\n" + 
            "       </logmsg>\n" + 
            "       <severity>Cleared</severity>\n" + 
            "   </event>\n" + 
            "   <event>\n" + 
            "       <uei>uei.opennms.org/vulnscand/specificVulnerabilityScan</uei>\n" + 
            "       <event-label>OpenNMS-defined vulnscand event: specificVulnerabilityScan</event-label>\n" + 
            "       <descr>\n" + 
            "           &lt;p&gt;A vulnerabilities scan has been forced.&lt;/p&gt;\n" + 
            "       </descr>\n" + 
            "       <logmsg dest=\'logndisplay\'>\n" + 
            "           &lt;p&gt;A vulnerabilities scan has been forced on this\n" + 
            "           interface.&lt;/p&gt;\n" + 
            "       </logmsg>\n" + 
            "       <severity>Major</severity>\n" + 
            "   </event>\n" + 
            "   <event>\n" + 
            "       <uei>uei.opennms.org/nodes/assetInfoChanged</uei>\n" + 
            "       <event-label>OpenNMS-defined node event: assetInfoChanged</event-label>\n" + 
            "       <descr>\n" + 
            "           &lt;p&gt;The Asset info for node %nodeid% (%nodelabel%)\n" + 
            "           has been changed via the webUI.&lt;/p&gt;\n" + 
            "       </descr>\n" + 
            "       <logmsg dest=\'logndisplay\'>\n" + 
            "           &lt;p&gt;The Asset info for node %nodeid% (%nodelabel%)\n" + 
            "           has been changed via the webUI.&lt;/p&gt;\n" + 
            "       </logmsg>\n" + 
            "       <severity>Normal</severity>\n" + 
            "   </event>\n" + 
            "   <event>\n" + 
            "                <mask>  \n" + 
            "                        <maskelement>   \n" + 
            "                                <mename>id</mename>   \n" + 
            "                                <mevalue>.1.3.6.1.4.1.5813.1</mevalue>\n" + 
            "                        </maskelement>  \n" + 
            "                        <maskelement>   \n" + 
            "                                <mename>generic</mename>   \n" + 
            "                                <mevalue>6</mevalue>  \n" + 
            "                        </maskelement>\n" + 
            "                        <maskelement>\n" + 
            "                                <mename>specific</mename>\n" + 
            "                                <mevalue>1</mevalue>\n" + 
            "                        </maskelement>\n" + 
            "                </mask>\n" + 
            "                <uei>uei.opennms.org/traps/forwardedEvent</uei>\n" + 
            "                <event-label>OpenNMS-defined SNMP Trap: forwardedEvent</event-label>\n" + 
            "                <descr>\n" + 
            "                        &lt;p&gt;An OpenNMS Event has been received as an SNMP Trap with\n" + 
            "                        the following information:&lt;/p&gt;\n" + 
            "                        &lt;UL&gt;\n" + 
            "                        &lt;LI&gt;Database ID: %parm[#1]%\n" + 
            "                        &lt;LI&gt;Distributed Poller Name: %parm[#2]%\n" + 
            "                        &lt;LI&gt;Event Creation Time: %parm[#3]%\n" + 
            "                        &lt;LI&gt;Master Station: %parm[#4]%\n" + 
            "                        &lt;LI&gt;UEI: %parm[#5]%\n" + 
            "                        &lt;LI&gt;Source: %parm[#6]%\n" + 
            "                        &lt;LI&gt;Node ID: %parm[#7]%\n" + 
            "                        &lt;LI&gt;Time: %parm[#8]%\n" + 
            "                        &lt;LI&gt;Host: %parm[#9]%\n" + 
            "                        &lt;LI&gt;Interface: %parm[#10]%\n" + 
            "                        &lt;LI&gt;SNMP Host: %parm[#11]%\n" + 
            "                        &lt;LI&gt;Service: %parm[#12]%\n" + 
            "                        &lt;LI&gt;Description: %parm[#13]%\n" + 
            "                        &lt;LI&gt;Severity: %parm[#14]%\n" + 
            "                        &lt;LI&gt;Path Outage: %parm[#15]%\n" + 
            "                        &lt;LI&gt;Operator Instructions: %parm[#16]%\n" + 
            "                        &lt;/UL&gt;\n" + 
            "                </descr>                \n" + 
            "                <logmsg dest=\'logndisplay\'>                        \n" + 
            "                        &lt;p&gt;An OpenNMS Event has been received as an SNMP Trap with UEI:\n" + 
            "                        %parm[#5]%.&lt;/p&gt;\n" + 
            "                </logmsg>\n" + 
            "                <severity>Indeterminate</severity>\n" + 
            "        </event>\n" + 
            "    <event>\n" + 
            "        <mask>\n" + 
            "            <maskelement>\n" + 
            "                <mename>generic</mename>\n" + 
            "                <mevalue>0</mevalue>\n" + 
            "            </maskelement>\n" + 
            "        </mask>\n" + 
            "        <uei>uei.opennms.org/generic/traps/SNMP_Cold_Start</uei>\n" + 
            "        <event-label>OpenNMS-defined trap event: SNMP_Cold_Start</event-label>\n" + 
            "        <descr>\n" + 
            "            &lt;p&gt;A coldStart trap signifies that the sending protocol entity is reinitializing itself such that the agent\'s\n" + 
            "            configuration or the protocol entity implementation may be altered.&lt;/p&gt;\n" + 
            "        </descr>\n" + 
            "        <logmsg dest=\'logndisplay\'>\n" + 
            "            Agent Up with Possible Changes (coldStart Trap) enterprise:%id% (%id%) args(%parm[##]%):%parm[all]%\n" + 
            "        </logmsg>\n" + 
            "        <severity>Normal</severity>\n" + 
            "    </event>\n" + 
            "    <event>\n" + 
            "        <mask>\n" + 
            "            <maskelement>\n" + 
            "                <mename>generic</mename>\n" + 
            "                <mevalue>1</mevalue>\n" + 
            "            </maskelement>\n" + 
            "        </mask>\n" + 
            "        <uei>uei.opennms.org/generic/traps/SNMP_Warm_Start</uei>\n" + 
            "        <event-label>OpenNMS-defined trap event: SNMP_Warm_Start</event-label>\n" + 
            "        <descr>\n" + 
            "            &lt;p&gt;A warmStart trap signifies that the sending protocol entity is reinitializing itself such that neither the agent\n" + 
            "            configuration nor the protocol entity implementation is altered.&lt;/p&gt;\n" + 
            "        </descr>\n" + 
            "        <logmsg dest=\'logndisplay\'>Agent Up with No Changes (warmStart Trap) enterprise:%id% (%id%) args(%parm[##]%):%parm[all]%</logmsg>\n" + 
            "        <severity>Normal</severity>\n" + 
            "    </event>\n" + 
            "    <event>\n" + 
            "        <mask>\n" + 
            "            <maskelement>\n" + 
            "                <mename>generic</mename>\n" + 
            "                <mevalue>2</mevalue>\n" + 
            "            </maskelement>\n" + 
            "        </mask>\n" + 
            "        <uei>uei.opennms.org/generic/traps/SNMP_Link_Down</uei>\n" + 
            "        <event-label>OpenNMS-defined trap event: SNMP_Link_Down</event-label>\n" + 
            "        <descr>\n" + 
            "            &lt;p&gt;A linkDown trap signifies that the sending protocol entity recognizes a failure in one of the communication link\n" + 
            "            represented in the agent\'s configuration. The data passed with the event are 1) The name and value of the ifIndex instance for\n" + 
            "            the affected interface. The name of the interface can be retrieved via an snmpget of .1.3.6.1.2.1.2.2.1.2.INST, where INST is\n" + 
            "            the instance returned with the trap.&lt;/p&gt;\n" + 
            "        </descr>\n" + 
            "        <logmsg dest=\'logndisplay\'>Agent Interface Down (linkDown Trap) enterprise:%id% (%id%) on interface %parm[#1]%</logmsg>\n" + 
            "        <severity>Minor</severity>\n" + 
            "    </event>\n" + 
            "    <event>\n" + 
            "        <mask>\n" + 
            "            <maskelement>\n" + 
            "                <mename>generic</mename>\n" + 
            "                <mevalue>3</mevalue>\n" + 
            "            </maskelement>\n" + 
            "        </mask>\n" + 
            "        <uei>uei.opennms.org/generic/traps/SNMP_Link_Up</uei>\n" + 
            "        <event-label>OpenNMS-defined trap event: SNMP_Link_Up</event-label>\n" + 
            "        <descr>\n" + 
            "            &lt;p&gt;A linkUp trap signifies that the sending protocol entity recognizes that one of the communication links represented in\n" + 
            "            the agent\'s configuration has come up. The data passed with the event are 1) The name and value of the ifIndex instance for the\n" + 
            "            affected interface. The name of the interface can be retrieved via an snmpget of .1.3.6.1.2.1.2.2.1.2.INST, where INST is the\n" + 
            "            instance returned with the trap.&lt;/p&gt;\n" + 
            "        </descr>\n" + 
            "        <logmsg dest=\'logndisplay\'>Agent Interface Up (linkUp Trap) enterprise:%id% (%id%) on interface %parm[#1]%</logmsg>\n" + 
            "        <severity>Normal</severity>\n" + 
            "    </event>\n" + 
            "    <event>\n" + 
            "        <mask>\n" + 
            "            <maskelement>\n" + 
            "                <mename>generic</mename>\n" + 
            "                <mevalue>4</mevalue>\n" + 
            "            </maskelement>\n" + 
            "        </mask>\n" + 
            "        <uei>uei.opennms.org/generic/traps/SNMP_Authen_Failure</uei>\n" + 
            "        <event-label>OpenNMS-defined trap event: SNMP_Authen_Failure</event-label>\n" + 
            "        <descr>\n" + 
            "            &lt;p&gt;An authentication failure trap signifies that the sending protocol entity is the addressee of a protocol message that\n" + 
            "            is not properly authenticated.&lt;/p&gt;\n" + 
            "        </descr>\n" + 
            "        <logmsg dest=\'logndisplay\'>\n" + 
            "            Incorrect Community Name (authenticationFailure Trap) enterprise:%id% (%id%) args(%parm[##]%):%parm[all]%\n" + 
            "        </logmsg>\n" + 
            "        <severity>Warning</severity>\n" + 
            "    </event>\n" + 
            "    <event>\n" + 
            "        <mask>\n" + 
            "            <maskelement>\n" + 
            "                <mename>generic</mename>\n" + 
            "                <mevalue>5</mevalue>\n" + 
            "            </maskelement>\n" + 
            "        </mask>\n" + 
            "        <uei>uei.opennms.org/generic/traps/SNMP_EGP_Down</uei>\n" + 
            "        <event-label>OpenNMS-defined trap event: SNMP_EGP_Down</event-label>\n" + 
            "        <descr>\n" + 
            "            &lt;p&gt;An egpNeighborLoss trap signifies that an EGP neighbor for whom the sending protocol entity was an EGP peer has been\n" + 
            "            marked down and the peer relationship no longer obtains. The data passed with the event are The name and value of the ifIndex\n" + 
            "            egpNeighAddr for the affected neighbor.&lt;/p&gt;\n" + 
            "        </descr>\n" + 
            "        <logmsg dest=\'logndisplay\'>EGP Neighbor Down (egpNeighborLoss Trap) enterprise:%id% (%id%) neighbor %parm[#1]%</logmsg>\n" + 
            "        <severity>Warning</severity>\n" + 
            "    </event>\n" + 
            "    <event>\n" + 
            "        <mask>\n" + 
            "            <maskelement>\n" + 
            "                <mename>generic</mename>\n" + 
            "                <mevalue>6</mevalue>\n" + 
            "            </maskelement>\n" + 
            "        </mask>\n" + 
            "        <uei>uei.opennms.org/generic/traps/EnterpriseDefault</uei>\n" + 
            "        <event-label>OpenNMS-defined trap event: EnterpriseDefault</event-label>\n" + 
            "        <descr>\n" + 
            "            &lt;p&gt;This is the default event format used when an enterprise specific event (trap) is received for which no format has been\n" + 
            "            configured (i.e. no event definition exists).&lt;/p&gt;\n" + 
            "        </descr>\n" + 
            "        <logmsg dest=\'logndisplay\'>\n" + 
            "            Received unformatted enterprise event (enterprise:%id% generic:%generic% specific:%specific%). %parm[##]% args: %parm[all]%\n" + 
            "        </logmsg>\n" + 
            "        <severity>Normal</severity>\n" + 
            "        <alarm-data reduction-key=\"%source%:%snmphost%:%id%:%generic%:%specific%\" alarm-type=\"2\" />\n"+
            "    </event>\n" + 
            "    <event>\n" + 
            "        <uei>uei.opennms.org/default/trap</uei>\n" + 
            "        <event-label>OpenNMS-defined default event: trap</event-label>\n" + 
            "        <descr>\n" + 
            "            &lt;p&gt;An SNMP Trap (%snmp%) with no matching configuration was received from interface %interface%.&lt;/p&gt; &lt;p&gt;The\n" + 
            "            trap included the following variable bindings:&lt;/p&gt; &lt;p&gt;%parm[all]%&lt;/p&gt;\n" + 
            "        </descr>\n" + 
            "        <logmsg dest=\'logndisplay\'>An SNMP Trap with no matching configuration was received from interface %interface%.</logmsg>\n" + 
            "        <severity>Indeterminate</severity>\n" + 
            "    </event>\n" + 
            "    <event>\n" + 
            "        <uei>uei.opennms.org/default/event</uei>\n" + 
            "        <event-label>OpenNMS-defined default event: event</event-label>\n" + 
            "        <descr>\n" + 
            "            &lt;p&gt;An event with no matching configuration was received from interface %interface%. This event included the following\n" + 
            "            parameters: %parm[all]%&lt;/p&gt;\n" + 
            "        </descr>\n" + 
            "        <logmsg dest=\'logndisplay\'>An event with no matching configuration was received from interface %interface%.</logmsg>\n" + 
            "        <severity>Indeterminate</severity>\n" + 
            "    </event>\n" + 
            "</events>\n" + 
            "";
    
    protected static MockDatabase m_db;
    protected static MockNetwork m_network;
    protected static Eventd m_eventd;
    protected static EventIpcManager m_eventdIpcMgr;

    protected static EventdConfigManager m_eventdConfigMgr;
    
    protected static boolean m_runSupers = true;

    /**
     * String representing snmp-config.xml
     */
    public String getSnmpConfig() {
        return "<?xml version=\"1.0\"?>\n" + 
                "<snmp-config "+ 
                " retry=\"3\" timeout=\"3000\"\n" + 
                " read-community=\"public\"" +
                " write-community=\"private\"\n" + 
                " port=\"161\"\n" +
                " version=\"v1\">\n" +
                "\n" +
                "   <definition port=\"9161\" version=\""+myVersion()+"\" " +
                "       security-name=\"opennmsUser\" \n" + 
                "       auth-passphrase=\"0p3nNMSv3\" \n" +
                "       privacy-passphrase=\"0p3nNMSv3\" >\n" +
                "       <specific>"+myLocalHost()+"</specific>\n" +
                "   </definition>\n" + 
                "\n" + 
                "   <definition version=\"v1\" read-community=\"specificv1\">\n" + 
                "       <specific>10.0.0.1</specific>\n" +
                "   </definition>\n" + 
                "\n" + 
                "   <definition version=\"v1\" read-community=\"specificv1\" max-request-size=\"434\">\n" + 
                "       <specific>10.0.0.2</specific>\n" +
                "   </definition>\n" + 
                "\n" + 
                "   <definition version=\"v1\" read-community=\"specificv1\" proxy-host=\""+myLocalHost()+"\">\n" + 
                "       <specific>10.0.0.3</specific>\n" +
                "   </definition>\n" + 
                "\n" + 
                "   <definition version=\"v3\" " +
                "       security-name=\"opennmsUser\" \n" + 
                "       auth-passphrase=\"0p3nNMSv3\" >\n" +
                "       <specific>20.20.20.20</specific>\n" +
                "   </definition>\n" + 
                "   <definition version=\"v3\" " +
                "       security-name=\"opennmsRangeUser\" \n" + 
                "       auth-passphrase=\"0p3nNMSv3\" >\n" +
                "       <range begin=\"1.1.1.1\" end=\"1.1.1.100\"/>\n" +
                "   </definition>\n" + 
                "\n" + 
                "   <definition version=\"v1\" read-community=\"rangev1\" max-vars-per-pdu=\"55\"> \n" + 
                "       <range begin=\"10.0.0.101\" end=\"10.0.0.200\"/>\n" +
                "   </definition>\n" + 
                "\n" + 
                "   <definition version=\"v2c\" read-community=\"rangev2c\">\n" + 
                "       <range begin=\"10.0.1.100\" end=\"10.0.5.100\"/>\n" +
                "       <range begin=\"10.7.20.100\" end=\"10.7.25.100\"/>\n" +
                "   </definition>\n" + 
                "\n" + 
                "   <definition version=\"v2c\" read-community=\"specificv2c\">\n" + 
                "       <specific>192.168.0.50</specific>\n" +
                "   </definition>\n" + 
                "\n" + 
                "   <definition version=\"v2c\" read-community=\"ipmatch\" max-vars-per-pdu=\"128\">\n" + 
                "       <ip-match>77.5-12,15.1-255.255</ip-match>\n" +
                "   </definition>\n" + 
                "\n" + 
                "</snmp-config>";
    }

    private boolean m_startEventd = true;

    /**
     * Helper method for getting the ip address of the localhost as a
     * String to be used in the snmp-config.
     * @return
     */
    protected String myLocalHost() {
        
//        try {
//            return InetAddress.getLocalHost().getHostAddress();
//        } catch (UnknownHostException e) {
//            e.printStackTrace();
//            fail("Exception getting localhost");
//        }
//        
//        return null;
        
        return "127.0.0.1";
    }
    
    private String myVersion() {
        switch (m_version) {
        case SnmpAgentConfig.VERSION1 :
            return "v1";
        case SnmpAgentConfig.VERSION2C :
            return "v2c";
        case SnmpAgentConfig.VERSION3 :
            return "v3";
        default :
            return "v1";
        }
    }

    int m_version = SnmpAgentConfig.VERSION1;

    private EventProxy m_eventProxy;

    protected PlatformTransactionManager m_transMgr;
    
    public void setVersion(int version) {
        m_version = version;
    }

    protected void setUp() throws Exception {
        super.setUp();
        MockUtil.println("------------ Begin Test "+getName()+" --------------------------");
        MockLogAppender.setupLogging();
        
        if (m_runSupers) {
        
            createMockNetwork();
            
            populateDatabase();
            
            DataSourceFactory.setInstance(m_db);

            Reader rdr = new StringReader(getSnmpConfig());
            SnmpPeerFactory.setInstance(new SnmpPeerFactory(rdr));
            
            if (isStartEventd()) {
                m_eventd = new Eventd();
                m_eventd.setDataSource(m_db);
                m_eventdConfigMgr = new MockEventConfigManager(MOCK_EVENT_CONFIG);
                m_eventd.setConfigManager(m_eventdConfigMgr);
                
                
                Reader configRdr = new StringReader(MOCK_EVENT_CONF);
                EventConfigurationManager.loadConfiguration(configRdr);
                
                
                m_eventdIpcMgr = new EventIpcManagerDefaultImpl(m_eventdConfigMgr);
                m_eventProxy = new EventProxy() {

                    public void send(Event event) throws EventProxyException {
                        m_eventdIpcMgr.sendNow(event);
                    }

                    public void send(Log eventLog) throws EventProxyException {
                        m_eventdIpcMgr.sendNow(eventLog);
                    }
                    
                };
                
                EventIpcManagerFactory.setIpcManager(m_eventdIpcMgr);
                m_eventd.setEventIpcManager(m_eventdIpcMgr);
                m_eventd.init();
                m_eventd.start();
            }
        
        }
        
        m_transMgr = new DataSourceTransactionManager(DataSourceFactory.getInstance());

    }

    protected void populateDatabase() throws Exception {
        m_db = new MockDatabase();
        m_db.populate(m_network);
    }

    protected void createMockNetwork() {
        m_network = new MockNetwork();
        m_network.setCriticalService("ICMP");
        m_network.addNode(1, "Router");
        m_network.addInterface("192.168.1.1");
        m_network.addService("ICMP");
        m_network.addService("SMTP");
        m_network.addInterface("192.168.1.2");
        m_network.addService("ICMP");
        m_network.addService("SMTP");
        m_network.addNode(2, "Server");
        m_network.addInterface("192.168.1.3");
        m_network.addService("ICMP");
        m_network.addService("HTTP");
        m_network.addNode(3, "Firewall");
        m_network.addInterface("192.168.1.4");
        m_network.addService("SMTP");
        m_network.addService("HTTP");
        m_network.addInterface("192.168.1.5");
        m_network.addService("SMTP");
        m_network.addService("HTTP");
    }
    
    @Override
    public void runTest() throws Throwable {
        try {
            super.runTest();
            MockLogAppender.assertNoWarningsOrGreater();
        } finally {
            MockUtil.println("------------ End Test "+getName()+" --------------------------");
        }
    }

    protected void tearDown() throws Exception {
        if(m_runSupers) {
            if (isStartEventd()) m_eventd.stop();
        }

        super.tearDown();
    }

    protected void setStartEventd(boolean startEventd) {
        m_startEventd = startEventd;
    }

    protected boolean isStartEventd() {
        return m_startEventd;
    }

    public void testDoNothing() {}

    protected void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
        }
    }

    protected EventProxy getEventProxy() {
        return m_eventProxy;
    }

    protected void setEventProxy(EventProxy eventProxy) {
        m_eventProxy = eventProxy;
    }

    public SimpleJdbcTemplate getJdbcTemplate() {
        return m_db.getJdbcTemplate();
    }

}
