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
package org.opennms.protocols.snmp;

import java.net.InetAddress;

/**
 * <P>
 * The SnmpTrapHandler interface is implemented by an object that wishs to
 * receive callbacks when a SNMP trap protocol data unit is received from an
 * agent.
 * </P>
 * 
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * @version 1.1.1.1 2001/11/11 17:27:22
 * 
 */
public interface SnmpTrapHandler {
    /**
     * <P>
     * This method is defined to handle SNMPv2 traps that are received by the
     * session. The parameters allow teh handler to determine the host, port,
     * and community string of the received PDU
     * </P>
     * 
     * @param session
     *            The SNMP session
     * @param agent
     *            The remote sender
     * @param port
     *            The remote senders port
     * @param community
     *            The community string
     * @param pdu
     *            The SNMP pdu
     * 
     */
    void snmpReceivedTrap(SnmpTrapSession session, InetAddress agent, int port, SnmpOctetString community, SnmpPduPacket pdu);

    /**
     * <P>
     * This method is define to handle SNMPv1 traps that are received by the
     * session. The parameters allow the handler to determine the host, port,
     * and community string of the received PDU.
     * </P>
     * 
     * @param session
     *            The SNMP session
     * @param agent
     *            The Trap sender
     * @param port
     *            The port of the sender
     * @param community
     *            The community string
     * @param pdu
     *            The SNMP trap pdu
     * 
     */
    void snmpReceivedTrap(SnmpTrapSession session, InetAddress agent, int port, SnmpOctetString community, SnmpPduTrap pdu);

    /**
     * <P>
     * This method is invoked if an error occurs in the trap session. The error
     * code that represents the failure will be passed in the second parameter,
     * 'error'. The error codes can be found in the class SnmpTrapSession class.
     * </P>
     * 
     * <P>
     * If a particular PDU is part of the error condition it will be passed in
     * the third parameter, 'pdu'. The pdu will be of the type SnmpPduRequest or
     * SnmpPduTrap object. The handler should use the "instanceof" operator to
     * determine which type the object is. Also, the object may be null if the
     * error condition is not associated with a particular PDU.
     * </P>
     * 
     * @param session
     *            The SNMP Trap Session
     * @param error
     *            The error condition value.
     * @param ref
     *            The PDU reference, or potentially null. It may also be an
     *            exception.
     * 
     * 
     */
    void snmpTrapSessionError(SnmpTrapSession session, int error, Object ref);
}
