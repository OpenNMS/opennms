/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.eventd.adaptors.udp;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Log;
import org.xml.sax.InputSource;

/**
 * 
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * @author <a href="http://www.oculan.com">Oculan Corporation </a>
 * 
 */
final class UdpReceivedEvent {
    /**
     * The received XML event, decoded using the US-ASCII encoding.
     */
    private String m_eventXML;

    /**
     * The decoded event document.
     */
    private Log m_log;

    /**
     * The internet addrress of the sending agent.
     */
    private InetAddress m_sender;

    /**
     * The port of the agent on the remote system.
     */
    private int m_port;

    /**
     * The list of event that have been acknowledged.
     */
    private List<Event> m_ackEvents;

    /**
     * Private constructor to prevent the used of <em>new</em> except by the
     * <code>make</code> method.
     */
    private UdpReceivedEvent() {
        // constructor not supported except through make method!
    }

    /**
     * Constructs a new event encapsulation instance based upon the information
     * passed to the method. The passed datagram data is decoded into a string
     * using the <tt>US-ASCII</tt> character encoding.
     * 
     * @param packet
     *            The datagram received from the remote agent.
     */
    static UdpReceivedEvent make(DatagramPacket packet) {
        return make(packet.getAddress(), packet.getPort(), packet.getData(), packet.getLength());
    }

    /**
     * Constructs a new event encapsulation instance based upon the information
     * passed to the method. The passed byte array is decoded into a string
     * using the <tt>US-ASCII</tt> character encoding.
     * 
     * @param addr
     *            The remote agent's address.
     * @param port
     *            The remote agent's port
     * @param data
     *            The XML data in US-ASCII encoding.
     * @param len
     *            The length of the XML data in the buffer.
     */
    static UdpReceivedEvent make(InetAddress addr, int port, byte[] data, int len) {
        UdpReceivedEvent e = new UdpReceivedEvent();
        e.m_sender = addr;
        e.m_port = port;
        e.m_eventXML = new String(Arrays.copyOf(data, data.length), 0, len, StandardCharsets.US_ASCII);
        e.m_ackEvents = new ArrayList<Event>(16);
        e.m_log = null;
        return e;
    }

    /**
     * Decodes the XML package from the remote agent. If an error occurs or the
     * datagram had malformed XML then an exception is generated.
     * 
     * @return The toplevel <code>Log</code> element of the XML document.
     *
     */
    Log unmarshal() {
        if (m_log == null) {
        	final InputStream is = new ByteArrayInputStream(m_eventXML.getBytes());
            m_log = JaxbUtils.unmarshal(Log.class, new InputSource(is));
        }
        return m_log;
    }

    /**
     * Adds the event to the list of events acknowledged in this event XML
     * document.
     * 
     * @param e
     *            The event to acknowledge.
     */
    void ackEvent(Event e) {
        if (!m_ackEvents.contains(e)) {
            m_ackEvents.add(e);
        }
    }

    /**
     * Returns the raw XML data as a string.
     */
    String getXmlData() {
        return m_eventXML;
    }

    /**
     * Returns the sender's address.
     */
    InetAddress getSender() {
        return m_sender;
    }

    /**
     * Returns the sender's port
     */
    int getPort() {
        return m_port;
    }

    /**
     * Get the acknowledged events
     *
     * @return a {@link java.util.List} object.
     */
    public List<Event> getAckedEvents() {
        return m_ackEvents;
    }

    /**
     * {@inheritDoc}
     *
     * Returns true if the instance matches the object based upon the remote
     * agent's address &amp; port. If the passed instance is from the same agent
     * then it is considered equal.
     */
    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof UdpReceivedEvent) {
            UdpReceivedEvent e = (UdpReceivedEvent) o;
            return (this == e || (m_port == e.m_port && m_sender.equals(e.m_sender)));
        }
        return false;
    }

    /**
     * Returns the hash code of the instance. The hash code is computed by
     * taking the bitwise XOR of the port and the agent's internet address hash
     * code.
     *
     * @return The 32-bit has code for the instance.
     */
    @Override
    public int hashCode() {
        return (m_port ^ m_sender.hashCode());
    }
}
