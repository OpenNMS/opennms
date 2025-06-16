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
package org.opennms.netmgt.snmp.joesnmp;

import java.net.InetAddress;

import org.opennms.netmgt.snmp.TrapNotificationListener;
import org.opennms.protocols.snmp.SnmpOctetString;
import org.opennms.protocols.snmp.SnmpPduPacket;
import org.opennms.protocols.snmp.SnmpPduTrap;
import org.opennms.protocols.snmp.SnmpTrapHandler;
import org.opennms.protocols.snmp.SnmpTrapSession;

public class JoeSnmpTrapNotifier implements SnmpTrapHandler {
    
    private TrapNotificationListener m_listener;

    public JoeSnmpTrapNotifier(TrapNotificationListener listener) {
        m_listener = listener;
    }

    /**
     * <P>
     * Process the recieved SNMP v2c trap that was received by the underlying
     * trap session.
     * </P>
     * 
     * @param session
     *            The trap session that received the datagram.
     * @param agent
     *            The remote agent that sent the datagram.
     * @param port
     *            The remmote port the trap was sent from.
     * @param community
     *            The community string contained in the message.
     * @param pdu
     *            The protocol data unit containing the data
     * 
     */
    @Override
    public void snmpReceivedTrap(SnmpTrapSession session, InetAddress agent,
            int port, SnmpOctetString community, SnmpPduPacket pdu) {
        m_listener.trapReceived(new V2TrapInformation(agent, new String(community.getString()), pdu));
    }
    
    /**
     * <P>
     * Process the recieved SNMP v1 trap that was received by the underlying
     * trap session.
     * </P>
     * 
     * @param session
     *            The trap session that received the datagram.
     * @param agent
     *            The remote agent that sent the datagram.
     * @param port
     *            The remmote port the trap was sent from.
     * @param community
     *            The community string contained in the message.
     * @param pdu
     *            The protocol data unit containing the data
     * 
     */
    @Override
    public void snmpReceivedTrap(SnmpTrapSession session, InetAddress agent,
            int port, SnmpOctetString community, SnmpPduTrap pdu) {
        m_listener.trapReceived(new V1TrapInformation(agent, new String(community.getString()), pdu));
    }
    
    /**
     * <P>
     * Processes an error condition that occurs in the SnmpTrapSession. The
     * errors are logged and ignored by the trapd class.
     * </P>
     */
    @Override
    public void snmpTrapSessionError(SnmpTrapSession session, int error, Object ref) {
        String msg = (ref != null ? ref.toString() : null);
        m_listener.trapError(error, msg);
    }


}