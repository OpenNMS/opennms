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
package org.opennms.netmgt.eventd;

/**
 * This class is a repository for constant, static information concerning
 * Eventd.
 *
 * @author <A HREF="mailto:weave@oculan.com">Sowmya Nataraj </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS.org </A>
 * 
 * @deprecated Use standard DAO calls instead of using JDBC statements
 */
public abstract class EventdConstants {

    /**
     * The SQL insertion string used by eventd to store the event information
     * into the database.
     */
    public static final String SQL_DB_INS_EVENT = "INSERT into events (eventID, eventUei, nodeID, eventTime, " +
    		"eventHost, ipAddr, systemId, eventSnmpHost, serviceID, eventSnmp, eventParms, eventCreateTime, eventDescr, " +
    		"eventLoggroup, eventLogmsg, eventLog, eventDisplay, eventSeverity, eventPathOutage, eventCorrelation, eventSuppressedCount, " +
    		"eventOperInstruct, eventAutoAction, eventOperAction, eventOperActionMenuText, eventNotification, eventTticket, eventTticketState, " +
    		"eventForward, eventMouseOverText, eventAckUser, eventAckTime, eventSource, ifIndex) " +
    		"values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    /**
     * The SQL statement necessary to convert the service name into a service id
     * using the distributed poller database.
     */
    public static final String SQL_DB_SVCNAME_TO_SVCID = "SELECT serviceID FROM service WHERE serviceName = ?";

    /**
     * The SQL statement necessary to convert the event host into a hostname
     * using the 'ipinterface' table.
     */
    public static final String SQL_DB_HOSTIP_TO_HOSTNAME = "SELECT ipHostname FROM ipinterface WHERE nodeId = ? AND ipAddr = ?";

}
