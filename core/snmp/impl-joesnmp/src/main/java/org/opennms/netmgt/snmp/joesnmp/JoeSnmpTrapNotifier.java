/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.snmp.joesnmp;

import java.net.InetAddress;

import org.opennms.netmgt.snmp.TrapNotificationListener;
import org.opennms.netmgt.snmp.TrapProcessorFactory;
import org.opennms.protocols.snmp.SnmpOctetString;
import org.opennms.protocols.snmp.SnmpPduPacket;
import org.opennms.protocols.snmp.SnmpPduTrap;
import org.opennms.protocols.snmp.SnmpTrapHandler;
import org.opennms.protocols.snmp.SnmpTrapSession;

public class JoeSnmpTrapNotifier implements SnmpTrapHandler {
    
    private TrapProcessorFactory m_trapProcessorFactory;
    private TrapNotificationListener m_listener;

    public JoeSnmpTrapNotifier(TrapNotificationListener listener, TrapProcessorFactory factory) {
        m_listener = listener;
        m_trapProcessorFactory = factory;
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
        m_listener.trapReceived(new V2TrapInformation(agent, new String(community.getString()), pdu, m_trapProcessorFactory.createTrapProcessor()));
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
        m_listener.trapReceived(new V1TrapInformation(agent, new String(community.getString()), pdu, m_trapProcessorFactory.createTrapProcessor()));
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