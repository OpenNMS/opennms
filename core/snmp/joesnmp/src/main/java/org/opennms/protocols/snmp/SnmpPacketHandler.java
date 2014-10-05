/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
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

package org.opennms.protocols.snmp;

import java.net.DatagramPacket;
import java.net.InetAddress;

/**
 * The SnmpPacketHandler is implemented by an object that wishes to be notified
 * when SNMP data is received from an agent. In addition, if an exception occurs
 * or an agent fails to respond then the object must handle those error
 * conditions.
 * 
 * @author <a href="mailto:weave@oculan.com">Brian Weaver</a>
 * @author Sowmya
 * 
 * @see SnmpPortal
 * @see SnmpSession
 * @see SnmpTrapSession
 */
interface SnmpPacketHandler {
    /**
     * <P>
     * This method is used to process received SNMP messages in the standard V1 &
     * V2 format. The only SNMP message not processed by this callback is an
     * SNMPv1 Trap message.
     * </P>
     * 
     * <P>
     * For any class that implements this interface the processing time should
     * be kept as small as possible. Any time spent in the handler method is
     * time that the SnmpPortal class is not receiveing datagrams. This can
     * result in lost packets during a high traffic time.
     * </P>
     * 
     * @param agent
     *            The remote sender of the message
     * @param port
     *            The port of the remote agent
     * @param version
     *            The SNMP version of the received message.
     * @param community
     *            The community string in the message.
     * @param pduType
     *            The PDU implicit command value.
     * @param pdu
     *            The Protocol Data Unit (PDU).
     * 
     * @see SnmpPortal.Receiver#run
     * @see SnmpPortal#handlePkt
     */
    void processSnmpMessage(InetAddress agent, int port, SnmpInt32 version, SnmpOctetString community, int pduType, SnmpPduPacket pdu) throws SnmpPduEncodingException;

    /**
     * <P>
     * This method is use to handle SNMPv1 trap exclusively. Since the
     * SnmpPduRequest & SnmpPduTrap do not share a common base class, a separate
     * method is used to handle the v1 traps.
     * </P>
     * 
     * <P>
     * Since this method only handles SNMPv1 traps the version and pdu type are
     * not passed as parameters to the object.
     * </P>
     * 
     * @param agent
     *            The remote sender of the message
     * @param port
     *            The port of the remote agent.
     * @param community
     *            The community string in the message.
     * @param pdu
     *            The SNMP trap Protocol Data Unit.
     * 
     * @see SnmpPduTrap
     * 
     */
    void processSnmpTrap(InetAddress agent, int port, SnmpOctetString community, SnmpPduTrap pdu) throws SnmpPduEncodingException;

    /**
     * <P>
     * Any messages received that are not properly formatted are passed to this
     * handler. The handler can choose to ignore the messages or to do further
     * processing to determine if it is encoded using a different encoder.
     * </P>
     * 
     * @param pkt
     *            The datagram packet that failed to parse.
     * 
     */
    void processBadDatagram(DatagramPacket pkt);

    /**
     * <P>
     * Any exception that is caught by the SnmpPortal class during the receipt
     * of an SNMP message is handled by this method. Methods that are not
     * forwarded are SnmpPduEncodingExceptions and AsnDecodingExceptions.
     * </P>
     * 
     * <P>
     * Exceptions that may need to be processed by the handler include
     * IOExceptions or other socket related errors.
     * </P>
     * 
     * @param e
     *            The caught exception.
     * 
     * @see SnmpPortal
     */
    void processException(Exception e);
}
