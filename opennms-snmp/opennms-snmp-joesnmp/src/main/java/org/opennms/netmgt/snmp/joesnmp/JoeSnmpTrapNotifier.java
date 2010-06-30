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
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.snmp.joesnmp;

import java.net.InetAddress;

import org.opennms.netmgt.snmp.TrapNotificationListener;
import org.opennms.netmgt.snmp.TrapProcessorFactory;
import org.opennms.protocols.snmp.SnmpOctetString;
import org.opennms.protocols.snmp.SnmpPduPacket;
import org.opennms.protocols.snmp.SnmpPduTrap;
import org.opennms.protocols.snmp.SnmpTrapHandler;
import org.opennms.protocols.snmp.SnmpTrapSession;

/**
 * <p>JoeSnmpTrapNotifier class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class JoeSnmpTrapNotifier implements SnmpTrapHandler {
    
    private TrapProcessorFactory m_trapProcessorFactory;
    private TrapNotificationListener m_listener;

    /**
     * <p>Constructor for JoeSnmpTrapNotifier.</p>
     *
     * @param listener a {@link org.opennms.netmgt.snmp.TrapNotificationListener} object.
     * @param factory a {@link org.opennms.netmgt.snmp.TrapProcessorFactory} object.
     */
    public JoeSnmpTrapNotifier(TrapNotificationListener listener, TrapProcessorFactory factory) {
        m_listener = listener;
        m_trapProcessorFactory = factory;
    }

    /**
     * {@inheritDoc}
     *
     * <P>
     * Process the recieved SNMP v2c trap that was received by the underlying
     * trap session.
     * </P>
     */
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
     */
    public void snmpReceivedTrap(SnmpTrapSession session, InetAddress agent,
            int port, SnmpOctetString community, SnmpPduTrap pdu) {
        m_listener.trapReceived(new V1TrapInformation(agent, new String(community.getString()), pdu, m_trapProcessorFactory.createTrapProcessor()));
    }
    
    /**
     * {@inheritDoc}
     *
     * <P>
     * Processes an error condition that occurs in the SnmpTrapSession. The
     * errors are logged and ignored by the trapd class.
     * </P>
     */
    public void snmpTrapSessionError(SnmpTrapSession session, int error, Object ref) {
        String msg = (ref != null ? ref.toString() : null);
        m_listener.trapError(error, msg);
    }


}
