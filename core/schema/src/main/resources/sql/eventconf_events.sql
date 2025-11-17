--
-- Licensed to The OpenNMS Group, Inc (TOG) under one or more
-- contributor license agreements.  See the LICENSE.md file
-- distributed with this work for additional information
-- regarding copyright ownership.
--
-- TOG licenses this file to You under the GNU Affero General
-- Public License Version 3 (the "License") or (at your option)
-- any later version.  You may not use this file except in
-- compliance with the License.  You may obtain a copy of the
-- License at:
--
--      https://www.gnu.org/licenses/agpl-3.0.txt
--
-- Unless required by applicable law or agreed to in writing,
-- software distributed under the License is distributed on an
-- "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
-- either express or implied.  See the License for the specific
-- language governing permissions and limitations under the
-- License.
--

INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content, severity, created_time, last_modified, modified_by) VALUES (49, 10, 'uei.opennms.org/threshold/absoluteChangeExceeded', 'OpenNMS-defined threshold event: absoluteChangeExceeded', 'Absolute change threshold for the following metric exceeded: %parm[all]%', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/threshold/absoluteChangeExceeded</uei>
   <event-label>OpenNMS-defined threshold event: absoluteChangeExceeded</event-label>
   <descr>Absolute change threshold for the following metric exceeded: %parm[all]%</descr>
   <logmsg dest="logndisplay">
            Absolute change exceeded for service %service% metric %parm[expressionLabel]% [%parm[ds]%] on interface %parm[label]%/%interface%
        </logmsg>
   <severity>Warning</severity>
</event>', 'Warning', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content, severity, created_time, last_modified, modified_by) VALUES (50, 11, 'uei.opennms.org/internal/discoveryConfigChange', 'OpenNMS-defined internal event: discovery configuration changed', 'This event is sent by the WebUI or the user when discovery configuration has changed and should be reloaded', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/internal/discoveryConfigChange</uei>
   <event-label>OpenNMS-defined internal event: discovery configuration changed</event-label>
   <descr>This event is sent by the WebUI or the user when discovery configuration has changed and should be reloaded</descr>
   <logmsg dest="logndisplay">
            The discovery configuration has been changed and should be reloaded
        </logmsg>
   <severity>Normal</severity>
</event>', 'Normal', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content, severity, created_time, last_modified, modified_by) VALUES (51, 11, 'uei.opennms.org/internal/discovery/hardwareInventoryFailed', 'OpenNMS-defined internal event: reload specified daemon configuration failed', '<p>The hardware discovery (%parm[method]%) on node %nodelabel% (IP address %interface%) has failed with the following reason: %parm[reason]%.</p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/internal/discovery/hardwareInventoryFailed</uei>
   <event-label>OpenNMS-defined internal event: reload specified daemon configuration failed</event-label>
   <descr>&lt;p>The hardware discovery (%parm[method]%) on node %nodelabel% (IP address %interface%) has failed with the following reason: %parm[reason]%.&lt;/p></descr>
   <logmsg dest="logndisplay">
            &lt;p>The hardware discovery (%parm[method]%) on node %nodelabel% (IP address %interface%) has failed.&lt;/p>
        </logmsg>
   <severity>Minor</severity>
</event>', 'Minor', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content, severity, created_time, last_modified, modified_by) VALUES (52, 11, 'uei.opennms.org/internal/discovery/hardwareInventorySuccessful', 'OpenNMS-defined internal event: hardware discovery successful', '<p>The hardware discovery (%parm[method]%) on node %nodelabel% (IP address %interface%) has been completed successfuly.</p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/internal/discovery/hardwareInventorySuccessful</uei>
   <event-label>OpenNMS-defined internal event: hardware discovery successful</event-label>
   <descr>&lt;p>The hardware discovery (%parm[method]%) on node %nodelabel% (IP address %interface%) has been completed successfuly.&lt;/p></descr>
   <logmsg dest="logndisplay">
            &lt;p>The hardware discovery (%parm[method]%) on node %nodelabel% (IP address %interface%) has been completed successfuly.&lt;/p>
        </logmsg>
   <severity>Normal</severity>
</event>', 'Normal', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content, severity, created_time, last_modified, modified_by) VALUES (53, 11, 'uei.opennms.org/internal/discovery/newSuspect', 'OpenNMS-defined internal event: discovery newSuspect', '<p>Interface %interface% has been discovered in location %parm[location]% and is
            being queued for a services scan.</p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/internal/discovery/newSuspect</uei>
   <event-label>OpenNMS-defined internal event: discovery newSuspect</event-label>
   <descr>&lt;p>Interface %interface% has been discovered in location %parm[location]% and is
            being queued for a services scan.&lt;/p></descr>
   <logmsg dest="logndisplay">
            A new interface (%interface%) has been discovered in location %parm[location]% and is
            being queued for a services scan.
        </logmsg>
   <severity>Warning</severity>
</event>', 'Warning', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content, severity, created_time, last_modified, modified_by) VALUES (54, 12, 'uei.opennms.org/internal/interfaceManaged', 'OpenNMS-defined internal event: interfaceManaged', '<p>The interface %interface% is being
            remanaged.</p> <p>This interface will now
            participate in service polling.</p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/internal/interfaceManaged</uei>
   <event-label>OpenNMS-defined internal event: interfaceManaged</event-label>
   <descr>&lt;p>The interface %interface% is being
            remanaged.&lt;/p> &lt;p>This interface will now
            participate in service polling.&lt;/p></descr>
   <logmsg dest="logndisplay">
            The interface %interface% is being remanaged.
        </logmsg>
   <severity>Warning</severity>
</event>', 'Warning', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content, created_time, last_modified, modified_by) VALUES (55, 12, 'uei.opennms.org/internal/interfaceUnmanaged', 'OpenNMS-defined internal event: interfaceUnmanaged', '<p>The interface %interface% is being forcibly
            unmanaged.</p> <p>This interface and all
            associated services will <b>NOT</b> be polled
            until the interface is remanaged.</p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/internal/interfaceUnmanaged</uei>
   <event-label>OpenNMS-defined internal event: interfaceUnmanaged</event-label>
   <descr>&lt;p>The interface %interface% is being forcibly
            unmanaged.&lt;/p> &lt;p>This interface and all
            associated services will &lt;b>NOT&lt;/b> be polled
            until the interface is remanaged.&lt;/p></descr>
   <logmsg dest="logndisplay">
            The interface %interface% is being forcibly unmanaged.
        </logmsg>
   <severity>Minor</severity>
</event>', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (56, 12, 'uei.opennms.org/internal/notificationWithoutUsers', 'OpenNMS-defined internal event: notificationWithoutUsers', '<p>A destination path in a notification has not been
            assigned any users.</p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/internal/notificationWithoutUsers</uei>
   <event-label>OpenNMS-defined internal event: notificationWithoutUsers</event-label>
   <descr>&lt;p>A destination path in a notification has not been
            assigned any users.&lt;/p></descr>
   <logmsg dest="logndisplay">
            A destination path in a notification has not been assigned
            any users.
        </logmsg>
   <severity>Warning</severity>
</event>','Warning', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (66, 12, 'uei.opennms.org/internal/authentication/failure', 'OpenNMS-defined internal event: an authentication failure has occurred in WebUI', 'This event is sent by the WebUI when an authentication failure occurs.', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/internal/authentication/failure</uei>
   <event-label>OpenNMS-defined internal event: an authentication failure has occurred in WebUI</event-label>
   <descr>This event is sent by the WebUI when an authentication failure occurs.</descr>
   <logmsg dest="logndisplay">
            OpenNMS user ''%parm[user]%'' (may be blank) has failed to login
            from %parm[ip]%. The failure event is %parm[exceptionName]% with
            the message ''%parm[exceptionMessage]%''.
        </logmsg>
   <severity>Minor</severity>
</event>','Minor', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (67, 12, 'uei.opennms.org/internal/authentication/loggedOut', 'OpenNMS-defined internal event: a user logged out of the web UI', 'This event is sent by the WebUI when a user logs out of the WebUI.', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/internal/authentication/loggedOut</uei>
   <event-label>OpenNMS-defined internal event: a user logged out of the web UI</event-label>
   <descr>This event is sent by the WebUI when a user logs out of the WebUI.</descr>
   <logmsg dest="logndisplay">
            OpenNMS user ''%parm[user]%'' logged out of the WebUI.
        </logmsg>
   <severity>Normal</severity>
</event>','Normal', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (68, 12, 'uei.opennms.org/internal/authentication/sessionRemoved', 'OpenNMS-defined internal event: a user''s session was removed from the WebUI', 'This event is sent by the WebUI when a user''s session is removed for any
            reason other than a user-initiated logout. This generally means that
            the session timed out due to inactivity.', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/internal/authentication/sessionRemoved</uei>
   <event-label>OpenNMS-defined internal event: a user''s session was removed from the WebUI</event-label>
   <descr>This event is sent by the WebUI when a user''s session is removed for any
            reason other than a user-initiated logout. This generally means that
            the session timed out due to inactivity.</descr>
   <logmsg dest="logndisplay">
            OpenNMS user ''%parm[user]%'' has been logged out of the WebUI, most likely
            due to a session timeout.
        </logmsg>
   <severity>Normal</severity>
</event>','Normal', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (69, 12, 'uei.opennms.org/internal/kscReportUpdated', 'OpenNMS-defined internal event: KSC report updated', '<p>The KSC Report ''%parm[reportTitle]%'' has been updated (remaining graphs: %parm[graphCount]%).</p>
            <p>Some graphs defined on the report have been removed, due to an invalid resource or chart.</p>
            <p>A resource is not valid on any of the following situations: the nodeId (or nodeSource) doesn''t
            exist, the resource type
            is not valid or doesn''t exist on the node, the resource name is not valid or doesn''t exist on the node.</p>
            <p>Check the logs for more details.</p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/internal/kscReportUpdated</uei>
   <event-label>OpenNMS-defined internal event: KSC report updated</event-label>
   <descr>&lt;p>The KSC Report ''%parm[reportTitle]%'' has been updated (remaining graphs: %parm[graphCount]%).&lt;/p>
            &lt;p>Some graphs defined on the report have been removed, due to an invalid resource or chart.&lt;/p>
            &lt;p>A resource is not valid on any of the following situations: the nodeId (or nodeSource) doesn''t
            exist, the resource type
            is not valid or doesn''t exist on the node, the resource name is not valid or doesn''t exist on the node.&lt;/p>
            &lt;p>Check the logs for more details.&lt;/p></descr>
   <logmsg dest="logndisplay">The KSC Report %parm[reportTitle]% has been updated.</logmsg>
   <severity>Warning</severity>
</event>','Warning', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (70, 12, 'uei.opennms.org/services/passiveServiceStatus', 'OpenNMS-defined service event: passiveServiceStatus', '<p>Status information for service %parm[passiveServiceName]% has been updated. <br/>
         Passive Service Name: %parm[passiveServiceName]%<br/>
         IP Interface: %parm[passiveIpAddr]%<br/>
         Service Status: %parm[passiveStatus]%<br/>
         Reason: %parm[passiveReasonCode]%</p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/services/passiveServiceStatus</uei>
   <event-label>OpenNMS-defined service event: passiveServiceStatus</event-label>
   <descr>&lt;p>Status information for service %parm[passiveServiceName]% has been updated. &lt;br/>
         Passive Service Name: %parm[passiveServiceName]%&lt;br/>
         IP Interface: %parm[passiveIpAddr]%&lt;br/>
         Service Status: %parm[passiveStatus]%&lt;br/>
         Reason: %parm[passiveReasonCode]%&lt;/p></descr>
   <logmsg dest="logndisplay">&lt;p>Status information for service %parm[passiveServiceName]% has been updated.&lt;/p></logmsg>
   <severity>Normal</severity>
</event>','Normal', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (71, 12, 'uei.opennms.org/asset/maintenance/expirationWarning', 'Maintenance contract will expire in less then %parm[#4]% days', '<p>Maintenance contract of %nodelabel% will expire in less then %parm[#4]% days.</p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/asset/maintenance/expirationWarning</uei>
   <event-label>Maintenance contract will expire in less then %parm[#4]% days</event-label>
   <descr>&lt;p>Maintenance contract of %nodelabel% will expire in less then %parm[#4]% days.&lt;/p></descr>
   <logmsg dest="logndisplay">&lt;p>Maintenance contract %parm[#3]% of %nodelabel% will expire at %parm[#2]%.&lt;/p></logmsg>
   <severity>Warning</severity>
   <alarm-data reduction-key="%uei%:%nodeid%" alarm-type="1"/>
</event>','Warning', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (72, 12, 'uei.opennms.org/internal/monitoringSystemAdded', 'Monitoring system Added', 'A new monitoring system has been added', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/internal/monitoringSystemAdded</uei>
   <event-label>Monitoring system Added</event-label>
   <descr>A new monitoring system has been added</descr>
   <logmsg dest="logndisplay">A new monitoring system of type ''%parm[monitoringSystemType]%'' has been added with ID
            ''%parm[monitoringSystemId]%'' at location ''%parm[monitoringSystemLocation]%''.
        </logmsg>
   <severity>Normal</severity>
</event>','Normal', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (73, 12, 'uei.opennms.org/internal/monitoringSystemLocationChanged', 'Monitoring system Location Changed', 'Monitoring system location changed', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/internal/monitoringSystemLocationChanged</uei>
   <event-label>Monitoring system Location Changed</event-label>
   <descr>Monitoring system location changed</descr>
   <logmsg dest="logndisplay">Monitoring system of type ''%parm[monitoringSystemType]%'' with ID
            ''%parm[monitoringSystemId]%'' has changed its location from ''%parm[monitoringSystemPreviousLocation]%'' to
            ''%parm[monitoringSystemLocation]%''.
        </logmsg>
   <severity>Normal</severity>
</event>','Normal', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (74, 12, 'uei.opennms.org/internal/monitoringSystemDeleted', 'Monitoring system Deleted', 'Monitoring system Deleted', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/internal/monitoringSystemDeleted</uei>
   <event-label>Monitoring system Deleted</event-label>
   <descr>Monitoring system Deleted</descr>
   <logmsg dest="logndisplay">Monitoring system of type ''%parm[monitoringSystemType]%'' with ID
            ''%parm[monitoringSystemId]%'' at location ''%parm[monitoringSystemLocation]%'' has been deleted.
        </logmsg>
   <severity>Normal</severity>
</event>','Normal', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (1, 1, 'uei.opennms.org/generic/traps/SNMP_Cold_Start', 'OpenNMS-defined trap event: SNMP_Cold_Start', '<p>A coldStart trap signifies that the sending protocol entity is reinitializing itself such that the
            agent''s configuration or the protocol entity implementation may be altered.</p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <mask>
      <maskelement>
         <mename>generic</mename>
         <mevalue>0</mevalue>
      </maskelement>
   </mask>
   <uei>uei.opennms.org/generic/traps/SNMP_Cold_Start</uei>
   <event-label>OpenNMS-defined trap event: SNMP_Cold_Start</event-label>
   <descr>&lt;p>A coldStart trap signifies that the sending protocol entity is reinitializing itself such that the
            agent''s configuration or the protocol entity implementation may be altered.&lt;/p></descr>
   <logmsg dest="logndisplay">Agent Up with Possible Changes (coldStart Trap)
        </logmsg>
   <severity>Normal</severity>
</event>','Normal', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (2, 1, 'uei.opennms.org/generic/traps/SNMP_Warm_Start', 'OpenNMS-defined trap event: SNMP_Warm_Start', '<p>A warmStart trap signifies that the sending protocol entity is reinitializing itself such that
            neither the agent configuration nor the
            protocol entity implementation is altered.</p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <mask>
      <maskelement>
         <mename>generic</mename>
         <mevalue>1</mevalue>
      </maskelement>
   </mask>
   <uei>uei.opennms.org/generic/traps/SNMP_Warm_Start</uei>
   <event-label>OpenNMS-defined trap event: SNMP_Warm_Start</event-label>
   <descr>&lt;p>A warmStart trap signifies that the sending protocol entity is reinitializing itself such that
            neither the agent configuration nor the
            protocol entity implementation is altered.&lt;/p></descr>
   <logmsg dest="logndisplay">Agent Up with No Changes (warmStart Trap)
        </logmsg>
   <severity>Normal</severity>
</event>','Normal', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (3, 1, 'uei.opennms.org/generic/traps/SNMP_Link_Down', 'OpenNMS-defined trap event: SNMP_Link_Down', '<p>A linkDown trap signifies that the sending protocol entity recognizes a failure in one of the
            communication link represented in the agent''s
            configuration. The data passed with the event are 1) The name and value of the ifIndex instance for the
            affected interface. The name of the
            interface can be retrieved via an snmpget of .1.3.6.1.2.1.2.2.1.2.INST, where INST is the instance returned
            with the trap.</p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <mask>
      <maskelement>
         <mename>generic</mename>
         <mevalue>2</mevalue>
      </maskelement>
   </mask>
   <uei>uei.opennms.org/generic/traps/SNMP_Link_Down</uei>
   <event-label>OpenNMS-defined trap event: SNMP_Link_Down</event-label>
   <descr>&lt;p>A linkDown trap signifies that the sending protocol entity recognizes a failure in one of the
            communication link represented in the agent''s
            configuration. The data passed with the event are 1) The name and value of the ifIndex instance for the
            affected interface. The name of the
            interface can be retrieved via an snmpget of .1.3.6.1.2.1.2.2.1.2.INST, where INST is the instance returned
            with the trap.&lt;/p></descr>
   <logmsg dest="donotpersist">Agent Interface Down (linkDown Trap)
        </logmsg>
   <severity>Minor</severity>
</event>','Minor', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (4, 1, 'uei.opennms.org/translator/traps/SNMP_Link_Down', 'Translator Enriched LinkDown Event', '<p>A linkDown trap signifies that the sending protocol entity recognizes a failure in one of the
            communication link represented in the agent''s configuration. </p>
            <p>Instance: %parm[#1]% </p>
            <p>IfDescr: %parm[ifDescr]% </p>
            <p>IfName: %parm[ifName]% </p>
            <p>IfAlias: %parm[ifAlias]% </p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/translator/traps/SNMP_Link_Down</uei>
   <event-label>Translator Enriched LinkDown Event</event-label>
   <descr>&lt;p>A linkDown trap signifies that the sending protocol entity recognizes a failure in one of the
            communication link represented in the agent''s configuration. &lt;/p>
            &lt;p>Instance: %parm[#1]% &lt;/p>
            &lt;p>IfDescr: %parm[ifDescr]% &lt;/p>
            &lt;p>IfName: %parm[ifName]% &lt;/p>
            &lt;p>IfAlias: %parm[ifAlias]% &lt;/p></descr>
   <logmsg dest="logndisplay">Agent Interface Down (linkDown Trap)
        </logmsg>
   <severity>Minor</severity>
   <alarm-data reduction-key="uei.opennms.org/nodes/snmp/interfaceOperDown:%dpname%:%nodeid%:%parm[#1]%" alarm-type="1" auto-clean="false">
      <update-field field-name="severity" update-on-reduction="true"/>
   </alarm-data>
</event>','Minor', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (5, 1, 'uei.opennms.org/generic/traps/SNMP_Link_Up', 'OpenNMS-defined trap event: SNMP_Link_Up', '<p>A linkUp trap signifies that the sending protocol entity recognizes that one of the communication
            links represented in the agent''s
            configuration has come up. The data passed with the event are 1) The name and value of the ifIndex instance
            for the affected interface. The name of
            the interface can be retrieved via an snmpget of .1.3.6.1.2.1.2.2.1.2.INST, where INST is the instance
            returned with the trap.</p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <mask>
      <maskelement>
         <mename>generic</mename>
         <mevalue>3</mevalue>
      </maskelement>
   </mask>
   <uei>uei.opennms.org/generic/traps/SNMP_Link_Up</uei>
   <event-label>OpenNMS-defined trap event: SNMP_Link_Up</event-label>
   <descr>&lt;p>A linkUp trap signifies that the sending protocol entity recognizes that one of the communication
            links represented in the agent''s
            configuration has come up. The data passed with the event are 1) The name and value of the ifIndex instance
            for the affected interface. The name of
            the interface can be retrieved via an snmpget of .1.3.6.1.2.1.2.2.1.2.INST, where INST is the instance
            returned with the trap.&lt;/p></descr>
   <logmsg dest="donotpersist">Agent Interface Up (linkUp Trap)
        </logmsg>
   <severity>Normal</severity>
</event>','Normal', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (6, 1, 'uei.opennms.org/translator/traps/SNMP_Link_Up', 'Translator Enriched LinkUp Event', '<p>A linkUp trap signifies that the sending protocol entity recognizes that one of the communication
            links represented in the agent''s configuration has come up. </p>
            <p>Instance: %parm[#1]% </p>
            <p>IfDescr: %parm[ifDescr]% </p>
            <p>IfName: %parm[ifName]% </p>
            <p>IfAlias: %parm[ifAlias]% </p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/translator/traps/SNMP_Link_Up</uei>
   <event-label>Translator Enriched LinkUp Event</event-label>
   <descr>&lt;p>A linkUp trap signifies that the sending protocol entity recognizes that one of the communication
            links represented in the agent''s configuration has come up. &lt;/p>
            &lt;p>Instance: %parm[#1]% &lt;/p>
            &lt;p>IfDescr: %parm[ifDescr]% &lt;/p>
            &lt;p>IfName: %parm[ifName]% &lt;/p>
            &lt;p>IfAlias: %parm[ifAlias]% &lt;/p></descr>
   <logmsg dest="logndisplay">Agent Interface Up (linkUp Trap)
        </logmsg>
   <severity>Normal</severity>
   <alarm-data reduction-key="%uei%:%dpname%:%nodeid%:%parm[#1]%" alarm-type="2" clear-key="uei.opennms.org/nodes/snmp/interfaceOperDown:%dpname%:%nodeid%:%parm[#1]%"/>
</event>','Normal', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (7, 1, 'uei.opennms.org/generic/traps/SNMP_Authen_Failure', 'OpenNMS-defined trap event: SNMP_Authen_Failure', '<p>An authentication failure trap signifies that the sending protocol entity is the addressee of a
            protocol message that is not properly
            authenticated.</p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <mask>
      <maskelement>
         <mename>generic</mename>
         <mevalue>4</mevalue>
      </maskelement>
   </mask>
   <uei>uei.opennms.org/generic/traps/SNMP_Authen_Failure</uei>
   <event-label>OpenNMS-defined trap event: SNMP_Authen_Failure</event-label>
   <descr>&lt;p>An authentication failure trap signifies that the sending protocol entity is the addressee of a
            protocol message that is not properly
            authenticated.&lt;/p></descr>
   <logmsg dest="logndisplay">Incorrect Community Name (authenticationFailure Trap)
        </logmsg>
   <severity>Warning</severity>
   <alarm-data reduction-key="%uei%:%dpname%:%nodeid%" alarm-type="3" auto-clean="true"/>
</event>','Warning', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (32, 8, 'uei.opennms.org/internal/reloadSnmpPollerConfig', 'OpenNMS-defined internal event: reloadSnmpPollerConfig', '<p>The administrator has changed the SnmpPoller
            configuration. SnmpPoller will load the new configuration.</p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/internal/reloadSnmpPollerConfig</uei>
   <event-label>OpenNMS-defined internal event: reloadSnmpPollerConfig</event-label>
   <descr>&lt;p>The administrator has changed the SnmpPoller
            configuration. SnmpPoller will load the new configuration.&lt;/p></descr>
   <logmsg dest="logndisplay">
            &lt;p>The SnmpPoller configuration files have changed.&lt;/p>
        </logmsg>
   <severity>Minor</severity>
</event>','Minor', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (8, 1, 'uei.opennms.org/generic/traps/SNMP_EGP_Down', 'OpenNMS-defined trap event: SNMP_EGP_Down', '<p>An egpNeighborLoss trap signifies that an EGP neighbor for whom the sending protocol entity was an
            EGP peer has been marked down and the
            peer relationship no longer obtains. The data passed with the event are The name and value of the ifIndex
            egpNeighAddr for the affected
            neighbor.</p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <mask>
      <maskelement>
         <mename>generic</mename>
         <mevalue>5</mevalue>
      </maskelement>
   </mask>
   <uei>uei.opennms.org/generic/traps/SNMP_EGP_Down</uei>
   <event-label>OpenNMS-defined trap event: SNMP_EGP_Down</event-label>
   <descr>&lt;p>An egpNeighborLoss trap signifies that an EGP neighbor for whom the sending protocol entity was an
            EGP peer has been marked down and the
            peer relationship no longer obtains. The data passed with the event are The name and value of the ifIndex
            egpNeighAddr for the affected
            neighbor.&lt;/p></descr>
   <logmsg dest="logndisplay">EGP Neighbor Down (egpNeighborLoss Trap)
        </logmsg>
   <severity>Warning</severity>
</event>','Warning', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (9, 2, 'uei.opennms.org/ackd/acknowledge', 'OpenNMS-defined Acknowledgment request', 'A message received requesting an Acknowledgable be acknowledged.
            <p>Acknowledgement Request:%parm[refId]% of type:%parm[ackType]% was received with the
            action:%parm[ackAction]% was received for User: %parm[ackUser]%</p>
            Typically received from an external source or as a choice of an AckReader implementation.', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/ackd/acknowledge</uei>
   <event-label>OpenNMS-defined Acknowledgment request</event-label>
   <descr>A message received requesting an Acknowledgable be acknowledged.
            &lt;p>Acknowledgement Request:%parm[refId]% of type:%parm[ackType]% was received with the
            action:%parm[ackAction]% was received for User: %parm[ackUser]%&lt;/p>
            Typically received from an external source or as a choice of an AckReader implementation.</descr>
   <logmsg dest="logndisplay">
            &lt;p>Acknowledgement Request:%parm[refId]% of type:%parm[ackType]% was received with the
            action:%parm[ackAction]% was received for User: %parm[ackUser]%.&lt;/p>
        </logmsg>
   <severity>Normal</severity>
</event>','Normal', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (10, 3, 'uei.opennms.org/alarms/trigger', 'Alarm: Generic Trigger', 'A problem has been triggered.', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/alarms/trigger</uei>
   <event-label>Alarm: Generic Trigger</event-label>
   <descr>A problem has been triggered.</descr>
   <logmsg dest="logndisplay">A problem has been triggered on %parm[node]%/%parm[ip]%/%parm[service]%.</logmsg>
   <severity>Warning</severity>
   <alarm-data reduction-key="%uei%:%parm[node]%:%parm[ip]%:%parm[service]%" alarm-type="1" auto-clean="false">
      <update-field field-name="severity" update-on-reduction="true"/>
   </alarm-data>
</event>','Warning', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (11, 3, 'uei.opennms.org/alarms/clear', 'Alarm: Generic Clear', 'A problem has been cleared.', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/alarms/clear</uei>
   <event-label>Alarm: Generic Clear</event-label>
   <descr>A problem has been cleared.</descr>
   <logmsg dest="logndisplay">A problem has been cleared on %parm[node]%/%parm[ip]%/%parm[service]%.</logmsg>
   <severity>Cleared</severity>
   <alarm-data reduction-key="uei.opennms.org/alarms/trigger:%parm[node]%:%parm[ip]%:%parm[service]%" alarm-type="2" auto-clean="false">
      <update-field field-name="severity" update-on-reduction="true"/>
   </alarm-data>
</event>','Cleared', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (12, 3, 'uei.opennms.org/alarms/situation', 'Alarm: Situation', '%parm[situationDescr]%', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/alarms/situation</uei>
   <event-label>Alarm: Situation</event-label>
   <descr>%parm[situationDescr]%</descr>
   <logmsg dest="logndisplay">%parm[situationLogMsg]%</logmsg>
   <severity>Normal</severity>
   <alarm-data reduction-key="%uei%:%parm[situationId]%" alarm-type="3" auto-clean="true"/>
</event>','Normal', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (13, 4, 'uei.opennms.org/bmp/peerDown', 'BMP: Peer Down', '<p>BGP session to Peer %parm[address]% at AS%parm[as]% lost (Router ID: %parm[id]%).
                Reason: %parm[type]%. Error: %parm[error]%.</p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/bmp/peerDown</uei>
   <event-label>BMP: Peer Down</event-label>
   <descr>&lt;p>BGP session to Peer %parm[address]% at AS%parm[as]% lost (Router ID: %parm[id]%).
                Reason: %parm[type]%. Error: %parm[error]%.&lt;/p></descr>
   <logmsg dest="logndisplay">Router has lost the BGP session to Peer %parm[address]% at AS%parm[as]% (Router ID: %parm[id]%).</logmsg>
   <severity>Minor</severity>
   <alarm-data reduction-key="uei.opennms.org/bmp/peerDown:%nodeid%:%interface%:%parm[as]%:%parm[id]%" alarm-type="1" auto-clean="false"/>
</event>','Minor', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (14, 4, 'uei.opennms.org/bmp/peerUp', 'BMP: Peer Up', '<p>BGP session to Peer %parm[address]% at AS%parm[as]% established (Router ID: %parm[id]%).</p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/bmp/peerUp</uei>
   <event-label>BMP: Peer Up</event-label>
   <descr>&lt;p>BGP session to Peer %parm[address]% at AS%parm[as]% established (Router ID: %parm[id]%).&lt;/p></descr>
   <logmsg dest="logndisplay">Router has established the BGP session to Peer %parm[address]% at AS%parm[as]% (Router ID: %parm[id]%).</logmsg>
   <severity>Normal</severity>
   <alarm-data reduction-key="uei.opennms.org/bmp/peerUp:%nodeid%:%interface%:%parm[as]%:%parm[id]%" alarm-type="2" clear-key="uei.opennms.org/bmp/peerDown:%nodeid%:%interface%:%parm[as]%:%parm[id]%" auto-clean="false"/>
</event>','Normal', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (15, 5, 'uei.opennms.org/bsm/serviceOperationalStatusChanged', 'Business Service Monitoring: Service Operational Status Changed', '<p>The operational status for business service ''%parm[businessServiceName]%'', with
            id=%parm[businessServiceId]%, changed from %parm[prevSeverityLabel]% to %parm[newSeverityLabel]%.
            args(%parm[##]%): %parm[all]%</p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/bsm/serviceOperationalStatusChanged</uei>
   <event-label>Business Service Monitoring: Service Operational Status Changed</event-label>
   <descr>&lt;p>The operational status for business service ''%parm[businessServiceName]%'', with
            id=%parm[businessServiceId]%, changed from %parm[prevSeverityLabel]% to %parm[newSeverityLabel]%.
            args(%parm[##]%): %parm[all]%&lt;/p></descr>
   <logmsg dest="logndisplay">The operational status for business service ''%parm[businessServiceName]%'' changed
            from %parm[prevSeverityLabel]% to %parm[newSeverityLabel]%.
        </logmsg>
   <severity>Indeterminate</severity>
</event>','Indeterminate', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (16, 5, 'uei.opennms.org/bsm/serviceProblem', 'Business Service Monitoring: Service Problem', '<p>There are currently one or more problems affecting business service ''%parm[businessServiceName]%''. Root cause: %parm[rootCause]%.
            args(%parm[##]%): %parm[all]%</p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/bsm/serviceProblem</uei>
   <event-label>Business Service Monitoring: Service Problem</event-label>
   <descr>&lt;p>There are currently one or more problems affecting business service ''%parm[businessServiceName]%''. Root cause: %parm[rootCause]%.
            args(%parm[##]%): %parm[all]%&lt;/p></descr>
   <logmsg dest="logndisplay">One or more problems are affecting business service ''%parm[businessServiceName]%''.
        </logmsg>
   <severity>Indeterminate</severity>
   <alarm-data reduction-key="%uei%:%parm[businessServiceId]%" alarm-type="1" auto-clean="false">
      <update-field field-name="severity" update-on-reduction="true"/>
   </alarm-data>
</event>','Indeterminate', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (17, 5, 'uei.opennms.org/bsm/serviceProblemResolved', 'Business Service Monitoring: Service Problem Resolved', '<p>The problem affecting business service ''%parm[businessServiceName]%'' has been resolved.
            args(%parm[##]%): %parm[all]%</p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/bsm/serviceProblemResolved</uei>
   <event-label>Business Service Monitoring: Service Problem Resolved</event-label>
   <descr>&lt;p>The problem affecting business service ''%parm[businessServiceName]%'' has been resolved.
            args(%parm[##]%): %parm[all]%&lt;/p></descr>
   <logmsg dest="logndisplay">The problems affecting business service ''%parm[businessServiceName]%'' have been
            resolved.
        </logmsg>
   <severity>Indeterminate</severity>
   <alarm-data reduction-key="uei.opennms.org/bsm/serviceProblem:%parm[businessServiceId]%" alarm-type="2" auto-clean="false"/>
</event>','Indeterminate', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (18, 5, 'uei.opennms.org/bsm/graphInvalidated', 'Business Service Monitoring: Graph invalidated', '<p>Business Service ''%parm[businessServiceName]%'' with ID ''%parm[businessServiceId]%'' is affected by the deletion of %parm[cause]%.
             A reload of the BSM daemon is scheduled. Make sure the Business Service still works properly.
             Please verify it''s <a href="admin/bsm/adminpage.jsp">definition</a>.</p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/bsm/graphInvalidated</uei>
   <event-label>Business Service Monitoring: Graph invalidated</event-label>
   <descr>&lt;p>Business Service ''%parm[businessServiceName]%'' with ID ''%parm[businessServiceId]%'' is affected by the deletion of %parm[cause]%.
             A reload of the BSM daemon is scheduled. Make sure the Business Service still works properly.
             Please verify it''s &lt;a href=&quot;admin/bsm/adminpage.jsp&quot;>definition&lt;/a>.&lt;/p></descr>
   <logmsg dest="logndisplay">Business service ''%parm[businessServiceName]%'' with ID ''%parm[businessServiceId]%'' is affected by the deletion of %parm[cause]%.</logmsg>
   <severity>Warning</severity>
   <alarm-data reduction-key="%uei%:%parm[businessServiceId]%" alarm-type="1" auto-clean="false"/>
</event>','Warning', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (19, 5, 'uei.opennms.org/internal/serviceDeleted', 'Business Service Monitoring: Service deleted', '<p>The business service ''%parm[businessServiceName]%'' has been deleted.
            args(%parm[##]%): %parm[all]%</p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/internal/serviceDeleted</uei>
   <event-label>Business Service Monitoring: Service deleted</event-label>
   <descr>&lt;p>The business service ''%parm[businessServiceName]%'' has been deleted.
            args(%parm[##]%): %parm[all]%&lt;/p></descr>
   <logmsg dest="logndisplay">The business service ''%parm[businessServiceName]%'' has been deleted.
       </logmsg>
   <severity>Indeterminate</severity>
   <alarm-data reduction-key="%uei%:%parm[businessServiceId]%" alarm-type="2" clear-key="uei.opennms.org/bsm/serviceProblem:%parm[businessServiceId]%" auto-clean="false">
      <update-field field-name="logmsg" update-on-reduction="false"/>
   </alarm-data>
</event>','Indeterminate', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (20, 6, 'uei.opennms.org/internal/capsd/discPause', 'OpenNMS-defined internal event: capsd discPause', '<p>The services scanning engine has asked discovery to
            pause due to a backlog of interfaces yet to be scanned.
            </p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/internal/capsd/discPause</uei>
   <event-label>OpenNMS-defined internal event: capsd discPause</event-label>
   <descr>&lt;p>The services scanning engine has asked discovery to
            pause due to a backlog of interfaces yet to be scanned.
            &lt;/p></descr>
   <logmsg dest="logonly">
            Capsd has asked Discovery to pause momentarily.
        </logmsg>
   <severity>Warning</severity>
</event>','Warning', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (21, 6, 'uei.opennms.org/internal/capsd/discResume', 'OpenNMS-defined internal event: capsd discResume', '<p>Capsd is approving discovery to resume adding nodes
            to the Capsd queue.</p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/internal/capsd/discResume</uei>
   <event-label>OpenNMS-defined internal event: capsd discResume</event-label>
   <descr>&lt;p>Capsd is approving discovery to resume adding nodes
            to the Capsd queue.&lt;/p></descr>
   <logmsg dest="logonly">
            Capsd is ready for Discovery to resume scheduling nodes.
        </logmsg>
   <severity>Normal</severity>
</event>','Normal', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (22, 6, 'uei.opennms.org/internal/capsd/forceRescan', 'OpenNMS-defined internal event: capsd forceRescan', '<p>A services scan has been forced.</p>
            <p>The administrator has forced a services scan on
            this node to update the list of supported
            services.</p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/internal/capsd/forceRescan</uei>
   <event-label>OpenNMS-defined internal event: capsd forceRescan</event-label>
   <descr>&lt;p>A services scan has been forced.&lt;/p>
            &lt;p>The administrator has forced a services scan on
            this node to update the list of supported
            services.&lt;/p></descr>
   <logmsg dest="logndisplay">
            &lt;p>A services scan has been forced on this
            node.&lt;/p>
        </logmsg>
   <severity>Warning</severity>
</event>','Warning', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (23, 6, 'uei.opennms.org/internal/capsd/rescanCompleted', 'OpenNMS-defined internal event: capsd rescanCompleted', '<p>A services scan has been completed.</p>
            <p>The list of services on this node has been
            updated.</p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/internal/capsd/rescanCompleted</uei>
   <event-label>OpenNMS-defined internal event: capsd rescanCompleted</event-label>
   <descr>&lt;p>A services scan has been completed.&lt;/p>
            &lt;p>The list of services on this node has been
            updated.&lt;/p></descr>
   <logmsg dest="logndisplay">
            &lt;p>A services scan has been completed on this
            node.&lt;/p>
        </logmsg>
   <severity>Normal</severity>
</event>','Normal', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (24, 6, 'uei.opennms.org/internal/capsd/addNode', 'OpenNMS-defined internal event: capsd addNode', '<p>This event is an external command to add a node
            to the database. The required paramater is the IP
            address for the main interface: %interface%, and
            the optional parameter of a node label: %nodelabel%.</p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/internal/capsd/addNode</uei>
   <event-label>OpenNMS-defined internal event: capsd addNode</event-label>
   <descr>&lt;p>This event is an external command to add a node
            to the database. The required paramater is the IP
            address for the main interface: %interface%, and
            the optional parameter of a node label: %nodelabel%.&lt;/p></descr>
   <logmsg dest="logndisplay">
            &lt;p>A request has been made to add a node with interface:
            %interface% and node label: %nodelabel%.&lt;/p>
        </logmsg>
   <severity>Normal</severity>
</event>','Normal', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (25, 6, 'uei.opennms.org/internal/capsd/deleteNode', 'OpenNMS-defined internal event: capsd deleteNode', '<p>This event is an external command to delete a node
            from the database. The required paramater is the IP
            address for one interface: %interface%.</p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/internal/capsd/deleteNode</uei>
   <event-label>OpenNMS-defined internal event: capsd deleteNode</event-label>
   <descr>&lt;p>This event is an external command to delete a node
            from the database. The required paramater is the IP
            address for one interface: %interface%.&lt;/p></descr>
   <logmsg dest="logndisplay">
            &lt;p>A request has been made to delete a node with interface:
            %interface%.&lt;/p>
        </logmsg>
   <severity>Normal</severity>
</event>','Normal', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (26, 6, 'uei.opennms.org/internal/capsd/deleteInterface', 'OpenNMS-defined internal event: capsd deleteInterface', '<p>This event is an external command to delete an interface
            from the database. The required paramater is the IP
            address for the interface: %interface%, or the nodeid %nodeid%
            and ifIndex %ifindex%.</p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/internal/capsd/deleteInterface</uei>
   <event-label>OpenNMS-defined internal event: capsd deleteInterface</event-label>
   <descr>&lt;p>This event is an external command to delete an interface
            from the database. The required paramater is the IP
            address for the interface: %interface%, or the nodeid %nodeid%
            and ifIndex %ifindex%.&lt;/p></descr>
   <logmsg dest="logndisplay">
            &lt;p>A request has been made to delete an interface:
            %interface% on node %nodeid% with ifIndex %ifindex%.&lt;/p>
        </logmsg>
   <severity>Normal</severity>
</event>','Normal', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (27, 6, 'uei.opennms.org/internal/capsd/changeService', 'OpenNMS-defined internal event: capsd changeService', '<p>This event will add or remove a service from an interface.
            The paramters include the interface, %interface%, the service,
            %service%, and any required qualifiers, %parm[#2]%. The action
            taken will be: %parm[#1]%.</p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/internal/capsd/changeService</uei>
   <event-label>OpenNMS-defined internal event: capsd changeService</event-label>
   <descr>&lt;p>This event will add or remove a service from an interface.
            The paramters include the interface, %interface%, the service,
            %service%, and any required qualifiers, %parm[#2]%. The action
            taken will be: %parm[#1]%.&lt;/p></descr>
   <logmsg dest="logndisplay">
            &lt;p>A request has been made to %parm[#1]% the %service% service
            on interface: %interface%.&lt;/p>
        </logmsg>
   <severity>Normal</severity>
</event>','Normal', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (28, 7, 'uei.opennms.org/nodes/dataCollectionFailed', 'OpenNMS-defined node event: dataCollectionFailed', '<p>%service% data collection on interface %interface% failed because of the following condition: ''%parm[reason]%''.</p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/nodes/dataCollectionFailed</uei>
   <event-label>OpenNMS-defined node event: dataCollectionFailed</event-label>
   <descr>&lt;p>%service% data collection on interface %interface% failed because of the following condition: ''%parm[reason]%''.&lt;/p></descr>
   <logmsg dest="logndisplay">%service% data collection on interface %interface% failed.</logmsg>
   <severity>Minor</severity>
   <alarm-data reduction-key="%uei%:%dpname%:%nodeid%:%interface%:%service%" alarm-type="1" auto-clean="false"/>
</event>','Minor', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (29, 7, 'uei.opennms.org/nodes/dataCollectionSucceeded', 'OpenNMS-defined node event: dataCollectionSucceeded', '<p>%service% data collection on interface %interface% previously failed and has been restored.</p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/nodes/dataCollectionSucceeded</uei>
   <event-label>OpenNMS-defined node event: dataCollectionSucceeded</event-label>
   <descr>&lt;p>%service% data collection on interface %interface% previously failed and has been restored.&lt;/p></descr>
   <logmsg dest="logndisplay">%service% data collection on interface %interface% previously failed and has been restored.</logmsg>
   <severity>Normal</severity>
   <alarm-data reduction-key="%uei%:%dpname%:%nodeid%:%interface%:%service%" alarm-type="2" clear-key="uei.opennms.org/nodes/dataCollectionFailed:%dpname%:%nodeid%:%interface%:%service%" auto-clean="false"/>
</event>','Normal', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (30, 8, 'uei.opennms.org/internal/reloadScriptConfig', 'OpenNMS-defined internal event: reloadScriptConfig', '<p>The administrator has changed the ScriptD configuration.
            ScriptD will load the new configuration.</p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/internal/reloadScriptConfig</uei>
   <event-label>OpenNMS-defined internal event: reloadScriptConfig</event-label>
   <descr>&lt;p>The administrator has changed the ScriptD configuration.
            ScriptD will load the new configuration.&lt;/p></descr>
   <logmsg dest="logndisplay">
            &lt;p>The ScriptD configuration files have changed.&lt;/p>
        </logmsg>
   <severity>Minor</severity>
</event>','Minor', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (31, 8, 'uei.opennms.org/internal/reloadVacuumdConfig', 'OpenNMS-defined internal event: reloadVacuumdConfig', '<p>The administrator has changed the Vacuumd
            configuration. Vacuumd will load the new configuration.</p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/internal/reloadVacuumdConfig</uei>
   <event-label>OpenNMS-defined internal event: reloadVacuumdConfig</event-label>
   <descr>&lt;p>The administrator has changed the Vacuumd
            configuration. Vacuumd will load the new configuration.&lt;/p></descr>
   <logmsg dest="logndisplay">
            &lt;p>The Vacuumd configuration files have changed.&lt;/p>
        </logmsg>
   <severity>Minor</severity>
</event>','Minor', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (33, 8, 'uei.opennms.org/internal/reloadDaemonConfig', 'OpenNMS-defined internal event: reload specified daemon configuration', '<p>The administrator has changed the daemon: %parm[daemonName]%
            configuration files and requests the configuration to be re-marshaled and applied.</p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/internal/reloadDaemonConfig</uei>
   <event-label>OpenNMS-defined internal event: reload specified daemon configuration</event-label>
   <descr>&lt;p>The administrator has changed the daemon: %parm[daemonName]%
            configuration files and requests the configuration to be re-marshaled and applied.&lt;/p></descr>
   <logmsg dest="logndisplay">
            &lt;p>The daemon: %parm[daemonName]% configuration files has changed.&lt;/p>
        </logmsg>
   <severity>Normal</severity>
</event>','Normal', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (34, 8, 'uei.opennms.org/internal/reloadDaemonConfigFailed', 'OpenNMS-defined internal event: reload specified daemon configuration failed', '<p>The administrator has changed the daemon: %parm[daemonName]%
            configuration files and the request for the configuration to be re-marshaled and applied
            has failed because of the following condition %parm[reason]%.</p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/internal/reloadDaemonConfigFailed</uei>
   <event-label>OpenNMS-defined internal event: reload specified daemon configuration failed</event-label>
   <descr>&lt;p>The administrator has changed the daemon: %parm[daemonName]%
            configuration files and the request for the configuration to be re-marshaled and applied
            has failed because of the following condition %parm[reason]%.&lt;/p></descr>
   <logmsg dest="logndisplay">
            &lt;p>The daemon: %parm[daemonName]% configuration changes have failed to be
            applied.&lt;/p>
        </logmsg>
   <severity>Major</severity>
   <alarm-data reduction-key="%uei%:%dpname%:%parm[daemonName]%" alarm-type="1" auto-clean="false"/>
</event>','Major', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (35, 8, 'uei.opennms.org/internal/reloadDaemonConfigSuccessful', 'OpenNMS-defined internal event: reload specified daemon configuration successful', '<p>The administrator has changed the daemon: %parm[daemonName]%
            configuration files and the request for the configuration to be re-marshaled and applied
            has succeeded.</p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/internal/reloadDaemonConfigSuccessful</uei>
   <event-label>OpenNMS-defined internal event: reload specified daemon configuration successful</event-label>
   <descr>&lt;p>The administrator has changed the daemon: %parm[daemonName]%
            configuration files and the request for the configuration to be re-marshaled and applied
            has succeeded.&lt;/p></descr>
   <logmsg dest="logndisplay">
            &lt;p>The daemon: %parm[daemonName]% configuration changes have successfully been
            applied.&lt;/p>
        </logmsg>
   <severity>Normal</severity>
   <alarm-data reduction-key="%uei%:%dpname%:%nodeid%" alarm-type="2" clear-key="uei.opennms.org/internal/reloadDaemonConfigFailed:%dpname%:%parm[daemonName]%" auto-clean="false"/>
</event>','Normal', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (36, 8, 'uei.opennms.org/internal/thresholdConfigChange', 'OpenNMS-defined internal event: threshold configuration changed', 'This event is sent by the WebUI or the user when threshold configuration has changed and should be reloaded', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/internal/thresholdConfigChange</uei>
   <event-label>OpenNMS-defined internal event: threshold configuration changed</event-label>
   <descr>This event is sent by the WebUI or the user when threshold configuration has changed and should be reloaded</descr>
   <logmsg dest="logndisplay">
            The thresholds configuration has been changed and should be reloaded
        </logmsg>
   <severity>Normal</severity>
</event>','Normal', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (37, 8, 'uei.opennms.org/internal/eventsConfigChange', 'OpenNMS-defined internal event: event configuration changed', 'This event is sent by the WebUI or the user when event configuration has changed and should be reloaded', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/internal/eventsConfigChange</uei>
   <event-label>OpenNMS-defined internal event: event configuration changed</event-label>
   <descr>This event is sent by the WebUI or the user when event configuration has changed and should be reloaded</descr>
   <logmsg dest="logndisplay">
            The events configuration has been changed and should be reloaded
        </logmsg>
   <severity>Normal</severity>
</event>','Normal', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (38, 8, 'uei.opennms.org/internal/reloadPollerConfig', 'OpenNMS-defined internal event: reloadPollerConfig', '<p>The administrator has changed the poller
            configuration files. The pollers and related services will
            now restart to detect the changes.</p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/internal/reloadPollerConfig</uei>
   <event-label>OpenNMS-defined internal event: reloadPollerConfig</event-label>
   <descr>&lt;p>The administrator has changed the poller
            configuration files. The pollers and related services will
            now restart to detect the changes.&lt;/p></descr>
   <logmsg dest="logndisplay">
            &lt;p>The poller configuration files have
            changed.&lt;/p>
        </logmsg>
   <severity>Warning</severity>
</event>','Warning', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (39, 8, 'uei.opennms.org/internal/syslogdConfigChange', 'OpenNMS-defined internal event: Syslogd configuration changed', 'This event is sent by the WebUI or the user when the Syslogd configuration has changed and should be
            reloaded', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/internal/syslogdConfigChange</uei>
   <event-label>OpenNMS-defined internal event: Syslogd configuration changed</event-label>
   <descr>This event is sent by the WebUI or the user when the Syslogd configuration has changed and should be
            reloaded</descr>
   <logmsg dest="logndisplay">
            The Syslogd configuration has been changed and should be reloaded
        </logmsg>
   <severity>Normal</severity>
</event>','Normal', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (40, 8, 'uei.opennms.org/internal/configureSNMP', 'OpenNMS-defined internal event: configureSNMP', '<p>SNMP definition for IP address
            %parm[firstIPAddress]%-%parm[lastIPAddress]% has been
            updated with community string
            "%parm[communityString]%"</p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/internal/configureSNMP</uei>
   <event-label>OpenNMS-defined internal event: configureSNMP</event-label>
   <descr>&lt;p>SNMP definition for IP address
            %parm[firstIPAddress]%-%parm[lastIPAddress]% has been
            updated with community string
            &quot;%parm[communityString]%&quot;&lt;/p></descr>
   <logmsg dest="logonly">
            &lt;p>SNMP community string
            &quot;%parm[communityString]%&quot; has been defined
            for IP %parm[firstIPAddress]%-%parm[lastIPAddress]%.&lt;/p>
        </logmsg>
   <severity>Warning</severity>
</event>','Warning', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (41, 8, 'uei.opennms.org/internal/translator/entityConfigChanged', 'OpenNMS defined event: A trap based event was received indicating a configuration change on a
            device and has been translated to this generic event', 'This is a translated entity configuration change event.<p>

            <p>Source: %parm[configSource]% </p>
            <p>User: %parm[configUser]% </p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/internal/translator/entityConfigChanged</uei>
   <event-label>OpenNMS defined event: A trap based event was received indicating a configuration change on a
            device and has been translated to this generic event</event-label>
   <descr>This is a translated entity configuration change event.&lt;p>

            &lt;p>Source: %parm[configSource]% &lt;/p>
            &lt;p>User: %parm[configUser]% &lt;/p></descr>
   <logmsg dest="logndisplay">
            &lt;p>&quot;%parm[configUser]%&quot; changed entity %nodelabel%_%interface% from source: %parm[configSource]% &lt;/a>&lt;/p>
        </logmsg>
   <severity>Warning</severity>
   <alarm-data reduction-key="%uei%:%dpname%:%nodeid%:%interface%" alarm-type="3" auto-clean="false"/>
</event>','Warning', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (42, 9, 'uei.opennms.org/correlation/serviceFlapping', 'OpenNMS-defined correlator event: A service has been detected to be in a flapping state', 'This event is sent when a correlation rule has detected that a service is flapping.', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/correlation/serviceFlapping</uei>
   <event-label>OpenNMS-defined correlator event: A service has been detected to be in a flapping state</event-label>
   <descr>This event is sent when a correlation rule has detected that a service is flapping.</descr>
   <logmsg dest="logndisplay">
            The service: %service% has been correlated to indicate a flapping state.
        </logmsg>
   <severity>Minor</severity>
   <alarm-data reduction-key="%uei%:%dpname%:%nodeid%:%interface%:%service%" alarm-type="3" auto-clean="false"/>
</event>','Minor', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (43, 9, 'uei.opennms.org/internal/droolsEngineException', 'OpenNMS-defined Drools Engine Encountered Exception', 'Drools engine encountered an exception while running rules', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/internal/droolsEngineException</uei>
   <event-label>OpenNMS-defined Drools Engine Encountered Exception</event-label>
   <descr>Drools engine encountered an exception while running rules</descr>
   <logmsg dest="logndisplay">
            Drools engine rule %parm[enginename]% has encountered an exception : %parm[stacktrace]%.
        </logmsg>
   <severity>Normal</severity>
   <alarm-data reduction-key="%uei%:%parm[enginename]%" alarm-type="3"/>
</event>','Normal', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (44, 10, 'uei.opennms.org/threshold/highThresholdExceeded', 'OpenNMS-defined threshold event: highThresholdExceeded', 'A high threshold for the following metric exceeded: %parm[all]%', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/threshold/highThresholdExceeded</uei>
   <event-label>OpenNMS-defined threshold event: highThresholdExceeded</event-label>
   <descr>A high threshold for the following metric exceeded: %parm[all]%</descr>
   <logmsg dest="logndisplay">
            High threshold exceeded for service %service% metric %parm[expressionLabel]% [%parm[ds]%] on interface %parm[label]%/%interface%
        </logmsg>
   <severity>Warning</severity>
   <alarm-data reduction-key="%uei%:%dpname%:%nodeid%:%interface%:%parm[ds]%:%parm[threshold]%:%parm[trigger]%:%parm[rearm]%:%parm[label]%" alarm-type="1" auto-clean="false"/>
</event>','Warning', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (45, 10, 'uei.opennms.org/threshold/lowThresholdExceeded', 'OpenNMS-defined threshold event: lowThresholdExceeded', 'Low threshold for the following metric exceeded: %parm[all]%', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/threshold/lowThresholdExceeded</uei>
   <event-label>OpenNMS-defined threshold event: lowThresholdExceeded</event-label>
   <descr>Low threshold for the following metric exceeded: %parm[all]%</descr>
   <logmsg dest="logndisplay">
            Low threshold exceeded for service %service% metric %parm[expressionLabel]% [%parm[ds]%]  on interface %parm[label]%/%interface%
        </logmsg>
   <severity>Warning</severity>
   <alarm-data reduction-key="%uei%:%dpname%:%nodeid%:%interface%:%parm[ds]%:%parm[threshold]%:%parm[trigger]%:%parm[rearm]%:%parm[label]%" alarm-type="1" auto-clean="false"/>
</event>','Warning', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (46, 10, 'uei.opennms.org/threshold/highThresholdRearmed', 'OpenNMS-defined threshold event: highThresholdRearmed', 'High threshold has been rearmed for the following metric: %parm[all]%', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/threshold/highThresholdRearmed</uei>
   <event-label>OpenNMS-defined threshold event: highThresholdRearmed</event-label>
   <descr>High threshold has been rearmed for the following metric: %parm[all]%</descr>
   <logmsg dest="logndisplay">
            High threshold rearmed for service %service% metric %parm[expressionLabel]% [%parm[ds]%] on interface %parm[label]%/%interface%
        </logmsg>
   <severity>Normal</severity>
   <alarm-data reduction-key="%uei%:%dpname%:%nodeid%:%interface%:%parm[ds]%:%parm[threshold]%:%parm[trigger]%:%parm[rearm]%:%parm[label]%" alarm-type="2" clear-key="uei.opennms.org/threshold/highThresholdExceeded:%dpname%:%nodeid%:%interface%:%parm[ds]%:%parm[threshold]%:%parm[trigger]%:%parm[rearm]%:%parm[label]%" auto-clean="false"/>
</event>','Normal', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (47, 10, 'uei.opennms.org/threshold/lowThresholdRearmed', 'OpenNMS-defined threshold event: lowThresholdRearmed', 'Low threshold has been rearmed for the following metric: %parm[all]%', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/threshold/lowThresholdRearmed</uei>
   <event-label>OpenNMS-defined threshold event: lowThresholdRearmed</event-label>
   <descr>Low threshold has been rearmed for the following metric: %parm[all]%</descr>
   <logmsg dest="logndisplay">
            Low threshold rearmed for service %service% metric %parm[expressionLabel]% [%parm[ds]%] on interface %parm[label]%/%interface%
        </logmsg>
   <severity>Normal</severity>
   <alarm-data reduction-key="%uei%:%dpname%:%nodeid%:%interface%:%parm[ds]%:%parm[threshold]%:%parm[trigger]%:%parm[rearm]%:%parm[label]%" alarm-type="2" clear-key="uei.opennms.org/threshold/lowThresholdExceeded:%dpname%:%nodeid%:%interface%:%parm[ds]%:%parm[threshold]%:%parm[trigger]%:%parm[rearm]%:%parm[label]%" auto-clean="false"/>
</event>','Normal', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (48, 10, 'uei.opennms.org/threshold/relativeChangeExceeded', 'OpenNMS-defined threshold event: relativeChangeExceeded', 'Relative change threshold for the following metric exceeded: %parm[all]%', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/threshold/relativeChangeExceeded</uei>
   <event-label>OpenNMS-defined threshold event: relativeChangeExceeded</event-label>
   <descr>Relative change threshold for the following metric exceeded: %parm[all]%</descr>
   <logmsg dest="logndisplay">
            Relative change change exceeded for service %service% metric %parm[expressionLabel]% [%parm[ds]%] on interface %parm[label]%/%interface%
        </logmsg>
   <severity>Warning</severity>
</event>','Warning', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (57, 12, 'uei.opennms.org/internal/notificationsTurnedOff', 'OpenNMS-defined internal event: notificationsTurnedOff', '<p>Notifications have been disabled.</p>
            <p>The administrator has disabled notifications on
            OpenNMS. No pages or emails will be sent until notifications
            are reenabled.</p>
            <p>
            Responsible user: <em>%parm[remoteUser]%</em>
            at <em>%parm[remoteHost]% (%parm[remoteAddr]%)</em>
            </p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/internal/notificationsTurnedOff</uei>
   <event-label>OpenNMS-defined internal event: notificationsTurnedOff</event-label>
   <descr>&lt;p>Notifications have been disabled.&lt;/p>
            &lt;p>The administrator has disabled notifications on
            OpenNMS. No pages or emails will be sent until notifications
            are reenabled.&lt;/p>
            &lt;p>
            Responsible user: &lt;em>%parm[remoteUser]%&lt;/em>
            at &lt;em>%parm[remoteHost]% (%parm[remoteAddr]%)&lt;/em>
            &lt;/p></descr>
   <logmsg dest="logndisplay">
            &lt;p>Notifications have been disabled.&lt;/p>
        </logmsg>
   <severity>Minor</severity>
</event>','Minor', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (58, 12, 'uei.opennms.org/internal/notificationsTurnedOn', 'OpenNMS-defined internal event: notificationsTurnedOn', '<p>Notifications have been enabled.</p>
            <p>The administrator has enabled notifications on
            OpenNMS. Pages and/or emails will be sent based upon receipt
            of important events.</p>
            <p>
            Responsible user: <em>%parm[remoteUser]%</em>
            at <em>%parm[remoteHost]% (%parm[remoteAddr]%)</em>
            </p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/internal/notificationsTurnedOn</uei>
   <event-label>OpenNMS-defined internal event: notificationsTurnedOn</event-label>
   <descr>&lt;p>Notifications have been enabled.&lt;/p>
            &lt;p>The administrator has enabled notifications on
            OpenNMS. Pages and/or emails will be sent based upon receipt
            of important events.&lt;/p>
            &lt;p>
            Responsible user: &lt;em>%parm[remoteUser]%&lt;/em>
            at &lt;em>%parm[remoteHost]% (%parm[remoteAddr]%)&lt;/em>
            &lt;/p></descr>
   <logmsg dest="logndisplay">
            &lt;p>Notifications have been enabled.&lt;/p>
        </logmsg>
   <severity>Warning</severity>
</event>','Warning', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (59, 12, 'uei.opennms.org/internal/restartSCM', 'OpenNMS-defined internal event: restartSCM', '<p>SCM has been asked to restart.</p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/internal/restartSCM</uei>
   <event-label>OpenNMS-defined internal event: restartSCM</event-label>
   <descr>&lt;p>SCM has been asked to restart.&lt;/p></descr>
   <logmsg dest="logonly">
            SCM has been asked to restart.
        </logmsg>
   <severity>Indeterminate</severity>
</event>','Indeterminate', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (60, 12, 'uei.opennms.org/internal/rtc/subscribe', 'OpenNMS-defined internal event: rtc subscribe', '<p>This event is generated to RTC by any process that
            wishes to receive POSTs of RTC data.</p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/internal/rtc/subscribe</uei>
   <event-label>OpenNMS-defined internal event: rtc subscribe</event-label>
   <descr>&lt;p>This event is generated to RTC by any process that
            wishes to receive POSTs of RTC data.&lt;/p></descr>
   <logmsg dest="donotpersist">
            A subscription to RTC for the %parm[viewname]% for
            %parm[url]% has been generated.
        </logmsg>
   <severity>Normal</severity>
</event>','Normal', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (61, 12, 'uei.opennms.org/internal/rtc/unsubscribe', 'OpenNMS-defined internal event: rtc unsubscribe', '<p>This event is generated to RTC by any subscribed
            process that wishes to discontinue receipt of POSTs of RTC
            data.</p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/internal/rtc/unsubscribe</uei>
   <event-label>OpenNMS-defined internal event: rtc unsubscribe</event-label>
   <descr>&lt;p>This event is generated to RTC by any subscribed
            process that wishes to discontinue receipt of POSTs of RTC
            data.&lt;/p></descr>
   <logmsg dest="donotpersist">
            Unsubscribe request received from %parm[url]%.
        </logmsg>
   <severity>Warning</severity>
</event>','Warning', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (62, 12, 'uei.opennms.org/internal/serviceManaged', 'OpenNMS-defined internal event: serviceManaged', '<p>The service %service% on interface %interface% is
            being remanaged.</p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/internal/serviceManaged</uei>
   <event-label>OpenNMS-defined internal event: serviceManaged</event-label>
   <descr>&lt;p>The service %service% on interface %interface% is
            being remanaged.&lt;/p></descr>
   <logmsg dest="logndisplay">
            The service %service% on interface %interface% is being
            remanaged.
        </logmsg>
   <severity>Warning</severity>
</event>','Warning', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (63, 12, 'uei.opennms.org/internal/schedOutagesChanged', 'OpenNMS-defined internal event: scehduled outage configuration changed', 'This event is sent by the WebUI or the user when scheduled outage configuration has changed and should be
            reloaded', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/internal/schedOutagesChanged</uei>
   <event-label>OpenNMS-defined internal event: scehduled outage configuration changed</event-label>
   <descr>This event is sent by the WebUI or the user when scheduled outage configuration has changed and should be
            reloaded</descr>
   <logmsg dest="logndisplay">
            The scheduled outage configuration has been changed and should be reloaded
        </logmsg>
   <severity>Normal</severity>
</event>','Normal', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (64, 12, 'uei.opennms.org/internal/promoteQueueData', 'OpenNMS-defined event: A request has been made promote data from the RRD Queue', 'This event is generated to invoke the promotion data of the Queueing RRD Strategy.', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/internal/promoteQueueData</uei>
   <event-label>OpenNMS-defined event: A request has been made promote data from the RRD Queue</event-label>
   <descr>This event is generated to invoke the promotion data of the Queueing RRD Strategy.</descr>
   <logmsg dest="donotpersist">
            A request has been generated to promote data from the queue for the file(s): %parm[filesToPromote]%.
        </logmsg>
   <severity>Normal</severity>
</event>','Normal', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (65, 12, 'uei.opennms.org/internal/authentication/successfulLogin', 'OpenNMS-defined internal event: a user has successfully authentication to the WebUI', 'This event is sent by the WebUI when a user has successfully authenticated', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/internal/authentication/successfulLogin</uei>
   <event-label>OpenNMS-defined internal event: a user has successfully authentication to the WebUI</event-label>
   <descr>This event is sent by the WebUI when a user has successfully authenticated</descr>
   <logmsg dest="donotpersist">
            OpenNMS user %parm[user]% has logged in from %parm[ip]%.
        </logmsg>
   <severity>Normal</severity>
</event>','Normal', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (75, 12, 'uei.opennms.org/internal/telemetry/clockSkewDetected', 'Clock Skew detected', 'Clock skew (%parm[delta]% ms) detected for flow exporter (maxClockSkew = %parm[maxClockSkew]% secs)', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/internal/telemetry/clockSkewDetected</uei>
   <event-label>Clock Skew detected</event-label>
   <descr>Clock skew (%parm[delta]% ms) detected for flow exporter (maxClockSkew = %parm[maxClockSkew]% secs)</descr>
   <logmsg dest="logndisplay">Clock skew for exporter with interface ''%interface%'' in location ''%parm[monitoringSystemLocation]%'' detected by ''%parm[monitoringSystemId]%''.</logmsg>
   <severity>Warning</severity>
</event>','Warning', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (76, 12, 'uei.opennms.org/translator/telemetry/clockSkewDetected', 'Clock Skew detected', 'Clock skew (%parm[delta]% ms) detected for flow exporter (maxClockSkew = %parm[maxClockSkew]% secs)', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/translator/telemetry/clockSkewDetected</uei>
   <event-label>Clock Skew detected</event-label>
   <descr>Clock skew (%parm[delta]% ms) detected for flow exporter (maxClockSkew = %parm[maxClockSkew]% secs)</descr>
   <logmsg dest="logndisplay">Clock skew for exporter with interface ''%interface%'' in location ''%parm[monitoringSystemLocation]%'' detected by ''%parm[monitoringSystemId]%''.</logmsg>
   <severity>Warning</severity>
   <alarm-data reduction-key="%uei%:%parm[monitoringSystemLocation]%:%interface%" alarm-type="3" auto-clean="true"/>
</event>','Warning', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (77, 12, 'uei.opennms.org/internal/applicationDeleted', 'OpenNMS-defined application event: applicationDeleted', 'Application ''%parm[applicationName]%'' with ID ''%parm[applicationId]%'' has been deleted.', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/internal/applicationDeleted</uei>
   <event-label>OpenNMS-defined application event: applicationDeleted</event-label>
   <descr>Application ''%parm[applicationName]%'' with ID ''%parm[applicationId]%'' has been deleted.</descr>
   <logmsg dest="logndisplay">Application ''%parm[applicationName]%'' has been deleted.</logmsg>
   <severity>Warning</severity>
</event>','Warning', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (78, 12, 'uei.opennms.org/internal/applicationChanged', 'OpenNMS-defined node event: applicationChanged', 'The application ''%parm[applicationName]%'' with ID ''%parm[applicationId]%'' has been changed.', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/internal/applicationChanged</uei>
   <event-label>OpenNMS-defined node event: applicationChanged</event-label>
   <descr>The application ''%parm[applicationName]%'' with ID ''%parm[applicationId]%'' has been changed.</descr>
   <logmsg dest="logndisplay">Application ''%parm[applicationName]%'' configuration has been changed.</logmsg>
   <severity>Warning</severity>
   <operinstruct>Make sure ''%parm[applicationName]%'' application''s definition still reflects the requirements. Please verify it''s &lt;a href=&quot;admin/applications.htm?applicationid=%parm[applicationId]%&amp;edit=services&quot;>definition&lt;/a>.</operinstruct>
</event>','Warning', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (79, 12, 'uei.opennms.org/internal/applicationCreated', 'OpenNMS-defined node event: applicationCreated', 'The application ''%parm[applicationName]%'' with ID ''%parm[applicationId]%'' has been created.', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/internal/applicationCreated</uei>
   <event-label>OpenNMS-defined node event: applicationCreated</event-label>
   <descr>The application ''%parm[applicationName]%'' with ID ''%parm[applicationId]%'' has been created.</descr>
   <logmsg dest="logndisplay">Application ''%parm[applicationName]%'' has been created.</logmsg>
   <severity>Normal</severity>
</event>','Normal', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (80, 12, 'uei.opennms.org/internal/telemetry/illegalFlowDetected', 'Illegal flow detected', 'A flow was dropped due to the following reason: ''%parm[cause]%''', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/internal/telemetry/illegalFlowDetected</uei>
   <event-label>Illegal flow detected</event-label>
   <descr>A flow was dropped due to the following reason: ''%parm[cause]%''</descr>
   <logmsg dest="logndisplay">A flow (protocol ''%parm[protocol]%'') from exporter ''%interface%'' in location ''%parm[monitoringSystemLocation]%'' was detected and dropped by ''%parm[monitoringSystemId]%'' due to the following reason: ''%parm[cause]%''.</logmsg>
   <severity>Warning</severity>
</event>','Warning', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (81, 13, 'uei.opennms.org/internal/topology/linkDown', 'OpenNMS-defined topology event: linkDown', '<p>node: %nodeid% with ifindex: %ifindex% is down </p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/internal/topology/linkDown</uei>
   <event-label>OpenNMS-defined topology event: linkDown</event-label>
   <descr>&lt;p>node: %nodeid% with ifindex: %ifindex% is down &lt;/p></descr>
   <logmsg dest="donotpersist">
            node: %nodeid% with ifindex: %ifindex% is down
        </logmsg>
   <severity>Minor</severity>
   <alarm-data reduction-key="%uei%:%nodeid%:%ifindex%" alarm-type="1" auto-clean="false"/>
</event>','Minor', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (82, 13, 'uei.opennms.org/internal/topology/linkUp', 'OpenNMS-defined topology event: linkUp', '<p>node: %nodeid% with ifindex: %ifindex% is up </p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/internal/topology/linkUp</uei>
   <event-label>OpenNMS-defined topology event: linkUp</event-label>
   <descr>&lt;p>node: %nodeid% with ifindex: %ifindex% is up &lt;/p></descr>
   <logmsg dest="donotpersist">
            node: %nodeid% with ifindex: %ifindex% is up
        </logmsg>
   <severity>Normal</severity>
   <alarm-data reduction-key="%uei%:%nodeid%:%ifindex%" alarm-type="2" clear-key="uei.opennms.org/internal/topology/linkDown:%nodeid%:%ifindex%" auto-clean="false"/>
</event>','Normal', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (83, 14, 'uei.opennms.org/traps/eventTrap', 'OPENNMS-MIB defined trap event: eventTrap', '<p>This is the definition of the generic OpenNMS trap sent from the
            scriptd process. Key variables are uei (which tells what type
            of OpenNMS event this was), interface (the IP address of the interface
            that caused the event) and severity.</p><table>
            <tr><td><b>

            dbid</b></td><td>
            %parm[#1]%;</td><td><p></p></td></tr>
            <tr><td><b>

            distPoller</b></td><td>
            %parm[#2]%;</td><td><p></p></td></tr>
            <tr><td><b>

            create-time</b></td><td>
            %parm[#3]%;</td><td><p></p></td></tr>
            <tr><td><b>

            master-station</b></td><td>
            %parm[#4]%;</td><td><p></p></td></tr>
            <tr><td><b>

            uei</b></td><td>
            %parm[#5]%;</td><td><p></p></td></tr>
            <tr><td><b>

            source</b></td><td>
            %parm[#6]%;</td><td><p></p></td></tr>
            <tr><td><b>

            nodeid</b></td><td>
            %parm[#7]%;</td><td><p></p></td></tr>
            <tr><td><b>

            time</b></td><td>
            %parm[#8]%;</td><td><p></p></td></tr>
            <tr><td><b>

            host</b></td><td>
            %parm[#9]%;</td><td><p></p></td></tr>
            <tr><td><b>

            interface</b></td><td>
            %parm[#10]%;</td><td><p></p></td></tr>
            <tr><td><b>

            snmphost</b></td><td>
            %parm[#11]%;</td><td><p></p></td></tr>
            <tr><td><b>

            service</b></td><td>
            %parm[#12]%;</td><td><p></p></td></tr>
            <tr><td><b>

            descr</b></td><td>
            %parm[#13]%;</td><td><p></p></td></tr>
            <tr><td><b>

            logmsg</b></td><td>
            %parm[#14]%;</td><td><p></p></td></tr>
            <tr><td><b>

            severity</b></td><td>
            %parm[#15]%;</td><td><p></p></td></tr>
            <tr><td><b>

            pathoutage</b></td><td>
            %parm[#16]%;</td><td><p></p></td></tr>
            <tr><td><b>

            operinst</b></td><td>
            %parm[#17]%;</td><td><p></p></td></tr>
            <tr><td><b>

            ifresolve</b></td><td>
            %parm[#18]%;</td><td><p></p></td></tr>
            <tr><td><b>

            nodelabel</b></td><td>
            %parm[#19]%;</td><td><p></p></td></tr></table>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <mask>
      <maskelement>
         <mename>id</mename>
         <mevalue>.1.3.6.1.4.1.5813.1</mevalue>
      </maskelement>
      <maskelement>
         <mename>generic</mename>
         <mevalue>6</mevalue>
      </maskelement>
      <maskelement>
         <mename>specific</mename>
         <mevalue>1</mevalue>
      </maskelement>
   </mask>
   <uei>uei.opennms.org/traps/eventTrap</uei>
   <event-label>OPENNMS-MIB defined trap event: eventTrap</event-label>
   <descr>&lt;p>This is the definition of the generic OpenNMS trap sent from the
            scriptd process. Key variables are uei (which tells what type
            of OpenNMS event this was), interface (the IP address of the interface
            that caused the event) and severity.&lt;/p>&lt;table>
            &lt;tr>&lt;td>&lt;b>

            dbid&lt;/b>&lt;/td>&lt;td>
            %parm[#1]%;&lt;/td>&lt;td>&lt;p>&lt;/p>&lt;/td>&lt;/tr>
            &lt;tr>&lt;td>&lt;b>

            distPoller&lt;/b>&lt;/td>&lt;td>
            %parm[#2]%;&lt;/td>&lt;td>&lt;p>&lt;/p>&lt;/td>&lt;/tr>
            &lt;tr>&lt;td>&lt;b>

            create-time&lt;/b>&lt;/td>&lt;td>
            %parm[#3]%;&lt;/td>&lt;td>&lt;p>&lt;/p>&lt;/td>&lt;/tr>
            &lt;tr>&lt;td>&lt;b>

            master-station&lt;/b>&lt;/td>&lt;td>
            %parm[#4]%;&lt;/td>&lt;td>&lt;p>&lt;/p>&lt;/td>&lt;/tr>
            &lt;tr>&lt;td>&lt;b>

            uei&lt;/b>&lt;/td>&lt;td>
            %parm[#5]%;&lt;/td>&lt;td>&lt;p>&lt;/p>&lt;/td>&lt;/tr>
            &lt;tr>&lt;td>&lt;b>

            source&lt;/b>&lt;/td>&lt;td>
            %parm[#6]%;&lt;/td>&lt;td>&lt;p>&lt;/p>&lt;/td>&lt;/tr>
            &lt;tr>&lt;td>&lt;b>

            nodeid&lt;/b>&lt;/td>&lt;td>
            %parm[#7]%;&lt;/td>&lt;td>&lt;p>&lt;/p>&lt;/td>&lt;/tr>
            &lt;tr>&lt;td>&lt;b>

            time&lt;/b>&lt;/td>&lt;td>
            %parm[#8]%;&lt;/td>&lt;td>&lt;p>&lt;/p>&lt;/td>&lt;/tr>
            &lt;tr>&lt;td>&lt;b>

            host&lt;/b>&lt;/td>&lt;td>
            %parm[#9]%;&lt;/td>&lt;td>&lt;p>&lt;/p>&lt;/td>&lt;/tr>
            &lt;tr>&lt;td>&lt;b>

            interface&lt;/b>&lt;/td>&lt;td>
            %parm[#10]%;&lt;/td>&lt;td>&lt;p>&lt;/p>&lt;/td>&lt;/tr>
            &lt;tr>&lt;td>&lt;b>

            snmphost&lt;/b>&lt;/td>&lt;td>
            %parm[#11]%;&lt;/td>&lt;td>&lt;p>&lt;/p>&lt;/td>&lt;/tr>
            &lt;tr>&lt;td>&lt;b>

            service&lt;/b>&lt;/td>&lt;td>
            %parm[#12]%;&lt;/td>&lt;td>&lt;p>&lt;/p>&lt;/td>&lt;/tr>
            &lt;tr>&lt;td>&lt;b>

            descr&lt;/b>&lt;/td>&lt;td>
            %parm[#13]%;&lt;/td>&lt;td>&lt;p>&lt;/p>&lt;/td>&lt;/tr>
            &lt;tr>&lt;td>&lt;b>

            logmsg&lt;/b>&lt;/td>&lt;td>
            %parm[#14]%;&lt;/td>&lt;td>&lt;p>&lt;/p>&lt;/td>&lt;/tr>
            &lt;tr>&lt;td>&lt;b>

            severity&lt;/b>&lt;/td>&lt;td>
            %parm[#15]%;&lt;/td>&lt;td>&lt;p>&lt;/p>&lt;/td>&lt;/tr>
            &lt;tr>&lt;td>&lt;b>

            pathoutage&lt;/b>&lt;/td>&lt;td>
            %parm[#16]%;&lt;/td>&lt;td>&lt;p>&lt;/p>&lt;/td>&lt;/tr>
            &lt;tr>&lt;td>&lt;b>

            operinst&lt;/b>&lt;/td>&lt;td>
            %parm[#17]%;&lt;/td>&lt;td>&lt;p>&lt;/p>&lt;/td>&lt;/tr>
            &lt;tr>&lt;td>&lt;b>

            ifresolve&lt;/b>&lt;/td>&lt;td>
            %parm[#18]%;&lt;/td>&lt;td>&lt;p>&lt;/p>&lt;/td>&lt;/tr>
            &lt;tr>&lt;td>&lt;b>

            nodelabel&lt;/b>&lt;/td>&lt;td>
            %parm[#19]%;&lt;/td>&lt;td>&lt;p>&lt;/p>&lt;/td>&lt;/tr>&lt;/table></descr>
   <logmsg dest="logndisplay">
            &lt;p>An OpenNMS Event has been received as an SNMP Trap
            with UEI: %parm[#5]%.&lt;/p>
        </logmsg>
   <severity>Normal</severity>
</event>','Normal', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (84, 14, 'uei.opennms.org/traps/tl1AutonomousMessageTrap', 'OPENNMS-MIB defined trap event: tl1AutonomousMessageTrap', '<p>This trap is used to convey the contents of a TL1 autonomous message
            received from a TL1 NE or a north-bound TL1 EMS. Managers receiving
            this trap may need to perform additional analysis of its varbinds in
            order to realize value from this trap.</p><table>
            <tr><td><b>

            nodeid</b></td><td>
            %parm[#1]%;</td><td><p></p></td></tr>
            <tr><td><b>

            time</b></td><td>
            %parm[#2]%;</td><td><p></p></td></tr>
            <tr><td><b>

            host</b></td><td>
            %parm[#3]%;</td><td><p></p></td></tr>
            <tr><td><b>

            interface</b></td><td>
            %parm[#4]%;</td><td><p></p></td></tr>
            <tr><td><b>

            service</b></td><td>
            %parm[#5]%;</td><td><p></p></td></tr>
            <tr><td><b>

            severity</b></td><td>
            %parm[#6]%;</td><td><p></p></td></tr>
            <tr><td><b>

            tl1amRawMessage</b></td><td>
            %parm[#7]%;</td><td><p></p></td></tr>
            <tr><td><b>

            tl1amAlarmCode</b></td><td>
            %parm[#8]%;</td><td><p></p></td></tr>
            <tr><td><b>

            tl1amAutonomousTag</b></td><td>
            %parm[#9]%;</td><td><p></p></td></tr>
            <tr><td><b>

            tl1amVerb</b></td><td>
            %parm[#10]%;</td><td><p></p></td></tr>
            <tr><td><b>

            tl1amAutoBlock</b></td><td>
            %parm[#11]%;</td><td><p></p></td></tr>
            <tr><td><b>

            tl1amAID</b></td><td>
            %parm[#12]%;</td><td><p></p></td></tr>
            <tr><td><b>

            tl1amAdditionalParams</b></td><td>
            %parm[#13]%;</td><td><p></p></td></tr></table>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <mask>
      <maskelement>
         <mename>id</mename>
         <mevalue>.1.3.6.1.4.1.5813.1</mevalue>
      </maskelement>
      <maskelement>
         <mename>generic</mename>
         <mevalue>6</mevalue>
      </maskelement>
      <maskelement>
         <mename>specific</mename>
         <mevalue>2</mevalue>
      </maskelement>
   </mask>
   <uei>uei.opennms.org/traps/tl1AutonomousMessageTrap</uei>
   <event-label>OPENNMS-MIB defined trap event: tl1AutonomousMessageTrap</event-label>
   <descr>&lt;p>This trap is used to convey the contents of a TL1 autonomous message
            received from a TL1 NE or a north-bound TL1 EMS. Managers receiving
            this trap may need to perform additional analysis of its varbinds in
            order to realize value from this trap.&lt;/p>&lt;table>
            &lt;tr>&lt;td>&lt;b>

            nodeid&lt;/b>&lt;/td>&lt;td>
            %parm[#1]%;&lt;/td>&lt;td>&lt;p>&lt;/p>&lt;/td>&lt;/tr>
            &lt;tr>&lt;td>&lt;b>

            time&lt;/b>&lt;/td>&lt;td>
            %parm[#2]%;&lt;/td>&lt;td>&lt;p>&lt;/p>&lt;/td>&lt;/tr>
            &lt;tr>&lt;td>&lt;b>

            host&lt;/b>&lt;/td>&lt;td>
            %parm[#3]%;&lt;/td>&lt;td>&lt;p>&lt;/p>&lt;/td>&lt;/tr>
            &lt;tr>&lt;td>&lt;b>

            interface&lt;/b>&lt;/td>&lt;td>
            %parm[#4]%;&lt;/td>&lt;td>&lt;p>&lt;/p>&lt;/td>&lt;/tr>
            &lt;tr>&lt;td>&lt;b>

            service&lt;/b>&lt;/td>&lt;td>
            %parm[#5]%;&lt;/td>&lt;td>&lt;p>&lt;/p>&lt;/td>&lt;/tr>
            &lt;tr>&lt;td>&lt;b>

            severity&lt;/b>&lt;/td>&lt;td>
            %parm[#6]%;&lt;/td>&lt;td>&lt;p>&lt;/p>&lt;/td>&lt;/tr>
            &lt;tr>&lt;td>&lt;b>

            tl1amRawMessage&lt;/b>&lt;/td>&lt;td>
            %parm[#7]%;&lt;/td>&lt;td>&lt;p>&lt;/p>&lt;/td>&lt;/tr>
            &lt;tr>&lt;td>&lt;b>

            tl1amAlarmCode&lt;/b>&lt;/td>&lt;td>
            %parm[#8]%;&lt;/td>&lt;td>&lt;p>&lt;/p>&lt;/td>&lt;/tr>
            &lt;tr>&lt;td>&lt;b>

            tl1amAutonomousTag&lt;/b>&lt;/td>&lt;td>
            %parm[#9]%;&lt;/td>&lt;td>&lt;p>&lt;/p>&lt;/td>&lt;/tr>
            &lt;tr>&lt;td>&lt;b>

            tl1amVerb&lt;/b>&lt;/td>&lt;td>
            %parm[#10]%;&lt;/td>&lt;td>&lt;p>&lt;/p>&lt;/td>&lt;/tr>
            &lt;tr>&lt;td>&lt;b>

            tl1amAutoBlock&lt;/b>&lt;/td>&lt;td>
            %parm[#11]%;&lt;/td>&lt;td>&lt;p>&lt;/p>&lt;/td>&lt;/tr>
            &lt;tr>&lt;td>&lt;b>

            tl1amAID&lt;/b>&lt;/td>&lt;td>
            %parm[#12]%;&lt;/td>&lt;td>&lt;p>&lt;/p>&lt;/td>&lt;/tr>
            &lt;tr>&lt;td>&lt;b>

            tl1amAdditionalParams&lt;/b>&lt;/td>&lt;td>
            %parm[#13]%;&lt;/td>&lt;td>&lt;p>&lt;/p>&lt;/td>&lt;/tr>&lt;/table></descr>
   <logmsg dest="logndisplay">&lt;p>
            tl1AutonomousMessageTrap trap received
            nodeid=%parm[#1]%
            time=%parm[#2]%
            host=%parm[#3]%
            interface=%parm[#4]%
            service=%parm[#5]%
            severity=%parm[#6]%
            tl1amRawMessage=%parm[#7]%
            tl1amAlarmCode=%parm[#8]%
            tl1amAutonomousTag=%parm[#9]%
            tl1amVerb=%parm[#10]%
            tl1amAutoBlock=%parm[#11]%
            tl1amAID=%parm[#12]%
            tl1amAdditionalParams=%parm[#13]%&lt;/p>
        </logmsg>
   <severity>Normal</severity>
</event>','Normal', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (85, 14, 'uei.opennms.org/traps/alarmTrap', 'OPENNMS-MIB defined trap event: alarmTrap', '<p>The OpenNMS alarm SNMP Trap</p><table>
            <tr><td><b>

            dbid</b></td><td>
            %parm[#1]%;</td><td><p></p></td></tr>
            <tr><td><b>

            distPoller</b></td><td>
            %parm[#2]%;</td><td><p></p></td></tr>
            <tr><td><b>

            create-time</b></td><td>
            %parm[#3]%;</td><td><p></p></td></tr>
            <tr><td><b>

            master-station</b></td><td>
            %parm[#4]%;</td><td><p></p></td></tr>
            <tr><td><b>

            uei</b></td><td>
            %parm[#5]%;</td><td><p></p></td></tr>
            <tr><td><b>

            source</b></td><td>
            %parm[#6]%;</td><td><p></p></td></tr>
            <tr><td><b>

            nodeid</b></td><td>
            %parm[#7]%;</td><td><p></p></td></tr>
            <tr><td><b>

            time</b></td><td>
            %parm[#8]%;</td><td><p></p></td></tr>
            <tr><td><b>

            host</b></td><td>
            %parm[#9]%;</td><td><p></p></td></tr>
            <tr><td><b>

            interface</b></td><td>
            %parm[#10]%;</td><td><p></p></td></tr>
            <tr><td><b>

            snmphost</b></td><td>
            %parm[#11]%;</td><td><p></p></td></tr>
            <tr><td><b>

            service</b></td><td>
            %parm[#12]%;</td><td><p></p></td></tr>
            <tr><td><b>

            descr</b></td><td>
            %parm[#13]%;</td><td><p></p></td></tr>
            <tr><td><b>

            logmsg</b></td><td>
            %parm[#14]%;</td><td><p></p></td></tr>
            <tr><td><b>

            severity</b></td><td>
            %parm[#15]%;</td><td><p></p></td></tr>
            <tr><td><b>

            pathoutage</b></td><td>
            %parm[#16]%;</td><td><p></p></td></tr>
            <tr><td><b>

            operinst</b></td><td>
            %parm[#17]%;</td><td><p></p></td></tr>
            <tr><td><b>

            ifresolve</b></td><td>
            %parm[#18]%;</td><td><p></p></td></tr>
            <tr><td><b>

            nodelabel</b></td><td>
            %parm[#19]%;</td><td><p></p></td></tr>
            <tr><td><b>

            alarmId</b></td><td>
            %parm[#20]%;</td><td><p></p></td></tr>
            <tr><td><b>

            synchronizing</b></td><td>
            %parm[#21]%;</td><td><p></p></td></tr></table>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <mask>
      <maskelement>
         <mename>id</mename>
         <mevalue>.1.3.6.1.4.1.5813.1</mevalue>
      </maskelement>
      <maskelement>
         <mename>generic</mename>
         <mevalue>6</mevalue>
      </maskelement>
      <maskelement>
         <mename>specific</mename>
         <mevalue>3</mevalue>
      </maskelement>
   </mask>
   <uei>uei.opennms.org/traps/alarmTrap</uei>
   <event-label>OPENNMS-MIB defined trap event: alarmTrap</event-label>
   <descr>&lt;p>The OpenNMS alarm SNMP Trap&lt;/p>&lt;table>
            &lt;tr>&lt;td>&lt;b>

            dbid&lt;/b>&lt;/td>&lt;td>
            %parm[#1]%;&lt;/td>&lt;td>&lt;p>&lt;/p>&lt;/td>&lt;/tr>
            &lt;tr>&lt;td>&lt;b>

            distPoller&lt;/b>&lt;/td>&lt;td>
            %parm[#2]%;&lt;/td>&lt;td>&lt;p>&lt;/p>&lt;/td>&lt;/tr>
            &lt;tr>&lt;td>&lt;b>

            create-time&lt;/b>&lt;/td>&lt;td>
            %parm[#3]%;&lt;/td>&lt;td>&lt;p>&lt;/p>&lt;/td>&lt;/tr>
            &lt;tr>&lt;td>&lt;b>

            master-station&lt;/b>&lt;/td>&lt;td>
            %parm[#4]%;&lt;/td>&lt;td>&lt;p>&lt;/p>&lt;/td>&lt;/tr>
            &lt;tr>&lt;td>&lt;b>

            uei&lt;/b>&lt;/td>&lt;td>
            %parm[#5]%;&lt;/td>&lt;td>&lt;p>&lt;/p>&lt;/td>&lt;/tr>
            &lt;tr>&lt;td>&lt;b>

            source&lt;/b>&lt;/td>&lt;td>
            %parm[#6]%;&lt;/td>&lt;td>&lt;p>&lt;/p>&lt;/td>&lt;/tr>
            &lt;tr>&lt;td>&lt;b>

            nodeid&lt;/b>&lt;/td>&lt;td>
            %parm[#7]%;&lt;/td>&lt;td>&lt;p>&lt;/p>&lt;/td>&lt;/tr>
            &lt;tr>&lt;td>&lt;b>

            time&lt;/b>&lt;/td>&lt;td>
            %parm[#8]%;&lt;/td>&lt;td>&lt;p>&lt;/p>&lt;/td>&lt;/tr>
            &lt;tr>&lt;td>&lt;b>

            host&lt;/b>&lt;/td>&lt;td>
            %parm[#9]%;&lt;/td>&lt;td>&lt;p>&lt;/p>&lt;/td>&lt;/tr>
            &lt;tr>&lt;td>&lt;b>

            interface&lt;/b>&lt;/td>&lt;td>
            %parm[#10]%;&lt;/td>&lt;td>&lt;p>&lt;/p>&lt;/td>&lt;/tr>
            &lt;tr>&lt;td>&lt;b>

            snmphost&lt;/b>&lt;/td>&lt;td>
            %parm[#11]%;&lt;/td>&lt;td>&lt;p>&lt;/p>&lt;/td>&lt;/tr>
            &lt;tr>&lt;td>&lt;b>

            service&lt;/b>&lt;/td>&lt;td>
            %parm[#12]%;&lt;/td>&lt;td>&lt;p>&lt;/p>&lt;/td>&lt;/tr>
            &lt;tr>&lt;td>&lt;b>

            descr&lt;/b>&lt;/td>&lt;td>
            %parm[#13]%;&lt;/td>&lt;td>&lt;p>&lt;/p>&lt;/td>&lt;/tr>
            &lt;tr>&lt;td>&lt;b>

            logmsg&lt;/b>&lt;/td>&lt;td>
            %parm[#14]%;&lt;/td>&lt;td>&lt;p>&lt;/p>&lt;/td>&lt;/tr>
            &lt;tr>&lt;td>&lt;b>

            severity&lt;/b>&lt;/td>&lt;td>
            %parm[#15]%;&lt;/td>&lt;td>&lt;p>&lt;/p>&lt;/td>&lt;/tr>
            &lt;tr>&lt;td>&lt;b>

            pathoutage&lt;/b>&lt;/td>&lt;td>
            %parm[#16]%;&lt;/td>&lt;td>&lt;p>&lt;/p>&lt;/td>&lt;/tr>
            &lt;tr>&lt;td>&lt;b>

            operinst&lt;/b>&lt;/td>&lt;td>
            %parm[#17]%;&lt;/td>&lt;td>&lt;p>&lt;/p>&lt;/td>&lt;/tr>
            &lt;tr>&lt;td>&lt;b>

            ifresolve&lt;/b>&lt;/td>&lt;td>
            %parm[#18]%;&lt;/td>&lt;td>&lt;p>&lt;/p>&lt;/td>&lt;/tr>
            &lt;tr>&lt;td>&lt;b>

            nodelabel&lt;/b>&lt;/td>&lt;td>
            %parm[#19]%;&lt;/td>&lt;td>&lt;p>&lt;/p>&lt;/td>&lt;/tr>
            &lt;tr>&lt;td>&lt;b>

            alarmId&lt;/b>&lt;/td>&lt;td>
            %parm[#20]%;&lt;/td>&lt;td>&lt;p>&lt;/p>&lt;/td>&lt;/tr>
            &lt;tr>&lt;td>&lt;b>

            synchronizing&lt;/b>&lt;/td>&lt;td>
            %parm[#21]%;&lt;/td>&lt;td>&lt;p>&lt;/p>&lt;/td>&lt;/tr>&lt;/table></descr>
   <logmsg dest="logndisplay">
            &lt;p>An OpenNMS Event has been received as an SNMP Trap
            with UEI: %parm[#5]%.&lt;/p>
        </logmsg>
   <severity>Normal</severity>
</event>','Normal', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (86, 14, 'uei.opennms.org/traps/heartbeatTrap', 'OPENNMS-MIB defined trap event: heartbeatTrap', '<p>Trap sent periodically by OpenNMS to keep alive external SNMP Manager</p><table></table>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <mask>
      <maskelement>
         <mename>id</mename>
         <mevalue>.1.3.6.1.4.1.5813.1</mevalue>
      </maskelement>
      <maskelement>
         <mename>generic</mename>
         <mevalue>6</mevalue>
      </maskelement>
      <maskelement>
         <mename>specific</mename>
         <mevalue>4</mevalue>
      </maskelement>
   </mask>
   <uei>uei.opennms.org/traps/heartbeatTrap</uei>
   <event-label>OPENNMS-MIB defined trap event: heartbeatTrap</event-label>
   <descr>&lt;p>Trap sent periodically by OpenNMS to keep alive external SNMP Manager&lt;/p>&lt;table>&lt;/table></descr>
   <logmsg dest="logndisplay">&lt;p>
            heartbeatTrap trap received&lt;/p>
        </logmsg>
   <severity>Normal</severity>
</event>','Normal', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (87, 14, 'uei.opennms.org/traps/startSyncTrap', 'OPENNMS-MIB defined trap event: startSyncTrap', '<p>OpenNMS Synchronization Process is starting</p><table></table>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <mask>
      <maskelement>
         <mename>id</mename>
         <mevalue>.1.3.6.1.4.1.5813.1</mevalue>
      </maskelement>
      <maskelement>
         <mename>generic</mename>
         <mevalue>6</mevalue>
      </maskelement>
      <maskelement>
         <mename>specific</mename>
         <mevalue>5</mevalue>
      </maskelement>
   </mask>
   <uei>uei.opennms.org/traps/startSyncTrap</uei>
   <event-label>OPENNMS-MIB defined trap event: startSyncTrap</event-label>
   <descr>&lt;p>OpenNMS Synchronization Process is starting&lt;/p>&lt;table>&lt;/table></descr>
   <logmsg dest="logndisplay">&lt;p>
            startSyncTrap trap received&lt;/p>
        </logmsg>
   <severity>Normal</severity>
</event>','Normal', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (88, 14, 'uei.opennms.org/traps/endSyncTrap', 'OPENNMS-MIB defined trap event: endSyncTrap', '<p>OpenNMS Synchronization Process is successfully ended</p><table></table>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <mask>
      <maskelement>
         <mename>id</mename>
         <mevalue>.1.3.6.1.4.1.5813.1</mevalue>
      </maskelement>
      <maskelement>
         <mename>generic</mename>
         <mevalue>6</mevalue>
      </maskelement>
      <maskelement>
         <mename>specific</mename>
         <mevalue>6</mevalue>
      </maskelement>
   </mask>
   <uei>uei.opennms.org/traps/endSyncTrap</uei>
   <event-label>OPENNMS-MIB defined trap event: endSyncTrap</event-label>
   <descr>&lt;p>OpenNMS Synchronization Process is successfully ended&lt;/p>&lt;table>&lt;/table></descr>
   <logmsg dest="logndisplay">&lt;p>
            endSyncTrap trap received&lt;/p>
        </logmsg>
   <severity>Normal</severity>
</event>','Normal', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (89, 14, 'uei.opennms.org/traps/syncRequestTrap', 'OPENNMS-MIB defined trap event: syncRequestTrap', '<p>OpenNMS synchronization request</p><table></table>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <mask>
      <maskelement>
         <mename>id</mename>
         <mevalue>.1.3.6.1.4.1.5813.1</mevalue>
      </maskelement>
      <maskelement>
         <mename>generic</mename>
         <mevalue>6</mevalue>
      </maskelement>
      <maskelement>
         <mename>specific</mename>
         <mevalue>7</mevalue>
      </maskelement>
   </mask>
   <uei>uei.opennms.org/traps/syncRequestTrap</uei>
   <event-label>OPENNMS-MIB defined trap event: syncRequestTrap</event-label>
   <descr>&lt;p>OpenNMS synchronization request&lt;/p>&lt;table>&lt;/table></descr>
   <logmsg dest="logndisplay">&lt;p>
            syncRequestTrap trap received&lt;/p>
        </logmsg>
   <severity>Normal</severity>
</event>','Normal', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (90, 15, 'uei.opennms.org/internal/poller/outageCreated', 'OpenNMS-defined node event: outageCreated', '<p>A %service% outage was created on interface
            %interface% because of the following condition: %parm[eventReason]%.</p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/internal/poller/outageCreated</uei>
   <event-label>OpenNMS-defined node event: outageCreated</event-label>
   <descr>&lt;p>A %service% outage was created on interface
            %interface% because of the following condition: %parm[eventReason]%.&lt;/p></descr>
   <logmsg dest="donotpersist">
            %service% outage identified on interface %interface%.
        </logmsg>
   <severity>Normal</severity>
</event>','Normal', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (91, 15, 'uei.opennms.org/internal/poller/outageResolved', 'OpenNMS-defined node event: outageResolved', '<p>The %service% service outage on interface %interface%
            has been restored.</p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/internal/poller/outageResolved</uei>
   <event-label>OpenNMS-defined node event: outageResolved</event-label>
   <descr>&lt;p>The %service% service outage on interface %interface%
            has been restored.&lt;/p></descr>
   <logmsg dest="donotpersist">
            The %service% outage on interface %interface% has been
            resolved.
        </logmsg>
   <severity>Normal</severity>
</event>','Normal', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (92, 15, 'uei.opennms.org/internal/poller/suspendPollingService', 'OpenNMS-defined poller event: suspendPollingService', '<p>A forced rescan has identified the %service% service
            on interface %interface% as no longer part of any poller package,
            or the service has been unmanaged.
            </p> Polling will be discontinued.</p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/internal/poller/suspendPollingService</uei>
   <event-label>OpenNMS-defined poller event: suspendPollingService</event-label>
   <descr>&lt;p>A forced rescan has identified the %service% service
            on interface %interface% as no longer part of any poller package,
            or the service has been unmanaged.
            &lt;/p> Polling will be discontinued.&lt;/p></descr>
   <logmsg dest="logndisplay">
            Polling will be discontinued for %service% service on interface
            %interface%.
        </logmsg>
   <severity>Normal</severity>
</event>','Normal', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (93, 15, 'uei.opennms.org/internal/poller/resumePollingService', 'OpenNMS-defined poller event: resumePollingService', '<p>A forced rescan has identified the %service% service
            on interface %interface% as covered by a poller package, and
            managed.
            </p> Polling will begin in accordance with the package and
            any applicable outage calendar.</p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/internal/poller/resumePollingService</uei>
   <event-label>OpenNMS-defined poller event: resumePollingService</event-label>
   <descr>&lt;p>A forced rescan has identified the %service% service
            on interface %interface% as covered by a poller package, and
            managed.
            &lt;/p> Polling will begin in accordance with the package and
            any applicable outage calendar.&lt;/p></descr>
   <logmsg dest="logndisplay">
            Polling will begin/resume for %service% service on interface
            %interface%.
        </logmsg>
   <severity>Normal</severity>
</event>','Normal', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (94, 15, 'uei.opennms.org/nodes/serviceUnmanaged', 'OpenNMS-defined internal event: serviceUnmanaged', '<p>The service %service% on interface %interface% is
            being forcibly unmanaged.</p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/nodes/serviceUnmanaged</uei>
   <event-label>OpenNMS-defined internal event: serviceUnmanaged</event-label>
   <descr>&lt;p>The service %service% on interface %interface% is
            being forcibly unmanaged.&lt;/p></descr>
   <logmsg dest="logndisplay">
            The service %service% on interface %interface% is being
            forcibly unmanaged.
        </logmsg>
   <severity>Warning</severity>
</event>','Warning', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (95, 15, 'uei.opennms.org/nodes/deleteService', 'OpenNMS-defined node event: deleteService', '<p>Due to excessive downtime, the %service% service on
            interface %interface% has been scheduled for
            deletion.</p> <p>When a service has been down
            for one week, it is determined to have been removed and will
            be deleted. If the service is later rediscovered, it will be
            re-added and associated with the appropriate
            interface.</p> <p>If this is the only service
            associated with an interface, the interface will be
            scheduled for deletion as well, with the generation of the
            deleteInterface event.</p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/nodes/deleteService</uei>
   <event-label>OpenNMS-defined node event: deleteService</event-label>
   <descr>&lt;p>Due to excessive downtime, the %service% service on
            interface %interface% has been scheduled for
            deletion.&lt;/p> &lt;p>When a service has been down
            for one week, it is determined to have been removed and will
            be deleted. If the service is later rediscovered, it will be
            re-added and associated with the appropriate
            interface.&lt;/p> &lt;p>If this is the only service
            associated with an interface, the interface will be
            scheduled for deletion as well, with the generation of the
            deleteInterface event.&lt;/p></descr>
   <logmsg dest="logndisplay">
            The %service% service on interface %interface% has been
            scheduled for deletion.
        </logmsg>
   <severity>Warning</severity>
</event>','Warning', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (96, 15, 'uei.opennms.org/nodes/duplicateNodeDeleted', 'OpenNMS-defined node event: duplicateNodeDeleted', '<p>Node :%nodeid% labled: %nodelabel%; was determined to be a
            duplicate node and is has been deleted.</p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/nodes/duplicateNodeDeleted</uei>
   <event-label>OpenNMS-defined node event: duplicateNodeDeleted</event-label>
   <descr>&lt;p>Node :%nodeid% labled: %nodelabel%; was determined to be a
            duplicate node and is has been deleted.&lt;/p></descr>
   <logmsg dest="logndisplay">
            &lt;p>Node #&lt;a
            href=&quot;element/node.jsp?node=%nodeid%&quot;>%nodeid%&lt;/a>
            was determined to be a duplicate node and is being flagged
            for deletion.&lt;/p>
        </logmsg>
   <severity>Minor</severity>
</event>','Minor', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (97, 15, 'uei.opennms.org/nodes/interfaceDeleted', 'OpenNMS-defined node event: interfaceDeleted', '<p>Interface %interface% deleted from node #<a
            href="element/node.jsp?node=%nodeid%">
            %nodeid%</a> with ifIndex %ifindex%.</p> <p>This event is
            generated following an extended outage for a service, in
            which that service is the only service associated with an
            interface. If the service is later rediscovered, a new
            interface will be added and the service will be associated
            with that new interface.</p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/nodes/interfaceDeleted</uei>
   <event-label>OpenNMS-defined node event: interfaceDeleted</event-label>
   <descr>&lt;p>Interface %interface% deleted from node #&lt;a
            href=&quot;element/node.jsp?node=%nodeid%&quot;>
            %nodeid%&lt;/a> with ifIndex %ifindex%.&lt;/p> &lt;p>This event is
            generated following an extended outage for a service, in
            which that service is the only service associated with an
            interface. If the service is later rediscovered, a new
            interface will be added and the service will be associated
            with that new interface.&lt;/p></descr>
   <logmsg dest="logndisplay">
            Interface %interface% deleted from node #&lt;a
            href=&quot;element/node.jsp?node=%nodeid%&quot;>%nodeid%&lt;/a>
            with ifIndex %ifindex%.
        </logmsg>
   <severity>Minor</severity>
</event>','Minor', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (98, 15, 'uei.opennms.org/nodes/interfaceDown', 'OpenNMS-defined node event: interfaceDown', '<p>All services are down on interface %interface%
            </p> <p>This event is generated when node outage
            processing determines that the critical service or all
            services on the interface are now down </p> <p>
            New outage records have been created and service level
            availability calculations will be impacted until this outage
            is resolved.</p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/nodes/interfaceDown</uei>
   <event-label>OpenNMS-defined node event: interfaceDown</event-label>
   <descr>&lt;p>All services are down on interface %interface%
            &lt;/p> &lt;p>This event is generated when node outage
            processing determines that the critical service or all
            services on the interface are now down &lt;/p> &lt;p>
            New outage records have been created and service level
            availability calculations will be impacted until this outage
            is resolved.&lt;/p></descr>
   <logmsg dest="logndisplay">
            Interface %interface% is down.
        </logmsg>
   <severity>Minor</severity>
   <alarm-data reduction-key="%uei%:%dpname%:%nodeid%:%interface%" alarm-type="1" auto-clean="false"/>
</event>','Minor', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (99, 15, 'uei.opennms.org/nodes/snmp/interfaceOperDown', 'OpenNMS-defined node event: snmp interface Oper Status Down', '<p>The operational status of interface is down
            </p> <p>This event is generated when an snmp poll on interface find the operational status down.
            </p>
            <p>Params %parm[all]% </p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/nodes/snmp/interfaceOperDown</uei>
   <event-label>OpenNMS-defined node event: snmp interface Oper Status Down</event-label>
   <descr>&lt;p>The operational status of interface is down
            &lt;/p> &lt;p>This event is generated when an snmp poll on interface find the operational status down.
            &lt;/p>
            &lt;p>Params %parm[all]% &lt;/p></descr>
   <logmsg dest="logndisplay">Operational status Down on interface ifname:%parm[snmpifname]%
            ifindex:%parm[snmpifindex]% ifdescr:%parm[snmpifdescr]%
        </logmsg>
   <severity>Minor</severity>
   <alarm-data reduction-key="%uei%:%dpname%:%nodeid%:%parm[snmpifindex]%" alarm-type="1" auto-clean="false">
      <update-field field-name="severity" update-on-reduction="true"/>
   </alarm-data>
</event>','Minor', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (100, 15, 'uei.opennms.org/nodes/snmp/interfaceOperTesting', 'OpenNMS-defined node event: snmp interface Oper Status Testing', '<p>The operational status of interface is testing
            </p> <p>This event is generated when an snmp poll on interface find the operational status testing.
            </p><p>The testing state indicates that some tests must be performed on the interface. Once completed
            the state may change to up, dormant, or down, as appropriate.</p>
            <p>Params %parm[all]% </p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/nodes/snmp/interfaceOperTesting</uei>
   <event-label>OpenNMS-defined node event: snmp interface Oper Status Testing</event-label>
   <descr>&lt;p>The operational status of interface is testing
            &lt;/p> &lt;p>This event is generated when an snmp poll on interface find the operational status testing.
            &lt;/p>&lt;p>The testing state indicates that some tests must be performed on the interface. Once completed
            the state may change to up, dormant, or down, as appropriate.&lt;/p>
            &lt;p>Params %parm[all]% &lt;/p></descr>
   <logmsg dest="logndisplay">Operational status Testing on interface ifname:%parm[snmpifname]%
            ifindex:%parm[snmpifindex]% ifdescr:%parm[snmpifdescr]%
        </logmsg>
   <severity>Warning</severity>
   <alarm-data reduction-key="uei.opennms.org/nodes/snmp/interfaceOperDown:%dpname%:%nodeid%:%parm[snmpifindex]%" alarm-type="1" auto-clean="false">
      <update-field field-name="severity" update-on-reduction="true"/>
   </alarm-data>
</event>','Warning', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (101, 15, 'uei.opennms.org/nodes/snmp/interfaceOperUnknown', 'OpenNMS-defined node event: snmp interface Oper Status Unknown', '<p>The operational status of interface is unknown
            </p> <p>This event is generated when an snmp poll on interface find the operational status unknown.
            </p> <p>The unknown state indicates that the state of the interface can not be
            ascertained.</p>
            <p>Params %parm[all]% </p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/nodes/snmp/interfaceOperUnknown</uei>
   <event-label>OpenNMS-defined node event: snmp interface Oper Status Unknown</event-label>
   <descr>&lt;p>The operational status of interface is unknown
            &lt;/p> &lt;p>This event is generated when an snmp poll on interface find the operational status unknown.
            &lt;/p> &lt;p>The unknown state indicates that the state of the interface can not be
            ascertained.&lt;/p>
            &lt;p>Params %parm[all]% &lt;/p></descr>
   <logmsg dest="logndisplay">Operational status Unknown on interface ifname:%parm[snmpifname]%
            ifindex:%parm[snmpifindex]% ifdescr:%parm[snmpifdescr]%
        </logmsg>
   <severity>Minor</severity>
   <alarm-data reduction-key="uei.opennms.org/nodes/snmp/interfaceOperDown:%dpname%:%nodeid%:%parm[snmpifindex]%" alarm-type="1" auto-clean="false">
      <update-field field-name="severity" update-on-reduction="true"/>
   </alarm-data>
</event>','Minor', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (102, 15, 'uei.opennms.org/nodes/snmp/interfaceOperDormant', 'OpenNMS-defined node event: snmp interface Oper Status Dormant', '<p>The operational status of interface is dormant
            </p> <p>This event is generated when an snmp poll on interface find the operational status dormant.
            </p><p>The dormant state indicates that the relevant interface is not actually in a condition
            to pass packets but is in a pending state, waiting for some external event.</p>
            <p>Params %parm[all]% </p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/nodes/snmp/interfaceOperDormant</uei>
   <event-label>OpenNMS-defined node event: snmp interface Oper Status Dormant</event-label>
   <descr>&lt;p>The operational status of interface is dormant
            &lt;/p> &lt;p>This event is generated when an snmp poll on interface find the operational status dormant.
            &lt;/p>&lt;p>The dormant state indicates that the relevant interface is not actually in a condition
            to pass packets but is in a pending state, waiting for some external event.&lt;/p>
            &lt;p>Params %parm[all]% &lt;/p></descr>
   <logmsg dest="logndisplay">Operational status Dormant on interface ifname:%parm[snmpifname]%
            ifindex:%parm[snmpifindex]% ifdescr:%parm[snmpifdescr]%
        </logmsg>
   <severity>Warning</severity>
   <alarm-data reduction-key="uei.opennms.org/nodes/snmp/interfaceOperDown:%dpname%:%nodeid%:%parm[snmpifindex]%" alarm-type="1" auto-clean="false">
      <update-field field-name="severity" update-on-reduction="true"/>
   </alarm-data>
</event>','Warning', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (103, 15, 'uei.opennms.org/nodes/snmp/interfaceOperNotPresent', 'OpenNMS-defined node event: snmp interface Oper Status Not Present', '<p>The operational status of interface is not present
            </p> <p>This event is generated when an snmp poll on interface find the operational status not present.
            </p> <p>The not present state indicates that the interface is down specifically because
            some component, typically a hardware component, is not present in the managed system.</p>
            <p>Params %parm[all]% </p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/nodes/snmp/interfaceOperNotPresent</uei>
   <event-label>OpenNMS-defined node event: snmp interface Oper Status Not Present</event-label>
   <descr>&lt;p>The operational status of interface is not present
            &lt;/p> &lt;p>This event is generated when an snmp poll on interface find the operational status not present.
            &lt;/p> &lt;p>The not present state indicates that the interface is down specifically because
            some component, typically a hardware component, is not present in the managed system.&lt;/p>
            &lt;p>Params %parm[all]% &lt;/p></descr>
   <logmsg dest="logndisplay">Operational status Not Present on interface ifname:%parm[snmpifname]%
            ifindex:%parm[snmpifindex]% ifdescr:%parm[snmpifdescr]%
        </logmsg>
   <severity>Minor</severity>
   <alarm-data reduction-key="uei.opennms.org/nodes/snmp/interfaceOperDown:%dpname%:%nodeid%:%parm[snmpifindex]%" alarm-type="1" auto-clean="false">
      <update-field field-name="severity" update-on-reduction="true"/>
   </alarm-data>
</event>','Minor', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (104, 15, 'uei.opennms.org/nodes/snmp/interfaceOperLowerLayerDown', 'OpenNMS-defined node event: snmp interface Oper Status Lower Layer Down', '<p>The operational status of interface is lower layer down
            </p> <p>This event is generated when an snmp poll on interface find the operational status lower layer down.
            </p> <p>The lower layer down state indicates that this interface runs on top of one or
            more other interfaces and that this interface is down specifically because one or more of these
            lower-layer interfaces are down.</p>
            <p>Params %parm[all]% </p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/nodes/snmp/interfaceOperLowerLayerDown</uei>
   <event-label>OpenNMS-defined node event: snmp interface Oper Status Lower Layer Down</event-label>
   <descr>&lt;p>The operational status of interface is lower layer down
            &lt;/p> &lt;p>This event is generated when an snmp poll on interface find the operational status lower layer down.
            &lt;/p> &lt;p>The lower layer down state indicates that this interface runs on top of one or
            more other interfaces and that this interface is down specifically because one or more of these
            lower-layer interfaces are down.&lt;/p>
            &lt;p>Params %parm[all]% &lt;/p></descr>
   <logmsg dest="logndisplay">Operational status Lower Layer Down on interface ifname:%parm[snmpifname]%
            ifindex:%parm[snmpifindex]% ifdescr:%parm[snmpifdescr]%
        </logmsg>
   <severity>Minor</severity>
   <alarm-data reduction-key="uei.opennms.org/nodes/snmp/interfaceOperDown:%dpname%:%nodeid%:%parm[snmpifindex]%" alarm-type="1" auto-clean="false">
      <update-field field-name="severity" update-on-reduction="true"/>
   </alarm-data>
</event>','Minor', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (105, 15, 'uei.opennms.org/nodes/snmp/interfaceAdminDown', 'OpenNMS-defined node event: snmp interface Admin Status Down', '<p>The administration status of interface is down
            </p> <p>This event is generated when an snmp poll on interface find the administration status
            down.
            </p>
            <p>Params %parm[all]% </p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/nodes/snmp/interfaceAdminDown</uei>
   <event-label>OpenNMS-defined node event: snmp interface Admin Status Down</event-label>
   <descr>&lt;p>The administration status of interface is down
            &lt;/p> &lt;p>This event is generated when an snmp poll on interface find the administration status
            down.
            &lt;/p>
            &lt;p>Params %parm[all]% &lt;/p></descr>
   <logmsg dest="logndisplay">Administration status Down on interface ifname:%parm[snmpifname]%
            ifindex:%parm[snmpifindex]% ifdescr:%parm[snmpifdescr]%
        </logmsg>
   <severity>Minor</severity>
   <alarm-data reduction-key="%uei%:%dpname%:%nodeid%:%interface%:%parm[snmpifindex]%" alarm-type="1" auto-clean="false"/>
</event>','Minor', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (106, 15, 'uei.opennms.org/nodes/interfaceReparented', 'OpenNMS-defined node event: interfaceReparented', '<p>Interface %interface% has been reparented under
            node %parm[newNodeID]% from node
            %parm[oldNodeID]%.</p> <p>Usually this happens
            after a services scan discovers that a node with multiple
            interfaces is now running an SNMP agent and is therefore
            able to reparent the node''s interfaces under a single node
            identifier.</p> <p>This is typically not a
            reason for concern, but you should be aware that the node
            association of this interface has changed.</p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/nodes/interfaceReparented</uei>
   <event-label>OpenNMS-defined node event: interfaceReparented</event-label>
   <descr>&lt;p>Interface %interface% has been reparented under
            node %parm[newNodeID]% from node
            %parm[oldNodeID]%.&lt;/p> &lt;p>Usually this happens
            after a services scan discovers that a node with multiple
            interfaces is now running an SNMP agent and is therefore
            able to reparent the node''s interfaces under a single node
            identifier.&lt;/p> &lt;p>This is typically not a
            reason for concern, but you should be aware that the node
            association of this interface has changed.&lt;/p></descr>
   <logmsg dest="logndisplay">
            %interface% has been reparented under node %parm[newNodeID]%
            from node %parm[oldNodeID]%.
        </logmsg>
   <severity>Warning</severity>
</event>','Warning', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (107, 15, 'uei.opennms.org/nodes/interfaceUp', 'OpenNMS-defined node event: interfaceUp', '<p>The interface %interface% which was previously down
            is now up.</p> <p>This event is generated when
            node outage processing determines that the critical service
            or all services on the interface are restored. </p>
            <p>This event will cause any active outages associated
            with this interface to be cleared.</p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/nodes/interfaceUp</uei>
   <event-label>OpenNMS-defined node event: interfaceUp</event-label>
   <descr>&lt;p>The interface %interface% which was previously down
            is now up.&lt;/p> &lt;p>This event is generated when
            node outage processing determines that the critical service
            or all services on the interface are restored. &lt;/p>
            &lt;p>This event will cause any active outages associated
            with this interface to be cleared.&lt;/p></descr>
   <logmsg dest="logndisplay">
            Interface %interface% is up.
        </logmsg>
   <severity>Normal</severity>
   <alarm-data reduction-key="%uei%:%dpname%:%nodeid%:%interface%" alarm-type="2" clear-key="uei.opennms.org/nodes/interfaceDown:%dpname%:%nodeid%:%interface%" auto-clean="false"/>
</event>','Normal', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (108, 15, 'uei.opennms.org/nodes/snmp/interfaceOperUp', 'OpenNMS-defined node event: snmp interface Oper Status Up', '<p>The operational status of interface is up
            </p> <p>This event is generated when an snmp poll on interface find the operational status up.
            </p>
            <p>Params %parm[all]% </p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/nodes/snmp/interfaceOperUp</uei>
   <event-label>OpenNMS-defined node event: snmp interface Oper Status Up</event-label>
   <descr>&lt;p>The operational status of interface is up
            &lt;/p> &lt;p>This event is generated when an snmp poll on interface find the operational status up.
            &lt;/p>
            &lt;p>Params %parm[all]% &lt;/p></descr>
   <logmsg dest="logndisplay">Operational status Up on interface ifname:%parm[snmpifname]%
            ifindex:%parm[snmpifindex]% ifdescr:%parm[snmpifdescr]%
        </logmsg>
   <severity>Normal</severity>
   <alarm-data reduction-key="%uei%:%dpname%:%nodeid%:%parm[snmpifindex]%" alarm-type="2" clear-key="uei.opennms.org/nodes/snmp/interfaceOperDown:%dpname%:%nodeid%:%parm[snmpifindex]%" auto-clean="false"/>
</event>','Normal', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (109, 15, 'uei.opennms.org/nodes/snmp/interfaceAdminUp', 'OpenNMS-defined node event: snmp interface Admin Status Up', '<p>The administration status of interface is down
            </p> <p>This event is generated when an snmp poll on interface find the administration status
            up.
            </p>
            <p>Params %parm[all]% </p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/nodes/snmp/interfaceAdminUp</uei>
   <event-label>OpenNMS-defined node event: snmp interface Admin Status Up</event-label>
   <descr>&lt;p>The administration status of interface is down
            &lt;/p> &lt;p>This event is generated when an snmp poll on interface find the administration status
            up.
            &lt;/p>
            &lt;p>Params %parm[all]% &lt;/p></descr>
   <logmsg dest="logndisplay">Administration status Up on interface ifname:%parm[snmpifname]%
            ifindex:%parm[snmpifindex]% ifdescr:%parm[snmpifdescr]%
        </logmsg>
   <severity>Normal</severity>
   <alarm-data reduction-key="%uei%:%dpname%:%nodeid%:%interface%:%parm[snmpifindex]%" alarm-type="2" clear-key="uei.opennms.org/nodes/snmp/interfaceAdminDown:%dpname%:%nodeid%:%interface%:%parm[snmpifindex]%" auto-clean="false"/>
</event>','Normal', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (110, 15, 'uei.opennms.org/nodes/nodeAdded', 'OpenNMS-defined node event: nodeAdded', 'The node "%parm[nodelabel]%" was added and is now being monitored.', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/nodes/nodeAdded</uei>
   <event-label>OpenNMS-defined node event: nodeAdded</event-label>
   <descr>The node &quot;%parm[nodelabel]%&quot; was added and is now being monitored.</descr>
   <logmsg dest="logndisplay">A new node &quot;%parm[nodelabel]%&quot; was added.</logmsg>
   <severity>Warning</severity>
   <operinstruct>This event is for information only. Please make sure that the newly added device &lt;a href=&quot;element/node.jsp?node=%nodeid%&quot;>&quot;%parm[nodelabel]%&quot;&lt;/a> is monitored as desired.</operinstruct>
</event>','Warning', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (111, 15, 'uei.opennms.org/nodes/nodeUpdated', 'OpenNMS-defined node event: nodeUpdated', '<p>A currently provisioned node (%parm[nodelabel]%) was updated by
            OpenNMS.</p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/nodes/nodeUpdated</uei>
   <event-label>OpenNMS-defined node event: nodeUpdated</event-label>
   <descr>&lt;p>A currently provisioned node (%parm[nodelabel]%) was updated by
            OpenNMS.&lt;/p></descr>
   <logmsg dest="logndisplay">
            A provisioned node (%parm[nodelabel]%) was updated by OpenNMS.
        </logmsg>
   <severity>Warning</severity>
</event>','Warning', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (112, 15, 'uei.opennms.org/nodes/nodeLocationChanged', 'OpenNMS-defined node event: nodeLocationChanged', '<p>A currently provisioned node (%parm[nodelabel]%) changed its
        location from (%parm[nodePrevLocation]%) to (%parm[nodeCurrentLocation]%).</p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/nodes/nodeLocationChanged</uei>
   <event-label>OpenNMS-defined node event: nodeLocationChanged</event-label>
   <descr>&lt;p>A currently provisioned node (%parm[nodelabel]%) changed its
        location from (%parm[nodePrevLocation]%) to (%parm[nodeCurrentLocation]%).&lt;/p></descr>
   <logmsg dest="logndisplay">
        A provisioned node (%parm[nodelabel]%) changed its location to (%parm[nodeCurrentLocation]%).
      </logmsg>
   <severity>Normal</severity>
</event>','Normal', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (113, 15, 'uei.opennms.org/nodes/nodeCategoryMembershipChanged', 'OpenNMS-defined node event: nodeCategoryMembershipChanged', '<p>Node (%parm[nodelabel]%) has changed its Category
          membership and deleted (%parm[categoriesDeleted]%) and added (%parm[categoriesAdded]%).</p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/nodes/nodeCategoryMembershipChanged</uei>
   <event-label>OpenNMS-defined node event: nodeCategoryMembershipChanged</event-label>
   <descr>&lt;p>Node (%parm[nodelabel]%) has changed its Category
          membership and deleted (%parm[categoriesDeleted]%) and added (%parm[categoriesAdded]%).&lt;/p></descr>
   <logmsg dest="logndisplay">
            Node category membership has changed for node (%parm[nodelabel]%).
        </logmsg>
   <severity>Warning</severity>
</event>','Warning', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (114, 15, 'uei.opennms.org/nodes/nodeDeleted', 'OpenNMS-defined node event: nodeDeleted', '<p>%parm[nodelabel]% (%parm[foreignSource]%:%parm[foreignId]%) in location %parm[location]% was deleted from requisition %parm[foreignSource]%.</p>
             <p>This can have multiple reasons.
             <ul>
             <li>It was removed from the corresponding requisition %parm[foreignSource]%. This can be done manually using the web UI or using provisiond import schedules.</li>
             <li>It was manually deleted using the "Delete nodes" entry in the Admin menu.</li>
             <li>It was removed using the ReST API</li>
             </ul>
             Operator Instructions:<br/>
             Please verify if the deletion was planned.</p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/nodes/nodeDeleted</uei>
   <event-label>OpenNMS-defined node event: nodeDeleted</event-label>
   <descr>&lt;p>%parm[nodelabel]% (%parm[foreignSource]%:%parm[foreignId]%) in location %parm[location]% was deleted from requisition %parm[foreignSource]%.&lt;/p>
             &lt;p>This can have multiple reasons.
             &lt;ul>
             &lt;li>It was removed from the corresponding requisition %parm[foreignSource]%. This can be done manually using the web UI or using provisiond import schedules.&lt;/li>
             &lt;li>It was manually deleted using the &quot;Delete nodes&quot; entry in the Admin menu.&lt;/li>
             &lt;li>It was removed using the ReST API&lt;/li>
             &lt;/ul>
             Operator Instructions:&lt;br/>
             Please verify if the deletion was planned.&lt;/p></descr>
   <logmsg dest="logndisplay">
            Node %parm[nodelabel]% (%nodeid%) was deleted.
        </logmsg>
   <severity>Warning</severity>
</event>','Warning', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (115, 15, 'uei.opennms.org/nodes/nodeDown', 'OpenNMS-defined node event: nodeDown', '<p>All interfaces on node %parm[nodelabel]% are
            down because of the following condition: %parm[eventReason]%.</p> <p>
            This event is generated when node outage processing determines
            that all interfaces on the node are down.</p> <p>
            New outage records have been created and service level
            availability calculations will be impacted until this outage
            is resolved.</p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/nodes/nodeDown</uei>
   <event-label>OpenNMS-defined node event: nodeDown</event-label>
   <descr>&lt;p>All interfaces on node %parm[nodelabel]% are
            down because of the following condition: %parm[eventReason]%.&lt;/p> &lt;p>
            This event is generated when node outage processing determines
            that all interfaces on the node are down.&lt;/p> &lt;p>
            New outage records have been created and service level
            availability calculations will be impacted until this outage
            is resolved.&lt;/p></descr>
   <logmsg dest="logndisplay">
            Node %parm[nodelabel]% is down.
        </logmsg>
   <severity>Major</severity>
   <alarm-data reduction-key="%uei%:%dpname%:%nodeid%" alarm-type="1" auto-clean="false"/>
</event>','Major', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (116, 15, 'uei.opennms.org/nodes/pathOutage', 'OpenNMS-defined node event: pathOutage', '<p>The state of node %parm[nodelabel]% is unknown
            because the critical path to the node is down.</p>
            <p>This event is generated when node outage processing
            determines that the critical path IP address/service for
            this node is not responding..</p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/nodes/pathOutage</uei>
   <event-label>OpenNMS-defined node event: pathOutage</event-label>
   <descr>&lt;p>The state of node %parm[nodelabel]% is unknown
            because the critical path to the node is down.&lt;/p>
            &lt;p>This event is generated when node outage processing
            determines that the critical path IP address/service for
            this node is not responding..&lt;/p></descr>
   <logmsg dest="logndisplay">
            %parm[nodelabel]% path outage. Critical path =
            %parm[criticalPathIp]% %parm[criticalPathServiceName]%
        </logmsg>
   <severity>Major</severity>
   <alarm-data reduction-key="%uei%:%dpname%:%nodeid%" alarm-type="3" auto-clean="false"/>
</event>','Major', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (117, 15, 'uei.opennms.org/nodes/nodeGainedInterface', 'OpenNMS-defined node event: nodeGainedInterface', '<p>Interface %interface% has been associated with Node
            #<a
            href="element/node.jsp?node=%nodeid%">%nodeid%</a>.</p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/nodes/nodeGainedInterface</uei>
   <event-label>OpenNMS-defined node event: nodeGainedInterface</event-label>
   <descr>&lt;p>Interface %interface% has been associated with Node
            #&lt;a
            href=&quot;element/node.jsp?node=%nodeid%&quot;>%nodeid%&lt;/a>.&lt;/p></descr>
   <logmsg dest="logndisplay">
            Interface %interface% has been associated with Node #&lt;a
            href=&quot;element/node.jsp?node=%nodeid%&quot;>%nodeid%&lt;/a>.
        </logmsg>
   <severity>Warning</severity>
</event>','Warning', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (118, 15, 'uei.opennms.org/nodes/nodeGainedService', 'OpenNMS-defined node event: nodeGainedService', '<p>A service scan has identified the %service% service
            on interface %interface%.</p> <p>If this
            interface (%interface%) is within the list of ranges and
            specific addresses to be managed by OpenNMS, this service
            will be scheduled for regular availability checks.</p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/nodes/nodeGainedService</uei>
   <event-label>OpenNMS-defined node event: nodeGainedService</event-label>
   <descr>&lt;p>A service scan has identified the %service% service
            on interface %interface%.&lt;/p> &lt;p>If this
            interface (%interface%) is within the list of ranges and
            specific addresses to be managed by OpenNMS, this service
            will be scheduled for regular availability checks.&lt;/p></descr>
   <logmsg dest="logndisplay">
            The %service% service has been discovered on interface
            %interface%.
        </logmsg>
   <severity>Warning</severity>
</event>','Warning', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (119, 15, 'uei.opennms.org/nodes/nodeInfoChanged', 'OpenNMS-defined node event: nodeInfoChanged', '<p>Node information has changed for node
            #%nodeid%.</p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/nodes/nodeInfoChanged</uei>
   <event-label>OpenNMS-defined node event: nodeInfoChanged</event-label>
   <descr>&lt;p>Node information has changed for node
            #%nodeid%.&lt;/p></descr>
   <logmsg dest="logndisplay">
            &lt;p>Node information has changed for &lt;a
            href=&quot;element/node.jsp?node=%nodeid%&quot;>%nodeid%&lt;/a>.&lt;/p>
        </logmsg>
   <severity>Warning</severity>
</event>','Warning', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (120, 15, 'uei.opennms.org/nodes/nodeLabelChanged', 'OpenNMS-defined node event: nodeLabelChanged', '<p>Node #<a
            href="element/node.jsp?node=%nodeid%">%nodeid%</a>''s
            label was changed from "%parm[oldNodeLabel]%" to
            "%parm[newNodeLabel]%".</p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/nodes/nodeLabelChanged</uei>
   <event-label>OpenNMS-defined node event: nodeLabelChanged</event-label>
   <descr>&lt;p>Node #&lt;a
            href=&quot;element/node.jsp?node=%nodeid%&quot;>%nodeid%&lt;/a>''s
            label was changed from &quot;%parm[oldNodeLabel]%&quot; to
            &quot;%parm[newNodeLabel]%&quot;.&lt;/p></descr>
   <logmsg dest="logndisplay">
            Node #&lt;a
            href=&quot;element/node.jsp?node=%nodeid%&quot;>%nodeid%&lt;/a>''s
            label was changed from &quot;%parm[oldNodeLabel]%&quot; to
            &quot;%parm[newNodeLabel]%&quot;.
        </logmsg>
   <severity>Normal</severity>
</event>','Normal', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (121, 15, 'uei.opennms.org/nodes/nodeLostService', 'OpenNMS-defined node event: nodeLostService', '<p>A %service% outage was identified on interface
            %interface% because of the following condition: %parm[eventReason]%.</p> <p>
            A new Outage record has been created and service level
            availability calculations will be impacted until this outage is
            resolved.</p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/nodes/nodeLostService</uei>
   <event-label>OpenNMS-defined node event: nodeLostService</event-label>
   <descr>&lt;p>A %service% outage was identified on interface
            %interface% because of the following condition: %parm[eventReason]%.&lt;/p> &lt;p>
            A new Outage record has been created and service level
            availability calculations will be impacted until this outage is
            resolved.&lt;/p></descr>
   <logmsg dest="logndisplay">
            %service% outage identified on interface %interface%.
        </logmsg>
   <severity>Minor</severity>
   <alarm-data reduction-key="%uei%:%dpname%:%nodeid%:%interface%:%service%" alarm-type="1" auto-clean="false"/>
</event>','Minor', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (122, 15, 'uei.opennms.org/nodes/nodeRegainedService', 'OpenNMS-defined node event: nodeRegainedService', '<p>The %service% service on interface %interface% was
            previously down and has been restored.</p>
            <p>This event is generated when a service which had
            previously failed polling attempts is again responding to
            polls by OpenNMS. </p> <p>This event will cause
            any active outages associated with this service/interface
            combination to be cleared.</p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/nodes/nodeRegainedService</uei>
   <event-label>OpenNMS-defined node event: nodeRegainedService</event-label>
   <descr>&lt;p>The %service% service on interface %interface% was
            previously down and has been restored.&lt;/p>
            &lt;p>This event is generated when a service which had
            previously failed polling attempts is again responding to
            polls by OpenNMS. &lt;/p> &lt;p>This event will cause
            any active outages associated with this service/interface
            combination to be cleared.&lt;/p></descr>
   <logmsg dest="logndisplay">
            The %service% outage on interface %interface% has been
            cleared. Service is restored.
        </logmsg>
   <severity>Normal</severity>
   <alarm-data reduction-key="%uei%:%dpname%:%nodeid%:%interface%:%service%" alarm-type="2" clear-key="uei.opennms.org/nodes/nodeLostService:%dpname%:%nodeid%:%interface%:%service%" auto-clean="false"/>
</event>','Normal', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (123, 15, 'uei.opennms.org/nodes/nodeUp', 'OpenNMS-defined node event: nodeUp', '<p>Node %parm[nodelabel]% which was previously down is
            now up.</p> <p>This event is generated when node
            outage processing determines that all interfaces on the node
            are up.</p> <p>This event will cause any active
            outages associated with this node to be cleared.</p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/nodes/nodeUp</uei>
   <event-label>OpenNMS-defined node event: nodeUp</event-label>
   <descr>&lt;p>Node %parm[nodelabel]% which was previously down is
            now up.&lt;/p> &lt;p>This event is generated when node
            outage processing determines that all interfaces on the node
            are up.&lt;/p> &lt;p>This event will cause any active
            outages associated with this node to be cleared.&lt;/p></descr>
   <logmsg dest="logndisplay">
            Node %parm[nodelabel]% is up.
        </logmsg>
   <severity>Normal</severity>
   <alarm-data reduction-key="%uei%:%dpname%:%nodeid%" alarm-type="2" clear-key="uei.opennms.org/nodes/nodeDown:%dpname%:%nodeid%" auto-clean="false"/>
</event>','Normal', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (124, 15, 'uei.opennms.org/nodes/primarySnmpInterfaceChanged', 'OpenNMS-defined node event: primarySnmpInterfaceChanged', '<p>This event indicates that the interface selected
            for SNMP data collection for this node has changed. This is
            usually due to a network or address reconfiguration
            impacting this device.</p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/nodes/primarySnmpInterfaceChanged</uei>
   <event-label>OpenNMS-defined node event: primarySnmpInterfaceChanged</event-label>
   <descr>&lt;p>This event indicates that the interface selected
            for SNMP data collection for this node has changed. This is
            usually due to a network or address reconfiguration
            impacting this device.&lt;/p></descr>
   <logmsg dest="logndisplay">
            Primary SNMP interface for node &lt;a
            href=&quot;element/node.jsp?node=%nodeid%&quot;>%nodeid%&lt;/a>
            has changed from %parm[oldPrimarySnmpAddress]% to
            %parm[newPrimarySnmpAddress]%.
        </logmsg>
   <severity>Warning</severity>
</event>','Warning', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (125, 15, 'uei.opennms.org/nodes/reinitializePrimarySnmpInterface', 'OpenNMS-defined node event: reinitializePrimarySnmpInterface', '<p>A change in configuration on this node has been
            detected and the SNMP data collection mechanism is being
            triggered to refresh its required profile of the remote
            node.</p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/nodes/reinitializePrimarySnmpInterface</uei>
   <event-label>OpenNMS-defined node event: reinitializePrimarySnmpInterface</event-label>
   <descr>&lt;p>A change in configuration on this node has been
            detected and the SNMP data collection mechanism is being
            triggered to refresh its required profile of the remote
            node.&lt;/p></descr>
   <logmsg dest="logndisplay">
            SNMP information on %interface% is being refreshed for data
            collection purposes.
        </logmsg>
   <severity>Warning</severity>
</event>','Warning', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (126, 15, 'uei.opennms.org/nodes/serviceResponsive', 'OpenNMS-defined node event: serviceResponsive', '<p>The %service% service which was previously unresponsive
            is now responding normally on interface %interface%.</p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/nodes/serviceResponsive</uei>
   <event-label>OpenNMS-defined node event: serviceResponsive</event-label>
   <descr>&lt;p>The %service% service which was previously unresponsive
            is now responding normally on interface %interface%.&lt;/p></descr>
   <logmsg dest="logndisplay">
            %service% is responding normally on interface %interface%.
        </logmsg>
   <severity>Normal</severity>
</event>','Normal', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (127, 15, 'uei.opennms.org/nodes/serviceDeleted', 'OpenNMS-defined node event: serviceDeleted', '<p>Service %service% was deleted from interface
            %interface%, associated with Node ID# %nodeid%.</p>
            <p>When a service is deleted from an interface, it is
            due to extended downtime model configured in pollerd
            configuration.</p> <p>If a previously deleted service
            becomes active again on an interface, it will be re-added to
            the OpenNMS database as a new occurrence of that service and
            will be disassociated with any historic outages.</p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/nodes/serviceDeleted</uei>
   <event-label>OpenNMS-defined node event: serviceDeleted</event-label>
   <descr>&lt;p>Service %service% was deleted from interface
            %interface%, associated with Node ID# %nodeid%.&lt;/p>
            &lt;p>When a service is deleted from an interface, it is
            due to extended downtime model configured in pollerd
            configuration.&lt;/p> &lt;p>If a previously deleted service
            becomes active again on an interface, it will be re-added to
            the OpenNMS database as a new occurrence of that service and
            will be disassociated with any historic outages.&lt;/p></descr>
   <logmsg dest="logndisplay">
            The %service% service was deleted from interface
            %interface%.
        </logmsg>
   <severity>Warning</severity>
</event>','Warning', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (128, 15, 'uei.opennms.org/nodes/serviceUnresponsive', 'OpenNMS-defined node event: serviceUnresponsive', '<p>The %service% service is up but was unresponsive
            during the last poll on interface %interface%.</p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/nodes/serviceUnresponsive</uei>
   <event-label>OpenNMS-defined node event: serviceUnresponsive</event-label>
   <descr>&lt;p>The %service% service is up but was unresponsive
            during the last poll on interface %interface%.&lt;/p></descr>
   <logmsg dest="logndisplay">
            %service% is up but unresponsive on interface %interface%.
        </logmsg>
   <severity>Minor</severity>
</event>','Minor', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (129, 15, 'uei.opennms.org/nodes/assetInfoChanged', 'OpenNMS-defined node event: assetInfoChanged', '<p>The Asset info for node %nodeid% (%nodelabel%)
            has been changed via the webUI.</p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/nodes/assetInfoChanged</uei>
   <event-label>OpenNMS-defined node event: assetInfoChanged</event-label>
   <descr>&lt;p>The Asset info for node %nodeid% (%nodelabel%)
            has been changed via the webUI.&lt;/p></descr>
   <logmsg dest="logndisplay">
            &lt;p>The Asset info for node %nodeid% (%nodelabel%)
            has been changed via the webUI.&lt;/p>
        </logmsg>
   <severity>Normal</severity>
</event>','Normal', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (130, 15, 'uei.opennms.org/deviceconfig/configBackupStarted', 'OpenNMS-defined node event: configBackupStarted', '<p>Config backup started on %service%
            during the last poll on interface %interface%.</p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/deviceconfig/configBackupStarted</uei>
   <event-label>OpenNMS-defined node event: configBackupStarted</event-label>
   <descr>&lt;p>Config backup started on %service%
            during the last poll on interface %interface%.&lt;/p></descr>
   <logmsg dest="logndisplay">
            %service% config backup started on interface %interface%.
        </logmsg>
   <severity>Normal</severity>
</event>','Normal', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (131, 15, 'uei.opennms.org/deviceconfig/configBackupFailed', 'OpenNMS-defined node event: configBackupFailed', '<p>Failed to backup config associated with %service%
            during the last poll on interface %interface% because of
            the following condition: %parm[eventReason]%.</p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/deviceconfig/configBackupFailed</uei>
   <event-label>OpenNMS-defined node event: configBackupFailed</event-label>
   <descr>&lt;p>Failed to backup config associated with %service%
            during the last poll on interface %interface% because of
            the following condition: %parm[eventReason]%.&lt;/p></descr>
   <logmsg dest="logndisplay">
            %service% config backup failed on interface %interface%.
        </logmsg>
   <severity>Minor</severity>
   <alarm-data reduction-key="%uei%:%dpname%:%nodeid%:%interface%:%service%" alarm-type="1" auto-clean="false"/>
</event>','Minor', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (132, 15, 'uei.opennms.org/deviceconfig/configBackupSucceeded', 'OpenNMS-defined node event: configBackupSucceeded', '<p>Config backup succeeded on %service%
            during the last poll on interface %interface%.</p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/deviceconfig/configBackupSucceeded</uei>
   <event-label>OpenNMS-defined node event: configBackupSucceeded</event-label>
   <descr>&lt;p>Config backup succeeded on %service%
            during the last poll on interface %interface%.&lt;/p></descr>
   <logmsg dest="logndisplay">
            %service% config backup succeeded on interface %interface%.
        </logmsg>
   <severity>Normal</severity>
   <alarm-data reduction-key="%uei%:%dpname%:%nodeid%:%interface%:%service%" alarm-type="2" clear-key="uei.opennms.org/deviceconfig/configBackupFailed:%dpname%:%nodeid%:%interface%:%service%" auto-clean="false"/>
</event>','Normal', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (133, 16, 'uei.opennms.org/provisioner/provisioningAdapterFailed', 'OpenNMS-defined Provisioning Adapter Failed message', 'A provisioning adapter failed for host %host% with the following condition: %parm[reason]%.<p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/provisioner/provisioningAdapterFailed</uei>
   <event-label>OpenNMS-defined Provisioning Adapter Failed message</event-label>
   <descr>A provisioning adapter failed for host %host% with the following condition: %parm[reason]%.&lt;p></descr>
   <logmsg dest="logndisplay">
            &lt;p>A provisioning adapter failed for host.&lt;/p>
        </logmsg>
   <severity>Major</severity>
   <alarm-data reduction-key="%uei%:%host%:%parm[reason]%" alarm-type="3" auto-clean="false"/>
</event>','Major', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (134, 16, 'uei.opennms.org/internal/provisiond/scheduledNodeScanStarted', 'OpenNMS-defined Provisiond Event: scheduledNodeScanStarted', 'A message from the Provisiond NodeScan lifecycle that a scheduled NodeScan has started:
        <p>The Node with Id: %nodeid%; ForeignSource: %parm[foreignSource]%; ForeignId:%parm[foreignId]% has
           started scheduled Node Scan. </p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/internal/provisiond/scheduledNodeScanStarted</uei>
   <event-label>OpenNMS-defined Provisiond Event: scheduledNodeScanStarted</event-label>
   <descr>A message from the Provisiond NodeScan lifecycle that a scheduled NodeScan has started:
        &lt;p>The Node with Id: %nodeid%; ForeignSource: %parm[foreignSource]%; ForeignId:%parm[foreignId]% has
           started scheduled Node Scan. &lt;/p></descr>
   <logmsg dest="logndisplay">
        &lt;p>The Node with Id: %nodeid%; ForeignSource: %parm[foreignSource]%; ForeignId:%parm[foreignId]% has
        started scheduled scan.&lt;/p>
      </logmsg>
   <severity>Normal</severity>
</event>','Normal', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (135, 16, 'uei.opennms.org/internal/provisiond/nodeScanCompleted', 'OpenNMS-defined Provisiond Event: nodeScanCompleted', 'A message from the Provisiond NodeScan lifecycle that a NodeScan has completed:
            <p>The Node with Id: %nodeid%; ForeignSource: %parm[foreignSource]%; ForeignId:%parm[foreignId]% has
            completed.</p>
            Typically the result of a request of an import request or a scheduled/user forced rescan.', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/internal/provisiond/nodeScanCompleted</uei>
   <event-label>OpenNMS-defined Provisiond Event: nodeScanCompleted</event-label>
   <descr>A message from the Provisiond NodeScan lifecycle that a NodeScan has completed:
            &lt;p>The Node with Id: %nodeid%; ForeignSource: %parm[foreignSource]%; ForeignId:%parm[foreignId]% has
            completed.&lt;/p>
            Typically the result of a request of an import request or a scheduled/user forced rescan.</descr>
   <logmsg dest="logndisplay">
            &lt;p>The Node with Id: %nodeid%; ForeignSource: %parm[foreignSource]%; ForeignId:%parm[foreignId]% has
            completed.&lt;/p>
        </logmsg>
   <severity>Normal</severity>
</event>','Normal', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (136, 16, 'uei.opennms.org/internal/provisiond/nodeScanAborted', 'OpenNMS-defined Provisiond Event: nodeScanAborted', 'A message from the Provisiond NodeScan lifecycle that a NodeScan has Aborted:
            <p>The Node with Id: %nodeid%; ForeignSource: %parm[foreignSource]%; ForeignId:%parm[foreignId]% has
            aborted for the following reason: %parm[reason]% </p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/internal/provisiond/nodeScanAborted</uei>
   <event-label>OpenNMS-defined Provisiond Event: nodeScanAborted</event-label>
   <descr>A message from the Provisiond NodeScan lifecycle that a NodeScan has Aborted:
            &lt;p>The Node with Id: %nodeid%; ForeignSource: %parm[foreignSource]%; ForeignId:%parm[foreignId]% has
            aborted for the following reason: %parm[reason]% &lt;/p></descr>
   <logmsg dest="logndisplay">
            &lt;p>The Node with Id: %nodeid%; ForeignSource: %parm[foreignSource]%; ForeignId:%parm[foreignId]% has
            aborted.&lt;/p>
        </logmsg>
   <severity>Warning</severity>
</event>','Warning', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (137, 16, 'uei.opennms.org/internal/importer/reloadImport', 'OpenNMS-defined internal event: importer reloadImport', '<p>This event will cause the importer to run the model-import process.
            The parameters include foreignSource, url, and deleteThreshold that override
            configuration properties as well as XML and default values.</p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/internal/importer/reloadImport</uei>
   <event-label>OpenNMS-defined internal event: importer reloadImport</event-label>
   <descr>&lt;p>This event will cause the importer to run the model-import process.
            The parameters include foreignSource, url, and deleteThreshold that override
            configuration properties as well as XML and default values.&lt;/p></descr>
   <logmsg dest="logndisplay">
            &lt;p>A request had been made to run the model-import process with the
            parms: %parm[all]%.&lt;/p>
        </logmsg>
   <severity>Normal</severity>
</event>','Normal', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (138, 16, 'uei.opennms.org/internal/importer/importStarted', 'OpenNMS-defined internal event: importer process has started', '<p>This event indicates the model-importer process has started</p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/internal/importer/importStarted</uei>
   <event-label>OpenNMS-defined internal event: importer process has started</event-label>
   <descr>&lt;p>This event indicates the model-importer process has started&lt;/p></descr>
   <logmsg dest="logndisplay">
            &lt;p>This event indicates the model-importer process has started from resource: %parm[importResource]%
            &lt;/p>
        </logmsg>
   <severity>Normal</severity>
</event>','Normal', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (139, 16, 'uei.opennms.org/internal/importer/importSuccessful', 'OpenNMS-defined internal event: importer process successfully completed', '<p>This event indicates the model-importer process has completed successfully. There
            is 1 parameter called importStats: %parm[importStats]%</p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/internal/importer/importSuccessful</uei>
   <event-label>OpenNMS-defined internal event: importer process successfully completed</event-label>
   <descr>&lt;p>This event indicates the model-importer process has completed successfully. There
            is 1 parameter called importStats: %parm[importStats]%&lt;/p></descr>
   <logmsg dest="logndisplay">
            &lt;p>This event indicates the model-importer process has completed successfully from resource:
            %parm[importResource]%&lt;/p>
        </logmsg>
   <severity>Normal</severity>
   <alarm-data reduction-key="%uei%:%parm[importResource]%" alarm-type="2" clear-key="uei.opennms.org/internal/importer/importFailed:%parm[importResource]%" auto-clean="true"/>
</event>','Normal', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (140, 16, 'uei.opennms.org/internal/importer/importFailed', 'OpenNMS-defined internal event: importer process failed.', '<p>This event indicates the model-importer process has failed. There is 1 parameter
            called failureMessage: %parm[failureMessage]%</p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/internal/importer/importFailed</uei>
   <event-label>OpenNMS-defined internal event: importer process failed.</event-label>
   <descr>&lt;p>This event indicates the model-importer process has failed. There is 1 parameter
            called failureMessage: %parm[failureMessage]%&lt;/p></descr>
   <logmsg dest="logndisplay">
            &lt;p>This event indicates the model-importer process has failed from resource: %parm[importResource]%&lt;/p>
        </logmsg>
   <severity>Warning</severity>
   <alarm-data reduction-key="%uei%:%parm[importResource]%" alarm-type="1" auto-clean="false"/>
</event>','Warning', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (141, 17, 'uei.opennms.org/circuitBreaker/stateChange', 'OpenNMS-defined event: Circuit breaker has changed state', 'A cirtcuit breaker named %parm[name]% on %dpname% has changed state from %parm[fromState]% to %parm[toState]%.', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <mask>
      <maskelement>
         <mename>parm[toState]</mename>
         <mevalue>~OPEN|HALF_OPEN|FORCED_OPEN</mevalue>
      </maskelement>
   </mask>
   <uei>uei.opennms.org/circuitBreaker/stateChange</uei>
   <event-label>OpenNMS-defined event: Circuit breaker has changed state</event-label>
   <descr>A cirtcuit breaker named %parm[name]% on %dpname% has changed state from %parm[fromState]% to %parm[toState]%.</descr>
   <logmsg dest="logndisplay">Circuit breaker %parm[name]% on %dpname% changed state to %parm[toState]%</logmsg>
   <severity>Warning</severity>
   <alarm-data reduction-key="%uei%:%dpname%:%parm[name]%" alarm-type="3" auto-clean="false">
      <update-field field-name="severity" update-on-reduction="true"/>
   </alarm-data>
</event>','Warning', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (142, 17, 'uei.opennms.org/circuitBreaker/stateChange', 'OpenNMS-defined event: Circuit breaker has changed state', 'A cirtcuit breaker named %parm[name]% on %dpname% has changed state from %parm[fromState]% to %parm[toState]%.', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <mask>
      <maskelement>
         <mename>parm[toState]</mename>
         <mevalue>~CLOSED|DISABLED</mevalue>
      </maskelement>
   </mask>
   <uei>uei.opennms.org/circuitBreaker/stateChange</uei>
   <event-label>OpenNMS-defined event: Circuit breaker has changed state</event-label>
   <descr>A cirtcuit breaker named %parm[name]% on %dpname% has changed state from %parm[fromState]% to %parm[toState]%.</descr>
   <logmsg dest="logndisplay">Circuit breaker %parm[name]% on %dpname% changed state to: %parm[toState]%</logmsg>
   <severity>Normal</severity>
   <alarm-data reduction-key="%uei%:%dpname%:%parm[name]%" alarm-type="3" auto-clean="false">
      <update-field field-name="severity" update-on-reduction="true"/>
   </alarm-data>
</event>','Normal', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (143, 18, 'uei.opennms.org/perspective/nodes/nodeLostService', 'OpenNMS-defined perspective poller event: A perspective poller detected a node lost service', '<p>A %service% outage was identified on interface %interface% from location: %parm[perspective]%.</p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/perspective/nodes/nodeLostService</uei>
   <event-label>OpenNMS-defined perspective poller event: A perspective poller detected a node lost service</event-label>
   <descr>&lt;p>A %service% outage was identified on interface %interface% from location: %parm[perspective]%.&lt;/p></descr>
   <logmsg dest="logndisplay">
            %service% outage identified on interface %interface% from location %parm[perspective]% with reason code: %parm[eventReason]%.
        </logmsg>
   <severity>Minor</severity>
   <alarm-data reduction-key="%uei%:%parm[perspective]%:%nodeid%:%interface%:%service%" alarm-type="1" auto-clean="false"/>
</event>','Minor', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (144, 18, 'uei.opennms.org/perspective/nodes/nodeRegainedService', 'OpenNMS-defined perspective poller event: A perspective poller detected a node regained service', '<p>The %service% service on interface %interface% was previously down from %parm[perspective]%.</p>
               <p>This event is generated when a service which had previously failed polling attempts is again responding to polls by OpenNMS. </p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/perspective/nodes/nodeRegainedService</uei>
   <event-label>OpenNMS-defined perspective poller event: A perspective poller detected a node regained service</event-label>
   <descr>&lt;p>The %service% service on interface %interface% was previously down from %parm[perspective]%.&lt;/p>
               &lt;p>This event is generated when a service which had previously failed polling attempts is again responding to polls by OpenNMS. &lt;/p></descr>
   <logmsg dest="logndisplay">
            %service% outage identified on interface %interface% from location %parm[perspective]% has cleared.
        </logmsg>
   <severity>Normal</severity>
   <alarm-data reduction-key="%uei%:%parm[perspective]%:%nodeid%:%interface%:%service%" alarm-type="2" clear-key="uei.opennms.org/perspective/nodes/nodeLostService:%parm[perspective]%:%nodeid%:%interface%:%service%" auto-clean="false"/>
</event>','Normal', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (145, 19, 'uei.opennms.org/reportd/reportRunFailed', 'OpenNMS-defined Reportd Event: reportRunFailed', 'A message from the Reportd reporting service that a report has failed to run:
            <p>The report with name %parm[reportName]% failed to run for the following reason: %parm[reason]% </p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/reportd/reportRunFailed</uei>
   <event-label>OpenNMS-defined Reportd Event: reportRunFailed</event-label>
   <descr>A message from the Reportd reporting service that a report has failed to run:
            &lt;p>The report with name %parm[reportName]% failed to run for the following reason: %parm[reason]% &lt;/p></descr>
   <logmsg dest="logndisplay">
            &lt;p>The report with name %parm[reportName]% failed to run.&lt;/p>
        </logmsg>
   <severity>Minor</severity>
   <alarm-data reduction-key="%uei%:%nodeid%:%dpname%:%parm[reportName]%" alarm-type="3" auto-clean="false"/>
</event>','Minor', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (146, 19, 'uei.opennms.org/reportd/reportDeliveryFailed', 'OpenNMS-defined Reportd Event: reportDeliveryFailed', 'A message from the Reportd delivery service that a report could not be delivered:
            <p>The report with name %parm[reportName]% could not be delivered for the following reason:
            %parm[reason]% </p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/reportd/reportDeliveryFailed</uei>
   <event-label>OpenNMS-defined Reportd Event: reportDeliveryFailed</event-label>
   <descr>A message from the Reportd delivery service that a report could not be delivered:
            &lt;p>The report with name %parm[reportName]% could not be delivered for the following reason:
            %parm[reason]% &lt;/p></descr>
   <logmsg dest="logndisplay">
            &lt;p>The report with name %parm[reportName]% could not be delivered.&lt;/p>
        </logmsg>
   <severity>Minor</severity>
   <alarm-data reduction-key="%uei%:%nodeid%:%dpname%:%parm[reportName]%" alarm-type="3" auto-clean="false"/>
</event>','Minor', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (147, 20, 'DISCARD-MATCHING-MESSAGES', 'OpenNMS-defined DISCARD-MATCHING-MESSAGES', 'DISCARD-MATCHING-MESSAGES is used in the syslogd to generate events that
            have no matching events. This event is not persisted by default.', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>DISCARD-MATCHING-MESSAGES</uei>
   <event-label>OpenNMS-defined DISCARD-MATCHING-MESSAGES</event-label>
   <descr>DISCARD-MATCHING-MESSAGES is used in the syslogd to generate events that
            have no matching events. This event is not persisted by default.</descr>
   <logmsg dest="donotpersist">
            &lt;p>DISCARD-MATCHING-MESSAGES.&lt;/p>
        </logmsg>
   <severity>Normal</severity>
</event>','Normal', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (148, 21, 'uei.opennms.org/troubleTicket/create', 'OpenNMS-defined trouble ticket event: A request has been made to create a trouble ticket', 'This event is generated to invoke the asynchronous Trouble Ticket API in OpenNMS
            for creating a new trouble ticket.', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/troubleTicket/create</uei>
   <event-label>OpenNMS-defined trouble ticket event: A request has been made to create a trouble ticket</event-label>
   <descr>This event is generated to invoke the asynchronous Trouble Ticket API in OpenNMS
            for creating a new trouble ticket.</descr>
   <logmsg dest="logndisplay">
            A request has been generated to create a trouble ticket.
        </logmsg>
   <severity>Normal</severity>
</event>','Normal', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (149, 21, 'uei.opennms.org/troubleTicket/update', 'OpenNMS-defined trouble ticket event: A request has been made to update a trouble ticket', 'This event is generated to invoke the asynchronous Trouble Ticket API in OpenNMS
            for updating an existing trouble ticket.', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/troubleTicket/update</uei>
   <event-label>OpenNMS-defined trouble ticket event: A request has been made to update a trouble ticket</event-label>
   <descr>This event is generated to invoke the asynchronous Trouble Ticket API in OpenNMS
            for updating an existing trouble ticket.</descr>
   <logmsg dest="logndisplay">
            A request has been generated to update a trouble ticket.
        </logmsg>
   <severity>Normal</severity>
</event>','Normal', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (150, 21, 'uei.opennms.org/troubleTicket/close', 'OpenNMS-defined trouble ticket event: A request has been made to close a trouble ticket', 'This event is generated to invoke the asynchronous Trouble Ticket API in OpenNMS
            for closing an existing trouble ticket.', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/troubleTicket/close</uei>
   <event-label>OpenNMS-defined trouble ticket event: A request has been made to close a trouble ticket</event-label>
   <descr>This event is generated to invoke the asynchronous Trouble Ticket API in OpenNMS
            for closing an existing trouble ticket.</descr>
   <logmsg dest="logndisplay">
            A request has been generated to close a trouble ticket.
        </logmsg>
   <severity>Normal</severity>
</event>','Normal', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (151, 21, 'uei.opennms.org/troubleTicket/cancel', 'OpenNMS-defined trouble ticket event: A request has been made to cancel a trouble ticket', 'This event is generated to invoke the asynchronous Trouble Ticket API in OpenNMS
            for canceling an existing trouble ticket.', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/troubleTicket/cancel</uei>
   <event-label>OpenNMS-defined trouble ticket event: A request has been made to cancel a trouble ticket</event-label>
   <descr>This event is generated to invoke the asynchronous Trouble Ticket API in OpenNMS
            for canceling an existing trouble ticket.</descr>
   <logmsg dest="logndisplay">
            A request has been generated to cancel a trouble ticket.
        </logmsg>
   <severity>Normal</severity>
</event>','Normal', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (152, 21, 'uei.opennms.org/troubleTicket/communicationError', 'OpenNMS-defined trouble ticket event: A communication error occurred', 'This event is generated when OpenNMS is unable to retrive, save or update a ticket
            via the Trouble Ticket API. Communications failed with reason: %parm[reason]%.', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/troubleTicket/communicationError</uei>
   <event-label>OpenNMS-defined trouble ticket event: A communication error occurred</event-label>
   <descr>This event is generated when OpenNMS is unable to retrive, save or update a ticket
            via the Trouble Ticket API. Communications failed with reason: %parm[reason]%.</descr>
   <logmsg dest="logndisplay">
            A communication error occurred between OpenNMS and the Trouble Ticket system.
        </logmsg>
   <severity>Warning</severity>
</event>','Warning', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (153, 22, 'uei.opennms.org/api/tl1d/message/autonomous', 'OpenNMS-defined Autonomous TL1 message', 'This is a TL1 autonomous message delivered for host: %host%.<p>

            <p>Message: %parm[raw-message]% </p>
            <p>Alarm Code: %parm[alarm-code]% </p>
            <p>ATAG: %parm[atag]% </p>
            <p>Verb: %parm[verb]% </p>
            <p>Auto Block: %parm[autoblock]% </p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/api/tl1d/message/autonomous</uei>
   <event-label>OpenNMS-defined Autonomous TL1 message</event-label>
   <descr>This is a TL1 autonomous message delivered for host: %host%.&lt;p>

            &lt;p>Message: %parm[raw-message]% &lt;/p>
            &lt;p>Alarm Code: %parm[alarm-code]% &lt;/p>
            &lt;p>ATAG: %parm[atag]% &lt;/p>
            &lt;p>Verb: %parm[verb]% &lt;/p>
            &lt;p>Auto Block: %parm[autoblock]% &lt;/p></descr>
   <logmsg dest="logndisplay">
            &lt;p> %host%:%parm[verb]%:%parm[autoblock]% &lt;/p>
        </logmsg>
   <severity>Warning</severity>
   <alarm-data reduction-key="%uei%:%host%:%parm[aid]%" alarm-type="3" auto-clean="false"/>
</event>','Warning', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (154, 23, 'MATCH-ANY-UEI', 'OpenNMS-defined event: MATCH-ANY-UEI', '<p>This UEI will never be generated, but exists
            so that notifications can match any UEI for a
            particular filter rule. Useful to see all events for
            a particular node via notifications.
            </p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>MATCH-ANY-UEI</uei>
   <event-label>OpenNMS-defined event: MATCH-ANY-UEI</event-label>
   <descr>&lt;p>This UEI will never be generated, but exists
            so that notifications can match any UEI for a
            particular filter rule. Useful to see all events for
            a particular node via notifications.
            &lt;/p></descr>
   <logmsg dest="logonly">
            MATCH-ANY-UEI event.
        </logmsg>
   <severity>Indeterminate</severity>
</event>','Indeterminate', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (155, 23, 'uei.opennms.org/default/trap', 'OpenNMS-defined default event: trap', '<p>An SNMP Trap (%snmp%) with no matching configuration was received from interface %interface%.</p>
            <p>The trap included the
            following variable bindings:</p> <p>%parm[all]%</p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/default/trap</uei>
   <event-label>OpenNMS-defined default event: trap</event-label>
   <descr>&lt;p>An SNMP Trap (%snmp%) with no matching configuration was received from interface %interface%.&lt;/p>
            &lt;p>The trap included the
            following variable bindings:&lt;/p> &lt;p>%parm[all]%&lt;/p></descr>
   <logmsg dest="logndisplay">An SNMP Trap with no matching configuration was received from interface
            %interface%.
        </logmsg>
   <severity>Indeterminate</severity>
   <alarm-data reduction-key="%uei%:%dpname%:%nodeid%:%interface%:%id%:%generic%:%specific%" alarm-type="3"/>
</event>','Indeterminate', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (156, 23, 'uei.opennms.org/default/event', 'OpenNMS-defined default event: event', '<p>An event with no matching configuration was received from interface %interface%. This event
            included the following parameters:
            %parm[all]%</p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <uei>uei.opennms.org/default/event</uei>
   <event-label>OpenNMS-defined default event: event</event-label>
   <descr>&lt;p>An event with no matching configuration was received from interface %interface%. This event
            included the following parameters:
            %parm[all]%&lt;/p></descr>
   <logmsg dest="logndisplay">An event with no matching configuration was received from interface %interface%.
        </logmsg>
   <severity>Indeterminate</severity>
   <alarm-data reduction-key="%uei%:%dpname%:%nodeid%:%interface%" alarm-type="3"/>
</event>','Indeterminate', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');
INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content,severity, created_time, last_modified, modified_by) VALUES (157, 23, 'uei.opennms.org/generic/traps/EnterpriseDefault', 'OpenNMS-defined trap event: EnterpriseDefault', '<p>This is the default event format used when an enterprise specific event (trap) is received for
            which no format has been configured
            (i.e. no event definition exists).</p> <p>The total number of arguments received with the trap:
            %parm[##]%.</p>
            <p>They were:<p> <p>%parm[all]%<p>
            <p>Here is a "mask" element definition that matches this
            event, for use in event configuration files:<br/>
            <blockquote>
            &lt;mask&gt;<br/>
            &nbsp;&nbsp;&lt;maskelement&gt;<br/>
            &nbsp;&nbsp;&nbsp;&nbsp;&lt;mename&gt;id&lt;/mename&gt;<br/>
            &nbsp;&nbsp;&nbsp;&nbsp;&lt;mevalue&gt;%id%&lt;/mevalue&gt;<br/>
            &nbsp;&nbsp;&lt;/maskelement&gt;<br/>
            &nbsp;&nbsp;&lt;maskelement&gt;<br/>
            &nbsp;&nbsp;&nbsp;&nbsp;&lt;mename&gt;generic&lt;/mename&gt;<br/>
            &nbsp;&nbsp;&nbsp;&nbsp;&lt;mevalue&gt;%generic%&lt;/mevalue&gt;<br/>
            &nbsp;&nbsp;&lt;/maskelement&gt;<br/>
            &nbsp;&nbsp;&lt;maskelement&gt;<br/>
            &nbsp;&nbsp;&nbsp;&nbsp;&lt;mename&gt;specific&lt;/mename&gt;<br/>
            &nbsp;&nbsp;&nbsp;&nbsp;&lt;mevalue&gt;%specific%&lt;/mevalue&gt;<br/>
            &nbsp;&nbsp;&lt;/maskelement&gt;<br/>
            &lt;/mask&gt;
            </blockquote>
            <p>', true, '<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
   <mask>
      <maskelement>
         <mename>generic</mename>
         <mevalue>6</mevalue>
      </maskelement>
   </mask>
   <uei>uei.opennms.org/generic/traps/EnterpriseDefault</uei>
   <event-label>OpenNMS-defined trap event: EnterpriseDefault</event-label>
   <descr>&lt;p>This is the default event format used when an enterprise specific event (trap) is received for
            which no format has been configured
            (i.e. no event definition exists).&lt;/p> &lt;p>The total number of arguments received with the trap:
            %parm[##]%.&lt;/p>
            &lt;p>They were:&lt;p> &lt;p>%parm[all]%&lt;p>
            &lt;p>Here is a &quot;mask&quot; element definition that matches this
            event, for use in event configuration files:&lt;br/>
            &lt;blockquote>
            &amp;lt;mask&amp;gt;&lt;br/>
            &amp;nbsp;&amp;nbsp;&amp;lt;maskelement&amp;gt;&lt;br/>
            &amp;nbsp;&amp;nbsp;&amp;nbsp;&amp;nbsp;&amp;lt;mename&amp;gt;id&amp;lt;/mename&amp;gt;&lt;br/>
            &amp;nbsp;&amp;nbsp;&amp;nbsp;&amp;nbsp;&amp;lt;mevalue&amp;gt;%id%&amp;lt;/mevalue&amp;gt;&lt;br/>
            &amp;nbsp;&amp;nbsp;&amp;lt;/maskelement&amp;gt;&lt;br/>
            &amp;nbsp;&amp;nbsp;&amp;lt;maskelement&amp;gt;&lt;br/>
            &amp;nbsp;&amp;nbsp;&amp;nbsp;&amp;nbsp;&amp;lt;mename&amp;gt;generic&amp;lt;/mename&amp;gt;&lt;br/>
            &amp;nbsp;&amp;nbsp;&amp;nbsp;&amp;nbsp;&amp;lt;mevalue&amp;gt;%generic%&amp;lt;/mevalue&amp;gt;&lt;br/>
            &amp;nbsp;&amp;nbsp;&amp;lt;/maskelement&amp;gt;&lt;br/>
            &amp;nbsp;&amp;nbsp;&amp;lt;maskelement&amp;gt;&lt;br/>
            &amp;nbsp;&amp;nbsp;&amp;nbsp;&amp;nbsp;&amp;lt;mename&amp;gt;specific&amp;lt;/mename&amp;gt;&lt;br/>
            &amp;nbsp;&amp;nbsp;&amp;nbsp;&amp;nbsp;&amp;lt;mevalue&amp;gt;%specific%&amp;lt;/mevalue&amp;gt;&lt;br/>
            &amp;nbsp;&amp;nbsp;&amp;lt;/maskelement&amp;gt;&lt;br/>
            &amp;lt;/mask&amp;gt;
            &lt;/blockquote>
            &lt;p></descr>
   <logmsg dest="logndisplay">
            Received unformatted enterprise event (enterprise:%id% generic:%generic% specific:%specific%). %parm[##]%
            args: %parm[all]%
        </logmsg>
   <severity>Normal</severity>
   <alarm-data reduction-key="%source%:%snmphost%:%id%:%generic%:%specific%" alarm-type="3" auto-clean="true"/>
</event>','Normal', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system-migration');

ALTER SEQUENCE eventconf_events_id_seq RESTART WITH 158;
